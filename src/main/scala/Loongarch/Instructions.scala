import chisel3.util.BitPat
import chisel3._
import chisel3.util._

/* Loongarch 32R */
object Instructions {
    // rdcnt
    def RDCNTIDW    = BitPat("b0000000000000000011000?????00000")
    def RDCNTVLW    = BitPat("b000000000000000001100000000?????")
    def RDCNTVHW    = BitPat("b000000000000000001100100000?????")
    
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

    // else
    def BREAK       = BitPat("b00000000001010100???????????????")
    def SYSCALL     = BitPat("b00000000001010110???????????????")

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

    // CSR
    def CSRRD       = BitPat("b00000100??????????????00000?????")
    def CSRWR       = BitPat("b00000100??????????????00001?????")
    def CSRXCHG     = BitPat("b00000100????????????????????????")

    // cacop
    def CACOP       = BitPat("b0000011000??????????????????????")

    // tlb
    def TLBSRCH     = BitPat("b00000110010010000010100000000000")
    def TLBRD       = BitPat("b00000110010010000010110000000000")
    def TLBWR       = BitPat("b00000110010010000011000000000000")
    def TLBFILL     = BitPat("b00000110010010000011010000000000")

    // priv
    def ERTN        = BitPat("b00000110010010000011100000000000")
    def IDLE        = BitPat("b00000110010010001???????????????")
    def INVTLB      = BitPat("b00000110010010011???????????????")

    // imm and pc
    def LU12IW      = BitPat("b0001010?????????????????????????")
    def PCADDU12I   = BitPat("b0001110?????????????????????????")

