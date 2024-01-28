import chisel3._
import chisel3.util._
import TLB_Config._

class MMU_IO extends Bundle{
    val csr_asid        = Input(UInt(10.W))  
    val csr_plv         = Input(UInt(2.W))

    // translate mode
    val csr_crmd_trans  = Input(UInt(6.W))
    
    // csr dmw
    val csr_dmw0        = Input(UInt(32.W))
    val csr_dmw1        = Input(UInt(32.W))

    // for tlbsrch
    val csr_tlbehi      = Input(UInt(19.W))
    val tlbsrch_idx     = Output(UInt(log2Ceil(TLB_ENTRY_NUM).W))
    val tlbsrch_hit     = Output(Bool())
    
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

class MMU extends Module{
    val io = IO(new MMU_IO)
    val tlb = Module(new TLB)

    tlb.io.csr_asid       := io.csr_asid
    tlb.io.csr_plv        := io.csr_plv
    tlb.io.csr_tlbehi     := io.csr_tlbehi
    io.tlbsrch_idx        := tlb.io.tlbsrch_idx
    io.tlbsrch_hit        := tlb.io.tlbsrch_hit
    tlb.io.csr_tlbidx     := io.csr_tlbidx
    io.tlbrd_entry        := tlb.io.tlbrd_entry
    tlb.io.tlbwr_entry    := io.tlbwr_entry
    tlb.io.tlbwr_en       := io.tlbwr_en
    tlb.io.tlbfill_idx    := io.tlbfill_idx
    tlb.io.tlbfill_en     := io.tlbfill_en

    // icache tlb search
    tlb.io.i_valid        := io.i_valid
    tlb.io.i_vaddr        := io.i_vaddr

    // dcache tlb search
    tlb.io.d_rvalid       := io.d_rvalid
    tlb.io.d_wvalid       := io.d_wvalid
    tlb.io.d_vaddr        := io.d_vaddr

    val is_da   = io.csr_crmd_trans(0)
    val is_pg   = io.csr_crmd_trans(1)

    // i_paddr
    val i_dmw0_hit = (io.i_vaddr(31, 29) === io.csr_dmw0(31, 29)) && (io.csr_dmw0(0) && io.csr_plv === 0.U || io.csr_dmw0(3) && io.csr_plv === 3.U)
    val i_dmw1_hit = (io.i_vaddr(31, 29) === io.csr_dmw1(31, 29)) && (io.csr_dmw1(0) && io.csr_plv === 0.U || io.csr_dmw1(3) && io.csr_plv === 3.U)
    io.i_paddr   := Mux(is_da, io.i_vaddr, 
                    Mux(i_dmw0_hit, io.csr_dmw0(27, 25) ## io.i_vaddr(28, 0), 
                    Mux(i_dmw1_hit, io.csr_dmw1(27, 25) ## io.i_vaddr(28, 0), tlb.io.i_paddr)))
    io.i_uncache := Mux(is_da, !io.csr_crmd_trans(2), 
                    Mux(i_dmw0_hit, io.csr_dmw0(4),
                    Mux(i_dmw1_hit, io.csr_dmw1(4), tlb.io.i_uncache)))
    io.i_exception := Mux(is_da || i_dmw0_hit || i_dmw1_hit, 0.U, tlb.io.i_exception)

    // d_paddr
    val d_dmw0_hit = (io.d_vaddr(31, 29) === io.csr_dmw0(31, 29)) && (io.csr_dmw0(0) && io.csr_plv === 0.U || io.csr_dmw0(3) && io.csr_plv === 3.U)
    val d_dmw1_hit = (io.d_vaddr(31, 29) === io.csr_dmw1(31, 29)) && (io.csr_dmw1(0) && io.csr_plv === 0.U || io.csr_dmw1(3) && io.csr_plv === 3.U)
    io.d_paddr   := Mux(is_da, io.d_vaddr, 
                    Mux(d_dmw0_hit, io.csr_dmw0(27, 25) ## io.d_vaddr(28, 0), 
                    Mux(d_dmw1_hit, io.csr_dmw1(27, 25) ## io.d_vaddr(28, 0), tlb.io.d_paddr)))
    io.d_uncache := Mux(is_da, !io.csr_crmd_trans(4), 
                    Mux(d_dmw0_hit, io.csr_dmw0(4),
                    Mux(d_dmw1_hit, io.csr_dmw1(4), tlb.io.d_uncache)))
    io.d_exception := Mux(is_da || d_dmw0_hit || d_dmw1_hit, 0.U, tlb.io.d_exception)

}
