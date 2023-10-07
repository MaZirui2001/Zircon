import chisel3._
import chisel3.util._
import Inst_Pack._
import chisel3.aop.Select

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
    id_rn_reg.io.insts_exist_ID := VecInit(inst_decode1.io.inst_exist, inst_decode2.io.inst_exist, inst_decode3.io.inst_exist, inst_decode4.io.inst_exist)

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
    
    val insts_pack_rn = Wire(Vec(4, new inst_pack_t))
    for (i <- 0 until 4){
        insts_pack_rn(i).rj             := id_rn_reg.io.rj_RN(i)
        insts_pack_rn(i).rj_valid       := id_rn_reg.io.rj_valid_RN(i)
        insts_pack_rn(i).prj            := reg_rename.io.prj(i)
        insts_pack_rn(i).rk             := id_rn_reg.io.rk_RN(i)
        insts_pack_rn(i).rk_valid       := id_rn_reg.io.rk_valid_RN(i)
        insts_pack_rn(i).prk            := reg_rename.io.prk(i)
        insts_pack_rn(i).rd             := id_rn_reg.io.rd_RN(i)
        insts_pack_rn(i).rd_valid       := id_rn_reg.io.rd_valid_RN(i)
        insts_pack_rn(i).prd            := reg_rename.io.prd(i)
        insts_pack_rn(i).imm            := id_rn_reg.io.imm_RN(i)
        insts_pack_rn(i).alu_op         := id_rn_reg.io.alu_op_RN(i)
        insts_pack_rn(i).alu_rs1_sel    := id_rn_reg.io.alu_rs1_sel_RN(i)
        insts_pack_rn(i).alu_rs2_sel    := id_rn_reg.io.alu_rs2_sel_RN(i)
        insts_pack_rn(i).br_type        := id_rn_reg.io.br_type_RN(i)
        insts_pack_rn(i).mem_type       := id_rn_reg.io.mem_type_RN(i)
        insts_pack_rn(i).fu_id          := id_rn_reg.io.fu_id_RN(i)
        insts_pack_rn(i).pc             := id_rn_reg.io.pcs_RN(i)
        // insts_pack_rn(i).rob_index      := 
        insts_pack_rn(i).inst_exist     := id_rn_reg.io.insts_exist_RN(i)
    }
    // RN-DP SegReg
    val rn_dp_reg = Module(new RN_DP_Reg)
    // rn_dp_reg.io.flush := false.B
    // rn_dp_reg.io.stall := false.B
    rn_dp_reg.io.insts_pack_RN := insts_pack_rn
    rn_dp_reg.io.insts_valid_RN := id_rn_reg.io.insts_valid_RN
    // rn_dp_reg.io.robs_index_RN := VecInit(Seq.fill(4)(0.U))
    rn_dp_reg.io.prj_raw_RN := reg_rename.io.prj_raw
    rn_dp_reg.io.prk_raw_RN := reg_rename.io.prk_raw

    // DP stage
    val dp = Module(new Dispatch)
    dp.io.inst_packs := rn_dp_reg.io.insts_pack_DP
    dp.io.insts_valid := rn_dp_reg.io.insts_valid_DP
    dp.io.prj_raw := rn_dp_reg.io.prj_raw_DP
    dp.io.prk_raw := rn_dp_reg.io.prk_raw_DP

    // dp.io.prd_queue := reg_rename.io.prd
    // dp.io.elem_num := 0.U

    // issue stage
    // 1. arith1, common calculate
    val iq1 = Module(new Unorder_Issue_Queue(8))
    val sel1 = Module(new Unorder_Select(8))
    iq1.io.insts_dispatch := dp.io.insts_dispatch(0)
    iq1.io.insert_num := dp.io.insert_num(0)
    iq1.io.prj_ready := dp.io.prj_ready(0)
    iq1.io.prk_ready := dp.io.prk_ready(0)
    iq1.io.issue_ack := sel1.io.issue_ack

    
    sel1.io.insts_issue := iq1.io.insts_issue
    sel1.io.issue_req := iq1.io.issue_req
    // sel1.io.stall := false.B
    

    // 2. arith2, calculate and branch 
    val iq2 = Module(new Unorder_Issue_Queue(8))
    val sel2 = Module(new Unorder_Select(8))
    iq2.io.insts_dispatch := dp.io.insts_dispatch(1)
    iq2.io.insert_num := dp.io.insert_num(1)
    iq2.io.prj_ready := dp.io.prj_ready(1)
    iq2.io.prk_ready := dp.io.prk_ready(1)
    iq2.io.issue_ack := sel2.io.issue_ack

    sel2.io.insts_issue := iq2.io.insts_issue
    sel2.io.issue_req := iq2.io.issue_req
    // sel2.io.stall := false.B

    // 3. load, load and store
    val iq3 = Module(new Order_Issue_Queue(8))
    val sel3 = Module(new Order_Select(8))
    iq3.io.insts_dispatch := dp.io.insts_dispatch(2)
    iq3.io.insert_num := dp.io.insert_num(2)
    iq3.io.prj_ready := dp.io.prj_ready(2)
    iq3.io.prk_ready := dp.io.prk_ready(2)
    iq3.io.issue_ack := sel3.io.issue_ack

    sel3.io.insts_issue := iq3.io.insts_issue
    sel3.io.issue_req := iq3.io.issue_req
    // sel3.io.stall := false.B


    // 4. multiply, multiply and divide
    val iq4 = Module(new Order_Issue_Queue(8))
    val sel4 = Module(new Order_Select(8))
    iq4.io.insts_dispatch := dp.io.insts_dispatch(3)
    iq4.io.insert_num := dp.io.insert_num(3)
    iq4.io.prj_ready := dp.io.prj_ready(3)
    iq4.io.prk_ready := dp.io.prk_ready(3)
    iq4.io.issue_ack := sel4.io.issue_ack

    sel4.io.insts_issue := iq4.io.insts_issue
    sel4.io.issue_req := iq4.io.issue_req
    // sel4.io.stall := false.B

    // mutual wakeup
    iq1.io.wake_preg := VecInit(sel1.io.wake_preg, RegNext(sel2.io.wake_preg), RegNext(sel3.io.wake_preg), RegNext(sel4.io.wake_preg))
    iq2.io.wake_preg := VecInit(RegNext(sel1.io.wake_preg), sel2.io.wake_preg, RegNext(sel3.io.wake_preg), RegNext(sel4.io.wake_preg))
    iq3.io.wake_preg := VecInit(RegNext(sel1.io.wake_preg), RegNext(sel2.io.wake_preg), sel3.io.wake_preg, RegNext(sel4.io.wake_preg))
    iq4.io.wake_preg := VecInit(RegNext(sel1.io.wake_preg), RegNext(sel2.io.wake_preg), RegNext(sel3.io.wake_preg), sel4.io.wake_preg)

    // IS-EX SegReg
    val is_ex_reg1 = Module(new IS_EX_Reg)
    // is_ex_reg1.io.flush := false.B
    // is_ex_reg1.io.stall := false.B
    is_ex_reg1.io.inst_pack_IS := sel1.io.inst_issue
    is_ex_reg1.io.inst_valid_IS := sel1.io.inst_issue_valid

    val is_ex_reg2 = Module(new IS_EX_Reg)
    // is_ex_reg2.io.flush := false.B
    // is_ex_reg2.io.stall := false.B
    is_ex_reg2.io.inst_pack_IS := sel2.io.inst_issue
    is_ex_reg2.io.inst_valid_IS := sel2.io.inst_issue_valid

    val is_ex_reg3 = Module(new IS_EX_Reg)
    // is_ex_reg3.io.flush := false.B
    // is_ex_reg3.io.stall := false.B
    is_ex_reg3.io.inst_pack_IS := sel3.io.inst_issue
    is_ex_reg3.io.inst_valid_IS := sel3.io.inst_issue_valid

    val is_ex_reg4 = Module(new IS_EX_Reg)
    // is_ex_reg4.io.flush := false.B
    // is_ex_reg4.io.stall := false.B
    is_ex_reg4.io.inst_pack_IS := sel4.io.inst_issue
    is_ex_reg4.io.inst_valid_IS := sel4.io.inst_issue_valid

    // EX stage
    // 1. arith1, common calculate
    
}