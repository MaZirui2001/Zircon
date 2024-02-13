import chisel3._
import chisel3.util._
import Inst_Pack._

class LS_RF_EX_Reg[T <: Bundle](inst_pack_t: T) extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_RF    = Input(inst_pack_t)
        val src1_RF         = Input(UInt(32.W))
        val src2_RF         = Input(UInt(32.W))
        val csr_rdata_RF    = Input(UInt(32.W))
        val uncache_RF      = Input(Bool())

        val inst_pack_EX    = Output(inst_pack_t)
        val src1_EX         = Output(UInt(32.W))
        val src2_EX         = Output(UInt(32.W))
        val csr_rdata_EX    = Output(UInt(32.W))
        val uncache_EX      = Output(Bool())
    })

    val inst_pack_reg   = RegInit(0.U.asTypeOf(inst_pack_t))
    val src1_reg        = RegInit(0.U(32.W))
    val src2_reg        = RegInit(0.U(32.W))
    val csr_rdata_reg   = RegInit(0.U(32.W))
    val uncache_reg     = RegInit(false.B)

    when(io.flush) {
        inst_pack_reg   := 0.U.asTypeOf(inst_pack_t)
    }.elsewhen(!io.stall){
        inst_pack_reg   := io.inst_pack_RF
        src1_reg        := io.src1_RF
        src2_reg        := io.src2_RF
        csr_rdata_reg   := io.csr_rdata_RF
        uncache_reg     := io.uncache_RF
    }

    io.inst_pack_EX     := inst_pack_reg
    io.src1_EX          := src1_reg
    io.src2_EX          := src2_reg
    io.csr_rdata_EX     := csr_rdata_reg
    io.uncache_EX       := uncache_reg
}

