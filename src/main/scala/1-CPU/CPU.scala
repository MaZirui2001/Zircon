import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._

class CPU_IO extends Bundle{
    val pc_IF                       = Output(UInt(32.W))
    val inst1_IF                    = Input(UInt(32.W))
    val inst2_IF                    = Input(UInt(32.W))
    val inst3_IF                    = Input(UInt(32.W))
    val inst4_IF                    = Input(UInt(32.W))

    val mem_raddr_ex                = Output(UInt(32.W))
    val mem_rdata_ex                = Input(UInt(32.W))
    val mem_is_load_ex              = Output(Bool())
    val mem_rlen_ex                 = Output(UInt(3.W))

    val mem_waddr_cmt               = Output(UInt(32.W))
    val mem_wdata_cmt               = Output(UInt(32.W))
    val mem_wlen_cmt                = Output(UInt(3.W))
    val mem_is_store_cmt            = Output(Bool())

    val commit_en1                  = Output(Bool())
    val commit_rd1                  = Output(UInt(5.W))
    val commit_prd1                 = Output(UInt(7.W))
    val commit_rd_valid1            = Output(Bool())
    val commit_rf_wdata1            = Output(UInt(32.W))
    val commit_pc_1                 = Output(UInt(32.W))
    val commit_is_ucread1           = Output(Bool())
    val commit_is_br1               = Output(Bool())
    val commit_br_type1             = Output(UInt(2.W))
    val commit_predict_fail1        = Output(Bool())

    val commit_en2                  = Output(Bool())
    val commit_rd2                  = Output(UInt(5.W))
    val commit_prd2                 = Output(UInt(7.W))
    val commit_rd_valid2            = Output(Bool())
    val commit_rf_wdata2            = Output(UInt(32.W))
    val commit_pc_2                 = Output(UInt(32.W))
    val commit_is_ucread2           = Output(Bool())
    val commit_is_br2               = Output(Bool())
    val commit_br_type2             = Output(UInt(2.W))
    val commit_predict_fail2        = Output(Bool())
    
    val commit_en3                  = Output(Bool())
    val commit_rd3                  = Output(UInt(5.W))
    val commit_prd3                 = Output(UInt(7.W))
    val commit_rd_valid3            = Output(Bool())
    val commit_rf_wdata3            = Output(UInt(32.W))
    val commit_pc_3                 = Output(UInt(32.W))
    val commit_is_ucread3           = Output(Bool())
    val commit_is_br3               = Output(Bool())
    val commit_br_type3             = Output(UInt(2.W))
    val commit_predict_fail3        = Output(Bool())

    val commit_en4                  = Output(Bool())
    val commit_rd4                  = Output(UInt(5.W))
    val commit_prd4                 = Output(UInt(7.W))
    val commit_rd_valid4            = Output(Bool())
    val commit_rf_wdata4            = Output(UInt(32.W))
    val commit_pc_4                 = Output(UInt(32.W))
    val commit_is_ucread4           = Output(Bool())
    val commit_is_br4               = Output(Bool())
    val commit_br_type4             = Output(UInt(2.W))
    val commit_predict_fail4        = Output(Bool())


    val commit_stall_by_fetch_queue = Output(Bool())
    val commit_stall_by_rename      = Output(Bool())
    val commit_stall_by_rob         = Output(Bool())
    val commit_stall_by_iq1         = Output(Bool())
    val commit_stall_by_iq2         = Output(Bool())
    val commit_stall_by_iq3         = Output(Bool())
    val commit_stall_by_iq4         = Output(Bool())
    val commit_stall_by_sb          = Output(Bool())
    


}
class CPU(RESET_VEC: Int) extends Module {
    val io = IO(new CPU_IO)

    val pc              = Module(new PC(RESET_VEC))
    val predict         = Module(new Predict)
    val if_pd_reg       = Module(new IF_PD_Reg)
    val pd              = Module(new Prev_Decode)
    val pd_fq_reg       = Module(new PD_FQ_Reg)
    val inst_decode     = VecInit(Seq.fill(4)(Module(new Decode).io))
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
    val sb              = Module(new SB(8))
    val fu4_bypass      = Module(new Bypass)

