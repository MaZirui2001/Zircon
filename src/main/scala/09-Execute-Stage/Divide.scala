import chisel3._
import chisel3.util._
import Control_Signal._

class Divide extends Module{
    val io = IO(new Bundle{
        val src1        = Input(UInt(32.W))
        val src2        = Input(UInt(32.W))
        val op          = Input(UInt(5.W))
        val res         = Output(UInt(32.W))
        val busy        = Output(Bool())
    })

    val en = (io.op === ALU_DIV) || (io.op === ALU_DIVU) || (io.op === ALU_MOD) || (io.op === ALU_MODU)
    // stage1 : record src2 and op and sign
    val res_sign = MuxLookup(io.op, false.B)(Seq(
        ALU_DIV  -> (io.src1(31) ^ io.src2(31)),
        ALU_DIVU -> false.B,
        ALU_MOD  -> io.src1(31),
        ALU_MODU -> false.B
    ))
    val src1 = Mux((io.op === ALU_DIV || io.op === ALU_MOD) && io.src1(31), ~io.src1 + 1.U, io.src1)
    val src2 = Mux((io.op === ALU_DIV || io.op === ALU_MOD) && io.src2(31), ~io.src2 + 1.U, io.src2)

    // get highest 1 in src1
    val high_rev = PriorityEncoder(Reverse(src1))
    val cnt = RegInit(0.U(6.W))
    when(cnt =/= 0.U){
        cnt := cnt - 1.U
    }.elsewhen(en){
        cnt := 33.U - high_rev
    }
    val src2_reg = RegInit(0.U(32.W))
    val op_reg = RegInit(0.U(5.W))
    val res_sign_reg = RegInit(false.B)

    when(en && cnt === 0.U){
        src2_reg := src2
        op_reg := io.op
        res_sign_reg := res_sign
    }

    val quo_rem = RegInit(0.U(65.W))
    when(cnt =/= 0.U){
        when(quo_rem(63, 32) >= src2_reg){
            quo_rem := (quo_rem(63, 32) - src2_reg) ## quo_rem(31, 0) ## 1.U(1.W)
        }.otherwise{
            quo_rem := (quo_rem(63, 0) ## 0.U(1.W))
        }
    }.elsewhen(en){
        quo_rem := (0.U(33.W) ## src1) << high_rev
    }

    io.busy := cnt =/= 0.U

    io.res := MuxLookup(op_reg, 0.U(32.W))(Seq(
        ALU_DIV  -> Mux(res_sign_reg, ~quo_rem(31, 0) + 1.U, quo_rem(31, 0)),
        ALU_DIVU -> quo_rem(31, 0),
        ALU_MOD  -> Mux(res_sign_reg, ~quo_rem(64, 33) + 1.U, quo_rem(64, 33)),
        ALU_MODU -> quo_rem(64, 33)
    ))
}