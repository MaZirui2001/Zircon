import chisel3._
import chisel3.util._
import Issue_Queue_Struct._
import Inst_Pack._
import CPU_Config._

class Order_Issue_Queue_IO[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Bundle{
    // input from dispatch
    val insts_disp_index = Input(Vec(2, UInt(1.W)))
    val insts_disp_valid = Input(Vec(2, Bool()))
    val insts_dispatch   = Input(Vec(2, inst_pack_t))
    val prj_ready        = Input(Vec(2, Bool()))
    val prk_ready        = Input(Vec(2, Bool()))
    val queue_ready      = Output(Bool()) 

    // input from wakeup
    val wake_preg        = Input(Vec(4, UInt(log2Ceil(PREG_NUM).W)))

    // input from load
    val ld_mem_prd       = Input(UInt(log2Ceil(PREG_NUM).W))
    // val is_ld_mem        = Input(Bool())
    val is_store_cmt_num = Input(UInt(2.W))
    val rob_index_cmt    = Input(UInt(log2Ceil(ROB_NUM).W))

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
    val queue       = RegInit(VecInit.fill(n)(0.U.asTypeOf(new issue_queue_t(inst_pack_t))))
    val tail        = RegInit(0.U((log2Ceil(n)+1).W))

    val insert_num  = PopCount(io.insts_disp_valid)
    val tail_pop    = Wire(UInt((log2Ceil(n)+1).W))
    val full        = tail > (n-2).U

    io.queue_ready  := !full
    io.full         := full

    val insts_dispatch  = io.insts_dispatch
    //val queue_next      = Wire(Vec(n, new issue_queue_t(inst_pack_t)))
    val disp_index      = io.insts_disp_index
    
    // issue
    tail_pop        := tail - io.issue_ack
    val ld_mem_prd_valid    = io.ld_mem_prd.orR
    val ld_wake_prd_valid   = io.wake_preg(3).orR
    for(i <- 0 until n){
        val queue_next  = Wire(new issue_queue_t(inst_pack_t))
        val mem_prd      = Wire(UInt(PREG_NUM.W))
        val mem_prd_valid = Wire(Bool())
        when(i.asUInt < tail_pop){
            queue_next := (if(i == n-1) queue(i) else Mux(io.issue_ack, queue(i+1), queue(i)))
            mem_prd                 := io.wake_preg(3)
            mem_prd_valid           := ld_wake_prd_valid

        }.otherwise{
            val idx                     = (i.U - tail_pop)(0)
            queue_next.inst             := io.insts_dispatch(disp_index(idx))
            queue_next.prj_waked        := io.prj_ready(disp_index(idx))
            queue_next.prk_waked        := io.prk_ready(disp_index(idx))
            queue_next.prj_wake_by_ld   := false.B
            queue_next.prk_wake_by_ld   := false.B
            mem_prd                     := io.ld_mem_prd
            mem_prd_valid               := ld_mem_prd_valid
        }
        queue(i).inst           := queue_next.inst
        queue(i).prj_waked      := queue_next.prj_waked || Wake_Up(io.wake_preg, queue_next.inst.asInstanceOf[inst_pack_DP_t].prj)
        queue(i).prk_waked      := queue_next.prk_waked || Wake_Up(io.wake_preg, queue_next.inst.asInstanceOf[inst_pack_DP_t].prk)
        queue(i).prj_wake_by_ld := (!(queue_next.inst.asInstanceOf[inst_pack_DP_t].prj ^ mem_prd) && mem_prd_valid) || queue_next.prj_wake_by_ld
        queue(i).prk_wake_by_ld := (!(queue_next.inst.asInstanceOf[inst_pack_DP_t].prk ^ mem_prd) && mem_prd_valid) || queue_next.prk_wake_by_ld
    }
    tail    := Mux(io.flush, 0.U, Mux(io.stall, tail_pop, tail_pop + Mux(io.queue_ready, insert_num, 0.U)))

    // store in pipeline
    val store_num = RegInit(0.U)
    when(io.flush){
        store_num := 0.U
    }.otherwise{
        if(inst_pack_t.isInstanceOf[inst_pack_DP_LS_t]){
            store_num := store_num + (io.issue_ack && queue(0).inst.asInstanceOf[inst_pack_DP_LS_t].mem_type(4)) - io.is_store_cmt_num
        }else{
            store_num := 0.U
        }
    }
    // output
    io.insts_issue := queue(0)
    if(inst_pack_t.isInstanceOf[inst_pack_DP_LS_t]){
        io.issue_req := (tail =/= 0.U && queue(0).prj_waked && queue(0).prk_waked 
                                      && (!(queue(0).inst.asInstanceOf[inst_pack_DP_LS_t].priv_vec(0) && (store_num =/= 0.U || queue(0).inst.asInstanceOf[inst_pack_DP_LS_t].rob_index =/= io.rob_index_cmt))))
    }else{
        io.issue_req := (tail =/= 0.U && queue(0).prj_waked && queue(0).prk_waked 
                                      && (!((queue(0).prj_wake_by_ld && !(queue(0).inst.prj ^ io.ld_mem_prd)
                                          || queue(0).prk_wake_by_ld && !(queue(0).inst.prk ^ io.ld_mem_prd)) && io.dcache_miss)))
    }
    
}
