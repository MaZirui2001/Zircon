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
    val predict_jump    = Input(Bool())
    val pred_npc        = Input(UInt(32.W))
    val real_jump       = Output(Bool())
    val predict_fail    = Output(Bool())
    val branch_target   = Output(UInt(32.W))
}

class Branch extends Module {
    val io = IO(new Branch_IO)
    io.predict_fail     := false.B
    io.branch_target    := io.pc_ex + io.imm_ex
    io.real_jump        := false.B
    switch(io.br_type) {
        is(BR_BEQ){
            io.predict_fail     := ((io.src1 === io.src2) ^ io.predict_jump) || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex
            io.real_jump        := (io.src1 === io.src2)
        }
        is(BR_BNE){
            io.predict_fail     := (io.src1 =/= io.src2) ^ io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex
            io.real_jump        := (io.src1 =/= io.src2)
        }
        is(BR_BLT){
            io.predict_fail     := (io.src1.asSInt < io.src2.asSInt) ^ io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex
            io.real_jump        := (io.src1.asSInt < io.src2.asSInt)
        }
        is(BR_BGE){
            io.predict_fail     := (io.src1.asSInt >= io.src2.asSInt) ^ io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex 
            io.real_jump        := (io.src1.asSInt >= io.src2.asSInt)
        }
        is(BR_BLTU){
            io.predict_fail     := (io.src1 < io.src2) ^ io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex
            io.real_jump        := (io.src1 < io.src2)
        }
        is(BR_BGEU){
            io.predict_fail     := (io.src1 >= io.src2) ^ io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex
            io.real_jump        := (io.src1 >= io.src2)
        }
        is(BR_JIRL){
            io.predict_fail     := ~io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.src1 + io.imm_ex
            io.real_jump        := true.B
        }
        is(BR_B){
            io.predict_fail     := ~io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex
            io.real_jump        := true.B
        }
        is(BR_BL){
            io.predict_fail     := ~io.predict_jump || (io.predict_jump && (io.pred_npc =/= (io.branch_target)))
            io.branch_target    := io.pc_ex + io.imm_ex
            io.real_jump        := true.B
        }
    }
}

// object Branch extends App {
//     emitVerilog(new Branch, Array("-td", "build/"))
// }
