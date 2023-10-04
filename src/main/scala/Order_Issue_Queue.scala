import chisel3._
import chisel3.util._
import Issue_Queue_Pack._
import Inst_Pack._

// LUT: 932 FF: 732
class Order_Issue_Queue_IO extends Bundle{
    // input from dispatch
    val insts_dispatch  = Input(Vec(4, new inst_pack_t))
    val insts_valid     = Input(Vec(4, Bool()))
    val queue_ready     = Output(Bool())

    // input from wakeup
    val wake_preg       = Input(Vec(4, UInt(6.W)))

    // input for issue ack
    val issue_ack       = Input(Bool())

    // output for issue
    val insts_issue     = Output(new issue_queue_t)
    val issue_req       = Output(Bool())
}
class Order_Issue_Queue(n: Int) extends Module {
    val io = IO(new Order_Issue_Queue_IO)
    val queue = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new issue_queue_t))))
    val tail = RegInit(0.U((log2Ceil(n)+1).W))

    val insert_num = PopCount(io.insts_valid)
    val tail_pop = Wire(UInt((log2Ceil(n)+1).W))
    val full = tail_pop >= n.U - insert_num

    io.queue_ready := !full
    val insts_dispatch = io.insts_dispatch

    val queue_temp = Wire(Vec(n+1, new issue_queue_t))
    
    // wake up 
    for(i <- 0 until n){
        queue_temp(i).inst := queue(i).inst
        queue_temp(i).prj_waked := queue(i).prj_waked | Wake_Up(io.wake_preg, queue(i).inst.prj)
        queue_temp(i).prk_waked := queue(i).prk_waked | Wake_Up(io.wake_preg, queue(i).inst.prk)
    }
    queue_temp(n) := 0.U.asTypeOf(new issue_queue_t)
    // issue
    // val next_mask = Mux(io.issue_ack, ~0.U(n.W), 0.U(n.W))
    tail_pop := tail - io.issue_ack

    for(i <- 0 until n){
        queue(i).inst := Mux(i.asUInt < tail_pop, Mux(io.issue_ack, queue_temp(i+1).inst, queue_temp(i).inst), insts_dispatch(i.asUInt - tail_pop))
        queue(i).prj_waked := Mux(i.asUInt < tail_pop, Mux(io.issue_ack, queue_temp(i+1).prj_waked, queue_temp(i).prj_waked), 
                                                       ~insts_dispatch(i.asUInt - tail_pop).rj_valid | insts_dispatch(i.asUInt - tail_pop).rj === 0.U)
        queue(i).prk_waked := Mux(i.asUInt < tail_pop, Mux(io.issue_ack, queue_temp(i+1).prk_waked, queue_temp(i).prk_waked), 
                                                       ~insts_dispatch(i.asUInt - tail_pop).rk_valid | insts_dispatch(i.asUInt - tail_pop).rk === 0.U)
    }
    tail := tail_pop + Mux(io.queue_ready, insert_num, 0.U)

    // output
    io.insts_issue := queue(0)
    io.issue_req := tail =/= 0.U && queue(0).prj_waked && queue(0).prk_waked
}
object Order_Issue_Queue extends App {
    emitVerilog(new Order_Issue_Queue(8), Array("-td", "build/"))
}