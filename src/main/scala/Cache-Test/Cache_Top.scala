import chisel3._
import chisel3.util._

class Cache_Top extends Module{
    val io = IO(new Bundle{
        val i_addr_pipe         = Input(UInt(32.W))
        val i_rvalid_pipe       = Input(Bool())
        val cache_miss_RM       = Output(Bool())
        val rdata_RM            = Output(UInt(128.W))

        val d_addr_pipe         = Input(UInt(32.W))
        val mem_type_pipe       = Input(UInt(5.W))
        val wdata_pipe          = Input(UInt(32.W))
        val cache_miss_MEM      = Output(Bool())
        val rdata_MEM           = Output(UInt(32.W))

        val araddr              = Output(UInt(32.W))
        val arburst             = Output(UInt(2.W))
        val arid                = Output(UInt(4.W))
        val arlen               = Output(UInt(8.W)) 
        val arready             = Input(Bool())
        val arsize              = Output(UInt(3.W))
        val arvalid             = Output(Bool())
        val awaddr              = Output(UInt(32.W))
        val awburst             = Output(UInt(2.W))
        val awid                = Output(UInt(4.W))
        val awlen               = Output(UInt(8.W))
        val awready             = Input(Bool())
        val awsize              = Output(UInt(3.W))
        val awvalid             = Output(Bool())
        val bid                 = Input(UInt(4.W))
        val bready              = Output(Bool())
        val bresp               = Input(UInt(2.W))
        val bvalid              = Input(Bool())
        val rdata               = Input(UInt(32.W))
        val rid                 = Input(UInt(4.W))
        val rlast               = Input(Bool())
        val rready              = Output(Bool())
        val rresp               = Input(UInt(2.W))
        val rvalid              = Input(Bool())
        val wdata               = Output(UInt(32.W))
        val wlast               = Output(Bool())
        val wready              = Input(Bool())
        val wstrb               = Output(UInt(4.W))
        val wvalid              = Output(Bool())
    })

    val ic                  = Module(new ICache)
    val dc                  = Module(new DCache)
    val arb                 = Module(new AXI_Arbiter)

    ic.io.addr_IF           := io.i_addr_pipe
    ic.io.rvalid_IF         := io.i_rvalid_pipe
    io.cache_miss_RM        := ic.io.cache_miss_RM
    io.rdata_RM             := ic.io.rdata_RM.asUInt

    ic.io.stall             := false.B
    ic.io.flush             := false.B

    dc.io.addr_RF           := io.d_addr_pipe
    dc.io.mem_type_RF       := io.mem_type_pipe
    dc.io.wdata_RF          := io.wdata_pipe
    io.cache_miss_MEM       := dc.io.cache_miss_MEM
    io.rdata_MEM            := dc.io.rdata_MEM

    dc.io.stall             := false.B

    arb.io.i_araddr         := ic.io.i_araddr
    arb.io.i_rvalid         := ic.io.i_rvalid
    ic.io.i_rready          := arb.io.i_rready
    ic.io.i_rdata           := arb.io.i_rdata
    ic.io.i_rlast           := arb.io.i_rlast
    arb.io.i_rsize          := ic.io.i_rsize
    arb.io.i_rburst         := ic.io.i_rburst
    arb.io.i_rlen           := ic.io.i_rlen

    arb.io.d_araddr         := dc.io.d_araddr
    arb.io.d_rvalid         := dc.io.d_rvalid
    dc.io.d_rready          := arb.io.d_rready
    dc.io.d_rdata           := arb.io.d_rdata
    dc.io.d_rlast           := arb.io.d_rlast
    arb.io.d_rsize          := dc.io.d_rsize
    arb.io.d_rburst         := dc.io.d_rburst
    arb.io.d_rlen           := dc.io.d_rlen

    arb.io.d_awaddr         := dc.io.d_awaddr
    arb.io.d_wdata          := dc.io.d_wdata
    arb.io.d_wvalid         := dc.io.d_wvalid  
    dc.io.d_wready          := arb.io.d_wready
    
    arb.io.d_wlast          := dc.io.d_wlast
    arb.io.d_wsize          := dc.io.d_wsize
    arb.io.d_wburst         := dc.io.d_wburst
    arb.io.d_wlen           := dc.io.d_wlen
    arb.io.d_wstrb          := dc.io.d_wstrb
    dc.io.d_bvalid          := arb.io.d_bvalid
    arb.io.d_bready         := false.B


    io.araddr               := arb.io.araddr
    io.arburst              := arb.io.arburst
    io.arid                 := arb.io.arid
    io.arlen                := arb.io.arlen
    arb.io.arready          := io.arready
    io.arsize               := arb.io.arsize
    io.arvalid              := arb.io.arvalid

    io.awaddr               := arb.io.awaddr
    io.awburst              := arb.io.awburst
    io.awid                 := arb.io.awid
    io.awlen                := arb.io.awlen
    arb.io.awready          := io.awready
    io.awsize               := arb.io.awsize
    io.awvalid              := arb.io.awvalid

    arb.io.bid              := io.bid
    io.bready               := arb.io.bready
    arb.io.bresp            := io.bresp
    arb.io.bvalid           := io.bvalid
    
    arb.io.rdata            := io.rdata
    arb.io.rid              := io.rid
    arb.io.rlast            := io.rlast
    io.rready               := arb.io.rready
    arb.io.rresp            := io.rresp
    arb.io.rvalid           := io.rvalid

    io.wdata                := arb.io.wdata
    io.wlast                := arb.io.wlast
    arb.io.wready           := io.wready
    io.wstrb                := arb.io.wstrb
    io.wvalid               := arb.io.wvalid


    
}


