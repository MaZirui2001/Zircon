import chisel3._
import chisel3.util._

object SB_Pack {
    class sb_t extends Bundle{
        val addr = UInt(32.W)
        val data = UInt(32.W)
        val wlen = UInt(3.W)
        val valid = Bool()
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
    val is_store_cmt    = Input(Bool())
    val st_addr_cmt     = Output(UInt(32.W))
    val st_data_cmt     = Output(UInt(32.W))
    val st_wlen_cmt     = Output(UInt(3.W))
    val flush           = Input(Bool())

    // for read in ex stage
    // val ld_addr_ex = Input(UInt(32.W))
    val ld_data_ex      = Output(UInt(32.W))
    val ld_hit          = Output(Bool())
}

class SB(n: Int) extends Module {
    val io = IO(new SB_IO)
    import SB_Pack._
    val sb = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new sb_t))))

    val head = RegInit(0.U(log2Ceil(n).W))
    val tail = RegInit(0.U(log2Ceil(n).W))
    val elem_num = RegInit(0.U((log2Ceil(n)+1).W))


    val full = elem_num === n.U
    io.full := full
    val empty = elem_num === 0.U

    // write from ex
    val is_store_ex = io.is_store_ex
    val st_addr_ex  = io.addr_ex
    val st_data_ex  = io.st_data_ex
    val st_addr_ex_valid = is_store_ex && !full
    when(io.flush){
        for(i <- 0 until n){
            sb(i).valid := false.B
        }
    }.elsewhen(st_addr_ex_valid && !io.flush){
        sb(tail).addr := st_addr_ex
        sb(tail).data := st_data_ex
        sb(tail).wlen := io.st_wlen_ex
        sb(tail).valid := true.B
    }
    // for commit 
    val is_store_and_cmt = io.is_store_cmt

    head := Mux(io.flush, 0.U, head + is_store_and_cmt)
    elem_num := Mux(io.flush, 0.U, elem_num - is_store_and_cmt + st_addr_ex_valid)
    tail := Mux(io.flush, 0.U, tail + st_addr_ex_valid)

    when(is_store_and_cmt){
        sb(head).valid := false.B
    }
    io.st_addr_cmt := sb(head).addr
    io.st_data_cmt := sb(head).data
    io.st_wlen_cmt := sb(head).wlen

    // read from ex
    val ld_addr_ex = io.addr_ex
    val ld_hit = Wire(Vec(n, Bool()))
    for(i <- 0 until n){
        ld_hit(i) := sb(i).valid && sb(i).addr === ld_addr_ex
    }
    io.ld_hit := ld_hit.exists(_ === true.B)
    io.ld_data_ex := Mux1H(ld_hit, sb.map(_.data))
}