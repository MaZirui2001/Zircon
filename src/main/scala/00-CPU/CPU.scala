import chisel3._
import chisel3.util._
import Inst_Pack._
import Control_Signal._
import TLB_Struct._


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
    val commit_en                   = Output(Vec(2, Bool()))
    val commit_rd                   = Output(Vec(2, UInt(5.W)))
    val commit_prd                  = Output(Vec(2, UInt(log2Ceil(PREG_NUM).W)))
    val commit_rd_valid             = Output(Vec(2, Bool()))
    val commit_rf_wdata             = Output(Vec(2, UInt(32.W)))
    val commit_csr_wdata            = Output(Vec(2, UInt(32.W)))
    val commit_csr_we               = Output(Vec(2, Bool()))
    val commit_csr_waddr            = Output(Vec(2, UInt(14.W)))
    val commit_pc                   = Output(Vec(2, UInt(32.W)))
    val commit_is_ucread            = Output(Vec(2, Bool()))
    val commit_is_br                = Output(Vec(2, Bool()))
    val commit_br_type              = Output(Vec(2, UInt(2.W)))
    val commit_predict_fail         = Output(Vec(2, Bool()))
    val commit_inst                 = Output(Vec(2, UInt(32.W)))
    val commit_interrupt            = Output(Bool())
    val commit_interrupt_type       = Output(UInt(13.W))

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
    val commit_tlbfill_en           = Output(Bool())
    val commit_tlbfill_idx          = Output(UInt(4.W))

}
class CPU extends Module {
    val io                          = IO(new CPU_IO)
    val arb                         = Module(new AXI_Arbiter)

    /* Previous Fetch Stage */
    val pc                          = Module(new PC(RESET_VEC))
    val predict                     = Module(new Predict)
    val pi_reg                      = Module(new PF_IF_Reg)

    /* Inst Fetch Stage */
    val icache                      = Module(new ICache)
    val ip_reg                      = Module(new IF_PD_Reg)

    /* Previous Decode Stage */
    val pd                          = Module(new Prev_Decode)
    val fq                          = Module(new Fetch_Queue)

    /* Decode Stage */          
    val decode                      = VecInit.fill(2)(Module(new Decode).io)
    val dr_reg                      = Module(new ID_RN_Reg)
    val free_list                   = Module(new Free_List(PREG_NUM))

    /* Rename Stage */          
    val rename                      = Module(new Reg_Rename(PREG_NUM))

    /* Dispatch Stage */            
    val dp                          = Module(new Dispatch)
    
    /* Issue Stage */
    val iq1                         = Module(new Unorder_Issue_Queue(IQ_AP_NUM, new inst_pack_DP_FU1_t))
    val sel1                        = Module(new Unorder_Select(IQ_AP_NUM, new inst_pack_DP_FU1_t))
    val ir_reg1                     = Module(new IS_RF_Reg(new inst_pack_IS_FU1_t))

    val iq2                         = Module(new Unorder_Issue_Queue(IQ_AB_NUM, new inst_pack_DP_FU2_t))
    val sel2                        = Module(new Unorder_Select(IQ_AB_NUM, new inst_pack_DP_FU2_t))
    val ir_reg2                     = Module(new IS_RF_Reg(new inst_pack_IS_FU2_t))


    val iq3                         = Module(new Order_Issue_Queue(IQ_MD_NUM, new inst_pack_DP_MD_t))
    val sel3                        = Module(new Order_Select(IQ_MD_NUM, new inst_pack_DP_MD_t))
    val ir_reg3                     = Module(new IS_RF_Reg(new inst_pack_IS_MD_t))

    val iq4                         = Module(new Order_Issue_Queue(IQ_LS_NUM, new inst_pack_DP_LS_t))
    val sel4                        = Module(new Order_Select(IQ_LS_NUM, new inst_pack_DP_LS_t))
    val ir_reg4                     = Module(new LS_RF_EX_Reg(new inst_pack_IS_LS_t))

    /* Regfile Read Stage */
    val rf                          = Module(new Physical_Regfile(PREG_NUM))
    val csr_rf                      = Module(new CSR_Regfile(32, 30))
    val stable_cnt                  = Module(new Stable_Counter)

    val re_reg1                     = Module(new RF_EX_Reg(new inst_pack_IS_FU1_t))
    val re_reg2                     = Module(new RF_EX_Reg(new inst_pack_IS_FU2_t))
    val re_reg3                     = Module(new RF_EX_Reg(new inst_pack_IS_MD_t))
    val re_reg4                     = Module(new RF_EX_Reg(new inst_pack_IS_LS_t))

    /* Execute Stage */
    val alu1                        = Module(new ALU)
    val ew_reg1                     = Module(new FU1_EX_WB_Reg)

    val alu2                        = Module(new ALU)
    val br                          = Module(new Branch)
    val ew_reg2                     = Module(new FU2_EX_WB_Reg)

    val mdu                         = Module(new MDU)
    val md_ex1_ex2_reg              = Module(new MD_EX1_EX2_Reg)
    val md_ex2_ex3_reg              = Module(new MD_EX1_EX2_Reg)
    val ew_reg3                     = Module(new MD_EX_WB_Reg)

    val exception_ls                = Module(new Exception_LS)
    val mmu                         = Module(new MMU)
    val sb                          = Module(new SB(SB_NUM))
    val dcache                      = Module(new DCache)
    val ls_ex_mem_reg               = Module(new LS_EX_MEM_Reg) 
    val ew_reg4                     = Module(new LS_EX2_WB_Reg)

    /* Write Back Stage */
    val rob                         = Module(new ROB(ROB_NUM))
    val bypass                      = Module(new Bypass_3)

    /* Commit Stage */          
    val arat                        = Module(new Arch_Rat(PREG_NUM))

    val stall_by_iq                 = iq1.io.full || iq2.io.full || iq3.io.full || iq4.io.full

    /* ---------- 1. Previous Fetch Stage ---------- */
    // PC
    pc.io.pc_stall                  := fq.io.full || icache.io.cache_miss_RM || icache.io.has_cacop_IF
    pc.io.predict_fail              := rob.io.predict_fail_cmt(0)
    pc.io.branch_target             := rob.io.branch_target_cmt
    pc.io.pred_jump                 := predict.io.predict_jump
    pc.io.pred_npc                  := predict.io.pred_npc
    pc.io.flush_by_pd               := pd.io.pred_fix
    pc.io.flush_pd_target           := pd.io.pred_fix_target
    pc.io.is_idle_cmt               := rob.io.idle_en_cmt
    pc.io.has_intr                  := ShiftRegister(csr_rf.io.interrupt_vec.orR, 1, false.B, true.B)
    pc.io.has_csr_change            := ShiftRegister(rob.io.tlbrd_en_cmt || rob.io.csr_we_cmt, 1, false.B, true.B)
    
