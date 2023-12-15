import chisel3._
import chisel3.util._
import Inst_Pack._
import CPU_Config._

// LUT: 22 FF; 408

class ID_RN_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())

        val insts_pack_ID   = Input(Vec(2, new inst_pack_ID_t))
        val alloc_preg_ID    = Input(Vec(2, UInt(log2Ceil(PREG_NUM).W)))
        val insts_pack_RN   = Output(Vec(2, new inst_pack_ID_t))
        val alloc_preg_RN    = Output(Vec(2, UInt(log2Ceil(PREG_NUM).W)))

    })

    val insts_pack_reg = RegInit(VecInit(Seq.fill(2)(0.U.asTypeOf(new inst_pack_ID_t))))
    val alloc_preg_reg = RegInit(VecInit(Seq.fill(2)(0.U(log2Ceil(PREG_NUM).W))))


    when(io.flush) {
        insts_pack_reg := VecInit(Seq.fill(2)(0.U.asTypeOf(new inst_pack_ID_t)))
        alloc_preg_reg := VecInit(Seq.fill(2)(0.U(5.W)))
    }
    .elsewhen(!io.stall){
        insts_pack_reg := io.insts_pack_ID
        alloc_preg_reg := io.alloc_preg_ID
    }
    io.insts_pack_RN := insts_pack_reg
    io.alloc_preg_RN := alloc_preg_reg
}
