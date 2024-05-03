import chisel3._
import chisel3.util._

class xilinx_simple_dual_port_1_clock_ram_no_change(RAM_WIDTH: Int, RAM_DEPTH: Int) extends BlackBox(Map( "RAM_WIDTH" -> RAM_WIDTH,
                                                                                                "RAM_DEPTH" -> RAM_DEPTH)) with HasBlackBoxInline {
    val io = IO(new Bundle {
        val addra = Input(UInt(log2Ceil(RAM_DEPTH).W))
        val addrb = Input(UInt(log2Ceil(RAM_DEPTH).W))
        val dina = Input(UInt(RAM_WIDTH.W))
        val clka = Input(Clock())
        val wea = Input(Bool())
        val doutb = Output(UInt(RAM_WIDTH.W))
    })
    val module = "xilinx_simple_dual_port_1_clock_ram_no_change.sv"
    setInline(module,
"""
|    
|//  Xilinx Simple Dual Port Single Clock RAM
|//  This code implements a parameterizable SDP single clock memory.
|//  If a reset or enable is not necessary, it may be tied off or removed from the code.
|
| module xilinx_simple_dual_port_1_clock_ram_no_change #(
|     parameter RAM_WIDTH = 64,                       // Specify RAM data width
|     parameter RAM_DEPTH = 512                      // Specify RAM depth (number of entries)
|   ) (
|     input [$clog2(RAM_DEPTH)-1:0] addra, // Write address bus, width determined from RAM_DEPTH
|     input [$clog2(RAM_DEPTH)-1:0] addrb, // Read address bus, width determined from RAM_DEPTH
|     input [RAM_WIDTH-1:0] dina,          // RAM input data
|     input clka,                          // Clock
|     input wea,                           // Write enable
|     output [RAM_WIDTH-1:0] doutb         // RAM output data
|   );
|   (*ram_style="block"*)
|     reg [RAM_WIDTH-1:0] BRAM [RAM_DEPTH-1:0];
|     reg [$clog2(RAM_DEPTH)-1:0] addr_r;
|     reg [RAM_WIDTH-1:0] ram_data = {RAM_WIDTH{1'b0}};
|         
|   generate
|       integer ram_index;
|       initial
|         for (ram_index = 0; ram_index < RAM_DEPTH; ram_index = ram_index + 1)
|           BRAM[ram_index] = {RAM_WIDTH{1'b0}};
|   endgenerate
|
|   always @(posedge clka) begin
|       if (wea) BRAM[addra] <= dina;
|       else ram_data <= BRAM[addrb];
|   end
|
|   assign doutb = ram_data;
|
|   endmodule
""".stripMargin)
}            

    