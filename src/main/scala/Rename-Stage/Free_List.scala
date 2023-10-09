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
    val head = RegInit(VecInit(Seq.fill(4)(0.U(4.W))))
    val tail = RegInit(VecInit(Seq.fill(4)(15.U(4.W))))

    io.empty := (head(0) === tail(0)) | (head(1) === tail(1)) | (head(2) === tail(2)) | (head(3) === tail(3))

    for(i <- 0 until 4){
        when(io.predict_fail){
            head(i) := io.head_arch(i)
        }.elsewhen(!io.empty){
            head(i) := head(i) + io.rd_valid(i)
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


        // val free_list = Mem(64, UInt(6.W))
    // when(reset.asBool) {
    //     for(i <- 0 until 64){
    //         free_list(i) := i.asUInt
    //     }
    // }
    // val head = RegInit(0.U(6.W))
    // val tail = RegInit(63.U(6.W))

    // io.empty := (head === tail)

    // val rd_valid = Wire(UInt(4.W))
    // rd_valid := io.rd_valid.asUInt

    // val alloc_pool = Wire(Vec(4, UInt(6.W)))
    // for(i <- 0 until 4){
    //     alloc_pool(i) := free_list(head + i.asUInt)
    // }

    // // read 
    // head := Mux(io.predict_fail, io.head_arch, Mux(io.rename_en.asUInt.orR, head + PopCount(io.rd_valid), head))
    // val read_shift_num = Wire(Vec(4, UInt(6.W)))
    // read_shift_num(0) := 0.U
    // for(i <- 1 until 4){
    //     read_shift_num(i) := PopCount(~rd_valid(i - 1, 0))
    // }

    // for(i <- 0 until 4){
    //     io.alloc_preg(i) := alloc_pool(i.asUInt - read_shift_num(i))
    // }

    // // write
    // tail := Mux(io.commit_en.asUInt.orR, tail + PopCount(io.commit_pprd_valid), tail)

    // val commit_pprd_valid = Wire(UInt(4.W))
    // commit_pprd_valid := io.commit_pprd_valid.asUInt

    // val write_shift_num = Wire(Vec(4, UInt(6.W)))
    // write_shift_num(0) := 0.U
    // for(i <- 1 until 4){
    //     write_shift_num(i) := PopCount(~commit_pprd_valid(i - 1, 0))
    // }

    // val write_hit = Wire(Vec(4, Vec(4, Bool())))
    // for(i <- 0 until 4){
    //     for(j <- 0 until 4){
    //         write_hit(i)(j) := (j.asUInt - write_shift_num(j) === i.asUInt) & commit_pprd_valid(j)
    //     }
    //     when(write_hit(i).asUInt.orR){
    //         free_list(tail + i.asUInt) := io.commit_pprd(OHToUInt(write_hit(i)))
    //     }
    // }

}
// object Free_List extends App{
//     emitVerilog(new Free_List, Array("-td", "verilog/"))
// }