    val fu1_ex_wb_reg   = Module(new FU1_EX_WB_Reg)
    val fu2_ex_wb_reg   = Module(new FU2_EX_WB_Reg)
    val fu3_ex_wb_reg   = Module(new LS_EX2_WB_Reg)
    val fu4_ex_wb_reg   = Module(new MD_EX_WB_Reg)

    val rob             = Module(new ROB(48))
    val arat            = Module(new Arch_Rat)

    val stall_by_iq = iq1.io.full || iq2.io.full || iq3.io.full || iq4.io.full

    // IF stage
    io.pc_IF                        := pc.io.pc_IF
    pc.io.pc_stall                  := !inst_queue.io.inst_queue_ready 
    pc.io.predict_fail              := rob.io.predict_fail_cmt
    pc.io.branch_target             := rob.io.branch_target_cmt
    pc.io.pred_jump                 := predict.io.predict_jump
    pc.io.pred_npc                  := predict.io.pred_npc
    pc.io.flush_by_pd               := pd.io.pred_fix 
    pc.io.flush_pd_target           := pd.io.pred_fix_target

    predict.io.npc                  := pc.io.npc
    predict.io.pc                   := pc.io.pc_IF
    predict.io.pc_cmt               := rob.io.pred_pc_cmt
    predict.io.real_jump            := rob.io.pred_real_jump_cmt
    predict.io.branch_target        := rob.io.pred_branch_target_cmt
    predict.io.update_en            := rob.io.pred_update_en_cmt
    predict.io.br_type              := rob.io.br_type_pred_cmt
    predict.io.predict_fail         := rob.io.predict_fail_cmt
    predict.io.top_arch             := arat.io.top_arch
    predict.io.ras_update_en        := rob.io.ras_update_en_cmt


    // IF-PD SegReg
    val inst_IF                 = VecInit(io.inst1_IF, io.inst2_IF, io.inst3_IF, io.inst4_IF)
    val pcs_IF                  = VecInit(pc.io.pc_IF, pc.io.pc_IF+4.U, pc.io.pc_IF+8.U, pc.io.pc_IF+12.U)
    if_pd_reg.io.flush          := rob.io.predict_fail_cmt || (!pd_fq_reg.io.stall && pd.io.pred_fix)
    if_pd_reg.io.stall          := !inst_queue.io.inst_queue_ready 
    if_pd_reg.io.insts_pack_IF  := VecInit(Seq.tabulate(4)(i => inst_pack_IF_gen(pcs_IF(i), inst_IF(i), pc.io.inst_valid_IF(i), predict.io.predict_jump(i), predict.io.pred_npc, predict.io.pred_valid(i))))

    // PD stage
    pd.io.insts_pack_IF         := if_pd_reg.io.insts_pack_PD

    // PD-FQ SegReg
    pd_fq_reg.io.flush          := rob.io.predict_fail_cmt
    pd_fq_reg.io.stall          := !inst_queue.io.inst_queue_ready
    pd_fq_reg.io.insts_pack_PD  := VecInit(Seq.tabulate(4)(i => inst_pack_PD_gen(pd.io.insts_pack_PD(i))))

    // Fetch_Queue stage && FQ-ID SegReg
    inst_queue.io.insts_pack    := pd_fq_reg.io.insts_pack_FQ
    inst_queue.io.next_ready    := !(rob.io.full || stall_by_iq || reg_rename.io.free_list_empty)
    inst_queue.io.flush         := rob.io.predict_fail_cmt

    // ID stage
    for(i <- 0 until 4){
        inst_decode(i).inst := inst_queue.io.insts_pack_id(i).inst
    }

