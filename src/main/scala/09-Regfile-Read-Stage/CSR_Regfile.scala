
import chisel3._
import chisel3.util._
import CSR_CONFIG._

class CSR_Regfile_IO extends Bundle{
    val raddr           = Input(UInt(14.W))
    val rdata           = Output(UInt(32.W))
    val waddr           = Input(UInt(14.W))
    val we              = Input(Bool())
    val wdata           = Input(UInt(32.W))

    // exception and ertn
    val exception       = Input(UInt(8.W))
    val is_eret         = Input(Bool())
    val pc_exp          = Input(UInt(32.W))
    val eentry_global   = Output(UInt(32.W))

    // interrupt
    val interrupt       = Input(UInt(8.W))
    val ip_int          = Input(Bool())
    val interrupt_vec   = Output(UInt(12.W))

    // debug
    val estat_13        = Output(UInt(13.W))
}

class CSR_Regfile(TLB_INDEX_WIDTH: 5, PALEN: 20, TIMER_INIT_WIDTH: 30) extends Module{
    val io          = IO(new CSR_Regfile_IO)
    val we          = io.we
    val waddr       = io.waddr
    val raddr       = io.raddr
    val wdata       = io.wdata
    val exception   = io.exception
    val is_eret     = io.is_eret

    val timer_int_reg = RegInit(false.B)
    val timer_int = timer_int_reg

    // CRMD：当前模式信息
    val crmd = RegInit(8.U(32.W))
    val prmd = RegInit(0.U(32.W))
    when(exception(7)){
        crmd := crmd(31, 3) ## 0.U(3.W) 
    }.elsewhen(is_eret){
        crmd := crmd(31, 3) ## prmd(2, 0)
    }.elsewhen(we && waddr === CSR_CRMD){
        crmd := 0.U(23.W) ## wdata(8, 0)
    }
    
    // PRMD：例外前模式信息
    when(exception(7)){
        prmd := prmd(31, 3) ## crmd(2, 0)
    }.elsewhen(we && waddr === CSR_PRMD){
        prmd := 0.U(29.W) ## wdata(2, 0)
    }

    // EUEN：扩展部件使能
    val euen = RegInit(0.U(32.W))
    when(we && waddr === CSR_EUEN){
        euen := 0.U(31.W) ## wdata(0)
    }

    // ECFG：例外控制
    val ecfg = RegInit(0.U(32.W))
    when(we && waddr === CSR_ECFG){
        ecfg := 0.U(19.W) ## wdata(12, 11) ## 0.U(1.W) ## wdata(9, 0)
    }

    // ESTAT：例外状态
    val estat = RegInit(0.U(32.W))
    when(exception(7)){
        estat := estat(31) ## 0.U(8.W) ## exception(6, 0) ## estat(15, 0)
    }.elsewhen(we && waddr === CSR_ESTAT){
        estat := 0.U(1.W) ## estat(30, 16) ## 0.U(3.W) ## io.ip_int ## timer_int ## 0.U(1.W) ## io.interrupt ## wdata(1, 0)
    }.otherwise{
        estat := 0.U(1.W) ## estat(30, 16) ## 0.U(3.W) ## io.ip_int ## timer_int ## 0.U(1.W) ## io.interrupt ## estat(1, 0)
    }
    io.estat_13 := estat(13, 0)

    // ERA：例外返回地址
    val era = RegInit(0.U(32.W))
    when(exception(7)){
        era := io.pc_exp
    }.elsewhen(we && waddr === CSR_ERA){
        era := wdata
    }

    // BADV：出错虚地址
    val badv = RegInit(0.U(32.W))
    when(we && waddr === CSR_BADV){
        badv := wdata
    }

    // EENTRY：例外入口地址
    val eentry = RegInit(0.U(32.W))
    when(we && waddr === CSR_EENTRY){
        eentry := wdata(31, 6) ## 0.U(6.W)
    }

