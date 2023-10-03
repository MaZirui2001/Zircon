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
    import RF_Func._

    val rf_we = VecInit(io.rf_we1, io.rf_we2, io.rf_we3, io.rf_we4)
    val wdata = VecInit(io.wdata1, io.wdata2, io.wdata3, io.wdata4)
    val prd = VecInit(io.prd_1, io.prd_2, io.prd_3, io.prd_4)

    io.rj_data_1 := Write_First_Read(rf_we, wdata, prd, io.prj_1, rf)
    io.rk_data_1 := Write_First_Read(rf_we, wdata, prd, io.prk_1, rf)
    io.rj_data_2 := Write_First_Read(rf_we, wdata, prd, io.prj_2, rf)
    io.rk_data_2 := Write_First_Read(rf_we, wdata, prd, io.prk_2, rf)
    io.rj_data_3 := Write_First_Read(rf_we, wdata, prd, io.prj_3, rf)
    io.rk_data_3 := Write_First_Read(rf_we, wdata, prd, io.prk_3, rf)
    io.rj_data_4 := Write_First_Read(rf_we, wdata, prd, io.prj_4, rf)
    io.rk_data_4 := Write_First_Read(rf_we, wdata, prd, io.prk_4, rf)
    

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