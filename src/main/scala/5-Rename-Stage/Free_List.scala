import chisel3._
import chisel3.util._
// LUT: 435 FF: 416
class Free_List(n: Int) extends Module{
    val io = IO(new Bundle{
        val rd_valid            = Input(Vec(4, Bool()))
        val rename_en           = Input(Vec(4, Bool()))
        val alloc_preg          = Output(Vec(4, UInt(log2Ceil(n).W)))
        val empty               = Output(Bool())

        val commit_en           = Input(Vec(4, Bool()))
        val commit_pprd_valid   = Input(Vec(4, Bool()))
        val commit_pprd         = Input(Vec(4, UInt(log2Ceil(n).W)))

        val predict_fail        = Input(Bool())
        val head_arch           = Input(UInt(log2Ceil(n).W))
    })

    // val free_list   = RegInit(VecInit.tabulate(4)(i => VecInit.tabulate(22)(j => (j * 4 + i + 1).asUInt)))
    // // ptrs
    // val head        = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    // val tail        = RegInit(VecInit(Seq.fill(4)(21.U(5.W))))
    // val tail_sel    = RegInit(0.U(2.W))

    // io.empty := VecInit(head.zip(tail).map{case(h, t) => (h === t)}).reduce(_||_)

    // for(i <- 0 until 4){
    //     // head update
    //     when(io.predict_fail){
    //         head(i) := io.head_arch(i)
    //     }.elsewhen(!io.empty && io.rename_en(i)){
    //         head(i) := Mux(head(i) + io.rd_valid(i) === 22.U, 0.U, head(i) + io.rd_valid(i))
    //     }
    //     // tail update
    //     when(io.commit_en(i)){
    //         tail(tail_sel+i.U) := Mux(tail(tail_sel+i.U) + io.commit_pprd_valid(i) === 22.U, 0.U, tail(tail_sel+i.U) + io.commit_pprd_valid(i))
    //         when(io.commit_pprd_valid(i)){
    //             free_list(tail_sel+i.U)(tail(tail_sel+i.U)) := io.commit_pprd(i)
    //         }
    //         tail_sel := Mux(io.predict_fail, 0.U, tail_sel + PopCount(io.commit_en))
    //     }
    // }

    // for(i <- 0 until 4){
    //     io.alloc_preg(i) := free_list(i)(head(i))
    // }

    val free_list = RegInit(VecInit.tabulate(n)(i => (i + 1).asUInt(log2Ceil(n).W)))
    val tail = RegInit((n - 1).U(log2Ceil(n).W))
    val head = RegInit(0.U(log2Ceil(n).W))

    io.empty := VecInit.tabulate(4)(i => Mux(head + i.U >= n.U, head + i.U - n.U, head + i.U) === tail).reduce(_||_)

    // alloc new reg
    val head_idx = Wire(Vec(4, UInt(log2Ceil(n).W)))
    var head_now = head
    io.alloc_preg := VecInit.fill(4)(0.U(log2Ceil(n).W))
    for(i <- 0 until 4){
        head_idx(i) := head_now
        head_now = Mux(io.rd_valid(i) && io.rename_en(i) && !io.empty, Mux(head_now === (n-1).U, 0.U, head_now + 1.U), head_now)
        io.alloc_preg(i) := free_list(head_idx(i))
    }
    head := head_now
    

    // commit old reg
    val tail_idx = Wire(Vec(4, UInt(log2Ceil(n).W)))
    var tail_now = tail
    for(i <- 0 until 4){
        tail_idx(i) := tail_now
        tail_now = Mux(io.commit_en(i) && io.commit_pprd_valid(i), Mux(tail_now === (n-1).U, 0.U, tail_now + 1.U), tail_now)
        when(io.commit_en(i) && io.commit_pprd_valid(i)){
            free_list(tail_idx(i)) := io.commit_pprd(i)
        }
    }
    
    tail := tail_now


    // flush
    when(io.predict_fail){
        head := io.head_arch
    }

}