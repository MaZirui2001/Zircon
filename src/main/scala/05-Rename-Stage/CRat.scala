import chisel3._
import chisel3.util._
import CPU_Config._
// LUT: 3255 FF: 378


class CRat_IO(n: Int) extends Bundle{
    val rj           = Input(Vec(2, UInt(5.W)))
    val rk           = Input(Vec(2, UInt(5.W)))

    val rd           = Input(Vec(2, UInt(5.W)))
    val rd_valid     = Input(Vec(2, Bool()))
    val alloc_preg   = Input(Vec(2, UInt(log2Ceil(n).W)))

    val prj          = Output(Vec(2, UInt(log2Ceil(n).W)))
    val prk          = Output(Vec(2, UInt(log2Ceil(n).W)))
    val pprd         = Output(Vec(2, UInt(log2Ceil(n).W)))

    val arch_rat     = Input(Vec(n, UInt(1.W)))
    val predict_fail = Input(Bool())

    // read by inst
    val prj_ready     = Output(Vec(2, Bool()))
    val prk_ready     = Output(Vec(2, Bool()))

    // write by wakeup
    val prd_wake     = Input(Vec(4, UInt(log2Ceil(n).W)))
    val wake_valid   = Input(Vec(4, Bool()))
    

}
class CRat(n: Int) extends Module{
    val io = IO(new CRat_IO(n))
    import Rat._
    val crat = RegInit(VecInit.fill(n)(0.U.asTypeOf(new rat_t)))


    // write by disp
    when(io.predict_fail){
        for(i <- 0 until n){
            crat(i).valid := io.arch_rat(i)
            crat(i).free  := true.B
        }
    }.otherwise{
        for(i <- 0 until 2){
            crat(io.alloc_preg(i)).lr := io.rd(i)
            when(io.rd_valid(i).asBool){
                crat(io.alloc_preg(i)).valid    := true.B
                crat(io.alloc_preg(i)).free     := false.B
                crat(io.pprd(i)).valid          := false.B
            }

        }
        for(i <- 0 until 4){
            when(io.wake_valid(i)){
                crat(io.prd_wake(i)).free := true.B
            }
        }
    }

    // read for rj, rk, rd
    for(i <- 0 until 2){
        val rj_hit_oh = Wire(Vec(n, Bool()))
        val rk_hit_oh = Wire(Vec(n, Bool()))
        val rd_hit_oh = Wire(Vec(n, Bool()))
        for(j <- 0 until n){
            rj_hit_oh(j) := crat(j).valid && !(crat(j).lr ^ io.rj(i))
            rk_hit_oh(j) := crat(j).valid && !(crat(j).lr ^ io.rk(i))
            rd_hit_oh(j) := crat(j).valid && !(crat(j).lr ^ io.rd(i))
        }
        
        io.prj(i)       := OHToUInt(rj_hit_oh)
        io.prk(i)       := OHToUInt(rk_hit_oh)
        io.pprd(i)      := OHToUInt(rd_hit_oh)
        io.prj_ready(i)  := Mux1H(rj_hit_oh, crat.map(_.free))
        io.prk_ready(i)  := Mux1H(rk_hit_oh, crat.map(_.free))
    }
    
}

