import chisel3._
import chisel3.util._
// LUT: 8199 FF: 2048
object RF_Func{
    def Write_First_Read(rf_we: Vec[Bool], wdata: Vec[UInt], prd: Vec[UInt], pr: UInt, rf: Vec[UInt]) : UInt = {
        val wf = Cat(
                    pr === prd(3) && rf_we(3), 
                    pr === prd(2) && rf_we(2),
                    pr === prd(1) && rf_we(1),
                    pr === prd(0) && rf_we(0)
                    )
        val wf_data = wdata(OHToUInt(wf))
        Mux(wf.orR, wf_data, rf(pr))
    }
}
class Physical_Regfile_IO extends Bundle{
    // 8 read ports
    val prj       = Input(Vec(4, UInt(6.W)))
    val prk       = Input(Vec(4, UInt(6.W)))

    val prj_data  = Output(Vec(4, UInt(32.W)))
    val prk_data  = Output(Vec(4, UInt(32.W)))

    // 4 write ports
    val prd       = Input(Vec(4, UInt(6.W)))
    val wdata     = Input(Vec(4, UInt(32.W)))
    val rf_we     = Input(Vec(4, Bool()))
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
}

// object Physical_Regfile extends App {
//     emitVerilog(new Physical_Regfile, Array("-td", "build/"))
// }