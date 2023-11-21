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
    val addr_ex         = Input(UInt(32.W))
    val st_data_ex      = Input(UInt(32.W))
    val mem_type_ex     = Input(UInt(5.W))
    val full            = Output(Bool())

    // for commit in wb stage
    val is_store_num_cmt = Input(UInt(2.W))
    val st_cmt_valid     = Output(Bool())
    val dcache_miss      = Input(Bool())
    val st_addr_cmt      = Output(UInt(32.W))
    val st_data_cmt      = Output(UInt(32.W))
    val st_wlen_cmt      = Output(UInt(2.W))
    val flush            = Input(Bool())

    // for read in ex stage
    val ld_data_ex       = Output(UInt(32.W))
    val ld_hit           = Output(Vec(4, Bool()))
}

class SB(n: Int) extends Module {
    val io = IO(new SB_IO)
    import SB_Pack._
    val sb = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new sb_t))))

    val head = RegInit(0.U((log2Ceil(n)+1).W))
    val head_idx = head(log2Ceil(n)-1, 0)
    val head_flg = head(log2Ceil(n))

    val tail = RegInit(0.U((log2Ceil(n)+1).W))
    val tail_idx = tail(log2Ceil(n)-1, 0)
    val tail_flg = tail(log2Ceil(n))

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
        sb(tail_idx).addr := st_addr_ex
        sb(tail_idx).data := st_data_ex
        sb(tail_idx).wlen := io.mem_type_ex(1, 0)
    }


    head        := Mux(flush_buf && !wait_to_cmt.orR, 0.U, head + (io.st_cmt_valid && !io.dcache_miss))
    elem_num    := Mux(flush_buf && !wait_to_cmt.orR, 0.U, elem_num - (io.st_cmt_valid && !io.dcache_miss) + Mux(full, 0.U, st_addr_ex_valid))
    tail        := Mux(flush_buf, 0.U, tail + Mux(full, 0.U, st_addr_ex_valid))

    io.st_addr_cmt := sb(head_idx).addr
    io.st_data_cmt := sb(head_idx).data
    io.st_wlen_cmt := sb(head_idx).wlen

    // read from ex
    val ld_addr_ex      = io.addr_ex
    val ld_hit          = Wire(Vec(4, Vec(n, Bool())))
    val ld_mask         = (UIntToOH(UIntToOH(io.mem_type_ex(1, 0))) - 1.U)(3, 0)
    val sb_order        = VecInit(Seq.tabulate(n)(i => sb(tail_idx-1.U-i.U)))
    val ld_bit_hit      = VecInit(Seq.tabulate(4)(i => ld_hit(i).reduce(_||_) && ld_mask(i)))
    val ld_hit_data     = Wire(Vec(4, UInt(8.W)))


    for(i <- 0 until 4){
        val addr_ex = ld_addr_ex + i.U
        for(j <- 0 until n){
            val in_queue = Mux(head_flg ^ tail_flg, 
                               tail_idx-j.U-1.U >= head_idx || tail_idx-j.U-1.U < tail_idx, 
                               tail_idx-j.U-1.U >= head_idx && tail_idx-j.U-1.U < tail_idx)
            ld_hit(i)(j) := (sb_order(j).addr <= addr_ex && sb_order(j).addr + UIntToOH(sb_order(j).wlen) > addr_ex) && in_queue
        }
        val ld_hit_index    = PriorityEncoder(ld_hit(i).asUInt)
        val hit_byte        = (sb_order(ld_hit_index).data >> ((ld_addr_ex + i.U - sb_order(ld_hit_index).addr) ## 0.U(3.W)))(7, 0)
        ld_hit_data(i)      := Mux(ld_bit_hit(i), hit_byte, 0.U)
    }


    val ld_hit_mask     = (15.U << UIntToOH(io.mem_type_ex(1, 0)))(3, 0) & Fill(4, io.mem_type_ex(3))
    io.ld_data_ex       := ld_hit_data.asUInt
    io.ld_hit           := (ld_hit_mask | ld_bit_hit.asUInt).asBools
}