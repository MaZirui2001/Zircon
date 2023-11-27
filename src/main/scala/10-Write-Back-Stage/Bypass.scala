import chisel3._
import chisel3.util._
import CPU_Config._

class Bypass_IO extends Bundle {
    val prd_wb              = Input(UInt(log2Ceil(PREG_NUM).W))
    val prj_ex              = Input(UInt(log2Ceil(PREG_NUM).W))
    val prk_ex              = Input(UInt(log2Ceil(PREG_NUM).W))
    val prf_wdata_wb        = Input(UInt(32.W))
    val rd_valid_wb         = Input(Bool())
    val forward_prj_en      = Output(Bool())
    val forward_prk_en      = Output(Bool())
    val forward_prj_data    = Output(UInt(32.W))
    val forward_prk_data    = Output(UInt(32.W))
}

class Bypass extends Module {
    val io = IO(new Bypass_IO)

    io.forward_prj_en := (io.rd_valid_wb && (io.prd_wb === io.prj_ex))
    io.forward_prk_en := (io.rd_valid_wb && (io.prd_wb === io.prk_ex))

    io.forward_prj_data := io.prf_wdata_wb
    io.forward_prk_data := io.prf_wdata_wb
}




