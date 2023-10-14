import chisel3._
import chisel3.util._
import Control_Signal._

// LUt: 136

class Branch_IO extends Bundle {
    val br_type = Input(UInt(4.W))
    val src1 = Input(UInt(32.W))
    val src2 = Input(UInt(32.W))
    val pc_ex = Input(UInt(32.W))
    val imm_ex = Input(UInt(32.W))
    val predict_fail = Output(Bool())
    val branch_target = Output(UInt(32.W))
}
class Branch extends RawModule {
    val io = IO(new Branch_IO)
    io.predict_fail := false.B
    io.branch_target := io.pc_ex + io.imm_ex
    switch(io.br_type) {
        is(BR_BEQ){
            io.predict_fail := (io.src1 === io.src2)
            io.branch_target := io.pc_ex + io.imm_ex
        }
        is(BR_BNE){
            io.predict_fail := (io.src1 =/= io.src2)
            io.branch_target := io.pc_ex + io.imm_ex
        }
        is(BR_BLT){
            io.predict_fail := (io.src1.asSInt < io.src2.asSInt)
            io.branch_target := io.pc_ex + io.imm_ex
        }
        is(BR_BGE){
            io.predict_fail := (io.src1.asSInt >= io.src2.asSInt)
            io.branch_target := io.pc_ex + io.imm_ex
        }
        is(BR_BLTU){
            io.predict_fail := (io.src1 < io.src2)
            io.branch_target := io.pc_ex + io.imm_ex
        }
        is(BR_BGEU){
            io.predict_fail := (io.src1 >= io.src2)
            io.branch_target := io.pc_ex + io.imm_ex
        }
        is(BR_JIRL){
            io.predict_fail := true.B
            io.branch_target := io.src1 + io.imm_ex
        }
        is(BR_B){
            io.predict_fail := true.B
            io.branch_target := io.pc_ex + io.imm_ex
        }
        is(BR_BL){
            io.predict_fail := true.B
            io.branch_target := io.pc_ex + io.imm_ex
        }
    }
}

// object Branch extends App {
//     emitVerilog(new Branch, Array("-td", "build/"))
// }
