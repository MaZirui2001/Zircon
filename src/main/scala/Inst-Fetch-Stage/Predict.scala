import chisel3._
import chisel3.util._

object BTB_Pack {
    class btb_t extends Bundle{
        val valid = Bool()
        val target = UInt(32.W)
        val tag = UInt(20.W)
    }
}
