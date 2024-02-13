import chisel3._
import chisel3.util._

object Decode_Map{
    import Instructions._
    import Control_Signal._
    val default = List(
    // rs1_valid rs2_valid rf_we, alu_op   alu_rs1_sel alu_rs2_sel br_type mem_type iq_id, rk_sel, rd_sel, imm_type, priv_vec, exception
        N,       N,        N,     ALU_ADD, RS1_REG,   RS2_IMM,   NO_BR,  NO_MEM,  SYST, RK,     RD,     IMM_00U,  NOT_PRIV, INE)
    val map = Array(
        //                  0| 1| 2| 3|         4|        5|       6|       7|        8|      9|  10| 11|      12|       13|      
        RDCNTIDW    -> List(N, N, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RJ, IMM_TID, CSR_RD,   NO_EXP),
        RDCNTVLW    -> List(N, N, Y, ALU_ADD,   RS1_REG, RS2_CNTL, NO_BR,   NO_MEM,   RDCNT, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        RDCNTVHW    -> List(N, N, Y, ALU_ADD,   RS1_REG, RS2_CNTH, NO_BR,   NO_MEM,   RDCNT, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        ADDW        -> List(Y, Y, Y, ALU_ADD,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        SUBW        -> List(Y, Y, Y, ALU_SUB,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        SLT         -> List(Y, Y, Y, ALU_SLT,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        SLTU        -> List(Y, Y, Y, ALU_SLTU,  RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        NOR         -> List(Y, Y, Y, ALU_NOR,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        AND         -> List(Y, Y, Y, ALU_AND,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        OR          -> List(Y, Y, Y, ALU_OR,    RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        XOR         -> List(Y, Y, Y, ALU_XOR,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        SLLW        -> List(Y, Y, Y, ALU_SLL,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        SRLW        -> List(Y, Y, Y, ALU_SRL,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        SRAW        -> List(Y, Y, Y, ALU_SRA,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        MULW        -> List(Y, Y, Y, ALU_MUL,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        MULHW       -> List(Y, Y, Y, ALU_MULH,  RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        MULHWU      -> List(Y, Y, Y, ALU_MULHU, RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        DIVW        -> List(Y, Y, Y, ALU_DIV,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        MODW        -> List(Y, Y, Y, ALU_MOD,   RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        DIVWU       -> List(Y, Y, Y, ALU_DIVU,  RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        MODWU       -> List(Y, Y, Y, ALU_MODU,  RS1_REG, RS2_REG,  NO_BR,   NO_MEM,   MD,    RK, RD, IMM_00U, NOT_PRIV, NO_EXP),
        BREAK       -> List(N, N, N, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, NOT_PRIV, BRK),
        SYSCALL     -> List(N, N, N, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, NOT_PRIV, SYS),
        SLLIW       -> List(Y, N, Y, ALU_SLL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, NOT_PRIV, NO_EXP),
        SRLIW       -> List(Y, N, Y, ALU_SRL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, NOT_PRIV, NO_EXP),
        SRAIW       -> List(Y, N, Y, ALU_SRA,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, NOT_PRIV, NO_EXP),
        SLTI        -> List(Y, N, Y, ALU_SLT,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        SLTUI       -> List(Y, N, Y, ALU_SLTU,  RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        ADDIW       -> List(Y, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        ANDI        -> List(Y, N, Y, ALU_AND,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, NOT_PRIV, NO_EXP),
        ORI         -> List(Y, N, Y, ALU_OR,    RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, NOT_PRIV, NO_EXP),
        XORI        -> List(Y, N, Y, ALU_XOR,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, NOT_PRIV, NO_EXP),
        CSRRD       -> List(N, N, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_CSR, CSR_RD,   NO_EXP),
        CSRWR       -> List(N, Y, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_CSR, CSR_WR,   NO_EXP),
        CSRXCHG     -> List(Y, Y, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_CSR, CSR_XCHG, NO_EXP),
        CACOP       -> List(Y, N, N, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_LDB,  LS,    RK, RD, IMM_COP, CACHE_OP, NO_EXP),
        TLBSRCH     -> List(Y, Y, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_TID, TLB_SRCH, NO_EXP),
        TLBRD       -> List(Y, Y, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, TLB_RD,   NO_EXP),
        TLBWR       -> List(Y, Y, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, TLB_WR,   NO_EXP),
        TLBFILL     -> List(Y, Y, Y, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, TLB_FILL, NO_EXP),
        ERTN        -> List(N, N, N, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_ERA, PRV_ERET, NO_EXP),
        IDLE        -> List(N, N, N, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RD, RD, IMM_00U, PRV_IDLE, NO_EXP),
        INVTLB      -> List(Y, Y, N, ALU_MUL,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   SYST,  RK, RD, IMM_COP, INV_TLB,  NO_EXP),
        LU12IW      -> List(N, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_20S, NOT_PRIV, NO_EXP),
        PCADDU12I   -> List(N, N, Y, ALU_ADD,   RS1_PC,  RS2_IMM,  NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_20S, NOT_PRIV, NO_EXP),
        LLW         -> List(Y, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_LDW,  LS,    RK, RD, IMM_14S, LS_LL,    NO_EXP),
        SCW         -> List(Y, Y, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_STW,  LS,    RD, RD, IMM_14S, LS_SC,    NO_EXP),
        LDB         -> List(Y, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_LDB,  LS,    RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        LDH         -> List(Y, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_LDH,  LS,    RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        LDW         -> List(Y, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_LDW,  LS,    RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        STB         -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_STB,  LS,    RD, RD, IMM_12S, NOT_PRIV, NO_EXP),
        STH         -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_STH,  LS,    RD, RD, IMM_12S, NOT_PRIV, NO_EXP),
        STW         -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_STW,  LS,    RD, RD, IMM_12S, NOT_PRIV, NO_EXP),
        LDBU        -> List(Y, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_LDBU, LS,    RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        LDHU        -> List(Y, N, Y, ALU_ADD,   RS1_REG, RS2_IMM,  NO_BR,   MEM_LDHU, LS,    RK, RD, IMM_12S, NOT_PRIV, NO_EXP),
        JIRL        -> List(Y, N, Y, ALU_ADD,   RS1_PC,  RS2_FOUR, BR_JIRL, NO_MEM,   BR,    RK, RD, IMM_16S, NOT_PRIV, NO_EXP),
        B           -> List(N, N, N, ALU_ADD,   RS1_REG, RS2_IMM,  BR_B,    NO_MEM,   BR,    RK, RD, IMM_26S, NOT_PRIV, NO_EXP),
        BL          -> List(N, N, Y, ALU_ADD,   RS1_PC,  RS2_FOUR, BR_BL,   NO_MEM,   BR,    RK, R1, IMM_26S, NOT_PRIV, NO_EXP),
        BEQ         -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  BR_BEQ,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, NO_EXP),
        BNE         -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  BR_BNE,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, NO_EXP),
        BLT         -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  BR_BLT,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, NO_EXP),
        BGE         -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  BR_BGE,  NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, NO_EXP),
        BLTU        -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  BR_BLTU, NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, NO_EXP),
        BGEU        -> List(Y, Y, N, ALU_ADD,   RS1_REG, RS2_IMM,  BR_BGEU, NO_MEM,   BR,    RD, RD, IMM_16S, NOT_PRIV, NO_EXP),
    )
}

class DecodeIO extends Bundle{
    val inst            = Input(UInt(32.W))
    val rj              = Output(UInt(5.W))
    val rk              = Output(UInt(5.W))
    val rd              = Output(UInt(5.W))
    val rd_valid        = Output(Bool())

    val imm             = Output(UInt(32.W))
    val alu_op          = Output(UInt(5.W))
    val alu_rs1_sel     = Output(UInt(1.W))
    val alu_rs2_sel     = Output(UInt(2.W))
    val br_type         = Output(UInt(4.W))
    val mem_type        = Output(UInt(5.W))

    val priv_vec        = Output(UInt(13.W))

    val fu_id           = Output(UInt(3.W))
    val exception       = Output(UInt(8.W))
}
class Decode extends Module{
    val io = IO(new DecodeIO)
    val ctrl = ListLookup(io.inst, Decode_Map.default, Decode_Map.map)

    io.rj               := Mux(ctrl(0).asBool, io.inst(9, 5), 0.U(5.W))

    io.rk               := Mux(ctrl(1).asBool, Mux(ctrl(9)(0).asBool, io.inst(14, 10), io.inst(4, 0)), 0.U)

    io.rd               := MuxLookup(ctrl(10), io.inst(4, 0))(Seq(
                            Control_Signal.RD    -> io.inst(4, 0),
                            Control_Signal.R1    -> 1.U(5.W),
                            Control_Signal.RJ    -> io.inst(9, 5)))
    io.rd_valid         := ctrl(2) & io.rd =/= 0.U(5.W)

    io.alu_op           := ctrl(3)
    io.alu_rs1_sel      := ctrl(4)
    io.alu_rs2_sel      := ctrl(5)

    io.br_type          := ctrl(6)
    io.mem_type         := ctrl(7)

    io.priv_vec         := ctrl(12)
    
    io.fu_id            := ctrl(8)
    io.exception        := Mux(ctrl(12)(8) && io.inst(4, 0) >= 7.U, 1.U ## Control_Signal.INE, ctrl(13))


    def Imm_Gen(inst: UInt, imm_type: UInt): UInt = {
        val imm = Wire(UInt(32.W))
        import Control_Signal._
        imm := DontCare
        switch(imm_type) {
            is(IMM_00U)     { imm := 0.U(32.W) }
            is(IMM_05U)     { imm := Cat(0.U(27.W), inst(14, 10)) }
            is(IMM_12U)     { imm := Cat(0.U(20.W), inst(21, 10)) }
            is(IMM_12S)     { imm := Cat(Fill(20, inst(21)), inst(21, 10)) }
            is(IMM_14S)     { imm := Cat(Fill(18, inst(21)), inst(21, 10), 0.U(2.W)) }
            is(IMM_16S)     { imm := Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W)) }
            is(IMM_20S)     { imm := Cat(inst(24, 5), 0.U(12.W)) }
            is(IMM_26S)     { imm := Cat(Fill(4, inst(9)), inst(9, 0), inst(25, 10), 0.U(2.W)) }
            is(IMM_CSR)     { imm := Cat(0.U(18.W), inst(23, 10)) }
            is(IMM_TID)     { imm := 0x40.U(32.W) }
            is(IMM_ERA)     { imm := 0x6.U(32.W) }
            is(IMM_COP)     { imm := Cat(Fill(15, inst(21)), inst(21, 10), inst(4, 0))}
        }
        imm
    }

    io.imm              := Imm_Gen(io.inst, ctrl(11))
}

