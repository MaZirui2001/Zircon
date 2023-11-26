
import chisel3._
import chisel3.util._

object Control_Signal{
    val Y = true.B
    val N = false.B

    // alu_op
    val ALU_ADD     = 0.U(5.W)
    val ALU_SUB     = 1.U(5.W)
    val ALU_SLT     = 2.U(5.W)
    val ALU_SLTU    = 3.U(5.W)
    val ALU_NOR     = 4.U(5.W)
    val ALU_AND     = 5.U(5.W)
    val ALU_OR      = 6.U(5.W)
    val ALU_XOR     = 7.U(5.W)
    val ALU_SLL     = 8.U(5.W)
    val ALU_SRL     = 9.U(5.W)
    val ALU_SRA     = 10.U(5.W)
    val ALU_MUL     = 11.U(5.W)
    val ALU_MULH    = 12.U(5.W)
    val ALU_MULHU   = 13.U(5.W)
    val ALU_DIV     = 14.U(5.W)
    val ALU_MOD     = 15.U(5.W)
    val ALU_DIVU    = 16.U(5.W)
    val ALU_MODU    = 17.U(5.W)

    // alu_rs1_sel
    val RS1_REG  = 0.U(2.W)
    val RS1_PC   = 1.U(2.W)
    val RS1_ZERO = 2.U(2.W)

    // alu_rs2_sel
    val RS2_REG  = 0.U(2.W)
    val RS2_IMM  = 1.U(2.W)
    val RS2_FOUR = 2.U(2.W)
    val RS2_CSR  = 2.U(2.W)
    val RS2_CNT  = 2.U(2.W)

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
    val CNT_ID   = 2.U(4.W)
    val CSR_WR   = 3.U(4.W) // bit 1
    val CSR_XCHG = 5.U(4.W) // bit 2

    // csr_sel
    val FROM_INST = 0.U(1.W)
    val FROM_TID  = 1.U(1.W)


    // fu_id
    val RDCNT    = 0.U(3.W)
    val CSR      = 1.U(3.W)
    val BR       = 2.U(3.W)
    val MD       = 3.U(3.W)
    val LS       = 4.U(3.W)
    val ARITH    = 5.U(3.W)

    // rk_sel
    val RD       = 0.U(1.W)
    val RK       = 1.U(1.W)

