import chisel3._
import chisel3.util._
import Inst_Pack._
// LUT: 14 FF: 260
class PD_FQ_Reg extends Module {
    val io = IO(new Bundle {
        val flush                   = Input(Bool())
        val stall                   = Input(Bool())
        val insts_pack_PD           = Input(Vec(4, new inst_pack_PD_t))
        val pred_fix_PD             = Input(Bool())
        val pred_fix_target_PD      = Input(UInt(32.W))
        val pred_fix_is_bl_PD       = Input(Bool())
        val pred_fix_pc_plus_4_PD   = Input(UInt(32.W))

        val insts_pack_FQ           = Output(Vec(4, new inst_pack_PD_t))
        val pred_fix_FQ             = Output(Bool())
        val pred_fix_target_FQ      = Output(UInt(32.W))
        val pred_fix_is_bl_FQ       = Output(Bool())
        val pred_fix_pc_plus_4_FQ   = Output(UInt(32.W))
        
    })


    val insts_pack_reg = RegInit(VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_PD_t))))
    val pred_fix_reg   = RegInit(false.B)
    val pred_fix_target_reg = RegInit(0.U(32.W))
    val pred_fix_is_bl_reg  = RegInit(false.B)
    val pred_fix_pc_plus_4_reg = RegInit(0.U(32.W))

    when(io.flush) {
        insts_pack_reg := VecInit(Seq.fill(4)(0.U.asTypeOf(new inst_pack_PD_t)))
        pred_fix_reg   := false.B
        pred_fix_target_reg := 0.U(32.W)
        pred_fix_is_bl_reg  := false.B
        pred_fix_pc_plus_4_reg := 0.U(32.W)
    }
    .elsewhen(!io.stall){
        insts_pack_reg := io.insts_pack_PD
        pred_fix_reg   := io.pred_fix_PD
        pred_fix_target_reg := io.pred_fix_target_PD
        pred_fix_is_bl_reg  := io.pred_fix_is_bl_PD
        pred_fix_pc_plus_4_reg := io.pred_fix_pc_plus_4_PD
    }
    io.insts_pack_FQ := insts_pack_reg
    io.pred_fix_FQ   := pred_fix_reg
    io.pred_fix_target_FQ := pred_fix_target_reg
    io.pred_fix_is_bl_FQ  := pred_fix_is_bl_reg
    io.pred_fix_pc_plus_4_FQ := pred_fix_pc_plus_4_reg
}

// object IF_FQ_Reg extends App {
//     emitVerilog(new IF_FQ_Reg, Array("-td", "build/"))
// }