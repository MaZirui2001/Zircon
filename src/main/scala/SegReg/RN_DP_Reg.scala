import chisel3._
import chisel3.util._
import Inst_Pack._
import CPU_Config._

// LUT: 38 FF: 520

class RN_DP_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val insts_pack_RN   = Input(Vec(2, new inst_pack_RN_t))
        val inst_RN         = Input(Vec(2, UInt(32.W)))

        val insts_pack_DP   = Output(Vec(2, new inst_pack_RN_t))
        val inst_DP         = Output(Vec(2, UInt(32.W)))

    })

    val insts_pack_reg  = RegInit(VecInit.fill(2)(0.U.asTypeOf(new inst_pack_RN_t)))
    val inst_reg        = RegInit(VecInit.fill(2)(0.U(32.W)))

    when(io.flush) {
        insts_pack_reg  := VecInit.fill(2)(0.U.asTypeOf(new inst_pack_RN_t))
    }.elsewhen(!io.stall){
        insts_pack_reg  := io.insts_pack_RN
        inst_reg        := io.inst_RN
    }

    io.insts_pack_DP    := insts_pack_reg
    io.inst_DP          := inst_reg


}
