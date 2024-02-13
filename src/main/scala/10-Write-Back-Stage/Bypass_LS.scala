import chisel3._
import chisel3.util._
import CPU_Config._

class Bypass_LS_IO extends Bundle {
    val prd_ex              = Input(Vec(3, UInt(log2Ceil(PREG_NUM).W)))             
    val prj_is              = Input(UInt(log2Ceil(PREG_NUM).W))
    val prk_is              = Input(UInt(log2Ceil(PREG_NUM).W))
    val prf_wdata_ex        = Input(Vec(3, UInt(32.W)))
    val rd_valid_ex         = Input(Vec(3, Bool()))
    val forward_prj_en      = Output(Bool())
    val forward_prk_en      = Output(Bool())
    val forward_prj_data    = Output(UInt(32.W))
    val forward_prk_data    = Output(UInt(32.W))
}

class Bypass_LS extends Module {
    val io = IO(new Bypass_LS_IO)

    io.forward_prj_en := ((io.rd_valid_ex(0) && !(io.prd_ex(0) ^ io.prj_is)) || (io.rd_valid_ex(1) && !(io.prd_ex(1) ^ io.prj_is)) || (io.rd_valid_ex(2) && !(io.prd_ex(2) ^ io.prj_is))) 
    io.forward_prk_en := ((io.rd_valid_ex(0) && !(io.prd_ex(0) ^ io.prk_is)) || (io.rd_valid_ex(1) && !(io.prd_ex(1) ^ io.prk_is)) || (io.rd_valid_ex(2) && !(io.prd_ex(2) ^ io.prk_is)))
    io.forward_prj_data := Mux(!(io.prd_ex(0) ^ io.prj_is), io.prf_wdata_ex(0), Mux(!(io.prd_ex(1) ^ io.prj_is), io.prf_wdata_ex(1), io.prf_wdata_ex(2)))
    io.forward_prk_data := Mux(!(io.prd_ex(0) ^ io.prk_is), io.prf_wdata_ex(0), Mux(!(io.prd_ex(1) ^ io.prk_is), io.prf_wdata_ex(1), io.prf_wdata_ex(2)))
}




