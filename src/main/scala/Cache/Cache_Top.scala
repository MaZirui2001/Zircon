import chisel3._
import chisel3.util._

class Cache_Top extends Module{
    val io = IO(new Bundle{
        val i_addr_pipe         = Input(UInt(32.W))
        val i_rvalid_pipe       = Input(Bool())
        val cache_miss_RM       = Output(Bool())
        val rdata_RM            = Output(UInt(128.W))

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
    val arb                 = Module(new AXI_Arbiter)

    ic.io.addr_IF           := io.i_addr_pipe
    ic.io.rvalid_IF         := io.i_rvalid_pipe
    io.cache_miss_RM        := ic.io.cache_miss_RM
    io.rdata_RM             := ic.io.rdata_RM

    ic.stall                := false.B
    ic.flush                := false.B

    arb.io.i_araddr         := ic.io.i_araddr
    arb.io.i_rvalid         := ic.io.i_rvalid
    ic.io.i_rready          := arb.io.i_rready
    ic.io.i_rdata           := arb.io.i_rdata
    ic.io.i_rlast           := arb.io.i_rlast
    arb.io.i_rsize          := ic.io.i_rsize
    arb.io.i_rburst         := ic.io.i_rburst
    arb.io.i_rlen           := ic.io.i_rlen

    arb.io.d_araddr         := 0.U // TODO
    arb.io.d_rvalid         := false.B
    arb.io.d_rsize          := 0.U
    arb.io.d_rburst         := 0.U
    arb.io.d_rlen           := 0.U
    arb.io.d_awaddr         := 0.U // TODO
    arb.io.d_wvalid         := false.B
    arb.io.d_wdata          := 0.U
    arb.io.d_wlast          := false.B
    arb.io.d_wsize          := 0.U
    arb.io.d_wburst         := 0.U
    arb.io.d_wlen           := 0.U
    arb.io.d_wstrb          := 0.U
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


