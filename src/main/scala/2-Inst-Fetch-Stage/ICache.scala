import chisel3._
import chisel3.util._

object ICache_Config{
    val INDEX_WIDTH = 6
    val INDEX_DEPTH = 1 << INDEX_WIDTH

    val OFFSET_WIDTH = 6
    val OFFSET_DEPTH = 1 << OFFSET_WIDTH

    val TAG_WIDTH = 32 - INDEX_WIDTH - OFFSET_WIDTH

    val FROM_CMEM = 0.U(1.W)
    val FROM_RBUF = 1.U(1.W)

    val FROM_PIPE = 0.U(1.W)
    val FROM_SEG  = 1.U(1.W)
}

class ICache_IO extends Bundle{
    // IF Stage
    val addr_IF         = Input(UInt(32.W))
    val rvalid_IF       = Input(Bool())

    // RM Stage
    val cache_miss_RM   = Output(Bool())
    val rdata_RM        = Output(Vec(4, UInt(32.W)))

    // control
    val stall           = Input(Bool())
    val flush           = Input(Bool())

    // for AXI arbiter
    val i_araddr        = Output(UInt(32.W))
    val i_rvalid        = Output(Bool())
    val i_rready        = Input(Bool())
    val i_rdata         = Input(UInt(32.W))
    val i_rlast         = Input(Bool())
    val i_rsize         = Output(UInt(3.W))
    val i_rburst        = Output(UInt(2.W))
    val i_rlen          = Output(UInt(8.W))

    // for stat
    val commit_icache_visit    = Output(Bool())
    val commit_icache_miss     = Output(Bool())
}

import ICache_Config._
class ICache extends Module{
    val io      = IO(new ICache_IO)
    val flush   = io.flush
    val stall   = io.stall

