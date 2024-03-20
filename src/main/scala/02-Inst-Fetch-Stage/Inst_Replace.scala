import chisel3._
import chisel3.util._
import ICache_Config._
import ICache_Struct._
import PreDecode_Config._
import Control_Signal._


class Inst_Replace_IO extends Bundle {
    val inst_raw        = Input(UInt(32.W))
    val pc              = Input(UInt(32.W))
    
    val icache_wdata    = Output(new icache_t)
}

class Inst_Replace extends Module {
    val io = IO(new Inst_Replace_IO)
    
    val inst_raw        = io.inst_raw

    val pc              = io.pc

    val br_type         = WireDefault(NOT_JUMP)


    when(inst_raw(31, 30) === "b01".U){
        when(inst_raw(29, 26) === BR_B || inst_raw(29, 26) === BR_BL){
            br_type  := YES_JUMP
        }.elsewhen(inst_raw(29, 26) =/= BR_JIRL){
            br_type := MAY_JUMP
        }
    }
    
    val pc_add_imm16 = pc + Cat(Fill(14, inst_raw(25)), inst_raw(25, 10), 0.U(2.W))
    val pc_add_imm26 = pc + Cat(Fill(4, inst_raw(9)), inst_raw(9, 0), inst_raw(25, 10), 0.U(2.W))
    val icache_wdata = WireDefault(0.U.asTypeOf(new icache_t))
    icache_wdata.inst := inst_raw
    icache_wdata.pc_sign := 0.U
    switch(br_type){
        is(YES_JUMP){
            icache_wdata.inst := Cat(inst_raw(31, 26), pc_add_imm26(17, 2), pc_add_imm26(27, 18))
            icache_wdata.pc_sign := (pc_add_imm26(31, 28) - pc(31, 28))(1, 0)
        }
        is(MAY_JUMP){
            icache_wdata.inst := Cat(inst_raw(31, 26), pc_add_imm16(17, 2), inst_raw(9, 0))
            icache_wdata.pc_sign := (pc_add_imm16(31, 18) - pc(31, 18))(1, 0)
        }
    }
    io.icache_wdata := icache_wdata
}