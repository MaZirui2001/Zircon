import chisel3._
import chisel3.util._
import Exception._
import CPU_Config._
import TLB_Struct._

class TLB_IO extends Bundle {
    val csr_asid        = Input(UInt(10.W))  
    val csr_plv         = Input(UInt(2.W))

    // for tlbsrch
    val csr_tlbehi      = Input(UInt(19.W))
    val tlbsrch_idx     = Output(UInt(log2Ceil(TLB_ENTRY_NUM).W))
    val tlbsrch_hit     = Output(Bool())
    
    // for tlbrd 
    val csr_tlbidx     = Input(UInt(log2Ceil(TLB_ENTRY_NUM).W))
    val tlbrd_entry    = Output(new tlb_t)

    // for tlbwr
    val tlbwr_entry    = Input(new tlb_t)
    val tlbwr_en       = Input(Bool())
    
    // for tlbfill
    val tlbfill_idx    = Input(UInt(log2Ceil(TLB_ENTRY_NUM).W))
    val tlbfill_en     = Input(Bool())

    // for invtlb
    val invtlb_en      = Input(Bool())
    val invtlb_op      = Input(UInt(5.W))
    val invtlb_asid    = Input(UInt(10.W))
    val invtlb_vaddr   = Input(UInt(32.W))

    // icache tlb search
    val i_valid         = Input(Bool())
    val i_vaddr         = Input(UInt(32.W))
    val i_paddr         = Output(UInt(32.W))
    val i_uncache       = Output(Bool())
    val i_exception     = Output(UInt(8.W))
    val i_stall         = Input(Bool())
    
    // dcache tlb search
    val d_rvalid        = Input(Bool())
    val d_wvalid        = Input(Bool())
    val d_vaddr         = Input(UInt(32.W))
    val d_paddr         = Output(UInt(32.W))
    val d_uncache       = Output(Bool())
    val d_exception     = Output(UInt(8.W))
    val d_stall         = Input(Bool())
}

class TLB extends Module{
    val io = IO(new TLB_IO)

