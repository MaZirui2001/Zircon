
import chisel3._
import chisel3.util._

object CPU_Config{
    val RESET_VEC       = 0x1c000000
    val FQ_NUM          = 8
    val PREG_NUM        = 54
    val ROB_NUM         = 24
    val SB_NUM          = 4
    val IQ_AP_NUM       = 6
    val IQ_AB_NUM       = 6
    val IQ_MD_NUM       = 4
    val IQ_LS_NUM       = 8
    val TLB_ENTRY_NUM   = 16
}

object Predict_Config {
    val BTB_INDEX_WIDTH = 7
    val BTB_TAG_WIDTH   = 28 - BTB_INDEX_WIDTH
    val BTB_DEPTH       = 1 << BTB_INDEX_WIDTH
    val BHT_INDEX_WIDTH = 6
    val BHT_DEPTH       = 1 << BHT_INDEX_WIDTH
    val PHT_INDEX_WIDTH = 6
    val PHT_DEPTH       = 1 << PHT_INDEX_WIDTH

    val RET       = 1.U(2.W)
    val BL        = 2.U(2.W)
    val ICALL     = 3.U(2.W)
    val ELSE      = 0.U(2.W)
}

object ICache_Config{
    val INDEX_WIDTH = 6
    val INDEX_DEPTH = 1 << INDEX_WIDTH

    val OFFSET_WIDTH = 6
    val OFFSET_DEPTH = 1 << OFFSET_WIDTH

    val TAG_WIDTH = 32 - INDEX_WIDTH - OFFSET_WIDTH

    val FROM_CMEM = 0.U(1.W)
    val FROM_RBUF = 1.U(1.W)

    val FROM_PIPE = 0.U(1.W)
    val FROM_SEG  = 1.U(1.W)
}

object DCache_Config{
    val INDEX_WIDTH = 6
    val INDEX_DEPTH = 1 << INDEX_WIDTH

    val OFFSET_WIDTH = 6
    val OFFSET_DEPTH = 1 << OFFSET_WIDTH

    val TAG_WIDTH = 32 - INDEX_WIDTH - OFFSET_WIDTH

    val FROM_CMEM = 0.U(1.W)
    val FROM_RBUF = 1.U(1.W)

    val FROM_PIPE = 0.U(1.W)
    val FROM_SEG  = 1.U(1.W)
}

object PreDecode_Config {


    val NOT_JUMP = 0.U(2.W)
    val YES_JUMP = 1.U(2.W)
    val MAY_JUMP = 2.U(2.W)
    val NOT_BR   = 3.U(2.W)
}

object Control_Signal{
    val Y = true.B
    val N = false.B

    // alu_op
    val ALU_ADD   = 0.U(4.W)
    val ALU_SUB   = 1.U(4.W)
    val ALU_SLT   = 2.U(4.W)
    val ALU_SLTU  = 3.U(4.W)
    val ALU_NOR   = 4.U(4.W)
    val ALU_AND   = 5.U(4.W)
    val ALU_OR    = 6.U(4.W)
    val ALU_XOR   = 7.U(4.W)
    val ALU_SLL   = 8.U(4.W)
    val ALU_SRL   = 9.U(4.W)
    val ALU_SRA   = 10.U(4.W)
    val ALU_MUL   = 0.U(4.W)
    val ALU_MULH  = 1.U(4.W)
    val ALU_MULHU = 2.U(4.W)
    val ALU_DIV   = 4.U(4.W)
    val ALU_MOD   = 5.U(4.W)
    val ALU_DIVU  = 6.U(4.W)
    val ALU_MODU  = 7.U(4.W)


    // alu_rs1_sel
    val RS1_REG  = 0.U(1.W)
    val RS1_PC   = 1.U(1.W)

    // alu_rs2_sel
    val RS2_REG  = 0.U(2.W)
    val RS2_IMM  = 1.U(2.W)
    val RS2_FOUR = 2.U(2.W)
    val RS2_CNTH = 2.U(2.W)
    val RS2_CNTL = 3.U(2.W)

    // br_type
    val NO_BR    = 0.U(4.W)
    val BR_JIRL  = 3.U(4.W)
    val BR_B     = 4.U(4.W)
    val BR_BL    = 5.U(4.W)
    val BR_BEQ   = 6.U(4.W)
    val BR_BNE   = 7.U(4.W)
    val BR_BLT   = 8.U(4.W)
    val BR_BGE   = 9.U(4.W)
    val BR_BLTU  = 10.U(4.W)
    val BR_BGEU  = 11.U(4.W)

    // mem_type
    val NO_MEM   = 0.U(5.W)
    val MEM_LDB  = 8.U(5.W)
    val MEM_LDH  = 9.U(5.W)
    val MEM_LDW  = 10.U(5.W)
    val MEM_STB  = 16.U(5.W)
    val MEM_STH  = 17.U(5.W)
    val MEM_STW  = 18.U(5.W)
    val MEM_LDBU = 12.U(5.W)
    val MEM_LDHU = 13.U(5.W)

    // priv vec: last bit signed whether it is priv
    val NOT_PRIV = 0x0.U(13.W)
    val CSR_RD   = 0x1.U(13.W)
    val CSR_WR   = 0x3.U(13.W)    // bit 1
    val CSR_XCHG = 0x5.U(13.W)    // bit 2
    val PRV_ERET = 0x9.U(13.W)    // bit 3
    val TLB_RD   = 0x11.U(13.W)   // bit 4
    val TLB_WR   = 0x21.U(13.W)   // bit 5
    val TLB_FILL = 0x41.U(13.W)   // bit 6
    val TLB_SRCH = 0x81.U(13.W)   // bit 7
    val INV_TLB  = 0x101.U(13.W)  // bit 8
    val PRV_IDLE = 0x201.U(13.W)  // bit 9
    val CACHE_OP = 0x401.U(13.W)  // bit 10
    val LS_LL    = 0x801.U(13.W)  // bit 11
    val LS_SC    = 0x1001.U(13.W) // bit 12

    // exception
    val NO_EXP    = 0.U(8.W)
    val SYS       = 0x8b.U(8.W)
    val BRK       = 0x8c.U(8.W)
    val INE       = 0x8d.U(8.W)

    // fu_id
    val RDCNT    = 0.U(3.W)
    val BR       = 1.U(3.W)
    val SYST     = 2.U(3.W)
    val MD       = 2.U(3.W)
    val LS       = 3.U(3.W)
    val ARITH    = 4.U(3.W)

    // rk_sel
    val RD       = 0.U(2.W)
    val RK       = 1.U(2.W)

    // rd_sel
    val R1       = 1.U(2.W)
    val RJ       = 2.U(2.W)

    // imm_type
    val IMM_00U   = 0.U(4.W)
    val IMM_05U   = 1.U(4.W)
    val IMM_12U   = 2.U(4.W)
    val IMM_12S   = 3.U(4.W)
    val IMM_16S   = 4.U(4.W)
    val IMM_20S   = 5.U(4.W)
    val IMM_26S   = 6.U(4.W)
    val IMM_CSR   = 7.U(4.W)
    val IMM_TID   = 8.U(4.W)
    val IMM_ERA   = 9.U(4.W)
    val IMM_COP   = 10.U(4.W)
    val IMM_14S   = 11.U(4.W)

}