    // CPUID：处理器编号
    val cpuid = RegInit(0.U(32.W))
    when(we && waddr === CSR_CPUID){
        cpuid := 0.U(23.W) ## cpuid(8, 0)
    }

    // SAVE0：数据保存
    val save0 = RegInit(0.U(32.W))
    when(we && waddr === CSR_SAVE0){
        save0 := wdata
    }

    // SAVE1：数据保存
    val save1 = RegInit(0.U(32.W))
    when(we && waddr === CSR_SAVE1){
        save1 := wdata
    }

    // SAVE2：数据保存
    val save2 = RegInit(0.U(32.W))
    when(we && waddr === CSR_SAVE2){
        save2 := wdata
    }

    // SAVE3：数据保存
    val save3 = RegInit(0.U(32.W))
    when(we && waddr === CSR_SAVE3){
        save3 := wdata
    }

    // LLBCTL：LLBit控制
    val llbctl = RegInit(0.U(32.W))
    when(we && waddr === CSR_LLBCTL){
        llbctl := 0.U(28.W) ## wdata(2) ## 0.U(1.W) ## Mux(wdata(1), 0.U(1.W), llbctl(0)) 
    }

    // TLBIDX：TLB索引
    val tlbidx = RegInit(0.U(32.W))
    when(we && waddr === CSR_TLBIDX){
        tlbidx := wdata(31) ## 0.U(1.W) ## wdata(29, 24) ## 0.U((24-TLB_INDEX_WIDTH).W) ## wdata(TLB_INDEX_WIDTH-1, 0)
    }

    // TLBEHI：TLB表项高位
    val tlbehi = RegInit(0.U(32.W))
    when(we && waddr === CSR_TLBEHI){
        tlbehi := wdata(31, 13) ## 0.U(13.W)
    }

    // TLBELO0：TLB表项低位
    val tlbelo0 = RegInit(0.U(32.W))
    when(we && waddr === CSR_TLBELO0){
        tlbelo0:= 0.U((36 - PALEN).W) ## wdata(PALEN - 5, 8) ## 0.U(1.W) ## wdata(7, 0)
    }

    // TLBELO1：TLB表项低位
    val tlbelo1 = RegInit(0.U(32.W))
    when(we && waddr === CSR_TLBELO1){
        tlbelo1:= 0.U((36 - PALEN).W) ## wdata(PALEN - 5, 8) ## 0.U(1.W) ## wdata(7, 0)
    }

    // ASID：地址空间标识符
    val asid = RegInit(0.U(32.W))
    when(we && waddr === CSR_ASID){
        asid := 0.U(22.W) ## wdata(9, 0)
    }

    // PGDL：低半地址空间全局目录基址
    val pgdl = RegInit(0.U(32.W))
    when(we && waddr === CSR_PGDL){
        pgdl := wdata(31, 12) ## 0.U(12.W)
    }

    // PGDH：高半地址空间全局目录基址
    val pgdh = RegInit(0.U(32.W))
    when(we && waddr === CSR_PGDH){
        pgdh := wdata(31, 12) ## 0.U(12.W)
    }
    
    // PGD：全局目录基址
    val pgd = RegInit(0.U(32.W))
    when(we && waddr === CSR_PGD){
        pgd := wdata(31, 12) ## 0.U(12.W)
    }

    // TLBRENTRY：TLB表项重填例外入口地址
    val tlbreentry = RegInit(0.U(32.W))
    when(we && waddr === CSR_TLBRENTRY){
        tlbreentry := wdata(31, 6) ## 0.U(5.W)
    }

    // DMW0：直接映射窗口
    val dmw0 = RegInit(0.U(32.W))
    when(we && waddr === CSR_DMW0){
        dmw0 := wdata(31, 29) ## 0.U(1.W) ## wdata(27, 25) ## 0.U(19.W) ## wdata(5, 3) ## 0.U(2.W) ## wdata(0) 
    }

