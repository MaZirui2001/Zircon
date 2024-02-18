import chisel3._
import chisel3.util._
import CPU_Config._

class Bypass_3_IO extends Bundle {
    val prd_wb              = Input(Vec(3, UInt(log2Ceil(PREG_NUM).W)))             
    val prj_ex              = Input(Vec(3, UInt(log2Ceil(PREG_NUM).W)))
    val prk_ex              = Input(Vec(3, UInt(log2Ceil(PREG_NUM).W)))
    val prf_wdata_wb        = Input(Vec(3, UInt(32.W)))
    val rd_valid_wb         = Input(Vec(3, Bool()))
    val forward_prj_en      = Output(Vec(3, Bool()))
    val forward_prk_en      = Output(Vec(3, Bool()))
    val forward_prj_data    = Output(Vec(3, UInt(32.W)))
    val forward_prk_data    = Output(Vec(3, UInt(32.W)))
}

class Bypass_3 extends Module {
    val io = IO(new Bypass_3_IO)

    for(i <- 0 until 2){
        io.forward_prj_en(i) := ((io.rd_valid_wb(0) && (io.prd_wb(0) === io.prj_ex(i))) || (io.rd_valid_wb(1) && (io.prd_wb(1) === io.prj_ex(i))))
        io.forward_prk_en(i) := ((io.rd_valid_wb(0) && (io.prd_wb(0) === io.prk_ex(i))) || (io.rd_valid_wb(1) && (io.prd_wb(1) === io.prk_ex(i))))

        io.forward_prj_data(i) := Mux((io.prd_wb(0) === io.prj_ex(i)), io.prf_wdata_wb(0), io.prf_wdata_wb(1))
        io.forward_prk_data(i) := Mux((io.prd_wb(0) === io.prk_ex(i)), io.prf_wdata_wb(0), io.prf_wdata_wb(1))
    }
    io.forward_prj_en(2) := (io.rd_valid_wb(0) && (io.prd_wb(0) === io.prj_ex(2))) || (io.rd_valid_wb(1) && (io.prd_wb(1) === io.prj_ex(2))) || (io.rd_valid_wb(2) && (io.prd_wb(2) === io.prj_ex(2)))
    io.forward_prk_en(2) := (io.rd_valid_wb(0) && (io.prd_wb(0) === io.prk_ex(2))) || (io.rd_valid_wb(1) && (io.prd_wb(1) === io.prk_ex(2))) || (io.rd_valid_wb(2) && (io.prd_wb(2) === io.prk_ex(2)))

    io.forward_prj_data(2) := Mux((io.prd_wb(0) === io.prj_ex(2)), io.prf_wdata_wb(0), Mux((io.prd_wb(1) === io.prj_ex(2)), io.prf_wdata_wb(1), io.prf_wdata_wb(2)))
    io.forward_prk_data(2) := Mux((io.prd_wb(0) === io.prk_ex(2)), io.prf_wdata_wb(0), Mux((io.prd_wb(1) === io.prk_ex(2)), io.prf_wdata_wb(1), io.prf_wdata_wb(2)))
}