    // Branch Prediction
    predict.io.npc                  := pc.io.npc
    predict.io.pc                   := pc.io.pc_PF
    predict.io.pc_cmt               := rob.io.pred_pc_cmt
    predict.io.real_jump            := rob.io.pred_real_jump_cmt
    predict.io.branch_target        := rob.io.pred_branch_target_cmt
    predict.io.update_en            := rob.io.pred_update_en_cmt
    predict.io.br_type              := rob.io.pred_br_type_cmt
    predict.io.predict_fail         := rob.io.predict_fail_cmt(0)
    predict.io.top_arch             := arat.io.top_arch
    predict.io.pd_pred_fix          := pd.io.pred_fix
    predict.io.pd_pred_fix_is_bl    := pd.io.pred_fix_is_bl
    predict.io.pd_pc_plus_4         := pd.io.pred_fix_pc
    predict.io.pc_stall             := pc.io.pc_stall
    predict.io.ras_arch             := arat.io.ras_arch

    // mmu
    mmu.io.i_valid                  := !reset.asBool 
    mmu.io.i_vaddr                  := pc.io.pc_PF(7)
    mmu.io.i_stall                  := pi_reg.io.stall

    /* ---------- PF-IF SegReg ---------- */
    val pcs_PF                      = VecInit(pc.io.pc_PF(8), pc.io.pc_PF(8) + 4.U)
    pi_reg.io.flush                 := rob.io.predict_fail_cmt(0) || (!fq.io.full && pd.io.pred_fix)
    pi_reg.io.stall                 := fq.io.full || icache.io.cache_miss_RM || icache.io.has_cacop_IF
    pi_reg.io.inst_pack_PF          := VecInit.tabulate(2)(i => inst_pack_PF_gen(pcs_PF(i), pc.io.inst_valid_PF(i), predict.io.predict_jump(i), predict.io.pred_npc, predict.io.pred_valid(i), pc.io.exception_PF))

    /* ---------- 2. Inst Fetch Stage ---------- */
    // icache
    icache.io.addr_IF               := Mux(RegNext(re_reg4.io.inst_pack_EX.priv_vec(0)), RegNext(re_reg4.io.src1_EX), pc.io.pc_PF(9))
    icache.io.rvalid_IF             := !reset.asBool 
    icache.io.paddr_IF              := Mux(RegNext(re_reg4.io.inst_pack_EX.priv_vec(0)), RegNext(mmu.io.d_paddr), mmu.io.i_paddr)
    icache.io.uncache_IF            := mmu.io.i_uncache 
    icache.io.stall                 := fq.io.full
    icache.io.flush                 := false.B
    icache.io.i_rready              := arb.io.i_rready
    icache.io.i_rdata               := arb.io.i_rdata
    icache.io.i_rlast               := arb.io.i_rlast
    icache.io.cacop_en              := RegNext(re_reg4.io.inst_pack_EX.priv_vec(0)) && RegNext(re_reg4.io.inst_pack_EX.imm(2, 0) === 0.U)
    icache.io.cacop_op              := RegNext(re_reg4.io.inst_pack_EX.imm(4, 3))
    icache.io.exception_RM          := mmu.io.i_exception(7)

    /* ---------- IF-PD SegReg ---------- */
    val NOP_inst                    = 0x001c0000.U
    ip_reg.io.flush                 := rob.io.predict_fail_cmt(1) || (!ip_reg.io.stall && (pd.io.pred_fix || icache.io.cache_miss_RM))
    ip_reg.io.stall                 := fq.io.full
    ip_reg.io.insts_pack_IF         := VecInit.tabulate(2)(i => inst_pack_IF_gen(pi_reg.io.inst_pack_IF(i), icache.io.rdata_RM(i), mmu.io.i_exception))
    ip_reg.io.npc16_IF              := VecInit.tabulate(2)(i => pi_reg.io.inst_pack_IF(i).pc + Cat(Fill(14, icache.io.rdata_RM(i)(25)), icache.io.rdata_RM(i)(25, 10), 0.U(2.W)))
    ip_reg.io.npc26_IF              := VecInit.tabulate(2)(i => pi_reg.io.inst_pack_IF(i).pc + Cat(Fill(4, icache.io.rdata_RM(i)(9)), icache.io.rdata_RM(i)(9, 0), icache.io.rdata_RM(i)(25, 10), 0.U(2.W)))
    ip_reg.io.npc4_IF               := VecInit.tabulate(2)(i => pi_reg.io.inst_pack_IF(i).pc + 4.U)

    /* ---------- 3. Previous Decode Stage ---------- */
    // Previous Decoder
    pd.io.insts_pack_IF             := ip_reg.io.insts_pack_PD
    pd.io.npc4_IF                   := VecInit.tabulate(2)(i => ip_reg.io.insts_pack_PD(i).pc + 4.U)
    pd.io.npc16_IF                  := VecInit.tabulate(2)(i => ip_reg.io.insts_pack_PD(i).pc + Cat(Fill(14, ip_reg.io.insts_pack_PD(i).inst(25)), ip_reg.io.insts_pack_PD(i).inst(25, 10), 0.U(2.W)))
    pd.io.npc26_IF                  := VecInit.tabulate(2)(i => ip_reg.io.insts_pack_PD(i).pc + Cat(Fill(4, ip_reg.io.insts_pack_PD(i).inst(9)), ip_reg.io.insts_pack_PD(i).inst(9, 0), ip_reg.io.insts_pack_PD(i).inst(25, 10), 0.U(2.W)))

    /* ---------- Fetch Queue ---------- */
    fq.io.insts_pack                := VecInit.tabulate(2)(i => inst_pack_IF_gen(pd.io.insts_pack_PD(i), Mux(ip_reg.io.insts_pack_PD(i).exception(7), NOP_inst, ip_reg.io.insts_pack_PD(i).inst), ip_reg.io.insts_pack_PD(i).exception))
    fq.io.next_ready                := !(rob.io.full(2) || stall_by_iq || free_list.io.empty)
    fq.io.flush                     := rob.io.predict_fail_cmt(3)

    /* ---------- 4. Decode Stage ---------- */
    // Decode
    for(i <- 0 until 2){
        decode(i).inst              := fq.io.insts_pack_id(i).inst
    }
    free_list.io.rd_valid           := decode.map(_.rd_valid)
    free_list.io.rename_en          := VecInit.tabulate(2)(i => fq.io.insts_valid_decode(i) && fq.io.next_ready)
    free_list.io.commit_en          := rob.io.cmt_en
    free_list.io.commit_pprd_valid  := (rob.io.rd_valid_cmt.asUInt & VecInit(rob.io.pprd_cmt.map(_ =/= 0.U)).asUInt).asBools
    free_list.io.commit_pprd        := rob.io.pprd_cmt
    free_list.io.predict_fail       := ShiftRegister(rob.io.predict_fail_cmt(3), 1)
    free_list.io.head_arch          := arat.io.head_arch

