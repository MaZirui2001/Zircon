import chisel3._
import chisel3.util._
import Control_Signal._

object SB_Pack {
    class sb_t extends Bundle{
        val addr = UInt(32.W)
        val data = UInt(32.W)
        val wstrb = UInt(4.W)
        val uncache = Bool()
    }
}

class SB_IO extends Bundle {
    // for write in ex stage
    val mem_type_ex     = Input(UInt(5.W))
    val full            = Output(Bool())
    val addr_mem        = Input(UInt(32.W))
    val st_data_mem     = Input(UInt(32.W))
    val mem_type_mem    = Input(UInt(5.W))
    val uncache_mem     = Input(Bool())

    // for commit in wb stage
    val is_store_num_cmt = Input(UInt(2.W))
    val st_cmt_valid     = Output(Bool())
    val dcache_miss      = Input(Bool())
    val st_addr_cmt      = Output(UInt(32.W))
    val st_data_cmt      = Output(UInt(32.W))
    val st_wlen_cmt      = Output(UInt(2.W))
    val is_uncache_cmt   = Output(Bool())
    val flush            = Input(Bool())

    // for read in ex stage
    val ld_data_mem      = Output(UInt(32.W))
    val ld_hit           = Output(Vec(4, Bool()))
}

class SB(n: Int) extends Module {
    val io = IO(new SB_IO)
    import SB_Pack._
    val sb = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new sb_t))))

    val head            = RegInit(0.U(log2Ceil(n).W))
    val tail            = RegInit(0.U(log2Ceil(n).W))
    val elem_num        = RegInit(0.U((log2Ceil(n)+1).W))

    val flush_buf       = RegInit(false.B)
    val full            = elem_num === n.U || flush_buf
    val empty           = elem_num === 0.U

    // commit 
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
    val offset     = PriorityEncoder(sb(head).wstrb)
    io.st_addr_cmt := sb(head).addr + offset
    io.st_data_cmt := sb(head).data >> (offset ## 0.U(3.W))
    io.st_wlen_cmt := OHToUInt(PopCount(sb(head).wstrb))
    io.is_uncache_cmt := sb(head).uncache


    // ex stage: update elem_num
    val is_store_ex         = io.mem_type_ex(4)
    val st_addr_ex_valid    = is_store_ex && !full
    elem_num                := Mux(flush_buf && !wait_to_cmt.orR, 0.U, elem_num - (io.st_cmt_valid && !io.dcache_miss) + st_addr_ex_valid)
    
    // mem stage: write to sb and check for hit
    val is_store_mem        = io.mem_type_mem(4)
    val st_addr_mem_valid   = is_store_mem
    val st_addr_mem         = io.addr_mem
    val st_data_mem         = io.st_data_mem
    when(flush_buf){
        var start = head + wait_to_cmt
        val clear_num = elem_num - wait_to_cmt
        for(i <- 0 until n){
            when(i.U < clear_num){
                val clear_idx = start(log2Ceil(n)-1, 0)
                sb(clear_idx).wstrb := 0.U
            }
            start = start + 1.U
        }
    }
    .elsewhen(!flush_buf && st_addr_mem_valid){
        sb(tail).addr   := st_addr_mem(31, 2) ## 0.U(2.W)
        sb(tail).data   := (st_data_mem << (st_addr_mem(1, 0) ## 0.U(3.W)))(31, 0)
        sb(tail).wstrb  := ((UIntToOH(UIntToOH(io.mem_type_mem(1, 0))) - 1.U) << st_addr_mem(1, 0))(3, 0)
        sb(tail).uncache:= io.uncache_mem
    }
    head            := head + (io.st_cmt_valid && !io.dcache_miss)
    tail            := Mux(flush_buf && !wait_to_cmt.orR, head + (io.st_cmt_valid && !io.dcache_miss), tail + st_addr_mem_valid)

    // read from mem
    val ld_addr_mem     = io.addr_mem
    val ld_mask         = (UIntToOH(UIntToOH(io.mem_type_mem(1, 0))) - 1.U)(3, 0)
    val sb_order        = VecInit.tabulate(n)(i => sb(tail-1.U-i.U))
    val ld_hit_data     = Wire(Vec(4, UInt(8.W)))
    val ld_hit_mask     = Mux(io.mem_type_mem(4), 0xf.U, (15.U << UIntToOH(io.mem_type_mem(1, 0)))(3, 0))
    
    // check for each bit
    for(i <- 0 until 4){
        val addr_mem        = ld_addr_mem(31, 2) ## (ld_addr_mem + i.U(2.W))(1, 0)
        val ld_hit          = VecInit.tabulate(n)(j => sb_order(j).addr(31, 2) === addr_mem(31, 2) && sb_order(j).wstrb(addr_mem(1, 0)))
        val ld_bit_hit      = ld_hit.asUInt.orR && ld_mask(i)
        val ld_hit_index    = PriorityEncoder(ld_hit.asUInt)
        val hit_byte        = sb_order(ld_hit_index).data >> (addr_mem(1, 0) ## 0.U(3.W))
        ld_hit_data(i)      := Mux(ld_bit_hit, hit_byte, 0.U)
        io.ld_hit(i)        := ld_hit_mask(i) | ld_bit_hit
    }

    io.ld_data_mem       := ld_hit_data.asUInt
}