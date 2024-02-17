
package chisel3.util

import chisel3._

object FullyAssociativeSearch {
  def apply(in: Seq[Bool], item: Vec[Bundle]): (UInt, Bundle) = apply(Cat(in.reverse), item, in.size)
  def apply(in: Vec[Bool], item: Vec[Bundle]): (UInt, Bundle) = apply(in.asUInt, item, in.size)
  def apply(in: Bits, item: Vec[Bundle]):      (UInt, Bundle) = apply(in, item, in.getWidth)

  def apply(in: Bits, item: Vec[Bundle], width: Int): (UInt, Bundle) = {
    if (width <= 2) {
      (Log2(in, width), Mux(in(0), item(0), item(1)))
    } else {
      val mid = 1 << (log2Ceil(width) - 1)
      val hi = in(width - 1, mid)
      val lo = in(mid - 1, 0)
      val item_lo = VecInit(item.take(mid))
      val item_hi = VecInit(item.drop(mid))
      val hi_orR = hi.orR
      val (index, result) = apply(hi | lo, Mux(hi_orR, item_hi, item_lo), mid)
      (Cat(hi_orR, index), result)
    }
  }
}
