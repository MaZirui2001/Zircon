import chisel3._
import chisel3.util._
import PRED_Config._

// LUT: 1119 FF: 425

object ROB_Pack{
    class rob_t extends Bundle(){
        val rd                  = UInt(5.W)
        val rd_valid            = Bool()
        val prd                 = UInt(7.W)
        val pprd                = UInt(7.W)
        val predict_fail        = Bool()
        val branch_target       = UInt(32.W)
        val real_jump           = Bool()
        val pred_update_en      = Bool()
        val br_type_pred        = UInt(2.W)
        val complete            = Bool()
        val pc                  = UInt(30.W)
        val rf_wdata            = UInt(32.W)
        val is_store            = Bool()
        val is_ucread           = Bool()
        val is_priv_wrt         = Bool() 
    }
    class priv_t(n: Int) extends Bundle{
        val valid = Bool()
        val rob_index = UInt(log2Ceil(n).W)
        val priv_vec = UInt(4.W)
        val csr_addr = UInt(14.W)
        val csr_wdata = UInt(32.W)
    }
}
class ROB_IO(n: Int) extends Bundle{
    // for reg rename
    val inst_valid_rn           = Input(Vec(4, Bool()))
    val rd_rn                   = Input(Vec(4, UInt(5.W)))
    val rd_valid_rn             = Input(Vec(4, Bool()))
    val prd_rn                  = Input(Vec(4, UInt(7.W)))
    val pprd_rn                 = Input(Vec(4, UInt(7.W)))
    val rob_index_rn            = Output(Vec(4, UInt(log2Ceil(n).W)))
    val pc_rn                   = Input(Vec(4, UInt(32.W)))
    val is_store_rn             = Input(Vec(4, Bool()))
    val br_type_pred_rn         = Input(Vec(4, UInt(2.W)))
    val pred_update_en_rn       = Input(Vec(4, Bool()))
    val csr_addr_rn             = Input(Vec(4, UInt(14.W)))
    val priv_vec_rn             = Input(Vec(4, UInt(4.W)))
    val full                    = Output(Bool())
    val stall                   = Input(Bool())

    // for wb stage 
    val inst_valid_wb           = Input(Vec(5, Bool()))
    val rob_index_wb            = Input(Vec(5, UInt(log2Ceil(n).W)))
    val is_ucread_wb            = Input(Vec(5, Bool()))
    val predict_fail_wb         = Input(Vec(5, Bool()))
    val real_jump_wb            = Input(Vec(5, Bool()))
    val branch_target_wb        = Input(Vec(5, UInt(32.W)))
    val rf_wdata_wb             = Input(Vec(5, UInt(32.W)))

    // for cpu state: arch rat
    val cmt_en                  = Output(Vec(4, Bool()))

    val prd_cmt                 = Output(Vec(4, UInt(7.W)))
    val rd_valid_cmt            = Output(Vec(4, Bool()))
    val pprd_cmt                = Output(Vec(4, UInt(7.W)))

    // for store buffer
    val is_store_num_cmt        = Output(UInt(2.W))

    // for predict and ras
    val predict_fail_cmt        = Output(Bool())
    val pred_update_en_cmt      = Output(Bool())
    val pred_branch_target_cmt  = Output(UInt(32.W))
    val pred_pc_cmt             = Output(UInt(32.W))
    val pred_real_jump_cmt      = Output(Bool())
    val br_type_pred_cmt        = Output(UInt(2.W))

    // for csr write
    val csr_addr_cmt            = Output(UInt(14.W))
    val csr_wdata_cmt           = Output(UInt(32.W))
    val csr_we_cmt              = Output(Bool())

    // diff
    val is_ucread_cmt           = Output(Vec(4, Bool()))
    val rd_cmt                  = Output(Vec(4, UInt(5.W)))
    val rf_wdata_cmt            = Output(Vec(4, UInt(32.W)))
    val branch_target_cmt       = Output(UInt(32.W))
    val pc_cmt                  = Output(Vec(4, UInt(32.W)))
    val csr_diff_addr_cmt       = Output(Vec(4, UInt(14.W)))
    val csr_diff_wdata_cmt      = Output(Vec(4, UInt(32.W)))
    val csr_diff_we_cmt         = Output(Vec(4, Bool()))

    // stat
    val predict_fail_stat       = Output(Vec(4, Bool()))
    val br_type_stat            = Output(Vec(4, UInt(2.W)))
    val is_br_stat              = Output(Vec(4, Bool()))
}

