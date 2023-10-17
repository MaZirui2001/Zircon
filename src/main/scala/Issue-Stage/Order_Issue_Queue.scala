import chisel3._
import chisel3.util._
import Issue_Queue_Pack._
import Inst_Pack._

// LUT: 1017 FF: 780
class Order_Issue_Queue_IO(n: Int) extends Bundle{
    // input from dispatch
    val insts_dispatch  = Input(Vec(4, new inst_pack_t))
    val insert_num      = Input(UInt(3.W))
    val prj_ready       = Input(Vec(4, Bool()))
    val prk_ready       = Input(Vec(4, Bool()))
    val queue_ready     = Output(Bool())

    // input from wakeup
    val wake_preg       = Input(Vec(4, UInt(6.W)))

    // input for issue ack
    val issue_ack       = Input(Bool())

    // output for issue
    val insts_issue     = Output(new issue_queue_t)
    val issue_req       = Output(Bool())

    // output for dispatch
    val prd_queue       = Output(Vec(n+1, UInt(6.W)))
    val full            = Output(Bool())

    val stall           = Input(Bool())
    val flush           = Input(Bool())
}
class Order_Issue_Queue(n: Int) extends Module {
    val io  = IO(new Order_Issue_Queue_IO(n))
    val queue = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new issue_queue_t))))
    val tail = RegInit(0.U((log2Ceil(n)+1).W))

    val insert_num = io.insert_num
    val tail_pop = Wire(UInt((log2Ceil(n)+1).W))
    val full = tail_pop >= n.U - insert_num

    io.queue_ready := !full
    io.full := full
    val insts_dispatch = io.insts_dispatch

    val queue_temp = Wire(Vec(n+1, new issue_queue_t))
    val queue_next = Wire(Vec(n, new issue_queue_t))
    
    io.prd_queue := 0.U.asTypeOf(Vec(n+1, UInt(6.W)))
    for(i <- 0 until n){
        queue_next(i) := Mux(i.asUInt < tail_pop, Mux(io.issue_ack, queue_temp(i+1), queue_temp(i)), 0.U.asTypeOf(new issue_queue_t))
        io.prd_queue(i) := Mux(queue_next(i).inst.rd_valid, queue_next(i).inst.prd, 0.U)
    }
    io.prd_queue(n) := Mux(queue(0).inst.rd_valid, queue(0).inst.prd, 0.U)
    // wake up 
    queue_temp := 0.U.asTypeOf(Vec(n+1, new issue_queue_t))
    for(i <- 0 until n){
        queue_temp(i).inst := queue(i).inst
        queue_temp(i).prj_waked := queue(i).prj_waked | Wake_Up(io.wake_preg, queue(i).inst.prj)
        queue_temp(i).prk_waked := queue(i).prk_waked | Wake_Up(io.wake_preg, queue(i).inst.prk)
    }
    // issue
    // val next_mask = Mux(io.issue_ack, ~0.U(n.W), 0.U(n.W))
    tail_pop := tail - io.issue_ack

    for(i <- 0 until n){
        queue(i).inst := Mux(i.asUInt < tail_pop, queue_next(i).inst, insts_dispatch(i.asUInt - tail_pop))
        queue(i).prj_waked := Mux(i.asUInt < tail_pop, queue_next(i).prj_waked, io.prj_ready(i.asUInt - tail_pop))
        queue(i).prk_waked := Mux(i.asUInt < tail_pop, queue_next(i).prk_waked, io.prk_ready(i.asUInt - tail_pop))
    }
    tail := Mux(io.flush, 0.U, Mux(io.stall, tail_pop, tail_pop + Mux(io.queue_ready, insert_num, 0.U)))

    // output
    io.insts_issue := queue(0)
    io.issue_req := tail =/= 0.U && queue(0).prj_waked && queue(0).prk_waked
}
// object Order_Issue_Queue extends App {
//     emitVerilog(new Order_Issue_Queue(8), Array("-td", "build/"))
// }