    // atmomic
    def LLW         = BitPat("b00100000????????????????????????")
    def SCW         = BitPat("b00100001????????????????????????")

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
import CPU_Config._
object Inst_Pack{
    class inst_pack_PF_t extends Bundle{
        val pc              = UInt(32.W)
        val inst_valid      = Bool()
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
        val pred_valid      = Bool()
    }
    def inst_pack_PF_gen(_pc: UInt, _inst_valid: Bool, _predict_jump: Bool, _pred_npc: UInt, _pred_valid: Bool) : inst_pack_PF_t = {
        val inst_pack_PF = Wire(new inst_pack_PF_t)
        inst_pack_PF.pc             := _pc
        inst_pack_PF.inst_valid     := _inst_valid
        inst_pack_PF.predict_jump   := _predict_jump
        inst_pack_PF.pred_npc       := _pred_npc
        inst_pack_PF.pred_valid     := _pred_valid
        inst_pack_PF
    }
    class inst_pack_IF_t extends inst_pack_PF_t{
        val inst            = UInt(32.W)
    }
    def inst_pack_IF_gen(_inst_pack_PF: inst_pack_PF_t, _inst : UInt) : inst_pack_IF_t = {
        val inst_pack_IF = Wire(new inst_pack_IF_t)
        inst_pack_IF.pc             := _inst_pack_PF.pc
        inst_pack_IF.inst           := _inst
        inst_pack_IF.inst_valid     := _inst_pack_PF.inst_valid
        inst_pack_IF.predict_jump   := _inst_pack_PF.predict_jump
        inst_pack_IF.pred_npc       := _inst_pack_PF.pred_npc
        inst_pack_IF.pred_valid     := _inst_pack_PF.pred_valid
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
        val csr_addr        = UInt(14.W)
        val priv_vec        = UInt(4.W)
        val fu_id           = UInt(3.W)
        val inst_exist      = Bool()
    }
    def inst_pack_ID_gen (inst_pack_PD : inst_pack_PD_t, _inst_valid: Bool, _rj : UInt, _rj_valid : Bool, _rk : UInt, _rk_valid : Bool, _rd : UInt, _rd_valid : Bool, _imm : UInt, _alu_op : UInt, 
                          _alu_rs1_sel : UInt, _alu_rs2_sel : UInt, _br_type : UInt, _mem_type : UInt, _fu_id : UInt, _inst_exist : Bool, _priv_vec: UInt, _csr_addr: UInt) : inst_pack_ID_t = {
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
        inst_pack_ID.csr_addr       := _csr_addr
        inst_pack_ID.priv_vec       := _priv_vec
        inst_pack_ID.fu_id          := _fu_id
        inst_pack_ID.inst_exist     := _inst_exist
        inst_pack_ID
    }
    class inst_pack_RN_t extends inst_pack_ID_t{
        val prj             = UInt(log2Ceil(PREG_NUM).W)
        val prk             = UInt(log2Ceil(PREG_NUM).W)
        val prd             = UInt(log2Ceil(PREG_NUM).W)
        val pprd            = UInt(log2Ceil(PREG_NUM).W)
        val prj_raw         = Bool()
        val prk_raw         = Bool()
    }
    def inst_pack_RN_gen (inst_pack_ID : inst_pack_ID_t, _prj : UInt, _prk : UInt, _prd : UInt, _pprd : UInt, _prj_raw : Bool, _prk_raw : Bool) : inst_pack_RN_t = {
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
        inst_pack_RN.csr_addr       := inst_pack_ID.csr_addr
        inst_pack_RN.priv_vec       := inst_pack_ID.priv_vec
        inst_pack_RN.fu_id          := inst_pack_ID.fu_id
        inst_pack_RN.inst_exist     := inst_pack_ID.inst_exist
        inst_pack_RN.prj            := _prj
        inst_pack_RN.prk            := _prk
        inst_pack_RN.prd            := _prd
        inst_pack_RN.pprd           := _pprd
        inst_pack_RN.prj_raw        := _prj_raw
        inst_pack_RN.prk_raw        := _prk_raw
        inst_pack_RN
    }
    class inst_pack_DP_t extends Bundle{
        val prj             = UInt(log2Ceil(PREG_NUM).W)
        val prk             = UInt(log2Ceil(PREG_NUM).W)
        val rd_valid        = Bool()
        val prd             = UInt(log2Ceil(PREG_NUM).W)
        val imm             = UInt(32.W)
        val rob_index       = UInt(log2Ceil(ROB_NUM).W)
        val inst_exist      = Bool()
    }
    class inst_pack_DP_FU1_t extends inst_pack_DP_t{
        val alu_op          = UInt(5.W)
        val alu_rs1_sel     = UInt(2.W)
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
        inst_pack_DP_FU1.inst_exist     := inst_pack_RN.inst_exist
        inst_pack_DP_FU1.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP_FU1.alu_rs1_sel    := inst_pack_RN.alu_rs1_sel
        inst_pack_DP_FU1.alu_rs2_sel    := inst_pack_RN.alu_rs2_sel
        inst_pack_DP_FU1.pc             := inst_pack_RN.pc
        inst_pack_DP_FU1
    }
    class inst_pack_DP_FU2_t extends inst_pack_DP_t{
        val alu_op          = UInt(5.W)
        val alu_rs1_sel     = UInt(2.W)
        val alu_rs2_sel     = UInt(2.W)
        val csr_addr        = UInt(14.W)
        val priv_vec        = UInt(4.W)
        val pc              = UInt(32.W)
    }
    def inst_pack_DP_FU2_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_FU2_t = {
        val inst_pack_DP_FU2 = Wire(new inst_pack_DP_FU2_t)
        inst_pack_DP_FU2.prj            := inst_pack_RN.prj
        inst_pack_DP_FU2.prk            := inst_pack_RN.prk
        inst_pack_DP_FU2.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_FU2.prd            := inst_pack_RN.prd
        inst_pack_DP_FU2.imm            := inst_pack_RN.imm
        inst_pack_DP_FU2.rob_index      := _rob_index
        inst_pack_DP_FU2.inst_exist     := inst_pack_RN.inst_exist
        inst_pack_DP_FU2.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP_FU2.alu_rs1_sel    := inst_pack_RN.alu_rs1_sel
        inst_pack_DP_FU2.alu_rs2_sel    := inst_pack_RN.alu_rs2_sel
        inst_pack_DP_FU2.csr_addr       := inst_pack_RN.csr_addr
        inst_pack_DP_FU2.priv_vec       := inst_pack_RN.priv_vec
        inst_pack_DP_FU2.pc             := inst_pack_RN.pc
        inst_pack_DP_FU2
    }
    class inst_pack_DP_FU3_t extends inst_pack_DP_t{
        val alu_op          = UInt(5.W)
        val alu_rs1_sel     = UInt(2.W)
        val alu_rs2_sel     = UInt(2.W)
        val pc              = UInt(32.W)
        val br_type         = UInt(4.W)
        val predict_jump    = Bool()
        val pred_npc        = UInt(32.W)
    }
    def inst_pack_DP_FU3_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_FU3_t = {
        val inst_pack_DP_FU3 = Wire(new inst_pack_DP_FU3_t)
        inst_pack_DP_FU3.prj            := inst_pack_RN.prj
        inst_pack_DP_FU3.prk            := inst_pack_RN.prk
        inst_pack_DP_FU3.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_FU3.prd            := inst_pack_RN.prd
        inst_pack_DP_FU3.imm            := inst_pack_RN.imm
        inst_pack_DP_FU3.rob_index      := _rob_index
        inst_pack_DP_FU3.inst_exist     := inst_pack_RN.inst_exist
        inst_pack_DP_FU3.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP_FU3.alu_rs1_sel    := inst_pack_RN.alu_rs1_sel
        inst_pack_DP_FU3.alu_rs2_sel    := inst_pack_RN.alu_rs2_sel
        inst_pack_DP_FU3.pc             := inst_pack_RN.pc
        inst_pack_DP_FU3.br_type        := inst_pack_RN.br_type
        inst_pack_DP_FU3.predict_jump   := inst_pack_RN.predict_jump
        inst_pack_DP_FU3.pred_npc       := inst_pack_RN.pred_npc
        inst_pack_DP_FU3
    }
    class inst_pack_DP_LS_t extends inst_pack_DP_t{
        val mem_type        = UInt(5.W)
    }
    def inst_pack_DP_LS_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_LS_t = {
        val inst_pack_DP_LS = Wire(new inst_pack_DP_LS_t)
        inst_pack_DP_LS.prj            := inst_pack_RN.prj
        inst_pack_DP_LS.prk            := inst_pack_RN.prk
        inst_pack_DP_LS.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_LS.prd            := inst_pack_RN.prd
        inst_pack_DP_LS.imm            := inst_pack_RN.imm
        inst_pack_DP_LS.rob_index      := _rob_index
        inst_pack_DP_LS.inst_exist     := inst_pack_RN.inst_exist
        inst_pack_DP_LS.mem_type       := inst_pack_RN.mem_type
        inst_pack_DP_LS
    }
    class inst_pack_DP_MD_t extends inst_pack_DP_t{
        val alu_op          = UInt(5.W)
    }
    def inst_pack_DP_MD_gen (inst_pack_RN : inst_pack_RN_t, _rob_index: UInt) : inst_pack_DP_MD_t = {
        val inst_pack_DP_MD = Wire(new inst_pack_DP_MD_t)
        inst_pack_DP_MD.prj            := inst_pack_RN.prj
        inst_pack_DP_MD.prk            := inst_pack_RN.prk
        inst_pack_DP_MD.rd_valid       := inst_pack_RN.rd_valid
        inst_pack_DP_MD.prd            := inst_pack_RN.prd
        inst_pack_DP_MD.imm            := inst_pack_RN.imm
        inst_pack_DP_MD.rob_index      := _rob_index
        inst_pack_DP_MD.inst_exist     := inst_pack_RN.inst_exist
        inst_pack_DP_MD.alu_op         := inst_pack_RN.alu_op
        inst_pack_DP_MD
    }
    class inst_pack_IS_t extends Bundle{
        val prj             = UInt(log2Ceil(PREG_NUM).W)
        val prk             = UInt(log2Ceil(PREG_NUM).W)
        val rd_valid        = Bool()
        val prd             = UInt(log2Ceil(PREG_NUM).W)
        val imm             = UInt(32.W)
        val rob_index       = UInt(log2Ceil(ROB_NUM).W)
        val inst_exist      = Bool()
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
        inst_pack_IS_FU1.inst_exist     := inst_pack_DP.inst_exist
        inst_pack_IS_FU1.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS_FU1.alu_rs1_sel    := inst_pack_DP.alu_rs1_sel
        inst_pack_IS_FU1.alu_rs2_sel    := inst_pack_DP.alu_rs2_sel
        inst_pack_IS_FU1.pc             := inst_pack_DP.pc
        inst_pack_IS_FU1.inst_valid     := _inst_valid
        inst_pack_IS_FU1

    }
    class inst_pack_IS_FU2_t extends inst_pack_DP_FU2_t{
        val inst_valid     = Bool()
    }
    def inst_pack_IS_FU2_gen(inst_pack_DP : inst_pack_DP_FU2_t, _inst_valid : Bool): inst_pack_IS_FU2_t = {
        val inst_pack_IS_FU2 = Wire(new inst_pack_IS_FU2_t)
        inst_pack_IS_FU2.prj            := inst_pack_DP.prj
        inst_pack_IS_FU2.prk            := inst_pack_DP.prk
        inst_pack_IS_FU2.rd_valid       := inst_pack_DP.rd_valid
        inst_pack_IS_FU2.prd            := inst_pack_DP.prd
        inst_pack_IS_FU2.imm            := inst_pack_DP.imm
        inst_pack_IS_FU2.rob_index      := inst_pack_DP.rob_index
        inst_pack_IS_FU2.inst_exist     := inst_pack_DP.inst_exist
        inst_pack_IS_FU2.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS_FU2.alu_rs1_sel    := inst_pack_DP.alu_rs1_sel
        inst_pack_IS_FU2.alu_rs2_sel    := inst_pack_DP.alu_rs2_sel
        inst_pack_IS_FU2.csr_addr       := inst_pack_DP.csr_addr
        inst_pack_IS_FU2.priv_vec       := inst_pack_DP.priv_vec
        inst_pack_IS_FU2.pc             := inst_pack_DP.pc
        inst_pack_IS_FU2.inst_valid     := _inst_valid
        inst_pack_IS_FU2

    }
    class inst_pack_IS_FU3_t extends inst_pack_DP_FU3_t{
        val inst_valid    = Bool()
    }
    def inst_pack_IS_FU3_gen (inst_pack_DP : inst_pack_DP_FU3_t, _inst_valid : Bool) : inst_pack_IS_FU3_t = {
        val inst_pack_IS_FU3 = Wire(new inst_pack_IS_FU3_t)
        inst_pack_IS_FU3.prj            := inst_pack_DP.prj
        inst_pack_IS_FU3.prk            := inst_pack_DP.prk
        inst_pack_IS_FU3.rd_valid       := inst_pack_DP.rd_valid
        inst_pack_IS_FU3.prd            := inst_pack_DP.prd
        inst_pack_IS_FU3.imm            := inst_pack_DP.imm
        inst_pack_IS_FU3.rob_index      := inst_pack_DP.rob_index
        inst_pack_IS_FU3.inst_exist     := inst_pack_DP.inst_exist
        inst_pack_IS_FU3.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS_FU3.alu_rs1_sel    := inst_pack_DP.alu_rs1_sel
        inst_pack_IS_FU3.alu_rs2_sel    := inst_pack_DP.alu_rs2_sel
        inst_pack_IS_FU3.pc             := inst_pack_DP.pc
        inst_pack_IS_FU3.br_type        := inst_pack_DP.br_type
        inst_pack_IS_FU3.predict_jump   := inst_pack_DP.predict_jump
        inst_pack_IS_FU3.pred_npc       := inst_pack_DP.pred_npc
        inst_pack_IS_FU3.inst_valid     := _inst_valid
        inst_pack_IS_FU3
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
        inst_pack_IS_LS.inst_exist     := inst_pack_DP.inst_exist
        inst_pack_IS_LS.mem_type       := inst_pack_DP.mem_type
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
        inst_pack_IS_MD.inst_exist     := inst_pack_DP.inst_exist
        inst_pack_IS_MD.alu_op         := inst_pack_DP.alu_op
        inst_pack_IS_MD.inst_valid     := _inst_valid
        inst_pack_IS_MD
    }
}