    /* ---------- ID-RN SegReg ---------- */
    dr_reg.io.flush                 := rob.io.predict_fail_cmt(4) || (!dr_reg.io.stall && free_list.io.empty)
    dr_reg.io.stall                 := rob.io.full(3) || stall_by_iq
    dr_reg.io.insts_pack_ID         := VecInit.tabulate(2)(i => inst_pack_ID_gen(fq.io.insts_pack_id(i), fq.io.insts_valid_decode(i), decode(i).rj, decode(i).rk, 
                                                                                   decode(i).rd, decode(i).rd_valid, decode(i).imm, decode(i).alu_op, decode(i).alu_rs1_sel, decode(i).alu_rs2_sel, 
                                                                                   decode(i).br_type, decode(i).mem_type, Mux(fq.io.insts_pack_id(i).exception(7), SYST, decode(i).fu_id), decode(i).priv_vec, 
                                                                                   Mux(fq.io.insts_pack_id(i).exception(7), fq.io.insts_pack_id(i).exception, decode(i).exception)))
    dr_reg.io.alloc_preg_ID         := free_list.io.alloc_preg
    dr_reg.io.inst_ID               := VecInit.tabulate(2)(i => fq.io.insts_pack_id(i).inst)
    /* ---------- 5. Rename Stage ---------- */
    // Rename
    rename.io.rj                    := dr_reg.io.insts_pack_RN.map(_.rj)
    rename.io.rk                    := dr_reg.io.insts_pack_RN.map(_.rk)
    rename.io.rd                    := dr_reg.io.insts_pack_RN.map(_.rd)
    rename.io.rd_valid              := dr_reg.io.insts_pack_RN.map(_.rd_valid)
    rename.io.rename_en             := VecInit.tabulate(2)(i => dr_reg.io.insts_pack_RN(i).inst_valid && !dr_reg.io.stall)
    rename.io.predict_fail          := ShiftRegister(rob.io.predict_fail_cmt(4), 1)
    rename.io.arch_rat              := arat.io.arch_rat
    rename.io.alloc_preg            := dr_reg.io.alloc_preg_RN
    rename.io.prd_wake              := VecInit(sel1.io.wake_preg, sel2.io.wake_preg, md_ex2_ex3_reg.io.inst_pack_EX2.prd, re_reg4.io.inst_pack_EX.prd)
    rename.io.wake_valid            := VecInit(sel1.io.inst_issue_valid, sel2.io.inst_issue_valid, !mdu.io.busy, !dcache.io.cache_miss_MEM(4))

    // dispatch
    dp.io.inst_packs                := VecInit.tabulate(2)(i => inst_pack_RN_gen(dr_reg.io.insts_pack_RN(i), rename.io.prj(i), rename.io.prk(i), rename.io.prd(i), rename.io.pprd(i)))
    dp.io.elem_num                  := VecInit(iq1.io.elem_num, iq2.io.elem_num)

    val prj_ready                   = VecInit.tabulate(2)(i => rename.io.prj_ready(i))
    val prk_ready                   = VecInit.tabulate(2)(i => rename.io.prk_ready(i))

    // rob  
    val is_store_dp                 = VecInit.tabulate(2)(i => (dr_reg.io.insts_pack_RN(i).mem_type(4)))
    val br_type_dp                  = VecInit.tabulate(2)(i => Mux(dr_reg.io.insts_pack_RN(i).br_type === BR_JIRL && dr_reg.io.insts_pack_RN(i).rd === 1.U, 3.U, 
                                                               Mux(dr_reg.io.insts_pack_RN(i).br_type === BR_JIRL && dr_reg.io.insts_pack_RN(i).rj === 1.U, 1.U(2.W), 
                                                               Mux(dr_reg.io.insts_pack_RN(i).br_type === BR_BL, 2.U(2.W), 0.U(2.W)))))
    val pred_update_en_dp           = VecInit.tabulate(2)(i => (dr_reg.io.insts_pack_RN(i).br_type =/= NO_BR))
    rob.io.inst_valid_dp            := dr_reg.io.insts_pack_RN.map(_.inst_valid)
    rob.io.rd_dp                    := dr_reg.io.insts_pack_RN.map(_.rd)
    rob.io.rd_valid_dp              := dr_reg.io.insts_pack_RN.map(_.rd_valid)
    rob.io.prd_dp                   := rename.io.prd
    rob.io.pprd_dp                  := rename.io.pprd
    rob.io.pc_dp                    := dr_reg.io.insts_pack_RN.map(_.pc)
    rob.io.is_store_dp              := is_store_dp
    rob.io.stall                    := dr_reg.io.stall
    rob.io.pred_update_en_dp        := pred_update_en_dp
    rob.io.br_type_pred_dp          := br_type_dp
    rob.io.priv_vec_dp              := dr_reg.io.insts_pack_RN.map(_.priv_vec)
    rob.io.exception_dp             := dr_reg.io.insts_pack_RN.map(_.exception)
    rob.io.inst_dp                  := dr_reg.io.inst_RN
    
    
    /* ---------- 6. Issue Stage ---------- */
    // 1. arith1, common calculate
    // issue queue
    iq1.io.insts_dispatch           := VecInit.tabulate(2)(i => inst_pack_DP_FU1_gen(dp.io.inst_packs(i), rob.io.rob_index_dp(i)))
    iq1.io.insts_disp_index         := dp.io.insts_disp_index(0)
    iq1.io.insts_disp_valid         := dp.io.insts_disp_valid(0)
    iq1.io.prj_ready                := prj_ready
    iq1.io.prk_ready                := prk_ready
    iq1.io.issue_ack                := sel1.io.issue_ack
    iq1.io.flush                    := rob.io.predict_fail_cmt(6)
    iq1.io.stall                    := stall_by_iq || rob.io.full(5)
    iq1.io.ld_mem_prd               := ls_ex_mem_reg.io.prd_MEM(0)
    iq1.io.dcache_miss              := dcache.io.cache_miss_iq

    // select   
    sel1.io.insts_issue             := iq1.io.insts_issue
    sel1.io.issue_req               := iq1.io.issue_req
    sel1.io.stall                   := !(iq1.io.issue_req.asUInt.orR) || ir_reg1.io.stall || ShiftRegister(ir_reg1.io.stall, 1)

    // 2. arith2, common calculate
    // issue queue
    iq2.io.insts_dispatch           := VecInit.tabulate(2)(i => inst_pack_DP_FU2_gen(dp.io.inst_packs(i), rob.io.rob_index_dp(i)))
    iq2.io.insts_disp_index         := dp.io.insts_disp_index(1)
    iq2.io.insts_disp_valid         := dp.io.insts_disp_valid(1)
    iq2.io.prj_ready                := prj_ready
    iq2.io.prk_ready                := prk_ready
    iq2.io.issue_ack                := sel2.io.issue_ack
    iq2.io.flush                    := rob.io.predict_fail_cmt(6)
    iq2.io.stall                    := stall_by_iq || rob.io.full(5)
    iq2.io.ld_mem_prd               := ls_ex_mem_reg.io.prd_MEM(1)
    iq2.io.dcache_miss              := dcache.io.cache_miss_iq

    // select   
    sel2.io.insts_issue             := iq2.io.insts_issue
    sel2.io.issue_req               := iq2.io.issue_req
    sel2.io.stall                   := !(iq2.io.issue_req.asUInt.orR) || ir_reg2.io.stall || ShiftRegister(ir_reg2.io.stall, 1)

