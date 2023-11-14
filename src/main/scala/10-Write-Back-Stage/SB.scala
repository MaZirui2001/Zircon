import chisel3._
import chisel3.util._

object SB_Pack {
    class sb_t extends Bundle{
        val addr = UInt(32.W)
        val data = UInt(32.W)
        val wlen = UInt(3.W)
    }
}

class SB_IO extends Bundle {
    // for write in ex stage
    val is_store_ex     = Input(Bool())
    val addr_ex         = Input(UInt(32.W))
    val st_data_ex      = Input(UInt(32.W))
    val st_wlen_ex      = Input(UInt(3.W))
    val full            = Output(Bool())

    // for commit in wb stage
    val is_store_num_cmt = Input(UInt(2.W))
    val is_store_cmt     = Output(Bool())
    val st_addr_cmt      = Output(UInt(32.W))
    val st_data_cmt      = Output(UInt(32.W))
    val st_wlen_cmt      = Output(UInt(3.W))
    val flush            = Input(Bool())

    // for read in ex stage
    // val ld_addr_ex = Input(UInt(32.W))
    val ld_data_ex       = Output(UInt(32.W))
    val ld_hit           = Output(Bool())
}

class SB(n: Int) extends Module {
    val io = IO(new SB_IO)
    import SB_Pack._
    val sb = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new sb_t))))

    val head = RegInit(0.U((log2Ceil(n)+1).W))
    val tail = RegInit(0.U((log2Ceil(n)+1).W))
    val elem_num = RegInit(0.U((log2Ceil(n)+1).W))

    val flush_buf = RegInit(false.B)
    val full = elem_num === n.U || flush_buf
    val wait_to_cmt = RegInit(0.U(log2Ceil(n).W))

    val has_store_cmt = io.is_store_num_cmt =/= 0.U

    when(has_store_cmt){
        wait_to_cmt := wait_to_cmt + io.is_store_num_cmt - 1.U
    }.elsewhen(io.is_store_cmt){
        wait_to_cmt := wait_to_cmt - 1.U
    }
    io.is_store_cmt := wait_to_cmt.orR || has_store_cmt
    flush_buf := Mux(io.flush && (io.is_store_num_cmt > 1.U || wait_to_cmt.orR), true.B, Mux(wait_to_cmt =/= 0.U, flush_buf, false.B))
    io.full := full
    val empty = elem_num === 0.U

    // write from ex
    val is_store_ex = io.is_store_ex
    val st_addr_ex  = io.addr_ex
    val st_data_ex  = io.st_data_ex
    val st_addr_ex_valid = is_store_ex && !full
    when(!io.flush && st_addr_ex_valid){
        sb(tail(log2Ceil(n)-1, 0)).addr := st_addr_ex
        sb(tail(log2Ceil(n)-1, 0)).data := st_data_ex
        sb(tail(log2Ceil(n)-1, 0)).wlen := io.st_wlen_ex
    }


    head := Mux((io.flush || flush_buf) && !wait_to_cmt.orR, 0.U, head + (wait_to_cmt.orR || has_store_cmt))
    elem_num := Mux((io.flush || flush_buf) && !wait_to_cmt.orR, 0.U, elem_num - io.is_store_cmt + Mux(full, 0.U, st_addr_ex_valid))
    tail := Mux(io.flush, 0.U, tail + Mux(full, 0.U, st_addr_ex_valid))

    io.st_addr_cmt := sb(head(log2Ceil(n)-1,0)).addr
    io.st_data_cmt := sb(head(log2Ceil(n)-1,0)).data
    io.st_wlen_cmt := sb(head(log2Ceil(n)-1,0)).wlen

    // read from ex
    val ld_addr_ex = io.addr_ex
    val ld_hit = Wire(Vec(n, Bool()))
    for(i <- 0 until n){
        ld_hit(i) := (sb(tail(log2Ceil(n)-1, 0)-i.U).addr <= ld_addr_ex && ld_addr_ex < sb(tail(log2Ceil(n)-1, 0)-i.U).addr + (1.U(32.W) << sb(tail(log2Ceil(n)-1, 0)-i.U).wlen) 
                        && Mux(head(log2Ceil(n)) ^ tail(log2Ceil(n)), tail(log2Ceil(n)-1, 0)-i.U >= head(log2Ceil(n)-1, 0) || tail(log2Ceil(n)-1, 0)-i.U < tail(log2Ceil(n)-1, 0), tail(log2Ceil(n)-1, 0)-i.U >= head(log2Ceil(n)-1, 0) && tail(log2Ceil(n)-1, 0)-i.U < tail(log2Ceil(n)-1, 0)))
    }

    io.ld_hit := ld_hit.exists(_ === true.B)
    val ld_hit_index = PriorityEncoderOH(ld_hit)
    val rdata = VecInit.tabulate(n)(i => sb(tail(log2Ceil(n)-1,0)-i.U).data)
    io.ld_data_ex := rdata(OHToUInt(ld_hit_index))
}