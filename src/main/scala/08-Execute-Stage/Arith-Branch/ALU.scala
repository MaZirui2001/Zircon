import chisel3._ 
import chisel3.util._
import Control_Signal._

class ALU_IO extends Bundle{
    val src1        = Input(UInt(32.W))
    val src2        = Input(UInt(32.W))
    val alu_op      = Input(UInt(4.W))
    val alu_out     = Output(UInt(32.W))
}

class ALU extends Module {
    val io = IO(new ALU_IO)

    val src1 = io.src1
    val src2 = io.src2

    val alu_op = io.alu_op

    val alu_out = Wire(UInt(32.W))

    alu_out := DontCare

    switch(alu_op){
        is(ALU_ADD) {
            alu_out := src1 + src2
        }
        is(ALU_SUB) {
            alu_out := src1 - src2
        }
        is(ALU_SLT) {
            alu_out := Mux(src1.asSInt < src2.asSInt, 1.U, 0.U)
        }
        is(ALU_SLTU) {
            alu_out := Mux(src1 < src2, 1.U, 0.U)
        }
        is(ALU_NOR) {
            alu_out := ~(src1 | src2)
        }
        is(ALU_AND){
            alu_out := src1 & src2
        }
        is(ALU_OR){
            alu_out := src1 | src2
        }
        is(ALU_XOR){
            alu_out := src1 ^ src2
        }
        is(ALU_SLL){
            alu_out := src1 << src2(4, 0)
        }
        is(ALU_SRL){
            alu_out := src1 >> src2(4, 0)
        }
        is(ALU_SRA){
            alu_out := (src1.asSInt >> src2(4, 0)).asUInt
        }
    }
    io.alu_out := alu_out
}
