import chisel3._
import chisel3.util._
import CPU_Config._

class PC_IO extends Bundle {
    val pc_PF           = Output(UInt(32.W))
    val pc_stall        = Input(Bool())
    val predict_fail    = Input(Bool())
    val npc             = Output(UInt(32.W))
    val pred_jump       = Input(Vec(2, Bool()))
    val pred_npc        = Input(UInt(32.W))
    val branch_target   = Input(UInt(32.W))
    val inst_valid_PF   = Output(Vec(2, Bool()))
    val exception_PF    = Output(UInt(8.W))

    val flush_by_pd     = Input(Bool())
    val flush_pd_target = Input(UInt(32.W))

    // idle
    val is_idle_cmt     = Input(Bool())
    val has_intr        = Input(Bool())
}

class PC(reset_val: Int) extends Module {
    val io = IO(new PC_IO)

    val pc = RegInit(reset_val.U(32.W))
    val idle_en = RegInit(false.B)

    when(io.has_intr){
        idle_en := false.B
    }.elsewhen(!idle_en){
        idle_en := io.is_idle_cmt
    }

    when(idle_en){
        io.npc := pc
    }.elsewhen(io.predict_fail) {
        io.npc := io.branch_target
    }
    .elsewhen(io.flush_by_pd){
        io.npc := io.flush_pd_target
    }
    .elsewhen(!io.pc_stall) {
        when(io.pred_jump.asUInt.orR){
            io.npc := io.pred_npc
        }.otherwise{
            io.npc := (pc + 8.U)(31, 3) ## 0.U(3.W)
        }
    }.otherwise{
        io.npc := pc
    }

    io.pc_PF := pc
    pc := io.npc

    val inst_valid_temp = Mux(idle_en, 0.U, !io.pred_jump(0) ## true.B)
    val valid_mask = !pc(2) ## true.B

    io.inst_valid_PF := (inst_valid_temp & valid_mask).asBools
    io.exception_PF := Mux(pc(1, 0) === 0.U, 0.U, 0x88.U)

}