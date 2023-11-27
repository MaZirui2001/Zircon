import chisel3._
import chisel3.util._
import CPU_Config._

class Busy_Board_IO extends Bundle{
    // read by insts
    val prj             = Input(Vec(4, UInt(log2Ceil(PREG_NUM).W)))
    val rj_valid        = Input(Vec(4, Bool()))
    val prk             = Input(Vec(4, UInt(log2Ceil(PREG_NUM).W)))
    val rk_valid        = Input(Vec(4, Bool()))

    val prj_busy        = Output(Vec(4, Bool()))
    val prk_busy        = Output(Vec(4, Bool()))

    // write by wakeup
    val prd_wake        = Input(Vec(5, UInt(log2Ceil(PREG_NUM).W)))
    val prd_wake_valid  = Input(Vec(5, Bool()))

    // write by dispatch 
    val prd_disp        = Input(Vec(4, UInt(log2Ceil(PREG_NUM).W)))
    val prd_disp_valid  = Input(Vec(4, Bool()))

    // flush
    val flush           = Input(Bool())
}

class Busy_Board extends Module {
    val io = IO(new Busy_Board_IO)
    val busy_board = RegInit(VecInit(Seq.fill(85)(false.B)))

    // read by insts
    for(i <- 0 until 4){
        io.prj_busy(i) := busy_board(io.prj(i))
        io.prk_busy(i) := busy_board(io.prk(i))
    }

    // write by wakeup
    for(i <- 0 until 5){
        when(io.prd_wake_valid(i)){
            busy_board(io.prd_wake(i)) := false.B
        }
    }
    // write by dispatch
    for(i <- 0 until 4){
        when(io.prd_disp_valid(i)){
            busy_board(io.prd_disp(i)) := true.B
        }
    }

    when(io.flush){
        busy_board := VecInit(Seq.fill(85)(false.B))
    }
}