import chisel3._
import chisel3.util._
import Inst_Pack._

// LUT: 38 FF: 520

class RN_DP_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val insts_pack_RN   = Input(Vec(4, new inst_pack_t))
        val insts_valid_RN  = Input(Vec(4, Bool()))
        val rob_index_RN    = Input(Vec(4, UInt(4.W)))
        val prj_raw_RN      = Input(Vec(4, Bool()))
        val prk_raw_RN      = Input(Vec(4, Bool()))

        val insts_pack_DP   = Output(Vec(4, new inst_pack_t))
        val insts_valid_DP  = Output(Vec(4, Bool()))
        val rob_index_DP    = Output(Vec(4, UInt(4.W)))
        val prj_raw_DP      = Output(Vec(4, Bool()))
        val prk_raw_DP      = Output(Vec(4, Bool()))

    })

    val insts_pack_reg = RegInit(VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_t))))
    val insts_valid_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val rob_index_reg = RegInit(VecInit(Seq.fill(4)(0.U(4.W))))
    val prj_raw_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val prk_raw_reg = RegInit(VecInit(Seq.fill(4)(false.B)))

    when(io.flush) {
        insts_pack_reg := VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_t)))
        insts_valid_reg := VecInit(Seq.fill(4)(false.B))
        rob_index_reg := VecInit(Seq.fill(4)(0.U(4.W)))
        prj_raw_reg := VecInit(Seq.fill(4)(false.B))
        prk_raw_reg := VecInit(Seq.fill(4)(false.B))
    }
    .elsewhen(!io.stall){
        insts_pack_reg := io.insts_pack_RN
        insts_valid_reg := io.insts_valid_RN
        rob_index_reg := io.rob_index_RN
        prj_raw_reg := io.prj_raw_RN
        prk_raw_reg := io.prk_raw_RN
    }

    io.insts_pack_DP := insts_pack_reg
    io.insts_valid_DP := insts_valid_reg
    io.rob_index_DP := rob_index_reg
    io.prj_raw_DP := prj_raw_reg
    io.prk_raw_DP := prk_raw_reg


}

object RN_DP_Reg extends App {
    emitVerilog(new RN_DP_Reg, Array("-td", "build/"))
}