class ROB(n: Int) extends Module{
    val io = IO(new ROB_IO(n))
    val neach = n / 4
    import ROB_Pack._
    /* ROB items */
    val rob         = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(neach)(0.U.asTypeOf(new rob_t))))))
    val priv_buf    = RegInit(0.U.asTypeOf(new priv_t(n)))

    /* ROB ptrs */
    val head        = RegInit(VecInit(Seq.fill(4)(0.U(log2Ceil(neach).W))))
    val tail        = RegInit((0.U(log2Ceil(neach).W)))
    val elem_num    = RegInit(VecInit(Seq.fill(4)(0.U((log2Ceil(neach)+1).W))))
    val head_sel    = RegInit(0.U(2.W))
    val hsel_idx    = VecInit.tabulate(4)(i => head_sel+i.U)
    val head_idx    = VecInit.tabulate(4)(i => head(hsel_idx(i)))

    /* ROB status */
    val empty       = VecInit(elem_num.map(_ === 0.U))
    val full        = VecInit(elem_num.map(_ === neach.U)).reduce(_||_)

    val inst_valid_rn = io.inst_valid_rn(0)
    // rn stage
    when(!full){
        for(i <- 0 until 4){
            when(inst_valid_rn){
                rob(i)(tail).rd              := io.rd_rn(i)
                rob(i)(tail).rd_valid        := io.rd_valid_rn(i)
                rob(i)(tail).prd             := io.prd_rn(i)
                rob(i)(tail).pprd            := io.pprd_rn(i)
                rob(i)(tail).pc              := io.pc_rn(i)(31, 2)
                rob(i)(tail).is_store        := io.is_store_rn(i)
                rob(i)(tail).br_type_pred    := io.br_type_pred_rn(i)
                rob(i)(tail).pred_update_en  := io.pred_update_en_rn(i)
                rob(i)(tail).complete        := false.B
                rob(i)(tail).is_priv_wrt     := io.priv_vec_rn(i)(0) && io.priv_vec_rn(i)(3, 1).orR
            }
        }
        val priv_bits = VecInit.tabulate(4)(i => io.priv_vec_rn(i)(0) && io.priv_vec_rn(i)(3, 1).orR)
        val priv_index = PriorityEncoder(priv_bits)
        when(!priv_buf.valid && inst_valid_rn && priv_bits.reduce(_||_)){
            priv_buf.csr_addr  := io.csr_addr_rn(priv_index)
            priv_buf.priv_vec  := io.priv_vec_rn(priv_index)
            priv_buf.valid     := true.B
            priv_buf.rob_index := tail ## priv_index(1, 0)
        }

    }
    io.rob_index_rn := VecInit.tabulate(4)(i => tail ## i.U(2.W))

    // wb stage
    for(i <- 0 until 5){
        when(io.inst_valid_wb(i)){
            val col_idx = io.rob_index_wb(i)(1, 0)
            val row_idx = io.rob_index_wb(i)(log2Ceil(n)-1, 2)
            rob(col_idx)(row_idx).complete        := true.B
            rob(col_idx)(row_idx).predict_fail    := io.predict_fail_wb(i)
            rob(col_idx)(row_idx).branch_target   := io.branch_target_wb(i)
            rob(col_idx)(row_idx).rf_wdata        := io.rf_wdata_wb(i)
            rob(col_idx)(row_idx).real_jump       := io.real_jump_wb(i)
            rob(col_idx)(row_idx).is_ucread       := io.is_ucread_wb(i)
        }
    }
    when(io.inst_valid_wb(1) && io.rob_index_wb(1) === priv_buf.rob_index){
        priv_buf.csr_wdata := io.branch_target_wb(1)
    }
    
    // cmt stage
    io.cmt_en(0) := rob(hsel_idx(0))(head_idx(0)).complete && !empty(head_sel)
    for(i <- 1 until 4){
        io.cmt_en(i) := (io.cmt_en(i-1) && rob(hsel_idx(i))(head_idx(i)).complete 
                        && !rob(hsel_idx(i-1))(head_idx(i-1)).pred_update_en 
                        && !rob(hsel_idx(i-1))(head_idx(i-1)).is_priv_wrt
                        && !empty(hsel_idx(i)))
    }
    io.full := full

    // update predict and ras
    val rob_commit_items        = VecInit.tabulate(4)(i => rob(hsel_idx(i))(head_idx(i)))
    val pred_update_bits        = VecInit.tabulate(4)(i => rob_commit_items(i).pred_update_en && io.cmt_en(i)).asUInt
    val pred_update_item        = Mux(pred_update_bits.orR, rob_commit_items(OHToUInt(pred_update_bits)), 0.U.asTypeOf(new rob_t))
    val csr_update_bits         = VecInit.tabulate(4)(i => rob_commit_items(i).is_priv_wrt && io.cmt_en(i)).asUInt
    val csr_update_item         = Mux(csr_update_bits.orR, rob_commit_items(OHToUInt(csr_update_bits)), 0.U.asTypeOf(new rob_t))

    io.predict_fail_cmt         := pred_update_item.predict_fail || csr_update_bits.orR
    io.branch_target_cmt        := Mux(csr_update_bits.orR, (csr_update_item.pc ## 0.U(2.W)) + 4.U, Mux(pred_update_item.real_jump, pred_update_item.branch_target, (pred_update_item.pc ## 0.U(2.W)) + 4.U))
    io.pred_update_en_cmt       := pred_update_item.pred_update_en
    io.pred_branch_target_cmt   := pred_update_item.branch_target
    io.br_type_pred_cmt         := pred_update_item.br_type_pred
    io.pred_pc_cmt              := pred_update_item.pc ## 0.U(2.W)
    io.pred_real_jump_cmt       := pred_update_item.real_jump


    // update store buffer
    val is_store_cmt_bit        = VecInit.tabulate(4)(i => rob_commit_items(i).is_store && io.cmt_en(i))
    io.is_store_num_cmt         := PopCount(is_store_cmt_bit)

    // update csr file
    io.csr_addr_cmt             := priv_buf.csr_addr
    io.csr_wdata_cmt            := priv_buf.csr_wdata
    io.csr_we_cmt               := csr_update_bits.orR && priv_buf.priv_vec(2, 1).orR
    when(io.predict_fail_cmt){
        priv_buf.valid          := false.B
    }
    
    io.rd_cmt                   := rob_commit_items.map(_.rd)
    io.rd_valid_cmt             := rob_commit_items.map(_.rd_valid)
    io.prd_cmt                  := rob_commit_items.map(_.prd)
    io.pprd_cmt                 := rob_commit_items.map(_.pprd)
    io.pc_cmt                   := VecInit.tabulate(4)(i => Mux(rob_commit_items(i).real_jump, rob_commit_items(i).branch_target, (rob_commit_items(i).pc ## 0.U(2.W)) + 4.U))
    io.rf_wdata_cmt             := rob_commit_items.map(_.rf_wdata)
    io.is_ucread_cmt            := VecInit.tabulate(4)(i => rob_commit_items(i).is_ucread && io.cmt_en(i))
    io.csr_diff_addr_cmt        := VecInit.fill(4)(priv_buf.csr_addr)
    io.csr_diff_wdata_cmt       := VecInit.fill(4)(priv_buf.csr_wdata)
    io.csr_diff_we_cmt          := VecInit.tabulate(4)(i => Mux(rob_commit_items(i).is_priv_wrt, priv_buf.priv_vec(2, 1).orR, false.B))

    
    // update ptrs
    head_sel                    := Mux(io.predict_fail_cmt, 0.U, head_sel + PopCount(io.cmt_en))
    val head_inc                = VecInit(Seq.fill(4)(false.B))
    for(i <- 0 until 4){
        head(hsel_idx(i))       := Mux(io.predict_fail_cmt, 0.U, Mux(head_idx(i) + io.cmt_en(i) === neach.U, 0.U, head_idx(i) + io.cmt_en(i)))
        head_inc(hsel_idx(i))   := io.cmt_en(i)
        elem_num(i)             := Mux(io.predict_fail_cmt, 0.U, Mux(!full && !io.stall, elem_num(i) + inst_valid_rn - head_inc(i), elem_num(i) - head_inc(i)))
    }
    tail := Mux(io.predict_fail_cmt, 0.U, Mux(!full && !io.stall, Mux(tail + inst_valid_rn === neach.U, 0.U, tail + inst_valid_rn), tail))


    // stat
    io.predict_fail_stat        := VecInit.tabulate(4)(i => rob(head_sel+i.U)(head(head_sel+i.U)).predict_fail & io.cmt_en(i))
    io.br_type_stat             := VecInit.tabulate(4)(i => rob(head_sel+i.U)(head(head_sel+i.U)).br_type_pred)
    io.is_br_stat               := VecInit.tabulate(4)(i => rob(head_sel+i.U)(head(head_sel+i.U)).pred_update_en & io.cmt_en(i))
} 