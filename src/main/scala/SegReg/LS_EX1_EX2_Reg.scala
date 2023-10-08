import chisel3._
import chisel3.util._
import Inst_Pack._

class LS_EX1_EX2_Reg extends Module {
  val io = IO(new Bundle {
    val flush = Input(Bool())
    val stall = Input(Bool())
    val inst_pack_EX1 = Input(new inst_pack_t)
    val mem_addr_EX1 = Input(UInt(32.W))
    val inst_valid_EX1 = Input(Bool())

    val inst_pack_EX2 = Output(new inst_pack_t)
    val mem_addr_EX2 = Output(UInt(32.W))
    val inst_valid_EX2 = Output(Bool())
  })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_t))
    val mem_addr_reg = RegInit(0.U(32.W))
    val inst_valid_reg = RegInit(false.B)

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_t)
        mem_addr_reg := 0.U
        inst_valid_reg := false.B
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX1
        mem_addr_reg := io.mem_addr_EX1
        inst_valid_reg := io.inst_valid_EX1
    }

    io.inst_pack_EX2 := inst_pack_reg
    io.mem_addr_EX2 := mem_addr_reg
    io.inst_valid_EX2 := inst_valid_reg

}
