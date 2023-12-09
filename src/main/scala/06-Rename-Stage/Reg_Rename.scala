import chisel3._
import chisel3.util._
import CPU_Config._
// LUT: 3759 FF: 794
class Reg_rename_IO(n: Int) extends Bundle{
    val rj                  = Input(Vec(FRONT_WIDTH, UInt(5.W)))
    val rk                  = Input(Vec(FRONT_WIDTH, UInt(5.W)))

    val rd                  = Input(Vec(FRONT_WIDTH, UInt(5.W)))
    val rd_valid            = Input(Vec(FRONT_WIDTH, Bool()))
    val rename_en           = Input(Vec(FRONT_WIDTH, Bool()))

    val alloc_preg          = Input(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))

    val prj                 = Output(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    val prk                 = Output(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    val prd                 = Output(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    val pprd                = Output(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    val prj_raw             = Output(Vec(FRONT_WIDTH, Bool()))
    val prk_raw             = Output(Vec(FRONT_WIDTH, Bool()))

    val predict_fail        = Input(Bool())
    val arch_rat            = Input(Vec(n, UInt(1.W)))

}

class Reg_Rename(n: Int) extends Module{
    val io              = IO(new Reg_rename_IO(n))
    val crat            = Module(new CRat(n))
    
    val prj_temp        = Wire(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    val prk_temp        = Wire(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    val pprd_temp       = Wire(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    val rd_valid_temp   = Wire(Vec(FRONT_WIDTH, Bool()))

    val alloc_preg      = io.alloc_preg

    prj_temp            := crat.io.prj
    prk_temp            := crat.io.prk
    pprd_temp           := crat.io.pprd

    io.prd              := alloc_preg

    // RAW
    io.prj              := prj_temp
    io.prj_raw          := VecInit(Seq.fill(FRONT_WIDTH)(false.B))
    when (io.rd_valid(0) & (io.rd(0) === io.rj(1))){
        io.prj(1)       := alloc_preg(0)
        io.prj_raw(1)   := true.B
    }
    if(FRONT_WIDTH == 4){
        when (io.rd_valid(1) & (io.rd(1) === io.rj(2))){
            io.prj(2)       := alloc_preg(1)
            io.prj_raw(2)   := true.B
        }
        .elsewhen(io.rd_valid(0) & (io.rd(0) === io.rj(2))){
            io.prj(2)       := alloc_preg(0)
            io.prj_raw(2)   := true.B
        }
        when (io.rd_valid(2) & (io.rd(2) === io.rj(3))){
            io.prj(3)       := alloc_preg(2)
            io.prj_raw(3)   := true.B
        }
        .elsewhen(io.rd_valid(1) & (io.rd(1) === io.rj(3))){
            io.prj(3)       := alloc_preg(1)
            io.prj_raw(3)   := true.B
        }
        .elsewhen(io.rd_valid(0) & (io.rd(0) === io.rj(3))){
            io.prj(3)       := alloc_preg(0)
            io.prj_raw(3)   := true.B
        }
    }


    io.prk              := prk_temp
    io.prk_raw          := VecInit(Seq.fill(FRONT_WIDTH)(false.B))
    when (io.rd_valid(0) & (io.rd(0) === io.rk(1))){
        io.prk(1)       := alloc_preg(0)
        io.prk_raw(1)   := true.B
    }
    if(FRONT_WIDTH == 4){
        when (io.rd_valid(1) & (io.rd(1) === io.rk(2))){
            io.prk(2)       := alloc_preg(1)
            io.prk_raw(2)   := true.B
        }
        .elsewhen(io.rd_valid(0) & (io.rd(0) === io.rk(2))){
            io.prk(2)       := alloc_preg(0)
            io.prk_raw(2)   := true.B
        }
        when (io.rd_valid(2) & (io.rd(2) === io.rk(3))){
            io.prk(3)       := alloc_preg(2)
            io.prk_raw(3)   := true.B
        }
        .elsewhen(io.rd_valid(1) & (io.rd(1) === io.rk(3))){
            io.prk(3)       := alloc_preg(1)
            io.prk_raw(3)   := true.B
        }
        .elsewhen(io.rd_valid(0) & (io.rd(0) === io.rk(3))){
            io.prk(3)       := alloc_preg(0)
            io.prk_raw(3)   := true.B
        }
    }


    // WAW
    if(FRONT_WIDTH == 4){
        rd_valid_temp(3)    := io.rd_valid(3)
        rd_valid_temp(2)    := io.rd_valid(2) & ~((io.rd(2) === io.rd(3)) & io.rd_valid(3))
        rd_valid_temp(1)    := io.rd_valid(1) & ~((io.rd(1) === io.rd(2)) & io.rd_valid(2)) & ~((io.rd(1) === io.rd(3)) & io.rd_valid(3))
        rd_valid_temp(0)    := io.rd_valid(0) & ~((io.rd(0) === io.rd(1)) & io.rd_valid(1)) & ~((io.rd(0) === io.rd(2)) & io.rd_valid(2)) & ~((io.rd(0) === io.rd(3)) & io.rd_valid(3))
    }else{
        rd_valid_temp(1)     := io.rd_valid(1)
        rd_valid_temp(0)     := io.rd_valid(0) & ~((io.rd(0) === io.rd(1)) & io.rd_valid(1))
    }


    io.pprd             := pprd_temp
    when(io.rd_valid(0) & (io.rd(0) === io.rd(1))){
        io.pprd(1)      := alloc_preg(0)
    }
    if(FRONT_WIDTH == 4){
        when(io.rd_valid(1) & (io.rd(1) === io.rd(2))){
            io.pprd(2)      := alloc_preg(1)
        }
        .elsewhen(io.rd_valid(0) & (io.rd(0) === io.rd(2))){
            io.pprd(2)      := alloc_preg(0)
        }
        when(io.rd_valid(2) & (io.rd(2) === io.rd(3))){
            io.pprd(3)      := alloc_preg(2)
        }
        .elsewhen(io.rd_valid(1) & (io.rd(1) === io.rd(3))){
            io.pprd(3)      := alloc_preg(1)
        }
        .elsewhen(io.rd_valid(0) & (io.rd(0) === io.rd(3))){
            io.pprd(3)      := alloc_preg(0)
        }
    }

    // crat
    crat.io.rj              := io.rj
    crat.io.rk              := io.rk
    crat.io.rd              := io.rd
    crat.io.rd_valid        := (rd_valid_temp.asUInt & io.rename_en.asUInt).asBools
    crat.io.alloc_preg      := io.alloc_preg
    crat.io.arch_rat        := io.arch_rat
    crat.io.predict_fail    := io.predict_fail
}

