import chisel3._
import chisel3.util._
object DCache_Config{
    val INDEX_WIDTH = 2
    val INDEX_DEPTH = 1 << INDEX_WIDTH

    val OFFSET_WIDTH = 4
    val OFFSET_DEPTH = 1 << OFFSET_WIDTH

    val TAG_WIDTH = 32 - INDEX_WIDTH - OFFSET_WIDTH

    val FROM_CMEM = 0.U(1.W)
    val FROM_RBUF = 1.U(1.W)

    val FROM_PIPE = 0.U(1.W)
    val FROM_SEG  = 1.U(1.W)
}

class DCache_IO extends Bundle{
    // EX stage
    val addr_EX         = Input(UInt(32.W))
    val mem_type_EX     = Input(UInt(5.W))
    val wdata_EX        = Input(UInt(32.W))

    // MEM stage
    val cache_miss_MEM  = Output(Bool())
    val rdata_MEM       = Output(UInt(32.W))

    // control
    val stall           = Input(Bool())

    // for AXI arbiter
    val d_araddr        = Output(UInt(32.W))
    val d_rvalid        = Output(Bool())
    val d_rready        = Input(Bool())
    val d_rdata         = Input(UInt(32.W))
    val d_rlast         = Input(Bool())
    val d_rsize         = Output(UInt(3.W))
    val d_rburst        = Output(UInt(2.W))
    val d_rlen          = Output(UInt(8.W))

    val d_awaddr        = Output(UInt(32.W))
    val d_wdata         = Output(UInt(32.W))
    val d_wvalid        = Output(Bool())
    val d_wready        = Input(Bool())
    val d_wlast         = Output(Bool())
    val d_wstrb         = Output(UInt(4.W))
    val d_wsize         = Output(UInt(3.W))
    val d_wburst        = Output(UInt(2.W))
    val d_wlen          = Output(UInt(8.W))
    val d_bvalid        = Input(Bool())
    val d_bready        = Output(Bool())
}
import DCache_Config._
class DCache extends Module{
    val io = IO(new DCache_IO)
    val stall = io.stall

