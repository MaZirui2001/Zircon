import chisel3._
import chisel3.util._
import Inst_Pack._

// LUT: 22 FF; 408

class ID_RN_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())

        val insts_pack_ID   = Input(Vec(4, new inst_pack_ID_t))
        val insts_pack_RN   = Output(Vec(4, new inst_pack_ID_t))

    })

    val insts_pack_reg = RegInit(VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_ID_t))))

    when(io.flush) {
        insts_pack_reg := VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_ID_t)))
    }
    .elsewhen(!io.stall){
        insts_pack_reg := io.insts_pack_ID
    }
    io.insts_pack_RN := insts_pack_reg
}
