import chisel3._
import chisel3.util._
import Control_Signal._

object SB_Pack {
    class sb_t extends Bundle{
        val addr = UInt(32.W)
        val data = UInt(32.W)
        val wstrb = UInt(4.W)
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

    val head            = RegInit(0.U((log2Ceil(n)+1).W))
    val head_idx        = head(log2Ceil(n)-1, 0)
    val head_flg        = head(log2Ceil(n))

    val tail            = RegInit(0.U((log2Ceil(n)+1).W))
    val tail_idx        = tail(log2Ceil(n)-1, 0)
    val tail_flg        = tail(log2Ceil(n))

    val elem_num        = RegInit(0.U((log2Ceil(n)+1).W))

    val flush_buf       = RegInit(false.B)
    val full            = elem_num === n.U || flush_buf

    val empty           = elem_num === 0.U
    val wait_to_cmt     = RegInit(0.U((log2Ceil(n)+1).W))

    val has_store_cmt   = io.is_store_num_cmt =/= 0.U

    wait_to_cmt         := wait_to_cmt + io.is_store_num_cmt - (io.st_cmt_valid && !io.dcache_miss)
    io.st_cmt_valid     := wait_to_cmt.orR
    io.full             := full
    when(io.flush){
        flush_buf := true.B
    }.elsewhen(wait_to_cmt === 0.U){
        flush_buf := false.B
    }


    // write from ex
    val is_store_ex         = io.mem_type_ex(4)
    val st_addr_ex          = io.addr_ex
    val st_data_ex          = io.st_data_ex
    val st_addr_ex_valid    = is_store_ex && !full
    when(flush_buf){
        var start = head_idx + wait_to_cmt
        val clear_num = elem_num - wait_to_cmt
        for(i <- 0 until n){
            when(i.U < clear_num){
                val clear_idx = start(log2Ceil(n)-1, 0)
                sb(clear_idx).wstrb := 0.U
            }
            start = start + Mux(i.U < clear_num, 1.U, 0.U)
        }

    }
    .elsewhen(!flush_buf && st_addr_ex_valid){
        sb(tail_idx).addr   := st_addr_ex(31, 2) ## 0.U(2.W)
        sb(tail_idx).data   := (st_data_ex << (st_addr_ex(1, 0) ## 0.U(3.W)))(31, 0)
        sb(tail_idx).wstrb  := ((UIntToOH(UIntToOH(io.mem_type_ex(1, 0))) - 1.U) << st_addr_ex(1, 0))(3, 0)
    }

    head            := head + (io.st_cmt_valid && !io.dcache_miss)
    elem_num        := Mux(flush_buf && !wait_to_cmt.orR, 0.U, elem_num - (io.st_cmt_valid && !io.dcache_miss) + Mux(full, 0.U, st_addr_ex_valid))
    tail            := Mux(flush_buf && !wait_to_cmt.orR, head + (io.st_cmt_valid && !io.dcache_miss), tail + Mux(full, 0.U, st_addr_ex_valid))

    val offset     = PriorityEncoder(sb(head_idx).wstrb)
    io.st_addr_cmt := sb(head_idx).addr + offset
    io.st_data_cmt := sb(head_idx).data >> (offset ## 0.U(3.W))
    io.st_wlen_cmt := OHToUInt(PopCount(sb(head_idx).wstrb))

    // read from ex
    val ld_addr_ex      = io.addr_ex
    val ld_mask         = (UIntToOH(UIntToOH(io.mem_type_ex(1, 0))) - 1.U)(3, 0)
    val sb_order        = VecInit.tabulate(n)(i => sb(tail_idx-1.U-i.U))
    val ld_hit_data     = Wire(Vec(4, UInt(8.W)))
    val ld_hit_mask     = Mux(io.mem_type_ex(4), 0xf.U, (15.U << UIntToOH(io.mem_type_ex(1, 0)))(3, 0))
    val is_in_queue     = VecInit.tabulate(n)(i => Mux(head_flg ^ tail_flg, 
                                                        tail_idx-i.U-1.U >= head_idx || tail_idx-i.U-1.U < tail_idx, 
                                                        tail_idx-i.U-1.U >= head_idx && tail_idx-i.U-1.U < tail_idx))
    // check for each bit
    for(i <- 0 until 4){
        val addr_ex         = ld_addr_ex + i.U
        val ld_hit          = VecInit.tabulate(n)(j => sb_order(j).addr(31, 2) === addr_ex(31, 2) && sb_order(j).wstrb(addr_ex(1, 0)))
        val ld_bit_hit      = ld_hit.asUInt.orR && ld_mask(i)
        val ld_hit_index    = PriorityEncoder(ld_hit.asUInt)
        val hit_byte        = sb_order(ld_hit_index).data >> (addr_ex(1, 0) ## 0.U(3.W))
        ld_hit_data(i)      := Mux(ld_bit_hit, hit_byte, 0.U)
        io.ld_hit(i)        := ld_hit_mask(i) | ld_bit_hit
    }
    io.ld_data_ex       := ld_hit_data.asUInt
}