import chisel3._
import chisel3.util._
import CPU_Config._
// LUT: 435 FF: 416
class Free_List(n: Int) extends Module{
    val io = IO(new Bundle{
        val rd_valid            = Input(Vec(FRONT_WIDTH, Bool()))
        val rename_en           = Input(Vec(FRONT_WIDTH, Bool()))
        val alloc_preg          = Output(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
        val empty               = Output(Bool())

        val commit_en           = Input(Vec(FRONT_WIDTH, Bool()))
        val commit_pprd_valid   = Input(Vec(FRONT_WIDTH, Bool()))
        val commit_pprd         = Input(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))

        val predict_fail        = Input(Bool())
        val head_arch           = Input(UInt(log2Ceil(n).W))
    })

    val free_list   = RegInit(VecInit.tabulate(n)(i => (i + 1).asUInt(log2Ceil(n)-1, 0)))
    val tail        = RegInit((n - 1).U(log2Ceil(n).W))
    val head        = RegInit(0.U(log2Ceil(n).W))

    io.empty        := VecInit.tabulate(FRONT_WIDTH)(i => Mux(head + i.U >= n.U, head + i.U - n.U, head + i.U) === tail).reduce(_||_)

    // alloc new reg
    val head_idx    = Wire(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    var head_now    = head
    io.alloc_preg   := VecInit.fill(FRONT_WIDTH)(0.U(log2Ceil(n).W))
    for(i <- 0 until FRONT_WIDTH){
        head_idx(i)         := head_now
        head_now            = Mux(io.rd_valid(i) && io.rename_en(i) && !io.empty, Mux(head_now === (n-1).U, 0.U, head_now + 1.U), head_now)
        io.alloc_preg(i)    := free_list(head_idx(i))
    }
    head := head_now
    

    // commit old reg
    val tail_idx    = Wire(Vec(FRONT_WIDTH, UInt(log2Ceil(n).W)))
    var tail_now    = tail
    for(i <- 0 until FRONT_WIDTH){
        tail_idx(i)         := tail_now
        tail_now            = Mux(io.commit_en(i) && io.commit_pprd_valid(i), Mux(tail_now === (n-1).U, 0.U, tail_now + 1.U), tail_now)
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