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
    
    val commit_iq1_issue            = Output(Bool())
    val commit_iq2_issue            = Output(Bool())
    val commit_iq3_issue            = Output(Bool())
    val commit_iq4_issue            = Output(Bool())

}
class CPU(RESET_VEC: Int) extends Module {
    val io = IO(new CPU_IO)

    /* Inst Fetch Stage */
    val pc              = Module(new PC(RESET_VEC))
    val predict         = Module(new Predict)
    val ip_reg          = Module(new IF_PD_Reg)

    /* Previous Decode Stage */
    val pd              = Module(new Prev_Decode)
    val pf_reg          = Module(new PD_FQ_Reg)

    /* Fetch Queue Stage */
    val fq              = Module(new Fetch_Queue)

    /* Decode Stage */
    val decode          = VecInit(Seq.fill(4)(Module(new Decode).io))
    val dr_reg          = Module(new ID_RN_Reg)

    /* Rename Stage */
    val rename          = Module(new Reg_Rename)
    val rp_reg          = Module(new RN_DP_Reg)

    /* Dispatch Stage */
    val dp              = Module(new Dispatch)
    val bd              = Module(new Busy_Board)
    
    /* Issue Stage */
    val iq1             = Module(new Unorder_Issue_Queue(8, new inst_pack_DP_FU1_t))
    val sel1            = Module(new Unorder_Select(8, new inst_pack_DP_FU1_t))
    val ir_reg1         = Module(new IS_RF_Reg(new inst_pack_IS_FU1_t))

    val iq2             = Module(new Unorder_Issue_Queue(8, new inst_pack_DP_FU2_t))
    val sel2            = Module(new Unorder_Select(8, new inst_pack_DP_FU2_t))
    val ir_reg2         = Module(new IS_RF_Reg(new inst_pack_IS_FU2_t))

    val iq3             = Module(new Unorder_Issue_Queue(8, new inst_pack_DP_LS_t))
    val sel3            = Module(new Unorder_Select(8, new inst_pack_DP_LS_t))
    val ir_reg3         = Module(new IS_RF_Reg(new inst_pack_IS_LS_t))

    val iq4             = Module(new Order_Issue_Queue(8, new inst_pack_DP_MD_t))
    val sel4            = Module(new Order_Select(8, new inst_pack_DP_MD_t))
    val ir_reg4         = Module(new IS_RF_Reg(new inst_pack_IS_MD_t))

    /* Regfile Read Stage */
    val rf              = Module(new Physical_Regfile)
    val re_reg1         = Module(new RF_EX_Reg(new inst_pack_IS_FU1_t))
    val re_reg2         = Module(new RF_EX_Reg(new inst_pack_IS_FU2_t))
    val re_reg3         = Module(new RF_EX_Reg(new inst_pack_IS_LS_t))
    val re_reg4         = Module(new RF_EX_Reg(new inst_pack_IS_MD_t))

    /* Execute Stage */
    val alu1            = Module(new ALU)
    // val bypass1         = Module(new Bypass)
    val ew_reg1         = Module(new FU1_EX_WB_Reg)

    val alu2            = Module(new ALU)
    val br              = Module(new Branch)
    // val bypass2         = Module(new Bypass)
    val bypass12        = Module(new Bypass_2)
    val ew_reg2         = Module(new FU2_EX_WB_Reg)

    val bypass3         = Module(new Bypass)
    val ls_ex_reg       = Module(new LS_EX1_EX2_Reg)
    val sb              = Module(new SB(8))
    val ew_reg3         = Module(new LS_EX2_WB_Reg)

    val mdu             = Module(new MDU)
    val bypass4         = Module(new Bypass)
    val ew_reg4         = Module(new MD_EX_WB_Reg)

    /* Write Back Stage */
    val rob             = Module(new ROB(36))

    /* Commit Stage */
    val arat            = Module(new Arch_Rat)


    val stall_by_iq = iq1.io.full || iq2.io.full || iq3.io.full || iq4.io.full

    /* IF Stage */
    io.pc_IF                        := pc.io.pc_IF
    pc.io.pc_stall                  := !fq.io.inst_queue_ready 
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
    predict.io.pd_pred_fix          := pd.io.pred_fix
    predict.io.pd_pred_fix_is_bl    := pd.io.pred_fix_is_bl
    predict.io.pd_pc_plus_4         := pd.io.pred_fix_pc_plus_4


