import chisel3._
import chisel3.util._
import Inst_Pack._
import Issue_Queue_Pack._

// LUT: 402

class Unorder_Select_IO(n: Int) extends Bundle {
    val insts_issue         = Input(Vec(n, new issue_queue_t))
    val issue_req           = Input(Vec(n, Bool()))
    val stall               = Input(Bool())

    val issue_ack           = Output(Vec(n, Bool()))
    val wake_preg           = Output(UInt(7.W))

    val inst_issue          = Output(new issue_queue_t)
    val inst_issue_valid    = Output(Bool())
}

class Unorder_Select(n: Int) extends Module {
    val io = IO(new Unorder_Select_IO(n))

    val issue_ack = PriorityEncoderOH(io.issue_req.asUInt)
    val issue_ack_vec = Wire(Vec(n, Bool()))
    for(i <- 0 until n){
        issue_ack_vec(i) := issue_ack(i)
    }
    io.issue_ack := Mux(io.issue_req.asUInt.orR && !io.stall, issue_ack_vec, 0.U.asTypeOf(Vec(n, Bool())))

    val select_index = OHToUInt(issue_ack)
    io.wake_preg := Mux(io.issue_ack.asUInt.orR && !io.stall, 
                    Mux(io.insts_issue(select_index).inst.rd_valid, io.insts_issue(select_index).inst.prd, 0.U), 0.U)
    
    val inst_issue = io.insts_issue(select_index)
    val bubble_inst_issue = 0.U.asTypeOf(new issue_queue_t)
    io.inst_issue := Mux(io.issue_ack.asUInt.orR, inst_issue, bubble_inst_issue)
    io.inst_issue_valid := io.issue_ack.asUInt.orR

}

// object Unorder_Select extends App {
//     emitVerilog(new Unorder_Select(8), Array("-td", "build/"))
// }
