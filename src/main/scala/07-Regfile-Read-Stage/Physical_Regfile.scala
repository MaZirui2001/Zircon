import chisel3._
import chisel3.util._
// LUT: 8199 FF: 2048

class Physical_Regfile_IO(n: Int) extends Bundle{
    // 8 read ports
    val prj       = Input(Vec(4, UInt(log2Ceil(n).W)))
    val prk       = Input(Vec(4, UInt(log2Ceil(n).W)))

    val prj_data  = Output(Vec(4, UInt(32.W)))
    val prk_data  = Output(Vec(4, UInt(32.W)))

    // 4 write ports
    val prd       = Input(Vec(4, UInt(log2Ceil(n).W)))
    val wdata     = Input(Vec(4, UInt(32.W)))
    val rf_we     = Input(Vec(4, Bool()))
}

class Physical_Regfile(n: Int) extends Module{
    val io = IO(new Physical_Regfile_IO(n))

    val rf = RegInit(VecInit.fill(n)(0.U(32.W)))
    //val rf = Reg(Vec(n, UInt(32.W)))

    // read, write first regfile
    import RF_Func._

    val rf_we = io.rf_we
    val wdata = io.wdata
    val prd = io.prd

    for(i <- 0 until 4){
        io.prj_data(i) := Write_First_Read(rf_we, wdata, prd, io.prj(i), rf)
        io.prk_data(i) := Write_First_Read(rf_we, wdata, prd, io.prk(i), rf)
    }
    for(i <- 0 until 4){
        when(io.rf_we(i)){
            rf(io.prd(i)) := io.wdata(i)
        }
    }

    // when(reset.asBool){
    //     rf(0) := 0.U
    // }
}