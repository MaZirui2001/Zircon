import chisel3._
import chisel3.util._
import Predict_Config._
import CPU_Config._
import TLB_Struct._

class ROB_IO(n: Int) extends Bundle{
    // for reg rename
    val inst_valid_dp           = Input(Vec(2, Bool()))
    val rd_dp                   = Input(Vec(2, UInt(5.W)))
    val rd_valid_dp             = Input(Vec(2, Bool()))
    val prd_dp                  = Input(Vec(2, UInt(log2Ceil(PREG_NUM).W)))
    val pprd_dp                 = Input(Vec(2, UInt(log2Ceil(PREG_NUM).W)))
    val rob_index_dp            = Output(Vec(2, UInt(log2Ceil(n).W)))
    val pc_dp                   = Input(Vec(2, UInt(32.W)))
    val is_store_dp             = Input(Vec(2, Bool()))
    val br_type_pred_dp         = Input(Vec(2, UInt(2.W)))
    val pred_update_en_dp       = Input(Vec(2, Bool()))
    val priv_vec_dp             = Input(Vec(2, UInt(13.W)))
    val exception_dp            = Input(Vec(2, UInt(8.W)))
    val inst_dp                 = Input(Vec(2, UInt(32.W)))
    val full                    = Output(Vec(10, Bool()))
    val stall                   = Input(Bool())

    // for wb stage 
    val inst_valid_wb           = Input(Vec(4, Bool()))
    val rob_index_wb            = Input(Vec(4, UInt(log2Ceil(n).W)))
    val exception_wb            = Input(Vec(4, UInt(8.W)))
    val is_ucread_wb            = Input(Vec(4, Bool()))
    val predict_fail_wb         = Input(Vec(4, Bool()))
    val real_jump_wb            = Input(Vec(4, Bool()))
    val branch_target_wb        = Input(Vec(4, UInt(32.W)))
    val rf_wdata_wb             = Input(Vec(4, UInt(32.W)))

    // for cpu state: arch rat
    val cmt_en                  = Output(Vec(2, Bool()))
    val prd_cmt                 = Output(Vec(2, UInt(log2Ceil(PREG_NUM).W)))
    val rd_valid_cmt            = Output(Vec(2, Bool()))
    val pprd_cmt                = Output(Vec(2, UInt(log2Ceil(PREG_NUM).W)))

    // for store buffer
    val is_store_num_cmt        = Output(UInt(2.W))

    // for predict and ras
    val predict_fail_cmt        = Output(UInt(10.W)) // opt fanout
    val pred_update_en_cmt      = Output(Bool())
    val pred_branch_target_cmt  = Output(UInt(32.W))
    val pred_pc_cmt             = Output(UInt(32.W))
    val pred_real_jump_cmt      = Output(Bool())
    val pred_br_type_cmt        = Output(UInt(2.W))

    // for csr write
    val csr_addr_cmt            = Output(UInt(14.W))
    val csr_wdata_cmt           = Output(UInt(32.W))
    val csr_we_cmt              = Output(Bool())

    // for dcache
    val rob_index_cmt           = Output(UInt(log2Ceil(n).W))

    // for exception
    val eentry_global           = Input(UInt(32.W))
    val tlbreentry_global       = Input(UInt(32.W))
    val badv_cmt                = Output(UInt(32.W))
    val exception_cmt           = Output(UInt(8.W))
    val is_eret_cmt             = Output(Bool())
    val interrupt_vec           = Input(UInt(13.W))

    // for tlb
    val tlbwr_en_cmt            = Output(Bool())
    val tlbrd_en_cmt            = Output(Bool())
    val tlbfill_en_cmt          = Output(Bool())
    val tlbsrch_en_cmt          = Output(Bool())
    val invtlb_en_cmt           = Output(Bool())
    val invtlb_op_cmt           = Output(UInt(5.W))
    val invtlb_vaddr_cmt        = Output(UInt(32.W))
    val invtlb_asid_cmt         = Output(UInt(10.W))
    val llbit_set_cmt           = Output(Bool())
    val llbit_clear_cmt         = Output(Bool())

    // for idle
    val idle_en_cmt             = Output(Bool())

    // for priv
    val priv_vec_ex             = Input(UInt(10.W))
    val csr_addr_ex             = Input(UInt(14.W))
    val tlbentry_ex             = Input(new tlb_t)
    val tlbentry_cmt            = Output(new tlb_t)
    val invtlb_op_ex            = Input(UInt(5.W))
    val invtlb_vaddr_ex         = Input(UInt(32.W))
    val invtlb_asid_ex          = Input(UInt(10.W))

