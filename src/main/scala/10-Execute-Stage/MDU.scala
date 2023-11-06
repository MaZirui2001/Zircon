import chisel3._
import chisel3.util._
import Control_Signal._

class MDU_IO extends Bundle {
    val src1 = Input(UInt(32.W))
    val src2 = Input(UInt(32.W))
    val md_op = Input(UInt(5.W))
    val md_out = Output(UInt(32.W))
}

class MDU extends Module {
    val io = IO(new MDU_IO)

    val md_out = Wire(UInt(32.W))
    val src1 = io.src1
    val src2 = io.src2

    md_out := 0.U
    switch(io.md_op){
        is(ALU_MUL) {
            md_out := src1 * src2
        }
        is(ALU_MULH) {
            md_out := (src1.asSInt * src2.asSInt)(63, 32).asUInt
        }
        is(ALU_MULHU) {
            md_out := (src1 * src2)(63, 32)
        }
        is(ALU_DIV) {
            md_out := (src1.asSInt / src2.asSInt).asUInt
        }
        is(ALU_DIVU) {
            md_out := src1 / src2
        }
        is(ALU_MOD) {
            md_out := (src1.asSInt % src2.asSInt).asUInt
        }
        is(ALU_MODU) {
            md_out := src1 % src2
        }

    }

    io.md_out := md_out
}


