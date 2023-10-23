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
}

class Prev_Decode_IO extends Bundle {
    val insts_pack_IF   = Input(Vec(4, new inst_pack_IF_t))
    
    val insts_pack_PD   = Output(Vec(4, new inst_pack_IF_t))
    val pred_fix        = Output(Bool())
    val pred_fix_target = Output(UInt(32.W))
}
import PD_Pack._
class Prev_Decode extends RawModule {
    val io = IO(new Prev_Decode_IO)

    val insts_opcode = VecInit(io.insts_pack_IF.map(_.inst(31, 30)))
    val br_type = VecInit(io.insts_pack_IF.map(_.inst(29, 26)))

    val inst_pack_pd = Wire(Vec(4, new inst_pack_IF_t))
    inst_pack_pd := io.insts_pack_IF
    val inst = VecInit(io.insts_pack_IF.map(_.inst))

    val pred_fix = VecInit(Seq.tabulate(4)(i => io.insts_pack_IF(i).inst_valid && !io.insts_pack_IF(i).predict_jump && inst_pack_pd(i).predict_jump))
    io.pred_fix := pred_fix.asUInt.orR

    val pred_index = PriorityEncoder(pred_fix)
    io.pred_fix_target := inst_pack_pd(pred_index).pred_npc


    for(i <- 0 until 4){
        when(insts_opcode(i) === "b01".U && !io.insts_pack_IF(i).pred_valid && io.insts_pack_IF(i).inst_valid){
            switch(br_type(i)){
                is(B){
                    inst_pack_pd(i).predict_jump := true.B
                    inst_pack_pd(i).pred_npc := io.insts_pack_IF(i).pc + Cat(Fill(4, inst(i)(9)), inst(i)(9, 0), inst(i)(25, 10), 0.U(2.W)) 
                }
                is(BL){
                    inst_pack_pd(i).predict_jump := true.B
                    inst_pack_pd(i).pred_npc := io.insts_pack_IF(i).pc + Cat(Fill(4, inst(i)(9)), inst(i)(9, 0), inst(i)(25, 10), 0.U(2.W)) 
                }
                is(BEQ){
                    inst_pack_pd(i).predict_jump := inst(i)(25)
                    inst_pack_pd(i).pred_npc := Mux(inst(i)(25), io.insts_pack_IF(i).pc + Cat(Fill(14, inst(i)(25)), inst(i)(25, 10), 0.U(2.W)), io.insts_pack_IF(i).pc + 4.U)
                }
                is(BNE){
                    inst_pack_pd(i).predict_jump := inst(i)(25)
                    inst_pack_pd(i).pred_npc := Mux(inst(i)(25), io.insts_pack_IF(i).pc + Cat(Fill(14, inst(i)(25)), inst(i)(25, 10), 0.U(2.W)), io.insts_pack_IF(i).pc + 4.U)
                }
                is(BLT){
                    inst_pack_pd(i).predict_jump := inst(i)(25)
                    inst_pack_pd(i).pred_npc := Mux(inst(i)(25), io.insts_pack_IF(i).pc + Cat(Fill(14, inst(i)(25)), inst(i)(25, 10), 0.U(2.W)), io.insts_pack_IF(i).pc + 4.U)
                }
                is(BGE){
                    inst_pack_pd(i).predict_jump :=inst(i)(25)
                    inst_pack_pd(i).pred_npc := Mux(inst(i)(25), io.insts_pack_IF(i).pc + Cat(Fill(14, inst(i)(25)), inst(i)(25, 10), 0.U(2.W)), io.insts_pack_IF(i).pc + 4.U)
                }
                is(BLTU){
                    inst_pack_pd(i).predict_jump := inst(i)(25)
                    inst_pack_pd(i).pred_npc := Mux(inst(i)(25), io.insts_pack_IF(i).pc + Cat(Fill(14, inst(i)(25)), inst(i)(25, 10), 0.U(2.W)), io.insts_pack_IF(i).pc + 4.U)
                }
                is(BGEU){
                    inst_pack_pd(i).predict_jump := inst(i)(25)
                    inst_pack_pd(i).pred_npc := Mux(inst(i)(25), io.insts_pack_IF(i).pc + Cat(Fill(14, inst(i)(25)), inst(i)(25, 10), 0.U(2.W)), io.insts_pack_IF(i).pc + 4.U)
                }
            }
        }
    }
    val inst_valid = VecInit(Seq.fill(4)(false.B))
    inst_valid(0) := io.insts_pack_IF(0).inst_valid
    inst_pack_pd(0).inst_valid := inst_valid(0)
    for(i <- 1 until 4){
        inst_valid(i) := inst_valid(i-1) && io.insts_pack_IF(i).inst_valid && !(inst_pack_pd(i-1).predict_jump && !io.insts_pack_IF(i-1).predict_jump)
        inst_pack_pd(i).inst_valid := inst_valid(i)
    }
    io.insts_pack_PD := inst_pack_pd
}
