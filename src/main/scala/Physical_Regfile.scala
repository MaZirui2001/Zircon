import chisel3._
import chisel3.util._
// LUT: 8059 FF: 2048
class Physical_Regfile_IO extends Bundle{
    // 8 read ports
    val prj_1       = Input(UInt(6.W))
    val prk_1       = Input(UInt(6.W))
    val prj_2       = Input(UInt(6.W))
    val prk_2       = Input(UInt(6.W))
    val prj_3       = Input(UInt(6.W))
    val prk_3       = Input(UInt(6.W))
    val prj_4       = Input(UInt(6.W))
    val prk_4       = Input(UInt(6.W))

    val rj_data_1  = Output(UInt(32.W))
    val rk_data_1  = Output(UInt(32.W))
    val rj_data_2  = Output(UInt(32.W))
    val rk_data_2  = Output(UInt(32.W))
    val rj_data_3  = Output(UInt(32.W))
    val rk_data_3  = Output(UInt(32.W))
    val rj_data_4  = Output(UInt(32.W))
    val rk_data_4  = Output(UInt(32.W))

    // 4 write ports
    val prd_1       = Input(UInt(6.W))
    val prd_2       = Input(UInt(6.W))
    val prd_3       = Input(UInt(6.W))
    val prd_4       = Input(UInt(6.W))
    val wdata1      = Input(UInt(32.W))
    val wdata2      = Input(UInt(32.W))
    val wdata3      = Input(UInt(32.W))
    val wdata4      = Input(UInt(32.W))
    val rf_we1      = Input(Bool())
    val rf_we2      = Input(Bool())
    val rf_we3      = Input(Bool())
    val rf_we4      = Input(Bool())
}

class Physical_Regfile extends Module{
    val io = IO(new Physical_Regfile_IO)

    val rf = Reg(Vec(64, UInt(32.W)))
    when(reset.asBool){
        for(i <- 0 until 64){
            rf(i) := 0.U
        }
    }

    // read, write first regfile
    // for arithmetic instructions
    val rj_data_1 = Mux(io.prj_1 === io.prd_1 && io.rf_we1, io.wdata1, 
                    Mux(io.prj_1 === io.prd_2 && io.rf_we2, io.wdata2, rf(io.prj_1)))
    val rk_data_1 = Mux(io.prk_1 === io.prd_1 && io.rf_we1, io.wdata1,
                    Mux(io.prk_1 === io.prd_2 && io.rf_we2, io.wdata2, rf(io.prk_1)))
    val rj_data_2 = Mux(io.prj_2 === io.prd_1 && io.rf_we1, io.wdata1, 
                    Mux(io.prj_2 === io.prd_2 && io.rf_we2, io.wdata2, rf(io.prj_2)))
    val rk_data_2 = Mux(io.prk_2 === io.prd_1 && io.rf_we1, io.wdata1,
                    Mux(io.prk_2 === io.prd_2 && io.rf_we2, io.wdata2, rf(io.prk_2)))
    // for load instructions
    val rj_data_3 = Mux(io.prj_3 === io.prd_3 && io.rf_we3, io.wdata3, rf(io.prj_3))
    val rk_data_3 = Mux(io.prk_3 === io.prd_3 && io.rf_we3, io.wdata3, rf(io.prk_3))

    // for mul and div instructions
    val rj_data_4 = Mux(io.prj_4 === io.prd_4 && io.rf_we4, io.wdata4, rf(io.prj_4))
    val rk_data_4 = Mux(io.prk_4 === io.prd_4 && io.rf_we4, io.wdata4, rf(io.prk_4)) 


    io.rj_data_1 := rj_data_1
    io.rk_data_1 := rk_data_1
    io.rj_data_2 := rj_data_2
    io.rk_data_2 := rk_data_2
    io.rj_data_3 := rj_data_3
    io.rk_data_3 := rk_data_3
    io.rj_data_4 := rj_data_4
    io.rk_data_4 := rk_data_4
    

    // write first memory, if raddr == waddr, give wdata
    when(io.rf_we1){
        rf(io.prd_1) := io.wdata1
    }
    when(io.rf_we2){
        rf(io.prd_2) := io.wdata2
    }
    when(io.rf_we3){
        rf(io.prd_3) := io.wdata3
    }
    when(io.rf_we4){
        rf(io.prd_4) := io.wdata4
    }
}

object Physical_Regfile extends App {
    emitVerilog(new Physical_Regfile, Array("-td", "build/"))
}