    // ID-RN SegReg
    id_rn_reg.io.flush          := rob.io.predict_fail_cmt
    id_rn_reg.io.stall          := rob.io.full || reg_rename.io.free_list_empty || stall_by_iq
    id_rn_reg.io.insts_pack_ID  := VecInit(Seq.tabulate(4)(i => inst_pack_ID_gen(inst_queue.io.insts_pack_id(i), inst_queue.io.insts_valid_decode(i), inst_decode(i).rj, inst_decode(i).rj_valid, inst_decode(i).rk, inst_decode(i).rk_valid, 
                                                                            inst_decode(i).rd, inst_decode(i).rd_valid, inst_decode(i).imm, inst_decode(i).alu_op, inst_decode(i).alu_rs1_sel, inst_decode(i).alu_rs2_sel, 
                                                                            inst_decode(i).br_type, inst_decode(i).mem_type, inst_decode(i).fu_id, inst_decode(i).inst_exist)))
    // Reg Rename
    reg_rename.io.rj                := id_rn_reg.io.insts_pack_RN.map(_.rj)
    reg_rename.io.rk                := id_rn_reg.io.insts_pack_RN.map(_.rk)
    reg_rename.io.rd                := id_rn_reg.io.insts_pack_RN.map(_.rd)
    reg_rename.io.rd_valid          := id_rn_reg.io.insts_pack_RN.map(_.rd_valid)
    reg_rename.io.rename_en         := VecInit(Seq.tabulate(4)(i => id_rn_reg.io.insts_pack_RN(i).inst_valid && !id_rn_reg.io.stall))
    reg_rename.io.commit_en         := rob.io.cmt_en
    reg_rename.io.commit_pprd_valid := rob.io.rd_valid_cmt
    reg_rename.io.commit_pprd       := rob.io.pprd_cmt
    reg_rename.io.predict_fail      := rob.io.predict_fail_cmt
    reg_rename.io.arch_rat          := arat.io.arch_rat
    reg_rename.io.head_arch         := arat.io.head_arch
    
    // RN-DP SegReg
    rn_dp_reg.io.flush          := ((rob.io.full || reg_rename.io.free_list_empty) && !(stall_by_iq)) || rob.io.predict_fail_cmt
    rn_dp_reg.io.stall          := stall_by_iq 
    rn_dp_reg.io.insts_pack_RN  := VecInit(Seq.tabulate(4)(i => inst_pack_RN_gen(id_rn_reg.io.insts_pack_RN(i), reg_rename.io.prj(i), reg_rename.io.prk(i), reg_rename.io.prd(i), reg_rename.io.pprd(i), rob.io.rob_index_rn(i), reg_rename.io.prj_raw(i), reg_rename.io.prk_raw(i))))

    // DP stage
    dp.io.inst_packs            := rn_dp_reg.io.insts_pack_DP
    dp.io.prd_queue             := VecInit( VecInit(iq1.io.prd_queue :+ 0.U(7.W)), 
                                            VecInit(iq2.io.prd_queue :+ 0.U(7.W)), 
                                            VecInit(iq3.io.prd_queue :+ Mux(rf_ex_reg3.io.inst_pack_RF.rd_valid, rf_ex_reg3.io.inst_pack_RF.prd, 0.U)), 
                                            VecInit(iq4.io.prd_queue :+ 0.U(7.W)))
    dp.io.elem_num              := VecInit(iq1.io.elem_num, iq2.io.elem_num)

    // issue stage
    // 1. arith1, common calculate
    iq1.io.insts_dispatch       := rn_dp_reg.io.insts_pack_DP
    iq1.io.insts_disp_index     := dp.io.insts_disp_index(0)
    iq1.io.insts_disp_valid     := dp.io.insts_disp_valid(0)
    iq1.io.insert_num           := dp.io.insert_num(0)
    iq1.io.prj_ready            := dp.io.prj_ready
    iq1.io.prk_ready            := dp.io.prk_ready
    iq1.io.issue_ack            := sel1.io.issue_ack
    iq1.io.flush                := rob.io.predict_fail_cmt
    iq1.io.stall                := stall_by_iq
    
    sel1.io.insts_issue         := iq1.io.insts_issue
    sel1.io.issue_req           := iq1.io.issue_req
    sel1.io.stall               := !(iq1.io.issue_req.asUInt.orR)

    // 2. arith2, calculate and branch 
    iq2.io.insts_dispatch       := rn_dp_reg.io.insts_pack_DP
    iq2.io.insts_disp_index     := dp.io.insts_disp_index(1)
    iq2.io.insts_disp_valid     := dp.io.insts_disp_valid(1)
    iq2.io.insert_num           := dp.io.insert_num(1)
    iq2.io.prj_ready            := dp.io.prj_ready
    iq2.io.prk_ready            := dp.io.prk_ready
    iq2.io.issue_ack            := sel2.io.issue_ack
    iq2.io.flush                := rob.io.predict_fail_cmt
    iq2.io.stall                := stall_by_iq 

