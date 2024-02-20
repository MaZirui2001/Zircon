import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._
import CPU_Config._

class Dispatch_IO(n: Int) extends Bundle{
    val inst_packs          = Input(Vec(2, new inst_pack_RN_t))
    // elem num in alu issue queues
    val elem_num            = Input(Vec(2, UInt((log2Ceil(n)+1).W)))

    // output for each issue queue
    val insts_disp_index    = Output(Vec(4, Vec(2, UInt(1.W))))
    val insts_disp_valid    = Output(Vec(4, Vec(2, Bool())))
}

class Dispatch extends Module{
    val io = IO(new Dispatch_IO(8))

    val queue_id_hit    = Wire(Vec(2, UInt(4.W)))
    val fu_ids          = io.inst_packs.map(_.fu_id)
    var fu1_num         = io.elem_num(0)
    var fu2_num         = io.elem_num(1)
    val min             = Mux(fu1_num <= fu2_num, 0.U, 1.U)
    for(i <- 0 until 2){
        queue_id_hit(i)     := (UIntToOH(Mux(io.inst_packs(i).fu_id === ARITH, min, io.inst_packs(i).fu_id))) & Fill(4, io.inst_packs(i).inst_valid)
    }
    
    // alloc insts to issue queue, pressed
    io.insts_disp_index     := DontCare
    io.insts_disp_valid     := VecInit.fill(4)(VecInit.fill(2)(false.B))
    // var alloc_index         = VecInit.fill(4)(0.U(1.W))
    for(i <- 0 until 4){
        var alloc_index = 0.U(1.W)
        for(j <- 0 until 2){
            io.insts_disp_index(i)(alloc_index) := j.U
            io.insts_disp_valid(i)(alloc_index) := queue_id_hit(j)(i)
            alloc_index = Mux(queue_id_hit(j)(i), 1.U, 0.U)
        }
    }
}
