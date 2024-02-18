import chisel3._
import chisel3.util._
import CPU_Config._

class Reg_rename_IO(n: Int) extends Bundle{
    val rj                  = Input(Vec(2, UInt(5.W)))
    val rk                  = Input(Vec(2, UInt(5.W)))

    val rd                  = Input(Vec(2, UInt(5.W)))
    val rd_valid            = Input(Vec(2, Bool()))
    val rename_en           = Input(Vec(2, Bool()))

    val alloc_preg          = Input(Vec(2, UInt(log2Ceil(n).W)))

    val prj                 = Output(Vec(2, UInt(log2Ceil(n).W)))
    val prk                 = Output(Vec(2, UInt(log2Ceil(n).W)))
    val prd                 = Output(Vec(2, UInt(log2Ceil(n).W)))
    val pprd                = Output(Vec(2, UInt(log2Ceil(n).W)))

    val predict_fail        = Input(Bool())
    val arch_rat            = Input(Vec(n, UInt(1.W)))

    val prj_ready           = Output(Vec(2, Bool()))
    val prk_ready           = Output(Vec(2, Bool()))

    val prd_wake            = Input(Vec(4, UInt(log2Ceil(n).W)))
    val wake_valid          = Input(Vec(4, Bool()))

}

class Reg_Rename(n: Int) extends Module{
    val io              = IO(new Reg_rename_IO(n))
    val crat            = Module(new CRat(n))
    
    val prj_temp        = Wire(Vec(2, UInt(log2Ceil(n).W)))
    val prk_temp        = Wire(Vec(2, UInt(log2Ceil(n).W)))
    val pprd_temp       = Wire(Vec(2, UInt(log2Ceil(n).W)))
    val rd_valid_temp   = Wire(Vec(2, Bool()))

    val alloc_preg      = io.alloc_preg

    prj_temp            := crat.io.prj
    prk_temp            := crat.io.prk
    pprd_temp           := crat.io.pprd

    io.prd              := alloc_preg

    // RAW
    io.prj               := prj_temp
    val prj_nraw         = VecInit.fill(2)(true.B)
    io.prj_ready         := VecInit.tabulate(2)(i => crat.io.prj_ready(i) && prj_nraw(i))
    when (io.rd_valid(0) & !(io.rd(0) ^ io.rj(1))){
        io.prj(1)        := alloc_preg(0)
        prj_nraw(1)      := false.B
    }

    io.prk               := prk_temp
    val prk_nraw         = VecInit.fill(2)(true.B)
    io.prk_ready         := VecInit.tabulate(2)(i => crat.io.prk_ready(i) && prk_nraw(i))
    when (io.rd_valid(0) & !(io.rd(0) ^ io.rk(1))){
        io.prk(1)        := alloc_preg(0)
        prk_nraw(1)      := false.B
    }


    // WAW
    rd_valid_temp(1)     := io.rd_valid(1)
    rd_valid_temp(0)     := io.rd_valid(0) & ~(!(io.rd(0) ^ io.rd(1)) & io.rd_valid(1))

    io.pprd              := pprd_temp
    when(io.rd_valid(0) & !(io.rd(0) ^ io.rd(1))){
        io.pprd(1)       := alloc_preg(0)
    }

    // crat
    crat.io.rj              := io.rj
    crat.io.rk              := io.rk
    crat.io.rd              := io.rd
    crat.io.rd_valid        := (rd_valid_temp.asUInt & io.rename_en.asUInt).asBools
    crat.io.alloc_preg      := io.alloc_preg
    crat.io.arch_rat        := io.arch_rat
    crat.io.predict_fail    := io.predict_fail
    crat.io.prd_wake        := io.prd_wake
    crat.io.wake_valid      := io.wake_valid
}

