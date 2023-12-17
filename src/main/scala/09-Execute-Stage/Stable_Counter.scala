import chisel3._
import chisel3.util._

class Stable_Counter extends Module{
    val io = IO(new Bundle{
        val value = Output(UInt(64.W))
    })
    val counter = RegInit(0.U(64.W))
    counter := counter + 1.U
    io.value := counter
}