    // for ls priv
    val priv_vec_ls             = Input(UInt(3.W))
    val llbit_global            = Input(Bool())

    // diff
    val is_ucread_cmt           = Output(Vec(2, Bool()))
    val rd_cmt                  = Output(Vec(2, UInt(5.W)))
    val rf_wdata_cmt            = Output(Vec(2, UInt(32.W)))
    val branch_target_cmt       = Output(UInt(32.W))
    val pc_cmt                  = Output(Vec(2, UInt(32.W)))
    val csr_diff_addr_cmt       = Output(Vec(2, UInt(14.W)))
    val csr_diff_wdata_cmt      = Output(Vec(2, UInt(32.W)))
    val csr_diff_we_cmt         = Output(Vec(2, Bool()))
    val inst_cmt                = Output(Vec(2, UInt(32.W)))

    // stat
    val predict_fail_stat       = Output(Vec(2, Bool()))
    val br_type_stat            = Output(Vec(2, UInt(2.W)))
    val is_br_stat              = Output(Vec(2, Bool()))
}

class ROB(n: Int) extends Module{
    val io              = IO(new ROB_IO(n))
    val FRONT_LOG2      = 1
    val neach           = n / 2
    import ROB_Struct._
    /* ROB items */
    val rob         = RegInit(VecInit.fill(2)(VecInit.fill(neach)(0.U.asTypeOf(new rob_t))))
    val priv_buf    = RegInit(0.U.asTypeOf(new priv_t(n)))
    val priv_ls_buf = RegInit(0.U.asTypeOf(new priv_ls_t))
    val int_vec_buf = RegInit(0.U(13.W))
 
    /* ROB ptrs */
    val head        = RegInit(0.U(log2Ceil(n).W))
    val head_plus_1 = RegInit(1.U(log2Ceil(n).W))
    val head_each   = VecInit(head, head_plus_1)
    // val head_each   = VecInit.tabulate(2)(i => head + i.U(log2Ceil(n).W)))
    val tail        = RegInit(0.U(log2Ceil(neach).W))
    val elem_num    = RegInit(VecInit.fill(10)(VecInit.fill(2)(0.U((log2Ceil(neach)+1).W))))
    val hsel_idx    = VecInit.tabulate(2)(i => head_each(i)(FRONT_LOG2-1, 0))
    val head_idx    = VecInit.tabulate(2)(i => head_each(i)(log2Ceil(n)-1, FRONT_LOG2))

    /* ROB status */
    val empty       = VecInit(elem_num(0).map(_ === 0.U))
    val full        = VecInit.tabulate(10)(i => VecInit(elem_num(i).map(_ === neach.U)).asUInt.orR)

