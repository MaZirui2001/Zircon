import chisel3._
import chisel3.util._

// LUT: 1119 FF: 425

object ROB_Pack{
    class rob_t extends Bundle(){
        val rd = UInt(5.W)
        val rd_valid = Bool()
        val prd = UInt(6.W)
        val pprd = UInt(6.W)
        val predict_fail = Bool()
        val complete = Bool()
        val pc = UInt(32.W)
    }
    
}
class ROB_IO(n: Int) extends Bundle{
    // for reg rename
    val inst_valid_rn = Input(Vec(4, Bool()))
    val rd_rn = Input(Vec(4, UInt(5.W)))
    val rd_valid_rn = Input(Vec(4, Bool()))
    val prd_rn = Input(Vec(4, UInt(6.W)))
    val pprd_rn = Input(Vec(4, UInt(6.W)))
    val rob_index_rn = Output(Vec(4, UInt(log2Ceil(n).W)))
    val pc_rn = Input(Vec(4, UInt(32.W)))
    val full = Output(Bool())
    
    // for wb stage
    val inst_valid_wb = Input(Vec(4, Bool()))
    val rob_index_wb = Input(Vec(4, UInt(log2Ceil(n).W)))
    val predict_fail_wb = Input(Vec(4, Bool()))

    // for cpu state: arch rat
    val cmt_en = Output(Vec(4, Bool()))
    val rd_cmt = Output(Vec(4, UInt(5.W)))
    val prd_cmt = Output(Vec(4, UInt(6.W)))
    val rd_valid_cmt = Output(Vec(4, Bool()))
    val pprd_cmt = Output(Vec(4, UInt(6.W)))
    val pc_cmt = Output(Vec(4, UInt(32.W)))

    val predict_fail_cmt = Output(Bool())
}

class ROB(n: Int) extends Module{
    val io = IO(new ROB_IO(n))
    import ROB_Pack._
    val rob = RegInit(VecInit(Seq.fill(n)(0.U.asTypeOf(new rob_t))))

    val head = RegInit(0.U(log2Ceil(n).W))
    val tail = RegInit(0.U(log2Ceil(n).W))
    val elem_num = RegInit(0.U((log2Ceil(n)+1).W))

    val empty = head === tail
    val insert_num = PopCount(io.inst_valid_rn)
    val full = elem_num + insert_num > n.asUInt

    when(~full){
        for(i <- 0 until 4){
            when(io.inst_valid_rn(i)){
                rob(tail+i.U).rd := io.rd_rn(i)
                rob(tail+i.U).rd_valid := io.rd_valid_rn(i)
                rob(tail+i.U).prd := io.prd_rn(i)
                rob(tail+i.U).pprd := io.pprd_rn(i)
                rob(tail+i.U).predict_fail := false.B
                rob(tail+i.U).complete := false.B
                rob(tail+i.U).pc := io.pc_rn(i)
            }
        }
        tail := tail + insert_num
    }
    for(i <- 0 until 4){
        io.rob_index_rn(i) := tail + i.U
    }
    
    // wb stage
    for(i <- 0 until 4){
        when(io.inst_valid_wb(i)){
            rob(io.rob_index_wb(i)).complete := true.B
            rob(io.rob_index_wb(i)).predict_fail := io.predict_fail_wb(i)
        }
    }
    
    // cmt stage
    io.cmt_en(0) := rob(head).complete && !empty
    for(i <- 1 until 4){
        io.cmt_en(i) := io.cmt_en(i-1) && rob(head+i.U).complete && !rob(head+(i-1).U).predict_fail 
    }
    io.full := full
    val predict_fail_bit = VecInit(Seq.fill(4)(false.B))
    for(i <- 0 until 4){
        predict_fail_bit(i) := rob(head+i.U).predict_fail && io.cmt_en(i)
    }
    io.predict_fail_cmt := predict_fail_bit.asUInt.orR

    head := head + PopCount(io.cmt_en)

    for(i <- 0 until 4){
        io.rd_cmt(i) := rob(head+i.U).rd
        io.rd_valid_cmt(i) := rob(head+i.U).rd_valid
        io.prd_cmt(i) := rob(head+i.U).prd
        io.pprd_cmt(i) := rob(head+i.U).pprd
        io.pc_cmt(i) := rob(head+i.U).pc
    }
} 

object ROB extends App {
    emitVerilog(new ROB(8), Array("-td", "build/"))
}