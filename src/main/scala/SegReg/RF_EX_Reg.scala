import chisel3._
import chisel3.util._
import Inst_Pack._

class RF_EX_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_RF    = Input(new inst_pack_t)
        val inst_valid_RF   = Input(Bool())
        val src1_RF         = Input(UInt(32.W))
        val src2_RF         = Input(UInt(32.W))

        val inst_pack_EX    = Output(new inst_pack_t)
        val inst_valid_EX   = Output(Bool())
        val src1_EX         = Output(UInt(32.W))
        val src2_EX         = Output(UInt(32.W))
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_t))
    val inst_valid_reg = RegInit(false.B)
    val src1_reg = RegInit(0.U(32.W))
    val src2_reg = RegInit(0.U(32.W))

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_t)
        inst_valid_reg := false.B
        src1_reg := 0.U(32.W)
        src2_reg := 0.U(32.W)
    }
    .elsewhen(!io.stall){
        inst_pack_reg := io.inst_pack_RF
        inst_valid_reg := io.inst_valid_RF
        src1_reg := io.src1_RF
        src2_reg := io.src2_RF
    }

    io.inst_pack_EX := inst_pack_reg
    io.inst_valid_EX := inst_valid_reg
    io.src1_EX := src1_reg
    io.src2_EX := src2_reg
}


