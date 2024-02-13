import chisel3._
import chisel3.util._
import Inst_Pack._

class LS_EX_MEM_Reg extends Module {
    import CPU_Config._
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_EX    = Input(new inst_pack_IS_LS_t)
        val is_ucread_EX    = Input(Bool())
        val uncache_EX      = Input(Bool())
        val src1_EX         = Input(UInt(32.W))
        val src2_EX         = Input(UInt(32.W))
        val paddr_EX        = Input(UInt(32.W))
        val llbit_EX        = Input(Bool())
        val prd_EX          = Input(Vec(4, UInt(PREG_NUM.W)))
        val exception_EX    = Input(UInt(8.W))
        val sb_rdata_EX     = Input(UInt(32.W))
        val sb_hit_EX       = Input(Vec(4, Bool()))

        val inst_pack_MEM   = Output(new inst_pack_IS_LS_t)
        val is_ucread_MEM   = Output(Bool())
        val uncache_MEM     = Output(Bool())
        val src1_MEM        = Output(UInt(32.W))
        val src2_MEM        = Output(UInt(32.W))
        val paddr_MEM       = Output(UInt(32.W))
        val llbit_MEM       = Output(Bool())
        val prd_MEM         = Output(Vec(4, UInt(PREG_NUM.W)))
        val exception_MEM   = Output(UInt(8.W))
        val sb_rdata_MEM    = Output(UInt(32.W))
        val sb_hit_MEM      = Output(Vec(4, Bool()))
    })

    val inst_pack_reg   = RegInit(0.U.asTypeOf(new inst_pack_IS_LS_t))
    val is_ucread_Reg   = RegInit(false.B)
    val src1_reg        = RegInit(0.U(32.W))
    val src2_reg        = RegInit(0.U(32.W))
    val uncache_reg     = RegInit(false.B)
    val paddr_reg       = RegInit(0.U(32.W))
    val llbit_reg       = RegInit(false.B)
    val prd_reg         = RegInit(VecInit.fill(4)(0.U(PREG_NUM.W)))
    val exception_reg   = RegInit(0.U(8.W))
    val sb_rdata_reg    = RegInit(0.U(32.W))
    val sb_hit_reg      = RegInit(VecInit.fill(4)(false.B))


    when(io.flush) {
        inst_pack_reg   := 0.U.asTypeOf(new inst_pack_IS_LS_t)
        prd_reg         := VecInit.fill(4)(0.U(PREG_NUM.W))
    }.elsewhen(!io.stall) {
        inst_pack_reg   := io.inst_pack_EX
        is_ucread_Reg   := io.is_ucread_EX
        src1_reg        := io.src1_EX
        src2_reg        := io.src2_EX
        uncache_reg     := io.uncache_EX
        paddr_reg       := io.paddr_EX
        llbit_reg       := io.llbit_EX
        prd_reg         := io.prd_EX
        exception_reg   := io.exception_EX
        sb_rdata_reg    := io.sb_rdata_EX
        sb_hit_reg      := io.sb_hit_EX
    }

    io.inst_pack_MEM    := inst_pack_reg
    io.is_ucread_MEM    := is_ucread_Reg
    io.src1_MEM         := src1_reg
    io.src2_MEM         := src2_reg
    io.uncache_MEM      := uncache_reg
    io.paddr_MEM        := paddr_reg
    io.llbit_MEM        := llbit_reg
    io.prd_MEM          := prd_reg
    io.exception_MEM    := exception_reg
    io.sb_rdata_MEM     := sb_rdata_reg
    io.sb_hit_MEM       := sb_hit_reg

}