    // 3. multiply, multiply and divide
    // issue queue
    iq3.io.insts_dispatch           := VecInit.tabulate(2)(i => inst_pack_DP_MD_gen(dp.io.inst_packs(i), rob.io.rob_index_dp(i)))
    iq3.io.insts_disp_index         := dp.io.insts_disp_index(2)
    iq3.io.insts_disp_valid         := dp.io.insts_disp_valid(2)
    iq3.io.prj_ready                := prj_ready
    iq3.io.prk_ready                := prk_ready
    iq3.io.issue_ack                := sel3.io.issue_ack
    iq3.io.flush                    := rob.io.predict_fail_cmt(6)
    iq3.io.stall                    := stall_by_iq || rob.io.full(5)
    iq3.io.ld_mem_prd               := ls_ex_mem_reg.io.prd_MEM(2)
    iq3.io.is_store_cmt_num         := DontCare
    iq3.io.rob_index_cmt            := DontCare
    iq3.io.dcache_miss              := dcache.io.cache_miss_iq

    // select   
    sel3.io.insts_issue             := iq3.io.insts_issue
    sel3.io.issue_req               := iq3.io.issue_req
    sel3.io.stall                   := !(iq3.io.issue_req.asUInt.orR) || ir_reg3.io.stall || ShiftRegister(ir_reg3.io.stall, 1)

    // 4. load store unit   
    // issue queue  
    iq4.io.insts_dispatch           := VecInit.tabulate(2)(i => inst_pack_DP_LS_gen(dp.io.inst_packs(i), rob.io.rob_index_dp(i)))
    iq4.io.insts_disp_index         := dp.io.insts_disp_index(3)
    iq4.io.insts_disp_valid         := dp.io.insts_disp_valid(3)
    iq4.io.prj_ready                := prj_ready
    iq4.io.prk_ready                := prk_ready
    iq4.io.issue_ack                := sel4.io.issue_ack
    iq4.io.flush                    := rob.io.predict_fail_cmt(6)
    iq4.io.stall                    := stall_by_iq || rob.io.full(5)
    iq4.io.ld_mem_prd               := ls_ex_mem_reg.io.prd_MEM(3)
    iq4.io.is_store_cmt_num         := rob.io.is_store_num_cmt
    iq4.io.rob_index_cmt            := rob.io.rob_index_cmt
    iq4.io.dcache_miss              := dcache.io.cache_miss_iq 

    // select   
    sel4.io.insts_issue             := iq4.io.insts_issue
    sel4.io.issue_req               := iq4.io.issue_req
    sel4.io.stall                   := !(iq4.io.issue_req.asUInt.orR) || ir_reg4.io.stall || ShiftRegister(ir_reg4.io.stall, 1)
    // mutual wakeup
    val iq_inline_wake_preg         = VecInit(sel1.io.wake_preg, 
                                              sel2.io.wake_preg, 
                                              Mux(!mdu.io.busy, md_ex2_ex3_reg.io.inst_pack_EX2.prd, 0.U),
                                              re_reg4.io.inst_pack_EX.prd)

    val iq_mutual_wake_preg         = VecInit(ir_reg1.io.inst_pack_RF.prd,
                                              ir_reg2.io.inst_pack_RF.prd,
                                              Mux(!mdu.io.busy, md_ex2_ex3_reg.io.inst_pack_EX2.prd, 0.U),
                                              Mux(!dcache.io.cache_miss_MEM(4), re_reg4.io.inst_pack_EX.prd, 0.U))

    iq1.io.wake_preg                := VecInit(iq_inline_wake_preg(0), iq_inline_wake_preg(1), iq_mutual_wake_preg(2), iq_mutual_wake_preg(3))
    iq2.io.wake_preg                := VecInit(iq_inline_wake_preg(0), iq_inline_wake_preg(1), iq_mutual_wake_preg(2), iq_mutual_wake_preg(3))
    iq3.io.wake_preg                := VecInit(iq_mutual_wake_preg(0), iq_mutual_wake_preg(1), iq_inline_wake_preg(2), iq_mutual_wake_preg(3))
    iq4.io.wake_preg                := VecInit(iq_mutual_wake_preg(0), iq_mutual_wake_preg(1), iq_mutual_wake_preg(2), iq_inline_wake_preg(3))

    /* ---------- IS-RF SegReg ---------- */
    ir_reg1.io.flush                := rob.io.predict_fail_cmt(7)
    ir_reg1.io.stall                := false.B
    ir_reg1.io.inst_pack_IS         := inst_pack_IS_FU1_gen(sel1.io.inst_issue.inst, sel1.io.inst_issue_valid)

    ir_reg2.io.flush                := rob.io.predict_fail_cmt(7)
    ir_reg2.io.stall                := false.B
    ir_reg2.io.inst_pack_IS         := inst_pack_IS_FU2_gen(sel2.io.inst_issue.inst, sel2.io.inst_issue_valid)

    ir_reg3.io.flush                := rob.io.predict_fail_cmt(7)
    ir_reg3.io.stall                := mdu.io.busy
    ir_reg3.io.inst_pack_IS         := inst_pack_IS_MD_gen(sel3.io.inst_issue.inst, sel3.io.inst_issue_valid)


    /* ---------- 7. Regfile Read Stage ---------- */
    // Regfile
    rf.io.prj                       := VecInit(ir_reg1.io.inst_pack_RF.prj, ir_reg2.io.inst_pack_RF.prj, ir_reg3.io.inst_pack_RF.prj, ir_reg4.io.inst_pack_RF.prj)
    rf.io.prk                       := VecInit(ir_reg1.io.inst_pack_RF.prk, ir_reg2.io.inst_pack_RF.prk, ir_reg3.io.inst_pack_RF.prk, ir_reg4.io.inst_pack_RF.prk)