    val i_tlb = RegInit(VecInit.fill(TLB_ENTRY_NUM)(0.U.asTypeOf(new tlb_t)))
    val d_tlb = RegNext(i_tlb)
    // for tlbsrch
    val csr_tlbehi_vppn   = io.csr_tlbehi
    val tlbsrch_hit       = WireDefault(VecInit.fill(TLB_ENTRY_NUM)(false.B))
    val tlbsrch_hit_idx   = OHToUInt(tlbsrch_hit)
    for(i <- 0 until TLB_ENTRY_NUM){
        val tlb_vppn = Mux(d_tlb(i).ps(3), d_tlb(i).vppn, d_tlb(i).vppn(18, 10) ## 0.U(10.W))
        val csr_vppn = Mux(d_tlb(i).ps(3), csr_tlbehi_vppn, csr_tlbehi_vppn(18, 10) ## 0.U(10.W))
        tlbsrch_hit(i) := ((d_tlb(i).e && !(tlb_vppn ^ csr_vppn))
                        && (d_tlb(i).g || !(d_tlb(i).asid ^ io.csr_asid)))
    }
    io.tlbsrch_idx := tlbsrch_hit_idx
    io.tlbsrch_hit := tlbsrch_hit.asUInt.orR

    // for tlbrd
    io.tlbrd_entry := d_tlb(io.csr_tlbidx)

    // for tlbwr and tlbfill
    when(io.tlbwr_en || io.tlbfill_en){
        val tlb_idx = Mux(io.tlbwr_en, io.csr_tlbidx, io.tlbfill_idx)
        i_tlb(tlb_idx) := io.tlbwr_entry
    }

    // for invtlb
    val invtlb_op = io.invtlb_op
    val invtlb_asid = io.invtlb_asid
    val invtlb_vaddr = io.invtlb_vaddr
    when(io.invtlb_en){
        for(i <- 0 until TLB_ENTRY_NUM){
            switch(invtlb_op){
                is(0.U){
                    // clear all tlb entries
                    i_tlb(i).e := false.B
                }
                is(1.U){
                    // clear all tlb entries
                    i_tlb(i).e := false.B
                }
                is(2.U){
                    // clear all tlb entries with g = 1
                    when(i_tlb(i).g){
                        i_tlb(i).e := false.B
                    }
                }
                is(3.U){
                    // clear all tlb entries with g = 0
                    when(!i_tlb(i).g){
                        i_tlb(i).e := false.B
                    }
                }
                is(4.U){
                    // clear all tlb entries with asid = invtlb_asid and g = 0
                    when(!(i_tlb(i).asid ^ invtlb_asid) && !i_tlb(i).g){
                        i_tlb(i).e := false.B
                    }
                }
                is(5.U){
                    // clear all tlb entries with asid = invtlb_asid and g = 0 and va[31:13] = invtlb_vaddr[31:13]
                    when(!(i_tlb(i).asid ^ invtlb_asid) && !i_tlb(i).g && !(i_tlb(i).vppn ^ invtlb_vaddr(31, 13))){
                        i_tlb(i).e := false.B
                    }
                }
                is(6.U){
                    // clear all tlb entries with asid = invtlb_asid or g = 1,  and va[31:13] = invtlb_vaddr[31:13]
                    when((!(i_tlb(i).asid ^ invtlb_asid) || i_tlb(i).g) && !(i_tlb(i).vppn ^ invtlb_vaddr(31, 13))){
                        i_tlb(i).e := false.B
                    }
                }
            }
        }
    }

    // icache tlb search
    val i_tlb_hit       = WireDefault(VecInit.fill(TLB_ENTRY_NUM)(false.B))
    val i_tlb_entry     = Mux1H(i_tlb_hit, i_tlb)
    val i_tlb_hit_entry = TLB_Hit_Gen(i_tlb_entry, Mux(i_tlb_entry.ps(3), io.i_vaddr(12), io.i_vaddr(21)))

    for(i <- 0 until TLB_ENTRY_NUM){
        val tlb_vppn    = Mux(i_tlb(i).ps(3), i_tlb(i).vppn, i_tlb(i).vppn(18, 10) ## 0.U(10.W))
        val i_vppn      = Mux(i_tlb(i).ps(3), io.i_vaddr(31, 13), io.i_vaddr(31, 23) ## 0.U(10.W))
        i_tlb_hit(i)    := ((i_tlb(i).e && !(tlb_vppn ^ i_vppn))
                        && (i_tlb(i).g || !(i_tlb(i).asid ^ RegNext(io.csr_asid))))
    }
    io.i_uncache   := i_tlb_hit_entry.mat(0)
    io.i_paddr     := Mux(i_tlb_hit_entry.ps(3), 
                          Cat(i_tlb_hit_entry.ppn, io.i_vaddr(11, 0)),
                          Cat(i_tlb_hit_entry.ppn(19, 9), io.i_vaddr(20, 0)))
    
    val i_tlb_hit_reg       = ShiftRegister(i_tlb_hit, 1, !io.i_stall)
    val i_tlb_hit_entry_reg = ShiftRegister(i_tlb_hit_entry, 1, !io.i_stall)
    val i_csr_plv_reg       = ShiftRegister(io.csr_plv, 1, !io.i_stall)
    val i_valid_reg         = ShiftRegister(io.i_valid, 1, !io.i_stall)
    io.i_exception          := Signal_Exception(i_tlb_hit_reg.asUInt.orR, i_tlb_hit_entry_reg, i_csr_plv_reg, i_valid_reg, false.B, false.B)
    
    // dcache tlb search
    val d_tlb_hit       = WireDefault(VecInit.fill(TLB_ENTRY_NUM)(false.B))
    val d_tlb_entry     = Mux1H(ShiftRegister(d_tlb_hit, 1, !io.d_stall), d_tlb)
    val d_tlb_hit_entry = TLB_Hit_Gen(d_tlb_entry, Mux(d_tlb_entry.ps(3), ShiftRegister(io.d_vaddr(12), 1, !io.d_stall), ShiftRegister(io.d_vaddr(21), 1, !io.d_stall)))


    for(i <- 0 until TLB_ENTRY_NUM){
        val tlb_vppn    = Mux(d_tlb(i).ps(3), d_tlb(i).vppn, d_tlb(i).vppn(18, 10) ## 0.U(10.W))
        val d_vppn      = Mux(d_tlb(i).ps(3), io.d_vaddr(31, 13), io.d_vaddr(31, 23) ## 0.U(10.W))
        d_tlb_hit(i)    := ((d_tlb(i).e && !(tlb_vppn ^ d_vppn))
                         && (d_tlb(i).g || !(d_tlb(i).asid ^ RegNext(io.csr_asid))))
    }
    io.d_uncache   := d_tlb_hit_entry.mat(0)
    io.d_paddr     := Mux(d_tlb_hit_entry.ps(3), 
                          Cat(d_tlb_hit_entry.ppn, ShiftRegister(io.d_vaddr(11, 0), 1, !io.d_stall)),
                          Cat(d_tlb_hit_entry.ppn(19, 9), ShiftRegister(io.d_vaddr(20, 0), 1, !io.d_stall)))
    val d_tlb_hit_reg       = ShiftRegister(d_tlb_hit, 1, !io.d_stall)
    val d_tlb_hit_entry_reg = d_tlb_hit_entry
    val d_csr_plv_reg       = ShiftRegister(io.csr_plv, 1, !io.d_stall)
    val d_rvalid_reg        = ShiftRegister(io.d_rvalid, 1, !io.d_stall)
    val d_wvalid_reg        = ShiftRegister(io.d_wvalid, 1, !io.d_stall)
    val d_paddr_reg         = io.d_paddr
    io.d_exception          := Mux(d_csr_plv_reg === 3.U && d_paddr_reg(31), 1.U(1.W) ## ADEM, Signal_Exception(d_tlb_hit_reg.asUInt.orR, d_tlb_hit_entry_reg, d_csr_plv_reg, false.B, d_rvalid_reg, d_wvalid_reg))
}
