
import chisel3._
import chisel3.util._
import Inst_Pack._

class MD_EX_WB_Reg extends Module {
    val io = IO(new Bundle {
        val flush = Input(Bool())
        val stall = Input(Bool())
        val inst_pack_EX = Input(new inst_pack_t)
        val md_out_EX = Input(UInt(32.W))
        val inst_valid_EX = Input(Bool())
    
        val inst_pack_WB = Output(new inst_pack_t)
        val md_out_WB = Output(UInt(32.W))
        val inst_valid_WB = Output(Bool())
    })  

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_t))
    val md_out_reg = RegInit(0.U(32.W))
    val inst_valid_reg = RegInit(false.B)
    
    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_t)
        md_out_reg := 0.U
        inst_valid_reg := false.B
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX
        md_out_reg := io.md_out_EX
        inst_valid_reg := io.inst_valid_EX
    }

    io.inst_pack_WB := inst_pack_reg
    io.md_out_WB := md_out_reg
    io.inst_valid_WB := inst_valid_reg
}