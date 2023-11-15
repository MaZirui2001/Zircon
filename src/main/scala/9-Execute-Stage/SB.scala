import chisel3._
import chisel3.util._
import Control_Signal._

object SB_Pack {
    class sb_t extends Bundle{
        val addr = UInt(32.W)
        val data = UInt(32.W)
        val wlen = UInt(2.W)
    }
}

class SB_IO extends Bundle {
    // for write in ex stage
    // val is_store_ex     = Input(Bool())
    val addr_ex         = Input(UInt(32.W))
    val st_data_ex      = Input(UInt(32.W))
    // val st_wlen_ex      = Input(UInt(2.W))
    val mem_type_ex     = Input(UInt(5.W))
    val full            = Output(Bool())

    // for commit in wb stage
    val is_store_num_cmt = Input(UInt(2.W))
    val st_cmt_valid     = Output(Bool())
    val dcache_miss      = Input(Bool())
    // val st_cmt_ready     = Input(Bool())
    val st_addr_cmt      = Output(UInt(32.W))
    val st_data_cmt      = Output(UInt(32.W))
    val st_wlen_cmt      = Output(UInt(2.W))
    val flush            = Input(Bool())

    // for read in ex stage
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

    wait_to_cmt := wait_to_cmt + io.is_store_num_cmt - (io.st_cmt_valid && !io.dcache_miss)
    io.st_cmt_valid := wait_to_cmt.orR
    when(io.flush){
        flush_buf := true.B
    }.elsewhen(wait_to_cmt === 0.U){
        flush_buf := false.B
    }
    io.full := full
    val empty = elem_num === 0.U

    // write from ex
    val is_store_ex = io.mem_type_ex(4)
    val st_addr_ex  = io.addr_ex
    val st_data_ex  = io.st_data_ex
    val st_addr_ex_valid = is_store_ex && !full
    when(!io.flush && st_addr_ex_valid){
        sb(tail(log2Ceil(n)-1, 0)).addr := st_addr_ex
        sb(tail(log2Ceil(n)-1, 0)).data := st_data_ex
        sb(tail(log2Ceil(n)-1, 0)).wlen := io.mem_type_ex(1, 0)
    }


    head := Mux(flush_buf && !wait_to_cmt.orR, 0.U, head + (io.st_cmt_valid && !io.dcache_miss))
    elem_num := Mux(flush_buf && !wait_to_cmt.orR, 0.U, elem_num - (io.st_cmt_valid && !io.dcache_miss) + Mux(full, 0.U, st_addr_ex_valid))
    tail := Mux(flush_buf, 0.U, tail + Mux(full, 0.U, st_addr_ex_valid))

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

    io.ld_hit := ld_hit.exists(_ === true.B) && io.mem_type_ex(3)
    val ld_hit_index = PriorityEncoderOH(ld_hit)
    val rdata = VecInit.tabulate(n)(i => sb(tail(log2Ceil(n)-1,0)-i.U).data)
    val ld_data_ex = rdata(OHToUInt(ld_hit_index))
    io.ld_data_ex := MuxLookup(io.mem_type_ex, 0.U)(Seq(
        MEM_LDB -> Fill(24, ld_data_ex(7)) ## ld_data_ex(7, 0),
        MEM_LDH -> Fill(16, ld_data_ex(15)) ## ld_data_ex(15, 0),
        MEM_LDW -> ld_data_ex,
        MEM_LDBU -> Fill(24, 0.U) ## ld_data_ex(7, 0),
        MEM_LDHU -> Fill(16, 0.U) ## ld_data_ex(15, 0),
    ))
    // io.ld_data_ex := rdata(OHToUInt(ld_hit_index))
}