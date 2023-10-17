import chisel3._
import chisel3.util._
import Control_Signal._

// LUt: 136

class Branch_IO extends Bundle {
    val br_type         = Input(UInt(4.W))
    val src1            = Input(UInt(32.W))
    val src2            = Input(UInt(32.W))
    val pc_ex           = Input(UInt(32.W))
    val imm_ex          = Input(UInt(32.W))
    val predict_fail    = Output(Bool())
    val branch_target   = Output(UInt(32.W))
    val predict_jump    = Input(Bool())
    val real_jump       = Output(Bool())
    val real_npc        = Output(UInt(32.W))
    val pred_update_en  = Output(Bool())
}
class Branch extends RawModule {
    val io = IO(new Branch_IO)
    io.predict_fail := false.B
    io.branch_target := io.pc_ex + io.imm_ex
    io.real_jump := false.B
    io.real_npc := Mux(io.real_jump, io.branch_target, io.pc_ex + 4.U)
    io.pred_update_en := false.B
    switch(io.br_type) {
        is(BR_BEQ){
            io.predict_fail := (io.src1 === io.src2) ^ io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := (io.src1 === io.src2)
            io.pred_update_en := true.B
        }
        is(BR_BNE){
            io.predict_fail := (io.src1 =/= io.src2) ^ io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := (io.src1 =/= io.src2)
            io.pred_update_en := true.B
        }
        is(BR_BLT){
            io.predict_fail := (io.src1.asSInt < io.src2.asSInt) ^ io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := (io.src1.asSInt < io.src2.asSInt)
            io.pred_update_en := true.B
        }
        is(BR_BGE){
            io.predict_fail := (io.src1.asSInt >= io.src2.asSInt) ^ io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := (io.src1.asSInt >= io.src2.asSInt)
            io.pred_update_en := true.B
        }
        is(BR_BLTU){
            io.predict_fail := (io.src1 < io.src2) ^ io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := (io.src1 < io.src2)
            io.pred_update_en := true.B
        }
        is(BR_BGEU){
            io.predict_fail := (io.src1 >= io.src2) ^ io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := (io.src1 >= io.src2)
            io.pred_update_en := true.B
        }
        is(BR_JIRL){
            io.predict_fail := true.B
            io.branch_target := io.src1 + io.imm_ex
            io.real_jump := true.B
            io.pred_update_en := false.B
        }
        is(BR_B){
            io.predict_fail := ~io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := true.B
            io.pred_update_en := true.B
        }
        is(BR_BL){
            io.predict_fail := ~io.predict_jump
            io.branch_target := io.pc_ex + io.imm_ex
            io.real_jump := true.B
            io.pred_update_en := true.B
        }
    }
}

// object Branch extends App {
//     emitVerilog(new Branch, Array("-td", "build/"))
// }
