import chisel3._
import chisel3.util._
import Issue_Queue_Pack._
import Inst_Pack._
import CPU_Config._

class Order_Issue_Queue_IO[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Bundle{
    // input from dispatch
    val insts_disp_index = Input(Vec(2, UInt(2.W)))
    val insts_disp_valid = Input(Vec(2, Bool()))
    val insts_dispatch   = Input(Vec(2, inst_pack_t))
    val prj_ready        = Input(Vec(2, Bool()))
    val prk_ready        = Input(Vec(2, Bool()))
    val queue_ready      = Output(Bool()) 

    // input from wakeup
    val wake_preg        = Input(Vec(4, UInt(log2Ceil(PREG_NUM).W)))

    // input from load
    val ld_mem_prd       = Input(UInt(log2Ceil(PREG_NUM).W))

    // input for issue ack
    val issue_ack        = Input(Bool())

    // output for issue
    val insts_issue      = Output(new issue_queue_t(inst_pack_t))
    val issue_req        = Output(Bool())

    // output for dispatch
    val full             = Output(Bool())

    val stall            = Input(Bool())
    val flush            = Input(Bool())
    val dcache_miss      = Input(Bool())
}
class Order_Issue_Queue[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Module {
    val io          = IO(new Order_Issue_Queue_IO(n, inst_pack_t))

    // queue
    val queue       = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new issue_queue_t(inst_pack_t)))))
    val tail        = RegInit(0.U((log2Ceil(n)+1).W))

    val insert_num  = PopCount(io.insts_disp_valid)
    val tail_pop    = Wire(UInt((log2Ceil(n)+1).W))
    val full        = tail > (n-2).U

    io.queue_ready  := !full
    io.full         := full

    val insts_dispatch  = io.insts_dispatch
    val queue_next      = Wire(Vec(n, new issue_queue_t(inst_pack_t)))
    
    // issue
    tail_pop        := tail - io.issue_ack

    for(i <- 0 until n){
        when(i.asUInt < tail_pop){
            queue_next(i).inst              := Mux(io.issue_ack, if(i == n-1) 0.U.asTypeOf(inst_pack_t) else queue(i+1).inst, queue(i).inst)
            queue_next(i).prj_waked         := Mux(io.issue_ack, if(i == n-1) false.B else queue(i+1).prj_waked, queue(i).prj_waked)
            queue_next(i).prk_waked         := Mux(io.issue_ack, if(i == n-1) false.B else queue(i+1).prk_waked, queue(i).prk_waked)
            queue_next(i).prj_wake_by_ld    := Mux(io.issue_ack, if(i == n-1) false.B else queue(i+1).prj_wake_by_ld, queue(i).prj_wake_by_ld)
            queue_next(i).prk_wake_by_ld    := Mux(io.issue_ack, if(i == n-1) false.B else queue(i+1).prk_wake_by_ld, queue(i).prk_wake_by_ld)
        }.otherwise{
            queue_next(i).inst              := Mux(io.insts_disp_valid((i.U - tail_pop)(1, 0)), io.insts_dispatch(io.insts_disp_index((i.U - tail_pop)(1, 0))), 0.U.asTypeOf(inst_pack_t))
            queue_next(i).prj_waked         := Mux(io.insts_disp_valid((i.U - tail_pop)(1, 0)), io.prj_ready(io.insts_disp_index((i.U - tail_pop)(1, 0))), false.B)
            queue_next(i).prk_waked         := Mux(io.insts_disp_valid((i.U - tail_pop)(1, 0)), io.prk_ready(io.insts_disp_index((i.U - tail_pop)(1, 0))), false.B)
            queue_next(i).prj_wake_by_ld    := Mux(io.insts_disp_valid((i.U - tail_pop)(1, 0)), io.insts_dispatch(io.insts_disp_index((i.U - tail_pop)(1, 0))).asInstanceOf[inst_pack_DP_t].prj === io.ld_mem_prd, false.B)
            queue_next(i).prk_wake_by_ld    := Mux(io.insts_disp_valid((i.U - tail_pop)(1, 0)), io.insts_dispatch(io.insts_disp_index((i.U - tail_pop)(1, 0))).asInstanceOf[inst_pack_DP_t].prk === io.ld_mem_prd, false.B)
        }
    }

    for(i <- 0 until n){
        queue(i).inst           := queue_next(i).inst
        queue(i).prj_waked      := queue_next(i).prj_waked || Wake_Up(io.wake_preg, queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prj)
        queue(i).prk_waked      := queue_next(i).prk_waked || Wake_Up(io.wake_preg, queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prk)
        queue(i).prj_wake_by_ld := queue_next(i).prj_wake_by_ld || queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prj === io.wake_preg(3)
        queue(i).prk_wake_by_ld := queue_next(i).prk_wake_by_ld || queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prk === io.wake_preg(3)
    }
    tail    := Mux(io.flush, 0.U, Mux(io.stall, tail_pop, tail_pop + Mux(io.queue_ready, insert_num, 0.U)))

    // output
    io.insts_issue := queue(0)
    io.issue_req := tail =/= 0.U && queue(0).prj_waked && queue(0).prk_waked && (!((queue(0).prj_wake_by_ld || queue(0).prk_wake_by_ld) && io.dcache_miss))
}
