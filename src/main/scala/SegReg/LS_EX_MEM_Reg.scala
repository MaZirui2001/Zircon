import chisel3._
import chisel3.util._
import Inst_Pack._

class LS_EX_MEM_Reg extends Module {
    val io = IO(new Bundle {
        val flush = Input(Bool())
        val stall = Input(Bool())
        val inst_pack_EX = Input(new inst_pack_IS_LS_t)
        val sb_hit_EX   = Input(Vec(4, Bool()))
        val sb_rdata_EX = Input(UInt(32.W))
        val sb_st_cmt_valid_EX = Input(Bool())
        val is_ucread_EX   = Input(Bool())
        val mem_type_EX = Input(UInt(5.W))

        val inst_pack_MEM = Output(new inst_pack_IS_LS_t)

        val sb_hit_MEM   = Output(Vec(4, Bool()))
        val sb_rdata_MEM = Output(UInt(32.W))
        val sb_st_cmt_valid_MEM = Output(Bool())
        val is_ucread_MEM = Output(Bool())
        val mem_type_MEM = Output(UInt(5.W))
    })

    val inst_pack_reg = RegInit(0.U.asTypeOf(new inst_pack_IS_LS_t))

    val sb_hit_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val sb_rdata_reg = RegInit(0.U(32.W))
    val sb_st_cmt_valid_reg = RegInit(false.B)
    val is_ucread_Reg = RegInit(false.B)
    val mem_type_reg = RegInit(0.U(5.W))


    when(io.flush) {
        inst_pack_reg := 0.U.asTypeOf(new inst_pack_IS_LS_t)

        sb_hit_reg := VecInit(Seq.fill(4)(false.B))
        sb_rdata_reg := 0.U
        sb_st_cmt_valid_reg := false.B
        is_ucread_Reg := false.B
        mem_type_reg := 0.U
        
    }.elsewhen(!io.stall) {
        inst_pack_reg := io.inst_pack_EX

        sb_hit_reg := io.sb_hit_EX
        sb_rdata_reg := io.sb_rdata_EX
        sb_st_cmt_valid_reg := io.sb_st_cmt_valid_EX
        is_ucread_Reg := io.is_ucread_EX
        mem_type_reg := io.mem_type_EX
    }

    io.inst_pack_MEM := inst_pack_reg
    io.sb_hit_MEM := sb_hit_reg
    io.sb_rdata_MEM := sb_rdata_reg
    io.is_ucread_MEM := is_ucread_Reg
    io.sb_st_cmt_valid_MEM := sb_st_cmt_valid_reg
    io.mem_type_MEM := mem_type_reg

}