    // address decode EX
    val addr_EX             = io.addr_EX
    val tag_EX              = addr_EX(31, 32-TAG_WIDTH)
    val index_EX            = addr_EX(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_EX           = addr_EX(OFFSET_WIDTH-1, 0)

    // EX-TC SegReg
    val addr_reg_EX_TC      = RegInit(0.U(32.W))
    val mem_type_reg_EX_TC  = RegInit(0.U(5.W))
    val wdata_reg_EX_TC     = RegInit(0.U(32.W))

    // TC Stage
    val tagv        = VecInit(Seq.fill(2)(Module(new xilinx_simple_dual_port_1_clock_ram_write_first(TAG_WIDTH+1, INDEX_DEPTH)).io))
    val tag_r_TC    = VecInit(Seq.tabulate(2)(i => tagv(i).doutb(TAG_WIDTH-1, 0)))
    val valid_r_TC  = VecInit(Seq.tabulate(2)(i => tagv(i).doutb(TAG_WIDTH)))
    
    // decode
    val addr_TC     = addr_reg_EX_TC
    val tag_TC      = addr_TC(31, 32-TAG_WIDTH)
    val index_TC    = addr_TC(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_TC   = addr_TC(OFFSET_WIDTH-1, 0)

    // TC-MEM SegReg
    val addr_reg_TC_MEM     = RegInit(0.U(32.W))
    val mem_type_reg_TC_MEM = RegInit(0.U(5.W))
    val wdata_reg_TC_MEM    = RegInit(0.U(32.W))
    val hit_reg_TC_MEM      = RegInit(0.U(2.W))
    val tag_reg_TC_MEM      = RegInit(VecInit(Seq.fill(2)(0.U(TAG_WIDTH.W))))

    // MEM Stage
    val cmem            = VecInit(Seq.fill(2)(Module(new xilinx_simple_dual_port_byte_write_1_clock_ram_write_first(OFFSET_DEPTH, 8, INDEX_DEPTH)).io))
    val addr_MEM        = addr_reg_TC_MEM
    val mem_type_MEM    = mem_type_reg_TC_MEM
    val wdata_MEM       = WireDefault(wdata_reg_TC_MEM)

    val cache_miss_MEM  = WireDefault(false.B)
    val data_sel        = WireDefault(FROM_RBUF)
    val d_rvalid        = WireDefault(false.B)
    val addr_sel        = WireDefault(FROM_PIPE)

    // mem we
    val tagv_we_TC      = WireDefault(VecInit(Seq.fill(2)(false.B)))
    val cmem_we_MEM     = WireDefault(VecInit(Seq.fill(2)(0.U(OFFSET_DEPTH.W))))

    // addr decode
    val tag_MEM         = addr_MEM(31, 32-TAG_WIDTH)
    val index_MEM       = addr_MEM(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_MEM      = addr_MEM(OFFSET_WIDTH-1, 0)
    val tag_r_MEM       = tag_reg_TC_MEM

    // lru
    val lru_mem         = RegInit(VecInit(Seq.fill(INDEX_DEPTH)(0.U(1.W))))
    val lru_sel         = lru_mem(index_MEM)
    val lru_miss_upd    = WireDefault(false.B)
    val lru_hit_upd     = WireDefault(false.B)

    // ret buf
    val ret_buf         = RegInit(0.U((8*OFFSET_DEPTH).W))

    // write_buf
    val wrt_buf         = RegInit(0.U((8*OFFSET_DEPTH+32).W))
    val wbuf_we         = WireDefault(false.B)

    // dirty table
    val dirty_table     = RegInit(VecInit(Seq.fill(2)(VecInit(Seq.fill(INDEX_DEPTH)(false.B)))))
    val dirty_we        = WireDefault(false.B)
    val dirty_clean     = WireDefault(false.B)
    val is_dirty        = dirty_table(lru_sel)(index_MEM)

    // wfsm control
    val wfsm_en         = WireDefault(false.B)
    val wfsm_reset      = WireDefault(false.B)
    val wrt_finish      = WireDefault(false.B)
    val d_wvalid        = WireDefault(false.B)
    val d_wlast         = WireDefault(false.B)
    val d_bready        = WireDefault(false.B)

    // EX Stage
    for(i <- 0 until 2){
        tagv(i).addra   := index_MEM
        tagv(i).addrb   := Mux(addr_sel === FROM_PIPE, index_EX, index_TC)
        tagv(i).dina    := true.B ## tag_MEM
        tagv(i).clka    := clock
        tagv(i).wea     := tagv_we_TC(i)
    }
    for(i <- 0 until 2){
        cmem(i).addra   := index_MEM
        cmem(i).addrb   := Mux(addr_sel === FROM_PIPE, index_TC, index_MEM)
        cmem(i).dina    := wdata_MEM
        cmem(i).clka    := clock
        cmem(i).wea     := cmem_we_MEM(i)
    }

    // EX-TC SegReg
    when(!(stall || cache_miss_MEM)){
        addr_reg_EX_TC      := io.addr_EX
        mem_type_reg_EX_TC  := io.mem_type_EX
        wdata_reg_EX_TC     := io.wdata_EX
    }

    // TC Stage
    /* hit logic */
    val hit_TC          = VecInit(Seq.tabulate(2)(i => valid_r_TC(i) && tag_r_TC(i) === tag_TC)).asUInt
    
    // TC-MEM SegReg
    when(!(stall || cache_miss_MEM)){
        addr_reg_TC_MEM     := addr_reg_EX_TC
        mem_type_reg_TC_MEM := mem_type_reg_EX_TC
        wdata_reg_TC_MEM    := wdata_reg_EX_TC
        hit_reg_TC_MEM      := hit_TC
        tag_reg_TC_MEM      := tag_r_TC
    }

    // MEM Stage
    val hit_MEM         = hit_reg_TC_MEM
    val hit_index_MEM   = OHToUInt(hit_MEM)
    val cache_hit_MEM   = hit_MEM.orR
    
    /* rdata logic */
    val block_offset    = offset_MEM ## 0.U(3.W)
    val cmem_rdata_MEM  = (cmem(hit_index_MEM).doutb >> block_offset)(31, 0)
    val rbuf_rdata_MEM  = (ret_buf >> block_offset)(31, 0)
    val rdata_MEM_temp  = Mux(data_sel === FROM_RBUF, rbuf_rdata_MEM, cmem_rdata_MEM)

    val highest_index   = (1.U << mem_type_MEM(1, 0)) ## 0.U(3.W)
    val rmask           = UIntToOH(highest_index)(31, 0) - 1.U
    val rfill           = Mux(mem_type_MEM(2), 0.U(1.W), rdata_MEM_temp((highest_index - 1.U)(4, 0)))
    val rdata_MEM       = (rmask & rdata_MEM_temp) | (~rmask & Fill(32, rfill))

    /* write logic */
    val wmask           = Mux(mem_type_MEM(3), 0.U((8 * OFFSET_DEPTH).W), 
                            ((0.U((8*OFFSET_DEPTH-32).W) ## (UIntToOH(highest_index)(31, 0) - 1.U)(31, 0)) << block_offset))
    val wdata_refill    = ((0.U((8*OFFSET_DEPTH-32).W) ## wdata_reg_TC_MEM) << block_offset)
    wdata_MEM           := (wmask & wdata_refill) | (~wmask & ret_buf)
    val wmask_byte      = VecInit(Seq.tabulate(OFFSET_DEPTH)(i => wmask(8*i))).asUInt

    /* return buffer update logic */
    when(io.d_rready){
        ret_buf := Cat(io.d_rdata, ret_buf(8*OFFSET_DEPTH-1, 32))
    }

    /* lru logic */
    when(lru_hit_upd){
        lru_mem(index_MEM) := !hit_index_MEM
    }.elsewhen(lru_miss_upd){
        lru_mem(index_MEM) := !lru_sel
    }

    /* dirty table logic */
    val is_store_MEM = mem_type_MEM(4)
    val is_load_MEM = mem_type_MEM(3)
    when(dirty_we){
        val write_way = Mux(cache_hit_MEM, hit_index_MEM, lru_sel)
        dirty_table(write_way)(index_MEM) := true.B
    }.elsewhen(dirty_clean){
        dirty_table(lru_sel)(index_MEM) := false.B
    }

    /* write buffer */
    when(wbuf_we){
        wrt_buf := cmem(lru_sel).doutb ## tag_r_MEM(lru_sel) ## addr_MEM(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH) ## 0.U(OFFSET_WIDTH.W)
    }.elsewhen(io.d_wready && io.d_wvalid){
        wrt_buf := 0.U(32.W) ## wrt_buf(8*OFFSET_DEPTH+32-1, 64) ## wrt_buf(31, 0)
    }
    
    /* read state machine */
    val s_idle :: s_miss :: s_refill :: s_wait :: Nil = Enum(4)
    val state = RegInit(s_idle)

    switch(state){
        is(s_idle){
            addr_sel := Mux(stall, FROM_SEG, FROM_PIPE)
            // has req
            when(mem_type_MEM(4, 3).orR){
                state                       := Mux(cache_hit_MEM, s_idle, s_miss)
                lru_hit_upd                 := cache_hit_MEM
                cache_miss_MEM              := !cache_hit_MEM
                data_sel                    := FROM_CMEM
                cmem_we_MEM(hit_index_MEM)  := Mux(is_store_MEM && cache_hit_MEM, wmask_byte, 0.U)
                dirty_we                    := is_store_MEM
                wbuf_we                     := !cache_hit_MEM
                wfsm_en                     := !cache_hit_MEM
            }
        }
        is(s_miss){
            d_rvalid            := true.B
            cache_miss_MEM      := true.B
            state               := Mux(io.d_rready && io.d_rlast, s_refill, s_miss)
            addr_sel            := FROM_SEG
        }
        is(s_refill){
            state                   := s_wait
            cache_miss_MEM          := true.B
            lru_miss_upd            := true.B
            tagv_we_TC(lru_sel)     := true.B
            cmem_we_MEM(lru_sel)    := Fill(OFFSET_DEPTH, 1.U(1.W))
            dirty_clean             := is_load_MEM
            dirty_we                := is_store_MEM
            addr_sel                := FROM_SEG
        }
        is(s_wait){
            addr_sel            := Mux(wrt_finish, FROM_PIPE, FROM_SEG)
            state               := Mux(wrt_finish, s_idle, s_wait)
            wfsm_reset          := true.B
            cache_miss_MEM      := !wrt_finish
        }
    }

    /* write fsm */
    val wrt_count         = RegInit(0.U(8.W))
    val wrt_count_reset   = WireDefault(false.B)
    val wrt_num           = (OFFSET_DEPTH / 4 - 1).U

    when(wrt_count_reset){
        wrt_count := wrt_num
    }.elsewhen(io.d_wvalid && io.d_wready){
        wrt_count := wrt_count - 1.U
    }

    val w_idle :: w_write :: w_finish :: Nil = Enum(3)
    val wrt_state = RegInit(w_idle)

    switch(wrt_state){
        is(w_idle){
            when(wfsm_en){
                wrt_state       := Mux(is_dirty, w_write, w_finish)
                wrt_count_reset := true.B
            }
        }
        is(w_write){
            wrt_state    := Mux(io.d_bvalid, w_finish, w_write)
            d_wvalid     := !wrt_count.andR
            d_wlast      := wrt_count === 0.U
            d_bready     := true.B
        }
        is(w_finish){
            wrt_state       := Mux(wfsm_reset && !stall, w_idle, w_finish)
            wrt_finish      := !stall
        }
    }


    // output
    io.cache_miss_MEM   := cache_miss_MEM
    io.rdata_MEM        := rdata_MEM
    io.d_araddr         := addr_MEM(31, OFFSET_WIDTH) ## 0.U(OFFSET_WIDTH.W)
    io.d_rvalid         := d_rvalid
    io.d_rsize          := 2.U
    io.d_rburst         := 1.U
    io.d_rlen           := (OFFSET_DEPTH / 4 - 1).U

    io.d_awaddr         := wrt_buf(31, 0)
    io.d_wdata          := wrt_buf(63, 32)
    io.d_wvalid         := d_wvalid
    io.d_wlast          := d_wlast
    io.d_wstrb          := Fill(4, 1.U(1.W))
    io.d_wsize          := 2.U
    io.d_wburst         := 1.U
    io.d_wlen           := wrt_num
    io.d_bready         := d_bready
}