    // IF-PD SegReg
    val inst_IF                 = VecInit(io.inst1_IF, io.inst2_IF, io.inst3_IF, io.inst4_IF)
    val pcs_IF                  = VecInit(pc.io.pc_IF, pc.io.pc_IF+4.U, pc.io.pc_IF+8.U, pc.io.pc_IF+12.U)
    ip_reg.io.flush             := rob.io.predict_fail_cmt || (!pf_reg.io.stall && pd.io.pred_fix)
    ip_reg.io.stall             := !fq.io.inst_queue_ready 
    ip_reg.io.insts_pack_IF     := VecInit(Seq.tabulate(4)(i => inst_pack_IF_gen(pcs_IF(i), inst_IF(i), pc.io.inst_valid_IF(i), predict.io.predict_jump(i), predict.io.pred_npc, predict.io.pred_valid(i))))

    // PD stage
    pd.io.insts_pack_IF         := ip_reg.io.insts_pack_PD

    // PD-FQ SegReg
    pf_reg.io.flush             := rob.io.predict_fail_cmt
    pf_reg.io.stall             := !fq.io.inst_queue_ready
    pf_reg.io.insts_pack_PD     := VecInit(Seq.tabulate(4)(i => inst_pack_PD_gen(pd.io.insts_pack_PD(i))))

    // Fetch_Queue stage && FQ-ID SegReg
    fq.io.insts_pack    := pf_reg.io.insts_pack_FQ
    fq.io.next_ready    := !(rob.io.full || stall_by_iq || rename.io.free_list_empty)
    fq.io.flush         := rob.io.predict_fail_cmt

    // ID stage
    for(i <- 0 until 4){
        decode(i).inst := fq.io.insts_pack_id(i).inst
    }

    // ID-RN SegReg
    dr_reg.io.flush          := rob.io.predict_fail_cmt
    dr_reg.io.stall          := rob.io.full || rename.io.free_list_empty || stall_by_iq
    dr_reg.io.insts_pack_ID  := VecInit(Seq.tabulate(4)(i => inst_pack_ID_gen(fq.io.insts_pack_id(i), fq.io.insts_valid_decode(i), decode(i).rj, decode(i).rj_valid, decode(i).rk, decode(i).rk_valid, 
                                                                                decode(i).rd, decode(i).rd_valid, decode(i).imm, decode(i).alu_op, decode(i).alu_rs1_sel, decode(i).alu_rs2_sel, 
                                                                                decode(i).br_type, decode(i).mem_type, decode(i).fu_id, decode(i).inst_exist)))
    // Reg Rename
    rename.io.rj                := dr_reg.io.insts_pack_RN.map(_.rj)
    rename.io.rk                := dr_reg.io.insts_pack_RN.map(_.rk)
    rename.io.rd                := dr_reg.io.insts_pack_RN.map(_.rd)
    rename.io.rd_valid          := dr_reg.io.insts_pack_RN.map(_.rd_valid)
    rename.io.rename_en         := VecInit(Seq.tabulate(4)(i => dr_reg.io.insts_pack_RN(i).inst_valid && !dr_reg.io.stall))
    rename.io.commit_en         := rob.io.cmt_en
    rename.io.commit_pprd_valid := rob.io.rd_valid_cmt
    rename.io.commit_pprd       := rob.io.pprd_cmt
    rename.io.predict_fail      := rob.io.predict_fail_cmt
    rename.io.arch_rat          := arat.io.arch_rat
    rename.io.head_arch         := arat.io.head_arch
    
    // RN-DP SegReg
    rp_reg.io.flush          := ((rob.io.full || rename.io.free_list_empty) && !(stall_by_iq)) || rob.io.predict_fail_cmt
    rp_reg.io.stall          := stall_by_iq 
    rp_reg.io.insts_pack_RN  := VecInit(Seq.tabulate(4)(i => inst_pack_RN_gen(dr_reg.io.insts_pack_RN(i), rename.io.prj(i), rename.io.prk(i), rename.io.prd(i), rename.io.pprd(i), rob.io.rob_index_rn(i), rename.io.prj_raw(i), rename.io.prk_raw(i))))

