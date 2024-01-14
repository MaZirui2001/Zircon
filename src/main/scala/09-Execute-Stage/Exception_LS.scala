import chisel3._
import chisel3.util._
import EXCEPTION._
class Exception_LS_IO extends Bundle{
    val exception_before = Input(UInt(8.W))
    val addr_EX          = Input(UInt(32.W))
    val mem_type_EX      = Input(UInt(5.W))
    val exception_ls     = Output(UInt(8.W))
}

class Exception_LS extends Module{
    val io = IO(new Exception_LS_IO)

    val exception_before = io.exception_before
    val addr_EX          = io.addr_EX
    val mem_type         = io.mem_type_EX

    val exception_ls = Wire(UInt(8.W))

    when(io.exception_before(7)){
        exception_ls := io.exception_before
    }.otherwise{
        val mem_mask = (mem_type(1, 0) ## 0.U(1.W)) - 1.U
        exception_ls := Mux((addr_EX & mem_mask) =/= 0.U, 0x89.U, 0.U)
    }

    io.exception_ls := exception_ls
}
