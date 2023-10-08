import chisel3._
import chisel3.util._
import Inst_Pack._

class IS_RF_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_IS    = Input(new inst_pack_t)
        val inst_valid_IS   = Input(Bool())

        val inst_pack_RF    = Output(new inst_pack_t)
        val inst_valid_RF   = Output(Bool())
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_t))
    val inst_valid_reg = RegInit(false.B)

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_t)
        inst_valid_reg := false.B
    }
    .elsewhen(!io.stall){
        inst_pack_reg := io.inst_pack_IS
        inst_valid_reg := io.inst_valid_IS
    }

    io.inst_pack_RF := inst_pack_reg
    io.inst_valid_RF := inst_valid_reg
}

