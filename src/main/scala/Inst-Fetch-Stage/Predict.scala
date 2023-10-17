import chisel3._
import chisel3.util._

class btb_t extends Bundle{
    val valid = Bool()
    val target = UInt(30.W)
    val tag = UInt(24.W)
}
class Predict_IO extends Bundle{
    // check
    val npc = Input(UInt(32.W))
    val pc = Input(UInt(32.W))
    val predict_jump = Output(Vec(4, Bool()))
    val pred_npc = Output(UInt(32.W))

    // update
    val pc_cmt = Input(UInt(32.W))
    val real_jump = Input(Bool())
    val branch_target = Input(UInt(32.W))
    val update_en = Input(Bool())
}


class Predict extends Module{
    val io = IO(new Predict_IO)

    val btb = SyncReadMem(16, Vec(4, new btb_t))
    val bht = RegInit(VecInit(Seq.fill(64)(0.U(4.W))))
    val pht = RegInit(VecInit(Seq.fill(64)(VecInit(Seq.fill(16)(2.U(2.W))))))

    // check
    val npc = io.npc
    val pc = io.pc
    val btb_rindex = npc(7, 4)
    val btb_rdata = Wire(Vec(4, new btb_t))
    btb_rdata := btb.read(btb_rindex)
    
    val bht_rindex = VecInit(pc(7, 4) ## 0.U(2.W), pc(7, 4) ## 1.U(2.W), pc(7, 4) ## 2.U(2.W), pc(7, 4) ## 3.U(2.W))
    val bht_rdata = Wire(Vec(4, UInt(4.W)))
    for (i <- 0 until 4){
        bht_rdata(i) := bht(bht_rindex(i))
    }

    val pht_line_index = bht_rdata
    val pht_colm_index = bht_rindex
    val pht_rdata = Wire(Vec(4, UInt(2.W)))
    for (i <- 0 until 4){
        pht_rdata(i) := pht(pht_colm_index(i))(pht_line_index(i))
    }

    val predict_jump = Wire(Vec(4, Bool()))
    for (i <- 0 until 4){
        predict_jump(i) := pht_rdata(i)(1) && btb_rdata(i).valid && (btb_rdata(i).tag === pc(31, 8))
    }

    val pred_valid = Wire(UInt(4.W))
    pred_valid := ~((1.U(4.W) << pc(3, 2)) - 1.U)

    val pred_hit = pred_valid & predict_jump.asUInt 

    io.predict_jump := (PriorityEncoderOH(pred_hit) >> pc(3, 2)).asBools
    io.pred_npc := btb_rdata(PriorityEncoder(pred_hit)).target ## 0.U(2.W)

    // update
    val update_en = io.update_en
    // btb
    val mask = UIntToOH(io.pc_cmt(3, 2))
    val btb_wdata = Wire(Vec(4, new btb_t))
    for (i <- 0 until 4){
        btb_wdata(i).valid := true.B
        btb_wdata(i).target := io.branch_target(31, 2)
        btb_wdata(i).tag := io.pc_cmt(31, 8)
    }
    when(update_en){
        btb.write(io.pc_cmt(7, 4), btb_wdata, mask.asBools)
    }
    // bht
    val bht_windex = io.pc_cmt(7, 2)
    val bht_wdata = io.real_jump
    when(update_en){
        bht(bht_windex) := bht_wdata ## bht(bht_windex)(3, 1)
    }

    // pht
    val pht_colm_windex = io.pc_cmt(7, 2)
    val pht_line_windex = bht(bht_windex)

    when(update_en){
        pht(pht_colm_windex)(pht_line_windex) := Mux(io.real_jump, 
                                            pht(pht_colm_windex)(pht_line_windex) + (pht(pht_colm_windex)(pht_line_windex) =/= 3.U), 
                                            pht(pht_colm_windex)(pht_line_windex) - (pht(pht_colm_windex)(pht_line_windex) =/= 0.U))
    }

}