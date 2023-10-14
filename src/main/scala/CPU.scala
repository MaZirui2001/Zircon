import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._

class CPU_IO extends Bundle{
    val pc_IF               = Output(UInt(32.W))
    val inst1_IF            = Input(UInt(32.W))
    val inst2_IF            = Input(UInt(32.W))
    val inst3_IF            = Input(UInt(32.W))
    val inst4_IF            = Input(UInt(32.W))

    val mem_addr_ex         = Output(UInt(32.W))
    val mem_rdata_ex        = Input(UInt(32.W))
    val mem_type_ex         = Output(UInt(5.W))
    val mem_wdata_ex        = Output(UInt(32.W))

    val commit_en1          = Output(Bool())
    val commit_rd1          = Output(UInt(5.W))
    val commit_prd1         = Output(UInt(6.W))
    val commit_rd_valid1    = Output(Bool())
    val commit_rf_wdata1    = Output(UInt(32.W))
    val commit_pc_1         = Output(UInt(32.W))
    val commit_en2          = Output(Bool())
    val commit_rd2          = Output(UInt(5.W))
    val commit_prd2         = Output(UInt(6.W))
    val commit_rd_valid2    = Output(Bool())
    val commit_rf_wdata2    = Output(UInt(32.W))
    val commit_pc_2         = Output(UInt(32.W))
    val commit_en3          = Output(Bool())
    val commit_rd3          = Output(UInt(5.W))
    val commit_prd3         = Output(UInt(6.W))
    val commit_rd_valid3    = Output(Bool())
    val commit_rf_wdata3    = Output(UInt(32.W))
    val commit_pc_3         = Output(UInt(32.W))
    val commit_en4          = Output(Bool())
    val commit_rd4          = Output(UInt(5.W))
    val commit_prd4         = Output(UInt(6.W))
    val commit_rd_valid4    = Output(Bool())
    val commit_rf_wdata4    = Output(UInt(32.W))
    val commit_pc_4         = Output(UInt(32.W))

}
class CPU(RESET_VEC: Int) extends Module {
    val io = IO(new CPU_IO)

    val pc              = Module(new PC(RESET_VEC))
    val if_fq_reg       = Module(new IF_FQ_Reg)
    val inst_decode1    = Module(new Decode)
    val inst_decode2    = Module(new Decode)
    val inst_decode3    = Module(new Decode)
    val inst_decode4    = Module(new Decode)
    val id_rn_reg       = Module(new ID_RN_Reg)
    val reg_rename      = Module(new Reg_Rename)
    val rn_dp_reg       = Module(new RN_DP_Reg)
    
    val dp              = Module(new Dispatch)
    val inst_queue      = Module(new Fetch_Queue)
    val iq1             = Module(new Unorder_Issue_Queue(8))
    val sel1            = Module(new Unorder_Select(8))
    val iq2             = Module(new Unorder_Issue_Queue(8))
    val sel2            = Module(new Unorder_Select(8))
    val iq3             = Module(new Order_Issue_Queue(8))
    val sel3            = Module(new Order_Select(8))
    val iq4             = Module(new Order_Issue_Queue(8))
    val sel4            = Module(new Order_Select(8))

    val is_rf_reg1      = Module(new IS_RF_Reg)
    val is_rf_reg2      = Module(new IS_RF_Reg)
    val is_rf_reg3      = Module(new IS_RF_Reg)
    val is_rf_reg4      = Module(new IS_RF_Reg)

    val rf              = Module(new Physical_Regfile)

    val rf_ex_reg1      = Module(new RF_EX_Reg)
    val rf_ex_reg2      = Module(new RF_EX_Reg)
    val rf_ex_reg3      = Module(new RF_EX_Reg)
    val rf_ex_reg4      = Module(new RF_EX_Reg)

    val alu1            = Module(new ALU)
    val fu1_bypass      = Module(new Bypass)

    val alu2            = Module(new ALU)
    val br              = Module(new Branch)
    val fu2_bypass      = Module(new Bypass)

    val fu3_bypass      = Module(new Bypass)
    val ls_ex1_ex2_reg  = Module(new LS_EX1_EX2_Reg)

    val mdu             = Module(new MDU)
    val fu4_bypass      = Module(new Bypass)

