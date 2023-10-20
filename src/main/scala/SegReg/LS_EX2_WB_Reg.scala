import chisel3._
import chisel3.util._
import Inst_Pack._

class LS_EX2_WB_Reg extends Module {
    val io = IO(new Bundle {
        val flush = Input(Bool())
        val stall = Input(Bool())
        val inst_pack_EX2 = Input(new inst_pack_IS_t)
        val mem_rdata_EX2 = Input(UInt(32.W))
    
        val inst_pack_WB = Output(new inst_pack_IS_t)
        val mem_rdata_WB = Output(UInt(32.W))
    })
    
    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_IS_t))
    val mem_rdata_reg = RegInit(0.U(32.W))
    
    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_IS_t)
        mem_rdata_reg := 0.U
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX2
        mem_rdata_reg := io.mem_rdata_EX2
    }

    io.inst_pack_WB := inst_pack_reg
    io.mem_rdata_WB := mem_rdata_reg

}