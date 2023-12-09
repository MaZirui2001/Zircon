import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._
object CPU_Config{
    val RESET_VEC   = 0x1c000000
    val PREG_NUM    = 56
    val ROB_NUM     = 32
    val SB_NUM      = 8
    val IQ_AR_NUM   = 8
    val IQ_AP_NUM   = 8
    val IQ_AB_NUM   = 8
    val IQ_MD_NUM   = 8
    val IQ_LS_NUM   = 8

}
import CPU_Config._
class CPU_IO extends Bundle{
    val araddr                      = Output(UInt(32.W))
    val arburst                     = Output(UInt(2.W))
    val arid                        = Output(UInt(4.W))
    val arlen                       = Output(UInt(8.W))  
    val arready                     = Input(Bool())
    val arsize                      = Output(UInt(3.W))
    val arvalid                     = Output(Bool())
    val awaddr                      = Output(UInt(32.W))
    val awburst                     = Output(UInt(2.W))
    val awid                        = Output(UInt(4.W))
    val awlen                       = Output(UInt(8.W))
    val awready                     = Input(Bool())
    val awsize                      = Output(UInt(3.W))
    val awvalid                     = Output(Bool())
    val bid                         = Input(UInt(4.W))
    val bready                      = Output(Bool())
    val bresp                       = Input(UInt(2.W))
    val bvalid                      = Input(Bool())
    val rdata                       = Input(UInt(32.W))
    val rid                         = Input(UInt(4.W))
    val rlast                       = Input(Bool())
    val rready                      = Output(Bool())
    val rresp                       = Input(UInt(2.W))
    val rvalid                      = Input(Bool())
    val wdata                       = Output(UInt(32.W))
    val wlast                       = Output(Bool())
    val wready                      = Input(Bool())
    val wstrb                       = Output(UInt(4.W))
    val wvalid                      = Output(Bool())

    // debug
    val commit_en                  = Output(Vec(4, Bool()))
    val commit_rd                  = Output(Vec(4, UInt(5.W)))
    val commit_prd                 = Output(Vec(4, UInt(log2Ceil(PREG_NUM).W)))
    val commit_rd_valid            = Output(Vec(4, Bool()))
    val commit_rf_wdata            = Output(Vec(4, UInt(32.W)))
    val commit_csr_wdata           = Output(Vec(4, UInt(32.W)))
    val commit_csr_we              = Output(Vec(4, Bool()))
    val commit_csr_waddr           = Output(Vec(4, UInt(14.W)))
    val commit_pc                  = Output(Vec(4, UInt(32.W)))
    val commit_is_ucread           = Output(Vec(4, Bool()))
    val commit_is_br               = Output(Vec(4, Bool()))
    val commit_br_type             = Output(Vec(4, UInt(2.W)))
    val commit_predict_fail        = Output(Vec(4, Bool()))

    val commit_stall_by_fetch_queue = Output(Bool())
    val commit_stall_by_rename      = Output(Bool())
    val commit_stall_by_rob         = Output(Bool())
    val commit_stall_by_iq          = Output(Vec(5, Bool()))
    val commit_stall_by_sb          = Output(Bool())
    val commit_stall_by_icache      = Output(Bool())
    val commit_stall_by_div         = Output(Bool())
    val commit_icache_miss          = Output(Bool())
    val commit_icache_visit         = Output(Bool())
    val commit_stall_by_dcache      = Output(Bool())
    val commit_dcache_miss          = Output(Bool())
    val commit_dcache_visit         = Output(Bool())
    
    val commit_iq_issue             = Output(Vec(5, Bool()))

}
class CPU extends Module {

    val io              = IO(new CPU_IO)
    val arb             = Module(new AXI_Arbiter)

    /* Previous Fetch Stage */
    val pc              = Module(new PC(RESET_VEC))
    val predict         = Module(new Predict)
    val pi_reg          = Module(new PF_IF_Reg)

    /* Inst Fetch Stage */
    val icache          = Module(new ICache)
    val ip_reg          = Module(new IF_PD_Reg)

    /* Previous Decode Stage */
    val pd              = Module(new Prev_Decode)
    val pf_reg          = Module(new PD_FQ_Reg)

    /* Fetch Queue Stage */
    val fq              = Module(new Fetch_Queue)

    /* Decode Stage */
    val decode          = VecInit(Seq.fill(4)(Module(new Decode).io))
    val dr_reg          = Module(new ID_RN_Reg)
    val free_list       = Module(new Free_List(PREG_NUM))

    /* Rename Stage */
    val rename          = Module(new Reg_Rename(PREG_NUM))
    val rp_reg          = Module(new RN_DP_Reg)

    /* Dispatch Stage */
    val dp              = Module(new Dispatch)
    val bd              = Module(new Busy_Board)
    
    /* Issue Stage */
    val iq1             = Module(new Unorder_Issue_Queue(IQ_AR_NUM, new inst_pack_DP_FU1_t))
    val sel1            = Module(new Unorder_Select(IQ_AR_NUM, new inst_pack_DP_FU1_t))
    val ir_reg1         = Module(new IS_RF_Reg(new inst_pack_IS_FU1_t))

    val iq2             = Module(new Unorder_Issue_Queue(IQ_AP_NUM, new inst_pack_DP_FU2_t))
    val sel2            = Module(new Unorder_Select(IQ_AP_NUM, new inst_pack_DP_FU2_t))
    val ir_reg2         = Module(new IS_RF_Reg(new inst_pack_IS_FU2_t))

    val iq3             = Module(new Unorder_Issue_Queue(IQ_AB_NUM, new inst_pack_DP_FU3_t))
    val sel3            = Module(new Unorder_Select(IQ_AB_NUM, new inst_pack_DP_FU3_t))
    val ir_reg3         = Module(new IS_RF_Reg(new inst_pack_IS_FU3_t))

    val iq4             = Module(new Unorder_Issue_Queue(IQ_MD_NUM, new inst_pack_DP_MD_t))
    val sel4            = Module(new Unorder_Select(IQ_MD_NUM, new inst_pack_DP_MD_t))
    val ir_reg4         = Module(new IS_RF_Reg(new inst_pack_IS_MD_t))

    val iq5             = Module(new Unorder_Issue_Queue(IQ_LS_NUM, new inst_pack_DP_LS_t))
    val sel5            = Module(new Unorder_Select(IQ_LS_NUM, new inst_pack_DP_LS_t))
    val ir_reg5         = Module(new IS_RF_Reg(new inst_pack_IS_LS_t))

    /* Regfile Read Stage */
    val rf              = Module(new Physical_Regfile(PREG_NUM))
    val csr_rf          = Module(new CSR_Regfile(5, 20, 30))

