import chisel3._
import chisel3.util._
object PRED_Config{
    val BTB_INDEX_WIDTH = 8
    val BTB_TAG_WIDTH = 28 - BTB_INDEX_WIDTH
    val BTB_DEPTH = 1 << BTB_INDEX_WIDTH
    class btb_t extends Bundle{
        val valid = Bool()
        val target = UInt(30.W)
        val tag = UInt(BTB_TAG_WIDTH.W)
        val typ = UInt(2.W)
    }
    val BHT_INDEX_WIDTH = 4
    val BHT_DEPTH = 1 << BHT_INDEX_WIDTH
    val PHT_INDEX_WIDTH = 6
    val PHT_DEPTH = 1 << PHT_INDEX_WIDTH

    val JIRL = 1.U(2.W)
    val BL = 2.U(2.W)
    val ELSE = 0.U(2.W)
}

class Predict_IO extends Bundle{
    // check
    val npc = Input(UInt(32.W))
    val pc = Input(UInt(32.W))
    val predict_jump = Output(Vec(4, Bool()))
    val pred_npc = Output(UInt(32.W))
    val pred_valid = Output(Vec(4, Bool()))

    // update
    val pc_cmt = Input(UInt(32.W))
    val real_jump = Input(Bool())
    val branch_target = Input(UInt(32.W))
    val update_en = Input(Bool())
    val br_type = Input(UInt(2.W))
    val ras_update_en = Input(Bool())

    // recover 
    val top_arch     = Input(UInt(4.W))
    val predict_fail = Input(Bool())
    val pd_pred_fix = Input(Bool())
    val pd_pred_fix_is_bl = Input(Bool())
    val pd_pc_plus_4 = Input(UInt(32.W))
}

import PRED_Config._
class Predict extends Module{
    val io = IO(new Predict_IO)

