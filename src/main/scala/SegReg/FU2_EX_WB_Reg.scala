
import chisel3._
import chisel3.util._
import Inst_Pack._

class FU2_EX_WB_Reg extends Module {
  val io = IO(new Bundle {
    val flush = Input(Bool())
    val stall = Input(Bool())
    val inst_pack_EX = Input(new inst_pack_IS_FU2_t)
    val alu_out_EX = Input(UInt(32.W))
    val csr_wdata_EX = Input(UInt(32.W))

    val inst_pack_WB = Output(new inst_pack_IS_FU2_t)
    val alu_out_WB = Output(UInt(32.W))
    val csr_wdata_WB = Output(UInt(32.W))
  })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_IS_FU2_t))
    val alu_out_reg = RegInit(0.U(32.W))
    val csr_wdata_reg = RegInit(0.U(32.W))

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_IS_FU2_t)
        alu_out_reg := 0.U
        csr_wdata_reg := 0.U
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX
        alu_out_reg := io.alu_out_EX
        csr_wdata_reg := io.csr_wdata_EX
    }


    io.inst_pack_WB := inst_pack_reg
    io.alu_out_WB := alu_out_reg
    io.csr_wdata_WB := csr_wdata_reg

}