    // rd_sel
    val R1       = 1.U  (1.W)

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
    // rs1_valid rs2_valid rf_we, alu_op   alu_rs1_sel alu_rs2_sel br_type mem_type issue_queue_id, rk_sel, rd_sel, imm_type, priv_vec, csr_sel, inst_exist
        N,       N,        N,      ALU_ADD, RS1_ZERO,   RS2_FOUR,   NO_BR,  NO_MEM,  ARITH,         RK,     RD,     IMM_00U,  NOT_PRIV,  FROM_INST, N
    )

    val map = Array(
        //                  0| 1| 2| 3|         4|        5|       6|       7|        8|      9|         10|      11|      12|       13|       14
        ADDW        -> List(Y, Y, Y, ALU_ADD,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        SUBW        -> List(Y, Y, Y, ALU_SUB,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        SLT         -> List(Y, Y, Y, ALU_SLT,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        SLTU        -> List(Y, Y, Y, ALU_SLTU,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        NOR         -> List(Y, Y, Y, ALU_NOR,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        AND         -> List(Y, Y, Y, ALU_AND,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        OR          -> List(Y, Y, Y, ALU_OR,    RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        XOR         -> List(Y, Y, Y, ALU_XOR,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        SLLW        -> List(Y, Y, Y, ALU_SLL,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        SRLW        -> List(Y, Y, Y, ALU_SRL,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        SRAW        -> List(Y, Y, Y, ALU_SRA,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        MULW        -> List(Y, Y, Y, ALU_MUL,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        MULHW       -> List(Y, Y, Y, ALU_MULH,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        MULHWU      -> List(Y, Y, Y, ALU_MULHU, RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        DIVW        -> List(Y, Y, Y, ALU_DIV,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        MODW        -> List(Y, Y, Y, ALU_MOD,   RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        DIVWU       -> List(Y, Y, Y, ALU_DIVU,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),
        MODWU       -> List(Y, Y, Y, ALU_MODU,  RS1_REG,  RS2_REG,  NO_BR,   NO_MEM,   MD,    RK,       RD,      IMM_00U,  NOT_PRIV, FROM_INST, Y),

        CSRRD       -> List(N, N, Y, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   CSR,   RD,       RD,      IMM_00U,  CSR_RD,   FROM_INST, Y),
        CSRWR       -> List(N, Y, Y, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   CSR,   RD,       RD,      IMM_00U,  CSR_WR,   FROM_INST, Y),
        CSRXCHG     -> List(Y, Y, Y, ALU_ADD,   RS1_ZERO, RS2_CSR,  NO_BR,   NO_MEM,   CSR,   RD,       RD,      IMM_00U,  CSR_XCHG, FROM_INST, Y),
      
        SLLIW       -> List(Y, N, Y, ALU_SLL,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_05U,  NOT_PRIV, FROM_INST, Y),
        SRLIW       -> List(Y, N, Y, ALU_SRL,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_05U,  NOT_PRIV, FROM_INST, Y),
        SRAIW       -> List(Y, N, Y, ALU_SRA,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_05U,  NOT_PRIV, FROM_INST, Y),
        SLTI        -> List(Y, N, Y, ALU_SLT,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        SLTUI       -> List(Y, N, Y, ALU_SLTU,  RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        ADDIW       -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        ANDI        -> List(Y, N, Y, ALU_AND,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_12U,  NOT_PRIV, FROM_INST, Y),
        ORI         -> List(Y, N, Y, ALU_OR,    RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_12U,  NOT_PRIV, FROM_INST, Y),
        XORI        -> List(Y, N, Y, ALU_XOR,   RS1_REG,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_12U,  NOT_PRIV, FROM_INST, Y),
      
        LU12IW      -> List(N, N, Y, ALU_ADD,   RS1_ZERO, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_20S,  NOT_PRIV, FROM_INST, Y),
        PCADDU12I   -> List(N, N, Y, ALU_ADD,   RS1_PC,   RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK,       RD,      IMM_20S,  NOT_PRIV, FROM_INST, Y),
    
        LDB         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDB,  LS,    RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        LDH         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDH,  LS,    RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        LDW         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDW,  LS,    RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        STB         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_STB,  LS,    RD,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        STH         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_STH,  LS,    RD,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        STW         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_STW,  LS,    RD,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        LDBU        -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDBU, LS,    RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
        LDHU        -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM,  NO_BR,   MEM_LDHU, LS,    RK,       RD,      IMM_12S,  NOT_PRIV, FROM_INST, Y),
 
        JIRL        -> List(Y, N, Y, ALU_ADD,   RS1_PC,   RS2_FOUR, BR_JIRL, NO_MEM,   BR,    RK,       RD,      IMM_16S,  NOT_PRIV, FROM_INST, Y),
        B           -> List(N, N, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_B,    NO_MEM,   BR,    RK,       RD,      IMM_26S,  NOT_PRIV, FROM_INST, Y),
        BL          -> List(N, N, Y, ALU_ADD,   RS1_PC,   RS2_FOUR, BR_BL,   NO_MEM,   BR,    RK,       R1,      IMM_26S,  NOT_PRIV, FROM_INST, Y),
        BEQ         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BEQ,  NO_MEM,   BR,    RD,       RD,      IMM_16S,  NOT_PRIV, FROM_INST, Y),
        BNE         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BNE,  NO_MEM,   BR,    RD,       RD,      IMM_16S,  NOT_PRIV, FROM_INST, Y),
        BLT         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BLT,  NO_MEM,   BR,    RD,       RD,      IMM_16S,  NOT_PRIV, FROM_INST, Y),
        BGE         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BGE,  NO_MEM,   BR,    RD,       RD,      IMM_16S,  NOT_PRIV, FROM_INST, Y),
        BLTU        -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BLTU, NO_MEM,   BR,    RD,       RD,      IMM_16S,  NOT_PRIV, FROM_INST, Y),
        BGEU        -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM,  BR_BGEU, NO_MEM,   BR,    RD,       RD,      IMM_16S,  NOT_PRIV, FROM_INST, Y),

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