    val re_reg1         = Module(new RF_EX_Reg(new inst_pack_IS_FU1_t))
    val re_reg2         = Module(new RF_EX_Reg(new inst_pack_IS_FU2_t))
    val re_reg3         = Module(new RF_EX_Reg(new inst_pack_IS_FU3_t))
    val re_reg4         = Module(new RF_EX_Reg(new inst_pack_IS_MD_t))
    val re_reg5         = Module(new RF_EX_Reg(new inst_pack_IS_LS_t))

    /* Execute Stage */
    val alu1            = Module(new ALU)
    val ew_reg1         = Module(new FU1_EX_WB_Reg)

    val alu2            = Module(new ALU)
    val ew_reg2         = Module(new FU2_EX_WB_Reg)

    val alu3            = Module(new ALU)
    val br              = Module(new Branch)
    val ew_reg3         = Module(new FU3_EX_WB_Reg)

    val mdu             = Module(new MDU)
    val md_ex1_ex2_reg  = Module(new MD_EX1_EX2_Reg)
    val ew_reg4         = Module(new MD_EX_WB_Reg)

    val sb              = Module(new SB(SB_NUM))
    val dcache          = Module(new DCache)
    val ls_ex_mem_reg   = Module(new LS_EX_MEM_Reg) 
    val ew_reg5         = Module(new LS_EX2_WB_Reg)

    /* Write Back Stage */
    val rob             = Module(new ROB(ROB_NUM))
    val bypass123       = Module(new Bypass_3)

    /* Commit Stage */
    val arat            = Module(new Arch_Rat(PREG_NUM))

    val stall_by_iq = iq1.io.full || iq2.io.full || iq3.io.full || iq4.io.full || iq5.io.full

    /* ---------- 1. Previous Fetch Stage ---------- */
    // PC
    pc.io.pc_stall                  := fq.io.full || icache.io.cache_miss_RM
    pc.io.predict_fail              := rob.io.predict_fail_cmt(0)
    pc.io.branch_target             := rob.io.branch_target_cmt
    pc.io.pred_jump                 := predict.io.predict_jump
    pc.io.pred_npc                  := predict.io.pred_npc
    pc.io.flush_by_pd               := pf_reg.io.pred_fix_FQ
    pc.io.flush_pd_target           := pf_reg.io.pred_fix_target_FQ

    // Branch Prediction
    predict.io.npc                  := pc.io.npc
    predict.io.pc                   := pc.io.pc_PF
    predict.io.pc_cmt               := rob.io.pred_pc_cmt
    predict.io.real_jump            := rob.io.pred_real_jump_cmt
    predict.io.branch_target        := rob.io.pred_branch_target_cmt
    predict.io.update_en            := rob.io.pred_update_en_cmt
    predict.io.br_type              := rob.io.br_type_pred_cmt
    predict.io.predict_fail         := rob.io.predict_fail_cmt(0)
    predict.io.top_arch             := arat.io.top_arch
    predict.io.pd_pred_fix          := pf_reg.io.pred_fix_FQ
    predict.io.pd_pred_fix_is_bl    := pf_reg.io.pred_fix_is_bl_FQ
    predict.io.pd_pc_plus_4         := pf_reg.io.pred_fix_pc_plus_4_FQ
    predict.io.pc_stall             := pc.io.pc_stall

    /* ---------- PF-IF SegReg ---------- */
    val pcs_PF                  = VecInit(pc.io.pc_PF, pc.io.pc_PF+4.U, pc.io.pc_PF+8.U, pc.io.pc_PF+12.U)
    pi_reg.io.flush             := rob.io.predict_fail_cmt(0) || (!fq.io.full && pf_reg.io.pred_fix_FQ)
    pi_reg.io.stall             := fq.io.full || icache.io.cache_miss_RM
    pi_reg.io.inst_pack_PF      := VecInit.tabulate(4)(i => inst_pack_PF_gen(pcs_PF(i), pc.io.inst_valid_PF(i), predict.io.predict_jump(i), predict.io.pred_npc, predict.io.pred_valid(i)))

    /* ---------- 2. Inst Fetch Stage ---------- */
    // icache
    icache.io.addr_IF           := pc.io.pc_PF
    icache.io.rvalid_IF         := !reset.asBool
    icache.io.stall             := fq.io.full
    icache.io.flush             := false.B
    icache.io.i_rready          := arb.io.i_rready
    icache.io.i_rdata           := arb.io.i_rdata
    icache.io.i_rlast           := arb.io.i_rlast

    /* ---------- IF-PD SegReg ---------- */
    ip_reg.io.flush             := rob.io.predict_fail_cmt(1) || (!ip_reg.io.stall && (pf_reg.io.pred_fix_FQ || icache.io.cache_miss_RM))
    ip_reg.io.stall             := fq.io.full
    ip_reg.io.insts_pack_IF     := VecInit.tabulate(4)(i => inst_pack_IF_gen(pi_reg.io.inst_pack_IF(i), icache.io.rdata_RM(i)))

    /* ---------- 3. Previous Decode Stage ---------- */
    // Previous Decoder
    pd.io.insts_pack_IF             := ip_reg.io.insts_pack_PD  

    pf_reg.io.flush                 := rob.io.predict_fail_cmt(2) || !pf_reg.io.stall && (pf_reg.io.pred_fix_FQ)
    pf_reg.io.stall                 := fq.io.full
    pf_reg.io.insts_pack_PD         := VecInit.tabulate(4)(i => inst_pack_PD_gen(pd.io.insts_pack_PD(i)))
    pf_reg.io.pred_fix_PD           := pd.io.pred_fix
    pf_reg.io.pred_fix_target_PD    := pd.io.pred_fix_target
    pf_reg.io.pred_fix_is_bl_PD     := pd.io.pred_fix_is_bl
    pf_reg.io.pred_fix_pc_plus_4_PD := pd.io.pred_fix_pc_plus_4

    /* ---------- Fetch Queue ---------- */
    fq.io.insts_pack    := pf_reg.io.insts_pack_FQ
    fq.io.next_ready    := !(rob.io.full || stall_by_iq || free_list.io.empty)
    fq.io.flush         := rob.io.predict_fail_cmt(3)

    /* ---------- 4. Decode Stage ---------- */
    // Decode
    for(i <- 0 until 4){
        decode(i).inst := fq.io.insts_pack_id(i).inst
    }
    free_list.io.rd_valid           := decode.map(_.rd_valid)
    free_list.io.rename_en          := VecInit.tabulate(4)(i => fq.io.insts_valid_decode(i) && fq.io.next_ready)
    free_list.io.commit_en          := rob.io.cmt_en
    free_list.io.commit_pprd_valid  := (rob.io.rd_valid_cmt.asUInt & VecInit(rob.io.pprd_cmt.map(_ =/= 0.U)).asUInt).asBools
    free_list.io.commit_pprd        := rob.io.pprd_cmt
    free_list.io.predict_fail       := ShiftRegister(rob.io.predict_fail_cmt(3), 1, false.B, true.B)
    free_list.io.head_arch          := arat.io.head_arch

