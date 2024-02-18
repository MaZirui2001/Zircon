import chisel3._
import chisel3.util._

/* Predict Struct */
object Predict_Struct{
    import Predict_Config._
    class btb_t extends Bundle{
        val valid       = Bool()
        val target      = UInt(30.W)
        val tag         = UInt(BTB_TAG_WIDTH.W)
        val typ         = UInt(2.W)
    }
}

/* Rename Rat */
object Rat{
    class rat_t extends Bundle{
        val valid = Bool()
        val lr    = UInt(5.W)
        val free  = Bool()
    }
}

/* Issue Queue Struct */
object Issue_Queue_Struct{
    import Inst_Pack._
    class issue_queue_t[T <: inst_pack_DP_t](inst_pack_t: T) extends Bundle{
        val inst            = inst_pack_t.cloneType
        val prj_waked       = Bool()
        val prk_waked       = Bool()
        val prj_wake_by_ld  = Bool()
        val prk_wake_by_ld  = Bool()
    }
    def Wake_Up(wake_preg: Vec[UInt], pr: UInt) : Bool = {
        val wf = Cat(
                    !(pr ^ wake_preg(3)), 
                    !(pr ^ wake_preg(2)),
                    !(pr ^ wake_preg(1)),
                    !(pr ^ wake_preg(0))
                    )
        wf.orR
    }
}

object RF_Func{
    def Write_First_Read(rf_we: Vec[Bool], wdata: Vec[UInt], prd: Vec[UInt], pr: UInt, rf: Vec[UInt]) : UInt = {
        val wf = Cat(
                    !(pr ^ prd(3)) && rf_we(3), 
                    !(pr ^ prd(2)) && rf_we(2),
                    !(pr ^ prd(1)) && rf_we(1),
                    !(pr ^ prd(0)) && rf_we(0)
                    )
        // val wf_data = wdata(OHToUInt(wf))
        val wf_data = Mux1H(wf, wdata)
        Mux(wf.orR, wf_data, rf(pr))
    }
}


object SB_Struct {
    class sb_t extends Bundle{
        val addr = UInt(32.W)
        val data = UInt(32.W)
        val wstrb = UInt(4.W)
        val uncache = Bool()
    }
}

object TLB_Struct {
    import Exception._
    class tlb_t extends Bundle{
        val vppn = UInt(19.W)
        val ps   = UInt(6.W)
        val g    = Bool()
        val asid = UInt(10.W)
        val e    = Bool()
        val ppn0 = UInt(20.W)
        val plv0 = UInt(2.W)
        val mat0 = UInt(2.W)
        val d0   = Bool()
        val v0   = Bool()
        val ppn1 = UInt(20.W)
        val plv1 = UInt(2.W)
        val mat1 = UInt(2.W)
        val d1   = Bool()
        val v1   = Bool()
    }
    class tlb_hit_t extends Bundle{
        val vppn = UInt(19.W)
        val ps   = UInt(6.W)
        val g    = Bool()
        val asid = UInt(10.W)
        val e    = Bool()
        val ppn  = UInt(20.W)
        val plv  = UInt(2.W)
        val mat  = UInt(2.W)
        val d    = Bool()
        val v    = Bool()
    }
    def TLB_Entry_Gen(vppn: UInt, ps: UInt, g: Bool, asid: UInt, e: Bool, ppn0: UInt, plv0: UInt, mat0: UInt, d0: Bool, v0: Bool, ppn1: UInt, plv1: UInt, mat1: UInt, d1: Bool, v1: Bool): tlb_t = {
        val tlb_entry = Wire(new tlb_t)
        tlb_entry.vppn := vppn
        tlb_entry.ps   := ps
        tlb_entry.g    := g
        tlb_entry.asid := asid
        tlb_entry.e    := e
        tlb_entry.ppn0 := ppn0
        tlb_entry.plv0 := plv0
        tlb_entry.mat0 := mat0
        tlb_entry.d0   := d0
        tlb_entry.v0   := v0
        tlb_entry.ppn1 := ppn1
        tlb_entry.plv1 := plv1
        tlb_entry.mat1 := mat1
        tlb_entry.d1   := d1
        tlb_entry.v1   := v1
        tlb_entry
    }
    def TLB_Hit_Gen(tlb_entry: tlb_t, last: Bool): tlb_hit_t = {
        val tlb_hit_entry = Wire(new tlb_hit_t)
        tlb_hit_entry.vppn := tlb_entry.vppn
        tlb_hit_entry.ps   := tlb_entry.ps
        tlb_hit_entry.g    := tlb_entry.g
        tlb_hit_entry.asid := tlb_entry.asid
        tlb_hit_entry.e    := tlb_entry.e
        tlb_hit_entry.ppn  := Mux(last, tlb_entry.ppn1, tlb_entry.ppn0)
        tlb_hit_entry.plv  := Mux(last, tlb_entry.plv1, tlb_entry.plv0)
        tlb_hit_entry.mat  := Mux(last, tlb_entry.mat1, tlb_entry.mat0)
        tlb_hit_entry.d    := Mux(last, tlb_entry.d1, tlb_entry.d0)
        tlb_hit_entry.v    := Mux(last, tlb_entry.v1, tlb_entry.v0)
        tlb_hit_entry
    }
    def Signal_Exception(tlb_hit: Bool, tlb_hit_entry: tlb_hit_t, csr_plv: UInt, i_valid: Bool, d_rvalid: Bool, d_wvalid: Bool): UInt = {
        val exception = WireDefault(0.U(8.W))
        when(!tlb_hit){
            exception := 1.U(1.W) ## TLBR
        }.elsewhen(!tlb_hit_entry.v){
            exception := Mux(i_valid, 1.U(1.W) ## PIF, Mux(d_rvalid, 1.U(1.W) ## PIL, Mux(d_wvalid, 1.U(1.W) ## PIS, 0.U)))
        }.elsewhen(csr_plv > tlb_hit_entry.plv){
            exception := 1.U(1.W) ## PPI
        }.elsewhen(d_wvalid && !tlb_hit_entry.d){
            exception := 1.U(1.W) ## PME
        }
        exception
    }
}

object ROB_Struct{
    import TLB_Struct._
    import CPU_Config._
    class rob_t extends Bundle(){
        val rd                  = UInt(5.W)
        val rd_valid            = Bool()
        val prd                 = UInt(log2Ceil(PREG_NUM).W)
        val pprd                = UInt(log2Ceil(PREG_NUM).W)
        val predict_fail        = Bool()
        val branch_target       = UInt(32.W)
        val real_jump           = Bool()
        val pred_update_en      = Bool()
        val br_type_pred        = UInt(2.W)
        val complete            = Bool()
        val pc                  = UInt(32.W)
        val rf_wdata            = UInt(32.W)
        val is_store            = Bool()
        val is_ucread           = Bool()
        val is_priv_wrt         = Bool()
        val is_priv_ls          = Bool()
        val allow_next_cmt      = Bool()
        val exception           = UInt(8.W)
        val inst                = UInt(32.W)
    }
    class priv_t(n: Int) extends Bundle{
        val valid       = Bool()
        val priv_vec    = UInt(10.W)
        val csr_addr    = UInt(14.W)
        val tlb_entry   = new tlb_t
        val inv_op      = UInt(5.W)
        val inv_vaddr   = UInt(32.W)
        val inv_asid    = UInt(10.W)
    }
    class priv_ls_t extends Bundle{
        val valid       = Bool()
        val priv_vec    = UInt(3.W)
    }
}
