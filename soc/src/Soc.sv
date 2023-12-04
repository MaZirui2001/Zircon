module Soc(
    input       clk,
    input       rstn,
    output      [15:0] led
);

wire cpu_clk;
ip_clock   ip_clock_inst (
    .clk_in1(clk),
    .reset(rstn),
    .cpu_clk(cpu_clk)
);

wire    [31:0]  io_araddr;
wire            io_arvalid;
wire            io_arready;
wire    [7:0]   io_arlen;
wire    [2:0]   io_arsize;
wire    [1:0]   io_arburst;
wire    [31:0]  io_rdata;
wire    [1:0]   io_rresp;
wire            io_rvalid;
wire            io_rready;
wire            io_rlast;
wire    [31:0]  io_awaddr;
wire            io_awvalid;
wire            io_awready;
wire    [7:0]   io_awlen;
wire    [2:0]   io_awsize;
wire    [1:0]   io_awburst;
wire    [31:0]  io_wdata;
wire    [3:0]   io_wstrb;
wire            io_wvalid;
wire            io_wready;
wire            io_wlast;
wire    [1:0]   io_bresp;
wire            io_bvalid;
wire            io_bready;
wire    [3:0]   io_bid;
wire    [3:0]   io_rid;
wire    [3:0]   io_arid;
wire    [3:0]   io_awid;
assign led = io_araddr[31:16];

CPU  CPU_inst (
    .clock(cpu_clk),
    .reset(!rstn),
    .io_araddr(io_araddr),
    .io_arburst(io_arburst),
    .io_arid(io_arid),
    .io_arlen(io_arlen),
    .io_arready(io_arready),
    .io_arsize(io_arsize),
    .io_arvalid(io_arvalid),
    .io_awaddr(io_awaddr),
    .io_awburst(io_awburst),
    .io_awid(io_awid),
    .io_awlen(io_awlen),
    .io_awready(io_awready),
    .io_awsize(io_awsize),
    .io_awvalid(io_awvalid),
    .io_bid(io_bid),
    .io_bready(io_bready),
    .io_bresp(io_bresp),
    .io_bvalid(io_bvalid),
    .io_rdata(io_rdata),
    .io_rid(io_rid),
    .io_rlast(io_rlast),
    .io_rready(io_rready),
    .io_rresp(io_rresp),
    .io_rvalid(io_rvalid),
    .io_wdata(io_wdata),
    .io_wlast(io_wlast),
    .io_wready(io_wready),
    .io_wstrb(io_wstrb),
    .io_wvalid(io_wvalid)
    // .io_commit_en_0(io_commit_en_0),
    // .io_commit_en_1(io_commit_en_1),
    // .io_commit_en_2(io_commit_en_2),
    // .io_commit_en_3(io_commit_en_3),
    // .io_commit_rd_0(io_commit_rd_0),
    // .io_commit_rd_1(io_commit_rd_1),
    // .io_commit_rd_2(io_commit_rd_2),
    // .io_commit_rd_3(io_commit_rd_3),
    // .io_commit_prd_0(io_commit_prd_0),
    // .io_commit_prd_1(io_commit_prd_1),
    // .io_commit_prd_2(io_commit_prd_2),
    // .io_commit_prd_3(io_commit_prd_3),
    // .io_commit_rd_valid_0(io_commit_rd_valid_0),
    // .io_commit_rd_valid_1(io_commit_rd_valid_1),
    // .io_commit_rd_valid_2(io_commit_rd_valid_2),
    // .io_commit_rd_valid_3(io_commit_rd_valid_3),
    // .io_commit_rf_wdata_0(io_commit_rf_wdata_0),
    // .io_commit_rf_wdata_1(io_commit_rf_wdata_1),
    // .io_commit_rf_wdata_2(io_commit_rf_wdata_2),
    // .io_commit_rf_wdata_3(io_commit_rf_wdata_3),
    // .io_commit_csr_wdata_0(io_commit_csr_wdata_0),
    // .io_commit_csr_wdata_1(io_commit_csr_wdata_1),
    // .io_commit_csr_wdata_2(io_commit_csr_wdata_2),
    // .io_commit_csr_wdata_3(io_commit_csr_wdata_3),
    // .io_commit_csr_we_0(io_commit_csr_we_0),
    // .io_commit_csr_we_1(io_commit_csr_we_1),
    // .io_commit_csr_we_2(io_commit_csr_we_2),
    // .io_commit_csr_we_3(io_commit_csr_we_3),
    // .io_commit_csr_waddr_0(io_commit_csr_waddr_0),
    // .io_commit_csr_waddr_1(io_commit_csr_waddr_1),
    // .io_commit_csr_waddr_2(io_commit_csr_waddr_2),
    // .io_commit_csr_waddr_3(io_commit_csr_waddr_3),
    // .io_commit_pc_0(io_commit_pc_0),
    // .io_commit_pc_1(io_commit_pc_1),
    // .io_commit_pc_2(io_commit_pc_2),
    // .io_commit_pc_3(io_commit_pc_3),
    // .io_commit_is_ucread_0(io_commit_is_ucread_0),
    // .io_commit_is_ucread_1(io_commit_is_ucread_1),
    // .io_commit_is_ucread_2(io_commit_is_ucread_2),
    // .io_commit_is_ucread_3(io_commit_is_ucread_3),
    // .io_commit_is_br_0(io_commit_is_br_0),
    // .io_commit_is_br_1(io_commit_is_br_1),
    // .io_commit_is_br_2(io_commit_is_br_2),
    // .io_commit_is_br_3(io_commit_is_br_3),
    // .io_commit_br_type_0(io_commit_br_type_0),
    // .io_commit_br_type_1(io_commit_br_type_1),
    // .io_commit_br_type_2(io_commit_br_type_2),
    // .io_commit_br_type_3(io_commit_br_type_3),
    // .io_commit_predict_fail_0(io_commit_predict_fail_0),
    // .io_commit_predict_fail_1(io_commit_predict_fail_1),
    // .io_commit_predict_fail_2(io_commit_predict_fail_2),
    // .io_commit_predict_fail_3(io_commit_predict_fail_3),
    // .io_commit_stall_by_fetch_queue(io_commit_stall_by_fetch_queue),
    // .io_commit_stall_by_rename(io_commit_stall_by_rename),
    // .io_commit_stall_by_rob(io_commit_stall_by_rob),
    // .io_commit_stall_by_iq_0(io_commit_stall_by_iq_0),
    // .io_commit_stall_by_iq_1(io_commit_stall_by_iq_1),
    // .io_commit_stall_by_iq_2(io_commit_stall_by_iq_2),
    // .io_commit_stall_by_iq_3(io_commit_stall_by_iq_3),
    // .io_commit_stall_by_iq_4(io_commit_stall_by_iq_4),
    // .io_commit_stall_by_sb(io_commit_stall_by_sb),
    // .io_commit_stall_by_icache(io_commit_stall_by_icache),
    // .io_commit_icache_miss(io_commit_icache_miss),
    // .io_commit_icache_visit(io_commit_icache_visit),
    // .io_commit_stall_by_dcache(io_commit_stall_by_dcache),
    // .io_commit_dcache_miss(io_commit_dcache_miss),
    // .io_commit_dcache_visit(io_commit_dcache_visit),
    // .io_commit_iq_issue_0(io_commit_iq_issue_0),
    // .io_commit_iq_issue_1(io_commit_iq_issue_1),
    // .io_commit_iq_issue_2(io_commit_iq_issue_2),
    // .io_commit_iq_issue_3(io_commit_iq_issue_3),
    // .io_commit_iq_issue_4(io_commit_iq_issue_4)
);