    /* ---------- ID-RN SegReg ---------- */
    dr_reg.io.flush          := rob.io.predict_fail_cmt(4) || (!dr_reg.io.stall && free_list.io.empty)
    dr_reg.io.stall          := rob.io.full || stall_by_iq
    dr_reg.io.insts_pack_ID  := VecInit.tabulate(4)(i => inst_pack_ID_gen(fq.io.insts_pack_id(i), fq.io.insts_valid_decode(i), decode(i).rj, decode(i).rj_valid, decode(i).rk, decode(i).rk_valid, 
                                                                            decode(i).rd, decode(i).rd_valid, decode(i).imm, decode(i).alu_op, decode(i).alu_rs1_sel, decode(i).alu_rs2_sel, 
                                                                            decode(i).br_type, decode(i).mem_type, decode(i).fu_id, decode(i).inst_exist, decode(i).priv_vec, decode(i).csr_addr))
    dr_reg.io.alloc_preg_ID  := free_list.io.alloc_preg
    /* ---------- 5. Rename Stage ---------- */
    // Rename
    rename.io.rj                := dr_reg.io.insts_pack_RN.map(_.rj)
    rename.io.rk                := dr_reg.io.insts_pack_RN.map(_.rk)
    rename.io.rd                := dr_reg.io.insts_pack_RN.map(_.rd)
    rename.io.rd_valid          := dr_reg.io.insts_pack_RN.map(_.rd_valid)
    rename.io.rename_en         := VecInit.tabulate(4)(i => dr_reg.io.insts_pack_RN(i).inst_valid && !dr_reg.io.stall)
    rename.io.predict_fail      := ShiftRegister(rob.io.predict_fail_cmt(4), 1, false.B, true.B)
    rename.io.arch_rat          := arat.io.arch_rat
    rename.io.alloc_preg        := dr_reg.io.alloc_preg_RN
    
    /* ---------- RN-DP SegReg ---------- */
    rp_reg.io.flush             := rob.io.predict_fail_cmt(5)
    rp_reg.io.stall             := stall_by_iq || rob.io.full
    rp_reg.io.insts_pack_RN     := VecInit.tabulate(4)(i => inst_pack_RN_gen(dr_reg.io.insts_pack_RN(i), rename.io.prj(i), rename.io.prk(i), rename.io.prd(i), rename.io.pprd(i), rename.io.prj_raw(i), rename.io.prk_raw(i)))

    /* ---------- 6. Dispatch Stage ---------- */
    // Dispatch
    dp.io.inst_packs            := rp_reg.io.insts_pack_DP
    dp.io.elem_num              := VecInit(iq1.io.elem_num, iq2.io.elem_num, iq3.io.elem_num)

    // busyboard
    bd.io.flush                 := rob.io.predict_fail_cmt(5)
    bd.io.prj                   := rp_reg.io.insts_pack_DP.map(_.prj)
    bd.io.rj_valid              := rp_reg.io.insts_pack_DP.map(_.rj_valid)
    bd.io.prk                   := rp_reg.io.insts_pack_DP.map(_.prk)
    bd.io.rk_valid              := rp_reg.io.insts_pack_DP.map(_.rk_valid)
    bd.io.prd_wake              := VecInit(sel1.io.wake_preg, sel2.io.wake_preg, sel3.io.wake_preg, md_ex1_ex2_reg.io.inst_pack_EX2.prd, re_reg5.io.inst_pack_EX.prd)
    bd.io.prd_wake_valid        := VecInit(sel1.io.inst_issue_valid, sel2.io.inst_issue_valid, sel3.io.inst_issue_valid, md_ex1_ex2_reg.io.inst_pack_EX2.rd_valid && !mdu.io.busy, re_reg5.io.inst_pack_EX.rd_valid && !dcache.io.cache_miss_MEM)
    bd.io.prd_disp              := rp_reg.io.insts_pack_DP.map(_.prd)
    bd.io.prd_disp_valid        := rp_reg.io.insts_pack_DP.map(_.rd_valid)
    val prj_ready               = VecInit.tabulate(4)(i => !rp_reg.io.insts_pack_DP(i).rj_valid || rp_reg.io.insts_pack_DP(i).prj === 0.U || !(rp_reg.io.insts_pack_DP(i).prj_raw || bd.io.prj_busy(i)))
    val prk_ready               = VecInit.tabulate(4)(i => !rp_reg.io.insts_pack_DP(i).rk_valid || rp_reg.io.insts_pack_DP(i).prk === 0.U || !(rp_reg.io.insts_pack_DP(i).prk_raw || bd.io.prk_busy(i)))
    
    // rob
    val is_store_dp             = VecInit.tabulate(4)(i => (rp_reg.io.insts_pack_DP(i).mem_type(4)))
    val br_type_pred            = VecInit.tabulate(4)(i => Mux(rp_reg.io.insts_pack_DP(i).br_type === BR_JIRL && rp_reg.io.insts_pack_DP(i).rd === 1.U, 3.U, 
                                                           Mux(rp_reg.io.insts_pack_DP(i).br_type === BR_JIRL && rp_reg.io.insts_pack_DP(i).rj === 1.U, 1.U(2.W), 
                                                           Mux(rp_reg.io.insts_pack_DP(i).br_type === BR_BL, 2.U(2.W), 0.U(2.W)))))
    val pred_update_en          = VecInit.tabulate(4)(i => (rp_reg.io.insts_pack_DP(i).br_type =/= NO_BR))
    rob.io.inst_valid_dp        := rp_reg.io.insts_pack_DP.map(_.inst_valid)
    rob.io.rd_dp                := rp_reg.io.insts_pack_DP.map(_.rd)
    rob.io.rd_valid_dp          := rp_reg.io.insts_pack_DP.map(_.rd_valid)
    rob.io.prd_dp               := rp_reg.io.insts_pack_DP.map(_.prd)
    rob.io.pprd_dp              := rp_reg.io.insts_pack_DP.map(_.pprd)
    rob.io.pc_dp                := rp_reg.io.insts_pack_DP.map(_.pc)
    rob.io.is_store_dp          := is_store_dp
    rob.io.stall                := rp_reg.io.stall
    rob.io.pred_update_en_dp    := pred_update_en
    rob.io.br_type_pred_dp      := br_type_pred
    rob.io.csr_addr_dp          := rp_reg.io.insts_pack_DP.map(_.csr_addr)
    rob.io.priv_vec_dp          := rp_reg.io.insts_pack_DP.map(_.priv_vec)
    
    
    /* ---------- 7. Issue Stage ---------- */
    // 1. arith1, common calculate
    // issue queue
    iq1.io.insts_dispatch       := VecInit.tabulate(4)(i => inst_pack_DP_FU1_gen(rp_reg.io.insts_pack_DP(i), rob.io.rob_index_dp(i)))
    iq1.io.insts_disp_index     := dp.io.insts_disp_index(0)
    iq1.io.insts_disp_valid     := dp.io.insts_disp_valid(0)
    iq1.io.prj_ready            := prj_ready
    iq1.io.prk_ready            := prk_ready
    iq1.io.issue_ack            := sel1.io.issue_ack
    iq1.io.flush                := rob.io.predict_fail_cmt(6)
    iq1.io.stall                := stall_by_iq || rob.io.full
    iq1.io.ld_mem_prd           := ls_ex_mem_reg.io.inst_pack_MEM.prd
    iq1.io.dcache_miss          := dcache.io.cache_miss_MEM || ShiftRegister(dcache.io.cache_miss_MEM, 1, false.B, true.B)

