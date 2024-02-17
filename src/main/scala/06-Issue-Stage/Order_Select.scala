import chisel3._
import chisel3.util._
import Inst_Pack._
import Issue_Queue_Struct._
import CPU_Config._

class Order_Select_IO[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Bundle {
    val insts_issue         = Input(new issue_queue_t(inst_pack_t))
    val issue_req           = Input(Bool())
    val stall               = Input(Bool())

    val issue_ack           = Output(Bool())
    val wake_preg           = Output(UInt(log2Ceil(PREG_NUM).W))

    val inst_issue          = Output(new issue_queue_t(inst_pack_t))
    val inst_issue_valid    = Output(Bool())
}

class Order_Select[T <: inst_pack_DP_t](n: Int, inst_pack_t: T) extends Module {
    val io = IO(new Order_Select_IO(n, inst_pack_t))
    
    io.issue_ack            := !io.stall && io.issue_req
    io.wake_preg            := Mux(io.issue_ack && io.insts_issue.inst.asInstanceOf[inst_pack_DP_t].rd_valid, io.insts_issue.inst.asInstanceOf[inst_pack_DP_t].prd, 0.U)

    val bubble_inst_issue   = 0.U.asTypeOf(new issue_queue_t(inst_pack_t))
    io.inst_issue           := Mux(io.issue_ack, io.insts_issue, bubble_inst_issue)
    io.inst_issue_valid     := io.issue_ack
}

