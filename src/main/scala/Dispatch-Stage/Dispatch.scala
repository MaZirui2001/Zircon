import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._

// LUT: 2377
object Dispatch_Func{
    def Dispatch_Ready_Generate(pr: UInt, prd_queue: Vec[UInt], queue_sel: UInt, index: UInt): Bool = {
        val prd_hit = Wire(Vec(10, Bool()))
        prd_hit := 0.U.asTypeOf(Vec(10, Bool()))
        val equal = MuxLookup(index, 0.U)(Seq(
            0.U -> 8.U,
            1.U -> 8.U,
            2.U -> 9.U,
            3.U -> 8.U
        ))
        val neq = MuxLookup(index, 0.U)(Seq(
            0.U -> 9.U,
            1.U -> 9.U,
            2.U -> 10.U,
            3.U -> 9.U
        ))
        val n = Mux(queue_sel === index, equal, neq)
        for(i <- 0 until 10){
            when(i.U < n) {
                prd_hit(i) := pr === prd_queue(i)
            }
        }
        !(prd_hit.exists(_ === true.B))
    }
    def Ready_Generate(pr: UInt, prd_queue: Vec[Vec[UInt]], queue_sel: UInt): Bool = {
        val prd_ready = Wire(Vec(4, Bool()))
        for(i <- 0 until 4){
            prd_ready(i) := Dispatch_Ready_Generate(pr, prd_queue(i), queue_sel, i.U(2.W))
        }
        prd_ready.forall(_ === true.B)
    }

}
class Dispatch_IO(n: Int) extends Bundle{
    val inst_packs = Input(Vec(4, new inst_pack_t))
    val prj_raw = Input(Vec(4, Bool()))
    val prk_raw = Input(Vec(4, Bool()))
    val insts_valid = Input(Vec(4, Bool()))

    // index of rd in the issue queue
    val prd_queue = Input(Vec(4, Vec(n+2, UInt(6.W))))
    val elem_num = Input(Vec(2, UInt((log2Ceil(n)+1).W)))

    // output for each issue queue
    val insts_dispatch = Output(Vec(4, Vec(4, new inst_pack_t)))

    val insert_num = Output(Vec(4, UInt(3.W)))

    val prj_ready = Output(Vec(4, Vec(4, Bool())))

    val prk_ready = Output(Vec(4, Vec(4, Bool())))
    
}

class Dispatch extends RawModule{
    val io = IO(new Dispatch_IO(8))

    val queue_sel = Wire(Vec(4, UInt(2.W)))
    val queue_id_hit = Wire(Vec(4, Vec(4, Bool())))
    val queue_id_hit_trav = Wire(Vec(4, Vec(4, Bool())))
    for(i <- 0 until 4){
        queue_sel(i) := Mux(io.inst_packs(i).fu_id === ARITH, 
                     Mux(io.elem_num(0) <= io.elem_num(1), 0.U, 1.U), io.inst_packs(i).fu_id)
        for(j <- 0 until 4){
            queue_id_hit(i)(j) := queue_sel(i) === j.U && io.insts_valid(i)
            queue_id_hit_trav(i)(j) := queue_id_hit(j)(i)
        }
        io.insert_num(i) := Mux(queue_id_hit_trav(i).asUInt.orR, PopCount(queue_id_hit_trav(i)), 0.U)
    }
    val prj_ready = Wire(Vec(4, Bool()))
    val prk_ready = Wire(Vec(4, Bool()))
    import Dispatch_Func._
    for(i <- 0 until 4){
        prj_ready(i) := !io.inst_packs(i).rj_valid || io.inst_packs(i).prj === 0.U || (!io.prj_raw(i) && Ready_Generate(io.inst_packs(i).prj, io.prd_queue, queue_sel(i)))
        prk_ready(i) := !io.inst_packs(i).rk_valid || io.inst_packs(i).prk === 0.U || (!io.prk_raw(i) && Ready_Generate(io.inst_packs(i).prk, io.prd_queue, queue_sel(i)))
    }
    
    // alloc insts to issue queue, pressed
    val index_now = Wire(Vec(5, Vec(4, UInt(2.W))))
    index_now := 0.U.asTypeOf(Vec(5, Vec(4, UInt(2.W))))
    io.insts_dispatch := 0.U.asTypeOf(Vec(4, Vec(4, new inst_pack_t)))
    io.prj_ready := 0.U.asTypeOf(Vec(4, Vec(4, Bool())))
    io.prk_ready := 0.U.asTypeOf(Vec(4, Vec(4, Bool())))

    for(i <- 0 until 4){
        for(j <- 0 until 4){
            when(queue_id_hit(i)(j)){
                io.insts_dispatch(j)(index_now(i)(j)) := io.inst_packs(i)
                io.prj_ready(j)(index_now(i)(j)) := prj_ready(i)
                io.prk_ready(j)(index_now(i)(j)) := prk_ready(i)
                
            }
            index_now(i+1)(j) := index_now(i)(j) + queue_id_hit(i)(j)
        }
    }
}
// object Dispatch extends App{
//     emitVerilog(new Dispatch, Array("-td", "build/"))
// }
