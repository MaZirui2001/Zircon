import chisel3._
import chisel3.util._

class PC_IO extends Bundle {
    val pc_IF           = Output(UInt(32.W))
    val pc_stall        = Input(Bool())
    val predict_fail    = Input(Bool())
    val npc             = Output(UInt(32.W))
    val pred_jump       = Input(Vec(4, Bool()))
    val pred_npc        = Input(UInt(32.W))
    val branch_target   = Input(UInt(32.W))
    val inst_valid_IF   = Output(Vec(4, Bool()))
}

class PC(reset_val: Int) extends Module {
    val io = IO(new PC_IO)

    val pc = RegInit(reset_val.U(32.W))

    when(io.predict_fail) {
        io.npc := io.branch_target
    }
    .elsewhen(!io.pc_stall) {
        when(io.pred_jump.asUInt.orR){
            io.npc := io.pred_npc
        }.otherwise{
            io.npc := (pc + 16.U)(31, 4) ## 0.U(4.W)
        }
    }
    .otherwise{
        io.npc := pc
    }

    io.pc_IF := pc
    pc := io.npc

    val inst_valid_temp = Wire(Vec(4, Bool()))
    inst_valid_temp := PriorityEncoderOH(io.pred_jump)

    io.inst_valid_IF := (((inst_valid_temp.asUInt << 1.U)(3, 0) - 1.U) & (15.U(4.W) >> pc(3, 2))).asBools


}