    val fu1_ex_wb_reg   = Module(new FU1_EX_WB_Reg)
    val fu2_ex_wb_reg   = Module(new FU2_EX_WB_Reg)
    val fu3_ex_wb_reg   = Module(new LS_EX2_WB_Reg)
    val fu4_ex_wb_reg   = Module(new MD_EX_WB_Reg)

    val rob             = Module(new ROB(32))
    val arat            = Module(new Arch_Rat)

    val stall_by_iq = iq1.io.full || iq2.io.full || iq3.io.full || iq4.io.full

    // IF stage
    io.pc_IF            := pc.io.pc_IF
    pc.io.pc_stall      := !inst_queue.io.inst_queue_ready 
    pc.io.predict_fail  := rob.io.predict_fail_cmt
    pc.io.branch_target := rob.io.branch_target_cmt

    // IF-FQ SegReg
    if_fq_reg.io.flush          := rob.io.predict_fail_cmt
    if_fq_reg.io.stall          := !inst_queue.io.inst_queue_ready 
    if_fq_reg.io.pcs_IF         := VecInit(pc.io.pc_IF, pc.io.pc_IF+4.U, pc.io.pc_IF+8.U, pc.io.pc_IF+12.U)
    if_fq_reg.io.insts_valid_IF := VecInit(true.B, true.B, true.B, true.B)
    if_fq_reg.io.insts_IF       := VecInit(io.inst1_IF, io.inst2_IF, io.inst3_IF, io.inst4_IF)

    // Fetch_Queue stage && FQ-ID SegReg
    inst_queue.io.insts         := if_fq_reg.io.insts_FQ
    inst_queue.io.insts_valid   := if_fq_reg.io.insts_valid_FQ
    inst_queue.io.pcs_FQ        := if_fq_reg.io.pcs_FQ
    inst_queue.io.next_ready    := !(rob.io.full || stall_by_iq || reg_rename.io.free_list_empty)
    inst_queue.io.flush         := rob.io.predict_fail_cmt

    // ID stage
    inst_decode1.io.inst        := inst_queue.io.insts_decode(0)
    inst_decode2.io.inst        := inst_queue.io.insts_decode(1)
    inst_decode3.io.inst        := inst_queue.io.insts_decode(2)
    inst_decode4.io.inst        := inst_queue.io.insts_decode(3)

    // ID-RN SegReg
    id_rn_reg.io.flush          := rob.io.predict_fail_cmt
    id_rn_reg.io.stall          := rob.io.full || reg_rename.io.free_list_empty || stall_by_iq
    id_rn_reg.io.insts_valid_ID := inst_queue.io.insts_valid_decode
    id_rn_reg.io.rj_ID          := VecInit(inst_decode1.io.rj, inst_decode2.io.rj, inst_decode3.io.rj, inst_decode4.io.rj)
    id_rn_reg.io.rj_valid_ID    := VecInit(inst_decode1.io.rj_valid, inst_decode2.io.rj_valid, inst_decode3.io.rj_valid, inst_decode4.io.rj_valid)
    id_rn_reg.io.rk_ID          := VecInit(inst_decode1.io.rk, inst_decode2.io.rk, inst_decode3.io.rk, inst_decode4.io.rk)
    id_rn_reg.io.rk_valid_ID    := VecInit(inst_decode1.io.rk_valid, inst_decode2.io.rk_valid, inst_decode3.io.rk_valid, inst_decode4.io.rk_valid)
    id_rn_reg.io.rd_ID          := VecInit(inst_decode1.io.rd, inst_decode2.io.rd, inst_decode3.io.rd, inst_decode4.io.rd)
    id_rn_reg.io.rd_valid_ID    := VecInit(inst_decode1.io.rd_valid, inst_decode2.io.rd_valid, inst_decode3.io.rd_valid, inst_decode4.io.rd_valid)
    id_rn_reg.io.imm_ID         := VecInit(inst_decode1.io.imm, inst_decode2.io.imm, inst_decode3.io.imm, inst_decode4.io.imm)
    id_rn_reg.io.alu_op_ID      := VecInit(inst_decode1.io.alu_op, inst_decode2.io.alu_op, inst_decode3.io.alu_op, inst_decode4.io.alu_op)
    id_rn_reg.io.alu_rs1_sel_ID := VecInit(inst_decode1.io.alu_rs1_sel, inst_decode2.io.alu_rs1_sel, inst_decode3.io.alu_rs1_sel, inst_decode4.io.alu_rs1_sel)
    id_rn_reg.io.alu_rs2_sel_ID := VecInit(inst_decode1.io.alu_rs2_sel, inst_decode2.io.alu_rs2_sel, inst_decode3.io.alu_rs2_sel, inst_decode4.io.alu_rs2_sel)
    id_rn_reg.io.br_type_ID     := VecInit(inst_decode1.io.br_type, inst_decode2.io.br_type, inst_decode3.io.br_type, inst_decode4.io.br_type)
    id_rn_reg.io.mem_type_ID    := VecInit(inst_decode1.io.mem_type, inst_decode2.io.mem_type, inst_decode3.io.mem_type, inst_decode4.io.mem_type)
    id_rn_reg.io.fu_id_ID       := VecInit(inst_decode1.io.fu_id, inst_decode2.io.fu_id, inst_decode3.io.fu_id, inst_decode4.io.fu_id)
    id_rn_reg.io.pcs_ID         := inst_queue.io.pcs_ID
    id_rn_reg.io.insts_exist_ID := VecInit(inst_decode1.io.inst_exist, inst_decode2.io.inst_exist, inst_decode3.io.inst_exist, inst_decode4.io.inst_exist)

