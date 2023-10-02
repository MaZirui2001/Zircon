module Physical_Regfile(
  input         clock,
                reset,
  input  [5:0]  io_prj_1,
                io_prk_1,
                io_prj_2,
                io_prk_2,
                io_prj_3,
                io_prk_3,
                io_prj_4,
                io_prk_4,
                io_prd_1,
                io_prd_2,
                io_prd_3,
                io_prd_4,
  input  [31:0] io_wdata1,
                io_wdata2,
                io_wdata3,
                io_wdata4,
  input         io_rf_we1,
                io_rf_we2,
                io_rf_we3,
                io_rf_we4,
  output [31:0] io_rj_data_1,
                io_rk_data_1,
                io_rj_data_2,
                io_rk_data_2,
                io_rj_data_3,
                io_rk_data_3,
                io_rj_data_4,
                io_rk_data_4
);

  reg [31:0] casez_tmp;
  reg [31:0] casez_tmp_0;
  reg [31:0] casez_tmp_1;
  reg [31:0] casez_tmp_2;
  reg [31:0] casez_tmp_3;
  reg [31:0] casez_tmp_4;
  reg [31:0] casez_tmp_5;
  reg [31:0] casez_tmp_6;
  reg [31:0] rf_0;
  reg [31:0] rf_1;
  reg [31:0] rf_2;
  reg [31:0] rf_3;
  reg [31:0] rf_4;
  reg [31:0] rf_5;
  reg [31:0] rf_6;
  reg [31:0] rf_7;
  reg [31:0] rf_8;
  reg [31:0] rf_9;
  reg [31:0] rf_10;
  reg [31:0] rf_11;
  reg [31:0] rf_12;
  reg [31:0] rf_13;
  reg [31:0] rf_14;
  reg [31:0] rf_15;
  reg [31:0] rf_16;
  reg [31:0] rf_17;
  reg [31:0] rf_18;
  reg [31:0] rf_19;
  reg [31:0] rf_20;
  reg [31:0] rf_21;
  reg [31:0] rf_22;
  reg [31:0] rf_23;
  reg [31:0] rf_24;
  reg [31:0] rf_25;
  reg [31:0] rf_26;
  reg [31:0] rf_27;
  reg [31:0] rf_28;
  reg [31:0] rf_29;
  reg [31:0] rf_30;
  reg [31:0] rf_31;
  reg [31:0] rf_32;
  reg [31:0] rf_33;
  reg [31:0] rf_34;
  reg [31:0] rf_35;
  reg [31:0] rf_36;
  reg [31:0] rf_37;
  reg [31:0] rf_38;
  reg [31:0] rf_39;
  reg [31:0] rf_40;
  reg [31:0] rf_41;
  reg [31:0] rf_42;
  reg [31:0] rf_43;
  reg [31:0] rf_44;
  reg [31:0] rf_45;
  reg [31:0] rf_46;
  reg [31:0] rf_47;
  reg [31:0] rf_48;
  reg [31:0] rf_49;
  reg [31:0] rf_50;
  reg [31:0] rf_51;
  reg [31:0] rf_52;
  reg [31:0] rf_53;
  reg [31:0] rf_54;
  reg [31:0] rf_55;
  reg [31:0] rf_56;
  reg [31:0] rf_57;
  reg [31:0] rf_58;
  reg [31:0] rf_59;
  reg [31:0] rf_60;
  reg [31:0] rf_61;
  reg [31:0] rf_62;
  reg [31:0] rf_63;
  always_comb begin
    casez (io_prj_1)
      6'b000000:
        casez_tmp = rf_0;
      6'b000001:
        casez_tmp = rf_1;
      6'b000010:
        casez_tmp = rf_2;
      6'b000011:
        casez_tmp = rf_3;
      6'b000100:
        casez_tmp = rf_4;
      6'b000101:
        casez_tmp = rf_5;
      6'b000110:
        casez_tmp = rf_6;
      6'b000111:
        casez_tmp = rf_7;
      6'b001000:
        casez_tmp = rf_8;
      6'b001001:
        casez_tmp = rf_9;
      6'b001010:
        casez_tmp = rf_10;
      6'b001011:
        casez_tmp = rf_11;
      6'b001100:
        casez_tmp = rf_12;
      6'b001101:
        casez_tmp = rf_13;
      6'b001110:
        casez_tmp = rf_14;
      6'b001111:
        casez_tmp = rf_15;
      6'b010000:
        casez_tmp = rf_16;
      6'b010001:
        casez_tmp = rf_17;
      6'b010010:
        casez_tmp = rf_18;
      6'b010011:
        casez_tmp = rf_19;
      6'b010100:
        casez_tmp = rf_20;
      6'b010101:
        casez_tmp = rf_21;
      6'b010110:
        casez_tmp = rf_22;
      6'b010111:
        casez_tmp = rf_23;
      6'b011000:
        casez_tmp = rf_24;
      6'b011001:
        casez_tmp = rf_25;
      6'b011010:
        casez_tmp = rf_26;
      6'b011011:
        casez_tmp = rf_27;
      6'b011100:
        casez_tmp = rf_28;
      6'b011101:
        casez_tmp = rf_29;
      6'b011110:
        casez_tmp = rf_30;
      6'b011111:
        casez_tmp = rf_31;
      6'b100000:
        casez_tmp = rf_32;
      6'b100001:
        casez_tmp = rf_33;
      6'b100010:
        casez_tmp = rf_34;
      6'b100011:
        casez_tmp = rf_35;
      6'b100100:
        casez_tmp = rf_36;
      6'b100101:
        casez_tmp = rf_37;
      6'b100110:
        casez_tmp = rf_38;
      6'b100111:
        casez_tmp = rf_39;
      6'b101000:
        casez_tmp = rf_40;
      6'b101001:
        casez_tmp = rf_41;
      6'b101010:
        casez_tmp = rf_42;
      6'b101011:
        casez_tmp = rf_43;
      6'b101100:
        casez_tmp = rf_44;
      6'b101101:
        casez_tmp = rf_45;
      6'b101110:
        casez_tmp = rf_46;
      6'b101111:
        casez_tmp = rf_47;
      6'b110000:
        casez_tmp = rf_48;
      6'b110001:
        casez_tmp = rf_49;
      6'b110010:
        casez_tmp = rf_50;
      6'b110011:
        casez_tmp = rf_51;
      6'b110100:
        casez_tmp = rf_52;
      6'b110101:
        casez_tmp = rf_53;
      6'b110110:
        casez_tmp = rf_54;
      6'b110111:
        casez_tmp = rf_55;
      6'b111000:
        casez_tmp = rf_56;
      6'b111001:
        casez_tmp = rf_57;
      6'b111010:
        casez_tmp = rf_58;
      6'b111011:
        casez_tmp = rf_59;
      6'b111100:
        casez_tmp = rf_60;
      6'b111101:
        casez_tmp = rf_61;
      6'b111110:
        casez_tmp = rf_62;
      default:
        casez_tmp = rf_63;
    endcase
  end // always_comb
  always_comb begin
    casez (io_prk_1)
      6'b000000:
        casez_tmp_0 = rf_0;
      6'b000001:
        casez_tmp_0 = rf_1;
      6'b000010:
        casez_tmp_0 = rf_2;
      6'b000011:
        casez_tmp_0 = rf_3;
      6'b000100:
        casez_tmp_0 = rf_4;
      6'b000101:
        casez_tmp_0 = rf_5;
      6'b000110:
        casez_tmp_0 = rf_6;
      6'b000111:
        casez_tmp_0 = rf_7;
      6'b001000:
        casez_tmp_0 = rf_8;
      6'b001001:
        casez_tmp_0 = rf_9;
      6'b001010:
        casez_tmp_0 = rf_10;
      6'b001011:
        casez_tmp_0 = rf_11;
      6'b001100:
        casez_tmp_0 = rf_12;
      6'b001101:
        casez_tmp_0 = rf_13;
      6'b001110:
        casez_tmp_0 = rf_14;
      6'b001111:
        casez_tmp_0 = rf_15;
      6'b010000:
        casez_tmp_0 = rf_16;
      6'b010001:
        casez_tmp_0 = rf_17;
      6'b010010:
        casez_tmp_0 = rf_18;
      6'b010011:
        casez_tmp_0 = rf_19;
      6'b010100:
        casez_tmp_0 = rf_20;
      6'b010101:
        casez_tmp_0 = rf_21;
      6'b010110:
        casez_tmp_0 = rf_22;
      6'b010111:
        casez_tmp_0 = rf_23;
      6'b011000:
        casez_tmp_0 = rf_24;
      6'b011001:
        casez_tmp_0 = rf_25;
      6'b011010:
        casez_tmp_0 = rf_26;
      6'b011011:
        casez_tmp_0 = rf_27;
      6'b011100:
        casez_tmp_0 = rf_28;
      6'b011101:
        casez_tmp_0 = rf_29;
      6'b011110:
        casez_tmp_0 = rf_30;
      6'b011111:
        casez_tmp_0 = rf_31;
      6'b100000:
        casez_tmp_0 = rf_32;
      6'b100001:
        casez_tmp_0 = rf_33;
      6'b100010:
        casez_tmp_0 = rf_34;
      6'b100011:
        casez_tmp_0 = rf_35;
      6'b100100:
        casez_tmp_0 = rf_36;
      6'b100101:
        casez_tmp_0 = rf_37;
      6'b100110:
        casez_tmp_0 = rf_38;
      6'b100111:
        casez_tmp_0 = rf_39;
      6'b101000:
        casez_tmp_0 = rf_40;
      6'b101001:
        casez_tmp_0 = rf_41;
      6'b101010:
        casez_tmp_0 = rf_42;
      6'b101011:
        casez_tmp_0 = rf_43;
      6'b101100:
        casez_tmp_0 = rf_44;
      6'b101101:
        casez_tmp_0 = rf_45;
      6'b101110:
        casez_tmp_0 = rf_46;
      6'b101111:
        casez_tmp_0 = rf_47;
      6'b110000:
        casez_tmp_0 = rf_48;
      6'b110001:
        casez_tmp_0 = rf_49;
      6'b110010:
        casez_tmp_0 = rf_50;
      6'b110011:
        casez_tmp_0 = rf_51;
      6'b110100:
        casez_tmp_0 = rf_52;
      6'b110101:
        casez_tmp_0 = rf_53;
      6'b110110:
        casez_tmp_0 = rf_54;
      6'b110111:
        casez_tmp_0 = rf_55;
      6'b111000:
        casez_tmp_0 = rf_56;
      6'b111001:
        casez_tmp_0 = rf_57;
      6'b111010:
        casez_tmp_0 = rf_58;
      6'b111011:
        casez_tmp_0 = rf_59;
      6'b111100:
        casez_tmp_0 = rf_60;
      6'b111101:
        casez_tmp_0 = rf_61;
      6'b111110:
        casez_tmp_0 = rf_62;
      default:
        casez_tmp_0 = rf_63;
    endcase
  end // always_comb
  always_comb begin
    casez (io_prj_2)
      6'b000000:
        casez_tmp_1 = rf_0;
      6'b000001:
        casez_tmp_1 = rf_1;
      6'b000010:
        casez_tmp_1 = rf_2;
      6'b000011:
        casez_tmp_1 = rf_3;
      6'b000100:
        casez_tmp_1 = rf_4;
      6'b000101:
        casez_tmp_1 = rf_5;
      6'b000110:
        casez_tmp_1 = rf_6;
      6'b000111:
        casez_tmp_1 = rf_7;
      6'b001000:
        casez_tmp_1 = rf_8;
      6'b001001:
        casez_tmp_1 = rf_9;
      6'b001010:
        casez_tmp_1 = rf_10;
      6'b001011:
        casez_tmp_1 = rf_11;
      6'b001100:
        casez_tmp_1 = rf_12;
      6'b001101:
        casez_tmp_1 = rf_13;
      6'b001110:
        casez_tmp_1 = rf_14;
      6'b001111:
        casez_tmp_1 = rf_15;
      6'b010000:
        casez_tmp_1 = rf_16;
      6'b010001:
        casez_tmp_1 = rf_17;
      6'b010010:
        casez_tmp_1 = rf_18;
      6'b010011:
        casez_tmp_1 = rf_19;
      6'b010100:
        casez_tmp_1 = rf_20;
      6'b010101:
        casez_tmp_1 = rf_21;
      6'b010110:
        casez_tmp_1 = rf_22;
      6'b010111:
        casez_tmp_1 = rf_23;
      6'b011000:
        casez_tmp_1 = rf_24;
      6'b011001:
        casez_tmp_1 = rf_25;
      6'b011010:
        casez_tmp_1 = rf_26;
      6'b011011:
        casez_tmp_1 = rf_27;
      6'b011100:
        casez_tmp_1 = rf_28;
      6'b011101:
        casez_tmp_1 = rf_29;
      6'b011110:
        casez_tmp_1 = rf_30;
      6'b011111:
        casez_tmp_1 = rf_31;
      6'b100000:
        casez_tmp_1 = rf_32;
      6'b100001:
        casez_tmp_1 = rf_33;
      6'b100010:
        casez_tmp_1 = rf_34;
      6'b100011:
        casez_tmp_1 = rf_35;
      6'b100100:
        casez_tmp_1 = rf_36;
      6'b100101:
        casez_tmp_1 = rf_37;
      6'b100110:
        casez_tmp_1 = rf_38;
      6'b100111:
        casez_tmp_1 = rf_39;
      6'b101000:
        casez_tmp_1 = rf_40;
      6'b101001:
        casez_tmp_1 = rf_41;
      6'b101010:
        casez_tmp_1 = rf_42;
      6'b101011:
        casez_tmp_1 = rf_43;
      6'b101100:
        casez_tmp_1 = rf_44;
      6'b101101:
        casez_tmp_1 = rf_45;
      6'b101110:
        casez_tmp_1 = rf_46;
      6'b101111:
        casez_tmp_1 = rf_47;
      6'b110000:
        casez_tmp_1 = rf_48;
      6'b110001:
        casez_tmp_1 = rf_49;
      6'b110010:
        casez_tmp_1 = rf_50;
      6'b110011:
        casez_tmp_1 = rf_51;
      6'b110100:
        casez_tmp_1 = rf_52;
      6'b110101:
        casez_tmp_1 = rf_53;
      6'b110110:
        casez_tmp_1 = rf_54;
      6'b110111:
        casez_tmp_1 = rf_55;
      6'b111000:
        casez_tmp_1 = rf_56;
      6'b111001:
        casez_tmp_1 = rf_57;
      6'b111010:
        casez_tmp_1 = rf_58;
      6'b111011:
        casez_tmp_1 = rf_59;
      6'b111100:
        casez_tmp_1 = rf_60;
      6'b111101:
        casez_tmp_1 = rf_61;
      6'b111110:
        casez_tmp_1 = rf_62;
      default:
        casez_tmp_1 = rf_63;
    endcase
  end // always_comb
  always_comb begin
    casez (io_prk_2)
      6'b000000:
        casez_tmp_2 = rf_0;
      6'b000001:
        casez_tmp_2 = rf_1;
      6'b000010:
        casez_tmp_2 = rf_2;
      6'b000011:
        casez_tmp_2 = rf_3;
      6'b000100:
        casez_tmp_2 = rf_4;
      6'b000101:
        casez_tmp_2 = rf_5;
      6'b000110:
        casez_tmp_2 = rf_6;
      6'b000111:
        casez_tmp_2 = rf_7;
      6'b001000:
        casez_tmp_2 = rf_8;
      6'b001001:
        casez_tmp_2 = rf_9;
      6'b001010:
        casez_tmp_2 = rf_10;
      6'b001011:
        casez_tmp_2 = rf_11;
      6'b001100:
        casez_tmp_2 = rf_12;
      6'b001101:
        casez_tmp_2 = rf_13;
      6'b001110:
        casez_tmp_2 = rf_14;
      6'b001111:
        casez_tmp_2 = rf_15;
      6'b010000:
        casez_tmp_2 = rf_16;
      6'b010001:
        casez_tmp_2 = rf_17;
      6'b010010:
        casez_tmp_2 = rf_18;
      6'b010011:
        casez_tmp_2 = rf_19;
      6'b010100:
        casez_tmp_2 = rf_20;
      6'b010101:
        casez_tmp_2 = rf_21;
      6'b010110:
        casez_tmp_2 = rf_22;
      6'b010111:
        casez_tmp_2 = rf_23;
      6'b011000:
        casez_tmp_2 = rf_24;
      6'b011001:
        casez_tmp_2 = rf_25;
      6'b011010:
        casez_tmp_2 = rf_26;
      6'b011011:
        casez_tmp_2 = rf_27;
      6'b011100:
        casez_tmp_2 = rf_28;
      6'b011101:
        casez_tmp_2 = rf_29;
      6'b011110:
        casez_tmp_2 = rf_30;
      6'b011111:
        casez_tmp_2 = rf_31;
      6'b100000:
        casez_tmp_2 = rf_32;
      6'b100001:
        casez_tmp_2 = rf_33;
      6'b100010:
        casez_tmp_2 = rf_34;
      6'b100011:
        casez_tmp_2 = rf_35;
      6'b100100:
        casez_tmp_2 = rf_36;
      6'b100101:
        casez_tmp_2 = rf_37;
      6'b100110:
        casez_tmp_2 = rf_38;
      6'b100111:
        casez_tmp_2 = rf_39;
      6'b101000:
        casez_tmp_2 = rf_40;
      6'b101001:
        casez_tmp_2 = rf_41;
      6'b101010:
        casez_tmp_2 = rf_42;
      6'b101011:
        casez_tmp_2 = rf_43;
      6'b101100:
        casez_tmp_2 = rf_44;
      6'b101101:
        casez_tmp_2 = rf_45;
      6'b101110:
        casez_tmp_2 = rf_46;
      6'b101111:
        casez_tmp_2 = rf_47;
      6'b110000:
        casez_tmp_2 = rf_48;
      6'b110001:
        casez_tmp_2 = rf_49;
      6'b110010:
        casez_tmp_2 = rf_50;
      6'b110011:
        casez_tmp_2 = rf_51;
      6'b110100:
        casez_tmp_2 = rf_52;
      6'b110101:
        casez_tmp_2 = rf_53;
      6'b110110:
        casez_tmp_2 = rf_54;
      6'b110111:
        casez_tmp_2 = rf_55;
      6'b111000:
        casez_tmp_2 = rf_56;
      6'b111001:
        casez_tmp_2 = rf_57;
      6'b111010:
        casez_tmp_2 = rf_58;
      6'b111011:
        casez_tmp_2 = rf_59;
      6'b111100:
        casez_tmp_2 = rf_60;
      6'b111101:
        casez_tmp_2 = rf_61;
      6'b111110:
        casez_tmp_2 = rf_62;
      default:
        casez_tmp_2 = rf_63;
    endcase
  end // always_comb
  always_comb begin
    casez (io_prj_3)
      6'b000000:
        casez_tmp_3 = rf_0;
      6'b000001:
        casez_tmp_3 = rf_1;
      6'b000010:
        casez_tmp_3 = rf_2;
      6'b000011:
        casez_tmp_3 = rf_3;
      6'b000100:
        casez_tmp_3 = rf_4;
      6'b000101:
        casez_tmp_3 = rf_5;
      6'b000110:
        casez_tmp_3 = rf_6;
      6'b000111:
        casez_tmp_3 = rf_7;
      6'b001000:
        casez_tmp_3 = rf_8;
      6'b001001:
        casez_tmp_3 = rf_9;
      6'b001010:
        casez_tmp_3 = rf_10;
      6'b001011:
        casez_tmp_3 = rf_11;
      6'b001100:
        casez_tmp_3 = rf_12;
      6'b001101:
        casez_tmp_3 = rf_13;
      6'b001110:
        casez_tmp_3 = rf_14;
      6'b001111:
        casez_tmp_3 = rf_15;
      6'b010000:
        casez_tmp_3 = rf_16;
      6'b010001:
        casez_tmp_3 = rf_17;
      6'b010010:
        casez_tmp_3 = rf_18;
      6'b010011:
        casez_tmp_3 = rf_19;
      6'b010100:
        casez_tmp_3 = rf_20;
      6'b010101:
        casez_tmp_3 = rf_21;
      6'b010110:
        casez_tmp_3 = rf_22;
      6'b010111:
        casez_tmp_3 = rf_23;
      6'b011000:
        casez_tmp_3 = rf_24;
      6'b011001:
        casez_tmp_3 = rf_25;
      6'b011010:
        casez_tmp_3 = rf_26;
      6'b011011:
        casez_tmp_3 = rf_27;
      6'b011100:
        casez_tmp_3 = rf_28;
      6'b011101:
        casez_tmp_3 = rf_29;
      6'b011110:
        casez_tmp_3 = rf_30;
      6'b011111:
        casez_tmp_3 = rf_31;
      6'b100000:
        casez_tmp_3 = rf_32;
      6'b100001:
        casez_tmp_3 = rf_33;
      6'b100010:
        casez_tmp_3 = rf_34;
      6'b100011:
        casez_tmp_3 = rf_35;
      6'b100100:
        casez_tmp_3 = rf_36;
      6'b100101:
        casez_tmp_3 = rf_37;
      6'b100110:
        casez_tmp_3 = rf_38;
      6'b100111:
        casez_tmp_3 = rf_39;
      6'b101000:
        casez_tmp_3 = rf_40;
      6'b101001:
        casez_tmp_3 = rf_41;
      6'b101010:
        casez_tmp_3 = rf_42;
      6'b101011:
        casez_tmp_3 = rf_43;
      6'b101100:
        casez_tmp_3 = rf_44;
      6'b101101:
        casez_tmp_3 = rf_45;
      6'b101110:
        casez_tmp_3 = rf_46;
      6'b101111:
        casez_tmp_3 = rf_47;
      6'b110000:
        casez_tmp_3 = rf_48;
      6'b110001:
        casez_tmp_3 = rf_49;
      6'b110010:
        casez_tmp_3 = rf_50;
      6'b110011:
        casez_tmp_3 = rf_51;
      6'b110100:
        casez_tmp_3 = rf_52;
      6'b110101:
        casez_tmp_3 = rf_53;
      6'b110110:
        casez_tmp_3 = rf_54;
      6'b110111:
        casez_tmp_3 = rf_55;
      6'b111000:
        casez_tmp_3 = rf_56;
      6'b111001:
        casez_tmp_3 = rf_57;
      6'b111010:
        casez_tmp_3 = rf_58;
      6'b111011:
        casez_tmp_3 = rf_59;
      6'b111100:
        casez_tmp_3 = rf_60;
      6'b111101:
        casez_tmp_3 = rf_61;
      6'b111110:
        casez_tmp_3 = rf_62;
      default:
        casez_tmp_3 = rf_63;
    endcase
  end // always_comb
  always_comb begin
    casez (io_prk_3)
      6'b000000:
        casez_tmp_4 = rf_0;
      6'b000001:
        casez_tmp_4 = rf_1;
      6'b000010:
        casez_tmp_4 = rf_2;
      6'b000011:
        casez_tmp_4 = rf_3;
      6'b000100:
        casez_tmp_4 = rf_4;
      6'b000101:
        casez_tmp_4 = rf_5;
      6'b000110:
        casez_tmp_4 = rf_6;
      6'b000111:
        casez_tmp_4 = rf_7;
      6'b001000:
        casez_tmp_4 = rf_8;
      6'b001001:
        casez_tmp_4 = rf_9;
      6'b001010:
        casez_tmp_4 = rf_10;
      6'b001011:
        casez_tmp_4 = rf_11;
      6'b001100:
        casez_tmp_4 = rf_12;
      6'b001101:
        casez_tmp_4 = rf_13;
      6'b001110:
        casez_tmp_4 = rf_14;
      6'b001111:
        casez_tmp_4 = rf_15;
      6'b010000:
        casez_tmp_4 = rf_16;
      6'b010001:
        casez_tmp_4 = rf_17;
      6'b010010:
        casez_tmp_4 = rf_18;
      6'b010011:
        casez_tmp_4 = rf_19;
      6'b010100:
        casez_tmp_4 = rf_20;
      6'b010101:
        casez_tmp_4 = rf_21;
      6'b010110:
        casez_tmp_4 = rf_22;
      6'b010111:
        casez_tmp_4 = rf_23;
      6'b011000:
        casez_tmp_4 = rf_24;
      6'b011001:
        casez_tmp_4 = rf_25;
      6'b011010:
        casez_tmp_4 = rf_26;
      6'b011011:
        casez_tmp_4 = rf_27;
      6'b011100:
        casez_tmp_4 = rf_28;
      6'b011101:
        casez_tmp_4 = rf_29;
      6'b011110:
        casez_tmp_4 = rf_30;
      6'b011111:
        casez_tmp_4 = rf_31;
      6'b100000:
        casez_tmp_4 = rf_32;
      6'b100001:
        casez_tmp_4 = rf_33;
      6'b100010:
        casez_tmp_4 = rf_34;
      6'b100011:
        casez_tmp_4 = rf_35;
      6'b100100:
        casez_tmp_4 = rf_36;
      6'b100101:
        casez_tmp_4 = rf_37;
      6'b100110:
        casez_tmp_4 = rf_38;
      6'b100111:
        casez_tmp_4 = rf_39;
      6'b101000:
        casez_tmp_4 = rf_40;
      6'b101001:
        casez_tmp_4 = rf_41;
      6'b101010:
        casez_tmp_4 = rf_42;
      6'b101011:
        casez_tmp_4 = rf_43;
      6'b101100:
        casez_tmp_4 = rf_44;
      6'b101101:
        casez_tmp_4 = rf_45;
      6'b101110:
        casez_tmp_4 = rf_46;
      6'b101111:
        casez_tmp_4 = rf_47;
      6'b110000:
        casez_tmp_4 = rf_48;
      6'b110001:
        casez_tmp_4 = rf_49;
      6'b110010:
        casez_tmp_4 = rf_50;
      6'b110011:
        casez_tmp_4 = rf_51;
      6'b110100:
        casez_tmp_4 = rf_52;
      6'b110101:
        casez_tmp_4 = rf_53;
      6'b110110:
        casez_tmp_4 = rf_54;
      6'b110111:
        casez_tmp_4 = rf_55;
      6'b111000:
        casez_tmp_4 = rf_56;
      6'b111001:
        casez_tmp_4 = rf_57;
      6'b111010:
        casez_tmp_4 = rf_58;
      6'b111011:
        casez_tmp_4 = rf_59;
      6'b111100:
        casez_tmp_4 = rf_60;
      6'b111101:
        casez_tmp_4 = rf_61;
      6'b111110:
        casez_tmp_4 = rf_62;
      default:
        casez_tmp_4 = rf_63;
    endcase
  end // always_comb
  always_comb begin
    casez (io_prj_4)
      6'b000000:
        casez_tmp_5 = rf_0;
      6'b000001:
        casez_tmp_5 = rf_1;
      6'b000010:
        casez_tmp_5 = rf_2;
      6'b000011:
        casez_tmp_5 = rf_3;
      6'b000100:
        casez_tmp_5 = rf_4;
      6'b000101:
        casez_tmp_5 = rf_5;
      6'b000110:
        casez_tmp_5 = rf_6;
      6'b000111:
        casez_tmp_5 = rf_7;
      6'b001000:
        casez_tmp_5 = rf_8;
      6'b001001:
        casez_tmp_5 = rf_9;
      6'b001010:
        casez_tmp_5 = rf_10;
      6'b001011:
        casez_tmp_5 = rf_11;
      6'b001100:
        casez_tmp_5 = rf_12;
      6'b001101:
        casez_tmp_5 = rf_13;
      6'b001110:
        casez_tmp_5 = rf_14;
      6'b001111:
        casez_tmp_5 = rf_15;
      6'b010000:
        casez_tmp_5 = rf_16;
      6'b010001:
        casez_tmp_5 = rf_17;
      6'b010010:
        casez_tmp_5 = rf_18;
      6'b010011:
        casez_tmp_5 = rf_19;
      6'b010100:
        casez_tmp_5 = rf_20;
      6'b010101:
        casez_tmp_5 = rf_21;
      6'b010110:
        casez_tmp_5 = rf_22;
      6'b010111:
        casez_tmp_5 = rf_23;
      6'b011000:
        casez_tmp_5 = rf_24;
      6'b011001:
        casez_tmp_5 = rf_25;
      6'b011010:
        casez_tmp_5 = rf_26;
      6'b011011:
        casez_tmp_5 = rf_27;
      6'b011100:
        casez_tmp_5 = rf_28;
      6'b011101:
        casez_tmp_5 = rf_29;
      6'b011110:
        casez_tmp_5 = rf_30;
      6'b011111:
        casez_tmp_5 = rf_31;
      6'b100000:
        casez_tmp_5 = rf_32;
      6'b100001:
        casez_tmp_5 = rf_33;
      6'b100010:
        casez_tmp_5 = rf_34;
      6'b100011:
        casez_tmp_5 = rf_35;
      6'b100100:
        casez_tmp_5 = rf_36;
      6'b100101:
        casez_tmp_5 = rf_37;
      6'b100110:
        casez_tmp_5 = rf_38;
      6'b100111:
        casez_tmp_5 = rf_39;
      6'b101000:
        casez_tmp_5 = rf_40;
      6'b101001:
        casez_tmp_5 = rf_41;
      6'b101010:
        casez_tmp_5 = rf_42;
      6'b101011:
        casez_tmp_5 = rf_43;
      6'b101100:
        casez_tmp_5 = rf_44;
      6'b101101:
        casez_tmp_5 = rf_45;
      6'b101110:
        casez_tmp_5 = rf_46;
      6'b101111:
        casez_tmp_5 = rf_47;
      6'b110000:
        casez_tmp_5 = rf_48;
      6'b110001:
        casez_tmp_5 = rf_49;
      6'b110010:
        casez_tmp_5 = rf_50;
      6'b110011:
        casez_tmp_5 = rf_51;
      6'b110100:
        casez_tmp_5 = rf_52;
      6'b110101:
        casez_tmp_5 = rf_53;
      6'b110110:
        casez_tmp_5 = rf_54;
      6'b110111:
        casez_tmp_5 = rf_55;
      6'b111000:
        casez_tmp_5 = rf_56;
      6'b111001:
        casez_tmp_5 = rf_57;
      6'b111010:
        casez_tmp_5 = rf_58;
      6'b111011:
        casez_tmp_5 = rf_59;
      6'b111100:
        casez_tmp_5 = rf_60;
      6'b111101:
        casez_tmp_5 = rf_61;
      6'b111110:
        casez_tmp_5 = rf_62;
      default:
        casez_tmp_5 = rf_63;
    endcase
  end // always_comb
  always_comb begin
    casez (io_prk_4)
      6'b000000:
        casez_tmp_6 = rf_0;
      6'b000001:
        casez_tmp_6 = rf_1;
      6'b000010:
        casez_tmp_6 = rf_2;
      6'b000011:
        casez_tmp_6 = rf_3;
      6'b000100:
        casez_tmp_6 = rf_4;
      6'b000101:
        casez_tmp_6 = rf_5;
      6'b000110:
        casez_tmp_6 = rf_6;
      6'b000111:
        casez_tmp_6 = rf_7;
      6'b001000:
        casez_tmp_6 = rf_8;
      6'b001001:
        casez_tmp_6 = rf_9;
      6'b001010:
        casez_tmp_6 = rf_10;
      6'b001011:
        casez_tmp_6 = rf_11;
      6'b001100:
        casez_tmp_6 = rf_12;
      6'b001101:
        casez_tmp_6 = rf_13;
      6'b001110:
        casez_tmp_6 = rf_14;
      6'b001111:
        casez_tmp_6 = rf_15;
      6'b010000:
        casez_tmp_6 = rf_16;
      6'b010001:
        casez_tmp_6 = rf_17;
      6'b010010:
        casez_tmp_6 = rf_18;
      6'b010011:
        casez_tmp_6 = rf_19;
      6'b010100:
        casez_tmp_6 = rf_20;
      6'b010101:
        casez_tmp_6 = rf_21;
      6'b010110:
        casez_tmp_6 = rf_22;
      6'b010111:
        casez_tmp_6 = rf_23;
      6'b011000:
        casez_tmp_6 = rf_24;
      6'b011001:
        casez_tmp_6 = rf_25;
      6'b011010:
        casez_tmp_6 = rf_26;
      6'b011011:
        casez_tmp_6 = rf_27;
      6'b011100:
        casez_tmp_6 = rf_28;
      6'b011101:
        casez_tmp_6 = rf_29;
      6'b011110:
        casez_tmp_6 = rf_30;
      6'b011111:
        casez_tmp_6 = rf_31;
      6'b100000:
        casez_tmp_6 = rf_32;
      6'b100001:
        casez_tmp_6 = rf_33;
      6'b100010:
        casez_tmp_6 = rf_34;
      6'b100011:
        casez_tmp_6 = rf_35;
      6'b100100:
        casez_tmp_6 = rf_36;
      6'b100101:
        casez_tmp_6 = rf_37;
      6'b100110:
        casez_tmp_6 = rf_38;
      6'b100111:
        casez_tmp_6 = rf_39;
      6'b101000:
        casez_tmp_6 = rf_40;
      6'b101001:
        casez_tmp_6 = rf_41;
      6'b101010:
        casez_tmp_6 = rf_42;
      6'b101011:
        casez_tmp_6 = rf_43;
      6'b101100:
        casez_tmp_6 = rf_44;
      6'b101101:
        casez_tmp_6 = rf_45;
      6'b101110:
        casez_tmp_6 = rf_46;
      6'b101111:
        casez_tmp_6 = rf_47;
      6'b110000:
        casez_tmp_6 = rf_48;
      6'b110001:
        casez_tmp_6 = rf_49;
      6'b110010:
        casez_tmp_6 = rf_50;
      6'b110011:
        casez_tmp_6 = rf_51;
      6'b110100:
        casez_tmp_6 = rf_52;
      6'b110101:
        casez_tmp_6 = rf_53;
      6'b110110:
        casez_tmp_6 = rf_54;
      6'b110111:
        casez_tmp_6 = rf_55;
      6'b111000:
        casez_tmp_6 = rf_56;
      6'b111001:
        casez_tmp_6 = rf_57;
      6'b111010:
        casez_tmp_6 = rf_58;
      6'b111011:
        casez_tmp_6 = rf_59;
      6'b111100:
        casez_tmp_6 = rf_60;
      6'b111101:
        casez_tmp_6 = rf_61;
      6'b111110:
        casez_tmp_6 = rf_62;
      default:
        casez_tmp_6 = rf_63;
    endcase
  end // always_comb
  always @(posedge clock) begin
    if (io_rf_we4 & io_prd_4 == 6'h0)
      rf_0 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h0)
      rf_0 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h0)
      rf_0 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h0)
      rf_0 <= io_wdata1;
    else if (reset)
      rf_0 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h1)
      rf_1 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h1)
      rf_1 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h1)
      rf_1 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h1)
      rf_1 <= io_wdata1;
    else if (reset)
      rf_1 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h2)
      rf_2 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h2)
      rf_2 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h2)
      rf_2 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h2)
      rf_2 <= io_wdata1;
    else if (reset)
      rf_2 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h3)
      rf_3 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h3)
      rf_3 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h3)
      rf_3 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h3)
      rf_3 <= io_wdata1;
    else if (reset)
      rf_3 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h4)
      rf_4 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h4)
      rf_4 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h4)
      rf_4 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h4)
      rf_4 <= io_wdata1;
    else if (reset)
      rf_4 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h5)
      rf_5 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h5)
      rf_5 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h5)
      rf_5 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h5)
      rf_5 <= io_wdata1;
    else if (reset)
      rf_5 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h6)
      rf_6 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h6)
      rf_6 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h6)
      rf_6 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h6)
      rf_6 <= io_wdata1;
    else if (reset)
      rf_6 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h7)
      rf_7 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h7)
      rf_7 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h7)
      rf_7 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h7)
      rf_7 <= io_wdata1;
    else if (reset)
      rf_7 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h8)
      rf_8 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h8)
      rf_8 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h8)
      rf_8 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h8)
      rf_8 <= io_wdata1;
    else if (reset)
      rf_8 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h9)
      rf_9 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h9)
      rf_9 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h9)
      rf_9 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h9)
      rf_9 <= io_wdata1;
    else if (reset)
      rf_9 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'hA)
      rf_10 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'hA)
      rf_10 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'hA)
      rf_10 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'hA)
      rf_10 <= io_wdata1;
    else if (reset)
      rf_10 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'hB)
      rf_11 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'hB)
      rf_11 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'hB)
      rf_11 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'hB)
      rf_11 <= io_wdata1;
    else if (reset)
      rf_11 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'hC)
      rf_12 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'hC)
      rf_12 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'hC)
      rf_12 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'hC)
      rf_12 <= io_wdata1;
    else if (reset)
      rf_12 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'hD)
      rf_13 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'hD)
      rf_13 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'hD)
      rf_13 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'hD)
      rf_13 <= io_wdata1;
    else if (reset)
      rf_13 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'hE)
      rf_14 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'hE)
      rf_14 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'hE)
      rf_14 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'hE)
      rf_14 <= io_wdata1;
    else if (reset)
      rf_14 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'hF)
      rf_15 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'hF)
      rf_15 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'hF)
      rf_15 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'hF)
      rf_15 <= io_wdata1;
    else if (reset)
      rf_15 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h10)
      rf_16 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h10)
      rf_16 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h10)
      rf_16 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h10)
      rf_16 <= io_wdata1;
    else if (reset)
      rf_16 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h11)
      rf_17 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h11)
      rf_17 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h11)
      rf_17 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h11)
      rf_17 <= io_wdata1;
    else if (reset)
      rf_17 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h12)
      rf_18 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h12)
      rf_18 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h12)
      rf_18 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h12)
      rf_18 <= io_wdata1;
    else if (reset)
      rf_18 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h13)
      rf_19 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h13)
      rf_19 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h13)
      rf_19 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h13)
      rf_19 <= io_wdata1;
    else if (reset)
      rf_19 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h14)
      rf_20 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h14)
      rf_20 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h14)
      rf_20 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h14)
      rf_20 <= io_wdata1;
    else if (reset)
      rf_20 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h15)
      rf_21 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h15)
      rf_21 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h15)
      rf_21 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h15)
      rf_21 <= io_wdata1;
    else if (reset)
      rf_21 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h16)
      rf_22 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h16)
      rf_22 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h16)
      rf_22 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h16)
      rf_22 <= io_wdata1;
    else if (reset)
      rf_22 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h17)
      rf_23 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h17)
      rf_23 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h17)
      rf_23 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h17)
      rf_23 <= io_wdata1;
    else if (reset)
      rf_23 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h18)
      rf_24 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h18)
      rf_24 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h18)
      rf_24 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h18)
      rf_24 <= io_wdata1;
    else if (reset)
      rf_24 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h19)
      rf_25 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h19)
      rf_25 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h19)
      rf_25 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h19)
      rf_25 <= io_wdata1;
    else if (reset)
      rf_25 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h1A)
      rf_26 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h1A)
      rf_26 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h1A)
      rf_26 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h1A)
      rf_26 <= io_wdata1;
    else if (reset)
      rf_26 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h1B)
      rf_27 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h1B)
      rf_27 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h1B)
      rf_27 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h1B)
      rf_27 <= io_wdata1;
    else if (reset)
      rf_27 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h1C)
      rf_28 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h1C)
      rf_28 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h1C)
      rf_28 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h1C)
      rf_28 <= io_wdata1;
    else if (reset)
      rf_28 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h1D)
      rf_29 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h1D)
      rf_29 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h1D)
      rf_29 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h1D)
      rf_29 <= io_wdata1;
    else if (reset)
      rf_29 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h1E)
      rf_30 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h1E)
      rf_30 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h1E)
      rf_30 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h1E)
      rf_30 <= io_wdata1;
    else if (reset)
      rf_30 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h1F)
      rf_31 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h1F)
      rf_31 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h1F)
      rf_31 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h1F)
      rf_31 <= io_wdata1;
    else if (reset)
      rf_31 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h20)
      rf_32 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h20)
      rf_32 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h20)
      rf_32 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h20)
      rf_32 <= io_wdata1;
    else if (reset)
      rf_32 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h21)
      rf_33 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h21)
      rf_33 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h21)
      rf_33 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h21)
      rf_33 <= io_wdata1;
    else if (reset)
      rf_33 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h22)
      rf_34 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h22)
      rf_34 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h22)
      rf_34 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h22)
      rf_34 <= io_wdata1;
    else if (reset)
      rf_34 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h23)
      rf_35 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h23)
      rf_35 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h23)
      rf_35 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h23)
      rf_35 <= io_wdata1;
    else if (reset)
      rf_35 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h24)
      rf_36 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h24)
      rf_36 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h24)
      rf_36 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h24)
      rf_36 <= io_wdata1;
    else if (reset)
      rf_36 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h25)
      rf_37 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h25)
      rf_37 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h25)
      rf_37 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h25)
      rf_37 <= io_wdata1;
    else if (reset)
      rf_37 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h26)
      rf_38 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h26)
      rf_38 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h26)
      rf_38 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h26)
      rf_38 <= io_wdata1;
    else if (reset)
      rf_38 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h27)
      rf_39 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h27)
      rf_39 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h27)
      rf_39 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h27)
      rf_39 <= io_wdata1;
    else if (reset)
      rf_39 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h28)
      rf_40 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h28)
      rf_40 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h28)
      rf_40 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h28)
      rf_40 <= io_wdata1;
    else if (reset)
      rf_40 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h29)
      rf_41 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h29)
      rf_41 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h29)
      rf_41 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h29)
      rf_41 <= io_wdata1;
    else if (reset)
      rf_41 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h2A)
      rf_42 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h2A)
      rf_42 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h2A)
      rf_42 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h2A)
      rf_42 <= io_wdata1;
    else if (reset)
      rf_42 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h2B)
      rf_43 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h2B)
      rf_43 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h2B)
      rf_43 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h2B)
      rf_43 <= io_wdata1;
    else if (reset)
      rf_43 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h2C)
      rf_44 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h2C)
      rf_44 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h2C)
      rf_44 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h2C)
      rf_44 <= io_wdata1;
    else if (reset)
      rf_44 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h2D)
      rf_45 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h2D)
      rf_45 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h2D)
      rf_45 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h2D)
      rf_45 <= io_wdata1;
    else if (reset)
      rf_45 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h2E)
      rf_46 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h2E)
      rf_46 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h2E)
      rf_46 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h2E)
      rf_46 <= io_wdata1;
    else if (reset)
      rf_46 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h2F)
      rf_47 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h2F)
      rf_47 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h2F)
      rf_47 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h2F)
      rf_47 <= io_wdata1;
    else if (reset)
      rf_47 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h30)
      rf_48 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h30)
      rf_48 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h30)
      rf_48 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h30)
      rf_48 <= io_wdata1;
    else if (reset)
      rf_48 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h31)
      rf_49 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h31)
      rf_49 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h31)
      rf_49 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h31)
      rf_49 <= io_wdata1;
    else if (reset)
      rf_49 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h32)
      rf_50 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h32)
      rf_50 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h32)
      rf_50 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h32)
      rf_50 <= io_wdata1;
    else if (reset)
      rf_50 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h33)
      rf_51 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h33)
      rf_51 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h33)
      rf_51 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h33)
      rf_51 <= io_wdata1;
    else if (reset)
      rf_51 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h34)
      rf_52 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h34)
      rf_52 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h34)
      rf_52 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h34)
      rf_52 <= io_wdata1;
    else if (reset)
      rf_52 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h35)
      rf_53 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h35)
      rf_53 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h35)
      rf_53 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h35)
      rf_53 <= io_wdata1;
    else if (reset)
      rf_53 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h36)
      rf_54 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h36)
      rf_54 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h36)
      rf_54 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h36)
      rf_54 <= io_wdata1;
    else if (reset)
      rf_54 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h37)
      rf_55 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h37)
      rf_55 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h37)
      rf_55 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h37)
      rf_55 <= io_wdata1;
    else if (reset)
      rf_55 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h38)
      rf_56 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h38)
      rf_56 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h38)
      rf_56 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h38)
      rf_56 <= io_wdata1;
    else if (reset)
      rf_56 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h39)
      rf_57 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h39)
      rf_57 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h39)
      rf_57 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h39)
      rf_57 <= io_wdata1;
    else if (reset)
      rf_57 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h3A)
      rf_58 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h3A)
      rf_58 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h3A)
      rf_58 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h3A)
      rf_58 <= io_wdata1;
    else if (reset)
      rf_58 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h3B)
      rf_59 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h3B)
      rf_59 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h3B)
      rf_59 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h3B)
      rf_59 <= io_wdata1;
    else if (reset)
      rf_59 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h3C)
      rf_60 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h3C)
      rf_60 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h3C)
      rf_60 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h3C)
      rf_60 <= io_wdata1;
    else if (reset)
      rf_60 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h3D)
      rf_61 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h3D)
      rf_61 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h3D)
      rf_61 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h3D)
      rf_61 <= io_wdata1;
    else if (reset)
      rf_61 <= 32'h0;
    if (io_rf_we4 & io_prd_4 == 6'h3E)
      rf_62 <= io_wdata4;
    else if (io_rf_we3 & io_prd_3 == 6'h3E)
      rf_62 <= io_wdata3;
    else if (io_rf_we2 & io_prd_2 == 6'h3E)
      rf_62 <= io_wdata2;
    else if (io_rf_we1 & io_prd_1 == 6'h3E)
      rf_62 <= io_wdata1;
    else if (reset)
      rf_62 <= 32'h0;
    if (io_rf_we4 & (&io_prd_4))
      rf_63 <= io_wdata4;
    else if (io_rf_we3 & (&io_prd_3))
      rf_63 <= io_wdata3;
    else if (io_rf_we2 & (&io_prd_2))
      rf_63 <= io_wdata2;
    else if (io_rf_we1 & (&io_prd_1))
      rf_63 <= io_wdata1;
    else if (reset)
      rf_63 <= 32'h0;
  end // always @(posedge)
  assign io_rj_data_1 =
    io_prj_1 == io_prd_1 & io_rf_we1
      ? io_wdata1
      : io_prj_1 == io_prd_2 & io_rf_we2 ? io_wdata2 : casez_tmp;
  assign io_rk_data_1 =
    io_prk_1 == io_prd_1 & io_rf_we1
      ? io_wdata1
      : io_prk_1 == io_prd_2 & io_rf_we2 ? io_wdata2 : casez_tmp_0;
  assign io_rj_data_2 =
    io_prj_2 == io_prd_1 & io_rf_we1
      ? io_wdata1
      : io_prj_2 == io_prd_2 & io_rf_we2 ? io_wdata2 : casez_tmp_1;
  assign io_rk_data_2 =
    io_prk_2 == io_prd_1 & io_rf_we1
      ? io_wdata1
      : io_prk_2 == io_prd_2 & io_rf_we2 ? io_wdata2 : casez_tmp_2;
  assign io_rj_data_3 = io_prj_3 == io_prd_3 & io_rf_we3 ? io_wdata3 : casez_tmp_3;
  assign io_rk_data_3 = io_prk_3 == io_prd_3 & io_rf_we3 ? io_wdata3 : casez_tmp_4;
  assign io_rj_data_4 = io_prj_4 == io_prd_4 & io_rf_we4 ? io_wdata4 : casez_tmp_5;
  assign io_rk_data_4 = io_prk_4 == io_prd_4 & io_rf_we4 ? io_wdata4 : casez_tmp_6;
endmodule
