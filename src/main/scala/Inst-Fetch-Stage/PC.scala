import chisel3._
import chisel3.util._

class PC_IO extends Bundle {
    val pc_IF = Output(UInt(32.W))
    val pc_stall = Input(Bool())
    val predict_fail = Input(Bool())
    val branch_target = Input(UInt(32.W))
}

class PC(reset_val: Int) extends Module {
    val io = IO(new PC_IO)

    val pc = RegInit(reset_val.U(32.W))

    when(io.predict_fail) {
        pc := io.branch_target
    }
    .elsewhen(!io.pc_stall) {
        pc := pc + 16.U
    }
    .otherwise{
        pc := pc
    }

    io.pc_IF := pc

}