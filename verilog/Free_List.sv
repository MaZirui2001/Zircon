module Free_List(
  input        clock,
               reset,
               io_rd_valid_0,
               io_rd_valid_1,
               io_rd_valid_2,
               io_rd_valid_3,
               io_commit_en_0,
               io_commit_en_1,
               io_commit_en_2,
               io_commit_en_3,
               io_commit_pprd_valid_0,
               io_commit_pprd_valid_1,
               io_commit_pprd_valid_2,
               io_commit_pprd_valid_3,
  input  [5:0] io_commit_pprd_0,
               io_commit_pprd_1,
               io_commit_pprd_2,
               io_commit_pprd_3,
  input        io_predict_fail,
  input  [5:0] io_head_arch_0,
               io_head_arch_1,
               io_head_arch_2,
               io_head_arch_3,
  output [5:0] io_alloc_preg_0,
               io_alloc_preg_1,
               io_alloc_preg_2,
               io_alloc_preg_3,
  output       io_empty
);

  reg  [5:0] casez_tmp;
  reg  [5:0] casez_tmp_0;
  reg  [5:0] casez_tmp_1;
  reg  [5:0] casez_tmp_2;
  reg  [5:0] free_list_0_0;
  reg  [5:0] free_list_0_1;
  reg  [5:0] free_list_0_2;
  reg  [5:0] free_list_0_3;
  reg  [5:0] free_list_0_4;
  reg  [5:0] free_list_0_5;
  reg  [5:0] free_list_0_6;
  reg  [5:0] free_list_0_7;
  reg  [5:0] free_list_0_8;
  reg  [5:0] free_list_0_9;
  reg  [5:0] free_list_0_10;
  reg  [5:0] free_list_0_11;
  reg  [5:0] free_list_0_12;
  reg  [5:0] free_list_0_13;
  reg  [5:0] free_list_0_14;
  reg  [5:0] free_list_0_15;
  reg  [5:0] free_list_1_0;
  reg  [5:0] free_list_1_1;
  reg  [5:0] free_list_1_2;
  reg  [5:0] free_list_1_3;
  reg  [5:0] free_list_1_4;
  reg  [5:0] free_list_1_5;
  reg  [5:0] free_list_1_6;
  reg  [5:0] free_list_1_7;
  reg  [5:0] free_list_1_8;
  reg  [5:0] free_list_1_9;
  reg  [5:0] free_list_1_10;
  reg  [5:0] free_list_1_11;
  reg  [5:0] free_list_1_12;
  reg  [5:0] free_list_1_13;
  reg  [5:0] free_list_1_14;
  reg  [5:0] free_list_1_15;
  reg  [5:0] free_list_2_0;
  reg  [5:0] free_list_2_1;
  reg  [5:0] free_list_2_2;
  reg  [5:0] free_list_2_3;
  reg  [5:0] free_list_2_4;
  reg  [5:0] free_list_2_5;
  reg  [5:0] free_list_2_6;
  reg  [5:0] free_list_2_7;
  reg  [5:0] free_list_2_8;
  reg  [5:0] free_list_2_9;
  reg  [5:0] free_list_2_10;
  reg  [5:0] free_list_2_11;
  reg  [5:0] free_list_2_12;
  reg  [5:0] free_list_2_13;
  reg  [5:0] free_list_2_14;
  reg  [5:0] free_list_2_15;
  reg  [5:0] free_list_3_0;
  reg  [5:0] free_list_3_1;
  reg  [5:0] free_list_3_2;
  reg  [5:0] free_list_3_3;
  reg  [5:0] free_list_3_4;
  reg  [5:0] free_list_3_5;
  reg  [5:0] free_list_3_6;
  reg  [5:0] free_list_3_7;
  reg  [5:0] free_list_3_8;
  reg  [5:0] free_list_3_9;
  reg  [5:0] free_list_3_10;
  reg  [5:0] free_list_3_11;
  reg  [5:0] free_list_3_12;
  reg  [5:0] free_list_3_13;
  reg  [5:0] free_list_3_14;
  reg  [5:0] free_list_3_15;
  reg  [3:0] head_0;
  reg  [3:0] head_1;
  reg  [3:0] head_2;
  reg  [3:0] head_3;
  reg  [3:0] tail_0;
  reg  [3:0] tail_1;
  reg  [3:0] tail_2;
  reg  [3:0] tail_3;
  wire       _io_empty_output =
    head_0 == tail_0 | head_1 == tail_1 | head_2 == tail_2 | head_3 == tail_3;
  always_comb begin
    casez (head_0)
      4'b0000:
        casez_tmp = free_list_0_0;
      4'b0001:
        casez_tmp = free_list_0_1;
      4'b0010:
        casez_tmp = free_list_0_2;
      4'b0011:
        casez_tmp = free_list_0_3;
      4'b0100:
        casez_tmp = free_list_0_4;
      4'b0101:
        casez_tmp = free_list_0_5;
      4'b0110:
        casez_tmp = free_list_0_6;
      4'b0111:
        casez_tmp = free_list_0_7;
      4'b1000:
        casez_tmp = free_list_0_8;
      4'b1001:
        casez_tmp = free_list_0_9;
      4'b1010:
        casez_tmp = free_list_0_10;
      4'b1011:
        casez_tmp = free_list_0_11;
      4'b1100:
        casez_tmp = free_list_0_12;
      4'b1101:
        casez_tmp = free_list_0_13;
      4'b1110:
        casez_tmp = free_list_0_14;
      default:
        casez_tmp = free_list_0_15;
    endcase
  end // always_comb
  always_comb begin
    casez (head_1)
      4'b0000:
        casez_tmp_0 = free_list_1_0;
      4'b0001:
        casez_tmp_0 = free_list_1_1;
      4'b0010:
        casez_tmp_0 = free_list_1_2;
      4'b0011:
        casez_tmp_0 = free_list_1_3;
      4'b0100:
        casez_tmp_0 = free_list_1_4;
      4'b0101:
        casez_tmp_0 = free_list_1_5;
      4'b0110:
        casez_tmp_0 = free_list_1_6;
      4'b0111:
        casez_tmp_0 = free_list_1_7;
      4'b1000:
        casez_tmp_0 = free_list_1_8;
      4'b1001:
        casez_tmp_0 = free_list_1_9;
      4'b1010:
        casez_tmp_0 = free_list_1_10;
      4'b1011:
        casez_tmp_0 = free_list_1_11;
      4'b1100:
        casez_tmp_0 = free_list_1_12;
      4'b1101:
        casez_tmp_0 = free_list_1_13;
      4'b1110:
        casez_tmp_0 = free_list_1_14;
      default:
        casez_tmp_0 = free_list_1_15;
    endcase
  end // always_comb
  always_comb begin
    casez (head_2)
      4'b0000:
        casez_tmp_1 = free_list_2_0;
      4'b0001:
        casez_tmp_1 = free_list_2_1;
      4'b0010:
        casez_tmp_1 = free_list_2_2;
      4'b0011:
        casez_tmp_1 = free_list_2_3;
      4'b0100:
        casez_tmp_1 = free_list_2_4;
      4'b0101:
        casez_tmp_1 = free_list_2_5;
      4'b0110:
        casez_tmp_1 = free_list_2_6;
      4'b0111:
        casez_tmp_1 = free_list_2_7;
      4'b1000:
        casez_tmp_1 = free_list_2_8;
      4'b1001:
        casez_tmp_1 = free_list_2_9;
      4'b1010:
        casez_tmp_1 = free_list_2_10;
      4'b1011:
        casez_tmp_1 = free_list_2_11;
      4'b1100:
        casez_tmp_1 = free_list_2_12;
      4'b1101:
        casez_tmp_1 = free_list_2_13;
      4'b1110:
        casez_tmp_1 = free_list_2_14;
      default:
        casez_tmp_1 = free_list_2_15;
    endcase
  end // always_comb
  always_comb begin
    casez (head_3)
      4'b0000:
        casez_tmp_2 = free_list_3_0;
      4'b0001:
        casez_tmp_2 = free_list_3_1;
      4'b0010:
        casez_tmp_2 = free_list_3_2;
      4'b0011:
        casez_tmp_2 = free_list_3_3;
      4'b0100:
        casez_tmp_2 = free_list_3_4;
      4'b0101:
        casez_tmp_2 = free_list_3_5;
      4'b0110:
        casez_tmp_2 = free_list_3_6;
      4'b0111:
        casez_tmp_2 = free_list_3_7;
      4'b1000:
        casez_tmp_2 = free_list_3_8;
      4'b1001:
        casez_tmp_2 = free_list_3_9;
      4'b1010:
        casez_tmp_2 = free_list_3_10;
      4'b1011:
        casez_tmp_2 = free_list_3_11;
      4'b1100:
        casez_tmp_2 = free_list_3_12;
      4'b1101:
        casez_tmp_2 = free_list_3_13;
      4'b1110:
        casez_tmp_2 = free_list_3_14;
      default:
        casez_tmp_2 = free_list_3_15;
    endcase
  end // always_comb
  always @(posedge clock) begin
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h0)
      free_list_0_0 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_0 <= 6'h0;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h1)
      free_list_0_1 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_1 <= 6'h4;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h2)
      free_list_0_2 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_2 <= 6'h8;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h3)
      free_list_0_3 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_3 <= 6'hC;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h4)
      free_list_0_4 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_4 <= 6'h10;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h5)
      free_list_0_5 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_5 <= 6'h14;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h6)
      free_list_0_6 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_6 <= 6'h18;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h7)
      free_list_0_7 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_7 <= 6'h1C;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h8)
      free_list_0_8 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_8 <= 6'h20;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'h9)
      free_list_0_9 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_9 <= 6'h24;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'hA)
      free_list_0_10 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_10 <= 6'h28;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'hB)
      free_list_0_11 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_11 <= 6'h2C;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'hC)
      free_list_0_12 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_12 <= 6'h30;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'hD)
      free_list_0_13 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_13 <= 6'h34;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & tail_0 == 4'hE)
      free_list_0_14 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_14 <= 6'h38;
    if (io_commit_en_0 & io_commit_pprd_valid_0 & (&tail_0))
      free_list_0_15 <= io_commit_pprd_0;
    else if (reset)
      free_list_0_15 <= 6'h3C;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h0)
      free_list_1_0 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_0 <= 6'h1;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h1)
      free_list_1_1 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_1 <= 6'h5;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h2)
      free_list_1_2 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_2 <= 6'h9;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h3)
      free_list_1_3 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_3 <= 6'hD;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h4)
      free_list_1_4 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_4 <= 6'h11;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h5)
      free_list_1_5 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_5 <= 6'h15;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h6)
      free_list_1_6 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_6 <= 6'h19;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h7)
      free_list_1_7 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_7 <= 6'h1D;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h8)
      free_list_1_8 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_8 <= 6'h21;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'h9)
      free_list_1_9 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_9 <= 6'h25;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'hA)
      free_list_1_10 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_10 <= 6'h29;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'hB)
      free_list_1_11 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_11 <= 6'h2D;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'hC)
      free_list_1_12 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_12 <= 6'h31;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'hD)
      free_list_1_13 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_13 <= 6'h35;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & tail_1 == 4'hE)
      free_list_1_14 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_14 <= 6'h39;
    if (io_commit_en_1 & io_commit_pprd_valid_1 & (&tail_1))
      free_list_1_15 <= io_commit_pprd_1;
    else if (reset)
      free_list_1_15 <= 6'h3D;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h0)
      free_list_2_0 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_0 <= 6'h2;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h1)
      free_list_2_1 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_1 <= 6'h6;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h2)
      free_list_2_2 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_2 <= 6'hA;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h3)
      free_list_2_3 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_3 <= 6'hE;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h4)
      free_list_2_4 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_4 <= 6'h12;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h5)
      free_list_2_5 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_5 <= 6'h16;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h6)
      free_list_2_6 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_6 <= 6'h1A;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h7)
      free_list_2_7 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_7 <= 6'h1E;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h8)
      free_list_2_8 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_8 <= 6'h22;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'h9)
      free_list_2_9 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_9 <= 6'h26;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'hA)
      free_list_2_10 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_10 <= 6'h2A;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'hB)
      free_list_2_11 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_11 <= 6'h2E;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'hC)
      free_list_2_12 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_12 <= 6'h32;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'hD)
      free_list_2_13 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_13 <= 6'h36;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & tail_2 == 4'hE)
      free_list_2_14 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_14 <= 6'h3A;
    if (io_commit_en_2 & io_commit_pprd_valid_2 & (&tail_2))
      free_list_2_15 <= io_commit_pprd_2;
    else if (reset)
      free_list_2_15 <= 6'h3E;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h0)
      free_list_3_0 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_0 <= 6'h3;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h1)
      free_list_3_1 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_1 <= 6'h7;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h2)
      free_list_3_2 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_2 <= 6'hB;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h3)
      free_list_3_3 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_3 <= 6'hF;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h4)
      free_list_3_4 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_4 <= 6'h13;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h5)
      free_list_3_5 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_5 <= 6'h17;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h6)
      free_list_3_6 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_6 <= 6'h1B;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h7)
      free_list_3_7 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_7 <= 6'h1F;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h8)
      free_list_3_8 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_8 <= 6'h23;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'h9)
      free_list_3_9 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_9 <= 6'h27;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'hA)
      free_list_3_10 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_10 <= 6'h2B;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'hB)
      free_list_3_11 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_11 <= 6'h2F;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'hC)
      free_list_3_12 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_12 <= 6'h33;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'hD)
      free_list_3_13 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_13 <= 6'h37;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & tail_3 == 4'hE)
      free_list_3_14 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_14 <= 6'h3B;
    if (io_commit_en_3 & io_commit_pprd_valid_3 & (&tail_3))
      free_list_3_15 <= io_commit_pprd_3;
    else if (reset)
      free_list_3_15 <= 6'h3F;
    if (reset) begin
      head_0 <= 4'h0;
      head_1 <= 4'h0;
      head_2 <= 4'h0;
      head_3 <= 4'h0;
      tail_0 <= 4'hF;
      tail_1 <= 4'hF;
      tail_2 <= 4'hF;
      tail_3 <= 4'hF;
    end
    else begin
      if (io_predict_fail) begin
        head_0 <= io_head_arch_0[3:0];
        head_1 <= io_head_arch_1[3:0];
        head_2 <= io_head_arch_2[3:0];
        head_3 <= io_head_arch_3[3:0];
      end
      else if (~_io_empty_output) begin
        head_0 <= head_0 + {3'h0, io_rd_valid_0};
        head_1 <= head_1 + {3'h0, io_rd_valid_1};
        head_2 <= head_2 + {3'h0, io_rd_valid_2};
        head_3 <= head_3 + {3'h0, io_rd_valid_3};
      end
      if (io_commit_en_0)
        tail_0 <= tail_0 + {3'h0, io_commit_pprd_valid_0};
      if (io_commit_en_1)
        tail_1 <= tail_1 + {3'h0, io_commit_pprd_valid_1};
      if (io_commit_en_2)
        tail_2 <= tail_2 + {3'h0, io_commit_pprd_valid_2};
      if (io_commit_en_3)
        tail_3 <= tail_3 + {3'h0, io_commit_pprd_valid_3};
    end
  end // always @(posedge)
  assign io_alloc_preg_0 = casez_tmp;
  assign io_alloc_preg_1 = casez_tmp_0;
  assign io_alloc_preg_2 = casez_tmp_1;
  assign io_alloc_preg_3 = casez_tmp_2;
  assign io_empty = _io_empty_output;
endmodule
