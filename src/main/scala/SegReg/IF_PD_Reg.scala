import chisel3._
import chisel3.util._
import Inst_Pack._
import CPU_Config._
class IF_PD_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val insts_pack_IF   = Input(Vec(2, new inst_pack_IF_t))
        val npc16_IF        = Input(Vec(2, UInt(32.W)))
        val npc26_IF        = Input(Vec(2, UInt(32.W)))
        val npc4_IF         = Input(Vec(2, UInt(32.W)))

        val insts_pack_PD   = Output(Vec(2, new inst_pack_IF_t))
        val npc16_PD        = Output(Vec(2, UInt(32.W)))
        val npc26_PD        = Output(Vec(2, UInt(32.W)))
        val npc4_PD         = Output(Vec(2, UInt(32.W)))
    })


    val insts_pack_reg  = RegInit(VecInit.fill(2)(0.U.asTypeOf(new inst_pack_IF_t)))
    val npc16_reg       = RegInit(VecInit.fill(2)(0.U(32.W)))
    val npc26_reg       = RegInit(VecInit.fill(2)(0.U(32.W)))
    val npc4_reg        = RegInit(VecInit.fill(2)(0.U(32.W)))

    when(io.flush) {
        insts_pack_reg  := VecInit.fill(2)(0.U.asTypeOf(new inst_pack_IF_t))
    }
    .elsewhen(!io.stall){
        insts_pack_reg  := io.insts_pack_IF
        npc16_reg       := io.npc16_IF
        npc26_reg       := io.npc26_IF
        npc4_reg        := io.npc4_IF
    }
    io.insts_pack_PD    := insts_pack_reg
    io.npc16_PD         := npc16_reg
    io.npc26_PD         := npc26_reg
    io.npc4_PD          := npc4_reg
}
