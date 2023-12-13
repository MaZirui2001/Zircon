import chisel3._
import chisel3.util._

class xilinx_single_port_ram_read_first(RAM_WIDTH: Int, RAM_DEPTH: Int) extends BlackBox(Map( "RAM_WIDTH" -> RAM_WIDTH,
                                                                                                "RAM_DEPTH" -> RAM_DEPTH)) with HasBlackBoxInline {
    val io = IO(new Bundle {
        val addra = Input(UInt(log2Ceil(RAM_DEPTH).W))
        val dina = Input(UInt(RAM_WIDTH.W))
        val clka = Input(Clock())
        val wea = Input(Bool())
        val douta = Output(UInt(RAM_WIDTH.W))
    })
    val module = "xilinx_single_port_ram_read_first.sv"
    setInline(module,
        """
| 
| module xilinx_single_port_ram_read_first #(
|   parameter RAM_WIDTH = 18,                       // Specify RAM data width
|   parameter RAM_DEPTH = 1024                     // Specify RAM depth (number of entries)
| ) (
|   input [$clog2(RAM_DEPTH)-1:0] addra,  // Address bus, width determined from RAM_DEPTH
|   input [RAM_WIDTH-1:0] dina,           // RAM input data
|   input clka,                           // Clock
|   input wea,                            // Write enable
|   output [RAM_WIDTH-1:0] douta          // RAM output data
| );
| (*ram_style="block"*)
|   reg [RAM_WIDTH-1:0] BRAM [RAM_DEPTH-1:0];
|   reg [$clog2(RAM_DEPTH)-1:0] addr_r;
|   reg [RAM_WIDTH-1:0] ram_data = {RAM_WIDTH{1'b0}};

|   // The following code either initializes the memory values to a specified file or to all zeros to match hardware
|   generate
|       integer ram_index;
|       initial
|         for (ram_index = 0; ram_index < RAM_DEPTH; ram_index = ram_index + 1)
|           BRAM[ram_index] = {RAM_WIDTH{1'b0}};
|   endgenerate
| 
|   always @(posedge clka) begin
|       addr_r <= addra;
|       if (wea) BRAM[addra] <= dina;
|   end
| 
| 
|    // The following is a 1 clock cycle read latency at the cost of a longer clock-to-out timing
|    assign douta = BRAM[addr_r];
| 
| endmodule	
        """.stripMargin)
}


