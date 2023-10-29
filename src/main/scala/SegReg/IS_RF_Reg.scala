import chisel3._
import chisel3.util._
import Inst_Pack._

class IS_RF_Reg[T <: Bundle](inst_pack_t: T) extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_IS    = Input(inst_pack_t)

        val inst_pack_RF    = Output(inst_pack_t)
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(inst_pack_t))

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(inst_pack_t)
    }
    .elsewhen(!io.stall){
        inst_pack_reg := io.inst_pack_IS
    }

    io.inst_pack_RF := inst_pack_reg
}

// object IS_RF_Reg {
//     def apply(_inst_pack_DP: inst_pack_DP_t, _inst_valid: Bool, _flush: Bool, _stall: Bool): IS_RF_Reg = {
//         val is_rf_reg = Module(new IS_RF_Reg)

//         is_rf_reg.io.flush                      := _flush
//         is_rf_reg.io.stall                      := _stall
//         is_rf_reg.io.inst_pack_IS.prj           := _inst_pack_DP.prj
//         is_rf_reg.io.inst_pack_IS.prk           := _inst_pack_DP.prk
//         is_rf_reg.io.inst_pack_IS.rd            := _inst_pack_DP.rd
//         is_rf_reg.io.inst_pack_IS.rd_valid      := _inst_pack_DP.rd_valid
//         is_rf_reg.io.inst_pack_IS.prd           := _inst_pack_DP.prd
//         is_rf_reg.io.inst_pack_IS.pprd          := _inst_pack_DP.pprd
//         is_rf_reg.io.inst_pack_IS.imm           := _inst_pack_DP.imm
//         is_rf_reg.io.inst_pack_IS.alu_op        := _inst_pack_DP.alu_op
//         is_rf_reg.io.inst_pack_IS.alu_rs1_sel   := _inst_pack_DP.alu_rs1_sel
//         is_rf_reg.io.inst_pack_IS.alu_rs2_sel   := _inst_pack_DP.alu_rs2_sel
//         is_rf_reg.io.inst_pack_IS.br_type       := _inst_pack_DP.br_type
//         is_rf_reg.io.inst_pack_IS.mem_type      := _inst_pack_DP.mem_type
//         is_rf_reg.io.inst_pack_IS.pc            := _inst_pack_DP.pc
//         is_rf_reg.io.inst_pack_IS.rob_index     := _inst_pack_DP.rob_index
//         is_rf_reg.io.inst_pack_IS.predict_jump  := _inst_pack_DP.predict_jump
//         is_rf_reg.io.inst_pack_IS.pred_npc      := _inst_pack_DP.pred_npc
//         is_rf_reg.io.inst_pack_IS.inst_exist    := _inst_pack_DP.inst_exist
//         is_rf_reg.io.inst_pack_IS.inst_valid    := _inst_valid
//         is_rf_reg
//     }
// }

