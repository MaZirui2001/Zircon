module CRat(
    input               clock,
    input               reset,

    input  logic [4:0]  rj_0,
    input  logic [4:0]  rj_1,
    input  logic [4:0]  rj_2,
    input  logic [4:0]  rj_3,
    input  logic [4:0]  rk_0,
    input  logic [4:0]  rk_1,
    input  logic [4:0]  rk_2,
    input  logic [4:0]  rk_3,
    input  logic [4:0]  rd_0,
    input  logic [4:0]  rd_1,
    input  logic [4:0]  rd_2,
    input  logic [4:0]  rd_3,


    input  logic        rd_valid_0,
    input  logic        rd_valid_1,
    input  logic        rd_valid_2,
    input  logic        rd_valid_3,

    output logic [5:0]  prj_0,
    output logic [5:0]  prj_1,
    output logic [5:0]  prj_2,
    output logic [5:0]  prj_3,
    output logic [5:0]  prk_0,
    output logic [5:0]  prk_1,
    output logic [5:0]  prk_2,
    output logic [5:0]  prk_3,
    output logic [5:0]  pprd_0,
    output logic [5:0]  pprd_1,
    output logic [5:0]  pprd_2,
    output logic [5:0]  pprd_3,

    input  logic [5:0]  alloc_preg_0,
    input  logic [5:0]  alloc_preg_1,
    input  logic [5:0]  alloc_preg_2,
    input  logic [5:0]  alloc_preg_3,

    input  logic [63:0]      arch_rat,
    input  logic             predict_fail

);
    logic [4:0] rj [4];
    logic [4:0] rk [4];
    logic [4:0] rd [4];
    logic       rd_valid [4];
    logic [5:0] prj [4];
    logic [5:0] prk [4];
    logic [5:0] pprd [4];
    
    logic [5:0] alloc_preg [4];

    assign rj[0] = rj_0;
    assign rj[1] = rj_1;
    assign rj[2] = rj_2;
    assign rj[3] = rj_3;
    assign rk[0] = rk_0;
    assign rk[1] = rk_1;
    assign rk[2] = rk_2;
    assign rk[3] = rk_3;
    assign rd[0] = rd_0;
    assign rd[1] = rd_1;
    assign rd[2] = rd_2;
    assign rd[3] = rd_3;
    assign rd_valid[0] = rd_valid_0;
    assign rd_valid[1] = rd_valid_1;
    assign rd_valid[2] = rd_valid_2;
    assign rd_valid[3] = rd_valid_3;
    assign alloc_preg[0] = alloc_preg_0;
    assign alloc_preg[1] = alloc_preg_1;
    assign alloc_preg[2] = alloc_preg_2;
    assign alloc_preg[3] = alloc_preg_3;

    assign prj_0 = prj[0];
    assign prj_1 = prj[1];
    assign prj_2 = prj[2];
    assign prj_3 = prj[3];
    assign prk_0 = prk[0];
    assign prk_1 = prk[1];
    assign prk_2 = prk[2];
    assign prk_3 = prk[3];
    assign pprd_0 = pprd[0];
    assign pprd_1 = pprd[1];
    assign pprd_2 = pprd[2];
    assign pprd_3 = pprd[3];

    typedef struct packed {
        logic [4:0]               src;
        logic                       valid;
    } RatEntry_t;
    
    // cRat
    RatEntry_t cRAT[64];

    // write cRAT
    always_ff @(posedge clock) begin
        if(reset) begin
            for(int i = 0; i < 64; i++) begin
                cRAT[i].src   <= 0;
                cRAT[i].valid <= 0;
            end
        end
        else if(predict_fail) begin
            for(int i = 0; i < 64; i++) begin
                cRAT[i].valid <= arch_rat[i];
            end
        end
        else begin
            // write cRAT and clear old by new instruction
            for(int i = 0; i < 4; i++) begin
                if(rd_valid[i]) begin
                    cRAT[alloc_preg[i]].src      <= rd[i];
                    cRAT[alloc_preg[i]].valid    <= 1;
                    cRAT[pprd[i]].valid <= 0;
                end
            end
        end
    end
    
    // read src
    logic [5:0]      rj_hit         [4][64];
    logic [5:0]      rk_hit         [4][64];
    logic [63:0]     rj_hit_trav    [4][6];
    logic [63:0]     rk_hit_trav    [4][6];
    for(genvar i = 0; i < 4; i++) begin
        for(genvar k = 0; k < 64; k++) begin
            assign rj_hit[i][k] = (cRAT[k].src == rj[i]) && cRAT[k].valid ? k[5:0] : 0;
            assign rk_hit[i][k] = (cRAT[k].src == rk[i]) && cRAT[k].valid ? k[5:0] : 0;
            for(genvar l = 0; l < 6; l++) begin
                assign rj_hit_trav[i][l][k] = rj_hit[i][k][l];
                assign rk_hit_trav[i][l][k] = rk_hit[i][k][l];
            end
        end
        for(genvar k = 0; k < 6; k++) begin
            assign prj[i][k] = |rj_hit_trav[i][k];
            assign prk[i][k] = |rk_hit_trav[i][k];
        end
    end


    // read dst
    logic [5:0]  rd_hit             [4][64];
    logic [63:0]     rd_hit_trav        [4][6];
    for(genvar i = 0; i < 4; i++) begin
        for(genvar j = 0; j < 64; j++) begin
            assign rd_hit[i][j] = (cRAT[j].src == rd[i]) && cRAT[j].valid ? j[5:0] : 0;
            for(genvar k = 0; k < 6; k++) begin
                assign rd_hit_trav[i][k][j] = rd_hit[i][j][k];
            end
        end
        for(genvar j = 0; j < 6; j++) begin
            assign pprd[i][j] = |rd_hit_trav[i][j];
        end
    end
endmodule