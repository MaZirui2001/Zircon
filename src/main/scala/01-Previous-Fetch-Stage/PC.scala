import chisel3._
import chisel3.util._
import CPU_Config._

class PC_IO extends Bundle {
    val pc_PF           = Output(UInt(32.W))
    val pc_stall        = Input(Bool())
    val predict_fail    = Input(Bool())
    val npc             = Output(UInt(32.W))
    val pred_jump       = Input(Vec(FRONT_WIDTH, Bool()))
    val pred_npc        = Input(UInt(32.W))
    val branch_target   = Input(UInt(32.W))
    val inst_valid_PF   = Output(Vec(FRONT_WIDTH, Bool()))

    val flush_by_pd     = Input(Bool())
    val flush_pd_target = Input(UInt(32.W))
}

class PC(reset_val: Int) extends Module {
    val FRONT_LOG2 = log2Ceil(FRONT_WIDTH)
    val io = IO(new PC_IO)

    val pc = RegInit(reset_val.U(32.W))

    when(io.predict_fail) {
        io.npc := io.branch_target
    }
    .elsewhen(io.flush_by_pd){
        io.npc := io.flush_pd_target
    }
    .elsewhen(!io.pc_stall) {
        when(io.pred_jump.reduce(_||_)){
            io.npc := io.pred_npc
        }.otherwise{
            io.npc := (pc + (1 << (FRONT_LOG2+2)).U)(31, FRONT_LOG2+2) ## 0.U((FRONT_LOG2+2).W)
        }
    }
    .otherwise{
        io.npc := pc
    }

    io.pc_PF := pc
    pc := io.npc

    val inst_valid_temp = VecInit(PriorityEncoderOH(io.pred_jump)).asUInt
    val valid_mask = ((1 << FRONT_WIDTH)-1).U >> pc(2+FRONT_LOG2-1, 2)

    io.inst_valid_PF := (((inst_valid_temp ## 0.U(1.W)) - 1.U)(FRONT_WIDTH-1, 0) & valid_mask).asBools

}