import chisel3._
import chisel3.util._
import CPU_Config._


class DCache_IO extends Bundle{
    // RF stage
    val addr_RF         = Input(UInt(32.W))
    val mem_type_RF     = Input(UInt(5.W))
    val wdata_RF        = Input(UInt(32.W))
    val store_cmt_RF    = Input(Bool())
    // val uncache_RF      = Input(Bool())

    // EX stage
    val rob_index_EX    = Input(UInt(log2Ceil(ROB_NUM).W))
    val paddr_EX        = Input(UInt(32.W))
    val uncache_EX      = Input(Bool())
    // val exception_EX    = Input(Bool())

    // MEM stage
    val cache_miss_MEM  = Output(Vec(5, Bool()))
    val rdata_MEM       = Output(UInt(32.W))
    val exception_MEM   = Input(Bool())

    val cache_miss_iq   = Output(Bool())

    // uncache cmt
    val rob_index_CMT   = Input(UInt(log2Ceil(ROB_NUM).W))

    // cacop 
    val cacop_en        = Input(Bool())
    val cacop_op        = Input(UInt(2.W))

    // control
    val stall           = Input(Bool())
    val flush           = Input(Bool())
    val has_store       = Output(Bool())

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

    // for stat
    val commit_dcache_visit    = Output(Bool())
    val commit_dcache_miss     = Output(Bool())
}
import DCache_Config._
class DCache extends Module{
    val io = IO(new DCache_IO)
    val stall                       = io.stall

