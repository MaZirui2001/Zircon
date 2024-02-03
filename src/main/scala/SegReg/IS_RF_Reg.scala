import chisel3._
import chisel3.util._
import Inst_Pack._

class IS_RF_Reg[T <: Bundle](inst_pack_t: T) extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_IS    = Input(inst_pack_t)

        val inst_pack_RF    = Output(inst_pack_t)
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(inst_pack_t))

    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(inst_pack_t)
    }.elsewhen(!io.stall){
        inst_pack_reg := io.inst_pack_IS
    }

    io.inst_pack_RF := inst_pack_reg
}