    sel2.io.insts_issue         := iq2.io.insts_issue
    sel2.io.issue_req           := iq2.io.issue_req
    sel2.io.stall               := !(iq2.io.issue_req.asUInt.orR)

    // 3. load, load and store
    iq3.io.insts_dispatch       := rn_dp_reg.io.insts_pack_DP
    iq3.io.insts_disp_index     := dp.io.insts_disp_index(2)
    iq3.io.insts_disp_valid     := dp.io.insts_disp_valid(2)
    iq3.io.insert_num           := dp.io.insert_num(2)
    iq3.io.prj_ready            := dp.io.prj_ready
    iq3.io.prk_ready            := dp.io.prk_ready
    iq3.io.issue_ack            := sel3.io.issue_ack
    iq3.io.flush                := rob.io.predict_fail_cmt
    iq3.io.stall                := stall_by_iq

    sel3.io.insts_issue         := iq3.io.insts_issue
    sel3.io.issue_req           := iq3.io.issue_req
    sel3.io.stall               := !(iq3.io.issue_req) || sb.io.full

    // 4. multiply, multiply and divide
    iq4.io.insts_dispatch       := rn_dp_reg.io.insts_pack_DP
    iq4.io.insts_disp_index     := dp.io.insts_disp_index(3)
    iq4.io.insts_disp_valid     := dp.io.insts_disp_valid(3)
    iq4.io.insert_num           := dp.io.insert_num(3)
    iq4.io.prj_ready            := dp.io.prj_ready
    iq4.io.prk_ready            := dp.io.prk_ready
    iq4.io.issue_ack            := sel4.io.issue_ack
    iq4.io.flush                := rob.io.predict_fail_cmt
    iq4.io.stall                := stall_by_iq 

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
    is_rf_reg1.io.inst_pack_IS  := inst_pack_IS_gen(sel1.io.inst_issue.inst, sel1.io.inst_issue_valid)

    is_rf_reg2.io.flush         := rob.io.predict_fail_cmt
    is_rf_reg2.io.stall         := false.B
    is_rf_reg2.io.inst_pack_IS  := inst_pack_IS_gen(sel2.io.inst_issue.inst, sel2.io.inst_issue_valid)

    is_rf_reg3.io.flush         := rob.io.predict_fail_cmt
    is_rf_reg3.io.stall         := false.B
    is_rf_reg3.io.inst_pack_IS  := inst_pack_IS_gen(sel3.io.inst_issue.inst, sel3.io.inst_issue_valid)

    is_rf_reg4.io.flush         := rob.io.predict_fail_cmt
    is_rf_reg4.io.stall         := false.B
    is_rf_reg4.io.inst_pack_IS  := inst_pack_IS_gen(sel4.io.inst_issue.inst, sel4.io.inst_issue_valid)

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
    rf_ex_reg1.io.src1_RF       := rf.io.prj_data(0)
    rf_ex_reg1.io.src2_RF       := rf.io.prk_data(0)

    rf_ex_reg2.io.flush         := rob.io.predict_fail_cmt
    rf_ex_reg2.io.stall         := false.B
    rf_ex_reg2.io.inst_pack_RF  := is_rf_reg2.io.inst_pack_RF
    rf_ex_reg2.io.src1_RF       := rf.io.prj_data(1)
    rf_ex_reg2.io.src2_RF       := rf.io.prk_data(1)

    rf_ex_reg3.io.flush         := rob.io.predict_fail_cmt
    rf_ex_reg3.io.stall         := false.B
    rf_ex_reg3.io.inst_pack_RF  := is_rf_reg3.io.inst_pack_RF
    rf_ex_reg3.io.src1_RF       := rf.io.prj_data(2)
    rf_ex_reg3.io.src2_RF       := rf.io.prk_data(2)

    rf_ex_reg4.io.flush         := rob.io.predict_fail_cmt
    rf_ex_reg4.io.stall         := false.B
    rf_ex_reg4.io.inst_pack_RF  := is_rf_reg4.io.inst_pack_RF
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
    br.io.predict_jump  := rf_ex_reg2.io.inst_pack_EX.predict_jump
    br.io.pred_npc      := rf_ex_reg2.io.inst_pack_EX.pred_npc

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
    fu2_ex_wb_reg.io.real_jump_EX       := br.io.real_jump

