import chisel3._
import chisel3.util._
// LUT: 1886 FF: 1042

object Fetch{
    class fetch_t extends Bundle{
        val inst = UInt(32.W)
        val pc = UInt(32.W)
    }
}

class Fetch_Queue_IO extends Bundle{
    val insts               = Input(Vec(4, UInt(32.W)))
    val insts_valid         = Input(Vec(4, Bool()))
    val pcs_FQ              = Input(Vec(4, UInt(32.W)))
    // val for_commit          = Input(Vec(4, Bool()))

    val next_ready          = Input(Bool())
    val insts_decode        = Output(Vec(4, UInt(32.W)))
    val pcs_ID              = Output(Vec(4, UInt(32.W)))
    val insts_valid_decode  = Output(Vec(4, Bool()))
    // val insts_for_commit    = Output(Vec(4, Bool()))

    val inst_queue_ready    = Output(Bool())
    val flush               = Input(Bool())
}

class Fetch_Queue extends Module{
    val io = IO(new Fetch_Queue_IO)
    import Fetch._
    val queue = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(4)(0.U.asTypeOf(new fetch_t))))))

    val head = RegInit(VecInit(Seq.fill(4)(0.U(2.W))))
    val tail = RegInit(VecInit(Seq.fill(4)(0.U(2.W))))
    val tail_sel = RegInit(0.U(2.W))

    val full = Wire(Bool())
    full := (head(0) === tail(0)+1.U) | (head(1) === tail(1)+1.U) | (head(2) === tail(2)+1.U) | (head(3) === tail(3)+1.U)
    val empty = Wire(Bool())
    empty := (head(0) === tail(0)) & (head(1) === tail(1)) & (head(2) === tail(2)) & (head(3) === tail(3))

    io.inst_queue_ready := !full

    for(i <- 0 until 4){
        when(io.flush){
            tail(i) := 0.U
        }.elsewhen(io.insts_valid(i) && !full){
            queue(i.U+tail_sel)(tail(i)).inst := io.insts(i)
            queue(i.U+tail_sel)(tail(i)).pc := io.pcs_FQ(i)
            // queue(i)(tail(i)).is_commit := io.for_commit(i)
            tail(i) := tail(i) + 1.U
            tail_sel := tail_sel + PopCount(io.insts_valid)
        }
        when(io.flush){
            head(i) := 0.U
        }.elsewhen(io.next_ready && !empty){
            head(i) := head(i) + 1.U
        }
    }

    for(i <- 0 until 4){
        io.insts_decode(i) := queue(i)(head(i)).inst
        io.insts_valid_decode(i) := !empty
        io.pcs_ID(i) := queue(i)(head(i)).pc
        // io.insts_for_commit(i) := queue(i)(head(i)).is_commit
    }

}
// object Fetch_Queue extends App{
//     emitVerilog(new Fetch_Queue, Array("-td", "build/"))
// }