    // address decode IF
    val addr_IF     = io.addr_IF
    val tag_IF      = addr_IF(31, 32-TAG_WIDTH)
    val index_IF    = addr_IF(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_IF   = addr_IF(OFFSET_WIDTH-1, 0)

    // IF Stage
    val tagv                = VecInit(Seq.fill(2)(Module(new xilinx_single_port_ram_read_first(TAG_WIDTH+1, INDEX_DEPTH)).io))
    val tag_r_RM            = VecInit.tabulate(2)(i => tagv(i).douta(TAG_WIDTH-1, 0))
    val valid_r_RM          = VecInit.tabulate(2)(i => tagv(i).douta(TAG_WIDTH))

    val cmem                = VecInit(Seq.fill(2)(Module(new xilinx_single_port_ram_read_first(8 * OFFSET_DEPTH, INDEX_DEPTH)).io))
    
    val addr_sel            = WireDefault(FROM_PIPE)
    
    // IF-RM SegReg
    val addr_reg_IF_RM      = RegInit(0.U(32.W))
    val rvalid_reg_IF_RM    = RegInit(false.B)

    // RM Stage
    val addr_RM             = addr_reg_IF_RM
    val rvalid_RM           = rvalid_reg_IF_RM
    val cache_miss_RM       = WireDefault(false.B)
    val data_sel            = WireDefault(FROM_RBUF)
    val i_rvalid            = WireDefault(false.B)

    // mem we
    val tagv_we_RM          = WireDefault(VecInit(Seq.fill(2)(false.B)))
    val cmem_we_RM          = WireDefault(VecInit(Seq.fill(2)(false.B)))

    // addr decode
    val tag_RM              = addr_RM(31, 32-TAG_WIDTH)
    val index_RM            = addr_RM(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_RM           = addr_RM(OFFSET_WIDTH-1, 0)

    // lru 
    val lru_mem             = RegInit(VecInit(Seq.fill(INDEX_DEPTH)(0.U(1.W))))
    val lru_sel             = lru_mem(index_RM)
    val lru_miss_upd        = WireDefault(false.B)
    val lru_hit_upd         = WireDefault(false.B)

    val ret_buf             = RegInit(0.U((8*OFFSET_DEPTH).W))

    // stat
    val icache_miss         = WireDefault(false.B)
    val icache_visit        = WireDefault(false.B)

    // IF Stage
    for(i <- 0 until 2){
        tagv(i).addra   := Mux(addr_sel === FROM_PIPE, index_IF, index_RM)
        tagv(i).dina    := true.B ## tag_RM
        tagv(i).clka    := clock
        tagv(i).wea     := tagv_we_RM(i)
    }
    for(i <- 0 until 2){
        cmem(i).addra   := Mux(addr_sel === FROM_PIPE, index_IF, index_RM)
        cmem(i).dina    := ret_buf
        cmem(i).clka    := clock
        cmem(i).wea     := cmem_we_RM(i)
    }

    // IF-RM SegReg
    when(!(stall || cache_miss_RM)){
        addr_reg_IF_RM      := io.addr_IF
        rvalid_reg_IF_RM    := io.rvalid_IF
    }

    // RM Stage
    /* hit logic */
    val hit_RM          = VecInit.tabulate(2)(i => valid_r_RM(i) && tag_r_RM(i) === tag_RM)
    val hit_index_RM    = OHToUInt(hit_RM)
    val cache_hit_RM    = hit_RM.reduce(_||_)

    /* rdata logic */
    val block_offset    = offset_RM(OFFSET_WIDTH-1, 2) ## 0.U(5.W)
    val cmem_rdata_RM   = (cmem(hit_index_RM).douta >> block_offset)(127, 0)
    val rbuf_rdata_RM   = (ret_buf >> block_offset)(127, 0)
    val rdata_RM        = VecInit.tabulate(4)(i => (Mux(data_sel === FROM_CMEM, cmem_rdata_RM(32*i+31, 32*i), rbuf_rdata_RM(32*i+31, 32*i))))

    /* return buffer update logic */
    when(io.i_rready){
        ret_buf := io.i_rdata ## ret_buf(8*OFFSET_DEPTH-1, 32)
    }
    /* lru logic */
    when(lru_hit_upd){
        lru_mem(index_RM) := !hit_index_RM
    }.elsewhen(lru_miss_upd){
        lru_mem(index_RM) := !lru_sel
    }

    /* FSM for read */
    val s_idle :: s_miss :: s_refill :: s_wait :: Nil = Enum(4)
    val state = RegInit(s_idle)
    switch(state){
        is(s_idle){
            addr_sel := Mux(stall, FROM_SEG, FROM_PIPE)
            when(rvalid_RM){
                state           := Mux(cache_hit_RM, s_idle, s_miss)
                lru_hit_upd     := cache_hit_RM
                cache_miss_RM   := !cache_hit_RM
                data_sel        := FROM_CMEM

                icache_visit    := !stall
                icache_miss     := !cache_hit_RM
            }
        }
        is(s_miss){
            i_rvalid            := true.B
            cache_miss_RM       := true.B
            state               := Mux(io.i_rready && io.i_rlast, s_refill, s_miss)
        }
        is(s_refill){
            state               := s_wait
            cache_miss_RM       := true.B
            lru_miss_upd        := true.B
            tagv_we_RM(lru_sel) := true.B
            cmem_we_RM(lru_sel) := true.B
            addr_sel            := FROM_SEG
        }
        is(s_wait){
            state               := Mux(stall, s_wait, s_idle)
            cache_miss_RM       := false.B
        }
    }

    // output
    io.cache_miss_RM    := cache_miss_RM
    io.rdata_RM         := rdata_RM
    io.i_araddr         := tag_RM ## index_RM ## 0.U(OFFSET_WIDTH.W)
    io.i_rvalid         := i_rvalid
    io.i_rsize          := 2.U
    io.i_rburst         := 1.U
    io.i_rlen           := (8*OFFSET_DEPTH/32-1).U

    io.commit_icache_miss    := icache_miss
    io.commit_icache_visit   := icache_visit
}
