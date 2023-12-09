import chisel3._
import chisel3.util._
import Control_Signal._
class Multiply extends Module{
    val io = IO(new Bundle{
        val src1 = Input(UInt(32.W))
        val src2 = Input(UInt(32.W))
        val op   = Input(UInt(5.W))
        val res  = Output(UInt(32.W))
    })

    val src1 = Mux(io.op === ALU_MULHU, 0.U(34.W) ## io.src1, Fill(34, io.src1(31)) ## io.src1)
    val src2 = Mux(io.op === ALU_MULHU, 0.U(34.W) ## io.src2, Fill(34, io.src2(31)) ## io.src2)

    def Booth2(x: UInt, y: UInt, n: Int) :UInt = {
        val res = WireDefault(x)
        val neg = y(2)
        switch(y){
            is(0.U){ res := 0.U }
            is(3.U){ res := x(n-2, 0) ## 0.U(1.W) }
            is(4.U){ res := x(n-2, 0) ## 0.U(1.W) }
            is(7.U){ res := 0.U }
        }
        Mux(neg, ~res + 1.U, res)
    }
    def CSA(x: UInt, y: UInt, z: UInt, n: Int) : UInt  = {
        val x_xor_y = WireDefault(x ^ y)
        val res1 = WireDefault(x_xor_y ^ z)
        val res2 = WireDefault((((x & y) | z & (x_xor_y)) ## 0.U(1.W))(n-1, 0))
        res2 ## res1
    }

    // stage 1: booth and wallce tree
    // booth encode
    val booth = VecInit(Seq.tabulate(33){ i => 
        Booth2(src1(65-2*i, 0) ## 0.U((2*i).W),
            (if(i == 0) src2(1, 0) ## 0.U(1.W) else src2(2*i+1, 2*i-1)), 66
        )
    })
    // wallce tree
    // level1: input 33, output 22
    val level1         = VecInit(Seq.tabulate(11){i => CSA(booth(3*i), booth(3*i+1), booth(3*i+2), 66)})
    val res1           = Wire(Vec(22, UInt(66.W)))
    for(i <- 0 until 11){
        res1(2*i)       := level1(i)(65, 0)
        res1(2*i+1)     := level1(i)(131, 66)
    }
    // level2: input 22, output 15
    val level2         = VecInit(Seq.tabulate(7){i => CSA(res1(3*i), res1(3*i+1), res1(3*i+2), 66)})
    val res2           = Wire(Vec(15, UInt(66.W)))
    for(i <- 0 until 7){
        res2(2*i)       := level2(i)(65, 0)
        res2(2*i+1)     := level2(i)(131, 66)
    }
    res2(14) := res1(21)
    // level3: input 15, output 10
    val level3         = VecInit(Seq.tabulate(5){i => CSA(res2(3*i), res2(3*i+1), res2(3*i+2), 66)})
    val res3           = Wire(Vec(10, UInt(66.W)))
    for(i <- 0 until 5){
        res3(2*i)       := level3(i)(65, 0)
        res3(2*i+1)     := level3(i)(131, 66)
    }
    // level4: input 10, output 7
    val level4         = VecInit(Seq.tabulate(3){i => CSA(res3(3*i), res3(3*i+1), res3(3*i+2), 66)})
    val res4           = Wire(Vec(7, UInt(66.W)))
    for(i <- 0 until 3){
        res4(2*i)       := level4(i)(65, 0)
        res4(2*i+1)     := level4(i)(131, 66)
    }
    res4(6) := res3(9)
    val level5        = VecInit(Seq.tabulate(2){i => CSA(res4(3*i), res4(3*i+1), res4(3*i+2), 66)})
    val res5          = Wire(Vec(5, UInt(66.W)))
    for(i <- 0 until 2){
        res5(2*i)       := level5(i)(65, 0)
        res5(2*i+1)     := level5(i)(131, 66)
    }
    res5(4) := res4(6)
    // level6: input 5, output 4
    val level6     = VecInit(Seq.tabulate(1){i => CSA(res5(0), res5(1), res5(2), 66)})
    val res6       = Wire(Vec(4, UInt(66.W)))
    res6(0)        := level6(0)(65, 0)
    res6(1)        := level6(0)(131, 66)
    res6(2)        := res5(3)
    res6(3)        := res5(4)
    // level7: input 4, output 3
    val level7      = VecInit(Seq.tabulate(1){i => CSA(res6(0), res6(1), res6(2), 66)})
    val res7        = Wire(Vec(3, UInt(66.W)))
    res7(0)         := level7(0)(65, 0)
    res7(1)         := level7(0)(131, 66)
    res7(2)         := res6(3)
    // level8: input 3, output 2
    val level8      = VecInit(Seq.tabulate(1){i => CSA(res7(0), res7(1), res7(2), 66)})
    val res8        = Wire(Vec(2, UInt(66.W)))
    res8(0)         := level8(0)(65, 0)
    res8(1)         := level8(0)(131, 66)

    // register
    val adder_src1  = ShiftRegister(res8(0), 1, 0.U(66.W), true.B)
    val adder_src2  = ShiftRegister(res8(1), 1, 0.U(66.W), true.B)
    val op_reg      = ShiftRegister(io.op, 1, 0.U(5.W), true.B)
    val res         = adder_src1 + adder_src2
    io.res          := Mux(op_reg === ALU_MUL, res(31, 0), res(63, 32))

}