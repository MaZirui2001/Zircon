import chisel3._
import chisel3.util._
// LUT: 3255 FF: 378
object RAT{
    class rat_t extends Bundle{
        val valid = Bool()
        val lr    = UInt(5.W)
    }
}

class CRat_IO extends Bundle{
    val rj           = Input(Vec(4, UInt(5.W)))
    val rk           = Input(Vec(4, UInt(5.W)))

    val rd           = Input(Vec(4, UInt(5.W)))
    val rd_valid     = Input(Vec(4, Bool()))
    val alloc_preg   = Input(Vec(4, UInt(6.W)))

    val prj          = Output(Vec(4, UInt(6.W)))
    val prk          = Output(Vec(4, UInt(6.W)))
    val pprd         = Output(Vec(4, UInt(6.W)))

    val arch_rat     = Input(Vec(64, UInt(1.W)))
    val predict_fail = Input(Bool())

    val stall = Input(Bool())
}
class CRat extends Module{
    val io = IO(new CRat_IO)
    import RAT._
    val crat = RegInit(VecInit(Seq.fill(64)(0.U.asTypeOf(new rat_t))))

    // write
    when(io.predict_fail){
        for(i <- 0 until 64){
            crat(i).valid := io.arch_rat(i)
        }
    }.otherwise{
        for(i <- 0 until 4){
            when(!io.stall){
                crat(io.alloc_preg(i)).lr := io.rd(i)
                when(io.rd_valid(i).asBool){
                    crat(io.alloc_preg(i)).valid := true.B
                    crat(io.pprd(i)).valid := false.B
                }
            }

        }
    }
    // read for rj, rk, rd
    val rj_hit_oh = Wire(Vec(4, Vec(64, Bool())))
    val rk_hit_oh = Wire(Vec(4, Vec(64, Bool())))
    val rd_hit_oh = Wire(Vec(4, Vec(64, Bool())))
    for(i <- 0 until 4){
        for(j <- 0 until 64){
            rj_hit_oh(i)(j) := crat(j).valid && (crat(j).lr === io.rj(i))
            rk_hit_oh(i)(j) := crat(j).valid && (crat(j).lr === io.rk(i))
            rd_hit_oh(i)(j) := crat(j).valid && (crat(j).lr === io.rd(i))
            io.prj(i) := OHToUInt(rj_hit_oh(i))
            io.prk(i) := OHToUInt(rk_hit_oh(i))
            io.pprd(i) := OHToUInt(rd_hit_oh(i))
        }
    }
}