    // select
    sel1.io.insts_issue         := iq1.io.insts_issue
    sel1.io.issue_req           := iq1.io.issue_req
    sel1.io.stall               := !(iq1.io.issue_req.asUInt.orR) || ir_reg1.io.stall || ShiftRegister(ir_reg1.io.stall, 1, false.B, true.B)

    // 2. arith2, common calculate
    // issue queue
    iq2.io.insts_dispatch       := VecInit.tabulate(4)(i => inst_pack_DP_FU2_gen(rp_reg.io.insts_pack_DP(i), rob.io.rob_index_dp(i)))
    iq2.io.insts_disp_index     := dp.io.insts_disp_index(1)
    iq2.io.insts_disp_valid     := dp.io.insts_disp_valid(1)
    iq2.io.prj_ready            := prj_ready
    iq2.io.prk_ready            := prk_ready
    iq2.io.issue_ack            := sel2.io.issue_ack
    iq2.io.flush                := rob.io.predict_fail_cmt(6)
    iq2.io.stall                := stall_by_iq || rob.io.full
    iq2.io.ld_mem_prd           := ls_ex_mem_reg.io.inst_pack_MEM.prd
    iq2.io.dcache_miss          := dcache.io.cache_miss_MEM || ShiftRegister(dcache.io.cache_miss_MEM, 1, false.B, true.B)

    // select
    sel2.io.insts_issue         := iq2.io.insts_issue
    sel2.io.issue_req           := iq2.io.issue_req
    sel2.io.stall               := !(iq2.io.issue_req.asUInt.orR) || ir_reg2.io.stall || ShiftRegister(ir_reg2.io.stall, 1, false.B, true.B)

    // 3. arith3, calculate and branch
    // issue queue
    iq3.io.insts_dispatch       := VecInit.tabulate(4)(i => inst_pack_DP_FU3_gen(rp_reg.io.insts_pack_DP(i), rob.io.rob_index_dp(i)))
    iq3.io.insts_disp_index     := dp.io.insts_disp_index(2)
    iq3.io.insts_disp_valid     := dp.io.insts_disp_valid(2)
    iq3.io.prj_ready            := prj_ready
    iq3.io.prk_ready            := prk_ready
    iq3.io.issue_ack            := sel3.io.issue_ack
    iq3.io.flush                := rob.io.predict_fail_cmt(6)
    iq3.io.stall                := stall_by_iq || rob.io.full
    iq3.io.ld_mem_prd           := ls_ex_mem_reg.io.inst_pack_MEM.prd
    iq3.io.dcache_miss          := dcache.io.cache_miss_MEM || ShiftRegister(dcache.io.cache_miss_MEM, 1, false.B, true.B)

    // select
    sel3.io.insts_issue         := iq3.io.insts_issue
    sel3.io.issue_req           := iq3.io.issue_req
    sel3.io.stall               := !(iq3.io.issue_req.asUInt.orR) || ir_reg3.io.stall || ShiftRegister(ir_reg3.io.stall, 1, false.B, true.B)

    // 4. multiply, multiply and divide
    // issue queue
    iq4.io.insts_dispatch       := VecInit.tabulate(4)(i => inst_pack_DP_MD_gen(rp_reg.io.insts_pack_DP(i), rob.io.rob_index_dp(i)))
    iq4.io.insts_disp_index     := dp.io.insts_disp_index(3)
    iq4.io.insts_disp_valid     := dp.io.insts_disp_valid(3)
    iq4.io.prj_ready            := prj_ready
    iq4.io.prk_ready            := prk_ready
    iq4.io.issue_ack            := sel4.io.issue_ack
    iq4.io.flush                := rob.io.predict_fail_cmt(6)
    iq4.io.stall                := stall_by_iq || rob.io.full
    iq4.io.ld_mem_prd           := ls_ex_mem_reg.io.inst_pack_MEM.prd
    iq4.io.dcache_miss          := dcache.io.cache_miss_MEM || ShiftRegister(dcache.io.cache_miss_MEM, 1, false.B, true.B)

    // select
    sel4.io.insts_issue         := iq4.io.insts_issue
    sel4.io.issue_req           := iq4.io.issue_req
    sel4.io.stall               := !(iq4.io.issue_req.asUInt.orR) || ir_reg4.io.stall || ShiftRegister(ir_reg4.io.stall, 1, false.B, true.B)

    // 5. load and store
    // issue queue
    iq5.io.insts_dispatch       := VecInit.tabulate(4)(i => inst_pack_DP_LS_gen(rp_reg.io.insts_pack_DP(i), rob.io.rob_index_dp(i)))
    iq5.io.insts_disp_index     := dp.io.insts_disp_index(4)
    iq5.io.insts_disp_valid     := dp.io.insts_disp_valid(4)
    iq5.io.prj_ready            := prj_ready
    iq5.io.prk_ready            := prk_ready
    iq5.io.issue_ack            := sel5.io.issue_ack
    iq5.io.flush                := rob.io.predict_fail_cmt(6)
    iq5.io.stall                := stall_by_iq || rob.io.full
    iq5.io.ld_mem_prd           := ls_ex_mem_reg.io.inst_pack_MEM.prd
    iq5.io.dcache_miss          := dcache.io.cache_miss_MEM

    // select
    sel5.io.insts_issue         := iq5.io.insts_issue
    sel5.io.issue_req           := iq5.io.issue_req
    sel5.io.stall               := !(iq5.io.issue_req.asUInt.orR) || ir_reg5.io.stall || ShiftRegister(ir_reg5.io.stall, 1, false.B, true.B)

    // mutual wakeup
    val iq_inline_wake_preg     = VecInit(sel1.io.wake_preg, 
                                          sel2.io.wake_preg, 
                                          sel3.io.wake_preg,
                                          Mux(md_ex1_ex2_reg.io.inst_pack_EX2.rd_valid && !mdu.io.busy, md_ex1_ex2_reg.io.inst_pack_EX2.prd, 0.U),
                                          Mux(re_reg5.io.inst_pack_EX.rd_valid, re_reg5.io.inst_pack_EX.prd, 0.U))