    // DP stage
    dp.io.inst_packs            := rp_reg.io.insts_pack_DP
    dp.io.elem_num              := VecInit(iq1.io.elem_num, iq2.io.elem_num)


    // issue stage
    // busyboard
    bd.io.flush                 := rob.io.predict_fail_cmt
    bd.io.prj                   := rp_reg.io.insts_pack_DP.map(_.prj)
    bd.io.rj_valid              := rp_reg.io.insts_pack_DP.map(_.rj_valid)
    bd.io.prk                   := rp_reg.io.insts_pack_DP.map(_.prk)
    bd.io.rk_valid              := rp_reg.io.insts_pack_DP.map(_.rk_valid)
    bd.io.prd_wake              := VecInit(sel1.io.wake_preg, sel2.io.wake_preg, re_reg3.io.inst_pack_RF.prd, sel4.io.wake_preg)
    bd.io.prd_wake_valid        := VecInit(sel1.io.inst_issue_valid, sel2.io.inst_issue_valid, re_reg3.io.inst_pack_RF.rd_valid, sel4.io.inst_issue_valid)
    bd.io.prd_disp              := rp_reg.io.insts_pack_DP.map(_.prd)
    bd.io.prd_disp_valid        := rp_reg.io.insts_pack_DP.map(_.rd_valid)
    val prj_ready               = VecInit(Seq.tabulate(4)(i => !rp_reg.io.insts_pack_DP(i).rj_valid || rp_reg.io.insts_pack_DP(i).prj === 0.U || (!rp_reg.io.insts_pack_DP(i).prj_raw && !bd.io.prj_busy(i))))
    val prk_ready               = VecInit(Seq.tabulate(4)(i => !rp_reg.io.insts_pack_DP(i).rk_valid || rp_reg.io.insts_pack_DP(i).prk === 0.U || (!rp_reg.io.insts_pack_DP(i).prk_raw && !bd.io.prk_busy(i))))
    
    // 1. arith1, common calculate
    iq1.io.insts_dispatch       := VecInit(Seq.tabulate(4)(i => inst_pack_DP_FU1_gen(rp_reg.io.insts_pack_DP(i))))
    iq1.io.insts_disp_index     := dp.io.insts_disp_index(0)
    iq1.io.insts_disp_valid     := dp.io.insts_disp_valid(0)
    iq1.io.prj_ready            := prj_ready
    iq1.io.prk_ready            := prk_ready
    iq1.io.issue_ack            := sel1.io.issue_ack
    iq1.io.flush                := rob.io.predict_fail_cmt
    iq1.io.stall                := stall_by_iq
    
    sel1.io.insts_issue         := iq1.io.insts_issue
    sel1.io.issue_req           := iq1.io.issue_req
    sel1.io.stall               := !(iq1.io.issue_req.reduce(_||_))

    // 2. arith2, calculate and branch 
    iq2.io.insts_dispatch       := VecInit(Seq.tabulate(4)(i => inst_pack_DP_FU2_gen(rp_reg.io.insts_pack_DP(i))))
    iq2.io.insts_disp_index     := dp.io.insts_disp_index(1)
    iq2.io.insts_disp_valid     := dp.io.insts_disp_valid(1)
    iq2.io.prj_ready            := prj_ready
    iq2.io.prk_ready            := prk_ready
    iq2.io.issue_ack            := sel2.io.issue_ack
    iq2.io.flush                := rob.io.predict_fail_cmt
    iq2.io.stall                := stall_by_iq 

    sel2.io.insts_issue         := iq2.io.insts_issue
    sel2.io.issue_req           := iq2.io.issue_req
    sel2.io.stall               := !(iq2.io.issue_req.reduce(_||_))

    // 3. load, load and store
    iq3.io.insts_dispatch       := VecInit(Seq.tabulate(4)(i => inst_pack_DP_LS_gen(rp_reg.io.insts_pack_DP(i))))
    iq3.io.insts_disp_index     := dp.io.insts_disp_index(2)
    iq3.io.insts_disp_valid     := dp.io.insts_disp_valid(2)
    iq3.io.prj_ready            := prj_ready
    iq3.io.prk_ready            := prk_ready
    iq3.io.issue_ack            := sel3.io.issue_ack
    iq3.io.flush                := rob.io.predict_fail_cmt
    iq3.io.stall                := stall_by_iq