    // address decode EX        
    val addr_RF                     = io.addr_RF
    val tag_RF                      = addr_RF(31, 32-TAG_WIDTH)
    val index_RF                    = addr_RF(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_RF                   = addr_RF(OFFSET_WIDTH-1, 0)

    val data_sel                    = WireDefault(FROM_RBUF)
    val d_rvalid                    = WireDefault(false.B)
    val addr_sel                    = WireDefault(FROM_PIPE)
    val cache_miss_MEM              = WireDefault(VecInit.fill(5)(false.B))

    // RF-EX SegReg     
    val EX_TC_en                    = !(stall || cache_miss_MEM(4))
    val addr_reg_RF_EX              = ShiftRegister(io.addr_RF, 1, EX_TC_en)
    val mem_type_reg_RF_EX          = ShiftRegister(io.mem_type_RF, 1, EX_TC_en)
    val wdata_reg_RF_EX             = ShiftRegister(io.wdata_RF, 1, EX_TC_en)
    val store_cmt_reg_RF_EX         = ShiftRegister(io.store_cmt_RF, 1, EX_TC_en)
    val cacop_en_reg_RF_EX          = ShiftRegister(io.cacop_en, 1, EX_TC_en)
    val cacop_op_reg_RF_EX          = ShiftRegister(io.cacop_op, 1, EX_TC_en)
    val flush_RF_EX                 = ShiftRegister(io.flush, 1, EX_TC_en || io.flush)

    // EX Stage
    val tagv                        = VecInit.fill(2)(Module(new xilinx_simple_dual_port_1_clock_ram_write_first(TAG_WIDTH+1, INDEX_DEPTH)).io)
    val cmem                        = VecInit.fill(2)(Module(new xilinx_simple_dual_port_byte_write_1_clock_ram_write_first(OFFSET_DEPTH, 8, INDEX_DEPTH)).io)
    val tag_r_EX                    = VecInit.tabulate(2)(i => tagv(i).doutb(TAG_WIDTH-1, 0))
    val valid_r_EX                  = VecInit.tabulate(2)(i => tagv(i).doutb(TAG_WIDTH))
    val uncache_EX                  = Mux(store_cmt_reg_RF_EX, false.B, Mux(flush_RF_EX, true.B, io.uncache_EX))
    val cmem_rdata_EX               = VecInit.tabulate(2)(i => cmem(i).doutb)
    val hit_EX                      = WireDefault(0.U(2.W))
    
    // decode
    val addr_EX                     = addr_reg_RF_EX
    val paddr_EX                    = io.paddr_EX
    val tag_EX                      = Mux(store_cmt_reg_RF_EX, addr_EX(31, 32-TAG_WIDTH), paddr_EX(31, 32-TAG_WIDTH))
    val index_EX                    = addr_EX(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)


    // TC-MEM SegReg
    val TC_MEM_en                   = !(stall || cache_miss_MEM(4))
    val addr_reg_EX_MEM             = ShiftRegister(addr_reg_RF_EX, 1, TC_MEM_en)
    val paddr_reg_EX_MEM            = ShiftRegister(Mux(store_cmt_reg_RF_EX || flush_RF_EX, addr_reg_RF_EX, io.paddr_EX), 1, TC_MEM_en)
    val mem_type_reg_EX_MEM         = ShiftRegister(Mux(mem_type_reg_RF_EX(3) || uncache_EX || store_cmt_reg_RF_EX, mem_type_reg_RF_EX, 0.U), 1, TC_MEM_en)
    val wdata_reg_EX_MEM            = ShiftRegister(wdata_reg_RF_EX, 1, TC_MEM_en)
    val uncache_reg_EX_MEM          = ShiftRegister(uncache_EX, 1, TC_MEM_en)
    val rob_index_EX_MEM            = ShiftRegister(io.rob_index_EX, 1, TC_MEM_en)
    val hit_reg_EX_MEM              = ShiftRegister(hit_EX, 1, TC_MEM_en)
    val valid_reg_EX_MEM            = ShiftRegister(valid_r_EX, 1, TC_MEM_en)
    val tag_reg_EX_MEM              = ShiftRegister(tag_r_EX, 1, TC_MEM_en)
    val cacop_en_reg_EX_MEM         = ShiftRegister(cacop_en_reg_RF_EX, 1, TC_MEM_en)
    val cacop_op_reg_EX_MEM         = ShiftRegister(cacop_op_reg_RF_EX, 1, TC_MEM_en)
    val flush_EX_MEM                = ShiftRegister(Mux(io.flush, io.flush, flush_RF_EX), 1, TC_MEM_en || io.flush)
    val cmem_rdata_reg_EX_MEM       = ShiftRegister(cmem_rdata_EX, 1, TC_MEM_en)
    val mem_type_reg_EX_MEM_backup  = ShiftRegister(VecInit.fill(5)(Mux(mem_type_reg_RF_EX(3) || uncache_EX || store_cmt_reg_RF_EX, mem_type_reg_RF_EX, 0.U)), 1, TC_MEM_en)

    val exception_MEM               = ShiftRegister(io.exception_MEM, 1)

    // MEM Stage
   
    val addr_MEM                    = paddr_reg_EX_MEM
    val mem_type_MEM                = mem_type_reg_EX_MEM
    val wdata_MEM                   = WireDefault(wdata_reg_EX_MEM)
    val cacop_en_MEM                = cacop_en_reg_EX_MEM
    val cacop_op_MEM                = cacop_op_reg_EX_MEM
    val cmem_rdata_MEM              = cmem_rdata_reg_EX_MEM

    // optimize fanout
    val mem_type_MEM_backup         = mem_type_reg_EX_MEM_backup

    // mem we
    val tagv_we_EX                  = WireDefault(VecInit.fill(2)(false.B))
    val cmem_we_MEM                 = WireDefault(VecInit.fill(2)(0.U(OFFSET_DEPTH.W)))

    // addr decode          
    val tag_MEM                     = addr_MEM(31, 32-TAG_WIDTH)
    val index_MEM                   = addr_MEM(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH)
    val offset_MEM                  = addr_MEM(OFFSET_WIDTH-1, 0)
    val uncache_MEM                 = uncache_reg_EX_MEM
    val tag_r_MEM                   = tag_reg_EX_MEM
    val valid_r_MEM                 = valid_reg_EX_MEM

    // lru          
    val lru_mem                     = RegInit(VecInit.fill(INDEX_DEPTH)(0.U(1.W)))
    val lru_sel                     = lru_mem(index_MEM)
    val lru_miss_upd                = WireDefault(false.B)
    val lru_hit_upd                 = WireDefault(false.B)

    // ret buf          
    val ret_buf                     = RegInit(0.U((8*OFFSET_DEPTH).W))

    // write_buf            
    val wrt_buf                     = RegInit(0.U((8*OFFSET_DEPTH+32).W))
    val wbuf_we                     = WireDefault(false.B)

    // dirty table          
    val dirty_table                 = RegInit(VecInit.fill(2)(VecInit.fill(INDEX_DEPTH)(false.B)))
    val dirty_we                    = WireDefault(false.B)
    val dirty_clean                 = WireDefault(false.B)
    val is_dirty                    = dirty_table(lru_sel)(index_MEM)

    // wfsm control         
    val wfsm_en                     = WireDefault(false.B)
    val wfsm_reset                  = WireDefault(false.B)
    val wrt_finish                  = WireDefault(false.B)
    val d_wvalid                    = WireDefault(false.B)
    val d_wlast                     = WireDefault(false.B)
    val d_bready                    = WireDefault(false.B)

    // stat         
    val dcache_miss                 = WireDefault(false.B)
    val dcache_visit                = WireDefault(false.B)

    // EX Stage
    for(i <- 0 until 2){
        tagv(i).addra   := index_MEM
        tagv(i).addrb   := Mux(addr_sel === FROM_PIPE, index_RF, index_EX)
        tagv(i).dina    := Mux(cacop_en_MEM, 0.U, true.B ## tag_MEM)
        tagv(i).clka    := clock
        tagv(i).wea     := tagv_we_EX(i)
    }
    for(i <- 0 until 2){
        cmem(i).addra   := index_MEM
        cmem(i).addrb   := Mux(addr_sel === FROM_PIPE, index_RF, index_EX)
        cmem(i).dina    := wdata_MEM
        cmem(i).clka    := clock
        cmem(i).wea     := cmem_we_MEM(i)
    }
    /* hit logic */
    hit_EX              := VecInit.tabulate(2)(i => valid_r_EX(i) && !(tag_r_EX(i) ^ tag_EX)).asUInt

    // MEM Stage
    val hit_MEM         = VecInit.tabulate(2)(i => hit_reg_EX_MEM(i)).asUInt
    val hit_index_MEM   = OHToUInt(hit_MEM)
    val cache_hit_MEM   = hit_MEM.orR
    val cacop_way_MEM   = Mux(cacop_op_MEM(1), hit_index_MEM, addr_MEM(0))
    val cacop_exec_MEM  = Mux(cacop_op_MEM(1), cache_hit_MEM, true.B)
    
    /* rdata logic */
    val block_offset    = offset_MEM ## 0.U(3.W)
    val mem_rdata_MEM   = (cmem_rdata_reg_EX_MEM(hit_index_MEM) >> block_offset)(31, 0)
    val rbuf_rdata_MEM  = Mux(uncache_MEM, ret_buf(8*OFFSET_DEPTH-1, 8*OFFSET_DEPTH-32), (ret_buf >> block_offset)(31, 0))
    val rdata_MEM_temp  = Mux(data_sel === FROM_RBUF, rbuf_rdata_MEM, mem_rdata_MEM)

    val highest_index   = (1.U << mem_type_MEM(1, 0)) ## 0.U(3.W)
    val highest_mask    = (UIntToOH(highest_index)(31, 0) - 1.U)(31, 0)
    val rmask           = highest_mask
    val rdata_MEM       = (rmask & rdata_MEM_temp)

    io.cache_miss_iq    := cacop_en_MEM || mem_type_reg_EX_MEM(4, 3).orR && (uncache_MEM || !hit_MEM.orR)

    /* write logic */
    val wmask           = Mux(mem_type_MEM(3), 0.U((8*OFFSET_DEPTH).W), ((0.U((8*OFFSET_DEPTH-32).W) ## highest_mask) << block_offset))
    val wdata_refill    = ((0.U((8*OFFSET_DEPTH-32).W) ## wdata_reg_EX_MEM) << block_offset)
    wdata_MEM           := (wmask & wdata_refill) | (~wmask & ret_buf)
    val wmask_byte      = VecInit.tabulate(OFFSET_DEPTH)(i => wmask(8*i)).asUInt

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
    val is_store_MEM    = mem_type_MEM(4)
    val is_load_MEM     = mem_type_MEM(3)
    when(dirty_we){
        val write_way = Mux(cache_hit_MEM, hit_index_MEM, lru_sel)
        dirty_table(write_way)(index_MEM) := true.B
    }.elsewhen(dirty_clean){
        dirty_table(lru_sel)(index_MEM) := false.B
    }

    /* write buffer */
    when(wbuf_we){
        when(cacop_en_MEM){
            val cmem_wb_idx = Mux(cacop_op_MEM(1), hit_index_MEM, addr_MEM(0))
            wrt_buf := cmem_rdata_MEM(cmem_wb_idx) ## addr_MEM
        }.elsewhen(uncache_MEM){
            wrt_buf := 0.U((8*OFFSET_DEPTH-32).W) ## wdata_reg_EX_MEM ## addr_MEM
        }.otherwise{
            wrt_buf := cmem_rdata_MEM(lru_sel) ## tag_r_MEM(lru_sel) ## addr_MEM(INDEX_WIDTH+OFFSET_WIDTH-1, OFFSET_WIDTH) ## 0.U(OFFSET_WIDTH.W)
        }
    }.elsewhen(io.d_wready && io.d_wvalid){
        wrt_buf := 0.U(32.W) ## wrt_buf(8*OFFSET_DEPTH+32-1, 64) ## wrt_buf(31, 0)
    }
    
    /* read state machine */
    val s_idle :: s_miss :: s_refill :: s_wait :: s_hold :: Nil = Enum(5)
    val state = RegInit(s_idle)
    // optimize fanout
    val state_backup = RegInit(VecInit.fill(5)(s_idle))

    switch(state){
        is(s_idle){
            when(cacop_en_MEM){
                state               := Mux(cacop_exec_MEM, s_refill, s_idle)
                // cache_miss_MEM   := cacop_exec_MEM
                addr_sel            := Mux(cacop_exec_MEM, FROM_SEG, FROM_PIPE)
                wbuf_we             := cacop_exec_MEM
                wfsm_en             := cacop_exec_MEM
            }.elsewhen(mem_type_MEM(4, 3).orR){
                when(uncache_MEM){
                    state               := s_hold
                    //cache_miss_MEM    := true.B
                    addr_sel            := FROM_SEG
                }.otherwise{
                    state                       := Mux(cache_hit_MEM, s_idle, s_miss)
                    //cache_miss_MEM            := !cache_hit_MEM
                    lru_hit_upd                 := cache_hit_MEM
                    data_sel                    := FROM_CMEM
                    cmem_we_MEM(hit_index_MEM)  := Mux(is_store_MEM && cache_hit_MEM, wmask_byte, 0.U)
                    dirty_we                    := is_store_MEM
                    wbuf_we                     := !cache_hit_MEM
                    wfsm_en                     := !cache_hit_MEM
                    addr_sel                    := FROM_PIPE
                    dcache_visit                := true.B
                    dcache_miss                 := !cache_hit_MEM
                }
            }
        }
        is(s_miss){
            d_rvalid                := true.B
            //cache_miss_MEM        := true.B
            state                   := Mux(io.d_rready && io.d_rlast, Mux(uncache_MEM, s_wait, s_refill), s_miss)
            addr_sel                := FROM_SEG
        }
        is(s_refill){
            val tag_idx             = Mux(cacop_en_MEM, cacop_way_MEM, lru_sel)
            state                   := s_wait
            //cache_miss_MEM        := true.B
            lru_miss_upd            := !cacop_en_MEM
            tagv_we_EX(tag_idx)     := true.B
            cmem_we_MEM(lru_sel)    := Mux(cacop_en_MEM, 0.U(OFFSET_DEPTH.W), Fill(OFFSET_DEPTH, 1.U(1.W)))
            dirty_clean             := is_load_MEM || cacop_en_MEM
            dirty_we                := is_store_MEM
            addr_sel                := FROM_SEG
        }
        is(s_wait){
            addr_sel                := Mux(wrt_finish, FROM_PIPE, FROM_SEG)
            state                   := Mux(wrt_finish, s_idle, s_wait)
            wfsm_reset              := true.B
            //cache_miss_MEM        := !wrt_finish
        }
        is(s_hold){
            val confirm_exec        = io.rob_index_CMT === rob_index_EX_MEM
            addr_sel                := Mux(flush_EX_MEM || exception_MEM, FROM_PIPE, FROM_SEG)
            state                   := Mux(flush_EX_MEM || exception_MEM, s_idle, Mux(confirm_exec, Mux(is_store_MEM, s_wait, s_miss), s_hold))
            //cache_miss_MEM        := !flush_EX_MEM
            wfsm_reset              := flush_EX_MEM || exception_MEM
            wfsm_en                 := confirm_exec && !(flush_EX_MEM || exception_MEM)
            wbuf_we                 := confirm_exec && !(flush_EX_MEM || exception_MEM)
        }
    }
    // optimize fanout
    for(i <- 0 until 5){
        switch(state_backup(i)){
            is(s_idle){
                when(cacop_en_MEM){
                    state_backup(i)     := Mux(cacop_exec_MEM, s_refill, s_idle)
                    cache_miss_MEM(i)   := cacop_exec_MEM
                }.elsewhen(mem_type_MEM_backup(i)(4, 3).orR){
                    when(uncache_MEM){
                        state_backup(i)     := s_hold
                        cache_miss_MEM(i)   := true.B
                    }.otherwise{
                        state_backup(i)     := Mux(cache_hit_MEM, s_idle, s_miss)
                        cache_miss_MEM(i)   := !cache_hit_MEM
                    }
                }
            }
            is(s_miss){
                cache_miss_MEM(i)   := true.B
                state_backup(i)     := Mux(io.d_rready && io.d_rlast, Mux(uncache_MEM, s_wait, s_refill), s_miss)
            }
            is(s_refill){
                state_backup(i)     := s_wait
                cache_miss_MEM(i)   := true.B
            }
            is(s_wait){
                state_backup(i)     := Mux(wrt_finish, s_idle, s_wait)
                cache_miss_MEM(i)   := !wrt_finish
            }
            is(s_hold){
                val confirm_exec    = io.rob_index_CMT === rob_index_EX_MEM
                state_backup(i)     := Mux(flush_EX_MEM || exception_MEM, s_idle, Mux(confirm_exec, Mux(is_store_MEM, s_wait, s_miss), s_hold))
                cache_miss_MEM(i)   := !(flush_EX_MEM || exception_MEM)
            }
        }
    }

    /* write fsm */
    val wrt_count         = RegInit(0.U(8.W))
    val wrt_count_reset   = WireDefault(false.B)
    val wrt_num           = Mux(uncache_MEM, 0.U, (OFFSET_DEPTH / 4 - 1).U)

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
                when(cacop_en_MEM){
                    wrt_state       := Mux(cacop_op_MEM === 0.U, w_finish, Mux(is_dirty, w_write, w_finish))
                }.elsewhen(uncache_MEM){
                    wrt_state       := Mux(is_store_MEM, w_write, w_finish)
                }.otherwise{
                    wrt_state       := Mux(is_dirty, w_write, w_finish)
                }
                wrt_count_reset     := true.B
            }
        }
        is(w_write){
            wrt_state    := Mux(io.d_bvalid, w_finish, w_write)
            d_wvalid     := !wrt_count.andR
            d_wlast      := wrt_count === 0.U
            d_bready     := true.B
        }
        is(w_finish){
            wrt_state    := Mux(wfsm_reset && !stall, w_idle, w_finish)
            wrt_finish   := !stall
        }
    }


    // output
    io.has_store        := io.mem_type_RF(4) || mem_type_reg_RF_EX(4)
    io.cache_miss_MEM   := cache_miss_MEM
    io.rdata_MEM        := rdata_MEM
    io.d_araddr         := Mux(uncache_MEM, addr_MEM, addr_MEM(31, OFFSET_WIDTH) ## 0.U(OFFSET_WIDTH.W))
    io.d_rvalid         := d_rvalid
    io.d_rsize          := Mux(uncache_MEM, mem_type_MEM(1, 0), 2.U)
    io.d_rburst         := 1.U
    io.d_rlen           := Mux(uncache_MEM, 0.U, (OFFSET_DEPTH / 4 - 1).U)

    io.d_awaddr         := wrt_buf(31, 0)
    io.d_wdata          := wrt_buf(63, 32)
    io.d_wvalid         := d_wvalid
    io.d_wlast          := d_wlast
    io.d_wstrb          := Mux(uncache_MEM, (UIntToOH(UIntToOH(mem_type_MEM(1, 0))) - 1.U)(3, 0), Fill(4, 1.U(1.W)))
    io.d_wsize          := Mux(uncache_MEM, mem_type_MEM(1, 0), 2.U)
    io.d_wburst         := 1.U
    io.d_wlen           := wrt_num
    io.d_bready         := d_bready

    io.commit_dcache_miss   := dcache_miss
    io.commit_dcache_visit  := dcache_visit
    
}