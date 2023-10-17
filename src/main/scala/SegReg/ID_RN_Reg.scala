import chisel3._
import chisel3.util._

// LUT: 22 FF; 408

class ID_RN_Reg extends Module {
    val io = IO(new Bundle {
        val flush           = Input(Bool())
        val stall           = Input(Bool())
        val insts_valid_ID  = Input(Vec(4, Bool()))
        val rj_ID           = Input(Vec(4, UInt(5.W)))
        val rj_valid_ID     = Input(Vec(4, Bool()))
        val rk_ID           = Input(Vec(4, UInt(5.W)))
        val rk_valid_ID     = Input(Vec(4, Bool()))
        val rd_ID           = Input(Vec(4, UInt(5.W)))
        val rd_valid_ID     = Input(Vec(4, Bool()))
        val pcs_ID          = Input(Vec(4, UInt(32.W)))
        
        val imm_ID          = Input(Vec(4, UInt(32.W)))
        val alu_op_ID       = Input(Vec(4, UInt(5.W)))
        val alu_rs1_sel_ID  = Input(Vec(4, UInt(2.W)))
        val alu_rs2_sel_ID  = Input(Vec(4, UInt(2.W)))
        val br_type_ID      = Input(Vec(4, UInt(4.W)))
        val mem_type_ID     = Input(Vec(4, UInt(5.W)))
        val fu_id_ID        = Input(Vec(4, UInt(2.W)))
        val pred_jump_ID    = Input(Vec(4, Bool()))
        val insts_exist_ID = Input(Vec(4, Bool()))

        val insts_valid_RN  = Output(Vec(4, Bool()))
        val rj_RN           = Output(Vec(4, UInt(5.W)))
        val rj_valid_RN     = Output(Vec(4, Bool()))
        val rk_RN           = Output(Vec(4, UInt(5.W)))
        val rk_valid_RN     = Output(Vec(4, Bool()))
        val rd_RN           = Output(Vec(4, UInt(5.W)))
        val rd_valid_RN     = Output(Vec(4, Bool()))

        val imm_RN          = Output(Vec(4, UInt(32.W)))
        val alu_op_RN       = Output(Vec(4, UInt(5.W)))
        val alu_rs1_sel_RN  = Output(Vec(4, UInt(2.W)))
        val alu_rs2_sel_RN  = Output(Vec(4, UInt(2.W)))
        val br_type_RN      = Output(Vec(4, UInt(4.W)))
        val mem_type_RN     = Output(Vec(4, UInt(5.W)))
        val fu_id_RN        = Output(Vec(4, UInt(2.W)))
        val pred_jump_RN    = Output(Vec(4, Bool()))
        val pcs_RN          = Output(Vec(4, UInt(32.W)))
        val insts_exist_RN = Output(Vec(4, Bool()))
    })

    val insts_valid_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val rj_reg = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val rj_valid_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val rk_reg = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val rk_valid_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val rd_reg = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val rd_valid_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    
    val imm_reg = RegInit(VecInit(Seq.fill(4)(0.U(32.W))))
    val alu_op_reg = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val alu_rs1_sel_reg = RegInit(VecInit(Seq.fill(4)(0.U(2.W))))
    val alu_rs2_sel_reg = RegInit(VecInit(Seq.fill(4)(0.U(2.W))))
    val br_type_reg = RegInit(VecInit(Seq.fill(4)(0.U(4.W))))
    val mem_type_reg = RegInit(VecInit(Seq.fill(4)(0.U(5.W))))
    val fu_id_reg = RegInit(VecInit(Seq.fill(4)(0.U(2.W))))
    val pred_jump_reg = RegInit(VecInit(Seq.fill(4)(false.B)))
    val pcs_reg = RegInit(VecInit(Seq.fill(4)(0x00000.U(32.W))))
    val insts_exist_reg = RegInit(VecInit(Seq.fill(4)(false.B)))

    when(io.flush) {
        insts_valid_reg := VecInit(Seq.fill(4)(false.B))
        rj_reg := VecInit(Seq.fill(4)(0.U(5.W)))
        rj_valid_reg := VecInit(Seq.fill(4)(false.B))
        rk_reg := VecInit(Seq.fill(4)(0.U(5.W)))
        rk_valid_reg := VecInit(Seq.fill(4)(false.B))
        rd_reg := VecInit(Seq.fill(4)(0.U(5.W)))
        rd_valid_reg := VecInit(Seq.fill(4)(false.B))
        
        imm_reg := VecInit(Seq.fill(4)(0.U(32.W)))
        alu_op_reg := VecInit(Seq.fill(4)(0.U(5.W)))
        alu_rs1_sel_reg := VecInit(Seq.fill(4)(0.U(2.W)))
        alu_rs2_sel_reg := VecInit(Seq.fill(4)(0.U(2.W)))
        br_type_reg := VecInit(Seq.fill(4)(0.U(4.W)))
        mem_type_reg := VecInit(Seq.fill(4)(0.U(5.W)))
        fu_id_reg := VecInit(Seq.fill(4)(0.U(2.W)))
        pred_jump_reg := VecInit(Seq.fill(4)(false.B))
        pcs_reg := VecInit(Seq.fill(4)(0x1c000000.U(32.W)))
        insts_exist_reg := VecInit(Seq.fill(4)(false.B))
    }
    .elsewhen(!io.stall){
        insts_valid_reg := io.insts_valid_ID
        rj_reg := io.rj_ID
        rj_valid_reg := io.rj_valid_ID
        rk_reg := io.rk_ID
        rk_valid_reg := io.rk_valid_ID
        rd_reg := io.rd_ID
        rd_valid_reg := io.rd_valid_ID
        
        imm_reg := io.imm_ID
        alu_op_reg := io.alu_op_ID
        alu_rs1_sel_reg := io.alu_rs1_sel_ID
        alu_rs2_sel_reg := io.alu_rs2_sel_ID
        br_type_reg := io.br_type_ID
        mem_type_reg := io.mem_type_ID
        fu_id_reg := io.fu_id_ID
        pred_jump_reg := io.pred_jump_ID
        pcs_reg := io.pcs_ID
        insts_exist_reg := io.insts_exist_ID
    }

    io.insts_valid_RN := insts_valid_reg
    io.rj_RN := rj_reg
    io.rj_valid_RN := rj_valid_reg
    io.rk_RN := rk_reg
    io.rk_valid_RN := rk_valid_reg
    io.rd_RN := rd_reg
    io.rd_valid_RN := rd_valid_reg
    
    io.imm_RN := imm_reg
    io.alu_op_RN := alu_op_reg
    io.alu_rs1_sel_RN := alu_rs1_sel_reg
    io.alu_rs2_sel_RN := alu_rs2_sel_reg
    io.br_type_RN := br_type_reg
    io.mem_type_RN := mem_type_reg
    io.fu_id_RN := fu_id_reg
    io.pred_jump_RN := pred_jump_reg
    io.pcs_RN := pcs_reg
    io.insts_exist_RN := insts_exist_reg


}
// object ID_RN_Reg extends App {
//     emitVerilog(new ID_RN_Reg, Array("-td", "build/"))
// }
