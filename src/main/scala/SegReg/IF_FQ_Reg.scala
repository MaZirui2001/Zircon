import chisel3._
import chisel3.util._
// LUT: 14 FF: 260
class IF_FQ_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val pcs_IF          = Input(Vec(4, UInt(32.W)))
        val insts_valid_IF  = Input(Vec(4, Bool()))
        val insts_IF        = Input(Vec(4, UInt(32.W)))

        val pcs_FQ          = Output(Vec(4, UInt(32.W)))
        val insts_valid_FQ  = Output(Vec(4, Bool()))
        val insts_FQ        = Output(Vec(4, UInt(32.W)))
    })

    val pcs_reg = RegInit(VecInit(Seq.fill(4)(0x1c000000.U(32.W))))
    val insts_valid_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val insts_reg = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))

    when(io.flush) {
        pcs_reg := VecInit(Seq.fill(4)(0x1c000000.U(32.W)))
        insts_valid_reg := VecInit(Seq.fill(4)(false.B))
        insts_reg := VecInit(Seq.fill(4)(0.U(32.W)))
    }
    .elsewhen(!io.stall){
        pcs_reg := io.pcs_IF
        insts_valid_reg := io.insts_valid_IF
        insts_reg := io.insts_IF
    }

    io.pcs_FQ := pcs_reg
    io.insts_valid_FQ := insts_valid_reg
    io.insts_FQ := insts_reg
}

object IF_FQ_Reg extends App {
    emitVerilog(new IF_FQ_Reg, Array("-td", "build/"))
}