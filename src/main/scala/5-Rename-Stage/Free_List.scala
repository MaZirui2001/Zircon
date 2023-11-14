import chisel3._
import chisel3.util._
// LUT: 435 FF: 416
class Free_List extends Module{
    val io = IO(new Bundle{
        val rd_valid            = Input(Vec(4, Bool()))
        val rename_en           = Input(Vec(4, Bool()))
        val alloc_preg          = Output(Vec(4, UInt(7.W)))
        val empty               = Output(Bool())

        val commit_en           = Input(Vec(4, Bool()))
        val commit_pprd_valid   = Input(Vec(4, Bool()))
        val commit_pprd         = Input(Vec(4, UInt(7.W)))

        val predict_fail        = Input(Bool())
        val head_arch           = Input(Vec(4, UInt(5.W)))
    })

    val free_list = RegInit(VecInit(Seq.tabulate(4)(i => VecInit(Seq.tabulate(21)(j => (j * 4 + i + 1).asUInt)))))

    val head = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val tail = RegInit(VecInit(Seq.fill(4)(20.U(5.W))))
    val tail_sel = RegInit(0.U(2.W))

    io.empty := VecInit(head.zip(tail).map{case(h, t) => (h === t)}).reduce(_||_)

    for(i <- 0 until 4){
        when(io.predict_fail){
            head(i) := io.head_arch(i)
        }.elsewhen(!io.empty && io.rename_en(i)){
            head(i) := Mux(head(i) + io.rd_valid(i) === 21.U, 0.U, head(i) + io.rd_valid(i))
        }
        when(io.commit_en(i)){
            tail(tail_sel+i.U) := Mux(tail(tail_sel+i.U) + io.commit_pprd_valid(i) === 21.U, 0.U, tail(tail_sel+i.U) + io.commit_pprd_valid(i))
            when(io.commit_pprd_valid(i)){
                free_list(tail_sel+i.U)(tail(tail_sel+i.U)) := io.commit_pprd(i)
            }
            tail_sel := Mux(io.predict_fail, 0.U, tail_sel + PopCount(io.commit_en))
        }
    }
    for(i <- 0 until 4){
        io.alloc_preg(i) := free_list(i)(head(i))
    }


}