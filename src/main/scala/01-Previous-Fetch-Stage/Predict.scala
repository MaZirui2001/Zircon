import chisel3._
import chisel3.util._
import CPU_Config._
import Predict_Config._
import Predict_Struct._

class Predict_IO extends Bundle{
    // check
    val npc                 = Input(Vec(10, UInt(32.W)))
    val pc                  = Input(Vec(10, UInt(32.W)))
    val predict_jump        = Output(Vec(2, Bool()))
    val pred_npc            = Output(UInt(32.W))
    val pred_valid          = Output(Vec(2, Bool()))
    val pc_stall            = Input(Bool())

    // update   
    val pc_cmt              = Input(UInt(32.W))
    val real_jump           = Input(Bool())
    val branch_target       = Input(UInt(32.W))
    val update_en           = Input(Bool())
    val br_type             = Input(UInt(2.W))

    // recover 
    val top_arch            = Input(UInt(3.W))
    val ras_arch            = Input(Vec(8, UInt(32.W)))
    val predict_fail        = Input(Bool())
    val pd_pred_fix         = Input(Bool())
    val pd_pred_fix_is_bl   = Input(Bool())
    val pd_pc_plus_4        = Input(UInt(32.W))
    
}
class Predict extends Module{
    
    val io = IO(new Predict_IO)

    val btb_tagv    = VecInit.fill(2)(Module(new xilinx_simple_dual_port_1_clock_ram_read_first(BTB_TAG_WIDTH+1, BTB_DEPTH)).io)
    val btb_targ    = VecInit.fill(2)(Module(new xilinx_simple_dual_port_1_clock_ram_read_first(30+2, BTB_DEPTH)).io)
    val bht         = RegInit(VecInit.fill(2)(VecInit.fill(BHT_DEPTH)(0.U(4.W))))
    val pht         = RegInit(VecInit.fill(2)(VecInit.fill(PHT_DEPTH)(2.U(2.W))))

    val ras         = RegInit(VecInit.fill(8)(0x1c000000.U(32.W)))
    val jirl_sel    = RegInit(2.U(2.W))
    val top         = RegInit(0x7.U(3.W))

    // check
    val npc             = io.npc
    val pc              = io.pc
    val pc_cmt          = io.pc_cmt
    val cmt_col         = pc_cmt(2)
    
    val btb_rindex      = VecInit.tabulate(2)(i => npc(i)(3+BTB_INDEX_WIDTH-1, 3))
    val btb_rdata       = Wire(Vec(2, new btb_t))

    val bht_rindex      = VecInit.tabulate(2)(i => pc(i)(3+BHT_INDEX_WIDTH-1, 3))
    val bht_rdata       = VecInit.tabulate(2)(i => bht(i)(bht_rindex(i)))

