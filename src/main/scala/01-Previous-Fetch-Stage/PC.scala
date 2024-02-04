import chisel3._
import chisel3.util._
import CPU_Config._

class PC_IO extends Bundle {
    val pc_PF           = Output(Vec(10, UInt(32.W)))
    val pc_stall        = Input(Bool())
    val predict_fail    = Input(Bool())
    val npc             = Output(Vec(10, UInt(32.W)))
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

    // csr change
    val has_csr_change  = Input(Bool())
}

class PC(reset_val: Int) extends Module {
    val io = IO(new PC_IO)

    val pc = RegInit(VecInit.fill(10)(reset_val.U(32.W)))
    val idle_en = RegInit(false.B)

    when(io.has_intr){
        idle_en := false.B
    }.elsewhen(!idle_en){
        idle_en := io.is_idle_cmt
    }
    for(i <- 0 until 10){
        when(idle_en || io.has_csr_change){
            io.npc(i) := pc(i)
        }.elsewhen(io.predict_fail) {
            io.npc(i) := io.branch_target
        }
        .elsewhen(io.flush_by_pd){
            io.npc(i) := io.flush_pd_target
        }
        .elsewhen(!io.pc_stall) {
            when(io.pred_jump.asUInt.orR){
                io.npc(i) := io.pred_npc
            }.otherwise{
                io.npc(i) := (pc(i) + 8.U)(31, 3) ## 0.U(3.W)
            }
        }.otherwise{
            io.npc(i) := pc(i)
        }

        io.pc_PF(i) := pc(i)
        pc(i) := io.npc(i)
    }

    val inst_valid_temp = Mux(idle_en || io.has_csr_change, 0.U, !io.pred_jump(0) ## true.B)
    val valid_mask = !pc(0)(2) ## true.B

    io.inst_valid_PF := (inst_valid_temp & valid_mask).asBools
    io.exception_PF := Mux(pc(0)(1, 0) === 0.U, 0.U, 0x88.U)

}