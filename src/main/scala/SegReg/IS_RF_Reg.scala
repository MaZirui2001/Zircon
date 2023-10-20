import chisel3._
import chisel3.util._
import Inst_Pack._

class IS_RF_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_IS    = Input(new inst_pack_IS_t)

        val inst_pack_RF    = Output(new inst_pack_IS_t)
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_IS_t))

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_IS_t)
    }
    .elsewhen(!io.stall){
        inst_pack_reg := io.inst_pack_IS
    }

    io.inst_pack_RF := inst_pack_reg
}

