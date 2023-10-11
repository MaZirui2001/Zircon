import chisel3._
import chisel3.util._
// LUT: 435 FF: 416
class Free_List extends Module{
    val io = IO(new Bundle{
        val rd_valid            = Input(Vec(4, Bool()))
        val rename_en           = Input(Vec(4, Bool()))
        val alloc_preg          = Output(Vec(4, UInt(6.W)))
        val empty               = Output(Bool())

        val commit_en           = Input(Vec(4, Bool()))
        val commit_pprd_valid   = Input(Vec(4, Bool()))
        val commit_pprd         = Input(Vec(4, UInt(6.W)))

        val predict_fail        = Input(Bool())
        val head_arch           = Input(Vec(4, UInt(4.W)))
    })

    val free_list = Reg(Vec(4, Vec(16, UInt(6.W))))
    when(reset.asBool) {
        for(j <- 0 until 16){
            for(i <- 0 until 4){
                free_list(i)(j) := (j * 4 + i).asUInt
            }
        }
    }
    val head = RegInit(VecInit(1.U(4.W), 0.U(4.W), 0.U(4.W), 0.U(4.W)))
    val tail = RegInit(VecInit(Seq.fill(4)(15.U(4.W))))

    io.empty := (head(0) === tail(0)) | (head(1) === tail(1)) | (head(2) === tail(2)) | (head(3) === tail(3))

    for(i <- 0 until 4){
        when(io.predict_fail){
            head(i) := io.head_arch(i)
        }.elsewhen(!io.empty){
            head(i) := head(i) + Mux(i.U =/= 0.U, io.rd_valid(i), Mux(head(i) === 15.U, Mux(io.rd_valid(i), 2.U, 0.U), io.rd_valid(i)))
        }
        when(io.commit_en(i)){
            tail(i) := tail(i) + io.commit_pprd_valid(i)
            when(io.commit_pprd_valid(i)){
                free_list(i)(tail(i)) := io.commit_pprd(i)
            }
        }
    }
    for(i <- 0 until 4){
        io.alloc_preg(i) := free_list(i)(head(i))
    }


}
// object Free_List extends App{
//     emitVerilog(new Free_List, Array("-td", "verilog/"))
// }