    // 3. load-store fu, include agu and cache
    ls_ex1_ex2_reg.io.flush             := rob.io.predict_fail_cmt
    ls_ex1_ex2_reg.io.stall             := false.B
    ls_ex1_ex2_reg.io.inst_pack_EX1     := rf_ex_reg3.io.inst_pack_EX
    ls_ex1_ex2_reg.io.mem_addr_EX1      := Mux(fu3_bypass.io.forward_prj_en, fu3_bypass.io.forward_prj_data, rf_ex_reg3.io.src1_EX) + rf_ex_reg3.io.inst_pack_EX.imm
    ls_ex1_ex2_reg.io.mem_wdata_EX1     := Mux(fu3_bypass.io.forward_prk_en, fu3_bypass.io.forward_prk_data, rf_ex_reg3.io.src2_EX)

    fu3_bypass.io.prd_wb        := fu3_ex_wb_reg.io.inst_pack_WB.prd
    fu3_bypass.io.prj_ex        := rf_ex_reg3.io.inst_pack_EX.prj
    fu3_bypass.io.prk_ex        := rf_ex_reg3.io.inst_pack_EX.prk
    fu3_bypass.io.prf_wdata_wb  := fu3_ex_wb_reg.io.mem_rdata_WB
    fu3_bypass.io.rd_valid_wb   := fu3_ex_wb_reg.io.inst_pack_WB.rd_valid

    // store_buf
    sb.io.flush             := rob.io.predict_fail_cmt
    sb.io.is_store_ex       := ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type(4) === 0.U && ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type =/= NO_MEM
    sb.io.addr_ex           := ls_ex1_ex2_reg.io.mem_addr_EX2
    sb.io.st_data_ex        := ls_ex1_ex2_reg.io.mem_wdata_EX2
    sb.io.st_wlen_ex        := ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type(2, 0)
    sb.io.is_store_num_cmt  := rob.io.is_store_num_cmt

    io.mem_raddr_ex      := ls_ex1_ex2_reg.io.mem_addr_EX2
    io.mem_rlen_ex       := ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type(2, 0)
    io.mem_is_load_ex    := ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type(4) === 1.U && ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type =/= NO_MEM
    io.mem_waddr_cmt     := sb.io.st_addr_cmt
    io.mem_wdata_cmt     := sb.io.st_data_cmt
    io.mem_wlen_cmt      := sb.io.st_wlen_cmt
    io.mem_is_store_cmt  := sb.io.is_store_cmt

    val mem_rdata = Mux(sb.io.ld_hit, sb.io.ld_data_ex, io.mem_rdata_ex)
    fu3_ex_wb_reg.io.flush          := rob.io.predict_fail_cmt
    fu3_ex_wb_reg.io.stall          := false.B
    fu3_ex_wb_reg.io.inst_pack_EX2  := ls_ex1_ex2_reg.io.inst_pack_EX2
    fu3_ex_wb_reg.io.mem_rdata_EX2  := MuxLookup(ls_ex1_ex2_reg.io.inst_pack_EX2.mem_type, 0.U)(Seq(
        NO_MEM -> 0.U,
        MEM_LDB -> Cat(Fill(24, mem_rdata(7)), mem_rdata(7, 0)),
        MEM_LDBU -> Cat(Fill(24, 0.U), mem_rdata(7, 0)),
        MEM_LDH -> Cat(Fill(16, mem_rdata(15)), mem_rdata(15, 0)),
        MEM_LDHU -> Cat(Fill(16, 0.U), mem_rdata(15, 0)),
        MEM_LDW -> mem_rdata(31, 0)
    ))
    fu3_ex_wb_reg.io.is_ucread_EX2  := ls_ex1_ex2_reg.io.mem_addr_EX2(31, 28) === 0xa.U

    // 4. multiply-divide fu
    mdu.io.md_op    := rf_ex_reg4.io.inst_pack_EX.alu_op
    mdu.io.src1     := Mux(fu4_bypass.io.forward_prj_en, fu4_bypass.io.forward_prj_data, rf_ex_reg4.io.src1_EX)
    mdu.io.src2     := Mux(fu4_bypass.io.forward_prk_en, fu4_bypass.io.forward_prk_data, rf_ex_reg4.io.src2_EX)

