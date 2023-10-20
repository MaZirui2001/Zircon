import chisel3._
import chisel3.util._
import Inst_Pack._

// LUT: 38 FF: 520

class RN_DP_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val insts_pack_RN   = Input(Vec(4, new inst_pack_RN_t))

        val insts_pack_DP   = Output(Vec(4, new inst_pack_RN_t))

    })

    val insts_pack_reg = RegInit(VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_RN_t))))

    when(io.flush) {
        insts_pack_reg := VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_RN_t)))
    }
    .elsewhen(!io.stall){
        insts_pack_reg := io.insts_pack_RN
    }

    io.insts_pack_DP := insts_pack_reg


}

// object RN_DP_Reg extends App {
//     emitVerilog(new RN_DP_Reg, Array("-td", "build/"))
// }