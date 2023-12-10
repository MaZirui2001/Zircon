import chisel3._
import chisel3.util._
import Inst_Pack._

class MD_EX1_EX2_Reg extends Module {
    val io = IO(new Bundle {
        val flush = Input(Bool())
        val stall = Input(Bool())
        val inst_pack_EX1 = Input(new inst_pack_IS_MD_t)
      
        val inst_pack_EX2 = Output(new inst_pack_IS_MD_t)
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_IS_MD_t))


    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_IS_MD_t)
        
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX1
    }

    io.inst_pack_EX2 := inst_pack_reg

}