    fu4_ex_wb_reg.io.flush          := rob.io.predict_fail_cmt
    fu4_ex_wb_reg.io.stall          := false.B
    fu4_ex_wb_reg.io.inst_pack_EX   := rf_ex_reg4.io.inst_pack_EX
    fu4_ex_wb_reg.io.md_out_EX      := mdu.io.md_out

    fu4_bypass.io.prd_wb        := fu4_ex_wb_reg.io.inst_pack_WB.prd
    fu4_bypass.io.prj_ex        := rf_ex_reg4.io.inst_pack_EX.prj
    fu4_bypass.io.prk_ex        := rf_ex_reg4.io.inst_pack_EX.prk
    fu4_bypass.io.prf_wdata_wb  := fu4_ex_wb_reg.io.md_out_WB
    fu4_bypass.io.rd_valid_wb   := fu4_ex_wb_reg.io.inst_pack_WB.rd_valid

    // WB stage
    val is_store_rn = VecInit(Seq.tabulate(4)(i => (id_rn_reg.io.insts_pack_RN(i).mem_type =/= NO_MEM && id_rn_reg.io.insts_pack_RN(i).mem_type(4) === 0.U)))
    val pred_update_en = VecInit(Seq.tabulate(4)(i => id_rn_reg.io.insts_pack_RN(i).br_type =/= NO_BR))
    val br_type_pred = VecInit(Seq.tabulate(4)(i => Mux(id_rn_reg.io.insts_pack_RN(i).br_type === BR_JIRL, 1.U(2.W), Mux(id_rn_reg.io.insts_pack_RN(i).br_type === BR_BL, 2.U(2.W), 0.U(2.W)))))
    val ras_update_en = VecInit(Seq.tabulate(4)(i => id_rn_reg.io.insts_pack_RN(i).br_type === BR_JIRL || id_rn_reg.io.insts_pack_RN(i).br_type === BR_BL))
    rob.io.inst_valid_rn        := id_rn_reg.io.insts_pack_RN.map(_.inst_valid)
    rob.io.rd_rn                := id_rn_reg.io.insts_pack_RN.map(_.rd)
    rob.io.rd_valid_rn          := id_rn_reg.io.insts_pack_RN.map(_.rd_valid)
    rob.io.prd_rn               := reg_rename.io.prd
    rob.io.pprd_rn              := reg_rename.io.pprd
    rob.io.pc_rn                := id_rn_reg.io.insts_pack_RN.map(_.pc)
    rob.io.is_store_rn          := is_store_rn
    rob.io.stall                := id_rn_reg.io.stall
    rob.io.pred_update_en_rn    := pred_update_en
    rob.io.br_type_pred_rn      := br_type_pred
    rob.io.ras_update_en_rn     := ras_update_en

    rob.io.inst_valid_wb        := VecInit(fu1_ex_wb_reg.io.inst_pack_WB.inst_valid, fu2_ex_wb_reg.io.inst_pack_WB.inst_valid, fu3_ex_wb_reg.io.inst_pack_WB.inst_valid, fu4_ex_wb_reg.io.inst_pack_WB.inst_valid)
    rob.io.rob_index_wb         := VecInit(fu1_ex_wb_reg.io.inst_pack_WB.rob_index, fu2_ex_wb_reg.io.inst_pack_WB.rob_index, fu3_ex_wb_reg.io.inst_pack_WB.rob_index, fu4_ex_wb_reg.io.inst_pack_WB.rob_index)
    rob.io.predict_fail_wb      := VecInit(false.B, fu2_ex_wb_reg.io.predict_fail_WB, false.B, false.B)
    rob.io.real_jump_wb         := VecInit(false.B, fu2_ex_wb_reg.io.real_jump_WB, false.B, false.B)
    rob.io.branch_target_wb     := VecInit(0.U, fu2_ex_wb_reg.io.branch_target_WB, 0.U, 0.U)
    rob.io.rf_wdata_wb          := VecInit(fu1_ex_wb_reg.io.alu_out_WB, fu2_ex_wb_reg.io.alu_out_WB, fu3_ex_wb_reg.io.mem_rdata_WB, fu4_ex_wb_reg.io.md_out_WB)
    rob.io.is_ucread_wb         := VecInit(false.B, false.B, fu3_ex_wb_reg.io.is_ucread_WB, false.B)
    
