import chisel3._
import chisel3.util._
import Inst_Pack._
import Issue_Queue_Pack._
import CPU_Config._

// LUT: 402

class Unorder_Select_IO[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Bundle {
    val insts_issue         = Input(Vec(n, new issue_queue_t(inst_pack_t)))
    val issue_req           = Input(Vec(n, Bool()))
    val stall               = Input(Bool())

    val issue_ack           = Output(Vec(n, Bool()))
    val wake_preg           = Output(UInt(log2Ceil(PREG_NUM).W))
    // val priv_issued         = Output(Bool())

    val inst_issue          = Output(new issue_queue_t(inst_pack_t))
    val inst_issue_valid    = Output(Bool())
}

class Unorder_Select[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Module {
    val io                  = IO(new Unorder_Select_IO(n, inst_pack_t))

    val select_index        = PriorityEncoder(io.issue_req)
    val issue_ack           = UIntToOH(select_index)(n-1, 0)
    val issue_ack_vec       = VecInit(issue_ack.asBools)
    io.issue_ack            := Mux(io.issue_req.asUInt.orR && !io.stall, issue_ack_vec, 0.U.asTypeOf(Vec(n, Bool())))

    
    io.wake_preg            := Mux(io.issue_ack.asUInt.orR && io.insts_issue(select_index).inst.asInstanceOf[inst_pack_DP_t].rd_valid, io.insts_issue(select_index).inst.asInstanceOf[inst_pack_DP_t].prd, 0.U)
    
    val inst_issue          = io.insts_issue(select_index)
    val bubble_inst_issue   = 0.U.asTypeOf(new issue_queue_t(inst_pack_t))
    io.inst_issue           := Mux(io.issue_ack.asUInt.orR, inst_issue, bubble_inst_issue)
    io.inst_issue_valid     := io.issue_ack.asUInt.orR

}

