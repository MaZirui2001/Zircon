import chisel3._
import chisel3.util._
import Rat._
import Predict_Config._
import CPU_Config._

class Arch_Rat_IO(n: Int) extends Bundle {
    // for commit 
    val cmt_en          = Input(Vec(2, Bool()))
    val prd_cmt         = Input(Vec(2, UInt(log2Ceil(n).W)))
    val pprd_cmt        = Input(Vec(2, UInt(log2Ceil(n).W)))
    val rd_valid_cmt    = Input(Vec(2, Bool()))
    val predict_fail    = Input(Bool())

    // for reg rename
    val arch_rat        = Output(Vec(n, UInt(1.W)))
    val head_arch       = Output(UInt(log2Ceil(n).W))

    // for ras
    val top_arch            = Output(UInt(3.W))
    val br_type_pred_cmt    = Input(UInt(2.W))
    val pc_cmt              = Input(UInt(32.W))
    val pred_update_en_cmt  = Input(Bool())
    val ras_arch            = Output(Vec(8, UInt(32.W)))
}

class Arch_Rat(n: Int) extends Module {
    val io = IO(new Arch_Rat_IO(n))

    val arat = RegInit(VecInit.fill(n)(false.B))
    val arat_next = Wire(Vec(n, Bool()))


    val head = RegInit(0.U(log2Ceil(n).W))
    var head_next = head
    for(i <- 0 until 2){
        head_next = Mux(io.cmt_en(i) && io.rd_valid_cmt(i), Mux(head_next === (n-1).U, 0.U, head_next + 1.U), head_next)
    }
    head := head_next

    arat_next := arat
    for(i <- 0 until 2){
        when(io.rd_valid_cmt(i) && io.cmt_en(i)){
            arat_next(io.pprd_cmt(i)) := false.B
            arat_next(io.prd_cmt(i)) := true.B
        }
    }
    arat := arat_next


    // ras
    val top         = RegInit(0x7.U(3.W))
    val top_next    = Wire(UInt(3.W))
    val ras         = RegInit(VecInit.fill(8)(0x1c000000.U(32.W)))
    val ras_next    = Wire(Vec(8, UInt(32.W)))
    top_next        := top
    ras_next        := ras
    when(io.br_type_pred_cmt === RET && io.pred_update_en_cmt){
        top_next := top - 1.U
    }.elsewhen(io.br_type_pred_cmt(1) && io.pred_update_en_cmt){
        top_next := top + 1.U
        ras_next(top) := io.pc_cmt
    }
    top         := top_next
    ras         := ras_next
    io.top_arch := top_next
    io.ras_arch := ras

    io.arch_rat     := arat
    io.head_arch    := head

}