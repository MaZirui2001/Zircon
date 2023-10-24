import chisel3.util.BitPat
import chisel3._

/* Loongarch 32R */
object Instructions {
    // logic reg-reg
    def ADDW        = BitPat("b00000000000100000???????????????")
    def SUBW        = BitPat("b00000000000100010???????????????")
    def SLT         = BitPat("b00000000000100100???????????????")
    def SLTU        = BitPat("b00000000000100101???????????????")
    def NOR         = BitPat("b00000000000101000???????????????")
    def AND         = BitPat("b00000000000101001???????????????")
    def OR          = BitPat("b00000000000101010???????????????")
    def XOR         = BitPat("b00000000000101011???????????????")
    def SLLW        = BitPat("b00000000000101110???????????????")
    def SRLW        = BitPat("b00000000000101111???????????????")
    def SRAW        = BitPat("b00000000000110000???????????????")
    def MULW        = BitPat("b00000000000111000???????????????")
    def MULHW       = BitPat("b00000000000111001???????????????")
    def MULHWU      = BitPat("b00000000000111010???????????????")
    def DIVW        = BitPat("b00000000001000000???????????????")
    def MODW        = BitPat("b00000000001000001???????????????")
    def DIVWU       = BitPat("b00000000001000010???????????????")
    def MODWU       = BitPat("b00000000001000011???????????????")
    // logic reg-imm
    def SLLIW       = BitPat("b00000000010000001???????????????")
    def SRLIW       = BitPat("b00000000010001001???????????????")
    def SRAIW       = BitPat("b00000000010010001???????????????")
    def SLTI        = BitPat("b0000001000??????????????????????")
    def SLTUI       = BitPat("b0000001001??????????????????????")
    def ADDIW       = BitPat("b0000001010??????????????????????")
    def ANDI        = BitPat("b0000001101??????????????????????")
    def ORI         = BitPat("b0000001110??????????????????????")
    def XORI        = BitPat("b0000001111??????????????????????")

    // imm and pc
    def LU12IW      = BitPat("b0001010?????????????????????????")
    def PCADDU12I   = BitPat("b0001110?????????????????????????")

    // load-store
    def LDB         = BitPat("b0010100000??????????????????????")
    def LDH         = BitPat("b0010100001??????????????????????")
    def LDW         = BitPat("b0010100010??????????????????????")
    def STB         = BitPat("b0010100100??????????????????????")
    def STH         = BitPat("b0010100101??????????????????????")
    def STW         = BitPat("b0010100110??????????????????????")
    def LDBU        = BitPat("b0010101000??????????????????????")
    def LDHU        = BitPat("b0010101001??????????????????????")
    
