import chisel3._
import chisel3.util._

class PC_IO extends Bundle {
    val pc_IF           = Output(UInt(32.W))
    val pc_stall        = Input(Bool())
    val predict_fail    = Input(Bool())
    val pc_offset       = Output(UInt(2.W))
    val branch_target   = Input(UInt(32.W))
}

class PC(reset_val: Int) extends Module {
    val io = IO(new PC_IO)

    val pc = RegInit(reset_val.U(32.W))
    val pc_offset = RegInit(0.U(2.W))

    when(io.predict_fail) {
        pc := io.branch_target(31, 4) ## 0.U(4.W) 
        pc_offset := io.branch_target(3, 2)
    }
    .elsewhen(!io.pc_stall) {
        pc := pc + 16.U
        pc_offset := 0.U
    }
    .otherwise{
        pc := pc
        pc_offset := pc_offset
    }

    io.pc_IF := pc
    io.pc_offset := pc_offset

}