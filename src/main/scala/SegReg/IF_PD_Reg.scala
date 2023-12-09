import chisel3._
import chisel3.util._
import Inst_Pack._
import CPU_Config._
// LUT: 1FRONT_WIDTH FF: 260
class IF_PD_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val insts_pack_IF   = Input(Vec(FRONT_WIDTH, new inst_pack_IF_t))

        val insts_pack_PD   = Output(Vec(FRONT_WIDTH, new inst_pack_IF_t))
    })


    val insts_pack_reg = RegInit(VecInit(Seq.fill(FRONT_WIDTH)(0.U.asTypeOf(new inst_pack_IF_t))))

    when(io.flush) {
        insts_pack_reg := VecInit(Seq.fill(FRONT_WIDTH)(0.U.asTypeOf(new inst_pack_IF_t)))
    }
    .elsewhen(!io.stall){
        insts_pack_reg := io.insts_pack_IF
    }
    io.insts_pack_PD := insts_pack_reg
}

// object IF_FQ_Reg extends App {
//     emitVerilog(new IF_FQ_Reg, Array("-td", "build/"))
// }