    // branch
    def JIRL        = BitPat("b010011??????????????????????????")
    def B           = BitPat("b010100??????????????????????????")
    def BL          = BitPat("b010101??????????????????????????")
    def BEQ         = BitPat("b010110??????????????????????????")
    def BNE         = BitPat("b010111??????????????????????????")    
    def BLT         = BitPat("b011000??????????????????????????")
    def BGE         = BitPat("b011001??????????????????????????")
    def BLTU        = BitPat("b011010??????????????????????????")
    def BGEU        = BitPat("b011011??????????????????????????")
}
object Inst_Pack{
    class inst_pack_IF_t extends Bundle{
        val pc              = UInt(32.W)
        val inst            = UInt(32.W)
        val inst_valid      = Bool()
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
        val pred_valid      = Bool()
    }
    def inst_pack_IF_gen(_pc: UInt, _inst : UInt, _inst_valid : Bool, _predict_jump : Bool, _pred_npc: UInt, _pred_valid: Bool) : inst_pack_IF_t = {
        val inst_pack_IF = Wire(new inst_pack_IF_t)
        inst_pack_IF.pc             := _pc
        inst_pack_IF.inst           := _inst
        inst_pack_IF.inst_valid     := _inst_valid
        inst_pack_IF.predict_jump   := _predict_jump
        inst_pack_IF.pred_npc       := _pred_npc
        inst_pack_IF.pred_valid     := _pred_valid
        inst_pack_IF
    }
    class inst_pack_PD_t extends Bundle{
        val pc              = UInt(32.W)
        val inst            = UInt(32.W)
        val inst_valid      = Bool()
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
    }
    def inst_pack_PD_gen(_inst_pack_IF : inst_pack_IF_t) : inst_pack_PD_t = {
        val inst_pack_PD = Wire(new inst_pack_PD_t)
        inst_pack_PD.pc             := _inst_pack_IF.pc
        inst_pack_PD.inst           := _inst_pack_IF.inst
        inst_pack_PD.inst_valid     := _inst_pack_IF.inst_valid
        inst_pack_PD.predict_jump   := _inst_pack_IF.predict_jump
        inst_pack_PD.pred_npc       := _inst_pack_IF.pred_npc
        inst_pack_PD
    }
    class inst_pack_ID_t extends inst_pack_PD_t{
        val rj              = UInt(5.W)
        val rj_valid        = Bool()
        val rk              = UInt(5.W)
        val rk_valid        = Bool()
        val rd              = UInt(5.W)
        val rd_valid        = Bool()
        val imm             = UInt(32.W)
        val alu_op          = UInt(5.W)
        val alu_rs1_sel     = UInt(2.W)
        val alu_rs2_sel     = UInt(2.W)
        val br_type         = UInt(4.W)
        val mem_type        = UInt(5.W)
        val fu_id           = UInt(2.W)
        val inst_exist      = Bool()
    }
    def inst_pack_ID_gen (inst_pack_PD : inst_pack_PD_t, _inst_valid: Bool, _rj : UInt, _rj_valid : Bool, _rk : UInt, _rk_valid : Bool, _rd : UInt, _rd_valid : Bool, _imm : UInt, _alu_op : UInt, _alu_rs1_sel : UInt, _alu_rs2_sel : UInt, _br_type : UInt, _mem_type : UInt, _fu_id : UInt, _inst_exist : Bool) : inst_pack_ID_t = {
        val inst_pack_ID = Wire(new inst_pack_ID_t)
        inst_pack_ID.pc             := inst_pack_PD.pc
        inst_pack_ID.inst           := inst_pack_PD.inst
        inst_pack_ID.inst_valid     := _inst_valid
        inst_pack_ID.predict_jump   := inst_pack_PD.predict_jump
        inst_pack_ID.pred_npc       := inst_pack_PD.pred_npc
        inst_pack_ID.rj             := _rj
        inst_pack_ID.rj_valid       := _rj_valid
        inst_pack_ID.rk             := _rk
        inst_pack_ID.rk_valid       := _rk_valid
        inst_pack_ID.rd             := _rd
        inst_pack_ID.rd_valid       := _rd_valid
        inst_pack_ID.imm            := _imm
        inst_pack_ID.alu_op         := _alu_op
        inst_pack_ID.alu_rs1_sel    := _alu_rs1_sel
        inst_pack_ID.alu_rs2_sel    := _alu_rs2_sel
        inst_pack_ID.br_type        := _br_type
        inst_pack_ID.mem_type       := _mem_type
        inst_pack_ID.fu_id          := _fu_id
        inst_pack_ID.inst_exist     := _inst_exist
        inst_pack_ID
    }
    class inst_pack_RN_t extends inst_pack_ID_t{
        val prj             = UInt(7.W)
        val prk             = UInt(7.W)
        val prd             = UInt(7.W)
        val pprd            = UInt(7.W)
        val rob_index       = UInt(6.W)
        val prj_raw         = Bool()
        val prk_raw         = Bool()
    }
    def inst_pack_RN_gen (inst_pack_ID : inst_pack_ID_t, _prj : UInt, _prk : UInt, _prd : UInt, _pprd : UInt, _rob_index : UInt, _prj_raw : Bool, _prk_raw : Bool) : inst_pack_RN_t = {
        val inst_pack_RN = Wire(new inst_pack_RN_t)
        inst_pack_RN.pc             := inst_pack_ID.pc
        inst_pack_RN.inst           := inst_pack_ID.inst
        inst_pack_RN.inst_valid     := inst_pack_ID.inst_valid
        inst_pack_RN.predict_jump   := inst_pack_ID.predict_jump
        inst_pack_RN.pred_npc       := inst_pack_ID.pred_npc
        inst_pack_RN.rj             := inst_pack_ID.rj
        inst_pack_RN.rj_valid       := inst_pack_ID.rj_valid
        inst_pack_RN.rk             := inst_pack_ID.rk
        inst_pack_RN.rk_valid       := inst_pack_ID.rk_valid
        inst_pack_RN.rd             := inst_pack_ID.rd
        inst_pack_RN.rd_valid       := inst_pack_ID.rd_valid
        inst_pack_RN.imm            := inst_pack_ID.imm
        inst_pack_RN.alu_op         := inst_pack_ID.alu_op
        inst_pack_RN.alu_rs1_sel    := inst_pack_ID.alu_rs1_sel
        inst_pack_RN.alu_rs2_sel    := inst_pack_ID.alu_rs2_sel
        inst_pack_RN.br_type        := inst_pack_ID.br_type
        inst_pack_RN.mem_type       := inst_pack_ID.mem_type
        inst_pack_RN.fu_id          := inst_pack_ID.fu_id
        inst_pack_RN.inst_exist     := inst_pack_ID.inst_exist
        inst_pack_RN.prj            := _prj
        inst_pack_RN.prk            := _prk
        inst_pack_RN.prd            := _prd
        inst_pack_RN.pprd           := _pprd
        inst_pack_RN.rob_index      := _rob_index
        inst_pack_RN.prj_raw        := _prj_raw
        inst_pack_RN.prk_raw        := _prk_raw
        inst_pack_RN
    }
    class inst_pack_DP_t extends Bundle{
        val rj              = UInt(5.W)
        val rj_valid        = Bool()
        val prj             = UInt(7.W)
        val rk              = UInt(5.W)
        val rk_valid        = Bool()
        val prk             = UInt(7.W)
        val rd              = UInt(5.W)
        val rd_valid        = Bool()
        val prd             = UInt(7.W)
        val pprd            = UInt(7.W)
        val imm             = UInt(32.W)
        val alu_op          = UInt(5.W)
        val alu_rs1_sel     = UInt(2.W)
        val alu_rs2_sel     = UInt(2.W)
        val br_type         = UInt(4.W)
        val mem_type        = UInt(5.W)
        val pc              = UInt(32.W)
        val rob_index       = UInt(6.W)
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
        val inst_exist      = Bool()
    }
    def inst_pack_DP_gen (inst_pack_RN : inst_pack_RN_t) : inst_pack_DP_t = {
        val inst_pack_DP = Wire(new inst_pack_DP_t)
        inst_pack_DP.rj             := inst_pack_RN.rj
        inst_pack_DP.rj_valid       := inst_pack_RN.rj_valid
        inst_pack_DP.prj            := inst_pack_RN.prj
        inst_pack_DP.rk             := inst_pack_RN.rk
        inst_pack_DP.rk_valid       := inst_pack_RN.rk_valid
        inst_pack_DP.prk            := inst_pack_RN.prk
        inst_pack_DP.rd             := inst_pack_RN.rd
        inst_pack_DP.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP.prd            := inst_pack_RN.prd
        inst_pack_DP.pprd           := inst_pack_RN.pprd
        inst_pack_DP.imm            := inst_pack_RN.imm
        inst_pack_DP.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP.alu_rs1_sel    := inst_pack_RN.alu_rs1_sel
        inst_pack_DP.alu_rs2_sel    := inst_pack_RN.alu_rs2_sel
        inst_pack_DP.br_type        := inst_pack_RN.br_type
        inst_pack_DP.mem_type       := inst_pack_RN.mem_type
        inst_pack_DP.pc             := inst_pack_RN.pc
        inst_pack_DP.rob_index      := inst_pack_RN.rob_index
        inst_pack_DP.predict_jump   := inst_pack_RN.predict_jump
        inst_pack_DP.pred_npc       := inst_pack_RN.pred_npc
        inst_pack_DP.inst_exist     := inst_pack_RN.inst_exist
        inst_pack_DP
    }
    class inst_pack_IS_t extends inst_pack_DP_t{
        val inst_valid     = Bool()
    }
    def inst_pack_IS_gen(inst_pack_DP : inst_pack_DP_t, _inst_valid : Bool) : inst_pack_IS_t = {
        val inst_pack_IS = Wire(new inst_pack_IS_t)
        inst_pack_IS.rj             := inst_pack_DP.rj
        inst_pack_IS.rj_valid       := inst_pack_DP.rj_valid
        inst_pack_IS.prj            := inst_pack_DP.prj
        inst_pack_IS.rk             := inst_pack_DP.rk
        inst_pack_IS.rk_valid       := inst_pack_DP.rk_valid
        inst_pack_IS.prk            := inst_pack_DP.prk
        inst_pack_IS.rd             := inst_pack_DP.rd
        inst_pack_IS.rd_valid       := inst_pack_DP.rd_valid
        inst_pack_IS.prd            := inst_pack_DP.prd
        inst_pack_IS.pprd           := inst_pack_DP.pprd
        inst_pack_IS.imm            := inst_pack_DP.imm
        inst_pack_IS.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS.alu_rs1_sel    := inst_pack_DP.alu_rs1_sel
        inst_pack_IS.alu_rs2_sel    := inst_pack_DP.alu_rs2_sel
        inst_pack_IS.br_type        := inst_pack_DP.br_type
        inst_pack_IS.mem_type       := inst_pack_DP.mem_type
        inst_pack_IS.pc             := inst_pack_DP.pc
        inst_pack_IS.rob_index      := inst_pack_DP.rob_index
        inst_pack_IS.predict_jump   := inst_pack_DP.predict_jump
        inst_pack_IS.pred_npc       := inst_pack_DP.pred_npc
        inst_pack_IS.inst_exist     := inst_pack_DP.inst_exist
        inst_pack_IS.inst_valid     := _inst_valid
        inst_pack_IS
    }
}