    ir_reg4.io.flush                := rob.io.predict_fail_cmt(7)
    ir_reg4.io.stall                := sb.io.full && re_reg4.io.inst_pack_EX.mem_type(4) || dcache.io.cache_miss_MEM(3) || (sb.io.st_cmt_valid && ir_reg4.io.inst_pack_EX.mem_type(4, 3).orR)
    ir_reg4.io.inst_pack_RF         := inst_pack_IS_LS_gen(sel4.io.inst_issue.inst, sel4.io.inst_issue_valid)
    ir_reg4.io.src1_RF              := rf.io.prj_data(3)
    ir_reg4.io.src2_RF              := rf.io.prk_data(3)
    ir_reg4.io.csr_rdata_RF         := Mux(ir_reg4.io.inst_pack_RF.priv_vec(0), Fill(5, ir_reg4.io.inst_pack_RF.imm(31)) ## ir_reg4.io.inst_pack_RF.imm(31, 5), ir_reg4.io.inst_pack_RF.imm)
    ir_reg4.io.forward_prj_en       := bypass.io.forward_prj_en(2)
    ir_reg4.io.forward_prj_data     := bypass.io.forward_prj_data(2)
    ir_reg4.io.forward_prk_en       := bypass.io.forward_prk_en(2)
    ir_reg4.io.forward_prk_data     := bypass.io.forward_prk_data(2)

    // MMU-dcache       
    mmu.io.d_rvalid                 := ir_reg4.io.inst_pack_EX.mem_type(3)
    mmu.io.d_wvalid                 := ir_reg4.io.inst_pack_EX.mem_type(4)
    mmu.io.d_vaddr                  := Mux(bypass.io.forward_prj_en(2), bypass.io.forward_prj_data(2), ir_reg4.io.src1_EX) + ir_reg4.io.csr_rdata_EX
    mmu.io.d_stall                  := re_reg4.io.stall

    // CSR Regfile      
    csr_rf.io.raddr                 := ir_reg3.io.inst_pack_RF.imm(13, 0)
    csr_rf.io.wdata                 := rob.io.csr_wdata_cmt
    csr_rf.io.waddr                 := rob.io.csr_addr_cmt
    csr_rf.io.we                    := rob.io.csr_we_cmt
    csr_rf.io.exception             := rob.io.exception_cmt
    csr_rf.io.badv_exp              := rob.io.badv_cmt
    csr_rf.io.is_eret               := rob.io.is_eret_cmt
    csr_rf.io.pc_exp                := rob.io.pred_pc_cmt
    csr_rf.io.interrupt             := 0.U
    csr_rf.io.ip_int                := false.B
    csr_rf.io.tlbentry_in           := rob.io.tlbentry_cmt
    csr_rf.io.tlbrd_en              := rob.io.tlbrd_en_cmt
    csr_rf.io.tlbsrch_en            := rob.io.tlbsrch_en_cmt
    csr_rf.io.llbit_clear           := rob.io.llbit_clear_cmt
    csr_rf.io.llbit_set             := rob.io.llbit_set_cmt

    /* ---------- RF-EX SegReg ---------- */
    re_reg1.io.flush                := rob.io.predict_fail_cmt(8)
    re_reg1.io.stall                := false.B
    re_reg1.io.inst_pack_RF         := ir_reg1.io.inst_pack_RF
    re_reg1.io.src1_RF              := rf.io.prj_data(0)
    re_reg1.io.src2_RF              := rf.io.prk_data(0)
    re_reg1.io.csr_rdata_RF         := DontCare

    re_reg2.io.flush                := rob.io.predict_fail_cmt(8)
    re_reg2.io.stall                := false.B
    re_reg2.io.inst_pack_RF         := ir_reg2.io.inst_pack_RF
    re_reg2.io.src1_RF              := rf.io.prj_data(1)
    re_reg2.io.src2_RF              := rf.io.prk_data(1)
    re_reg2.io.csr_rdata_RF         := DontCare

    re_reg3.io.flush                := rob.io.predict_fail_cmt(8)
    re_reg3.io.stall                := mdu.io.busy
    re_reg3.io.inst_pack_RF         := ir_reg3.io.inst_pack_RF
    re_reg3.io.src1_RF              := rf.io.prj_data(2)
    re_reg3.io.src2_RF              := rf.io.prk_data(2)
    re_reg3.io.csr_rdata_RF         := csr_rf.io.rdata

    re_reg4.io.flush                := rob.io.predict_fail_cmt(8) || !re_reg4.io.stall && (sb.io.st_cmt_valid && ir_reg4.io.inst_pack_EX.mem_type(4, 3).orR)
    re_reg4.io.stall                := sb.io.full && re_reg4.io.inst_pack_EX.mem_type(4) || dcache.io.cache_miss_MEM(3)
    re_reg4.io.inst_pack_RF         := ir_reg4.io.inst_pack_EX
    re_reg4.io.src1_RF              := Mux(bypass.io.forward_prj_en(2), bypass.io.forward_prj_data(2), ir_reg4.io.src1_EX) + ir_reg4.io.csr_rdata_EX
    re_reg4.io.src2_RF              := Mux(bypass.io.forward_prk_en(2), bypass.io.forward_prk_data(2), ir_reg4.io.src2_EX)
    re_reg4.io.csr_rdata_RF         := DontCare

    /* ---------- 8. Execute Stage ---------- */
    // 1. arith common fu1
    // ALU
    alu1.io.alu_op                  := re_reg1.io.inst_pack_EX.alu_op
    alu1.io.src1                    := MuxLookup(re_reg1.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG                     -> Mux(bypass.io.forward_prj_en(0), bypass.io.forward_prj_data(0), re_reg1.io.src1_EX),
        RS1_PC                      -> re_reg1.io.inst_pack_EX.pc))

