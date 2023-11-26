import chisel3._
import chisel3.util._

class Imm_Gen extends Module{
    val io = IO(new Bundle{
        val inst        = Input(UInt(32.W))
        val imm_type    = Input(UInt(4.W))
        val imm         = Output(UInt(32.W))
    })

    val inst = io.inst
    val imm_type = io.imm_type
    val imm = Wire(UInt(32.W))

    import Control_Signal._
    imm := 0.U(32.W)
    switch(imm_type) {
        is(IMM_00U)     { imm := 0.U(32.W) }
        is(IMM_05U)     { imm := Cat(0.U(27.W), inst(14, 10)) }
        is(IMM_12U)     { imm := Cat(0.U(20.W), inst(21, 10)) }
        is(IMM_12S)     { imm := Cat(Fill(20, inst(21)), inst(21, 10)) }
        is(IMM_16S)     { imm := Cat(Fill(14, inst(25)), inst(25, 10), 0.U(2.W)) }
        is(IMM_20S)     { imm := Cat(inst(24, 5), 0.U(12.W)) }
        is(IMM_26S)     { imm := Cat(Fill(4, inst(9)), inst(9, 0), inst(25, 10), 0.U(2.W)) }
    }
    io.imm := imm
}

class DecodeIO extends Bundle{
    val inst            = Input(UInt(32.W))
    val rj              = Output(UInt(5.W))
    val rj_valid        = Output(Bool())
    val rk              = Output(UInt(5.W))
    val rk_valid        = Output(Bool())
    val rd              = Output(UInt(5.W))
    val rd_valid        = Output(Bool())

    val imm             = Output(UInt(32.W))
    val alu_op          = Output(UInt(5.W))
    val alu_rs1_sel     = Output(UInt(2.W))
    val alu_rs2_sel     = Output(UInt(2.W))
    val br_type         = Output(UInt(4.W))
    val mem_type        = Output(UInt(5.W))

    val priv_vec        = Output(UInt(4.W))
    val csr_addr        = Output(UInt(14.W))

    val fu_id           = Output(UInt(3.W))
    val inst_exist      = Output(Bool())
}
class Decode extends Module{
    val io = IO(new DecodeIO)
    val ctrl = ListLookup(io.inst, Control_Signal.default, Control_Signal.map)
    val imm_gen = Module(new Imm_Gen)

    io.rj               := io.inst(9, 5)
    io.rj_valid         := ctrl(0)

    io.rk               := Mux(ctrl(9).asBool, io.inst(14, 10), io.inst(4, 0))
    io.rk_valid         := ctrl(1)

    io.rd               := Mux(ctrl(10).asBool, 1.U(5.W), io.inst(4, 0))
    io.rd_valid         := ctrl(2) & io.rd =/= 0.U(5.W)

    io.alu_op           := ctrl(3)
    io.alu_rs1_sel      := ctrl(4)
    io.alu_rs2_sel      := ctrl(5)

    io.br_type          := ctrl(6)
    io.mem_type         := ctrl(7)

    io.priv_vec         := ctrl(12)
    io.csr_addr         := Mux(ctrl(13) === 0.U, io.inst(23, 10), 0x40.U(14.W))

    io.fu_id            := ctrl(8)
    io.inst_exist       := ctrl(14)

    imm_gen.io.inst     := io.inst
    imm_gen.io.imm_type := ctrl(11)
    io.imm              := imm_gen.io.imm
}

