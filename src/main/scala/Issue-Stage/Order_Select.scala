import chisel3._
import chisel3.util._
import Inst_Pack._
import Issue_Queue_Pack._

// LUT: 88

class Order_Select_IO(n: Int) extends Bundle {
    val insts_issue         = Input(new issue_queue_t)
    val issue_req           = Input(Bool())
    val stall               = Input(Bool())

    val issue_ack           = Output(Bool())
    val wake_preg           = Output(UInt(6.W))

    val inst_issue          = Output(new issue_queue_t)
    val inst_issue_valid    = Output(Bool())
}

class Order_Select(n: Int) extends Module {
    val io = IO(new Order_Select_IO(n))
    
    io.issue_ack := !io.stall && io.issue_req
    io.wake_preg := Mux(io.issue_ack && io.insts_issue.inst.rd_valid, io.insts_issue.inst.prd, 0.U)

    val bubble_inst_issue = 0.U.asTypeOf(new issue_queue_t)
    io.inst_issue := Mux(io.issue_ack, io.insts_issue, bubble_inst_issue)
    io.inst_issue_valid := io.issue_ack
}

// object Order_Select extends App {
//     emitVerilog(new Order_Select(8), Array("-td", "build/"))
// }