    alu1.io.src2                    := MuxLookup(re_reg1.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG                     -> Mux(bypass.io.forward_prk_en(0), bypass.io.forward_prk_data(0), re_reg1.io.src2_EX),
        RS2_IMM                     -> re_reg1.io.inst_pack_EX.imm,
        RS2_CNTH                    -> stable_cnt.io.value(63, 32),
        RS2_CNTL                    -> stable_cnt.io.value(31, 0)))
    
    // 2. arith common fu2
    // ALU
    alu2.io.alu_op                  := re_reg2.io.inst_pack_EX.alu_op
    alu2.io.src1                    := MuxLookup(re_reg2.io.inst_pack_EX.alu_rs1_sel, 0.U)(Seq(
        RS1_REG                     -> Mux(bypass.io.forward_prj_en(1), bypass.io.forward_prj_data(1), re_reg2.io.src1_EX),
        RS1_PC                      -> re_reg2.io.inst_pack_EX.pc))

    alu2.io.src2                    := MuxLookup(re_reg2.io.inst_pack_EX.alu_rs2_sel, 0.U)(Seq(
        RS2_REG                     -> Mux(bypass.io.forward_prk_en(1), bypass.io.forward_prk_data(1), re_reg2.io.src2_EX),
        RS2_IMM                     -> re_reg2.io.inst_pack_EX.imm,
        RS2_FOUR                    -> 4.U))
    
    // Branch
    br.io.br_type                   := re_reg2.io.inst_pack_EX.br_type
    br.io.src1                      := Mux(bypass.io.forward_prj_en(1), bypass.io.forward_prj_data(1), re_reg2.io.src1_EX)
    br.io.src2                      := Mux(bypass.io.forward_prk_en(1), bypass.io.forward_prk_data(1), re_reg2.io.src2_EX)
    br.io.pc_ex                     := re_reg2.io.inst_pack_EX.pc
    br.io.imm_ex                    := re_reg2.io.inst_pack_EX.imm
    br.io.predict_jump              := re_reg2.io.inst_pack_EX.predict_jump
    br.io.pred_npc                  := re_reg2.io.inst_pack_EX.pred_npc

    // 3. multiply-divide fu
    mdu.io.md_op                    := re_reg3.io.inst_pack_EX.alu_op
    mdu.io.src1                     := re_reg3.io.src1_EX
    mdu.io.src2                     := re_reg3.io.src2_EX

    // CSR update
    val csr_op                      = re_reg3.io.inst_pack_EX.priv_vec(2)
    val is_mret                     = re_reg3.io.inst_pack_EX.priv_vec(3)
    val is_tlbsrch                  = re_reg3.io.inst_pack_EX.priv_vec(7)
    val csr_src1                    = re_reg3.io.src1_EX
    val csr_src2                    = re_reg3.io.src2_EX
    val csr_wdata                   = Mux(is_tlbsrch, mmu.io.tlbsrch_hit ## mmu.io.tlbsrch_idx, 
                                      Mux(is_mret, re_reg3.io.csr_rdata_EX, 
                                      Mux(csr_op, csr_src1 & csr_src2 | ~csr_src1 & re_reg3.io.csr_rdata_EX,csr_src2)))

    rob.io.priv_vec_ex              := re_reg3.io.inst_pack_EX.priv_vec
    rob.io.csr_addr_ex              := re_reg3.io.inst_pack_EX.imm(13, 0)
    rob.io.tlbentry_ex              := mmu.io.tlbrd_entry
    rob.io.invtlb_op_ex             := re_reg3.io.inst_pack_EX.imm(4, 0)
    rob.io.invtlb_asid_ex           := re_reg3.io.src1_EX(9, 0)
    rob.io.invtlb_vaddr_ex          := re_reg3.io.src2_EX

    md_ex1_ex2_reg.io.flush         := rob.io.predict_fail_cmt(6)
    md_ex1_ex2_reg.io.stall         := mdu.io.busy
    md_ex1_ex2_reg.io.inst_pack_EX1 := re_reg3.io.inst_pack_EX
    md_ex1_ex2_reg.io.csr_wdata_EX1 := csr_wdata
    md_ex1_ex2_reg.io.csr_rdata_EX1 := re_reg3.io.csr_rdata_EX

    md_ex2_ex3_reg.io.flush         := rob.io.predict_fail_cmt(6)
    md_ex2_ex3_reg.io.stall         := mdu.io.busy
    md_ex2_ex3_reg.io.inst_pack_EX1 := md_ex1_ex2_reg.io.inst_pack_EX2
    md_ex2_ex3_reg.io.csr_wdata_EX1 := md_ex1_ex2_reg.io.csr_wdata_EX2
    md_ex2_ex3_reg.io.csr_rdata_EX1 := md_ex1_ex2_reg.io.csr_rdata_EX2


    // 4. load-store fu, include cache
    // EX Stage
    // MMU-EX
    mmu.io.csr_asid                 := csr_rf.io.asid_global
    mmu.io.csr_plv                  := csr_rf.io.plv_global
    mmu.io.csr_tlbehi               := csr_rf.io.tlbehi_global
    mmu.io.csr_tlbidx               := csr_rf.io.tlbidx_global
    mmu.io.tlbwr_entry              := csr_rf.io.tlbentry_global
    mmu.io.tlbwr_en                 := rob.io.tlbwr_en_cmt
    mmu.io.tlbfill_idx              := stable_cnt.io.value(3, 0)
    mmu.io.tlbfill_en               := rob.io.tlbfill_en_cmt
    mmu.io.invtlb_en                := rob.io.invtlb_en_cmt
    mmu.io.invtlb_op                := rob.io.invtlb_op_cmt
    mmu.io.invtlb_asid              := rob.io.invtlb_asid_cmt
    mmu.io.invtlb_vaddr             := rob.io.invtlb_vaddr_cmt
    mmu.io.csr_crmd_trans           := csr_rf.io.crmd_trans
    mmu.io.csr_dmw0                 := csr_rf.io.dmw0_global
    mmu.io.csr_dmw1                 := csr_rf.io.dmw1_global

    // exception detect
    rob.io.priv_vec_ls              := re_reg4.io.inst_pack_EX.priv_vec
    rob.io.llbit_global             := csr_rf.io.llbit_global

    // exception detect
    exception_ls.io.addr_EX         := re_reg4.io.src1_EX
    exception_ls.io.mem_type_EX     := re_reg4.io.inst_pack_EX.mem_type
    val exception_EX                = Mux((re_reg4.io.inst_pack_EX.priv_vec(0) && re_reg4.io.inst_pack_EX.imm(4, 3) =/= 2.U
                                        || re_reg4.io.inst_pack_EX.priv_vec(2) && !csr_rf.io.llbit_global), 0.U, Mux(exception_ls.io.exception_ls(7), exception_ls.io.exception_ls, mmu.io.d_exception))

    // store_buf
    sb.io.flush                     := rob.io.predict_fail_cmt(6)
    sb.io.addr_ex                   := mmu.io.d_paddr
    sb.io.st_data_ex                := re_reg4.io.src2_EX
    sb.io.mem_type_ex               := Mux(re_reg4.io.stall, 0.U, re_reg4.io.inst_pack_EX.mem_type)
    sb.io.uncache_ex                := mmu.io.d_uncache
    sb.io.is_store_num_cmt          := rob.io.is_store_num_cmt
    sb.io.dcache_miss               := dcache.io.cache_miss_MEM(4)
    sb.io.em_stall                  := ls_ex_mem_reg.io.stall

    // EX-MEM SegReg
    ls_ex_mem_reg.io.flush          := rob.io.predict_fail_cmt(6) || (!ls_ex_mem_reg.io.stall && sb.io.full && re_reg4.io.inst_pack_EX.mem_type(4))
    ls_ex_mem_reg.io.stall          := dcache.io.cache_miss_MEM(4)
    ls_ex_mem_reg.io.inst_pack_EX   := re_reg4.io.inst_pack_EX
    ls_ex_mem_reg.io.is_ucread_EX   := re_reg4.io.src1_EX(31, 28) === 0xa.U
    ls_ex_mem_reg.io.src1_EX        := re_reg4.io.src1_EX
    ls_ex_mem_reg.io.src2_EX        := re_reg4.io.src2_EX
    ls_ex_mem_reg.io.uncache_EX     := mmu.io.d_uncache
    ls_ex_mem_reg.io.paddr_EX       := mmu.io.d_paddr
    ls_ex_mem_reg.io.llbit_EX       := csr_rf.io.llbit_global
    ls_ex_mem_reg.io.prd_EX         := VecInit.fill(4)(re_reg4.io.inst_pack_EX.prd)
    ls_ex_mem_reg.io.exception_EX   := exception_EX
    ls_ex_mem_reg.io.sb_rdata_EX    := sb.io.ld_data_mem
    ls_ex_mem_reg.io.sb_hit_EX      := sb.io.ld_hit

    // MEM Stage
    // dcache
    dcache.io.addr_RF               := Mux(sb.io.st_cmt_valid, sb.io.st_addr_cmt, re_reg4.io.src1_RF)
    dcache.io.paddr_EX              := mmu.io.d_paddr
    dcache.io.uncache_EX            := mmu.io.d_uncache
    dcache.io.mem_type_RF           := Mux(sb.io.st_cmt_valid, Mux(sb.io.is_uncache_cmt, 0.U, 4.U(3.W) ## sb.io.st_wlen_cmt(1, 0)), re_reg4.io.inst_pack_RF.mem_type)
    dcache.io.wdata_RF              := Mux(sb.io.st_cmt_valid, sb.io.st_data_cmt, re_reg4.io.src2_RF)
    dcache.io.stall                 := false.B
    dcache.io.exception_MEM         := ls_ex_mem_reg.io.exception_MEM
    dcache.io.d_rready              := arb.io.d_rready
    dcache.io.d_rdata               := arb.io.d_rdata
    dcache.io.d_rlast               := arb.io.d_rlast
    dcache.io.d_wready              := arb.io.d_wready
    dcache.io.d_bvalid              := arb.io.d_bvalid
    dcache.io.rob_index_EX          := re_reg4.io.inst_pack_EX.rob_index
    dcache.io.rob_index_CMT         := rob.io.rob_index_cmt
    dcache.io.flush                 := rob.io.predict_fail_cmt(6)
    dcache.io.store_cmt_RF          := sb.io.st_cmt_valid
    dcache.io.cacop_en              := Mux(sb.io.st_cmt_valid, false.B, re_reg4.io.inst_pack_RF.priv_vec(0) && re_reg4.io.inst_pack_RF.imm(2, 0) === 1.U)
    dcache.io.cacop_op              := re_reg4.io.inst_pack_RF.imm(4, 3)

    // bypass for 1, 2
    bypass.io.prd_wb                := VecInit(ew_reg1.io.inst_pack_WB.prd, ew_reg2.io.inst_pack_WB.prd, ew_reg4.io.inst_pack_WB.prd)
    bypass.io.prj_ex                := VecInit(re_reg1.io.inst_pack_EX.prj, re_reg2.io.inst_pack_EX.prj, ir_reg4.io.inst_pack_EX.prj)
    bypass.io.prk_ex                := VecInit(re_reg1.io.inst_pack_EX.prk, re_reg2.io.inst_pack_EX.prk, ir_reg4.io.inst_pack_EX.prk)
    bypass.io.prf_wdata_wb          := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg4.io.mem_rdata_WB)
    bypass.io.rd_valid_wb           := VecInit(ew_reg1.io.inst_pack_WB.rd_valid, ew_reg2.io.inst_pack_WB.rd_valid, ew_reg4.io.inst_pack_WB.rd_valid)

    val mem_rdata_raw               = VecInit.tabulate(4)(i => Mux(sb.io.ld_hit(i), sb.io.ld_data_mem(i*8+7, i*8), dcache.io.rdata_MEM(i*8+7, i*8))).asUInt 
    val mem_rdata                   = MuxLookup(ls_ex_mem_reg.io.inst_pack_MEM.mem_type(2, 0), 0.U)(Seq(
                                                        0.U -> Fill(24, mem_rdata_raw(7)) ## mem_rdata_raw(7, 0),
                                                        1.U -> Fill(16, mem_rdata_raw(15)) ## mem_rdata_raw(15, 0),
                                                        2.U -> mem_rdata_raw,
                                                        4.U -> 0.U(24.W) ## mem_rdata_raw(7, 0),
                                                        5.U -> 0.U(16.W) ## mem_rdata_raw(15, 0)))
    val ls_wb_data                  = Mux(ls_ex_mem_reg.io.inst_pack_MEM.priv_vec(2), 0.U(31.W) ## ls_ex_mem_reg.io.llbit_MEM, mem_rdata)

    
    /* ---------- EX-WB SegReg ---------- */
    ew_reg1.io.flush                := rob.io.predict_fail_cmt(9)
    ew_reg1.io.stall                := false.B
    ew_reg1.io.inst_pack_EX         := re_reg1.io.inst_pack_EX
    ew_reg1.io.alu_out_EX           := alu1.io.alu_out
    ew_reg1.io.is_ucread_EX         := re_reg1.io.inst_pack_EX.alu_rs2_sel === RS2_CNTH || re_reg1.io.inst_pack_EX.alu_rs2_sel === RS2_CNTL

    ew_reg2.io.flush                := rob.io.predict_fail_cmt(9)
    ew_reg2.io.stall                := false.B
    ew_reg2.io.inst_pack_EX         := re_reg2.io.inst_pack_EX
    ew_reg2.io.alu_out_EX           := alu2.io.alu_out
    ew_reg2.io.predict_fail_EX      := br.io.predict_fail
    ew_reg2.io.branch_target_EX     := br.io.branch_target
    ew_reg2.io.real_jump_EX         := br.io.real_jump

    ew_reg3.io.flush                := rob.io.predict_fail_cmt(9) || mdu.io.busy
    ew_reg3.io.stall                := false.B
    ew_reg3.io.inst_pack_EX         := md_ex2_ex3_reg.io.inst_pack_EX2
    ew_reg3.io.md_out_EX            := Mux(md_ex2_ex3_reg.io.inst_pack_EX2.priv_vec(0), md_ex2_ex3_reg.io.csr_rdata_EX2, Mux(!md_ex2_ex3_reg.io.inst_pack_EX2.alu_op(2), mdu.io.mul_out, mdu.io.div_out))
    ew_reg3.io.csr_wdata_EX         := md_ex2_ex3_reg.io.csr_wdata_EX2

    ew_reg4.io.flush                := rob.io.predict_fail_cmt(9) || dcache.io.cache_miss_MEM(3)
    ew_reg4.io.stall                := false.B  
    ew_reg4.io.inst_pack_EX2        := ls_ex_mem_reg.io.inst_pack_MEM
    ew_reg4.io.exception_EX2        := ls_ex_mem_reg.io.exception_MEM
    ew_reg4.io.vaddr_EX2            := ls_ex_mem_reg.io.src1_MEM
    ew_reg4.io.mem_rdata_EX2        := ls_wb_data
    ew_reg4.io.sb_rdata_EX2         := sb.io.ld_data_mem
    ew_reg4.io.llbit_EX2            := ls_ex_mem_reg.io.llbit_MEM
    ew_reg4.io.sb_hit_EX2           := sb.io.ld_hit
    ew_reg4.io.is_ucread_EX2        := ls_ex_mem_reg.io.is_ucread_MEM

    /* ---------- 9. Write Back Stage ---------- */
    rf.io.wdata                     := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg3.io.md_out_WB, ew_reg4.io.mem_rdata_WB)
    rf.io.rf_we                     := VecInit(ew_reg1.io.inst_pack_WB.rd_valid, ew_reg2.io.inst_pack_WB.rd_valid, ew_reg3.io.inst_pack_WB.rd_valid, ew_reg4.io.inst_pack_WB.rd_valid && ! ew_reg4.io.exception_WB(7))
    rf.io.prd                       := VecInit(ew_reg1.io.inst_pack_WB.prd, ew_reg2.io.inst_pack_WB.prd, ew_reg3.io.inst_pack_WB.prd, ew_reg4.io.inst_pack_WB.prd)
    
    // rob
    rob.io.inst_valid_wb            := VecInit(ew_reg1.io.inst_pack_WB.inst_valid && !ew_reg1.io.stall, ew_reg2.io.inst_pack_WB.inst_valid && !ew_reg2.io.stall, ew_reg3.io.inst_pack_WB.inst_valid && !ew_reg3.io.stall, ew_reg4.io.inst_pack_WB.inst_valid && !ew_reg4.io.stall)
    rob.io.rob_index_wb             := VecInit(ew_reg1.io.inst_pack_WB.rob_index, ew_reg2.io.inst_pack_WB.rob_index, ew_reg3.io.inst_pack_WB.rob_index, ew_reg4.io.inst_pack_WB.rob_index)
    rob.io.predict_fail_wb          := VecInit(DontCare, ew_reg2.io.predict_fail_WB, DontCare, DontCare)
    rob.io.real_jump_wb             := VecInit(DontCare, ew_reg2.io.real_jump_WB, DontCare, DontCare)
    rob.io.branch_target_wb         := VecInit(DontCare, ew_reg2.io.branch_target_WB, ew_reg3.io.csr_wdata_WB, ew_reg4.io.vaddr_WB)
    rob.io.rf_wdata_wb              := VecInit(ew_reg1.io.alu_out_WB, ew_reg2.io.alu_out_WB, ew_reg3.io.md_out_WB, ew_reg4.io.mem_rdata_WB)
    rob.io.is_ucread_wb             := VecInit(ew_reg1.io.is_ucread_WB, false.B, false.B, ew_reg4.io.is_ucread_WB)
    rob.io.exception_wb             := VecInit(DontCare, DontCare, DontCare, ew_reg4.io.exception_WB)
    rob.io.tlbreentry_global        := csr_rf.io.tlbreentry_global
    rob.io.eentry_global            := csr_rf.io.eentry_global
    rob.io.interrupt_vec            := csr_rf.io.interrupt_vec
    
    /* ---------- 10. Commit Stage ---------- */
    // Arch Rat
    arat.io.cmt_en                  := rob.io.cmt_en
    arat.io.prd_cmt                 := rob.io.prd_cmt
    arat.io.pprd_cmt                := rob.io.pprd_cmt
    arat.io.rd_valid_cmt            := rob.io.rd_valid_cmt
    arat.io.predict_fail            := rob.io.predict_fail_cmt(9)
    arat.io.br_type_pred_cmt        := rob.io.pred_br_type_cmt
    arat.io.pred_update_en_cmt      := rob.io.pred_update_en_cmt
    arat.io.pc_cmt                  := rob.io.pred_pc_cmt
    
    // arbiter  
    arb.io.i_araddr                 := icache.io.i_araddr
    arb.io.i_rvalid                 := icache.io.i_rvalid
    arb.io.i_rsize                  := icache.io.i_rsize
    arb.io.i_rburst                 := icache.io.i_rburst
    arb.io.i_rlen                   := icache.io.i_rlen
    
    arb.io.d_araddr                 := dcache.io.d_araddr
    arb.io.d_rvalid                 := dcache.io.d_rvalid
    arb.io.d_rsize                  := dcache.io.d_rsize
    arb.io.d_rburst                 := dcache.io.d_rburst
    arb.io.d_rlen                   := dcache.io.d_rlen
    arb.io.d_awaddr                 := dcache.io.d_awaddr
    arb.io.d_wvalid                 := dcache.io.d_wvalid
    arb.io.d_wdata                  := dcache.io.d_wdata
    arb.io.d_wlast                  := dcache.io.d_wlast
    arb.io.d_wsize                  := dcache.io.d_wsize
    arb.io.d_wburst                 := dcache.io.d_wburst
    arb.io.d_wlen                   := dcache.io.d_wlen
    arb.io.d_wstrb                  := dcache.io.d_wstrb
    arb.io.d_bready                 := dcache.io.d_bready
    
    
    io.araddr                       := arb.io.araddr  
    io.arburst                      := arb.io.arburst
    io.arid                         := arb.io.arid
    io.arlen                        := arb.io.arlen
    arb.io.arready                  := io.arready
    io.arsize                       := arb.io.arsize
    io.arvalid                      := arb.io.arvalid
    
    io.awaddr                       := arb.io.awaddr
    io.awburst                      := arb.io.awburst
    io.awid                         := arb.io.awid
    io.awlen                        := arb.io.awlen
    arb.io.awready                  := io.awready
    io.awsize                       := arb.io.awsize
    io.awvalid                      := arb.io.awvalid
    
    arb.io.bid                      := io.bid
    io.bready                       := arb.io.bready
    arb.io.bresp                    := io.bresp
    arb.io.bvalid                   := io.bvalid
    
    arb.io.rdata                    := io.rdata
    arb.io.rid                      := io.rid
    arb.io.rlast                    := io.rlast
    io.rready                       := arb.io.rready
    arb.io.rresp                    := io.rresp
    arb.io.rvalid                   := io.rvalid
    
    io.wdata                        := arb.io.wdata
    io.wlast                        := arb.io.wlast
    arb.io.wready                   := io.wready
    io.wstrb                        := arb.io.wstrb
    io.wvalid                       := arb.io.wvalid


    // statitic
    if(System.getProperties().getProperty("mode") == "sim"){
        io.commit_en                    := rob.io.cmt_en
        io.commit_rd                    := rob.io.rd_cmt
        io.commit_prd                   := rob.io.prd_cmt
        io.commit_rd_valid              := rob.io.rd_valid_cmt
        io.commit_rf_wdata              := rob.io.rf_wdata_cmt
        io.commit_csr_wdata             := rob.io.csr_diff_wdata_cmt
        io.commit_csr_we                := rob.io.csr_diff_we_cmt
        io.commit_csr_waddr             := rob.io.csr_diff_addr_cmt
        io.commit_pc                    := rob.io.pc_cmt
        io.commit_is_ucread             := rob.io.is_ucread_cmt
        io.commit_is_br                 := rob.io.is_br_stat
        io.commit_br_type               := rob.io.br_type_stat
        io.commit_predict_fail          := rob.io.predict_fail_stat
        io.commit_inst                  := rob.io.inst_cmt
        io.commit_interrupt             := rob.io.exception_cmt(7) && rob.io.exception_cmt(6, 0) === 0.U
        io.commit_interrupt_type        := csr_rf.io.estat_13


        io.commit_stall_by_fetch_queue  := fq.io.full
        io.commit_stall_by_rename       := free_list.io.empty
        io.commit_stall_by_rob          := rob.io.full(7)
        io.commit_stall_by_iq           := VecInit(iq1.io.full, iq2.io.full, iq3.io.full, iq4.io.full, DontCare)
        io.commit_stall_by_sb           := sb.io.full

        io.commit_stall_by_icache       := icache.io.cache_miss_RM
        io.commit_stall_by_dcache       := dcache.io.cache_miss_MEM(4)
        io.commit_icache_miss           := icache.io.commit_icache_miss
        io.commit_dcache_miss           := dcache.io.commit_dcache_miss
        io.commit_icache_visit          := icache.io.commit_icache_visit
        io.commit_dcache_visit          := dcache.io.commit_dcache_visit
        io.commit_stall_by_div          := mdu.io.busy

        io.commit_iq_issue              := VecInit(sel1.io.inst_issue_valid, sel2.io.inst_issue_valid, sel3.io.inst_issue_valid, sel4.io.inst_issue_valid, DontCare)
        io.commit_tlbfill_en            := rob.io.tlbfill_en_cmt
        io.commit_tlbfill_idx           := stable_cnt.io.value(3, 0)
    }
    else {
        io.commit_en                    := DontCare
        io.commit_rd                    := DontCare
        io.commit_prd                   := DontCare
        io.commit_rd_valid              := DontCare
        io.commit_rf_wdata              := DontCare
        io.commit_csr_wdata             := DontCare
        io.commit_csr_we                := DontCare
        io.commit_csr_waddr             := DontCare
        io.commit_pc                    := DontCare
        io.commit_is_ucread             := DontCare
        io.commit_is_br                 := DontCare
        io.commit_br_type               := DontCare
        io.commit_predict_fail          := DontCare
        io.commit_inst                  := DontCare
        io.commit_interrupt             := DontCare
        io.commit_interrupt_type        := DontCare


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
        io.commit_tlbfill_en            := DontCare
        io.commit_tlbfill_idx           := DontCare

    }

}

