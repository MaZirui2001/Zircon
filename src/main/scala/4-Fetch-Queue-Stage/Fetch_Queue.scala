import chisel3._
import chisel3.util._
import Inst_Pack._
// LUT: 1886 FF: 1042

object Fetch{
    class fetch_t extends Bundle{
        val inst = UInt(32.W)
        val pc = UInt(32.W)
        val pred_jump = Bool()
    }
}

class Fetch_Queue_IO extends Bundle{
    val insts_pack          = Input(Vec(4, new inst_pack_PD_t))

    val next_ready          = Input(Bool())
    val insts_valid_decode  = Output(Vec(4, Bool()))
    val insts_pack_id       = Output(Vec(4, new inst_pack_PD_t))
    

    val inst_queue_ready    = Output(Bool())
    val flush               = Input(Bool())
}

class Fetch_Queue extends Module{
    val io = IO(new Fetch_Queue_IO)

    /* config */
    val num_entries = 32
    val fetch_width = 4
    val row_width = num_entries / fetch_width
    val queue = RegInit(VecInit(Seq.fill(fetch_width)(VecInit(Seq.fill(row_width)(0.U.asTypeOf(new inst_pack_PD_t))))))

    val head = RegInit((1.U(row_width.W)))
    val head_index = OHToUInt(head)
    val tail = RegInit((1.U(num_entries.W)))
    val tail_index = OHToUInt(tail)

    val full = head_index === tail_index(log2Ceil(num_entries)-1, log2Ceil(fetch_width)) + 1.U
    val empty = head_index === tail_index(log2Ceil(num_entries)-1, log2Ceil(fetch_width))


    // Enqueue
    io.inst_queue_ready := !full

    def inc(ptr: UInt) = {
        val n = ptr.getWidth
        Cat(ptr(n-2, 0), ptr(n-1))
    }
    // calculate the entry index for each instruction
    val entry_idxs = Wire(Vec(fetch_width, UInt(num_entries.W)))
    var entry_index = tail
    for(i <- 0 until fetch_width){
        entry_idxs(i) := entry_index
        entry_index = Mux(io.insts_pack(i).inst_valid, inc(entry_index), entry_index)
    }
    // write to queue
    for(i <- 0 until fetch_width){
        for(j <- 0 until num_entries){
            when(!full && io.insts_pack(i).inst_valid && entry_idxs(i)(j)){
                queue(j % fetch_width)(j / fetch_width) := io.insts_pack(i)
            }
        }
    }
    // Dequeue
    for(i <- 0 until fetch_width){
        io.insts_pack_id(i) := queue(i)(head_index)
        io.insts_valid_decode(i) := !empty
    }
    // update ptrs
    when(!full){
        tail := entry_index
    }
    when(io.next_ready && !empty){
        head := inc(head)
    }
    when(io.flush){
        head := 1.U
        tail := 1.U
    }


}
// object Fetch_Queue extends App{
//     emitVerilog(new Fetch_Queue, Array("-td", "build/"))
// }