main_memory main_mem(
    .s_aclk         (cpu_clk ),
    .s_aresetn      (rstn ),
    .s_axi_araddr   (io_araddr ),
    .s_axi_arburst  (io_arburst ),
    .s_axi_arid     (io_arid),
    .s_axi_arlen    (io_arlen ),
    .s_axi_arready  (io_arready ),
    .s_axi_arsize   (io_arsize ),
    .s_axi_arvalid  (io_arvalid ),
    .s_axi_awaddr   (io_awaddr ),
    .s_axi_awburst  (io_awburst ),
    .s_axi_awid     (io_awid),
    .s_axi_awlen    (io_awlen ),
    .s_axi_awready  (io_awready ),
    .s_axi_awsize   (io_awsize ),
    .s_axi_awvalid  (io_awvalid ),
    .s_axi_bid      (io_bid),
    .s_axi_bready   (io_bready ),
    .s_axi_bresp    (io_bresp ),
    .s_axi_bvalid   (io_bvalid ),
    .s_axi_rdata    (io_rdata ),
    .s_axi_rid      (io_rid),
    .s_axi_rlast    (io_rlast ),
    .s_axi_rready   (io_rready ),
    .s_axi_rresp    (io_rresp ),
    .s_axi_rvalid   (io_rvalid ),
    .s_axi_wdata    (io_wdata ),
    .s_axi_wlast    (io_wlast ),
    .s_axi_wready   (io_wready ),
    .s_axi_wstrb    (io_wstrb ),
    .s_axi_wvalid   (io_wvalid )
);
    
    
endmodule