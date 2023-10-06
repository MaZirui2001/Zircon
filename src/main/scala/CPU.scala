import chisel3._
import chisel3.util._

class CPU_IO extends Bundle{
    val pc_IF = Output(UInt(32.W))
    val inst1_IF = Input(UInt(32.W))
    val inst2_IF = Input(UInt(32.W))
    val inst3_IF = Input(UInt(32.W))
    val inst4_IF = Input(UInt(32.W))

    val mem_addr_ex = Output(UInt(32.W))
    val mem_rdata_ex = Input(UInt(32.W))
    val mem_we_ex = Output(Bool())
    val mem_wdata_ex = Output(UInt(32.W))
}
class CPU extends Module {
    val io = IO(new CPU_IO)
    // IF stage
    val pc = Module(new PC(0x1c000000))

    io.pc_IF := pc.io.pc_IF
    // io.pc_stall = false
    // io.pc_predict_fail = false
    // io.pc_branch_target = 0.U

    // IF-FQ SegReg
    val if_fq_reg = Module(new IF_FQ_Reg)
    // if_fq_reg.io.flush := false.B
    // if_fq_reg.io.stall := false.B
    if_fq_reg.io.pcs_IF := VecInit(pc.io.pc_IF, pc.io.pc_IF+4.U, pc.io.pc_IF+8.U, pc.io.pc_IF+12.U)
    if_fq_reg.io.insts_valid_IF := VecInit(true.B, true.B, true.B, true.B)
    if_fq_reg.io.insts_IF := VecInit(io.inst1_IF, io.inst2_IF, io.inst3_IF, io.inst4_IF)

    // Fetch_Queue stage && FQ-ID SegReg
    val inst_queue = Module(new Fetch_Queue)
    inst_queue.io.insts := if_fq_reg.io.insts_FQ
    inst_queue.io.insts_valid := if_fq_reg.io.insts_valid_FQ
    inst_queue.io.pcs_FQ := if_fq_reg.io.pcs_FQ
    // inst_queue.io.next_ready := VecInit(true.B, true.B, true.B, true.B)
    // inst_queue.io.flush := false.B

    // ID stage
    val inst_decode1 = Module(new Decode)
    val inst_decode2 = Module(new Decode)
    val inst_decode3 = Module(new Decode)
    val inst_decode4 = Module(new Decode)

    inst_decode1.io.inst := inst_queue.io.insts_decode(0)
    inst_decode2.io.inst := inst_queue.io.insts_decode(1)
    inst_decode3.io.inst := inst_queue.io.insts_decode(2)
    inst_decode4.io.inst := inst_queue.io.insts_decode(3)

    // ID-RN SegReg
    val id_rn_reg = Module(new ID_RN_Reg)
    // id_rn_reg.io.flush := false.B
    // id_rn_reg.io.stall := false.B
    id_rn_reg.io.insts_valid_ID := inst_queue.io.insts_valid_decode
    id_rn_reg.io.rj_ID := VecInit(inst_decode1.io.rj, inst_decode2.io.rj, inst_decode3.io.rj, inst_decode4.io.rj)
    id_rn_reg.io.rj_valid_ID := VecInit(inst_decode1.io.rj_valid, inst_decode2.io.rj_valid, inst_decode3.io.rj_valid, inst_decode4.io.rj_valid)
    id_rn_reg.io.rk_ID := VecInit(inst_decode1.io.rk, inst_decode2.io.rk, inst_decode3.io.rk, inst_decode4.io.rk)
    id_rn_reg.io.rk_valid_ID := VecInit(inst_decode1.io.rk_valid, inst_decode2.io.rk_valid, inst_decode3.io.rk_valid, inst_decode4.io.rk_valid)
    id_rn_reg.io.rd_ID := VecInit(inst_decode1.io.rd, inst_decode2.io.rd, inst_decode3.io.rd, inst_decode4.io.rd)
    id_rn_reg.io.rd_valid_ID := VecInit(inst_decode1.io.rd_valid, inst_decode2.io.rd_valid, inst_decode3.io.rd_valid, inst_decode4.io.rd_valid)
    id_rn_reg.io.imm_ID := VecInit(inst_decode1.io.imm, inst_decode2.io.imm, inst_decode3.io.imm, inst_decode4.io.imm)
    id_rn_reg.io.alu_op_ID := VecInit(inst_decode1.io.alu_op, inst_decode2.io.alu_op, inst_decode3.io.alu_op, inst_decode4.io.alu_op)
    id_rn_reg.io.alu_rs1_sel_ID := VecInit(inst_decode1.io.alu_rs1_sel, inst_decode2.io.alu_rs1_sel, inst_decode3.io.alu_rs1_sel, inst_decode4.io.alu_rs1_sel)
    id_rn_reg.io.alu_rs2_sel_ID := VecInit(inst_decode1.io.alu_rs2_sel, inst_decode2.io.alu_rs2_sel, inst_decode3.io.alu_rs2_sel, inst_decode4.io.alu_rs2_sel)
    id_rn_reg.io.br_type_ID := VecInit(inst_decode1.io.br_type, inst_decode2.io.br_type, inst_decode3.io.br_type, inst_decode4.io.br_type)
    id_rn_reg.io.mem_type_ID := VecInit(inst_decode1.io.mem_type, inst_decode2.io.mem_type, inst_decode3.io.mem_type, inst_decode4.io.mem_type)
    id_rn_reg.io.fu_id_ID := VecInit(inst_decode1.io.fu_id, inst_decode2.io.fu_id, inst_decode3.io.fu_id, inst_decode4.io.fu_id)
    id_rn_reg.io.pcs_ID := inst_queue.io.pcs_ID

    // Reg Rename
    val reg_rename = Module(new Reg_Rename)
    reg_rename.io.rj := id_rn_reg.io.rj_RN
    reg_rename.io.rk := id_rn_reg.io.rk_RN
    reg_rename.io.rd := id_rn_reg.io.rd_RN
    reg_rename.io.rd_valid := id_rn_reg.io.rd_valid_RN
    reg_rename.io.rename_en := id_rn_reg.io.insts_valid_RN
    // reg_rename.io.commit_en := VecInit(false.B, false.B, false.B, false.B)
    // reg_rename.io.commit_pprd_valid := VecInit(false.B, false.B, false.B, false.B)
    // reg_rename.io.commit_pprd := VecInit(0.U, 0.U, 0.U, 0.U)
    // reg_rename.io.predict_fail := false.B
    // reg_rename.io.arch_rat := VecInit(Seq.fill(64)(0.U))
    // reg_rename.io.head_arch := VecInit(Seq.fill(4)(0.U))
    




}