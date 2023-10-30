import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._

// LUT: 1036, FF: 780
object Issue_Queue_Pack{
    class issue_queue_t[T <: inst_pack_DP_t](inst_pack_t: T) extends Bundle{
        val inst = inst_pack_t.cloneType
        val prj_waked = Bool()
        val prk_waked = Bool()
        val issued = Bool()
    }
    def Wake_Up(wake_preg: Vec[UInt], pr: UInt) : Bool = {
        val wf = Cat(
                    pr === wake_preg(3), 
                    pr === wake_preg(2),
                    pr === wake_preg(1),
                    pr === wake_preg(0)
                    )
        wf.orR
    }
}

import Issue_Queue_Pack._
class Unorder_Issue_Queue_IO[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Bundle{
    // input from dispatch
    val insts_disp_index = Input(Vec(4, UInt(2.W)))
    val insts_disp_valid = Input(Vec(4, Bool()))
    val insts_dispatch   = Input(Vec(4, inst_pack_t))
    val insert_num       = Input(UInt(3.W))
    val prj_ready        = Input(Vec(4, Bool()))
    val prk_ready        = Input(Vec(4, Bool()))
    val queue_ready      = Output(Bool())

    // input from wakeup
    val wake_preg        = Input(Vec(4, UInt(7.W)))

    // input for issue ack
    val issue_ack        = Input(Vec(n, Bool()))

    // output for issue
    val insts_issue      = Output(Vec(n, new issue_queue_t(inst_pack_t)))
    val issue_req        = Output(Vec(n, Bool()))

    // output for dispatch
    val prd_queue        = Output(Vec(n+1, UInt(7.W)))
    val elem_num         = Output(UInt((log2Ceil(n)+1).W))
    val full             = Output(Bool())
 
    val stall            = Input(Bool())
    val flush            = Input(Bool())
}

class Unorder_Issue_Queue[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Module{
    val io = IO(new Unorder_Issue_Queue_IO(n, inst_pack_t))
    val queue = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new issue_queue_t(inst_pack_t)))))
    val tail = RegInit(0.U((log2Ceil(n)+1).W))

    val empty = tail === 0.U
    val insert_num = io.insert_num
    val tail_pop = Wire(UInt((log2Ceil(n)+1).W))
    val full = tail_pop >= n.U - insert_num
    io.full := full

    io.queue_ready := !full
    io.elem_num := tail_pop
    val insts_dispatch = io.insts_dispatch

    val queue_temp = Wire(Vec(n+1, new issue_queue_t(inst_pack_t)))
    val queue_next = Wire(Vec(n, new issue_queue_t(inst_pack_t)))
    
    // wake up 
    queue_temp := 0.U.asTypeOf(Vec(n+1, new issue_queue_t(inst_pack_t)))
    for(i <- 0 until n){
        queue_temp(i).inst := queue(i).inst
        queue_temp(i).prj_waked := queue(i).prj_waked | Wake_Up(io.wake_preg, queue(i).inst.asInstanceOf[inst_pack_DP_t].prj)
        queue_temp(i).prk_waked := queue(i).prk_waked | Wake_Up(io.wake_preg, queue(i).inst.asInstanceOf[inst_pack_DP_t].prk)
    }
    // issue
    val next_mask = ~(io.issue_ack.asUInt - 1.U)
    tail_pop := tail - io.issue_ack.exists(_ === true.B)

    io.prd_queue := 0.U.asTypeOf(Vec(n+1, UInt(7.W)))
    for(i <- 0 until n){
        queue_next(i) := Mux(i.asUInt < tail_pop, Mux(next_mask(i), queue_temp(i+1), queue_temp(i)), 0.U.asTypeOf(new issue_queue_t(inst_pack_t)))
        io.prd_queue(i) := Mux(queue_next(i).inst.asInstanceOf[inst_pack_DP_t].rd_valid, queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prd, 0.U)
    }
    io.prd_queue(n) := Mux(queue(OHToUInt(io.issue_ack)).inst.asInstanceOf[inst_pack_DP_t].rd_valid, queue(OHToUInt(io.issue_ack)).inst.asInstanceOf[inst_pack_DP_t].prd, 0.U)

    for(i <- 0 until n){
        queue(i).inst := Mux(i.asUInt < tail_pop, queue_next(i).inst, Mux(io.insts_disp_valid((i.U - tail_pop)(1, 0)), io.insts_dispatch(io.insts_disp_index((i.U - tail_pop)(1, 0))), 0.U.asTypeOf(inst_pack_t)))
        queue(i).prj_waked := Mux(i.asUInt < tail_pop, queue_next(i).prj_waked, io.prj_ready(io.insts_disp_index((i.U - tail_pop)(1, 0))))
        queue(i).prk_waked := Mux(i.asUInt < tail_pop, queue_next(i).prk_waked, io.prk_ready(io.insts_disp_index((i.U - tail_pop)(1, 0))))
    }
    tail := Mux(io.flush, 0.U, Mux(io.stall, tail_pop, tail_pop + Mux(io.queue_ready, insert_num, 0.U)))

    // output
    io.insts_issue := queue
    
    for(i <- 0 until n){
        if(inst_pack_t.isInstanceOf[inst_pack_DP_LS_t]){
            if(i == 0){
                io.issue_req(i) := i.asUInt < tail && queue(i).prj_waked && queue(i).prk_waked
            }
            else{
                val mem_type = queue(i).inst.asInstanceOf[inst_pack_DP_LS_t].mem_type
                when(mem_type =/= NO_MEM && mem_type(4) === 0.U){
                    io.issue_req(i) := false.B
                }.otherwise{
                    val mem_type_ahead = VecInit(queue.map(_.inst.asInstanceOf[inst_pack_DP_LS_t].mem_type).take(i))
                    val store_ahead = VecInit(Seq.tabulate(i)(j => mem_type_ahead(j) =/= NO_MEM && mem_type_ahead(j)(4) === 0.U)).reduce(_||_)
                    io.issue_req(i) := (i.asUInt < tail && queue(i).prj_waked && queue(i).prk_waked) && !store_ahead
                }
            }
        }
        else {
            io.issue_req(i) := i.asUInt < tail && queue(i).prj_waked && queue(i).prk_waked
        }
        
    }

}

// object Unorder_Issue_Queue extends App {
//     emitVerilog(new Unorder_Issue_Queue(8), Array("-td", "build/"))
// }
