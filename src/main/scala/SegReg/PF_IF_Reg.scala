import chisel3._
import chisel3.util._
import Inst_Pack._
import CPU_Config._

class PF_IF_Reg extends Module {
    val io = IO(new Bundle{
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_PF    = Input(Vec(2, new inst_pack_PF_t))
        val inst_pack_IF    = Output(Vec(2, new inst_pack_PF_t))
    })

    val inst_pack_reg = RegInit(VecInit(Seq.fill(2)(0.U.asTypeOf(new inst_pack_PF_t))))

    when(io.flush) {
        inst_pack_reg := VecInit(Seq.fill(2)(0.U.asTypeOf(new inst_pack_PF_t)))
    }
    .elsewhen(!io.stall){
        inst_pack_reg := io.inst_pack_PF
    }
    io.inst_pack_IF := inst_pack_reg


}