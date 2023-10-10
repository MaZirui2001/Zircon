import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._

// LUT: 2377

object Dispatch_Func{
    def Dispatch_Ready_Generate(pr_raw: Bool, pr: UInt, prd_queue: Vec[Vec[UInt]]): Bool = {
        val prd_queue_temp = Wire(Vec(4, Vec(8, Bool())))
        for(i <- 0 until 4){
            for(j <- 0 until 8){
                prd_queue_temp(i)(j) := pr === prd_queue(i)(j)
            }
        }
        val prd_hit = prd_queue_temp(0).asUInt.orR | prd_queue_temp(1).asUInt.orR | prd_queue_temp(2).asUInt.orR | prd_queue_temp(3).asUInt.orR
        val ready = !prd_hit & !pr_raw & pr =/= 0.U
        ready
    }
}
class Dispatch_IO(n: Int) extends Bundle{
    val inst_packs = Input(Vec(4, new inst_pack_t))
    val prj_raw = Input(Vec(4, Bool()))
    val prk_raw = Input(Vec(4, Bool()))
    val insts_valid = Input(Vec(4, Bool()))

    // index of rd in the issue queue
    val prd_queue = Input(Vec(4, Vec(n, UInt(6.W))))
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
    for(i <- 0 until 4){
        queue_sel(i) := Mux(io.inst_packs(i).fu_id === ARITH, 
                     Mux(io.elem_num(0) < io.elem_num(1), 0.U, 1.U), io.inst_packs(i).fu_id)
        for(j <- 0 until 4){
            queue_id_hit(i)(j) := queue_sel(i) === j.U
        }
        io.insert_num(i) := PopCount(queue_id_hit(i))
    }
    val prj_ready = Wire(Vec(4, Bool()))
    val prk_ready = Wire(Vec(4, Bool()))
    for(i <- 0 until 4){
        prj_ready(i) := Dispatch_Func.Dispatch_Ready_Generate(io.prj_raw(i), io.inst_packs(i).prj, io.prd_queue)
        prk_ready(i) := Dispatch_Func.Dispatch_Ready_Generate(io.prk_raw(i), io.inst_packs(i).prk, io.prd_queue)
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
                index_now(i+1)(j) := index_now(i)(j) + 1.U
            }
        }
    }
}
// object Dispatch extends App{
//     emitVerilog(new Dispatch, Array("-td", "build/"))
// }
