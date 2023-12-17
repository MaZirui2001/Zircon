import chisel3._
import chisel3.util._
import Control_Signal._

class MDU_IO extends Bundle {
    val src1    = Input(UInt(32.W))
    val src2    = Input(UInt(32.W))
    val md_op   = Input(UInt(5.W))
    val mul_out = Output(UInt(32.W))
    val div_out = Output(UInt(32.W))
    val busy    = Output(Bool())
}

class MDU extends Module {
    val io = IO(new MDU_IO)
    val mul = Module(new Multiply)
    val div = Module(new Divide)

    mul.io.src1 := io.src1
    mul.io.src2 := io.src2
    mul.io.op   := io.md_op

    div.io.src1 := io.src1
    div.io.src2 := io.src2
    div.io.op   := io.md_op

    io.mul_out := mul.io.res
    io.div_out := div.io.res
    io.busy    := div.io.busy
}


