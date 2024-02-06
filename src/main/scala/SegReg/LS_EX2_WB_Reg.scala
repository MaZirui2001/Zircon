import chisel3._
import chisel3.util._
import Inst_Pack._

class LS_EX2_WB_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_EX2   = Input(new inst_pack_IS_LS_t)
        val vaddr_EX2       = Input(UInt(32.W))
        val exception_EX2   = Input(UInt(8.W))
        val mem_rdata_EX2   = Input(UInt(32.W))
        val sb_rdata_EX2    = Input(UInt(32.W))
        val llbit_EX2       = Input(Bool())
        val sb_hit_EX2      = Input(Vec(4, Bool()))
        val is_ucread_EX2   = Input(Bool())
    
        val inst_pack_WB    = Output(new inst_pack_IS_LS_t)
        val vaddr_WB        = Output(UInt(32.W))
        val exception_WB    = Output(UInt(8.W))
        val mem_rdata_WB    = Output(UInt(32.W))
        val sb_rdata_WB     = Output(UInt(32.W))
        val llbit_WB        = Output(Bool())
        val sb_hit_WB       = Output(Vec(4, Bool()))
        val is_ucread_WB    = Output(Bool())
    })
    
    val inst_pack_reg   = RegInit(0.U.asTypeOf(new inst_pack_IS_LS_t))
    val vaddr_reg       = RegInit(0.U(32.W))
    val exception_reg   = RegInit(0.U(8.W))
    val mem_rdata_reg   = RegInit(0.U(32.W))
    val sb_rdata_reg    = RegInit(0.U(32.W))
    val llbit_reg       = RegInit(false.B)
    val sb_hit_reg      = RegInit(VecInit.fill(4)(false.B))
    val is_ucread_reg   = RegInit(false.B)

    
    when(io.flush) {
        inst_pack_reg   := 0.U.asTypeOf(new inst_pack_IS_LS_t)
    }.elsewhen(!io.stall) {
        inst_pack_reg   := io.inst_pack_EX2
        vaddr_reg       := io.vaddr_EX2
        exception_reg   := io.exception_EX2
        mem_rdata_reg   := io.mem_rdata_EX2
        sb_rdata_reg    := io.sb_rdata_EX2
        llbit_reg       := io.llbit_EX2
        sb_hit_reg      := io.sb_hit_EX2
        is_ucread_reg   := io.is_ucread_EX2
    }

    io.inst_pack_WB     := inst_pack_reg
    io.vaddr_WB         := vaddr_reg
    io.exception_WB     := exception_reg
    io.mem_rdata_WB     := mem_rdata_reg
    io.sb_rdata_WB      := sb_rdata_reg
    io.llbit_WB         := llbit_reg
    io.sb_hit_WB        := sb_hit_reg
    io.is_ucread_WB     := is_ucread_reg

}