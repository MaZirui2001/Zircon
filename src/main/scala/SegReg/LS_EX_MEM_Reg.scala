import chisel3._
import chisel3.util._
import Inst_Pack._

class LS_EX_MEM_Reg extends Module {
  val io = IO(new Bundle {
    val flush = Input(Bool())
    val stall = Input(Bool())
    val inst_pack_EX = Input(new inst_pack_IS_LS_t)
    val sb_hit_EX   = Input(Bool())
    val sb_rdata_EX = Input(UInt(32.W))
    val sb_st_cmt_valid_EX = Input(Bool())
    val is_ucread_EX   = Input(Bool())

    val inst_pack_MEM = Output(new inst_pack_IS_LS_t)

    val sb_hit_MEM   = Output(Bool())
    val sb_rdata_MEM = Output(UInt(32.W))
    val sb_st_cmt_valid_MEM = Output(Bool())
    val is_ucread_MEM = Output(Bool())
  })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_IS_LS_t))

    val sb_hit_reg = RegInit(false.B)
    val sb_rdata_reg = RegInit(0.U(32.W))
    val sb_st_cmt_valid_reg = RegInit(false.B)
    val is_ucread_Reg = RegInit(false.B)


    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_IS_LS_t)

        sb_hit_reg := false.B
        sb_rdata_reg := 0.U
        sb_st_cmt_valid_reg := false.B
        is_ucread_Reg := false.B
        
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX

        sb_hit_reg := io.sb_hit_EX
        sb_rdata_reg := io.sb_rdata_EX
        sb_st_cmt_valid_reg := io.sb_st_cmt_valid_EX
        is_ucread_Reg := io.is_ucread_EX
    }

    io.inst_pack_MEM := inst_pack_reg
    io.sb_hit_MEM := sb_hit_reg
    io.sb_rdata_MEM := sb_rdata_reg
    io.is_ucread_MEM := is_ucread_Reg
    io.sb_st_cmt_valid_MEM := sb_st_cmt_valid_reg

}
