import chisel3._
import chisel3.util._
import Inst_Pack._

class IS_EX_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_IS    = Input(new inst_pack_t)
        val inst_valid_IS   = Input(Bool())

        val inst_pack_EX    = Output(new inst_pack_t)
        val inst_valid_EX   = Output(Bool())
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

    io.inst_pack_EX := inst_pack_reg
    io.inst_valid_EX := inst_valid_reg
}

