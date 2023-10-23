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
    val insts_pack          = Input(Vec(4, new inst_pack_IF_t))

    val next_ready          = Input(Bool())
    val insts_valid_decode  = Output(Vec(4, Bool()))
    val insts_pack_id       = Output(Vec(4, new inst_pack_IF_t))
    

    val inst_queue_ready    = Output(Bool())
    val flush               = Input(Bool())
}

class Fetch_Queue extends Module{
    val io = IO(new Fetch_Queue_IO)
    import Fetch._
    val queue = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(8)(0.U.asTypeOf(new inst_pack_IF_t))))))

    val head = RegInit(VecInit(Seq.fill(4)(0.U(3.W))))
    val tail = RegInit(VecInit(Seq.fill(4)(0.U(3.W))))
    val tail_sel = RegInit(0.U(2.W))

    val full = Wire(Bool())
    full := (head(0) === tail(0)+1.U) | (head(1) === tail(1)+1.U) | (head(2) === tail(2)+1.U) | (head(3) === tail(3)+1.U)
    val empty = Wire(Bool())
    empty := ((head(0) === tail(0)) | (head(1) === tail(1)) | (head(2) === tail(2)) | (head(3) === tail(3)))

    io.inst_queue_ready := !full

    for(i <- 0 until 4){
        when(io.flush){
            tail(i) := 0.U
        }.elsewhen(io.insts_pack(i).inst_valid && !full){
            queue(i.U+tail_sel)(tail(i.U+tail_sel)) := io.insts_pack(i)
            tail(i.U+tail_sel) := tail(i.U+tail_sel) + 1.U
        }
        
        when(io.flush){
            head(i) := 0.U
        }.elsewhen(io.next_ready && !empty){
            head(i) := head(i) + 1.U
        }
    }
    tail_sel := Mux(io.flush, 0.U, Mux(full, tail_sel, tail_sel + PopCount(io.insts_pack.map(_.inst_valid))))

    for(i <- 0 until 4){
        io.insts_pack_id(i) := queue(i)(head(i))
        io.insts_valid_decode(i) := !empty
    }
}
// object Fetch_Queue extends App{
//     emitVerilog(new Fetch_Queue, Array("-td", "build/"))
// }
