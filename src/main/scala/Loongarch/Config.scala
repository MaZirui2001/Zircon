
import chisel3._
import chisel3.util._

object Control_Signal{
    val Y = true.B
    val N = false.B

    // alu_op
    val ALU_ADD   = 0.U(5.W)
    val ALU_SUB   = 1.U(5.W)
    val ALU_SLT   = 2.U(5.W)
    val ALU_SLTU  = 3.U(5.W)
    val ALU_NOR   = 4.U(5.W)
    val ALU_AND   = 5.U(5.W)
    val ALU_OR    = 6.U(5.W)
    val ALU_XOR   = 7.U(5.W)
    val ALU_SLL   = 8.U(5.W)
    val ALU_SRL   = 9.U(5.W)
    val ALU_SRA   = 10.U(5.W)
    val ALU_MUL   = 13.U(5.W)
    val ALU_MULH  = 14.U(5.W)
    val ALU_MULHU = 15.U(5.W)
    val ALU_DIV   = 16.U(5.W)
    val ALU_MOD   = 17.U(5.W)
    val ALU_DIVU  = 18.U(5.W)
    val ALU_MODU  = 19.U(5.W)


    // alu_rs1_sel
    val RS1_REG  = 0.U(2.W)
    val RS1_PC   = 1.U(2.W)
    val RS1_ZERO = 2.U(2.W)
    val RS1_CNTH = 3.U(2.W)

    // alu_rs2_sel
    val RS2_REG  = 0.U(2.W)
    val RS2_IMM  = 1.U(2.W)
    val RS2_FOUR = 2.U(2.W)
    val RS2_CSR  = 2.U(2.W)
    val RS2_CNTL = 3.U(2.W)

    // br_type
    val NO_BR    = 0.U(4.W)
    val BR_JIRL  = 1.U(4.W)
    val BR_B     = 2.U(4.W)
    val BR_BL    = 3.U(4.W)
    val BR_BEQ   = 4.U(4.W)
    val BR_BNE   = 5.U(4.W)
    val BR_BLT   = 6.U(4.W)
    val BR_BGE   = 7.U(4.W)
    val BR_BLTU  = 8.U(4.W)
    val BR_BGEU  = 9.U(4.W)

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
    val NOT_PRIV = 0.U(4.W)
    val CSR_RD   = 1.U(4.W)
    val CSR_WR   = 3.U(4.W) // bit 1
    val CSR_XCHG = 5.U(4.W) // bit 2
    val PRV_ERET = 9.U(4.W) // bit 3

    // csr_sel
    val FROM_INST = 0.U(2.W)
    val FROM_TID  = 1.U(2.W)
    val FROM_ERA  = 2.U(2.W)

    // exception
    val NO_EXP    = 0.U(8.W)
    val SYS       = 0x8b.U(8.W)
    val BRK       = 0x8c.U(8.W)
    val INE       = 0x8d.U(8.W)

    // fu_id
    val SYST     = 0.U(3.W)
    val BR       = 1.U(3.W)
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

    import Instructions._

    val default = List(
    // rs1_valid rs2_valid rf_we, alu_op   alu_rs1_sel alu_rs2_sel br_type mem_type iq_id, rk_sel, rd_sel, imm_type, priv_vec, csr_sel,   exception
        N,       N,        N,     ALU_ADD, RS1_ZERO,   RS2_FOUR,   NO_BR,  NO_MEM,  ARITH, RK,     RD,     IMM_00U,  NOT_PRIV, FROM_INST, INE)

