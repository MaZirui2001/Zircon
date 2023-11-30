import chisel3._
import chisel3.util._
// LUT: 3255 FF: 378
object RAT{
    class rat_t extends Bundle{
        val valid = Bool()
        val lr    = UInt(5.W)
    }
}

class CRat_IO(n: Int) extends Bundle{
    val rj           = Input(Vec(4, UInt(5.W)))
    val rk           = Input(Vec(4, UInt(5.W)))

    val rd           = Input(Vec(4, UInt(5.W)))
    val rd_valid     = Input(Vec(4, Bool()))
    val alloc_preg   = Input(Vec(4, UInt(log2Ceil(n).W)))

    val prj          = Output(Vec(4, UInt(log2Ceil(n).W)))
    val prk          = Output(Vec(4, UInt(log2Ceil(n).W)))
    val pprd         = Output(Vec(4, UInt(log2Ceil(n).W)))

    val arch_rat     = Input(Vec(n, UInt(1.W)))
    val predict_fail = Input(Bool())

}
class CRat(n: Int) extends Module{
    val io = IO(new CRat_IO(n))
    import RAT._
    val crat = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new rat_t))))


    // write
    when(io.predict_fail){
        for(i <- 0 until n){
            crat(i).valid := io.arch_rat(i)
        }
    }.otherwise{
        for(i <- 0 until 4){
            crat(io.alloc_preg(i)).lr := io.rd(i)
            when(io.rd_valid(i).asBool){
                crat(io.alloc_preg(i)).valid    := true.B
                crat(io.pprd(i)).valid          := false.B
            }

        }
    }
    // read for rj, rk, rd
    for(i <- 0 until 4){
        val rj_hit_oh = Wire(Vec(n, Bool()))
        val rk_hit_oh = Wire(Vec(n, Bool()))
        val rd_hit_oh = Wire(Vec(n, Bool()))
        for(j <- 0 until n){
            rj_hit_oh(j) := crat(j).valid && (crat(j).lr === io.rj(i))
            rk_hit_oh(j) := crat(j).valid && (crat(j).lr === io.rk(i))
            rd_hit_oh(j) := crat(j).valid && (crat(j).lr === io.rd(i))
        }
        io.prj(i)   := OHToUInt(rj_hit_oh)
        io.prk(i)   := OHToUInt(rk_hit_oh)
        io.pprd(i)  := OHToUInt(rd_hit_oh)
    }
}

