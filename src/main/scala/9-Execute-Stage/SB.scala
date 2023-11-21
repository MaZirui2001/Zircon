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
    val ld_hit = Wire(Vec(4, Vec(n, Bool())))
    val ld_mask = (UIntToOH(UIntToOH(io.mem_type_ex(1, 0))) - 1.U)(3, 0)
    val hit_item = Wire(Vec(4, new sb_t))
    val sb_tail_first = VecInit(Seq.tabulate(n)(i => sb(tail(log2Ceil(n)-1, 0)-i.U)))
    val ld_bit_hit = VecInit(Seq.tabulate(4)(i => ld_hit(i).reduce(_||_) && ld_mask(i)))
    for(i <- 0 until 4){
        val addr_ex = ld_addr_ex + i.U
        for(j <- 0 until n){
            val in_queue = Mux(head(log2Ceil(n)) ^ tail(log2Ceil(n)), tail(log2Ceil(n)-1, 0)-j.U >= head(log2Ceil(n)-1, 0) || tail(log2Ceil(n)-1, 0)-j.U < tail(log2Ceil(n)-1, 0), tail(log2Ceil(n)-1, 0)-j.U >= head(log2Ceil(n)-1, 0) && tail(log2Ceil(n)-1, 0)-j.U < tail(log2Ceil(n)-1, 0))
            ld_hit(i)(j) := (sb_tail_first(j).addr <= addr_ex && sb_tail_first(j).addr + UIntToOH(sb_tail_first(j).wlen) > addr_ex) && in_queue 
        }
        val ld_hit_index = PriorityEncoder(ld_hit(i).asUInt)
        hit_item(i) := Mux(ld_bit_hit(i), sb_tail_first(ld_hit_index), 0.U.asTypeOf(new sb_t))
    }

    val ld_hit_data = Wire(Vec(4, UInt(8.W)))

    for(i <- 0 until 4){
        val hit_byte = (hit_item(i).data >> ((ld_addr_ex + i.U - hit_item(i).addr) ## 0.U(3.W)))(7, 0)
        ld_hit_data(i) := Mux(ld_bit_hit(i), hit_byte, 0.U)
    }
    val ld_hit_mask = (15.U << UIntToOH(io.mem_type_ex(1, 0)))(3, 0) & Fill(4, io.mem_type_ex(3))
    io.ld_data_ex := ld_hit_data.asUInt
    io.ld_hit := (ld_hit_mask | ld_bit_hit.asUInt).asBools
    //val ld_hit = Wire(Vec(4, Vec(n, Bool())))
    //val ld_byte_hit = Wire(Vec(4, Vec(n, Bool())))
    // for(j <- 0 until 4){
    //     for(i <- 0 until n){
    //         ld_hit(j)(i) := (sb(tail(log2Ceil(n)-1, 0)-i.U).addr === ld_addr_ex + j.U) && Mux(head(log2Ceil(n)) ^ tail(log2Ceil(n)), tail(log2Ceil(n)-1, 0)-i.U >= head(log2Ceil(n)-1, 0) || tail(log2Ceil(n)-1, 0)-i.U < tail(log2Ceil(n)-1, 0), tail(log2Ceil(n)-1, 0)-i.U >= head(log2Ceil(n)-1, 0) && tail(log2Ceil(n)-1, 0)-i.U < tail(log2Ceil(n)-1, 0))
    //         //ld_byte_hit(j)(i) := (sb(tail(log2Ceil(n)-1, 0)-i.U).addr === ld_addr_ex + j.U) && Mux(head(log2Ceil(n)) ^ tail(log2Ceil(n)), tail(log2Ceil(n)-1, 0)-i.U >= head(log2Ceil(n)-1, 0) || tail(log2Ceil(n)-1, 0)-i.U < tail(log2Ceil(n)-1, 0), tail(log2Ceil(n)-1, 0)-i.U >= head(log2Ceil(n)-1, 0) && tail(log2Ceil(n)-1, 0)-i.U < tail(log2Ceil(n)-1, 0))
    //     }
    // }
    // val ld_mask = (15.U << UIntToOH(io.mem_type_ex(1, 0)))(3, 0) & Fill(4, io.mem_type_ex(3))
    // val byte_hit = VecInit(ld_hit.map(_.reduce(_||_) && io.mem_type_ex(3)))

    // val ld_hit_index = VecInit(Seq.tabulate(4)(i => PriorityEncoder(ld_hit(i).asUInt)))
    // val data_all = VecInit(Seq.tabulate(n)(i => sb(tail(log2Ceil(n)-1,0)-i.U).data))
    // val wlen_all = VecInit(Seq.tabulate(n)(i => sb(tail(log2Ceil(n)-1,0)-i.U).wlen))
    // val rdata = VecInit(Seq.tabulate(4)(i => Mux(byte_hit(i), (data_all(ld_hit_index(i)) << (i.U ## 0.U(3.W))) , 0.U)))
    // val ld_data_ex = rdata.reduce(_|_)
    // val ld_hit_mask = (((1.U << VecInit(Seq.tabulate(4)(i => Mux(byte_hit(i), 1.U << (wlen_all(ld_hit_index(i))), 0.U))).reduce(_+_)) - 1.U) << )(3, 0)
    // io.ld_hit := (ld_hit_mask | ld_mask.asUInt).asBools

    // // io.ld_data_ex := MuxLookup(io.mem_type_ex, 0.U)(Seq(
    // //     MEM_LDB -> Fill(24, ld_data_ex(7)) ## ld_data_ex(7, 0),
    // //     MEM_LDH -> Fill(16, ld_data_ex(15)) ## ld_data_ex(15, 0),
    // //     MEM_LDW -> ld_data_ex,
    // //     MEM_LDBU -> Fill(24, 0.U) ## ld_data_ex(7, 0),
    // //     MEM_LDHU -> Fill(16, 0.U) ## ld_data_ex(15, 0),
    // // ))
    // io.ld_data_ex := MuxLookup(io.mem_type_ex(1, 0), 0.U)(Seq(
    //     0.U(2.W) -> ld_data_ex(7, 0),
    //     1.U(2.W) -> ld_data_ex(15, 0),
    //     2.U(2.W) -> ld_data_ex(31, 0)
    // ))
}