    sel3.io.insts_issue         := iq3.io.insts_issue
    sel3.io.issue_req           := iq3.io.issue_req
    sel3.io.stall               := !(iq3.io.issue_req.reduce(_||_)) || sb.io.full

    // 4. multiply, multiply and divide
    iq4.io.insts_dispatch       := VecInit(Seq.tabulate(4)(i => inst_pack_DP_MD_gen(rp_reg.io.insts_pack_DP(i))))
    iq4.io.insts_disp_index     := dp.io.insts_disp_index(3)
    iq4.io.insts_disp_valid     := dp.io.insts_disp_valid(3)
    iq4.io.prj_ready            := prj_ready
    iq4.io.prk_ready            := prk_ready
    iq4.io.issue_ack            := sel4.io.issue_ack
    iq4.io.flush                := rob.io.predict_fail_cmt
    iq4.io.stall                := stall_by_iq 

    sel4.io.insts_issue         := iq4.io.insts_issue
    sel4.io.issue_req           := iq4.io.issue_req
    sel4.io.stall               := !(iq4.io.issue_req)

    // mutual wakeup
    val iq_inline_wake_preg     = VecInit(sel1.io.wake_preg, sel2.io.wake_preg, Mux(ir_reg3.io.inst_pack_RF.rd_valid, ir_reg3.io.inst_pack_RF.prd, 0.U), sel4.io.wake_preg)
    val iq_mutual_wake_preg     = VecInit(Mux(ir_reg1.io.inst_pack_RF.rd_valid, ir_reg1.io.inst_pack_RF.prd, 0.U),
                                          Mux(ir_reg2.io.inst_pack_RF.rd_valid, ir_reg2.io.inst_pack_RF.prd, 0.U),
                                          Mux(re_reg3.io.inst_pack_EX.rd_valid, re_reg3.io.inst_pack_EX.prd, 0.U),
                                          Mux(ir_reg4.io.inst_pack_RF.rd_valid, ir_reg4.io.inst_pack_RF.prd, 0.U))
    
    iq1.io.wake_preg            := VecInit(iq_inline_wake_preg(0), iq_inline_wake_preg(1), iq_mutual_wake_preg(2), iq_mutual_wake_preg(3))
    iq2.io.wake_preg            := VecInit(iq_inline_wake_preg(0), iq_inline_wake_preg(1), iq_mutual_wake_preg(2), iq_mutual_wake_preg(3))
    iq3.io.wake_preg            := VecInit(iq_mutual_wake_preg(0), iq_mutual_wake_preg(1), iq_inline_wake_preg(2), iq_mutual_wake_preg(3))
    iq4.io.wake_preg            := VecInit(iq_mutual_wake_preg(0), iq_mutual_wake_preg(1), iq_mutual_wake_preg(2), iq_inline_wake_preg(3))

    // IS-RF SegReg
    ir_reg1.io.flush         := rob.io.predict_fail_cmt
    ir_reg1.io.stall         := false.B
    ir_reg1.io.inst_pack_IS  := inst_pack_IS_FU1_gen(sel1.io.inst_issue.inst, sel1.io.inst_issue_valid)

    ir_reg2.io.flush         := rob.io.predict_fail_cmt
    ir_reg2.io.stall         := false.B
    ir_reg2.io.inst_pack_IS  := inst_pack_IS_FU2_gen(sel2.io.inst_issue.inst, sel2.io.inst_issue_valid)

    ir_reg3.io.flush         := rob.io.predict_fail_cmt
    ir_reg3.io.stall         := false.B
    ir_reg3.io.inst_pack_IS  := inst_pack_IS_LS_gen(sel3.io.inst_issue.inst, sel3.io.inst_issue_valid)

    ir_reg4.io.flush         := rob.io.predict_fail_cmt
    ir_reg4.io.stall         := false.B
    ir_reg4.io.inst_pack_IS  := inst_pack_IS_MD_gen(sel4.io.inst_issue.inst, sel4.io.inst_issue_valid)

