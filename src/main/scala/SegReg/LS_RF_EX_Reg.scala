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
        val forward_prj_en  = Input(Bool())
        val forward_prk_en  = Input(Bool())
        val forward_prj_data= Input(UInt(32.W))
        val forward_prk_data= Input(UInt(32.W))

        val inst_pack_EX    = Output(inst_pack_t)
        val src1_EX         = Output(UInt(32.W))
        val src2_EX         = Output(UInt(32.W))
        val csr_rdata_EX    = Output(UInt(32.W))
    })

    val inst_pack_reg   = RegInit(0.U.asTypeOf(inst_pack_t))
    val src1_reg        = RegInit(0.U(32.W))
    val src2_reg        = RegInit(0.U(32.W))
    val csr_rdata_reg   = RegInit(0.U(32.W))

    when(io.flush) {
        inst_pack_reg   := 0.U.asTypeOf(inst_pack_t)
    }.elsewhen(!io.stall){
        inst_pack_reg   := io.inst_pack_RF
        src1_reg        := io.src1_RF
        src2_reg        := io.src2_RF
        csr_rdata_reg   := io.csr_rdata_RF
    }.otherwise{
        when(io.forward_prj_en){
            src1_reg    := io.forward_prj_data
        }
        when(io.forward_prk_en){
            src2_reg    := io.forward_prk_data
        }
    }

    io.inst_pack_EX     := inst_pack_reg
    io.src1_EX          := src1_reg
    io.src2_EX          := src2_reg
    io.csr_rdata_EX     := csr_rdata_reg
}