    val pht_rindex      = VecInit.tabulate(2)(i => bht_rdata(i)(3, 2)  ## (bht_rdata(i)(1, 0) ^ pc(i+2)(PHT_INDEX_WIDTH, PHT_INDEX_WIDTH-1)) ## pc(i+2)(PHT_INDEX_WIDTH-2, 3))
    val pht_rdata       = VecInit.tabulate(2)(i => pht(i)(pht_rindex(i)))

    val predict_valid   = VecInit.tabulate(2)(i => btb_rdata(i).valid && !(btb_rdata(i).tag ^ pc(i+4)(31, 32 - BTB_TAG_WIDTH)))
    val predict_jump    = VecInit.tabulate(2)(i => (pht_rdata(i)(1)) && predict_valid(i))

    val valid_mask      = true.B ## !pc(6)(2)
    val pred_hit        = VecInit.tabulate(2)(i => predict_jump(i) && valid_mask(i))
    val pred_valid_hit  = VecInit.tabulate(2)(i => predict_valid(i) && valid_mask(i))

    val pred_hit_index  = !pred_hit(0)

    io.predict_jump     := (pred_hit(1) ## Mux(pc(6)(2), pred_hit(1), pred_hit(0))).asBools
    io.pred_valid       := (pred_valid_hit(1) ## Mux(pc(6)(2), pred_valid_hit(1), pred_valid_hit(0))).asBools
    io.pred_npc         := Mux(btb_rdata(pred_hit_index).typ === RET, ras(top-1.U) + 4.U, btb_rdata(pred_hit_index).target ## 0.U(2.W)) 
    
    // update
    val update_en       = io.update_en
    // btb
    val mask            = UIntToOH(cmt_col)
    val btb_wdata       = Wire(Vec(2, new btb_t))
    val btb_windex      = pc_cmt(3-1+BTB_INDEX_WIDTH, 3)

    for (i <- 0 until 2){
        btb_wdata(i).valid  := true.B
        btb_wdata(i).target := io.branch_target(31, 2)
        btb_wdata(i).tag    := pc_cmt(31, 32-BTB_TAG_WIDTH)
        btb_wdata(i).typ    := io.br_type
    }
    for(i <- 0 until 2){
        btb_tagv(i).addra   := btb_windex
        btb_tagv(i).addrb   := btb_rindex(i)
        btb_tagv(i).dina    := btb_wdata(i).valid ## btb_wdata(i).tag
        btb_tagv(i).clka    := clock
        btb_tagv(i).wea     := update_en && mask(i)
        btb_rdata(i).valid  := btb_tagv(i).doutb(BTB_TAG_WIDTH)
        btb_rdata(i).tag    := btb_tagv(i).doutb(BTB_TAG_WIDTH-1, 0)
    }
    for(i <- 0 until 2){
        btb_targ(i).addra   := btb_windex
        btb_targ(i).addrb   := btb_rindex(i)
        btb_targ(i).dina    := btb_wdata(i).target ## btb_wdata(i).typ
        btb_targ(i).clka    := clock
        btb_targ(i).wea     := update_en && mask(i)
        btb_rdata(i).target := btb_targ(i).doutb(31, 2)
        btb_rdata(i).typ    := btb_targ(i).doutb(1, 0)
    }

    // bht
    val bht_windex = pc_cmt(3-1+BHT_INDEX_WIDTH, 3)
    val bht_wdata = io.real_jump
    when(update_en){
        bht(cmt_col)(bht_windex) := bht_wdata ## bht(cmt_col)(bht_windex)(3, 1)
    }

    // pht
    val pht_windex = bht(cmt_col)(bht_windex)(3, 2) ## (bht(cmt_col)(bht_windex)(1, 0) ^ pc_cmt(PHT_INDEX_WIDTH, PHT_INDEX_WIDTH-1)) ## pc_cmt(PHT_INDEX_WIDTH-2, 3)
    val pht_raw_rdata = pht(cmt_col)(pht_windex)

    when(update_en){
        pht(cmt_col)(pht_windex) := Mux(io.real_jump, 
                                        pht_raw_rdata + (pht_raw_rdata =/= 3.U), 
                                        pht_raw_rdata - (pht_raw_rdata =/= 0.U))
    }

    // RAS
    when(io.predict_fail){
        top := io.top_arch
        ras := io.ras_arch
    }
    .elsewhen(io.pd_pred_fix){
        when(io.pd_pred_fix_is_bl){
            top             := top + 1.U
            ras(top + 1.U)  := io.pd_pc_plus_4
        }
    }.elsewhen(btb_rdata(pred_hit_index).typ(1) && pred_valid_hit(pred_hit_index)){
        top                 := top + 1.U
        ras(top + 1.U)      := pc(6)(31, 3) ## pred_hit_index ## 0.U(2.W)
    }.elsewhen(btb_rdata(pred_hit_index).typ === RET && pred_valid_hit(pred_hit_index)){
        top             := top - 1.U
    }

    when(io.update_en && io.br_type === RET){
        jirl_sel := Mux(io.predict_fail, Mux(jirl_sel(0), 2.U, 1.U), Mux(jirl_sel(1), 3.U, 0.U))
    }

}

