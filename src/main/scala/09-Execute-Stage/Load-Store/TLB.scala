import chisel3._
import chisel3.util._
import EXCEPTION._
object TLB_Config {
    val TLB_ENTRY_NUM = 16
    val VA_LEN = 32
    val PA_LEN = 32
    class TLB_ENTRY extends Bundle{
        val vppn = UInt((VA_LEN-13).W)
        val ps   = UInt(6.W)
        val g    = Bool()
        val asid = UInt(10.W)
        val e    = Bool()
        val ppn0 = UInt((PA_LEN-12).W)
        val plv0 = UInt(2.W)
        val mat0 = UInt(2.W)
        val d0   = Bool()
        val v0   = Bool()
        val ppn1 = UInt((PA_LEN-12).W)
        val plv1 = UInt(2.W)
        val mat1 = UInt(2.W)
        val d1   = Bool()
        val v1   = Bool()
    }
    class TLB_HIT_ENTRY extends Bundle{
        val vppn = UInt((VA_LEN-13).W)
        val ps   = UInt(6.W)
        val g    = Bool()
        val asid = UInt(10.W)
        val e    = Bool()
        val ppn  = UInt((PA_LEN-12).W)
        val plv  = UInt(2.W)
        val mat  = UInt(2.W)
        val d    = Bool()
        val v    = Bool()
    }
    def TLB_Hit_Gen(tlb_entry: TLB_ENTRY, last: Bool): TLB_HIT_ENTRY = {
        val tlb_hit_entry = Wire(new TLB_HIT_ENTRY)
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
    def Signal_Exception(tlb_hit: Bool, tlb_hit_entry: TLB_HIT_ENTRY, csr_plv: UInt, i_valid: Bool, d_rvalid: Bool, d_wvalid: Bool): UInt = {
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
import TLB_Config._
class TLB_IO extends Bundle {
    val csr_asid        = Input(UInt(10.W))  
    val csr_plv         = Input(UInt(2.W))

    // for tlbsrch
    val csr_tlbehi      = Input(UInt(19.W))
    val tlbsrch_idx     = Output(UInt(log2Ceil(TLB_ENTRY_NUM).W))
    
    // for tlbrd 
    val csr_tlbidx     = Input(UInt(log2Ceil(TLB_ENTRY_NUM).W))
    val tlbrd_entry    = Output(new TLB_ENTRY)

    // for tlbwr
    val tlbwr_entry    = Input(new TLB_ENTRY)
    val tlbwr_en       = Input(Bool())
    
    // for tlbfill
    val tlbfill_idx    = Input(UInt(log2Ceil(TLB_ENTRY_NUM).W))
    val tlbfill_en     = Input(Bool())

    // icache tlb search
    val i_valid         = Input(Bool())
    val i_vaddr         = Input(UInt(VA_LEN.W))
    val i_paddr         = Output(UInt(PA_LEN.W))
    val i_uncache       = Output(Bool())
    val i_exception     = Output(UInt(8.W))
    
    // dcache tlb search
    val d_rvalid        = Input(Bool())
    val d_wvalid        = Input(Bool())
    val d_vaddr         = Input(UInt(VA_LEN.W))
    val d_paddr         = Output(UInt(PA_LEN.W))
    val d_uncache       = Output(Bool())
    val d_exception     = Output(UInt(8.W))
}

class TLB extends Module{
    val io = IO(new TLB_IO)

    val tlb = RegInit(VecInit(Seq.fill(TLB_ENTRY_NUM)(0.U.asTypeOf(new TLB_ENTRY))))

    // for tlbsrch
    val csr_tlbehi_vppn   = io.csr_tlbehi
    val tlbsrch_hit       = WireDefault(VecInit(Seq.fill(TLB_ENTRY_NUM)(false.B)))
    val tlbsrch_hit_idx   = OHToUInt(tlbsrch_hit)
    for(i <- 0 until TLB_ENTRY_NUM){
        val tlb_vppn = Mux(tlb(i).ps === 12.U, tlb(i).vppn, tlb(i).vppn(VA_LEN-14, VA_LEN-22) ## 0.U(10.W))
        val csr_vppn = Mux(io.csr_tlbehi(18, 12) === 0.U, csr_tlbehi_vppn, csr_tlbehi_vppn(VA_LEN-14, VA_LEN-22) ## 0.U(10.W))
        tlbsrch_hit(i) := (tlb(i).e 
                        && (tlb(i).g || tlb(i).asid === io.csr_asid)
                        && tlb_vppn === csr_vppn)
    }
    io.tlbsrch_idx := tlbsrch_hit_idx

    // for tlbrd
    io.tlbrd_entry := tlb(io.csr_tlbidx)

    // for tlbwr and tlbfill
    when(io.tlbwr_en || io.tlbfill_en){
        val tlb_idx = Mux(io.tlbwr_en, io.csr_tlbidx, io.tlbfill_idx)
        tlb(tlb_idx) := io.tlbwr_entry
    }

    // icache tlb search
    val i_tlb_hit       = WireDefault(VecInit(Seq.fill(TLB_ENTRY_NUM)(false.B)))
    val i_tlb_hit_idx   = OHToUInt(i_tlb_hit)
    val i_tlb_hit_entry = TLB_Hit_Gen(tlb(i_tlb_hit_idx), Mux(tlb(i_tlb_hit_idx).ps === 12.U, io.i_vaddr(12), io.i_vaddr(21)))

    for(i <- 0 until TLB_ENTRY_NUM){
        val tlb_vppn = Mux(tlb(i).ps === 12.U, tlb(i).vppn, tlb(i).vppn(VA_LEN-14, VA_LEN-22) ## 0.U(10.W))
        val i_vppn = Mux(tlb(i).ps === 12.U, io.i_vaddr(VA_LEN-1,VA_LEN-19), io.i_vaddr(VA_LEN-1,VA_LEN-9) ## 0.U(10.W))
        i_tlb_hit(i) := (tlb(i).e 
                     && (tlb(i).g || tlb(i).asid === io.csr_asid)
                     && tlb_vppn === i_vppn)
    }
    io.i_uncache   := i_tlb_hit_entry.mat(0)
    io.i_paddr     := Mux(i_tlb_hit_entry.ps === 12.U, 
                          Cat(i_tlb_hit_entry.ppn, io.i_vaddr(PA_LEN-21, 0)),
                          Cat(i_tlb_hit_entry.ppn(PA_LEN-13, 9), io.i_vaddr(PA_LEN-12, 0)))
    io.i_exception := Signal_Exception(i_tlb_hit.asUInt.orR, i_tlb_hit_entry, io.csr_plv, io.i_valid, false.B, false.B)
    
    // dcache tlb search
    val d_tlb_hit       = WireDefault(VecInit(Seq.fill(TLB_ENTRY_NUM)(false.B)))
    val d_tlb_hit_idx   = OHToUInt(d_tlb_hit)
    val d_tlb_hit_entry = TLB_Hit_Gen(tlb(d_tlb_hit_idx), Mux(tlb(d_tlb_hit_idx).ps === 12.U, io.d_vaddr(12), io.d_vaddr(21)))

    for(i <- 0 until TLB_ENTRY_NUM){
        val tlb_vppn = Mux(tlb(i).ps === 12.U, tlb(i).vppn, tlb(i).vppn(VA_LEN-14, VA_LEN-22) ## 0.U(10.W))
        val d_vppn = Mux(tlb(i).ps === 12.U, io.d_vaddr(VA_LEN-1,VA_LEN-19), io.d_vaddr(VA_LEN-1,VA_LEN-9) ## 0.U(10.W))
        d_tlb_hit(i) := (tlb(i).e 
                     && (tlb(i).g || tlb(i).asid === io.csr_asid)
                     && tlb_vppn === d_vppn)
    }
    io.d_uncache   := d_tlb_hit_entry.mat(0)
    io.d_paddr     := Mux(d_tlb_hit_entry.ps === 12.U, 
                          Cat(d_tlb_hit_entry.ppn, io.d_vaddr(PA_LEN-21, 0)),
                          Cat(d_tlb_hit_entry.ppn(PA_LEN-13, 9), io.d_vaddr(PA_LEN-12, 0)))
    io.d_exception := Signal_Exception(d_tlb_hit.asUInt.orR, d_tlb_hit_entry, io.csr_plv, false.B, io.d_rvalid, io.d_wvalid)
    
}
