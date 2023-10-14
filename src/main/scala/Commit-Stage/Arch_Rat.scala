import chisel3._
import chisel3.util._
import RAT._
object ARCH_RAT_Func{
    def Valid_Write_First_Read(cmt_en: Vec[Bool], rd_valid_cmt: Vec[Bool], prd_cmt: Vec[UInt], pprd_cmt: Vec[UInt], arat: Vec[rat_t], rindex: Int) : Bool = {
        val prd_wf = Cat(
                    rindex.U === prd_cmt(3) && cmt_en(3) && rd_valid_cmt(3),
                    rindex.U === prd_cmt(2) && cmt_en(2) && rd_valid_cmt(2),
                    rindex.U === prd_cmt(1) && cmt_en(1) && rd_valid_cmt(1),
                    rindex.U === prd_cmt(0) && cmt_en(0) && rd_valid_cmt(0)
                    )

        val pprd_wf = Cat(
                    rindex.U === pprd_cmt(3) && cmt_en(3) && rd_valid_cmt(3),
                    rindex.U === pprd_cmt(2) && cmt_en(2) && rd_valid_cmt(2),
                    rindex.U === pprd_cmt(1) && cmt_en(1) && rd_valid_cmt(1),
                    rindex.U === pprd_cmt(0) && cmt_en(0) && rd_valid_cmt(0)
                    )
        Mux(prd_wf.orR, true.B, Mux(pprd_wf.orR, false.B, arat(rindex).valid))
    }
}
import ARCH_RAT_Func._
class Arch_Rat_IO extends Bundle {
    // for commit 
    val cmt_en          = Input(Vec(4, Bool()))
    val rd_cmt          = Input(Vec(4, UInt(5.W)))
    val prd_cmt         = Input(Vec(4, UInt(6.W)))
    val pprd_cmt        = Input(Vec(4, UInt(6.W)))
    val rd_valid_cmt    = Input(Vec(4, Bool()))

    val arch_rat_lr     = Output(UInt((64*5).W))

    // for reg rename
    val arch_rat        = Output(Vec(64, UInt(1.W)))
    val head_arch       = Output(Vec(4, UInt(4.W)))
}

class Arch_Rat extends Module {
    val io = IO(new Arch_Rat_IO)

    val arat = RegInit(VecInit(Seq.fill(64)(0.U.asTypeOf(new rat_t))))
    val arat_next = Wire(Vec(64, new rat_t))


    val head = RegInit(VecInit(1.U(4.W), 0.U(4.W), 0.U(4.W), 0.U(4.W)))
    val head_next = Wire(Vec(4, UInt(4.W)))
    val head_sel = RegInit(0.U(2.W))

    arat_next := arat
    for(i <- 0 until 4){
        when(io.rd_valid_cmt(i)){
            arat_next(io.pprd_cmt(i)).valid := false.B
            arat_next(io.prd_cmt(i)).lr := io.rd_cmt(i)
            arat_next(io.prd_cmt(i)).valid := true.B
        }
    }
    arat := arat_next

    val cmt_en = io.cmt_en.asUInt.orR
    head_next := head
    for(i <- 0 until 4){
        head_next(head_sel+i.U) := head(head_sel+i.U) + (io.cmt_en(i) && io.rd_valid_cmt(i))
    }
    head := head_next
    head_sel := head_sel + PopCount(io.cmt_en)

    for(i <- 0 until 64){
        io.arch_rat(i) := arat_next(i).valid
    }
    for(i <- 0 until 4){
        io.head_arch(i) := head_next(i)
    }
    io.arch_rat_lr := Cat(arat(63).lr, arat(62).lr, arat(61).lr, arat(60).lr, arat(59).lr, arat(58).lr, arat(57).lr, arat(56).lr, 
                        arat(55).lr, arat(54).lr, arat(53).lr, arat(52).lr, arat(51).lr, arat(50).lr, arat(49).lr, arat(48).lr, 
                        arat(47).lr, arat(46).lr, arat(45).lr, arat(44).lr, arat(43).lr, arat(42).lr, arat(41).lr, arat(40).lr, 
                        arat(39).lr, arat(38).lr, arat(37).lr, arat(36).lr, arat(35).lr, arat(34).lr, arat(33).lr, arat(32).lr, 
                        arat(31).lr, arat(30).lr, arat(29).lr, arat(28).lr, arat(27).lr, arat(26).lr, arat(25).lr, arat(24).lr,
                        arat(23).lr, arat(22).lr, arat(21).lr, arat(20).lr, arat(19).lr, arat(18).lr, arat(17).lr, arat(16).lr, 
                        arat(15).lr, arat(14).lr, arat(13).lr, arat(12).lr, arat(11).lr, arat(10).lr, arat(9).lr, arat(8).lr, 
                        arat(7).lr, arat(6).lr, arat(5).lr, arat(4).lr, arat(3).lr, arat(2).lr, arat(1).lr, arat(0).lr)

}