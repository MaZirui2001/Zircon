import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._

class Dispatch_IO(n: Int) extends Bundle{
    val inst_packs          = Input(Vec(4, new inst_pack_RN_t))
    // elem num in alu issue queues
    val elem_num            = Input(Vec(3, UInt((log2Ceil(n)+1).W)))

    // output for each issue queue
    val insts_disp_index    = Output(Vec(5, Vec(4, UInt(3.W))))
    val insts_disp_valid    = Output(Vec(5, Vec(4, Bool())))
}

class Dispatch extends Module{
    val io = IO(new Dispatch_IO(8))

    val queue_id_hit    = Wire(Vec(4, UInt(5.W)))
    val fu_ids          = io.inst_packs.map(_.fu_id)
    var fu1_num         = io.elem_num(0)
    var fu2_num         = io.elem_num(1)
    var fu3_num         = io.elem_num(2)
    val min             = Mux(fu1_num <= fu2_num, Mux(fu1_num <= fu3_num, 0.U, 2.U), Mux(fu2_num <= fu3_num, 1.U, 2.U))
    for(i <- 0 until 4){
        // val fu1_next_num    = Wire(UInt((log2Ceil(8)+1).W))
        // val fu2_next_num    = Wire(UInt((log2Ceil(8)+1).W))
        // val fu3_next_num    = Wire(UInt((log2Ceil(8)+1).W))
        //val min             = Mux(fu1_num <= fu2_num, Mux(fu1_num <= fu3_num, 0.U, 2.U), Mux(fu2_num <= fu3_num, 1.U, 2.U))
        queue_id_hit(i)     := (UIntToOH(Mux(io.inst_packs(i).fu_id === ARITH, min, io.inst_packs(i).fu_id))) & Fill(5, io.inst_packs(i).inst_valid)
        // fu1_next_num        := Mux(queue_id_hit(i)(0), fu1_num + 1.U, fu1_num)
        // fu2_next_num        := Mux(queue_id_hit(i)(1), fu2_num + 1.U, fu2_num)
        // fu3_next_num        := Mux(queue_id_hit(i)(2), fu3_num + 1.U, fu3_num)
        // fu1_num             = fu1_next_num
        // fu2_num             = fu2_next_num
        // fu3_num             = fu3_next_num
    }
    
    // alloc insts to issue queue, pressed
    io.insts_disp_index     := DontCare
    io.insts_disp_valid     := VecInit(Seq.fill(5)(VecInit(Seq.fill(4)(false.B))))
    var alloc_index         = VecInit(Seq.fill(5)(0.U(2.W)))
    for(i <- 0 until 4){
        var next_alloc_index = Wire(Vec(5, UInt(2.W)))
        for(j <- 0 until 5){
            when(queue_id_hit(i)(j)){
                io.insts_disp_index(j)(alloc_index(j)) := i.U(2.W)
                io.insts_disp_valid(j)(alloc_index(j)) := true.B
            }
            next_alloc_index(j) := Mux(queue_id_hit(i)(j), alloc_index(j) + 1.U, alloc_index(j))
        }
        alloc_index = next_alloc_index
    }
}