    // RF stage
    rf.io.prj       := VecInit(ir_reg1.io.inst_pack_RF.prj, ir_reg2.io.inst_pack_RF.prj, ir_reg3.io.inst_pack_RF.prj, ir_reg4.io.inst_pack_RF.prj)
    rf.io.prk       := VecInit(ir_reg1.io.inst_pack_RF.prk, ir_reg2.io.inst_pack_RF.prk, ir_reg3.io.inst_pack_RF.prk, ir_reg4.io.inst_pack_RF.prk)
    rf.io.wdata     := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg3.io.mem_rdata_WB, ew_reg4.io.md_out_WB)
    rf.io.rf_we     := VecInit(ew_reg1.io.inst_pack_WB.rd_valid, ew_reg2.io.inst_pack_WB.rd_valid, ew_reg3.io.inst_pack_WB.rd_valid, ew_reg4.io.inst_pack_WB.rd_valid)
    rf.io.prd       := VecInit(ew_reg1.io.inst_pack_WB.prd, ew_reg2.io.inst_pack_WB.prd, ew_reg3.io.inst_pack_WB.prd, ew_reg4.io.inst_pack_WB.prd)

    // RF-EX SegReg
    re_reg1.io.flush         := rob.io.predict_fail_cmt
    re_reg1.io.stall         := false.B
    re_reg1.io.inst_pack_RF  := ir_reg1.io.inst_pack_RF
    re_reg1.io.src1_RF       := rf.io.prj_data(0)
    re_reg1.io.src2_RF       := rf.io.prk_data(0)

    re_reg2.io.flush         := rob.io.predict_fail_cmt
    re_reg2.io.stall         := false.B
    re_reg2.io.inst_pack_RF  := ir_reg2.io.inst_pack_RF
    re_reg2.io.src1_RF       := rf.io.prj_data(1)
    re_reg2.io.src2_RF       := rf.io.prk_data(1)

    re_reg3.io.flush         := rob.io.predict_fail_cmt
    re_reg3.io.stall         := false.B
    re_reg3.io.inst_pack_RF  := ir_reg3.io.inst_pack_RF
    re_reg3.io.src1_RF       := rf.io.prj_data(2)
    re_reg3.io.src2_RF       := rf.io.prk_data(2)

    re_reg4.io.flush         := rob.io.predict_fail_cmt
    re_reg4.io.stall         := false.B
    re_reg4.io.inst_pack_RF  := ir_reg4.io.inst_pack_RF
    re_reg4.io.src1_RF       := rf.io.prj_data(3)
    re_reg4.io.src2_RF       := rf.io.prk_data(3)

    // EX stage
    // 1. arith common fu
    alu1.io.alu_op := re_reg1.io.inst_pack_EX.alu_op
    alu1.io.src1 := MuxLookup(re_reg1.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG -> Mux(bypass12.io.forward_prj_en(0), bypass12.io.forward_prj_data(0), re_reg1.io.src1_EX),
        RS1_PC -> re_reg1.io.inst_pack_EX.pc,
        RS1_ZERO -> 0.U)
    )
    alu1.io.src2 := MuxLookup(re_reg1.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG -> Mux(bypass12.io.forward_prk_en(0), bypass12.io.forward_prk_data(0), re_reg1.io.src2_EX),
        RS2_IMM -> re_reg1.io.inst_pack_EX.imm,
        RS2_FOUR -> 4.U)
    )
    
    ew_reg1.io.flush          := rob.io.predict_fail_cmt
    ew_reg1.io.stall          := false.B
    ew_reg1.io.inst_pack_EX   := re_reg1.io.inst_pack_EX
    ew_reg1.io.alu_out_EX     := alu1.io.alu_out

    // 2. arith-branch fu
    alu2.io.alu_op := re_reg2.io.inst_pack_EX.alu_op
    alu2.io.src1 := MuxLookup(re_reg2.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG -> Mux(bypass12.io.forward_prj_en(1), bypass12.io.forward_prj_data(1), re_reg2.io.src1_EX),
        RS1_PC -> re_reg2.io.inst_pack_EX.pc,
        RS1_ZERO -> 0.U)
    )
    alu2.io.src2 := MuxLookup(re_reg2.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG -> Mux(bypass12.io.forward_prk_en(1), bypass12.io.forward_prk_data(1), re_reg2.io.src2_EX),
        RS2_IMM -> re_reg2.io.inst_pack_EX.imm,
        RS2_FOUR -> 4.U)
    )

    br.io.br_type       := re_reg2.io.inst_pack_EX.br_type
    br.io.src1          := Mux(bypass12.io.forward_prj_en(1), bypass12.io.forward_prj_data(1), re_reg2.io.src1_EX)
    br.io.src2          := Mux(bypass12.io.forward_prk_en(1), bypass12.io.forward_prk_data(1), re_reg2.io.src2_EX)
    br.io.pc_ex         := re_reg2.io.inst_pack_EX.pc
    br.io.imm_ex        := re_reg2.io.inst_pack_EX.imm
    br.io.predict_jump  := re_reg2.io.inst_pack_EX.predict_jump
    br.io.pred_npc      := re_reg2.io.inst_pack_EX.pred_npc

    bypass12.io.prd_wb        := VecInit(ew_reg1.io.inst_pack_WB.prd, ew_reg2.io.inst_pack_WB.prd)
    bypass12.io.prj_ex        := VecInit(re_reg1.io.inst_pack_EX.prj, re_reg2.io.inst_pack_EX.prj)
    bypass12.io.prk_ex        := VecInit(re_reg1.io.inst_pack_EX.prk, re_reg2.io.inst_pack_EX.prk)
    bypass12.io.prf_wdata_wb  := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB)
    bypass12.io.rd_valid_wb   := VecInit(ew_reg1.io.inst_pack_WB.rd_valid, ew_reg2.io.inst_pack_WB.rd_valid)

    ew_reg2.io.flush              := rob.io.predict_fail_cmt
    ew_reg2.io.stall              := false.B
    ew_reg2.io.inst_pack_EX       := re_reg2.io.inst_pack_EX
    ew_reg2.io.alu_out_EX         := alu2.io.alu_out
    ew_reg2.io.predict_fail_EX    := br.io.predict_fail
    ew_reg2.io.branch_target_EX   := br.io.branch_target
    ew_reg2.io.real_jump_EX       := br.io.real_jump

    // 3. load-store fu, include agu and cache
    ls_ex_reg.io.flush             := rob.io.predict_fail_cmt
    ls_ex_reg.io.stall             := false.B
    ls_ex_reg.io.inst_pack_EX1     := re_reg3.io.inst_pack_EX
    ls_ex_reg.io.mem_addr_EX1      := Mux(bypass3.io.forward_prj_en, bypass3.io.forward_prj_data, re_reg3.io.src1_EX) + re_reg3.io.inst_pack_EX.imm
    ls_ex_reg.io.mem_wdata_EX1     := Mux(bypass3.io.forward_prk_en, bypass3.io.forward_prk_data, re_reg3.io.src2_EX)

    bypass3.io.prd_wb        := ew_reg3.io.inst_pack_WB.prd
    bypass3.io.prj_ex        := re_reg3.io.inst_pack_EX.prj
    bypass3.io.prk_ex        := re_reg3.io.inst_pack_EX.prk
    bypass3.io.prf_wdata_wb  := ew_reg3.io.mem_rdata_WB
    bypass3.io.rd_valid_wb   := ew_reg3.io.inst_pack_WB.rd_valid

    // store_buf
    sb.io.flush             := rob.io.predict_fail_cmt
    sb.io.is_store_ex       := ls_ex_reg.io.inst_pack_EX2.mem_type(4)
    sb.io.addr_ex           := ls_ex_reg.io.mem_addr_EX2
    sb.io.st_data_ex        := ls_ex_reg.io.mem_wdata_EX2
    sb.io.st_wlen_ex        := ls_ex_reg.io.inst_pack_EX2.mem_type(2, 0)
    sb.io.is_store_num_cmt  := rob.io.is_store_num_cmt

    io.mem_raddr_ex      := ls_ex_reg.io.mem_addr_EX2
    io.mem_rlen_ex       := ls_ex_reg.io.inst_pack_EX2.mem_type(2, 0)
    io.mem_is_load_ex    := ls_ex_reg.io.inst_pack_EX2.mem_type(3)
    io.mem_waddr_cmt     := sb.io.st_addr_cmt
    io.mem_wdata_cmt     := sb.io.st_data_cmt
    io.mem_wlen_cmt      := sb.io.st_wlen_cmt
    io.mem_is_store_cmt  := sb.io.is_store_cmt

    val mem_rdata = Mux(sb.io.ld_hit, sb.io.ld_data_ex, io.mem_rdata_ex)
    ew_reg3.io.flush          := rob.io.predict_fail_cmt
    ew_reg3.io.stall          := false.B
    ew_reg3.io.inst_pack_EX2  := ls_ex_reg.io.inst_pack_EX2
    ew_reg3.io.mem_rdata_EX2  := MuxLookup(ls_ex_reg.io.inst_pack_EX2.mem_type, 0.U)(Seq(
        NO_MEM -> 0.U,
        MEM_LDB -> Cat(Fill(24, mem_rdata(7)), mem_rdata(7, 0)),
        MEM_LDBU -> Cat(Fill(24, 0.U), mem_rdata(7, 0)),
        MEM_LDH -> Cat(Fill(16, mem_rdata(15)), mem_rdata(15, 0)),
        MEM_LDHU -> Cat(Fill(16, 0.U), mem_rdata(15, 0)),
        MEM_LDW -> mem_rdata(31, 0)
    ))
    ew_reg3.io.is_ucread_EX2  := ls_ex_reg.io.mem_addr_EX2(31, 28) === 0xa.U

    // 4. multiply-divide fu
    mdu.io.md_op    := re_reg4.io.inst_pack_EX.alu_op
    mdu.io.src1     := Mux(bypass4.io.forward_prj_en, bypass4.io.forward_prj_data, re_reg4.io.src1_EX)
    mdu.io.src2     := Mux(bypass4.io.forward_prk_en, bypass4.io.forward_prk_data, re_reg4.io.src2_EX)

    ew_reg4.io.flush          := rob.io.predict_fail_cmt
    ew_reg4.io.stall          := false.B
    ew_reg4.io.inst_pack_EX   := re_reg4.io.inst_pack_EX
    ew_reg4.io.md_out_EX      := mdu.io.md_out

    bypass4.io.prd_wb        := ew_reg4.io.inst_pack_WB.prd
    bypass4.io.prj_ex        := re_reg4.io.inst_pack_EX.prj
    bypass4.io.prk_ex        := re_reg4.io.inst_pack_EX.prk
    bypass4.io.prf_wdata_wb  := ew_reg4.io.md_out_WB
    bypass4.io.rd_valid_wb   := ew_reg4.io.inst_pack_WB.rd_valid

    // WB stage
    val is_store_rn = VecInit(Seq.tabulate(4)(i => (dr_reg.io.insts_pack_RN(i).mem_type(4))))
    val br_type_pred = VecInit(Seq.tabulate(4)(i => Mux(dr_reg.io.insts_pack_RN(i).br_type === NO_BR, 3.U, 
                                                    Mux(dr_reg.io.insts_pack_RN(i).br_type === BR_JIRL && dr_reg.io.insts_pack_RN(i).rj === 1.U, 1.U(2.W), 
                                                    Mux(dr_reg.io.insts_pack_RN(i).br_type === BR_BL, 2.U(2.W), 0.U(2.W))))))
    rob.io.inst_valid_rn        := dr_reg.io.insts_pack_RN.map(_.inst_valid)
    rob.io.rd_rn                := dr_reg.io.insts_pack_RN.map(_.rd)
    rob.io.rd_valid_rn          := dr_reg.io.insts_pack_RN.map(_.rd_valid)
    rob.io.prd_rn               := rename.io.prd
    rob.io.pprd_rn              := rename.io.pprd
    rob.io.pc_rn                := dr_reg.io.insts_pack_RN.map(_.pc)
    rob.io.is_store_rn          := is_store_rn
    rob.io.stall                := dr_reg.io.stall
    rob.io.br_type_pred_rn      := br_type_pred

    rob.io.inst_valid_wb        := VecInit(ew_reg1.io.inst_pack_WB.inst_valid, ew_reg2.io.inst_pack_WB.inst_valid, ew_reg3.io.inst_pack_WB.inst_valid, ew_reg4.io.inst_pack_WB.inst_valid)
    rob.io.rob_index_wb         := VecInit(ew_reg1.io.inst_pack_WB.rob_index, ew_reg2.io.inst_pack_WB.rob_index, ew_reg3.io.inst_pack_WB.rob_index, ew_reg4.io.inst_pack_WB.rob_index)
    rob.io.predict_fail_wb      := VecInit(false.B, ew_reg2.io.predict_fail_WB, false.B, false.B)
    rob.io.real_jump_wb         := VecInit(false.B, ew_reg2.io.real_jump_WB, false.B, false.B)
    rob.io.branch_target_wb     := VecInit(0.U, ew_reg2.io.branch_target_WB, 0.U, 0.U)
    rob.io.rf_wdata_wb          := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg3.io.mem_rdata_WB, ew_reg4.io.md_out_WB)
    rob.io.is_ucread_wb         := VecInit(false.B, false.B, ew_reg3.io.is_ucread_WB, false.B)
    
    // Commit stage
    arat.io.cmt_en              := rob.io.cmt_en
    arat.io.prd_cmt             := rob.io.prd_cmt
    arat.io.pprd_cmt            := rob.io.pprd_cmt
    arat.io.rd_valid_cmt        := rob.io.rd_valid_cmt
    arat.io.predict_fail        := rob.io.predict_fail_cmt
    arat.io.br_type_pred_cmt    := rob.io.ras_type_pred_cmt
    arat.io.ras_update_en_cmt   := rob.io.ras_update_en_cmt


    // statitic
    if(System.getProperties().getProperty("mode") == "sim"){
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

        io.commit_stall_by_fetch_queue  := !fq.io.inst_queue_ready
        io.commit_stall_by_rename       := rename.io.free_list_empty
        io.commit_stall_by_rob          := rob.io.full
        io.commit_stall_by_iq1          := iq1.io.full  && !iq1.io.stall
        io.commit_stall_by_iq2          := iq2.io.full  && !iq2.io.stall
        io.commit_stall_by_iq3          := iq3.io.full  && !iq3.io.stall
        io.commit_stall_by_iq4          := iq4.io.full && !iq4.io.stall
        io.commit_stall_by_sb           := sb.io.full

        io.commit_iq1_issue             := sel1.io.inst_issue_valid
        io.commit_iq2_issue             := sel2.io.inst_issue_valid
        io.commit_iq3_issue             := sel3.io.inst_issue_valid
        io.commit_iq4_issue             := sel4.io.inst_issue_valid
    }
    else {
        io.commit_en1           := DontCare
        io.commit_rd1           := DontCare
        io.commit_prd1          := DontCare
        io.commit_rd_valid1     := DontCare
        io.commit_rf_wdata1     := DontCare
        io.commit_pc_1          := DontCare
        io.commit_is_ucread1    := DontCare
        io.commit_is_br1        := DontCare
        io.commit_br_type1      := DontCare
        io.commit_predict_fail1 := DontCare

        io.commit_en2           := DontCare
        io.commit_rd2           := DontCare
        io.commit_prd2          := DontCare
        io.commit_rd_valid2     := DontCare
        io.commit_rf_wdata2     := DontCare
        io.commit_pc_2          := DontCare
        io.commit_is_ucread2    := DontCare
        io.commit_is_br2        := DontCare
        io.commit_br_type2      := DontCare
        io.commit_predict_fail2 := DontCare

        io.commit_en3           := DontCare
        io.commit_rd3           := DontCare
        io.commit_prd3          := DontCare
        io.commit_rd_valid3     := DontCare
        io.commit_rf_wdata3     := DontCare
        io.commit_pc_3          := DontCare
        io.commit_is_ucread3    := DontCare
        io.commit_is_br3        := DontCare
        io.commit_br_type3      := DontCare
        io.commit_predict_fail3 := DontCare

        io.commit_en4           := DontCare
        io.commit_rd4           := DontCare
        io.commit_prd4          := DontCare
        io.commit_rd_valid4     := DontCare
        io.commit_rf_wdata4     := DontCare
        io.commit_pc_4          := DontCare
        io.commit_is_ucread4    := DontCare
        io.commit_is_br4        := DontCare
        io.commit_br_type4      := DontCare
        io.commit_predict_fail4 := DontCare

        io.commit_stall_by_fetch_queue  := DontCare
        io.commit_stall_by_rename       := DontCare
        io.commit_stall_by_rob          := DontCare
        io.commit_stall_by_iq1          := DontCare
        io.commit_stall_by_iq2          := DontCare
        io.commit_stall_by_iq3          := DontCare
        io.commit_stall_by_iq4          := DontCare
        io.commit_stall_by_sb           := DontCare

        io.commit_iq1_issue             := DontCare
        io.commit_iq2_issue             := DontCare
        io.commit_iq3_issue             := DontCare
        io.commit_iq4_issue             := DontCare

    }

}

