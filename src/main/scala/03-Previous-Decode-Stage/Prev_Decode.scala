import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._
import PreDecode_Config._

class Prev_Decode_IO extends Bundle {
    val insts_pack_IF       = Input(Vec(2, new inst_pack_IF_t))
    val pc_sign_IF          = Input(Vec(2, UInt(2.W)))

    val npc16_IF            = Input(Vec(2, UInt(32.W)))
    val npc26_IF            = Input(Vec(2, UInt(32.W)))
    val npc4_IF             = Input(Vec(2, UInt(32.W)))
    
    val insts_pack_PD       = Output(Vec(2, new inst_pack_IF_t))
    val pred_fix            = Output(Bool())
    val pred_fix_target     = Output(UInt(32.W))
    val pred_fix_is_bl      = Output(Bool())
    val pred_fix_pc         = Output(UInt(32.W))


}
class Prev_Decode extends Module {
    val io = IO(new Prev_Decode_IO)

    val inst_pack_IF    = io.insts_pack_IF
    val insts_opcode    = VecInit(inst_pack_IF.map(_.inst(31, 30)))
    val br_type         = VecInit(inst_pack_IF.map(_.inst(29, 26)))

    val inst_pack_pd    = Wire(Vec(2, new inst_pack_IF_t))
    inst_pack_pd        := inst_pack_IF
    val inst            = VecInit(inst_pack_IF.map(_.inst))

    val need_fix        = VecInit.fill(2)(false.B)
    val fix_index       = !need_fix(0)
    io.pred_fix         := need_fix.asUInt.orR

    io.pred_fix_target  := inst_pack_pd(fix_index).pred_npc

    val jump_type       = VecInit.fill(2)(NOT_JUMP)
    for(i <- 0 until 2){
        when(!(insts_opcode(i) ^ "b01".U)){
            when(!((br_type(i) ^ BR_B) & (br_type(i) ^ BR_BL))){
                jump_type(i) := YES_JUMP
            }.elsewhen((br_type(i) ^ BR_JIRL).orR){
                jump_type(i) := MAY_JUMP
            }
        }.otherwise{
            jump_type(i) := NOT_BR
        }
    }
    val pred_fix_is_bl      = VecInit.tabulate(2)(i => !(inst_pack_IF(i).inst(29, 26) ^ BR_BL))
    val pred_fix_pc         = VecInit.tabulate(2)(i => inst_pack_IF(i).pc)
    io.pred_fix_is_bl       := pred_fix_is_bl(fix_index)
    io.pred_fix_pc          := pred_fix_pc(fix_index)

    val pc_in = VecInit.tabulate(2)(i => MuxLookup(io.pc_sign_IF(i), 0.U)(Seq(
        0.U  -> inst_pack_IF(i).pc,
        1.U  -> Mux(!(jump_type(i) ^ YES_JUMP), inst_pack_IF(i).pc_plus_1_28, inst_pack_IF(i).pc_plus_1_18),
        3.U  -> Mux(!(jump_type(i) ^ YES_JUMP), inst_pack_IF(i).pc_minus_1_28, inst_pack_IF(i).pc_minus_1_18)
    )))
    val npc18 = VecInit.tabulate(2)(i => pc_in(i)(31, 18) ## inst_pack_IF(i).inst(25, 10) ## 0.U(2.W))
    val npc28 = VecInit.tabulate(2)(i => pc_in(i)(31, 28) ## inst_pack_IF(i).inst(9, 0) ## inst_pack_IF(i).inst(25, 10) ## 0.U(2.W))
    
    for(i <- 0 until 2){
        switch(jump_type(i)){
            is(YES_JUMP){
                val imm_raw                     = npc28(i) - inst_pack_IF(i).pc
                need_fix(i)                     := inst_pack_IF(i).inst_valid && !(inst_pack_IF(i).predict_jump && !(inst_pack_IF(i).pred_npc ^ npc28(i)))
                inst_pack_pd(i).predict_jump    := true.B
                inst_pack_pd(i).pred_npc        := npc28(i)
                inst_pack_pd(i).inst            := inst_pack_IF(i).inst(31, 26) ## imm_raw(17, 2) ## imm_raw(27, 18)
            }
            is(MAY_JUMP){
                val imm_raw                     = npc18(i) - inst_pack_IF(i).pc
                inst_pack_pd(i).inst            := inst_pack_IF(i).inst(31, 26) ## imm_raw(17, 2) ## inst_pack_IF(i).inst(9, 0)
                when(!inst_pack_IF(i).pred_valid){
                    need_fix(i)                     := inst(i)(25) && inst_pack_IF(i).inst_valid
                    inst_pack_pd(i).predict_jump    := inst(i)(25)
                    inst_pack_pd(i).pred_npc        := Mux(inst(i)(25), npc18(i), io.npc4_IF(i))
                }
            }
            is(NOT_BR){
                inst_pack_pd(i).predict_jump    := false.B
                inst_pack_pd(i).pred_npc        := io.npc4_IF(i)
                need_fix(i)                     := inst_pack_IF(i).inst_valid && inst_pack_IF(i).predict_jump
            }
        }
    }
    val inst_valid              = VecInit.fill(2)(false.B)
    inst_valid(0)               := inst_pack_IF(0).inst_valid
    inst_pack_pd(0).inst_valid  := inst_valid(0)
    for(i <- 1 until 2){
        inst_pack_pd(i).inst_valid  := inst_valid(i-1) && inst_pack_IF(i).inst_valid && !need_fix(i-1)
    }
    io.insts_pack_PD := inst_pack_pd
}
