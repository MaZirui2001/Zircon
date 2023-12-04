import chisel3._
import chisel3.util._
import Inst_Pack._
object PD_Pack {
    val JIRL = 3.U(4.W)
    val B    = 4.U(4.W)
    val BL   = 5.U(4.W)
    val BEQ  = 6.U(4.W)
    val BNE  = 7.U(4.W)
    val BLT  = 8.U(4.W)
    val BGE  = 9.U(4.W)
    val BLTU = 10.U(4.W)
    val BGEU = 11.U(4.W)

    val NOT_JUMP = 0.U(2.W)
    val YES_JUMP = 1.U(2.W)
    val MAY_JUMP = 2.U(2.W)
    val NOT_BR   = 3.U(2.W)
}

class Prev_Decode_IO extends Bundle {
    val insts_pack_IF       = Input(Vec(4, new inst_pack_IF_t))
    
    val insts_pack_PD       = Output(Vec(4, new inst_pack_IF_t))
    val pred_fix            = Output(Bool())
    val pred_fix_target     = Output(UInt(32.W))
    val pred_fix_is_bl      = Output(Bool())
    val pred_fix_pc_plus_4  = Output(UInt(32.W))
}
import PD_Pack._
class Prev_Decode extends Module {
    val io = IO(new Prev_Decode_IO)

    val inst_pack_IF    = io.insts_pack_IF
    val insts_opcode    = VecInit(inst_pack_IF.map(_.inst(31, 30)))
    val br_type         = VecInit(inst_pack_IF.map(_.inst(29, 26)))

    val inst_pack_pd    = Wire(Vec(4, new inst_pack_IF_t))
    inst_pack_pd        := inst_pack_IF
    val inst            = VecInit(inst_pack_IF.map(_.inst))

    val need_fix        = VecInit(Seq.fill(4)(false.B))
    val fix_index       = PriorityEncoder(need_fix)
    io.pred_fix         := need_fix.reduce(_||_)

    io.pred_fix_target  := inst_pack_pd(fix_index).pred_npc

    val jump_type       = VecInit(Seq.fill(4)(NOT_JUMP))
    for(i <- 0 until 4){
        when(insts_opcode(i) === "b01".U){
            when(br_type(i) === B || br_type(i) === BL){
                jump_type(i) := YES_JUMP
            }.elsewhen(br_type(i) >= 6.U && br_type(i) <= 11.U){
                jump_type(i) := MAY_JUMP
            }
        }.otherwise{
            jump_type(i) := NOT_BR
        }
    }
    val pred_fix_is_bl      = VecInit.tabulate(4)(i => inst_pack_IF(i).inst(29, 26) === BL)
    val pred_fix_pc_plus_4  = VecInit.tabulate(4)(i => inst_pack_IF(i).pc + 4.U)
    io.pred_fix_is_bl       := pred_fix_is_bl(fix_index)
    io.pred_fix_pc_plus_4   := pred_fix_pc_plus_4(fix_index)
    
    for(i <- 0 until 4){
        switch(jump_type(i)){
            is(YES_JUMP){
                inst_pack_pd(i).predict_jump    := true.B
                inst_pack_pd(i).pred_npc        := inst_pack_IF(i).pc + Cat(Fill(4, inst(i)(9)), inst(i)(9, 0), inst(i)(25, 10), 0.U(2.W)) 
                need_fix(i)                     := (!inst_pack_IF(i).predict_jump || inst_pack_pd(i).pred_npc =/= inst_pack_IF(i).pred_npc) && inst_pack_IF(i).inst_valid
            }
            is(MAY_JUMP){
                val pc_target = inst_pack_IF(i).pc + Cat(Fill(14, inst(i)(25)), inst(i)(25, 10), 0.U(2.W))
                when(!inst_pack_IF(i).pred_valid){
                    need_fix(i)                     := inst(i)(25) && inst_pack_IF(i).inst_valid
                    inst_pack_pd(i).predict_jump    := inst(i)(25)
                    inst_pack_pd(i).pred_npc        := Mux(inst(i)(25), pc_target, inst_pack_IF(i).pc + 4.U)
                }.otherwise{
                    when(inst_pack_IF(i).predict_jump){
                        need_fix(i)                     := pc_target =/= inst_pack_IF(i).pred_npc && inst_pack_IF(i).inst_valid
                        inst_pack_pd(i).predict_jump    := inst_pack_IF(i).predict_jump
                        inst_pack_pd(i).pred_npc        := pc_target
                    }
                }
            }
            is(NOT_BR){
                inst_pack_pd(i).predict_jump    := false.B
                inst_pack_pd(i).pred_npc        := inst_pack_IF(i).pc + 4.U
                need_fix(i)                     := inst_pack_IF(i).inst_valid && inst_pack_IF(i).predict_jump
            }
        }
    }
    val inst_valid = VecInit(Seq.fill(4)(false.B))
    inst_valid(0) := inst_pack_IF(0).inst_valid
    inst_pack_pd(0).inst_valid := inst_valid(0)
    for(i <- 1 until 4){
        inst_valid(i) := inst_valid(i-1) && inst_pack_IF(i).inst_valid && !need_fix(i-1)
        inst_pack_pd(i).inst_valid := inst_valid(i)
    }
    io.insts_pack_PD := inst_pack_pd
}