    val map = Array(
        //                  0| 1| 2| 3|         4|        5|       6|       7|        8|      9|  10| 11|      12|       13|       |14     
        RDCNTIDW    -> List(N, N, Y, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   SYST,  RD, RJ, IMM_00U, NOT_PRIV, FROM_TID,  NO_EXP),
        RDCNTVLW    -> List(N, N, Y, ALU_ADD,   RS1_ZERO, RS2_CNTL, NO_BR,   NO_MEM,   SYST,  RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        RDCNTVHW    -> List(N, N, Y, ALU_ADD,   RS1_CNTH, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        ADDW        -> List(Y, Y, Y, ALU_ADD,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        SUBW        -> List(Y, Y, Y, ALU_SUB,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        SLT         -> List(Y, Y, Y, ALU_SLT,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        SLTU        -> List(Y, Y, Y, ALU_SLTU,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        NOR         -> List(Y, Y, Y, ALU_NOR,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        AND         -> List(Y, Y, Y, ALU_AND,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        OR          -> List(Y, Y, Y, ALU_OR,    RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        XOR         -> List(Y, Y, Y, ALU_XOR,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        SLLW        -> List(Y, Y, Y, ALU_SLL,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        SRLW        -> List(Y, Y, Y, ALU_SRL,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        SRAW        -> List(Y, Y, Y, ALU_SRA,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        MULW        -> List(Y, Y, Y, ALU_MUL,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        MULHW       -> List(Y, Y, Y, ALU_MULH,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        MULHWU      -> List(Y, Y, Y, ALU_MULHU, RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        DIVW        -> List(Y, Y, Y, ALU_DIV,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        MODW        -> List(Y, Y, Y, ALU_MOD,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        DIVWU       -> List(Y, Y, Y, ALU_DIVU,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        MODWU       -> List(Y, Y, Y, ALU_MODU,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, FROM_INST, NO_EXP),
        CSRRD       -> List(N, N, Y, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, CSR_RD,   FROM_INST, NO_EXP),
        CSRWR       -> List(N, Y, Y, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, CSR_WR,   FROM_INST, NO_EXP),
        CSRXCHG     -> List(Y, Y, Y, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, CSR_XCHG, FROM_INST, NO_EXP),
        BREAK       -> List(N, N, N, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, NOT_PRIV, FROM_INST, BRK),
        SYSCALL     -> List(N, N, N, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, NOT_PRIV, FROM_INST, SYS),
        SLLIW       -> List(Y, N, Y, ALU_SLL,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, NOT_PRIV, FROM_INST, NO_EXP),
        SRLIW       -> List(Y, N, Y, ALU_SRL,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, NOT_PRIV, FROM_INST, NO_EXP),
        SRAIW       -> List(Y, N, Y, ALU_SRA,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, NOT_PRIV, FROM_INST, NO_EXP),
        SLTI        -> List(Y, N, Y, ALU_SLT,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        SLTUI       -> List(Y, N, Y, ALU_SLTU,  RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        ADDIW       -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        ANDI        -> List(Y, N, Y, ALU_AND,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, NOT_PRIV, FROM_INST, NO_EXP),
        ORI         -> List(Y, N, Y, ALU_OR,    RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, NOT_PRIV, FROM_INST, NO_EXP),
        XORI        -> List(Y, N, Y, ALU_XOR,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, NOT_PRIV, FROM_INST, NO_EXP),
        ERTN        -> List(N, N, N, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, PRV_ERET, FROM_ERA,  NO_EXP),
        LU12IW      -> List(N, N, Y, ALU_ADD,   RS1_ZERO, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_20S, NOT_PRIV, FROM_INST, NO_EXP),
        PCADDU12I   -> List(N, N, Y, ALU_ADD,   RS1_PC,   RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_20S, NOT_PRIV, FROM_INST, NO_EXP),
        LDB         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDB,  LS,    RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        LDH         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDH,  LS,    RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        LDW         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDW,  LS,    RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        STB         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_STB,  LS,    RD, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        STH         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_STH,  LS,    RD, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        STW         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_STW,  LS,    RD, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        LDBU        -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDBU, LS,    RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        LDHU        -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDHU, LS,    RK, RD, IMM_12S, NOT_PRIV, FROM_INST, NO_EXP),
        JIRL        -> List(Y, N, Y, ALU_ADD,   RS1_PC,   RS2_FOUR, BR_JIRL, NO_MEM,   BR,    RK, RD, IMM_16S, NOT_PRIV, FROM_INST, NO_EXP),
        B           -> List(N, N, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_B,    NO_MEM,   BR,    RK, RD, IMM_26S, NOT_PRIV, FROM_INST, NO_EXP),
        BL          -> List(N, N, Y, ALU_ADD,   RS1_PC,   RS2_FOUR, BR_BL,   NO_MEM,   BR,    RK, R1, IMM_26S, NOT_PRIV, FROM_INST, NO_EXP),
        BEQ         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BEQ,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, FROM_INST, NO_EXP),
        BNE         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BNE,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, FROM_INST, NO_EXP),
        BLT         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BLT,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, FROM_INST, NO_EXP),
        BGE         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BGE,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, FROM_INST, NO_EXP),
        BLTU        -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BLTU, NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, FROM_INST, NO_EXP),
        BGEU        -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BGEU, NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, FROM_INST, NO_EXP),
    )
}

object CSR_CONFIG {
    val CSR_CRMD        = 0x0.U(14.W)
    val CSR_PRMD        = 0x1.U(14.W)
    val CSR_EUEN        = 0x2.U(14.W)
    val CSR_ECFG        = 0x4.U(14.W)
    val CSR_ESTAT       = 0x5.U(14.W)
    val CSR_ERA         = 0x6.U(14.W)
    val CSR_BADV        = 0x7.U(14.W)
    val CSR_EENTRY      = 0xc.U(14.W)
    val CSR_TLBIDX      = 0x10.U(14.W)
    val CSR_TLBEHI      = 0x11.U(14.W)
    val CSR_TLBELO0     = 0x12.U(14.W)
    val CSR_TLBELO1     = 0x13.U(14.W)
    val CSR_ASID        = 0x18.U(14.W)
    val CSR_PGDL        = 0x19.U(14.W)
    val CSR_PGDH        = 0x1a.U(14.W)
    val CSR_PGD         = 0x1b.U(14.W)
    val CSR_CPUID       = 0x20.U(14.W)
    val CSR_SAVE0       = 0x30.U(14.W)
    val CSR_SAVE1       = 0x31.U(14.W)
    val CSR_SAVE2       = 0x32.U(14.W)
    val CSR_SAVE3       = 0x33.U(14.W)
    val CSR_TID         = 0x40.U(14.W)
    val CSR_TCFG        = 0x41.U(14.W)
    val CSR_TVAL        = 0x42.U(14.W)
    val CSR_TICLR       = 0x44.U(14.W)
    val CSR_LLBCTL      = 0x60.U(14.W)
    val CSR_TLBRENTRY   = 0x88.U(14.W)
    val CSR_CTAG        = 0x98.U(14.W)
    val CSR_DMW0        = 0x180.U(14.W)
    val CSR_DMW1        = 0x181.U(14.W)
}

object EXCEPTION{
    val INT     = 0x00.U(7.W) // interrupt
    val PIL     = 0x01.U(7.W) // page illegal load
    val PIS     = 0x02.U(7.W) // page illegal store
    val PIF     = 0x03.U(7.W) // page illegal fetch
    val PME     = 0x04.U(7.W) // page maintain exception
    val PPI     = 0x07.U(7.W) // page privilege illegal
    val ADEF    = 0x08.U(7.W) // address exception fetch
    val ADEM    = 0x48.U(7.W) // address exception memory
    val ALE     = 0x09.U(7.W) // address align exception
    val SYS     = 0x0b.U(7.W) // system call
    val BRK     = 0x0c.U(7.W) // breakpoint
    val INE     = 0x0d.U(7.W) // instruction not exist
    val IPE     = 0x0e.U(7.W) // instruction privilege exception
    val FPD     = 0x0f.U(7.W) // floating point disable
    val FPE     = 0x12.U(7.W) // floating point exception
    val TLBR    = 0x3F.U(7.W) // TLB refill

}