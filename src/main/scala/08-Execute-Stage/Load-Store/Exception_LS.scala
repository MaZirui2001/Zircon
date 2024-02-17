import chisel3._
import chisel3.util._
import Exception._
class Exception_LS_IO extends Bundle{
    val addr_EX          = Input(UInt(32.W))
    val mem_type_EX      = Input(UInt(5.W))
    val exception_ls     = Output(UInt(8.W))
}

class Exception_LS extends Module{
    val io = IO(new Exception_LS_IO)
    val addr_EX          = io.addr_EX
    val mem_type         = io.mem_type_EX

    val exception_ls = Wire(UInt(8.W))

    val mem_mask = (1.U(4.W) << mem_type(1, 0)) - 1.U
    exception_ls := Mux((addr_EX & mem_mask) =/= 0.U, 1.U(1.W) ## ALE, 0.U)

    io.exception_ls := exception_ls
}
