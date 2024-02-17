import chisel3._
import chisel3.util._
import CPU_Config._

object Inst_Pack{
    class inst_pack_PF_t extends Bundle{
        val pc              = UInt(32.W)
        val inst_valid      = Bool()
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
        val pred_valid      = Bool()
        val exception       = UInt(8.W)
    }
    def inst_pack_PF_gen(_pc: UInt, _inst_valid: Bool, _predict_jump: Bool, _pred_npc: UInt, _pred_valid: Bool, _exception: UInt) : inst_pack_PF_t = {
        val inst_pack_PF = Wire(new inst_pack_PF_t)
        inst_pack_PF.pc             := _pc
        inst_pack_PF.inst_valid     := _inst_valid
        inst_pack_PF.predict_jump   := _predict_jump
        inst_pack_PF.pred_npc       := _pred_npc
        inst_pack_PF.pred_valid     := _pred_valid
        inst_pack_PF.exception      := _exception
        inst_pack_PF
    }
    class inst_pack_IF_t extends inst_pack_PF_t{
        val inst            = UInt(32.W)
    }
    def inst_pack_IF_gen(_inst_pack_PF: inst_pack_PF_t, _inst : UInt, _exception : UInt) : inst_pack_IF_t = {
        val inst_pack_IF = Wire(new inst_pack_IF_t)
        inst_pack_IF.pc             := _inst_pack_PF.pc
        inst_pack_IF.inst           := _inst
        inst_pack_IF.inst_valid     := _inst_pack_PF.inst_valid
        inst_pack_IF.predict_jump   := _inst_pack_PF.predict_jump
        inst_pack_IF.pred_npc       := _inst_pack_PF.pred_npc
        inst_pack_IF.pred_valid     := _inst_pack_PF.pred_valid
        inst_pack_IF.exception      := Mux(_inst_pack_PF.exception(7), _inst_pack_PF.exception, _exception)
        inst_pack_IF
    }
    class inst_pack_PD_t extends Bundle{
        val pc              = UInt(32.W)
        val inst            = UInt(32.W)
        val inst_valid      = Bool()
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
        val exception       = UInt(8.W)
    }
    def inst_pack_PD_gen(_inst_pack_IF : inst_pack_IF_t) : inst_pack_PD_t = {
        val inst_pack_PD = Wire(new inst_pack_PD_t)
        inst_pack_PD.pc             := _inst_pack_IF.pc
        inst_pack_PD.inst           := _inst_pack_IF.inst
        inst_pack_PD.inst_valid     := _inst_pack_IF.inst_valid
        inst_pack_PD.predict_jump   := _inst_pack_IF.predict_jump
        inst_pack_PD.pred_npc       := _inst_pack_IF.pred_npc
        inst_pack_PD.exception      := _inst_pack_IF.exception
        inst_pack_PD
    }
    class inst_pack_ID_t extends inst_pack_PD_t{
        val rj              = UInt(5.W)
        val rk              = UInt(5.W)
        val rd              = UInt(5.W)
        val rd_valid        = Bool()
        val imm             = UInt(32.W)
        val alu_op          = UInt(4.W)
        val alu_rs1_sel     = UInt(1.W)
        val alu_rs2_sel     = UInt(2.W)
        val br_type         = UInt(4.W)
        val mem_type        = UInt(5.W)
        val priv_vec        = UInt(13.W)
        val fu_id           = UInt(3.W)
    }
    def inst_pack_ID_gen (inst_pack_PD : inst_pack_PD_t, _inst_valid: Bool, _rj : UInt, _rk : UInt, _rd : UInt, _rd_valid : Bool, _imm : UInt, _alu_op : UInt, 
                          _alu_rs1_sel : UInt, _alu_rs2_sel : UInt, _br_type : UInt, _mem_type : UInt, _fu_id : UInt, _priv_vec: UInt, _exception: UInt) : inst_pack_ID_t = {
        val inst_pack_ID = Wire(new inst_pack_ID_t)
        inst_pack_ID.pc             := inst_pack_PD.pc
        inst_pack_ID.inst           := inst_pack_PD.inst
        inst_pack_ID.inst_valid     := _inst_valid
        inst_pack_ID.predict_jump   := inst_pack_PD.predict_jump
        inst_pack_ID.pred_npc       := inst_pack_PD.pred_npc
        inst_pack_ID.rj             := _rj
        inst_pack_ID.rk             := _rk
        inst_pack_ID.rd             := _rd
        inst_pack_ID.rd_valid       := _rd_valid
        inst_pack_ID.imm            := _imm
        inst_pack_ID.alu_op         := _alu_op
        inst_pack_ID.alu_rs1_sel    := _alu_rs1_sel
        inst_pack_ID.alu_rs2_sel    := _alu_rs2_sel
        inst_pack_ID.br_type        := _br_type
        inst_pack_ID.mem_type       := _mem_type
        inst_pack_ID.priv_vec       := _priv_vec
        inst_pack_ID.fu_id          := _fu_id
        inst_pack_ID.exception      := _exception
        inst_pack_ID
    }
    class inst_pack_RN_t extends inst_pack_ID_t{
        val prj             = UInt(log2Ceil(PREG_NUM).W)
        val prk             = UInt(log2Ceil(PREG_NUM).W)
        val prd             = UInt(log2Ceil(PREG_NUM).W)
        val pprd            = UInt(log2Ceil(PREG_NUM).W)
    }
    def inst_pack_RN_gen (inst_pack_ID : inst_pack_ID_t, _prj : UInt, _prk : UInt, _prd : UInt, _pprd : UInt) : inst_pack_RN_t = {
        val inst_pack_RN = Wire(new inst_pack_RN_t)
        inst_pack_RN.pc             := inst_pack_ID.pc
        inst_pack_RN.inst           := inst_pack_ID.inst
        inst_pack_RN.inst_valid     := inst_pack_ID.inst_valid
        inst_pack_RN.predict_jump   := inst_pack_ID.predict_jump
        inst_pack_RN.pred_npc       := inst_pack_ID.pred_npc
        inst_pack_RN.rj             := inst_pack_ID.rj
        inst_pack_RN.rk             := inst_pack_ID.rk
        inst_pack_RN.rd             := inst_pack_ID.rd
        inst_pack_RN.rd_valid       := inst_pack_ID.rd_valid
        inst_pack_RN.imm            := inst_pack_ID.imm
        inst_pack_RN.alu_op         := inst_pack_ID.alu_op
        inst_pack_RN.alu_rs1_sel    := inst_pack_ID.alu_rs1_sel
        inst_pack_RN.alu_rs2_sel    := inst_pack_ID.alu_rs2_sel
        inst_pack_RN.br_type        := inst_pack_ID.br_type
        inst_pack_RN.mem_type       := inst_pack_ID.mem_type
        inst_pack_RN.priv_vec       := inst_pack_ID.priv_vec
        inst_pack_RN.fu_id          := inst_pack_ID.fu_id
        inst_pack_RN.prj            := _prj
        inst_pack_RN.prk            := _prk
        inst_pack_RN.prd            := Mux(inst_pack_ID.rd_valid, _prd, 0.U)
        inst_pack_RN.pprd           := _pprd
        inst_pack_RN.exception      := inst_pack_ID.exception
        inst_pack_RN
    }
    class inst_pack_DP_t extends Bundle{
        val prj             = UInt(log2Ceil(PREG_NUM).W)
        val prk             = UInt(log2Ceil(PREG_NUM).W)
        val rd_valid        = Bool()
        val prd             = UInt(log2Ceil(PREG_NUM).W)
        val imm             = UInt(32.W)
        val rob_index       = UInt(log2Ceil(ROB_NUM).W)
        
    }
    class inst_pack_DP_FU1_t extends inst_pack_DP_t{
        val alu_op          = UInt(4.W)
        val alu_rs1_sel     = UInt(1.W)
        val alu_rs2_sel     = UInt(2.W)
        val pc              = UInt(32.W)
    }
    def inst_pack_DP_FU1_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_FU1_t = {
        val inst_pack_DP_FU1 = Wire(new inst_pack_DP_FU1_t)
        inst_pack_DP_FU1.prj            := inst_pack_RN.prj
        inst_pack_DP_FU1.prk            := inst_pack_RN.prk
        inst_pack_DP_FU1.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_FU1.prd            := inst_pack_RN.prd
        inst_pack_DP_FU1.imm            := inst_pack_RN.imm
        inst_pack_DP_FU1.rob_index      := _rob_index
        inst_pack_DP_FU1.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP_FU1.alu_rs1_sel    := inst_pack_RN.alu_rs1_sel
        inst_pack_DP_FU1.alu_rs2_sel    := inst_pack_RN.alu_rs2_sel
        inst_pack_DP_FU1.pc             := inst_pack_RN.pc
        inst_pack_DP_FU1
    }
    class inst_pack_DP_FU2_t extends inst_pack_DP_t{
        val alu_op          = UInt(4.W)
        val alu_rs1_sel     = UInt(1.W)
        val alu_rs2_sel     = UInt(2.W)
        val pc              = UInt(32.W)
        val br_type         = UInt(4.W)
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
    }
    def inst_pack_DP_FU2_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_FU2_t = {
        val inst_pack_DP_FU2 = Wire(new inst_pack_DP_FU2_t)
        inst_pack_DP_FU2.prj            := inst_pack_RN.prj
        inst_pack_DP_FU2.prk            := inst_pack_RN.prk
        inst_pack_DP_FU2.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_FU2.prd            := inst_pack_RN.prd
        inst_pack_DP_FU2.imm            := inst_pack_RN.imm
        inst_pack_DP_FU2.rob_index      := _rob_index
        inst_pack_DP_FU2.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP_FU2.alu_rs1_sel    := inst_pack_RN.alu_rs1_sel
        inst_pack_DP_FU2.alu_rs2_sel    := inst_pack_RN.alu_rs2_sel
        inst_pack_DP_FU2.pc             := inst_pack_RN.pc
        inst_pack_DP_FU2.br_type        := inst_pack_RN.br_type
        inst_pack_DP_FU2.predict_jump   := inst_pack_RN.predict_jump
        inst_pack_DP_FU2.pred_npc       := inst_pack_RN.pred_npc
        inst_pack_DP_FU2
    }
    class inst_pack_DP_LS_t extends inst_pack_DP_t{
        val mem_type        = UInt(5.W)
        val priv_vec        = UInt(3.W)
    }
    def inst_pack_DP_LS_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_LS_t = {
        val inst_pack_DP_LS = Wire(new inst_pack_DP_LS_t)
        inst_pack_DP_LS.prj            := inst_pack_RN.prj
        inst_pack_DP_LS.prk            := inst_pack_RN.prk
        inst_pack_DP_LS.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_LS.prd            := inst_pack_RN.prd
        inst_pack_DP_LS.imm            := inst_pack_RN.imm
        inst_pack_DP_LS.rob_index      := _rob_index
        inst_pack_DP_LS.mem_type       := inst_pack_RN.mem_type
        inst_pack_DP_LS.priv_vec       := inst_pack_RN.priv_vec(12, 10)
        inst_pack_DP_LS
    }
    class inst_pack_DP_MD_t extends inst_pack_DP_t{
        val priv_vec        = UInt(10.W)
        val alu_op          = UInt(4.W)
    }
    def inst_pack_DP_MD_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_MD_t = {
        val inst_pack_DP_MD = Wire(new inst_pack_DP_MD_t)
        inst_pack_DP_MD.prj            := inst_pack_RN.prj
        inst_pack_DP_MD.prk            := inst_pack_RN.prk
        inst_pack_DP_MD.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_MD.prd            := inst_pack_RN.prd
        inst_pack_DP_MD.imm            := inst_pack_RN.imm
        inst_pack_DP_MD.rob_index      := _rob_index
        inst_pack_DP_MD.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP_MD.priv_vec       := inst_pack_RN.priv_vec(9, 0)
        inst_pack_DP_MD
    }
    class inst_pack_IS_t extends Bundle{
        val prj             = UInt(log2Ceil(PREG_NUM).W)
        val prk             = UInt(log2Ceil(PREG_NUM).W)
        val rd_valid        = Bool()
        val prd             = UInt(log2Ceil(PREG_NUM).W)
        val imm             = UInt(32.W)
        val rob_index       = UInt(log2Ceil(ROB_NUM).W)
        val inst_valid      = Bool()
    }
    class inst_pack_IS_FU1_t extends inst_pack_DP_FU1_t{
        val inst_valid     = Bool()
    }
    def inst_pack_IS_FU1_gen(inst_pack_DP : inst_pack_DP_FU1_t, _inst_valid : Bool): inst_pack_IS_FU1_t = {
        val inst_pack_IS_FU1 = Wire(new inst_pack_IS_FU1_t)
        inst_pack_IS_FU1.prj            := inst_pack_DP.prj
        inst_pack_IS_FU1.prk            := inst_pack_DP.prk
        inst_pack_IS_FU1.rd_valid       := inst_pack_DP.rd_valid
        inst_pack_IS_FU1.prd            := inst_pack_DP.prd
        inst_pack_IS_FU1.imm            := inst_pack_DP.imm
        inst_pack_IS_FU1.rob_index      := inst_pack_DP.rob_index
        inst_pack_IS_FU1.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS_FU1.alu_rs1_sel    := inst_pack_DP.alu_rs1_sel
        inst_pack_IS_FU1.alu_rs2_sel    := inst_pack_DP.alu_rs2_sel
        inst_pack_IS_FU1.pc             := inst_pack_DP.pc
        inst_pack_IS_FU1.inst_valid     := _inst_valid
        inst_pack_IS_FU1

    }
    class inst_pack_IS_FU2_t extends inst_pack_DP_FU2_t{
        val inst_valid    = Bool()
    }
    def inst_pack_IS_FU2_gen (inst_pack_DP : inst_pack_DP_FU2_t, _inst_valid : Bool) : inst_pack_IS_FU2_t = {
        val inst_pack_IS_FU2 = Wire(new inst_pack_IS_FU2_t)
        inst_pack_IS_FU2.prj            := inst_pack_DP.prj
        inst_pack_IS_FU2.prk            := inst_pack_DP.prk
        inst_pack_IS_FU2.rd_valid       := inst_pack_DP.rd_valid
        inst_pack_IS_FU2.prd            := inst_pack_DP.prd
        inst_pack_IS_FU2.imm            := inst_pack_DP.imm
        inst_pack_IS_FU2.rob_index      := inst_pack_DP.rob_index
        inst_pack_IS_FU2.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS_FU2.alu_rs1_sel    := inst_pack_DP.alu_rs1_sel
        inst_pack_IS_FU2.alu_rs2_sel    := inst_pack_DP.alu_rs2_sel
        inst_pack_IS_FU2.pc             := inst_pack_DP.pc
        inst_pack_IS_FU2.br_type        := inst_pack_DP.br_type
        inst_pack_IS_FU2.predict_jump   := inst_pack_DP.predict_jump
        inst_pack_IS_FU2.pred_npc       := inst_pack_DP.pred_npc
        inst_pack_IS_FU2.inst_valid     := _inst_valid
        inst_pack_IS_FU2
    }
    class inst_pack_IS_LS_t extends inst_pack_DP_LS_t{
        val inst_valid    = Bool()
    }
    def inst_pack_IS_LS_gen (inst_pack_DP : inst_pack_DP_LS_t, _inst_valid : Bool) : inst_pack_IS_LS_t = {
        val inst_pack_IS_LS = Wire(new inst_pack_IS_LS_t)
        inst_pack_IS_LS.prj            := inst_pack_DP.prj
        inst_pack_IS_LS.prk            := inst_pack_DP.prk
        inst_pack_IS_LS.rd_valid       := inst_pack_DP.rd_valid
        inst_pack_IS_LS.prd            := inst_pack_DP.prd
        inst_pack_IS_LS.imm            := inst_pack_DP.imm
        inst_pack_IS_LS.rob_index      := inst_pack_DP.rob_index
        inst_pack_IS_LS.mem_type       := inst_pack_DP.mem_type
        inst_pack_IS_LS.priv_vec       := inst_pack_DP.priv_vec
        inst_pack_IS_LS.inst_valid     := _inst_valid
        inst_pack_IS_LS
    }
    class inst_pack_IS_MD_t extends inst_pack_DP_MD_t{
        val inst_valid    = Bool()
    }
    def inst_pack_IS_MD_gen (inst_pack_DP : inst_pack_DP_MD_t, _inst_valid : Bool) : inst_pack_IS_MD_t = {
        val inst_pack_IS_MD = Wire(new inst_pack_IS_MD_t)
        inst_pack_IS_MD.prj            := inst_pack_DP.prj
        inst_pack_IS_MD.prk            := inst_pack_DP.prk
        inst_pack_IS_MD.rd_valid       := inst_pack_DP.rd_valid
        inst_pack_IS_MD.prd            := inst_pack_DP.prd
        inst_pack_IS_MD.imm            := inst_pack_DP.imm
        inst_pack_IS_MD.rob_index      := inst_pack_DP.rob_index
        inst_pack_IS_MD.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS_MD.priv_vec       := inst_pack_DP.priv_vec
        inst_pack_IS_MD.inst_valid     := _inst_valid
        inst_pack_IS_MD
    }
}