import chisel3._
import chisel3.util._
// LUT: 112
object Inst_Pack{
    class inst_pack_t extends Bundle{
        val rj              = UInt(5.W)
        val rj_valid        = Bool()
        val prj             = UInt(6.W)
        val rk              = UInt(5.W)
        val rk_valid        = Bool()
        val prk             = UInt(6.W)
        val rd              = UInt(5.W)
        val rd_valid        = Bool()
        val pprd            = UInt(6.W)
        val imm             = UInt(32.W)
        val alu_op          = UInt(5.W)
        val alu_rs1_sel     = UInt(2.W)
        val alu_rs2_sel     = UInt(2.W)
        val br_type         = UInt(4.W)
        val mem_type        = UInt(5.W)
        val fu_id           = UInt(2.W)
        val inst_valid      = Bool()
        
    }   
}
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
    val NO_MEM       = 0.U(5.W)
    val MEM_LDB      = 16.U(5.W)
    val MEM_LDH      = 17.U(5.W)
    val MEM_LDW      = 18.U(5.W)
    val MEM_STB      = 8.U(5.W)
    val MEM_STH      = 9.U(5.W)
    val MEM_STW      = 10.U(5.W)
    val MEM_LDBU     = 20.U(5.W)
    val MEM_LDHU     = 21.U(5.W)

    // fu_id
    val ARITH    = 0.U(2.W)
    val BR       = 1.U(2.W)
    val LS       = 2.U(2.W)
    val MD       = 3.U(2.W)

    // rj_sel
    val RK       = 0.U(1.W)
    val RD       = 1.U(1.W)

    // rd_sel
    val R1       = 1.U(1.W)

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
    // rs1_valid rs2_valid rf_we, alu_op   alu_rs1_sel alu_rs2_sel br_type mem_type issue_queue_id, rk_sel, rd_sel, imm_type, inst_valid
        N,       N,        N,      ALU_ADD, RS1_ZERO,   RS2_FOUR,   NO_BR,  NO_MEM,  ARITH,         RK,     RD,     IMM_00U,  N
    )

    val map = Array(
        //                  0| 1| 2| 3|         4|        5|       6|       7|        8|     9|  10| 11|      12
        ADDW        -> List(Y, Y, Y, ALU_ADD,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        SUBW        -> List(Y, Y, Y, ALU_SUB,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        SLT         -> List(Y, Y, Y, ALU_SLT,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        SLTU        -> List(Y, Y, Y, ALU_SLTU,  RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        NOR         -> List(Y, Y, Y, ALU_NOR,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        AND         -> List(Y, Y, Y, ALU_AND,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        OR          -> List(Y, Y, Y, ALU_OR,    RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        XOR         -> List(Y, Y, Y, ALU_XOR,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        SLLW        -> List(Y, Y, Y, ALU_SLL,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        SRLW        -> List(Y, Y, Y, ALU_SRL,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        SRAW        -> List(Y, Y, Y, ALU_SRA,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        MULW        -> List(Y, Y, Y, ALU_MUL,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        MULHW       -> List(Y, Y, Y, ALU_MULH,  RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        MULHWU      -> List(Y, Y, Y, ALU_MULHU, RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        DIVW        -> List(Y, Y, Y, ALU_DIV,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        MODW        -> List(Y, Y, Y, ALU_MOD,   RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        DIVWU       -> List(Y, Y, Y, ALU_DIVU,  RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
        MODWU       -> List(Y, Y, Y, ALU_MODU,  RS1_REG,  RS2_REG, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_00U, Y),
     
        SLLIW       -> List(Y, N, Y, ALU_SLL,   RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, Y),
        SRLIW       -> List(Y, N, Y, ALU_SRL,   RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, Y),
        SRAIW       -> List(Y, N, Y, ALU_SRA,   RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_05U, Y),
        SLTI        -> List(Y, N, Y, ALU_SLT,   RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, Y),
        SLTUI       -> List(Y, N, Y, ALU_SLTU,  RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, Y),
        ADDIW       -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12S, Y),
        ANDI        -> List(Y, N, Y, ALU_AND,   RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, Y),
        ORI         -> List(Y, N, Y, ALU_OR,    RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, Y),
        XORI        -> List(Y, N, Y, ALU_XOR,   RS1_REG,  RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_12U, Y),
     
        LU12IW      -> List(N, N, Y, ALU_ADD,   RS1_ZERO, RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_20S, Y),
        PCADDU12I   -> List(N, N, Y, ALU_ADD,   RS1_PC,   RS2_IMM, NO_BR,   NO_MEM,   ARITH, RK, RD, IMM_20S, Y),
   
        LDB         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_LDB,  LS,    RK, RD, IMM_12S, Y),
        LDH         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_LDH,  LS,    RK, RD, IMM_12S, Y),
        LDW         -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_LDW,  LS,    RK, RD, IMM_12S, Y),
        STB         -> List(Y, N, N, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_STB,  LS,    RK, RD, IMM_12S, Y),
        STH         -> List(Y, N, N, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_STH,  LS,    RK, RD, IMM_12S, Y),
        STW         -> List(Y, N, N, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_STW,  LS,    RK, RD, IMM_12S, Y),
        LDBU        -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_LDBU, LS,    RK, RD, IMM_12S, Y),
        LDHU        -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, NO_BR,   MEM_LDHU, LS,    RK, RD, IMM_12S, Y),
 
        JIRL        -> List(Y, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, BR_JIRL, NO_MEM,   BR,    RK, RD, IMM_16S, Y),
        B           -> List(N, N, N, ALU_ADD,   RS1_REG,  RS2_IMM, BR_B,    NO_MEM,   BR,    RK, RD, IMM_26S, Y),
        BL          -> List(N, N, Y, ALU_ADD,   RS1_REG,  RS2_IMM, BR_BL,   NO_MEM,   BR,    RK, R1, IMM_26S, Y),
        BEQ         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM, BR_BEQ,  NO_MEM,   BR,    RD, RD, IMM_16S, Y),
        BNE         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM, BR_BNE,  NO_MEM,   BR,    RD, RD, IMM_16S, Y),
        BLT         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM, BR_BLT,  NO_MEM,   BR,    RD, RD, IMM_16S, Y),
        BGE         -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM, BR_BGE,  NO_MEM,   BR,    RD, RD, IMM_16S, Y),
        BLTU        -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM, BR_BLTU, NO_MEM,   BR,    RD, RD, IMM_16S, Y),
        BGEU        -> List(Y, Y, N, ALU_ADD,   RS1_REG,  RS2_IMM, BR_BGEU, NO_MEM,   BR,    RD, RD, IMM_16S, Y),

    )
}
class Imm_Gen extends RawModule{
    val io = IO(new Bundle{
        val inst        = Input(UInt(32.W))
        val imm_type    = Input(UInt(4.W))
        val imm         = Output(UInt(32.W))
    })

    val inst = io.inst
    val imm_type = io.imm_type
    val imm = Wire(UInt(32.W))

    import Control_Signal._
    imm := 0.U(32.W)
    switch(imm_type) {
        is(IMM_00U)     { imm := 0.U(32.W) }
        is(IMM_05U)     { imm := Cat(0.U(27.W), inst(14, 10)) }
        is(IMM_12U)     { imm := Cat(0.U(20.W), inst(21, 10)) }
        is(IMM_12S)     { imm := Cat(Fill(20, inst(21)), inst(21, 10)) }
        is(IMM_16S)     { imm := Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W)) }
        is(IMM_20S)     { imm := Cat(Fill(10, inst(24)), inst(24, 5), 0.U(2.W)) }
        is(IMM_26S)     { imm := Cat(Fill(4, inst(9)), inst(9, 0), inst(25, 10), 0.U(2.W)) }
    }
    io.imm := imm
}

class DecodeIO extends Bundle{
    val inst            = Input(UInt(32.W))
    val rj              = Output(UInt(5.W))
    val rj_valid        = Output(Bool())
    val rk              = Output(UInt(5.W))
    val rk_valid        = Output(Bool())
    val rd              = Output(UInt(5.W))
    val rd_valid        = Output(Bool())

    val imm             = Output(UInt(32.W))
    val alu_op          = Output(UInt(5.W))
    val alu_rs1_sel     = Output(UInt(2.W))
    val alu_rs2_sel     = Output(UInt(2.W))
    val br_type         = Output(UInt(4.W))
    val mem_type        = Output(UInt(5.W))

    val fu_id  = Output(UInt(2.W))
    val inst_valid      = Output(Bool())
    //val inst_pack          = Output(Inst_Pack.inst_pack_t)
}
class Decode extends RawModule{
    val io = IO(new DecodeIO)
    val ctrl = ListLookup(io.inst, Control_Signal.default, Control_Signal.map)
    val imm_gen = Module(new Imm_Gen)

    io.rj               := io.inst(9, 5)
    io.rj_valid         := ctrl(0)

    io.rk               := Mux(ctrl(9).asBool, io.inst(4, 0), io.inst(14, 10))
    io.rk_valid         := ctrl(1)

    io.rd               := Mux(ctrl(10).asBool, 1.U(5.W), io.inst(4, 0))
    io.rd_valid         := ctrl(2)

    io.alu_op           := ctrl(3)
    io.alu_rs1_sel      := ctrl(4)
    io.alu_rs2_sel      := ctrl(5)

    io.br_type          := ctrl(6)
    io.mem_type         := ctrl(7)

    io.fu_id   := ctrl(8)
    io.inst_valid       := ctrl(12)

    imm_gen.io.inst     := io.inst
    imm_gen.io.imm_type := ctrl(11)
    io.imm              := imm_gen.io.imm
}

// object Decode extends App {
//     emitVerilog(new Decode, Array("-td", "build/"))
// }