    val iq_mutual_wake_preg     = VecInit(Mux(ir_reg1.io.inst_pack_RF.rd_valid, ir_reg1.io.inst_pack_RF.prd, 0.U),
                                          Mux(ir_reg2.io.inst_pack_RF.rd_valid, ir_reg2.io.inst_pack_RF.prd, 0.U),
                                          Mux(ir_reg3.io.inst_pack_RF.rd_valid, ir_reg3.io.inst_pack_RF.prd, 0.U),
                                          Mux(md_ex1_ex2_reg.io.inst_pack_EX2.rd_valid && !mdu.io.busy, md_ex1_ex2_reg.io.inst_pack_EX2.prd, 0.U),
                                          Mux(re_reg5.io.inst_pack_EX.rd_valid, re_reg5.io.inst_pack_EX.prd, 0.U))
    
    iq1.io.wake_preg            := VecInit(iq_inline_wake_preg(0), iq_inline_wake_preg(1), iq_inline_wake_preg(2), iq_mutual_wake_preg(3), iq_mutual_wake_preg(4))
    iq2.io.wake_preg            := VecInit(iq_inline_wake_preg(0), iq_inline_wake_preg(1), iq_inline_wake_preg(2), iq_mutual_wake_preg(3), iq_mutual_wake_preg(4))
    iq3.io.wake_preg            := VecInit(iq_inline_wake_preg(0), iq_inline_wake_preg(1), iq_inline_wake_preg(2), iq_mutual_wake_preg(3), iq_mutual_wake_preg(4))
    iq4.io.wake_preg            := VecInit(iq_mutual_wake_preg(0), iq_mutual_wake_preg(1), iq_mutual_wake_preg(2), iq_inline_wake_preg(3), iq_mutual_wake_preg(4))
    iq5.io.wake_preg            := VecInit(iq_mutual_wake_preg(0), iq_mutual_wake_preg(1), iq_mutual_wake_preg(2), iq_mutual_wake_preg(3), iq_inline_wake_preg(4))

    /* ---------- IS-RF SegReg ---------- */
    ir_reg1.io.flush         := rob.io.predict_fail_cmt(7)
    ir_reg1.io.stall         := false.B
    ir_reg1.io.inst_pack_IS  := inst_pack_IS_FU1_gen(sel1.io.inst_issue.inst, sel1.io.inst_issue_valid)

    ir_reg2.io.flush         := rob.io.predict_fail_cmt(7)
    ir_reg2.io.stall         := false.B
    ir_reg2.io.inst_pack_IS  := inst_pack_IS_FU2_gen(sel2.io.inst_issue.inst, sel2.io.inst_issue_valid)

    ir_reg3.io.flush         := rob.io.predict_fail_cmt(7)
    ir_reg3.io.stall         := false.B
    ir_reg3.io.inst_pack_IS  := inst_pack_IS_FU3_gen(sel3.io.inst_issue.inst, sel3.io.inst_issue_valid)

    ir_reg4.io.flush         := rob.io.predict_fail_cmt(7)
    ir_reg4.io.stall         := mdu.io.busy
    ir_reg4.io.inst_pack_IS  := inst_pack_IS_MD_gen(sel4.io.inst_issue.inst, sel4.io.inst_issue_valid)

    ir_reg5.io.flush         := rob.io.predict_fail_cmt(7)
    ir_reg5.io.stall         := sb.io.full && re_reg5.io.inst_pack_EX.mem_type(4) || dcache.io.cache_miss_MEM || (sb.io.st_cmt_valid && ir_reg5.io.inst_pack_RF.mem_type(3))
    ir_reg5.io.inst_pack_IS  := inst_pack_IS_LS_gen(sel5.io.inst_issue.inst, sel5.io.inst_issue_valid)

