import chisel3._
import chisel3.util._
import Inst_Pack._

class PF_IF_Reg extends Module {
    val io = IO(new Bundle{
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val inst_pack_PF    = Input(Vec(4, new inst_pack_PF_t))
        val inst_pack_IF    = Output(Vec(4, new inst_pack_PF_t))
    })

    val inst_pack_reg = RegInit(VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_PF_t))))

    when(io.flush) {
        inst_pack_reg := VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_PF_t)))
    }
    .elsewhen(!io.stall){
        inst_pack_reg := io.inst_pack_PF
    }
    io.inst_pack_IF := inst_pack_reg


}