import chisel3._
import chisel3.util._
import Inst_Pack._

// LUT: 944, FF: 732
object Unorder_Issue_Queue_Pack{
    class issue_queue_t extends Bundle{
        val inst = new inst_pack_t
        val prj_waked = Bool()
        val prk_waked = Bool()
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

import Unorder_Issue_Queue_Pack._
class Unorder_Issue_Queue_IO(n: Int) extends Bundle{
    // input from dispatch
    val insts_dispatch  = Input(Vec(4, new inst_pack_t))
    val insts_valid     = Input(Vec(4, Bool()))
    val queue_ready     = Output(Bool())

    // input from wakeup
    val wake_preg       = Input(Vec(4, UInt(6.W)))

    // input for issue ack
    val issue_ack       = Input(Vec(n, Bool()))

    // output for issue
    val insts_issue     = Output(Vec(n, new issue_queue_t))
    val issue_req       = Output(Vec(n, Bool()))
}

class Unorder_Issue_Queue(n: Int) extends Module{
    val io = IO(new Unorder_Issue_Queue_IO(n))
    val queue = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new issue_queue_t))))
    val tail = RegInit(0.U((log2Ceil(n)+1).W))

    val empty = tail === 0.U
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
    val next_mask = Mux(io.issue_ack.asUInt.orR, ~(io.issue_ack.asUInt - 1.U), 0.U)
    tail_pop := tail - io.issue_ack.asUInt.orR

    for(i <- 0 until n){
        queue(i).inst := Mux(i.asUInt < tail_pop, Mux(next_mask(i), queue_temp(i+1).inst, queue_temp(i).inst), insts_dispatch(i.asUInt - tail_pop))
        queue(i).prj_waked := Mux(i.asUInt < tail_pop, Mux(next_mask(i), queue_temp(i+1).prj_waked, queue_temp(i).prj_waked), 
                                                       ~insts_dispatch(i.asUInt - tail_pop).rj_valid | insts_dispatch(i.asUInt - tail_pop).rj === 0.U)
        queue(i).prk_waked := Mux(i.asUInt < tail_pop, Mux(next_mask(i), queue_temp(i+1).prk_waked, queue_temp(i).prk_waked), 
                                                       ~insts_dispatch(i.asUInt - tail_pop).rk_valid | insts_dispatch(i.asUInt - tail_pop).rk === 0.U)
    }
    tail := tail_pop + Mux(io.queue_ready, insert_num, 0.U)

    // output
    io.insts_issue := queue
    for(i <- 0 until n){
        io.issue_req(i) := i.asUInt < tail && queue(i).prj_waked && queue(i).prk_waked
    }

}

object Unorder_Issue_Queue extends App {
    emitVerilog(new Unorder_Issue_Queue(8), Array("-td", "build/"))
}
