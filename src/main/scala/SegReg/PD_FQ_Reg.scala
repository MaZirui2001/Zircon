import chisel3._
import chisel3.util._
import Inst_Pack._
// LUT: 14 FF: 260
class PD_FQ_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val insts_pack_PD   = Input(Vec(4, new inst_pack_IF_t))

        val insts_pack_FQ   = Output(Vec(4, new inst_pack_IF_t))
    })


    val insts_pack_reg = RegInit(VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_IF_t))))

    when(io.flush) {
        insts_pack_reg := VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_IF_t)))
    }
    .elsewhen(!io.stall){
        insts_pack_reg := io.insts_pack_PD
    }
    io.insts_pack_FQ := insts_pack_reg
}

// object IF_FQ_Reg extends App {
//     emitVerilog(new IF_FQ_Reg, Array("-td", "build/"))
// }