    val inst_valid_dp = io.inst_valid_dp(0)
    // rn stage
    when(!full(0)){
        for(i <- 0 until 2){
            when(inst_valid_dp){
                rob(i)(tail).rd              := io.rd_dp(i)
                rob(i)(tail).rd_valid        := io.rd_valid_dp(i)
                rob(i)(tail).prd             := io.prd_dp(i)
                rob(i)(tail).pprd            := io.pprd_dp(i)
                rob(i)(tail).pc              := io.pc_dp(i) + 4.U
                rob(i)(tail).is_store        := io.is_store_dp(i)
                rob(i)(tail).br_type_pred    := io.br_type_pred_dp(i)
                rob(i)(tail).pred_update_en  := io.pred_update_en_dp(i)
                rob(i)(tail).predict_fail    := false.B
                rob(i)(tail).complete        := false.B
                rob(i)(tail).is_priv_wrt     := io.priv_vec_dp(i)(0) && io.priv_vec_dp(i)(9, 1).orR
                rob(i)(tail).is_priv_ls      := io.priv_vec_dp(i)(12, 10).orR
                rob(i)(tail).exception       := io.exception_dp(i)
                rob(i)(tail).inst            := io.inst_dp(i)
                rob(i)(tail).allow_next_cmt  := !(io.pred_update_en_dp(i) || io.priv_vec_dp(i)(0) && io.priv_vec_dp(i)(12, 1).orR || io.exception_dp(i)(7))
            }
        }
    }
    io.rob_index_dp := VecInit.tabulate(2)(i => tail ## i.U(FRONT_LOG2.W))
    io.full         := full

    // ex stage
    when(io.predict_fail_cmt(0)){
        priv_buf.valid          := false.B
        // priv_buf.priv_vec       := 0.U
    }.elsewhen(!priv_buf.valid && io.priv_vec_ex(0) && io.priv_vec_ex(9, 1).orR){
        priv_buf.csr_addr       := io.csr_addr_ex
        priv_buf.priv_vec       := io.priv_vec_ex
        priv_buf.tlb_entry      := io.tlbentry_ex
        priv_buf.inv_op         := io.invtlb_op_ex
        priv_buf.inv_vaddr      := io.invtlb_vaddr_ex
        priv_buf.inv_asid       := io.invtlb_asid_ex
        priv_buf.valid          := true.B
    }
    when(io.predict_fail_cmt(0)){
        priv_ls_buf.valid       := false.B
    }.elsewhen(!priv_ls_buf.valid && io.priv_vec_ls.orR){
        priv_ls_buf.priv_vec    := io.priv_vec_ls
        priv_ls_buf.valid       := true.B
    }
    when(io.cmt_en(0)){
        int_vec_buf             := 0.U(1.W) ## io.interrupt_vec
    }.elsewhen(!int_vec_buf(12)){
        int_vec_buf             := 1.U(1.W) ## io.interrupt_vec
    }

    // wb stage
    for(i <- 0 until 4){
        when(io.inst_valid_wb(i)){
            val col_idx = io.rob_index_wb(i)(FRONT_LOG2-1, 0)
            val row_idx = io.rob_index_wb(i)(log2Ceil(n)-1, FRONT_LOG2)
            rob(col_idx)(row_idx).complete        := true.B
            rob(col_idx)(row_idx).rf_wdata        := io.rf_wdata_wb(i)
            rob(col_idx)(row_idx).is_ucread       := io.is_ucread_wb(i)
            if(i != 0){
                if(i == 2){
                    rob(col_idx)(row_idx).branch_target   := Mux(rob(col_idx)(row_idx).exception(7), rob(col_idx)(row_idx).pc - 4.U, io.branch_target_wb(i))
                }else{
                    rob(col_idx)(row_idx).branch_target   := io.branch_target_wb(i)
                }
            }
            if(i == 1){
                rob(col_idx)(row_idx).predict_fail      := io.predict_fail_wb(i)
                rob(col_idx)(row_idx).real_jump         := io.real_jump_wb(i)
            }
            if(i == 3){
                rob(col_idx)(row_idx).exception         := io.exception_wb(i)
                rob(col_idx)(row_idx).allow_next_cmt    := !io.exception_wb(i)(7)
            }
        }
    }
    
    // cmt stage
    val interrupt_vec           = int_vec_buf(11, 0).orR 
    val cmt_en                  = Wire(Vec(2, Bool()))
    val rob_commit_items        = VecInit.tabulate(2)(i => rob(hsel_idx(i))(head_idx(i)))

    cmt_en(0)                   := rob_commit_items(0).complete && !empty(hsel_idx(0))
    cmt_en(1)                   := rob_commit_items(1).complete && !empty(hsel_idx(1)) && cmt_en(0) && rob_commit_items(0).allow_next_cmt && !interrupt_vec
    
    io.cmt_en                   := ShiftRegister(cmt_en, 1)
    io.rob_index_cmt            := ShiftRegister(head, 1)

    val eentry_global           = ShiftRegister(io.eentry_global, 1);
    val tlbreentry_global       = ShiftRegister(io.tlbreentry_global, 1);
    val interrupt               = interrupt_vec && cmt_en(0)

    // update predict and ras
    val rob_update_item         = Mux(cmt_en(0), Mux(cmt_en(1), rob_commit_items(1), rob_commit_items(0)), 0.U.asTypeOf(new rob_t))

    val predict_fail_cmt        = rob_update_item.predict_fail || rob_update_item.is_priv_wrt || rob_update_item.is_priv_ls || rob_update_item.exception(7) || interrupt
    val branch_target_cmt       = Mux(rob_update_item.exception(7) || interrupt_vec, Mux(rob_update_item.exception(5, 0) === 0x3f.U, tlbreentry_global, eentry_global), 
                                  Mux(rob_update_item.is_priv_wrt && priv_buf.priv_vec(3) || rob_update_item.pred_update_en && rob_update_item.real_jump, rob_update_item.branch_target, rob_update_item.pc))
    val pred_update_en_cmt      = rob_update_item.pred_update_en
    val pred_branch_target_cmt  = rob_update_item.branch_target
    val pred_br_type_cmt        = rob_update_item.br_type_pred
    val pred_pc_cmt             = rob_update_item.pc - 4.U
    val pred_real_jump_cmt      = rob_update_item.real_jump
    val exception_cmt           = Mux(interrupt, 0x80.U(8.W), rob_update_item.exception)

    io.predict_fail_cmt         := ShiftRegister(VecInit.fill(10)(predict_fail_cmt).asUInt, 1)
    io.branch_target_cmt        := ShiftRegister(branch_target_cmt, 1)
    io.pred_update_en_cmt       := ShiftRegister(pred_update_en_cmt, 1)
    io.pred_branch_target_cmt   := ShiftRegister(pred_branch_target_cmt, 1)
    io.pred_br_type_cmt         := ShiftRegister(pred_br_type_cmt, 1)
    io.pred_pc_cmt              := ShiftRegister(pred_pc_cmt, 1)
    io.pred_real_jump_cmt       := ShiftRegister(pred_real_jump_cmt, 1)
    io.exception_cmt            := ShiftRegister(exception_cmt, 1)


    // update store buffer
    val is_store_cmt_bit        = VecInit.tabulate(2)(i => rob_commit_items(i).is_store && cmt_en(i) && !rob_commit_items(i).exception(7) && !(rob_commit_items(i).is_priv_ls && !RegNext(io.llbit_global)))
    val is_store_num_cmt        = PopCount(is_store_cmt_bit)
    io.is_store_num_cmt         := ShiftRegister(is_store_num_cmt, 1)

    // update csr file
    val csr_addr_cmt            = priv_buf.csr_addr
    val csr_wdata_cmt           = rob_update_item.branch_target
    val csr_we_cmt              = rob_update_item.is_priv_wrt && priv_buf.priv_vec(2, 1).orR
    val is_eret_cmt             = rob_update_item.is_priv_wrt && priv_buf.priv_vec(3)
    val badv_cmt                = rob_update_item.branch_target
    val tlbrd_en_cmt            = rob_update_item.is_priv_wrt && priv_buf.priv_vec(4)
    val tlbwr_en_cmt            = rob_update_item.is_priv_wrt && priv_buf.priv_vec(5)
    val tlbfill_en_cmt          = rob_update_item.is_priv_wrt && priv_buf.priv_vec(6)
    val tlbsrch_en_cmt          = rob_update_item.is_priv_wrt && priv_buf.priv_vec(7)
    val tlbentry_cmt            = priv_buf.tlb_entry
    val invtlb_en_cmt           = rob_update_item.is_priv_wrt && priv_buf.priv_vec(8)
    val idle_en_cmt             = rob_update_item.is_priv_wrt && priv_buf.priv_vec(9)
    val invtlb_op_cmt           = priv_buf.inv_op
    val invtlb_vaddr_cmt        = priv_buf.inv_vaddr
    val invtlb_asid_cmt         = priv_buf.inv_asid
    val llbit_set_cmt           = rob_update_item.is_priv_ls && priv_ls_buf.priv_vec(1)
    val llbit_clear_cmt         = rob_update_item.is_priv_ls && priv_ls_buf.priv_vec(2)

    io.csr_addr_cmt             := ShiftRegister(csr_addr_cmt, 1)
    io.csr_wdata_cmt            := ShiftRegister(csr_wdata_cmt, 1)
    io.csr_we_cmt               := ShiftRegister(csr_we_cmt, 1)
    io.is_eret_cmt              := ShiftRegister(is_eret_cmt, 1)
    io.badv_cmt                 := ShiftRegister(badv_cmt, 1)
    io.tlbrd_en_cmt             := ShiftRegister(tlbrd_en_cmt, 1)
    io.tlbwr_en_cmt             := ShiftRegister(tlbwr_en_cmt, 1)
    io.tlbfill_en_cmt           := ShiftRegister(tlbfill_en_cmt, 1)
    io.tlbsrch_en_cmt           := ShiftRegister(tlbsrch_en_cmt, 1)
    io.tlbentry_cmt             := ShiftRegister(tlbentry_cmt, 1)
    io.invtlb_en_cmt            := ShiftRegister(invtlb_en_cmt, 1)
    io.invtlb_op_cmt            := ShiftRegister(invtlb_op_cmt, 1)
    io.invtlb_vaddr_cmt         := ShiftRegister(invtlb_vaddr_cmt, 1)
    io.invtlb_asid_cmt          := ShiftRegister(invtlb_asid_cmt, 1)
    io.idle_en_cmt              := ShiftRegister(idle_en_cmt, 1)
    io.llbit_set_cmt            := ShiftRegister(llbit_set_cmt, 1)
    io.llbit_clear_cmt          := ShiftRegister(llbit_clear_cmt, 1)

    // update ptrs
    val cmt_num                 = PopCount(cmt_en)
    head                        := Mux(io.predict_fail_cmt(0) || predict_fail_cmt, 0.U, Mux(head + cmt_num >= n.U, head + cmt_num - n.U, head + cmt_num))
    head_plus_1                 := Mux(io.predict_fail_cmt(0) || predict_fail_cmt, 1.U, Mux(head_plus_1 + cmt_num >= n.U, head_plus_1 + cmt_num - n.U, head_plus_1 + cmt_num))                 
    val head_inc                = VecInit.fill(2)(false.B)
    for(i <- 0 until 2){
        head_inc(hsel_idx(i))   := cmt_en(i)
        for(j <- 0 until 10){
            elem_num(j)(i)      := Mux(io.predict_fail_cmt(0) || predict_fail_cmt, 0.U, Mux(!full(i) && !io.stall, elem_num(j)(i) + inst_valid_dp - head_inc(i), elem_num(j)(i) - head_inc(i)))
        }
        
    }
    tail                        := Mux(io.predict_fail_cmt(0) || predict_fail_cmt, 0.U, Mux(!full(1) && !io.stall, Mux(tail + inst_valid_dp === neach.U, 0.U, tail + inst_valid_dp), tail))


    // stat
    val rd_cmt                   = VecInit.tabulate(2)(i => rob_commit_items(i).rd)
    val rd_valid_cmt             = VecInit.tabulate(2)(i => rob_commit_items(i).rd_valid && !rob_commit_items(i).exception(7))
    val prd_cmt                  = VecInit.tabulate(2)(i => rob_commit_items(i).prd)
    val pprd_cmt                 = VecInit.tabulate(2)(i => rob_commit_items(i).pprd)
    val pc_cmt                   = VecInit.tabulate(2)(i => Mux(rob_commit_items(i).exception(7) || interrupt, Mux(rob_commit_items(i).exception(5, 0) === 0x3f.U, tlbreentry_global, eentry_global), 
                                                            Mux(rob_commit_items(i).is_priv_wrt && priv_buf.priv_vec(3) || rob_commit_items(i).pred_update_en && rob_commit_items(i).real_jump, rob_commit_items(i).branch_target, rob_commit_items(i).pc)))
    val rf_wdata_cmt             = VecInit.tabulate(2)(i => rob_commit_items(i).rf_wdata)
    val is_ucread_cmt            = VecInit.tabulate(2)(i => rob_commit_items(i).is_ucread && cmt_en(i))
    val csr_diff_addr_cmt        = VecInit.fill(2)(priv_buf.csr_addr)
    val csr_diff_wdata_cmt       = VecInit.fill(2)(rob_update_item.branch_target)
    val csr_diff_we_cmt          = VecInit.tabulate(2)(i => Mux(rob_commit_items(i).is_priv_wrt, priv_buf.priv_vec(2, 1).orR, false.B))
    val inst_cmt                 = VecInit.tabulate(2)(i => rob_commit_items(i).inst)

    io.rd_valid_cmt             := ShiftRegister(rd_valid_cmt, 1)
    io.rd_cmt                   := ShiftRegister(rd_cmt, 1)
    io.prd_cmt                  := ShiftRegister(prd_cmt, 1)
    io.pprd_cmt                 := ShiftRegister(pprd_cmt, 1)
    io.pc_cmt                   := ShiftRegister(pc_cmt, 1)
    io.rf_wdata_cmt             := ShiftRegister(rf_wdata_cmt, 1)
    io.is_ucread_cmt            := ShiftRegister(is_ucread_cmt, 1)
    io.csr_diff_addr_cmt        := ShiftRegister(csr_diff_addr_cmt, 1)
    io.csr_diff_wdata_cmt       := ShiftRegister(csr_diff_wdata_cmt, 1)
    io.csr_diff_we_cmt          := ShiftRegister(csr_diff_we_cmt, 1)
    io.predict_fail_stat        := ShiftRegister(VecInit.tabulate(2)(i => rob(hsel_idx(i))(head_idx(i)).predict_fail & cmt_en(i)), 1)
    io.br_type_stat             := ShiftRegister(VecInit.tabulate(2)(i => rob(hsel_idx(i))(head_idx(i)).br_type_pred), 1)
    io.is_br_stat               := ShiftRegister(VecInit.tabulate(2)(i => rob(hsel_idx(i))(head_idx(i)).pred_update_en & cmt_en(i)), 1)
    io.inst_cmt                 := ShiftRegister(inst_cmt, 1)
} 