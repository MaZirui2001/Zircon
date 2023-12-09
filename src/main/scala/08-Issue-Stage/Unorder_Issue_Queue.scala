import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._
import CPU_Config._

// LUT: 1036, FF: 780
object Issue_Queue_Pack{
    class issue_queue_t[T <: inst_pack_DP_t](inst_pack_t: T) extends Bundle{
        val inst = inst_pack_t.cloneType
        val prj_waked       = Bool()
        val prk_waked       = Bool()
        val prj_wake_by_ld  = Bool()
        val prk_wake_by_ld  = Bool()
        // val issued = Bool()
    }
    def Wake_Up(wake_preg: Vec[UInt], pr: UInt) : Bool = {
        val wf = Cat(
                    pr === wake_preg(4),
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
    val prj_ready        = Input(Vec(4, Bool()))
    val prk_ready        = Input(Vec(4, Bool()))
    val queue_ready      = Output(Bool())

    // input from wakeup
    val wake_preg        = Input(Vec(5, UInt(log2Ceil(PREG_NUM).W)))

    // input for priv
    // val priv_issued      = Input(Bool())
    // val priv_commited    = Input(Bool())

    // input from load
    val ld_mem_prd       = Input(UInt(log2Ceil(PREG_NUM).W))

    // input for issue ack
    val issue_ack        = Input(Vec(n, Bool()))

    // output for issue
    val insts_issue      = Output(Vec(n, new issue_queue_t(inst_pack_t)))
    val issue_req        = Output(Vec(n, Bool()))

    // output for dispatch
    val elem_num         = Output(UInt((log2Ceil(n)+1).W))
    val full             = Output(Bool())
 
    val stall            = Input(Bool())
    val flush            = Input(Bool())
    val dcache_miss      = Input(Bool())
}

class Unorder_Issue_Queue[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Module{
    val io              = IO(new Unorder_Issue_Queue_IO(n, inst_pack_t))
    val queue           = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new issue_queue_t(inst_pack_t)))))
    val tail            = RegInit(0.U((log2Ceil(n)+1).W))

    val empty           = tail === 0.U
    val insert_num      = PopCount(io.insts_disp_valid)
    val tail_pop        = tail - io.issue_ack.exists(_ === true.B)
    val full            = tail > (n-4).U
    

    val insts_dispatch  = io.insts_dispatch
    val disp_index      = io.insts_disp_index
    val queue_next      = Wire(Vec(n, new issue_queue_t(inst_pack_t)))
    
    // issue
    val next_mask       = ~(io.issue_ack.asUInt - 1.U)

    for(i <- 0 until n){
        when(i.asUInt < tail_pop){
            queue_next(i).inst              := Mux(next_mask(i), if(i == n-1) 0.U.asTypeOf(inst_pack_t) else queue(i+1).inst, queue(i).inst)
            queue_next(i).prj_waked         := Mux(next_mask(i), if(i == n-1) false.B else queue(i+1).prj_waked, queue(i).prj_waked)
            queue_next(i).prk_waked         := Mux(next_mask(i), if(i == n-1) false.B else queue(i+1).prk_waked, queue(i).prk_waked)
            queue_next(i).prj_wake_by_ld    := Mux(next_mask(i), if(i == n-1) false.B else queue(i+1).prj_wake_by_ld, queue(i).prj_wake_by_ld)
            queue_next(i).prk_wake_by_ld    := Mux(next_mask(i), if(i == n-1) false.B else queue(i+1).prk_wake_by_ld, queue(i).prk_wake_by_ld)
        }
        .otherwise{
            val idx = (i.U - tail_pop)(1, 0)
            queue_next(i).inst              := Mux(io.insts_disp_valid(idx), io.insts_dispatch(io.insts_disp_index(idx)), 0.U.asTypeOf(inst_pack_t))
            queue_next(i).prj_waked         := Mux(io.insts_disp_valid(idx), io.prj_ready(io.insts_disp_index(idx)), false.B)
            queue_next(i).prk_waked         := Mux(io.insts_disp_valid(idx), io.prk_ready(io.insts_disp_index(idx)), false.B)
            queue_next(i).prj_wake_by_ld    := Mux(io.insts_disp_valid(idx), io.insts_dispatch(io.insts_disp_index(idx)).asInstanceOf[inst_pack_DP_t].prj === io.ld_mem_prd, false.B)
            queue_next(i).prk_wake_by_ld    := Mux(io.insts_disp_valid(idx), io.insts_dispatch(io.insts_disp_index(idx)).asInstanceOf[inst_pack_DP_t].prk === io.ld_mem_prd, false.B)
        }
    }
    for(i <- 0 until n){
        queue(i).inst           := queue_next(i).inst
        queue(i).prj_waked      := queue_next(i).prj_waked || Wake_Up(io.wake_preg, queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prj)
        queue(i).prk_waked      := queue_next(i).prk_waked || Wake_Up(io.wake_preg, queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prk)
        queue(i).prj_wake_by_ld := queue_next(i).prj_wake_by_ld || (queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prj === io.wake_preg(4))
        queue(i).prk_wake_by_ld := queue_next(i).prk_wake_by_ld || (queue_next(i).inst.asInstanceOf[inst_pack_DP_t].prk === io.wake_preg(4))

    }
    tail    := Mux(io.flush, 0.U, Mux(io.stall, tail_pop, tail_pop + Mux(io.queue_ready, insert_num, 0.U)))

    // output
    for(i <- 0 until n){
        if(inst_pack_t.isInstanceOf[inst_pack_DP_LS_t]){
            if(i == 0){
                io.issue_req(i) := i.asUInt < tail && queue(i).prj_waked && queue(i).prk_waked
            }
            else{
                val mem_type = queue(i).inst.asInstanceOf[inst_pack_DP_LS_t].mem_type
                when(mem_type(4)){
                    io.issue_req(i)     := false.B
                }.otherwise{
                    val mem_type_ahead  = VecInit(queue.map(_.inst.asInstanceOf[inst_pack_DP_LS_t].mem_type).take(i))
                    val store_ahead     = VecInit.tabulate(i)(j => mem_type_ahead(j)(4)).reduce(_||_)
                    io.issue_req(i)     := (i.asUInt < tail && queue(i).prj_waked && queue(i).prk_waked) && !store_ahead
                }
            }
        }
        else {
            io.issue_req(i) := (i.asUInt < tail && queue(i).prj_waked && queue(i).prk_waked 
                            && !((queue(i).prj_wake_by_ld || queue(i).prk_wake_by_ld) && io.dcache_miss))
        }
        
    }
    io.insts_issue      := queue
    io.full             := full
    io.queue_ready      := !full
    io.elem_num         := tail

}