    // Commit stage
    arat.io.cmt_en          := rob.io.cmt_en
    arat.io.rd_cmt          := rob.io.rd_cmt
    arat.io.prd_cmt         := rob.io.prd_cmt
    arat.io.pprd_cmt        := rob.io.pprd_cmt
    arat.io.rd_valid_cmt    := rob.io.rd_valid_cmt
    arat.io.predict_fail    := rob.io.predict_fail_cmt
    arat.io.br_type_pred_cmt := rob.io.br_type_pred_cmt
    arat.io.ras_update_en_cmt := rob.io.ras_update_en_cmt

    io.commit_en1           := rob.io.cmt_en(0)
    io.commit_rd1           := rob.io.rd_cmt(0)
    io.commit_prd1          := rob.io.prd_cmt(0)
    io.commit_rd_valid1     := rob.io.rd_valid_cmt(0)
    io.commit_rf_wdata1     := rob.io.rf_wdata_cmt(0)
    io.commit_pc_1          := rob.io.pc_cmt(0)
    io.commit_is_ucread1    := rob.io.is_ucread_cmt(0)
    io.commit_is_br1        := rob.io.is_br_stat(0)
    io.commit_br_type1      := rob.io.br_type_stat(0)
    io.commit_predict_fail1 := rob.io.predict_fail_stat(0)

    io.commit_en2           := rob.io.cmt_en(1)
    io.commit_rd2           := rob.io.rd_cmt(1)
    io.commit_prd2          := rob.io.prd_cmt(1)
    io.commit_rd_valid2     := rob.io.rd_valid_cmt(1)
    io.commit_rf_wdata2     := rob.io.rf_wdata_cmt(1)
    io.commit_pc_2          := rob.io.pc_cmt(1)
    io.commit_is_ucread2    := rob.io.is_ucread_cmt(1)
    io.commit_is_br2        := rob.io.is_br_stat(1)
    io.commit_br_type2      := rob.io.br_type_stat(1)
    io.commit_predict_fail2 := rob.io.predict_fail_stat(1)

    io.commit_en3           := rob.io.cmt_en(2)
    io.commit_rd3           := rob.io.rd_cmt(2)
    io.commit_prd3          := rob.io.prd_cmt(2)
    io.commit_rd_valid3     := rob.io.rd_valid_cmt(2)
    io.commit_rf_wdata3     := rob.io.rf_wdata_cmt(2)
    io.commit_pc_3          := rob.io.pc_cmt(2)
    io.commit_is_ucread3    := rob.io.is_ucread_cmt(2)
    io.commit_is_br3        := rob.io.is_br_stat(2)
    io.commit_br_type3      := rob.io.br_type_stat(2)
    io.commit_predict_fail3 := rob.io.predict_fail_stat(2)

    io.commit_en4           := rob.io.cmt_en(3)
    io.commit_rd4           := rob.io.rd_cmt(3)
    io.commit_prd4          := rob.io.prd_cmt(3)
    io.commit_rd_valid4     := rob.io.rd_valid_cmt(3)
    io.commit_rf_wdata4     := rob.io.rf_wdata_cmt(3)
    io.commit_pc_4          := rob.io.pc_cmt(3)
    io.commit_is_ucread4    := rob.io.is_ucread_cmt(3)
    io.commit_is_br4        := rob.io.is_br_stat(3)
    io.commit_br_type4      := rob.io.br_type_stat(3)
    io.commit_predict_fail4 := rob.io.predict_fail_stat(3)

    io.commit_stall_by_fetch_queue  := !inst_queue.io.inst_queue_ready
    io.commit_stall_by_rename       := reg_rename.io.free_list_empty
    io.commit_stall_by_rob          := rob.io.full
    io.commit_stall_by_iq1          := iq1.io.full  && !iq1.io.stall
    io.commit_stall_by_iq2          := iq2.io.full  && !iq2.io.stall
    io.commit_stall_by_iq3          := iq3.io.full  && !iq3.io.stall
    io.commit_stall_by_iq4          := iq4.io.full && !iq4.io.stall
    io.commit_stall_by_sb           := sb.io.full


}