    val btb_tagv = VecInit(Seq.fill(4)(Module(new xilinx_simple_dual_port_1_clock_ram(BTB_TAG_WIDTH+1, BTB_DEPTH)).io))
    val btb_targ = VecInit(Seq.fill(4)(Module(new xilinx_simple_dual_port_1_clock_ram(30+2, BTB_DEPTH)).io))
    val bht = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(16)(0.U(4.W))))))
    val pht = RegInit(VecInit(Seq.fill(4)(VecInit(Seq.fill(64)(2.U(2.W))))))

    val ras = RegInit(VecInit(Seq.fill(16)(0x1c000000.U(32.W))))
    val top = RegInit(0.U(4.W))
    val jirl_sel = RegInit(0.U(2.W))

    // check
    val npc             = io.npc
    val pc              = io.pc
    val btb_rindex      = npc(4-1+BTB_INDEX_WIDTH, 4)
    val btb_rdata       = Wire(Vec(4, new btb_t))

    val bht_rindex      = pc(7, 4) 
    val bht_rdata       = VecInit(bht.map(_(bht_rindex)))

    val pht_rindex      = VecInit(Seq.tabulate(4)(i => (bht_rdata(i) ^ pc(9, 6)) ## pc(5, 4)))
    val pht_rdata       = VecInit(Seq.tabulate(4)(i => pht(i)(pht_rindex(i))))

    val predict_jump    = VecInit(Seq.tabulate(4)(i => pht_rdata(i)(1) && btb_rdata(i).valid && (btb_rdata(i).tag === pc(31, 32 - BTB_TAG_WIDTH))))
    val predict_valid   = VecInit(Seq.tabulate(4)(i => btb_rdata(i).valid && (btb_rdata(i).tag === pc(31, 32 - BTB_TAG_WIDTH))))

    val pred_valid      = ~((1.U(4.W) << pc(3, 2)) - 1.U)(3, 0)

    val pred_hit        = pred_valid & predict_jump.asUInt 
    val pred_valid_hit  = pred_valid & predict_valid.asUInt 
    val pred_hit_oh     = Mux(pred_hit.orR, PriorityEncoderOH(pred_hit), 0.U)
    val pred_hit_index  = PriorityEncoder(pred_hit)

    io.predict_jump     := (pred_hit_oh >> pc(3, 2)).asBools
    io.pred_valid       := (pred_valid_hit >> pc(3, 2)).asBools
    io.pred_npc         := Mux(btb_rdata(pred_hit_index).typ === JIRL && jirl_sel =/= 3.U, ras(top-1.U), btb_rdata(pred_hit_index).target ## 0.U(2.W)) 
    // io.pred_npc         := btb_rdata(pred_hit_index).target ## 0.U(2.W)
    // update
    val update_en       = io.update_en
    // btb
    val mask = UIntToOH(io.pc_cmt(3, 2))
    val btb_wdata = Wire(Vec(4, new btb_t))
    val btb_windex = io.pc_cmt(4-1+BTB_INDEX_WIDTH, 4)
    for (i <- 0 until 4){
        btb_wdata(i).valid := true.B
        btb_wdata(i).target := io.branch_target(31, 2)
        btb_wdata(i).tag := io.pc_cmt(31, 32-BTB_TAG_WIDTH)
        btb_wdata(i).typ := io.br_type
    }
    for(i <- 0 until 4){
        btb_tagv(i).addra := btb_windex
        btb_tagv(i).addrb := btb_rindex
        btb_tagv(i).dina := btb_wdata(i).valid ## btb_wdata(i).tag
        btb_tagv(i).clka := clock
        btb_tagv(i).wea := update_en && mask(i)
        btb_tagv(i).enb := true.B
        btb_rdata(i).valid := btb_tagv(i).doutb(BTB_TAG_WIDTH)
        btb_rdata(i).tag := btb_tagv(i).doutb(BTB_TAG_WIDTH-1, 0)
    }
    for(i <- 0 until 4){
        btb_targ(i).addra := btb_windex
        btb_targ(i).addrb := btb_rindex
        btb_targ(i).dina := btb_wdata(i).target ## btb_wdata(i).typ
        btb_targ(i).clka := clock
        btb_targ(i).wea := update_en && mask(i)
        btb_targ(i).enb := true.B
        btb_rdata(i).target := btb_targ(i).doutb(31, 2)
        btb_rdata(i).typ := btb_targ(i).doutb(1, 0)
    }

    // bht
    val bht_windex = io.pc_cmt(7, 4)
    val bht_wdata = io.real_jump
    when(update_en){
        bht(io.pc_cmt(3, 2))(bht_windex) := bht_wdata ## bht(io.pc_cmt(3, 2))(bht_windex)(3, 1)
    }

    // pht
    val pht_windex = (bht(io.pc_cmt(3, 2))(bht_windex) ^ io.pc_cmt(9, 6)) ## io.pc_cmt(5, 4)

    when(update_en){
        pht(io.pc_cmt(3, 2))(pht_windex) := Mux(io.real_jump, 
                                            pht(io.pc_cmt(3, 2))(pht_windex) + (pht(io.pc_cmt(3, 2))(pht_windex) =/= 3.U), 
                                            pht(io.pc_cmt(3, 2))(pht_windex) - (pht(io.pc_cmt(3, 2))(pht_windex) =/= 0.U))
    }


    // RAS
    when(io.predict_fail){
        when(io.br_type === JIRL){
            top := io.top_arch
        }
    }
    .elsewhen(io.pd_pred_fix){
        when(io.pd_pred_fix_is_bl){
            top := top + 1.U
            ras(top) := io.pd_pc_plus_4
        }
    }.elsewhen(btb_rdata(pred_hit_index).typ === BL){
        top := top + 1.U
        ras(top) := (io.pc(31, 4) ## (0.U(4.W))) + (pred_hit_index << 2.U) + 4.U
    }.elsewhen(btb_rdata(pred_hit_index).typ === JIRL){
        top := top - 1.U
    }

    when(io.ras_update_en && io.br_type === JIRL){
        jirl_sel := Mux(io.predict_fail, Mux(jirl_sel === 3.U, 2.U, jirl_sel+1.U), Mux(jirl_sel === 3.U, 3.U, jirl_sel-(jirl_sel =/= 0.U)))
    }

}

