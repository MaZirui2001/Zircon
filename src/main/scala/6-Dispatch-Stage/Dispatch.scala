import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._

// LUT: 2377

class Dispatch_IO(n: Int) extends Bundle{
    val inst_packs          = Input(Vec(4, new inst_pack_RN_t))

    val elem_num            = Input(Vec(2, UInt((log2Ceil(n)+1).W)))

    // output for each issue queue
    val insts_disp_index    = Output(Vec(4, Vec(4, UInt(2.W))))
    val insts_disp_valid    = Output(Vec(4, Vec(4, Bool())))
}

class Dispatch extends Module{
    val io = IO(new Dispatch_IO(8))

    val queue_id_hit = Wire(Vec(4, UInt(4.W)))
    var fu1_num = io.elem_num(0)
    var fu2_num = io.elem_num(1)
    for(i <- 0 until 4){
        val fu1_next_num = Wire(UInt((log2Ceil(8)+1).W))
        val fu2_next_num = Wire(UInt((log2Ceil(8)+1).W))
        queue_id_hit(i) := (UIntToOH(Mux(io.inst_packs(i).fu_id === ARITH, 
                                    Mux(fu1_num <= fu2_num, 0.U, 1.U), io.inst_packs(i).fu_id))) & Fill(4, io.inst_packs(i).inst_valid)
        fu1_next_num := Mux(queue_id_hit(i)(0), fu1_num + 1.U, fu1_num)
        fu2_next_num := Mux(queue_id_hit(i)(1), fu2_num + 1.U, fu2_num)
        fu1_num = fu1_next_num
        fu2_num = fu2_next_num
    }
    
    // alloc insts to issue queue, pressed
    io.insts_disp_index := DontCare
    io.insts_disp_valid := VecInit(Seq.fill(4)(VecInit(Seq.fill(4)(false.B))))
    var alloc_index = VecInit(Seq.fill(4)(0.U(2.W)))
    for(i <- 0 until 4){
        var next_alloc_index = Wire(Vec(4, UInt(2.W)))
        for(j <- 0 until 4){
            when(queue_id_hit(i)(j)){
                io.insts_disp_index(j)(alloc_index(j)) := i.U(3.W)
                io.insts_disp_valid(j)(alloc_index(j)) := true.B
            }
            next_alloc_index(j) := Mux(queue_id_hit(i)(j), alloc_index(j) + 1.U, alloc_index(j))
        }
        alloc_index = next_alloc_index
    }
}


// object Dispatch_Func{
//     def Dispatch_Ready_Generate(pr: UInt, prd_queue: Vec[UInt], index: UInt): Bool = {
//         val prd_hit = Wire(Vec(9, Bool()))
//         prd_hit := 0.U.asTypeOf(Vec(9, Bool()))
//         val n = MuxLookup(index, 0.U)(Seq(
//             0.U -> 8.U,
//             1.U -> 8.U,
//             2.U -> 9.U,
//             3.U -> 8.U
//         ))
//         for(i <- 0 until 9){
//             when(i.U < n) {
//                 prd_hit(i) := pr === prd_queue(i)
//             }
//         }
//         !(prd_hit.reduce(_ || _))
//     }
//     def Ready_Generate(pr: UInt, prd_queue: Vec[Vec[UInt]]): Bool = {
//         val prd_ready = Wire(Vec(4, Bool()))
//         for(i <- 0 until 4){
//             prd_ready(i) := Dispatch_Ready_Generate(pr, prd_queue(i), i.U(2.W))
//         }
//         prd_ready.reduce(_ && _)
//     }

// }