    // Reg Rename
    reg_rename.io.rj                := id_rn_reg.io.rj_RN
    reg_rename.io.rk                := id_rn_reg.io.rk_RN
    reg_rename.io.rd                := id_rn_reg.io.rd_RN
    reg_rename.io.rd_valid          := id_rn_reg.io.rd_valid_RN
    reg_rename.io.rename_en         := (id_rn_reg.io.insts_valid_RN.asUInt & VecInit(Seq.fill(4)(!id_rn_reg.io.stall)).asUInt).asBools
    reg_rename.io.commit_en         := rob.io.cmt_en
    reg_rename.io.commit_pprd_valid := rob.io.rd_valid_cmt
    reg_rename.io.commit_pprd       := rob.io.pprd_cmt
    reg_rename.io.predict_fail      := rob.io.predict_fail_cmt
    reg_rename.io.arch_rat          := arat.io.arch_rat
    reg_rename.io.head_arch         := arat.io.head_arch
    
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
        insts_pack_rn(i).pprd           := reg_rename.io.pprd(i)
        insts_pack_rn(i).imm            := id_rn_reg.io.imm_RN(i)
        insts_pack_rn(i).alu_op         := id_rn_reg.io.alu_op_RN(i)
        insts_pack_rn(i).alu_rs1_sel    := id_rn_reg.io.alu_rs1_sel_RN(i)
        insts_pack_rn(i).alu_rs2_sel    := id_rn_reg.io.alu_rs2_sel_RN(i)
        insts_pack_rn(i).br_type        := id_rn_reg.io.br_type_RN(i)
        insts_pack_rn(i).mem_type       := id_rn_reg.io.mem_type_RN(i)
        insts_pack_rn(i).fu_id          := id_rn_reg.io.fu_id_RN(i)
        insts_pack_rn(i).pc             := id_rn_reg.io.pcs_RN(i)
        insts_pack_rn(i).rob_index      := rob.io.rob_index_rn(i)
        insts_pack_rn(i).inst_exist     := id_rn_reg.io.insts_exist_RN(i)
    }
    // RN-DP SegReg
    rn_dp_reg.io.flush          := (rob.io.full || reg_rename.io.free_list_empty) && !(stall_by_iq)
    rn_dp_reg.io.stall          := stall_by_iq 
    rn_dp_reg.io.insts_pack_RN  := insts_pack_rn
    rn_dp_reg.io.insts_valid_RN := id_rn_reg.io.insts_valid_RN
    rn_dp_reg.io.prj_raw_RN     := reg_rename.io.prj_raw
    rn_dp_reg.io.prk_raw_RN     := reg_rename.io.prk_raw

    // DP stage
    dp.io.inst_packs            := rn_dp_reg.io.insts_pack_DP
    dp.io.insts_valid           := rn_dp_reg.io.insts_valid_DP
    dp.io.prj_raw               := rn_dp_reg.io.prj_raw_DP
    dp.io.prk_raw               := rn_dp_reg.io.prk_raw_DP
    dp.io.prd_queue             := VecInit(iq1.io.prd_queue, iq2.io.prd_queue, iq3.io.prd_queue, iq4.io.prd_queue)
    dp.io.elem_num              := VecInit(iq1.io.elem_num, iq2.io.elem_num)

    // issue stage
    // 1. arith1, common calculate
    iq1.io.insts_dispatch       := dp.io.insts_dispatch(0)
    iq1.io.insert_num           := dp.io.insert_num(0)
    iq1.io.prj_ready            := dp.io.prj_ready(0)
    iq1.io.prk_ready            := dp.io.prk_ready(0)
    iq1.io.issue_ack            := sel1.io.issue_ack
    iq1.io.flush                := rob.io.predict_fail_cmt
    
    sel1.io.insts_issue         := iq1.io.insts_issue
    sel1.io.issue_req           := iq1.io.issue_req
    sel1.io.stall               := !(iq1.io.issue_req.asUInt.orR)

    // 2. arith2, calculate and branch 
    iq2.io.insts_dispatch       := dp.io.insts_dispatch(1)
    iq2.io.insert_num           := dp.io.insert_num(1)
    iq2.io.prj_ready            := dp.io.prj_ready(1)
    iq2.io.prk_ready            := dp.io.prk_ready(1)
    iq2.io.issue_ack            := sel2.io.issue_ack
    iq2.io.flush                := rob.io.predict_fail_cmt

    sel2.io.insts_issue         := iq2.io.insts_issue
    sel2.io.issue_req           := iq2.io.issue_req
    sel2.io.stall               := !(iq2.io.issue_req.asUInt.orR)

    // 3. load, load and store
    iq3.io.insts_dispatch       := dp.io.insts_dispatch(2)
    iq3.io.insert_num           := dp.io.insert_num(2)
    iq3.io.prj_ready            := dp.io.prj_ready(2)
    iq3.io.prk_ready            := dp.io.prk_ready(2)
    iq3.io.issue_ack            := sel3.io.issue_ack
    iq3.io.flush                := rob.io.predict_fail_cmt

    sel3.io.insts_issue         := iq3.io.insts_issue
    sel3.io.issue_req           := iq3.io.issue_req
    sel3.io.stall               := !(iq3.io.issue_req)

    // 4. multiply, multiply and divide
    iq4.io.insts_dispatch       := dp.io.insts_dispatch(3)
    iq4.io.insert_num           := dp.io.insert_num(3)
    iq4.io.prj_ready            := dp.io.prj_ready(3)
    iq4.io.prk_ready            := dp.io.prk_ready(3)
    iq4.io.issue_ack            := sel4.io.issue_ack
    iq4.io.flush                := rob.io.predict_fail_cmt

    sel4.io.insts_issue         := iq4.io.insts_issue
    sel4.io.issue_req           := iq4.io.issue_req
    sel4.io.stall               := !(iq4.io.issue_req)

    // mutual wakeup
    iq1.io.wake_preg            := VecInit(sel1.io.wake_preg, ShiftRegister(sel2.io.wake_preg, 1, 0.U, true.B), ShiftRegister(sel3.io.wake_preg, 2, 0.U, true.B), ShiftRegister(sel4.io.wake_preg, 1, 0.U, true.B))
    iq2.io.wake_preg            := VecInit(ShiftRegister(sel1.io.wake_preg, 1, 0.U, true.B), sel2.io.wake_preg, ShiftRegister(sel3.io.wake_preg, 2, 0.U, true.B), ShiftRegister(sel4.io.wake_preg, 1, 0.U, true.B))
    iq3.io.wake_preg            := VecInit(ShiftRegister(sel1.io.wake_preg, 1, 0.U, true.B), ShiftRegister(sel2.io.wake_preg, 1, 0.U, true.B), ShiftRegister(sel3.io.wake_preg, 1, 0.U, true.B), ShiftRegister(sel4.io.wake_preg, 1, 0.U, true.B))
    iq4.io.wake_preg            := VecInit(ShiftRegister(sel1.io.wake_preg, 1, 0.U, true.B), ShiftRegister(sel2.io.wake_preg, 1, 0.U, true.B), ShiftRegister(sel3.io.wake_preg, 2, 0.U, true.B), sel4.io.wake_preg)

    // IS-EX SegReg
    is_rf_reg1.io.flush         := rob.io.predict_fail_cmt
    is_rf_reg1.io.stall         := false.B
    is_rf_reg1.io.inst_pack_IS  := sel1.io.inst_issue.inst
    is_rf_reg1.io.inst_valid_IS := sel1.io.inst_issue_valid

    is_rf_reg2.io.flush         := rob.io.predict_fail_cmt
    is_rf_reg2.io.stall         := false.B
    is_rf_reg2.io.inst_pack_IS  := sel2.io.inst_issue.inst
    is_rf_reg2.io.inst_valid_IS := sel2.io.inst_issue_valid

    is_rf_reg3.io.flush         := rob.io.predict_fail_cmt
    is_rf_reg3.io.stall         := false.B
    is_rf_reg3.io.inst_pack_IS  := sel3.io.inst_issue.inst
    is_rf_reg3.io.inst_valid_IS := sel3.io.inst_issue_valid

    is_rf_reg4.io.flush         := rob.io.predict_fail_cmt
    is_rf_reg4.io.stall         := false.B
    is_rf_reg4.io.inst_pack_IS  := sel4.io.inst_issue.inst
    is_rf_reg4.io.inst_valid_IS := sel4.io.inst_issue_valid

    // RF stage
    rf.io.prj       := VecInit(is_rf_reg1.io.inst_pack_RF.prj, is_rf_reg2.io.inst_pack_RF.prj, is_rf_reg3.io.inst_pack_RF.prj, is_rf_reg4.io.inst_pack_RF.prj)
    rf.io.prk       := VecInit(is_rf_reg1.io.inst_pack_RF.prk, is_rf_reg2.io.inst_pack_RF.prk, is_rf_reg3.io.inst_pack_RF.prk, is_rf_reg4.io.inst_pack_RF.prk)
    rf.io.wdata     := VecInit(fu1_ex_wb_reg.io.alu_out_WB, fu2_ex_wb_reg.io.alu_out_WB, fu3_ex_wb_reg.io.mem_rdata_WB, fu4_ex_wb_reg.io.md_out_WB)
    rf.io.rf_we     := VecInit(fu1_ex_wb_reg.io.inst_pack_WB.rd_valid, fu2_ex_wb_reg.io.inst_pack_WB.rd_valid, fu3_ex_wb_reg.io.inst_pack_WB.rd_valid, fu4_ex_wb_reg.io.inst_pack_WB.rd_valid)
    rf.io.prd       := VecInit(fu1_ex_wb_reg.io.inst_pack_WB.prd, fu2_ex_wb_reg.io.inst_pack_WB.prd, fu3_ex_wb_reg.io.inst_pack_WB.prd, fu4_ex_wb_reg.io.inst_pack_WB.prd)

    // RF-EX SegReg
    rf_ex_reg1.io.flush         := rob.io.predict_fail_cmt
    rf_ex_reg1.io.stall         := false.B
    rf_ex_reg1.io.inst_pack_RF  := is_rf_reg1.io.inst_pack_RF
    rf_ex_reg1.io.inst_valid_RF := is_rf_reg1.io.inst_valid_RF
    rf_ex_reg1.io.src1_RF       := rf.io.prj_data(0)
    rf_ex_reg1.io.src2_RF       := rf.io.prk_data(0)

    rf_ex_reg2.io.flush         := rob.io.predict_fail_cmt
    rf_ex_reg2.io.stall         := false.B
    rf_ex_reg2.io.inst_pack_RF  := is_rf_reg2.io.inst_pack_RF
    rf_ex_reg2.io.inst_valid_RF := is_rf_reg2.io.inst_valid_RF
    rf_ex_reg2.io.src1_RF       := rf.io.prj_data(1)
    rf_ex_reg2.io.src2_RF       := rf.io.prk_data(1)

    rf_ex_reg3.io.flush         := rob.io.predict_fail_cmt
    rf_ex_reg3.io.stall         := false.B
    rf_ex_reg3.io.inst_pack_RF  := is_rf_reg3.io.inst_pack_RF
    rf_ex_reg3.io.inst_valid_RF := is_rf_reg3.io.inst_valid_RF
    rf_ex_reg3.io.src1_RF       := rf.io.prj_data(2)
    rf_ex_reg3.io.src2_RF       := rf.io.prk_data(2)

    rf_ex_reg4.io.flush         := rob.io.predict_fail_cmt
    rf_ex_reg4.io.stall         := false.B
    rf_ex_reg4.io.inst_pack_RF  := is_rf_reg4.io.inst_pack_RF
    rf_ex_reg4.io.inst_valid_RF := is_rf_reg4.io.inst_valid_RF
    rf_ex_reg4.io.src1_RF       := rf.io.prj_data(3)
    rf_ex_reg4.io.src2_RF       := rf.io.prk_data(3)

    // EX stage
    // 1. arith common fu
    alu1.io.alu_op := rf_ex_reg1.io.inst_pack_EX.alu_op
    alu1.io.src1 := MuxLookup(rf_ex_reg1.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG -> Mux(fu1_bypass.io.forward_prj_en, fu1_bypass.io.forward_prj_data, rf_ex_reg1.io.src1_EX),
        RS1_PC -> rf_ex_reg1.io.inst_pack_EX.pc,
        RS1_ZERO -> 0.U)
    )
    alu1.io.src2 := MuxLookup(rf_ex_reg1.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG -> Mux(fu1_bypass.io.forward_prk_en, fu1_bypass.io.forward_prk_data, rf_ex_reg1.io.src2_EX),
        RS2_IMM -> rf_ex_reg1.io.inst_pack_EX.imm,
        RS2_FOUR -> 4.U)
    )

    fu1_bypass.io.prd_wb        := fu1_ex_wb_reg.io.inst_pack_WB.prd
    fu1_bypass.io.prj_ex        := rf_ex_reg1.io.inst_pack_EX.prj
    fu1_bypass.io.prk_ex        := rf_ex_reg1.io.inst_pack_EX.prk
    fu1_bypass.io.prf_wdata_wb  := fu1_ex_wb_reg.io.alu_out_WB
    fu1_bypass.io.rd_valid_wb   := fu1_ex_wb_reg.io.inst_pack_WB.rd_valid
    
    fu1_ex_wb_reg.io.flush          := rob.io.predict_fail_cmt
    fu1_ex_wb_reg.io.stall          := false.B
    fu1_ex_wb_reg.io.inst_pack_EX   := rf_ex_reg1.io.inst_pack_EX
    fu1_ex_wb_reg.io.alu_out_EX     := alu1.io.alu_out
    fu1_ex_wb_reg.io.inst_valid_EX  := rf_ex_reg1.io.inst_valid_EX

    // 2. arith-branch fu
    alu2.io.alu_op := rf_ex_reg2.io.inst_pack_EX.alu_op
    alu2.io.src1 := MuxLookup(rf_ex_reg2.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG -> Mux(fu2_bypass.io.forward_prj_en, fu2_bypass.io.forward_prj_data, rf_ex_reg2.io.src1_EX),
        RS1_PC -> rf_ex_reg2.io.inst_pack_EX.pc,
        RS1_ZERO -> 0.U)
    )
    alu2.io.src2 := MuxLookup(rf_ex_reg2.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG -> Mux(fu2_bypass.io.forward_prk_en, fu2_bypass.io.forward_prk_data, rf_ex_reg2.io.src2_EX),
        RS2_IMM -> rf_ex_reg2.io.inst_pack_EX.imm,
        RS2_FOUR -> 4.U)
    )

    br.io.br_type       := rf_ex_reg2.io.inst_pack_EX.br_type
    br.io.src1          := Mux(fu2_bypass.io.forward_prj_en, fu2_bypass.io.forward_prj_data, rf_ex_reg2.io.src1_EX)
    br.io.src2          := Mux(fu2_bypass.io.forward_prk_en, fu2_bypass.io.forward_prk_data, rf_ex_reg2.io.src2_EX)
    br.io.pc_ex         := rf_ex_reg2.io.inst_pack_EX.pc
    br.io.imm_ex        := rf_ex_reg2.io.inst_pack_EX.imm

    fu2_bypass.io.prd_wb        := fu2_ex_wb_reg.io.inst_pack_WB.prd
    fu2_bypass.io.prj_ex        := rf_ex_reg2.io.inst_pack_EX.prj
    fu2_bypass.io.prk_ex        := rf_ex_reg2.io.inst_pack_EX.prk
    fu2_bypass.io.prf_wdata_wb  := fu2_ex_wb_reg.io.alu_out_WB
    fu2_bypass.io.rd_valid_wb   := fu2_ex_wb_reg.io.inst_pack_WB.rd_valid

    fu2_ex_wb_reg.io.flush              := rob.io.predict_fail_cmt
    fu2_ex_wb_reg.io.stall              := false.B
    fu2_ex_wb_reg.io.inst_pack_EX       := rf_ex_reg2.io.inst_pack_EX
    fu2_ex_wb_reg.io.alu_out_EX         := alu2.io.alu_out
    fu2_ex_wb_reg.io.predict_fail_EX    := br.io.predict_fail
    fu2_ex_wb_reg.io.branch_target_EX   := br.io.branch_target
    fu2_ex_wb_reg.io.inst_valid_EX      := rf_ex_reg2.io.inst_valid_EX

    // 3. load-store fu, include agu and cache
    ls_ex1_ex2_reg.io.flush             := rob.io.predict_fail_cmt
    ls_ex1_ex2_reg.io.stall             := false.B
    ls_ex1_ex2_reg.io.inst_pack_EX1     := rf_ex_reg3.io.inst_pack_EX
    ls_ex1_ex2_reg.io.mem_addr_EX1      := Mux(fu3_bypass.io.forward_prj_en, fu3_bypass.io.forward_prj_data, rf_ex_reg3.io.src1_EX) + rf_ex_reg3.io.inst_pack_EX.imm
    ls_ex1_ex2_reg.io.inst_valid_EX1    := rf_ex_reg3.io.inst_valid_EX

    fu3_bypass.io.prd_wb        := fu3_ex_wb_reg.io.inst_pack_WB.prd
    fu3_bypass.io.prj_ex        := rf_ex_reg3.io.inst_pack_EX.prj
    fu3_bypass.io.prk_ex        := rf_ex_reg3.io.inst_pack_EX.prk
    fu3_bypass.io.prf_wdata_wb  := fu3_ex_wb_reg.io.mem_rdata_WB
    fu3_bypass.io.rd_valid_wb   := fu3_ex_wb_reg.io.inst_pack_WB.rd_valid



    // DCache
    io.mem_addr_ex      := ls_ex1_ex2_reg.io.mem_addr_EX2
    io.mem_type_ex      := ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type
    io.mem_wdata_ex     := rf_ex_reg4.io.src2_EX

    fu3_ex_wb_reg.io.flush          := rob.io.predict_fail_cmt
    fu3_ex_wb_reg.io.stall          := false.B
    fu3_ex_wb_reg.io.inst_pack_EX2  := ls_ex1_ex2_reg.io.inst_pack_EX2
    fu3_ex_wb_reg.io.mem_rdata_EX2  := io.mem_rdata_ex
    fu3_ex_wb_reg.io.inst_valid_EX2 := ls_ex1_ex2_reg.io.inst_valid_EX2

    // 4. multiply-divide fu
    mdu.io.md_op    := rf_ex_reg4.io.inst_pack_EX.alu_op
    mdu.io.src1     := Mux(fu4_bypass.io.forward_prj_en, fu4_bypass.io.forward_prj_data, rf_ex_reg4.io.src1_EX)
    mdu.io.src2     := Mux(fu4_bypass.io.forward_prk_en, fu4_bypass.io.forward_prk_data, rf_ex_reg4.io.src2_EX)

    fu4_ex_wb_reg.io.flush          := rob.io.predict_fail_cmt
    fu4_ex_wb_reg.io.stall          := false.B
    fu4_ex_wb_reg.io.inst_pack_EX   := rf_ex_reg4.io.inst_pack_EX
    fu4_ex_wb_reg.io.md_out_EX      := mdu.io.md_out
    fu4_ex_wb_reg.io.inst_valid_EX  := rf_ex_reg4.io.inst_valid_EX

    fu4_bypass.io.prd_wb        := fu4_ex_wb_reg.io.inst_pack_WB.prd
    fu4_bypass.io.prj_ex        := rf_ex_reg4.io.inst_pack_EX.prj
    fu4_bypass.io.prk_ex        := rf_ex_reg4.io.inst_pack_EX.prk
    fu4_bypass.io.prf_wdata_wb  := fu4_ex_wb_reg.io.md_out_WB
    fu4_bypass.io.rd_valid_wb   := fu4_ex_wb_reg.io.inst_pack_WB.rd_valid

    // WB stage
    rob.io.inst_valid_rn        := id_rn_reg.io.insts_valid_RN
    rob.io.rd_rn                := id_rn_reg.io.rd_RN
    rob.io.rd_valid_rn          := id_rn_reg.io.rd_valid_RN
    rob.io.prd_rn               := reg_rename.io.prd
    rob.io.pprd_rn              := reg_rename.io.pprd
    rob.io.pc_rn                := id_rn_reg.io.pcs_RN
    rob.io.stall                := id_rn_reg.io.stall

    rob.io.inst_valid_wb        := VecInit(fu1_ex_wb_reg.io.inst_valid_WB, fu2_ex_wb_reg.io.inst_valid_WB, fu3_ex_wb_reg.io.inst_valid_WB, fu4_ex_wb_reg.io.inst_valid_WB)
    rob.io.rob_index_wb         := VecInit(fu1_ex_wb_reg.io.inst_pack_WB.rob_index, fu2_ex_wb_reg.io.inst_pack_WB.rob_index, fu3_ex_wb_reg.io.inst_pack_WB.rob_index, fu4_ex_wb_reg.io.inst_pack_WB.rob_index)
    rob.io.predict_fail_wb      := VecInit(false.B, fu2_ex_wb_reg.io.predict_fail_WB, false.B, false.B)
    rob.io.branch_target_wb     := VecInit(0.U, fu2_ex_wb_reg.io.branch_target_WB, 0.U, 0.U)
    rob.io.rf_wdata_wb          := VecInit(fu1_ex_wb_reg.io.alu_out_WB, fu2_ex_wb_reg.io.alu_out_WB, fu3_ex_wb_reg.io.mem_rdata_WB, fu4_ex_wb_reg.io.md_out_WB)

    // Commit stage
    arat.io.cmt_en          := rob.io.cmt_en
    arat.io.rd_cmt          := rob.io.rd_cmt
    arat.io.prd_cmt         := rob.io.prd_cmt
    arat.io.pprd_cmt        := rob.io.pprd_cmt
    arat.io.rd_valid_cmt    := rob.io.rd_valid_cmt
    arat.io.predict_fail    := rob.io.predict_fail_cmt

    io.commit_en1           := rob.io.cmt_en(0)
    io.commit_rd1           := rob.io.rd_cmt(0)
    io.commit_prd1          := rob.io.prd_cmt(0)
    io.commit_rd_valid1     := rob.io.rd_valid_cmt(0)
    io.commit_rf_wdata1     := rob.io.rf_wdata_cmt(0)
    io.commit_pc_1          := rob.io.pc_cmt(0)

    io.commit_en2           := rob.io.cmt_en(1)
    io.commit_rd2           := rob.io.rd_cmt(1)
    io.commit_prd2          := rob.io.prd_cmt(1)
    io.commit_rd_valid2     := rob.io.rd_valid_cmt(1)
    io.commit_rf_wdata2     := rob.io.rf_wdata_cmt(1)
    io.commit_pc_2          := rob.io.pc_cmt(1)

    io.commit_en3           := rob.io.cmt_en(2)
    io.commit_rd3           := rob.io.rd_cmt(2)
    io.commit_prd3          := rob.io.prd_cmt(2)
    io.commit_rd_valid3     := rob.io.rd_valid_cmt(2)
    io.commit_rf_wdata3     := rob.io.rf_wdata_cmt(2)
    io.commit_pc_3          := rob.io.pc_cmt(2)

    io.commit_en4           := rob.io.cmt_en(3)
    io.commit_rd4           := rob.io.rd_cmt(3)
    io.commit_prd4          := rob.io.prd_cmt(3)
    io.commit_rd_valid4     := rob.io.rd_valid_cmt(3)
    io.commit_rf_wdata4     := rob.io.rf_wdata_cmt(3)
    io.commit_pc_4          := rob.io.pc_cmt(3)
    
}

object CPU extends App {
    emitVerilog(new CPU(0x1c000000), Array("-td", "build/"))
}