    // DMW1：直接映射窗口
    val dmw1 = RegInit(0.U(32.W))
    when(we && waddr === CSR_DMW1){
        dmw1 := wdata(31, 29) ## 0.U(1.W) ## wdata(27, 25) ## 0.U(19.W) ## wdata(5, 3) ## 0.U(2.W) ## wdata(0) 
    }

    // TID：定时器编号
    val tid = RegInit(0.U(32.W))
    when(we && waddr === CSR_TID){
        tid := wdata
    }

    // TCFG：定时器配置
    val tcfg = RegInit(0.U(32.W))
    when(we && waddr === CSR_TCFG){
        tcfg := 0.U((32 - TIMER_INIT_WIDTH).W) ## wdata(TIMER_INIT_WIDTH - 1, 0)
    }

    // TVAL：定时器数值
    val tval = RegInit(0.U(32.W))
    when(we && waddr === CSR_TCFG){
        tval := 0.U((32 - TIMER_INIT_WIDTH).W) ## wdata(TIMER_INIT_WIDTH - 1, 2) ## 1.U(2.W)
    }.elsewhen(tcfg(0) === 1.U){
        when(tval === 0.U){
            tval := 0.U((32 - TIMER_INIT_WIDTH).W) ## Mux(tcfg(1), tcfg(TIMER_INIT_WIDTH - 1, 2) ## 1.U(2.W), 
                                                                   0.U(TIMER_INIT_WIDTH.W))
        }.otherwise{
            tval := tval - 1.U
        }
    }

    // TICLR：定时器中断清除
    val ticlr = RegInit(0.U(32.W))
    val tval_edge = ShiftRegister(tval, 1)
    when(we && waddr === CSR_TICLR && wdata(0) === 1.U){
        timer_int_reg := false.B
    }.elsewhen(tcfg(0) === 1.U && tval === 0.U && tval_edge === 1.U){
        timer_int_reg := true.B
    }

    io.interrupt_vec := Mux(!crmd(2), 0.U(12.W), (estat(12, 11) & ecfg(12, 11)) ## (estat(9, 0) & ecfg(9, 0)))  

    val rdata = WireDefault(0.U(32.W))

    switch(raddr){
        is(CSR_CRMD)        { rdata := crmd }
        is(CSR_PRMD)        { rdata := prmd }
        is(CSR_EUEN)        { rdata := euen }
        is(CSR_ECFG)        { rdata := ecfg }   
        is(CSR_ESTAT)       { rdata := estat }
        is(CSR_ERA)         { rdata := era }
        is(CSR_BADV)        { rdata := badv }
        is(CSR_EENTRY)      { rdata := eentry }
        is(CSR_CPUID)       { rdata := cpuid }
        is(CSR_SAVE0)       { rdata := save0 }
        is(CSR_SAVE1)       { rdata := save1 }
        is(CSR_SAVE2)       { rdata := save2 }
        is(CSR_SAVE3)       { rdata := save3 }
        is(CSR_LLBCTL)      { rdata := llbctl }
        is(CSR_TLBIDX)      { rdata := tlbidx }
        is(CSR_TLBEHI)      { rdata := tlbehi }
        is(CSR_TLBELO0)     { rdata := tlbelo0 }
        is(CSR_TLBELO1)     { rdata := tlbelo1 }
        is(CSR_ASID)        { rdata := asid }
        is(CSR_PGDL)        { rdata := pgdl }
        is(CSR_PGDH)        { rdata := pgdh }
        is(CSR_PGD)         { rdata := pgd }
        is(CSR_TLBRENTRY)   { rdata := tlbreentry }
        is(CSR_DMW0)        { rdata := dmw0 }
        is(CSR_DMW1)        { rdata := dmw1 }
        is(CSR_TID)         { rdata := tid }
        is(CSR_TCFG)        { rdata := tcfg }
        is(CSR_TVAL)        { rdata := tval }
        is(CSR_TICLR)       { rdata := ticlr }
    }
    io.rdata            := rdata
    io.eentry_global    := eentry

}