    /* ---------- 8. Regfile Read Stage ---------- */
    // Regfile
    rf.io.prj               := VecInit(ir_reg1.io.inst_pack_RF.prj, ir_reg2.io.inst_pack_RF.prj, ir_reg3.io.inst_pack_RF.prj, ir_reg4.io.inst_pack_RF.prj, ir_reg5.io.inst_pack_RF.prj)
    rf.io.prk               := VecInit(ir_reg1.io.inst_pack_RF.prk, ir_reg2.io.inst_pack_RF.prk, ir_reg3.io.inst_pack_RF.prk, ir_reg4.io.inst_pack_RF.prk, ir_reg5.io.inst_pack_RF.prk)
    rf.io.wdata             := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg3.io.alu_out_WB, ew_reg4.io.md_out_WB, ew_reg5.io.mem_rdata_WB)
    rf.io.rf_we             := VecInit(ew_reg1.io.inst_pack_WB.rd_valid, ew_reg2.io.inst_pack_WB.rd_valid, ew_reg3.io.inst_pack_WB.rd_valid, ew_reg4.io.inst_pack_WB.rd_valid, ew_reg5.io.inst_pack_WB.rd_valid)
    rf.io.prd               := VecInit(ew_reg1.io.inst_pack_WB.prd, ew_reg2.io.inst_pack_WB.prd, ew_reg3.io.inst_pack_WB.prd, ew_reg4.io.inst_pack_WB.prd, ew_reg5.io.inst_pack_WB.prd)

    // CSR Regfile
    csr_rf.io.raddr         := ir_reg2.io.inst_pack_RF.csr_addr
    csr_rf.io.wdata         := rob.io.csr_wdata_cmt
    csr_rf.io.waddr         := rob.io.csr_addr_cmt
    csr_rf.io.we            := rob.io.csr_we_cmt

    /* ---------- RF-EX SegReg ---------- */
    re_reg1.io.flush         := rob.io.predict_fail_cmt(8)
    re_reg1.io.stall         := false.B
    re_reg1.io.inst_pack_RF  := ir_reg1.io.inst_pack_RF
    re_reg1.io.src1_RF       := rf.io.prj_data(0)
    re_reg1.io.src2_RF       := rf.io.prk_data(0)
    re_reg1.io.csr_rdata_RF  := DontCare

    re_reg2.io.flush         := rob.io.predict_fail_cmt(8)
    re_reg2.io.stall         := false.B
    re_reg2.io.inst_pack_RF  := ir_reg2.io.inst_pack_RF
    re_reg2.io.src1_RF       := rf.io.prj_data(1)
    re_reg2.io.src2_RF       := rf.io.prk_data(1)
    re_reg2.io.csr_rdata_RF  := csr_rf.io.rdata

    re_reg3.io.flush         := rob.io.predict_fail_cmt(8)
    re_reg3.io.stall         := false.B
    re_reg3.io.inst_pack_RF  := ir_reg3.io.inst_pack_RF
    re_reg3.io.src1_RF       := rf.io.prj_data(2) 
    re_reg3.io.src2_RF       := rf.io.prk_data(2)
    re_reg3.io.csr_rdata_RF  := DontCare

    re_reg4.io.flush         := rob.io.predict_fail_cmt(8)
    re_reg4.io.stall         := mdu.io.busy
    re_reg4.io.inst_pack_RF  := ir_reg4.io.inst_pack_RF
    re_reg4.io.src1_RF       := rf.io.prj_data(3)
    re_reg4.io.src2_RF       := rf.io.prk_data(3)
    re_reg4.io.csr_rdata_RF  := DontCare

    re_reg5.io.flush         := rob.io.predict_fail_cmt(8) || !re_reg5.io.stall && (sb.io.st_cmt_valid && ir_reg5.io.inst_pack_RF.mem_type(3))
    re_reg5.io.stall         := sb.io.full && re_reg5.io.inst_pack_EX.mem_type(4) || dcache.io.cache_miss_MEM
    re_reg5.io.inst_pack_RF  := ir_reg5.io.inst_pack_RF
    re_reg5.io.src1_RF       := rf.io.prj_data(4) + ir_reg5.io.inst_pack_RF.imm
    re_reg5.io.src2_RF       := rf.io.prk_data(4)
    re_reg5.io.csr_rdata_RF  := DontCare

    /* ---------- 9. Execute Stage ---------- */
    // 1. arith common fu1
    // ALU
    alu1.io.alu_op := re_reg1.io.inst_pack_EX.alu_op
    alu1.io.src1 := MuxLookup(re_reg1.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG     -> Mux(bypass123.io.forward_prj_en(0), bypass123.io.forward_prj_data(0), re_reg1.io.src1_EX),
        RS1_PC      -> re_reg1.io.inst_pack_EX.pc,
        RS1_ZERO    -> 0.U)
    )
    alu1.io.src2 := MuxLookup(re_reg1.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG     -> Mux(bypass123.io.forward_prk_en(0), bypass123.io.forward_prk_data(0), re_reg1.io.src2_EX),
        RS2_IMM     -> re_reg1.io.inst_pack_EX.imm,
        RS2_FOUR    -> 4.U)
    )
    
    // 2. arith common fu2
    // ALU
    alu2.io.alu_op := re_reg2.io.inst_pack_EX.alu_op
    alu2.io.src1 := MuxLookup(re_reg2.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG     -> Mux(bypass123.io.forward_prj_en(1), bypass123.io.forward_prj_data(1), re_reg2.io.src1_EX),
        RS1_PC      -> re_reg2.io.inst_pack_EX.pc,
        RS1_ZERO    -> 0.U)
    )
    alu2.io.src2 := MuxLookup(re_reg2.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG     -> Mux(bypass123.io.forward_prk_en(1), bypass123.io.forward_prk_data(1), re_reg2.io.src2_EX),
        RS2_IMM     -> re_reg2.io.inst_pack_EX.imm,
        RS2_CSR     -> re_reg2.io.csr_rdata_EX)
    )
    // CSR 
    val csr_op = re_reg2.io.inst_pack_EX.priv_vec(2)
    val csr_src1 = Mux(bypass123.io.forward_prj_en(1), bypass123.io.forward_prj_data(1), re_reg2.io.src1_EX)
    val csr_src2 = Mux(bypass123.io.forward_prk_en(1), bypass123.io.forward_prk_data(1), re_reg2.io.src2_EX)
    val csr_wdata = Mux(csr_op, csr_src1 & csr_src2 | ~csr_src1 & re_reg2.io.csr_rdata_EX,csr_src2)

    // 3. arith-branch fu
    // ALU
    alu3.io.alu_op := re_reg3.io.inst_pack_EX.alu_op
    alu3.io.src1 := MuxLookup(re_reg3.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG     -> Mux(bypass123.io.forward_prj_en(2), bypass123.io.forward_prj_data(2), re_reg3.io.src1_EX),
        RS1_PC      -> re_reg3.io.inst_pack_EX.pc,
        RS1_ZERO    -> 0.U)
    )
    alu3.io.src2 := MuxLookup(re_reg3.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG     -> Mux(bypass123.io.forward_prk_en(2), bypass123.io.forward_prk_data(2), re_reg3.io.src2_EX),
        RS2_IMM     -> re_reg3.io.inst_pack_EX.imm,
        RS2_FOUR    -> 4.U)
    )

    // Branch
    br.io.br_type               := re_reg3.io.inst_pack_EX.br_type
    br.io.src1                  := Mux(bypass123.io.forward_prj_en(2), bypass123.io.forward_prj_data(2), re_reg3.io.src1_EX)
    br.io.src2                  := Mux(bypass123.io.forward_prk_en(2), bypass123.io.forward_prk_data(2), re_reg3.io.src2_EX)
    br.io.pc_ex                 := re_reg3.io.inst_pack_EX.pc
    br.io.imm_ex                := re_reg3.io.inst_pack_EX.imm
    br.io.predict_jump          := re_reg3.io.inst_pack_EX.predict_jump
    br.io.pred_npc              := re_reg3.io.inst_pack_EX.pred_npc

    // 4. multiply-divide fu
    mdu.io.md_op                := re_reg4.io.inst_pack_EX.alu_op
    mdu.io.src1                 := re_reg4.io.src1_EX
    mdu.io.src2                 := re_reg4.io.src2_EX

    md_ex1_ex2_reg.io.flush         := rob.io.predict_fail_cmt(8)
    md_ex1_ex2_reg.io.stall         := mdu.io.busy
    md_ex1_ex2_reg.io.inst_pack_EX1 := re_reg4.io.inst_pack_EX

    // 5. load-store fu, include cache
    // EX stage
    // store_buf
    sb.io.flush                         := rob.io.predict_fail_cmt(8)
    sb.io.addr_ex                       := re_reg5.io.src1_EX
    sb.io.st_data_ex                    := re_reg5.io.src2_EX
    sb.io.mem_type_ex                   := Mux(dcache.io.cache_miss_MEM, 0.U, re_reg5.io.inst_pack_EX.mem_type)
    sb.io.is_store_num_cmt              := rob.io.is_store_num_cmt
    sb.io.dcache_miss                   := dcache.io.cache_miss_MEM

    // EX-MEM SegReg
    ls_ex_mem_reg.io.flush              := rob.io.predict_fail_cmt(8) || (!ls_ex_mem_reg.io.stall && sb.io.full && re_reg5.io.inst_pack_EX.mem_type(4))
    ls_ex_mem_reg.io.stall              := dcache.io.cache_miss_MEM
    ls_ex_mem_reg.io.inst_pack_EX       := re_reg5.io.inst_pack_EX
    ls_ex_mem_reg.io.mem_type_EX        := re_reg5.io.inst_pack_EX.mem_type
    ls_ex_mem_reg.io.sb_hit_EX          := sb.io.ld_hit
    ls_ex_mem_reg.io.sb_rdata_EX        := sb.io.ld_data_ex
    ls_ex_mem_reg.io.sb_st_cmt_valid_EX := sb.io.st_cmt_valid
    ls_ex_mem_reg.io.is_ucread_EX       := re_reg5.io.src1_EX(31, 28) === 0xa.U

    // MEM Stage
    // dcache
    dcache.io.addr_RF             := Mux(sb.io.st_cmt_valid, sb.io.st_addr_cmt, re_reg5.io.src1_RF)
    dcache.io.mem_type_RF         := Mux(sb.io.st_cmt_valid, 4.U(3.W) ## sb.io.st_wlen_cmt(1, 0), 
                                     Mux(re_reg5.io.inst_pack_RF.mem_type(3), re_reg5.io.inst_pack_RF.mem_type, 0.U))
    dcache.io.wdata_RF            := sb.io.st_data_cmt
    dcache.io.stall               := false.B
    dcache.io.d_rready            := arb.io.d_rready
    dcache.io.d_rdata             := arb.io.d_rdata
    dcache.io.d_rlast             := arb.io.d_rlast
    dcache.io.d_wready            := arb.io.d_wready
    dcache.io.d_bvalid            := arb.io.d_bvalid

    val mem_rdata_raw              = VecInit.tabulate(32)(i => Mux(ls_ex_mem_reg.io.sb_hit_MEM(i.U(4, 3)), ls_ex_mem_reg.io.sb_rdata_MEM(i), dcache.io.rdata_MEM(i))).asUInt 
    val mem_rdata                  = MuxLookup(ls_ex_mem_reg.io.mem_type_MEM, 0.U)(Seq(
                                                MEM_LDB     -> Fill(24, mem_rdata_raw(7)) ## mem_rdata_raw(7, 0),
                                                MEM_LDH     -> Fill(16, mem_rdata_raw(15)) ## mem_rdata_raw(15, 0),
                                                MEM_LDW     -> mem_rdata_raw,
                                                MEM_LDBU    -> 0.U(24.W) ## mem_rdata_raw(7, 0),
                                                MEM_LDHU    -> 0.U(16.W) ## mem_rdata_raw(15, 0)
                                    ))

    // bypass for 1, 2, 3
    bypass123.io.prd_wb             := VecInit(ew_reg1.io.inst_pack_WB.prd, ew_reg2.io.inst_pack_WB.prd, ew_reg3.io.inst_pack_WB.prd)
    bypass123.io.prj_ex             := VecInit(re_reg1.io.inst_pack_EX.prj, re_reg2.io.inst_pack_EX.prj, re_reg3.io.inst_pack_EX.prj)
    bypass123.io.prk_ex             := VecInit(re_reg1.io.inst_pack_EX.prk, re_reg2.io.inst_pack_EX.prk, re_reg3.io.inst_pack_EX.prk)
    bypass123.io.prf_wdata_wb       := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg3.io.alu_out_WB)
    bypass123.io.rd_valid_wb        := VecInit(ew_reg1.io.inst_pack_WB.rd_valid, ew_reg2.io.inst_pack_WB.rd_valid, ew_reg3.io.inst_pack_WB.rd_valid)


    /* ---------- EX-WB SegReg ---------- */
    ew_reg1.io.flush                := rob.io.predict_fail_cmt(9)
    ew_reg1.io.stall                := false.B
    ew_reg1.io.inst_pack_EX         := re_reg1.io.inst_pack_EX
    ew_reg1.io.alu_out_EX           := alu1.io.alu_out

    ew_reg2.io.flush                := rob.io.predict_fail_cmt(9)
    ew_reg2.io.stall                := false.B
    ew_reg2.io.inst_pack_EX         := re_reg2.io.inst_pack_EX
    ew_reg2.io.alu_out_EX           := alu2.io.alu_out
    ew_reg2.io.csr_wdata_EX         := csr_wdata

    ew_reg3.io.flush                := rob.io.predict_fail_cmt(9)
    ew_reg3.io.stall                := false.B
    ew_reg3.io.inst_pack_EX         := re_reg3.io.inst_pack_EX
    ew_reg3.io.alu_out_EX           := alu3.io.alu_out
    ew_reg3.io.predict_fail_EX      := br.io.predict_fail
    ew_reg3.io.branch_target_EX     := br.io.branch_target
    ew_reg3.io.real_jump_EX         := br.io.real_jump

    ew_reg4.io.flush                := rob.io.predict_fail_cmt(9) || mdu.io.busy
    ew_reg4.io.stall                := false.B
    ew_reg4.io.inst_pack_EX         := md_ex1_ex2_reg.io.inst_pack_EX2
    ew_reg4.io.md_out_EX            := Mux(md_ex1_ex2_reg.io.inst_pack_EX2.alu_op <= 13.U, mdu.io.mul_out, mdu.io.div_out)

    ew_reg5.io.flush                := rob.io.predict_fail_cmt(9) || dcache.io.cache_miss_MEM
    ew_reg5.io.stall                := false.B  
    ew_reg5.io.inst_pack_EX2        := ls_ex_mem_reg.io.inst_pack_MEM
    ew_reg5.io.mem_rdata_EX2        := mem_rdata
    ew_reg5.io.is_ucread_EX2        := ls_ex_mem_reg.io.is_ucread_MEM

    /* ---------- 10. Write Back Stage ---------- */
    // rob
    rob.io.inst_valid_wb        := VecInit(ew_reg1.io.inst_pack_WB.inst_valid && !ew_reg1.io.stall, ew_reg2.io.inst_pack_WB.inst_valid && !ew_reg2.io.stall, ew_reg3.io.inst_pack_WB.inst_valid && !ew_reg3.io.stall, ew_reg4.io.inst_pack_WB.inst_valid && !ew_reg4.io.stall, ew_reg5.io.inst_pack_WB.inst_valid && !ew_reg5.io.stall)
    rob.io.rob_index_wb         := VecInit(ew_reg1.io.inst_pack_WB.rob_index, ew_reg2.io.inst_pack_WB.rob_index, ew_reg3.io.inst_pack_WB.rob_index, ew_reg4.io.inst_pack_WB.rob_index, ew_reg5.io.inst_pack_WB.rob_index)
    rob.io.predict_fail_wb      := VecInit(false.B, false.B, ew_reg3.io.predict_fail_WB, false.B, false.B)
    rob.io.real_jump_wb         := VecInit(false.B, false.B, ew_reg3.io.real_jump_WB, false.B, false.B)
    rob.io.branch_target_wb     := VecInit(DontCare, ew_reg2.io.csr_wdata_WB, ew_reg3.io.branch_target_WB, DontCare, DontCare)
    rob.io.rf_wdata_wb          := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg3.io.alu_out_WB, ew_reg4.io.md_out_WB, ew_reg5.io.mem_rdata_WB)
    rob.io.is_ucread_wb         := VecInit(false.B, false.B, false.B, false.B, ew_reg5.io.is_ucread_WB)
    
    /* ---------- 11. Commit Stage ---------- */
    // Arch Rat
    arat.io.cmt_en              := rob.io.cmt_en
    arat.io.prd_cmt             := rob.io.prd_cmt
    arat.io.pprd_cmt            := rob.io.pprd_cmt
    arat.io.rd_valid_cmt        := rob.io.rd_valid_cmt
    arat.io.predict_fail        := rob.io.predict_fail_cmt(9)
    arat.io.br_type_pred_cmt    := rob.io.br_type_pred_cmt
    arat.io.pred_update_en_cmt  := rob.io.pred_update_en_cmt

    // arbiter
    arb.io.i_araddr             := icache.io.i_araddr
    arb.io.i_rvalid             := icache.io.i_rvalid
    arb.io.i_rsize              := icache.io.i_rsize
    arb.io.i_rburst             := icache.io.i_rburst
    arb.io.i_rlen               := icache.io.i_rlen

    arb.io.d_araddr             := dcache.io.d_araddr
    arb.io.d_rvalid             := dcache.io.d_rvalid
    arb.io.d_rsize              := dcache.io.d_rsize
    arb.io.d_rburst             := dcache.io.d_rburst
    arb.io.d_rlen               := dcache.io.d_rlen
    arb.io.d_awaddr             := dcache.io.d_awaddr
    arb.io.d_wvalid             := dcache.io.d_wvalid
    arb.io.d_wdata              := dcache.io.d_wdata
    arb.io.d_wlast              := dcache.io.d_wlast
    arb.io.d_wsize              := dcache.io.d_wsize
    arb.io.d_wburst             := dcache.io.d_wburst
    arb.io.d_wlen               := dcache.io.d_wlen
    arb.io.d_wstrb              := dcache.io.d_wstrb
    arb.io.d_bready             := dcache.io.d_bready


    io.araddr                   := arb.io.araddr  
    io.arburst                  := arb.io.arburst
    io.arid                     := arb.io.arid
    io.arlen                    := arb.io.arlen
    arb.io.arready              := io.arready
    io.arsize                   := arb.io.arsize
    io.arvalid                  := arb.io.arvalid

    io.awaddr                   := arb.io.awaddr
    io.awburst                  := arb.io.awburst
    io.awid                     := arb.io.awid
    io.awlen                    := arb.io.awlen
    arb.io.awready              := io.awready
    io.awsize                   := arb.io.awsize
    io.awvalid                  := arb.io.awvalid

    arb.io.bid                  := io.bid
    io.bready                   := arb.io.bready
    arb.io.bresp                := io.bresp
    arb.io.bvalid               := io.bvalid

    arb.io.rdata                := io.rdata
    arb.io.rid                  := io.rid
    arb.io.rlast                := io.rlast
    io.rready                   := arb.io.rready
    arb.io.rresp                := io.rresp
    arb.io.rvalid               := io.rvalid

    io.wdata                    := arb.io.wdata
    io.wlast                    := arb.io.wlast
    arb.io.wready               := io.wready
    io.wstrb                    := arb.io.wstrb
    io.wvalid                   := arb.io.wvalid


    // statitic
    if(System.getProperties().getProperty("mode") == "sim"){
        io.commit_en           := rob.io.cmt_en
        io.commit_rd           := rob.io.rd_cmt
        io.commit_prd          := rob.io.prd_cmt
        io.commit_rd_valid     := rob.io.rd_valid_cmt
        io.commit_rf_wdata     := rob.io.rf_wdata_cmt
        io.commit_csr_wdata    := rob.io.csr_diff_wdata_cmt
        io.commit_csr_we       := rob.io.csr_diff_we_cmt
        io.commit_csr_waddr    := rob.io.csr_diff_addr_cmt
        io.commit_pc           := rob.io.pc_cmt
        io.commit_is_ucread    := rob.io.is_ucread_cmt
        io.commit_is_br        := rob.io.is_br_stat
        io.commit_br_type      := rob.io.br_type_stat
        io.commit_predict_fail := rob.io.predict_fail_stat


        io.commit_stall_by_fetch_queue  := fq.io.full
        io.commit_stall_by_rename       := free_list.io.empty
        io.commit_stall_by_rob          := rob.io.full
        io.commit_stall_by_iq           := VecInit(iq1.io.full, iq2.io.full, iq3.io.full, iq4.io.full, iq5.io.full)
        io.commit_stall_by_sb           := sb.io.full

        io.commit_stall_by_icache       := icache.io.cache_miss_RM
        io.commit_stall_by_dcache       := dcache.io.cache_miss_MEM
        io.commit_icache_miss           := icache.io.commit_icache_miss
        io.commit_dcache_miss           := dcache.io.commit_dcache_miss
        io.commit_icache_visit          := icache.io.commit_icache_visit
        io.commit_dcache_visit          := dcache.io.commit_dcache_visit
        io.commit_stall_by_div          := mdu.io.busy

        io.commit_iq_issue             := VecInit(sel1.io.inst_issue_valid, sel2.io.inst_issue_valid, sel3.io.inst_issue_valid, sel4.io.inst_issue_valid, sel5.io.inst_issue_valid)



    }
    else {
        io.commit_en            := DontCare
        io.commit_rd            := DontCare
        io.commit_prd           := DontCare
        io.commit_rd_valid      := DontCare
        io.commit_rf_wdata      := DontCare
        io.commit_csr_wdata     := DontCare
        io.commit_csr_we        := DontCare
        io.commit_csr_waddr     := DontCare
        io.commit_pc            := DontCare
        io.commit_is_ucread     := DontCare
        io.commit_is_br         := DontCare
        io.commit_br_type       := DontCare
        io.commit_predict_fail  := DontCare


        io.commit_stall_by_fetch_queue  := DontCare
        io.commit_stall_by_rename       := DontCare
        io.commit_stall_by_rob          := DontCare
        io.commit_stall_by_iq           := DontCare
        io.commit_stall_by_sb           := DontCare

        io.commit_stall_by_icache       := DontCare
        io.commit_stall_by_dcache       := DontCare
        io.commit_icache_miss           := DontCare
        io.commit_dcache_miss           := DontCare
        io.commit_icache_visit          := DontCare
        io.commit_dcache_visit          := DontCare
        io.commit_stall_by_div          := DontCare

        io.commit_iq_issue              := DontCare

    }

}

