// Generated by CIRCT firtool-1.56.1
module Decode(
  input  [31:0] io_inst,
  output [4:0]  io_rj,
  output        io_rj_valid,
  output [4:0]  io_rk,
  output        io_rk_valid,
  output [4:0]  io_rd,
  output        io_rd_valid,
  output [31:0] io_imm,
  output [4:0]  io_alu_op,
  output [1:0]  io_alu_rs1_sel,
                io_alu_rs2_sel,
  output [3:0]  io_br_type,
  output [4:0]  io_mem_type,
  output [1:0]  io_fu_id
);

  wire       _ctrl_T_1 = io_inst[31:15] == 17'h20;
  wire       _ctrl_T_3 = io_inst[31:15] == 17'h22;
  wire       _ctrl_T_5 = io_inst[31:15] == 17'h24;
  wire       _ctrl_T_7 = io_inst[31:15] == 17'h25;
  wire       _ctrl_T_9 = io_inst[31:15] == 17'h28;
  wire       _ctrl_T_11 = io_inst[31:15] == 17'h29;
  wire       _ctrl_T_13 = io_inst[31:15] == 17'h2A;
  wire       _ctrl_T_15 = io_inst[31:15] == 17'h2B;
  wire       _ctrl_T_17 = io_inst[31:15] == 17'h2E;
  wire       _ctrl_T_19 = io_inst[31:15] == 17'h2F;
  wire       _ctrl_T_21 = io_inst[31:15] == 17'h30;
  wire       _ctrl_T_23 = io_inst[31:15] == 17'h38;
  wire       _ctrl_T_25 = io_inst[31:15] == 17'h39;
  wire       _ctrl_T_27 = io_inst[31:15] == 17'h3A;
  wire       _ctrl_T_29 = io_inst[31:15] == 17'h40;
  wire       _ctrl_T_31 = io_inst[31:15] == 17'h41;
  wire       _ctrl_T_33 = io_inst[31:15] == 17'h42;
  wire       _ctrl_T_35 = io_inst[31:15] == 17'h43;
  wire       _ctrl_T_37 = io_inst[31:15] == 17'h81;
  wire       _ctrl_T_39 = io_inst[31:15] == 17'h89;
  wire       _ctrl_T_41 = io_inst[31:15] == 17'h91;
  wire       _ctrl_T_43 = io_inst[31:22] == 10'h8;
  wire       _ctrl_T_45 = io_inst[31:22] == 10'h9;
  wire       _ctrl_T_47 = io_inst[31:22] == 10'hA;
  wire       _ctrl_T_49 = io_inst[31:22] == 10'hD;
  wire       _ctrl_T_51 = io_inst[31:22] == 10'hE;
  wire       _ctrl_T_53 = io_inst[31:22] == 10'hF;
  wire       _ctrl_T_55 = io_inst[31:25] == 7'hA;
  wire       _ctrl_T_57 = io_inst[31:25] == 7'hE;
  wire       _ctrl_T_59 = io_inst[31:22] == 10'hA0;
  wire       _ctrl_T_61 = io_inst[31:22] == 10'hA1;
  wire       _ctrl_T_63 = io_inst[31:22] == 10'hA2;
  wire       _ctrl_T_65 = io_inst[31:22] == 10'hA4;
  wire       _ctrl_T_67 = io_inst[31:22] == 10'hA5;
  wire       _ctrl_T_69 = io_inst[31:22] == 10'hA6;
  wire       _ctrl_T_71 = io_inst[31:22] == 10'hA8;
  wire       _ctrl_T_73 = io_inst[31:22] == 10'hA9;
  wire       _ctrl_T_75 = io_inst[31:26] == 6'h13;
  wire       _ctrl_T_77 = io_inst[31:26] == 6'h14;
  wire       _ctrl_T_548 = io_inst[31:26] == 6'h15;
  wire       _ctrl_T_81 = io_inst[31:26] == 6'h16;
  wire       _ctrl_T_83 = io_inst[31:26] == 6'h17;
  wire       _ctrl_T_85 = io_inst[31:26] == 6'h18;
  wire       _ctrl_T_87 = io_inst[31:26] == 6'h19;
  wire       _ctrl_T_89 = io_inst[31:26] == 6'h1A;
  wire       _ctrl_T_632 = io_inst[31:26] == 6'h1B;
  wire       _GEN = _ctrl_T_81 | _ctrl_T_83 | _ctrl_T_85 | _ctrl_T_87 | _ctrl_T_89;
  wire       _GEN_0 = _ctrl_T_77 | _ctrl_T_548;
  wire       _GEN_1 = _ctrl_T_71 | _ctrl_T_73 | _ctrl_T_75;
  wire       _GEN_2 = _ctrl_T_55 | _ctrl_T_57;
  wire       _GEN_3 = _ctrl_T_49 | _ctrl_T_51 | _ctrl_T_53;
  wire       _GEN_4 =
    _ctrl_T_1 | _ctrl_T_3 | _ctrl_T_5 | _ctrl_T_7 | _ctrl_T_9 | _ctrl_T_11 | _ctrl_T_13
    | _ctrl_T_15 | _ctrl_T_17 | _ctrl_T_19 | _ctrl_T_21 | _ctrl_T_23 | _ctrl_T_25
    | _ctrl_T_27 | _ctrl_T_29 | _ctrl_T_31 | _ctrl_T_33 | _ctrl_T_35 | _ctrl_T_37
    | _ctrl_T_39 | _ctrl_T_41 | _ctrl_T_43 | _ctrl_T_45 | _ctrl_T_47 | _GEN_3;
  wire       _GEN_5 = _ctrl_T_71 | _ctrl_T_73 | _ctrl_T_75 | _GEN_0;
  wire       _GEN_6 = _ctrl_T_65 | _ctrl_T_67 | _ctrl_T_69;
  wire       _GEN_7 =
    _ctrl_T_37 | _ctrl_T_39 | _ctrl_T_41 | _ctrl_T_43 | _ctrl_T_45 | _ctrl_T_47
    | _ctrl_T_49 | _ctrl_T_51 | _ctrl_T_53 | _ctrl_T_55 | _ctrl_T_57 | _ctrl_T_59
    | _ctrl_T_61 | _ctrl_T_63;
  wire       _GEN_8 =
    _ctrl_T_23 | _ctrl_T_25 | _ctrl_T_27 | _ctrl_T_29 | _ctrl_T_31 | _ctrl_T_33
    | _ctrl_T_35;
  wire       _GEN_9 =
    _ctrl_T_1 | _ctrl_T_3 | _ctrl_T_5 | _ctrl_T_7 | _ctrl_T_9 | _ctrl_T_11 | _ctrl_T_13
    | _ctrl_T_15 | _ctrl_T_17 | _ctrl_T_19 | _ctrl_T_21 | _GEN_8;
  wire       _GEN_10 =
    _ctrl_T_1 | _ctrl_T_3 | _ctrl_T_5 | _ctrl_T_7 | _ctrl_T_9 | _ctrl_T_11 | _ctrl_T_13
    | _ctrl_T_15 | _ctrl_T_17 | _ctrl_T_19 | _ctrl_T_21 | _ctrl_T_23 | _ctrl_T_25
    | _ctrl_T_27 | _ctrl_T_29 | _ctrl_T_31 | _ctrl_T_33 | _ctrl_T_35 | _GEN_7;
  wire       _GEN_11 =
    _ctrl_T_81 | _ctrl_T_83 | _ctrl_T_85 | _ctrl_T_87 | _ctrl_T_89 | _ctrl_T_632;
  wire       _GEN_12 =
    _ctrl_T_59 | _ctrl_T_61 | _ctrl_T_63 | _ctrl_T_65 | _ctrl_T_67 | _ctrl_T_69
    | _ctrl_T_71 | _ctrl_T_73;
  wire       _GEN_13 =
    _ctrl_T_37 | _ctrl_T_39 | _ctrl_T_41 | _ctrl_T_43 | _ctrl_T_45 | _ctrl_T_47
    | _ctrl_T_49 | _ctrl_T_51 | _ctrl_T_53 | _ctrl_T_55 | _ctrl_T_57 | _GEN_12;
  wire       _GEN_14 =
    _ctrl_T_37 | _ctrl_T_39 | _ctrl_T_41 | _ctrl_T_43 | _ctrl_T_45 | _ctrl_T_47
    | _ctrl_T_49 | _ctrl_T_51 | _ctrl_T_53 | _GEN_2;
  wire [4:0] _io_rd_output =
    ~(_ctrl_T_1 | _ctrl_T_3 | _ctrl_T_5 | _ctrl_T_7 | _ctrl_T_9 | _ctrl_T_11 | _ctrl_T_13
      | _ctrl_T_15 | _ctrl_T_17 | _ctrl_T_19 | _ctrl_T_21 | _ctrl_T_23 | _ctrl_T_25
      | _ctrl_T_27 | _ctrl_T_29 | _ctrl_T_31 | _ctrl_T_33 | _ctrl_T_35 | _ctrl_T_37
      | _ctrl_T_39 | _ctrl_T_41 | _ctrl_T_43 | _ctrl_T_45 | _ctrl_T_47 | _ctrl_T_49
      | _ctrl_T_51 | _ctrl_T_53 | _ctrl_T_55 | _ctrl_T_57 | _ctrl_T_59 | _ctrl_T_61
      | _ctrl_T_63 | _ctrl_T_65 | _ctrl_T_67 | _ctrl_T_69 | _ctrl_T_71 | _ctrl_T_73
      | _ctrl_T_75 | _ctrl_T_77) & _ctrl_T_548
      ? 5'h1
      : io_inst[4:0];
  Imm_Gen imm_gen (
    .io_inst     (io_inst),
    .io_imm_type
      (_GEN_9
         ? 4'h0
         : _ctrl_T_37 | _ctrl_T_39 | _ctrl_T_41
             ? 4'h1
             : _ctrl_T_43 | _ctrl_T_45 | _ctrl_T_47
                 ? 4'h3
                 : _GEN_3
                     ? 4'h2
                     : _GEN_2
                         ? 4'h5
                         : _GEN_12
                             ? 4'h3
                             : _ctrl_T_75 ? 4'h4 : _GEN_0 ? 4'h6 : {1'h0, _GEN_11, 2'h0}),
    .io_imm      (io_imm)
  );
  assign io_rj = io_inst[9:5];
  assign io_rj_valid =
    _GEN_4 | ~_GEN_2
    & (_ctrl_T_59 | _ctrl_T_61 | _ctrl_T_63 | _ctrl_T_65 | _ctrl_T_67 | _ctrl_T_69
       | _GEN_1 | ~_GEN_0 & (_GEN | _ctrl_T_632));
  assign io_rk =
    _GEN_10 | ~_GEN_6 & (_GEN_5 | ~_GEN & ~_ctrl_T_632) ? io_inst[14:10] : io_inst[4:0];
  assign io_rk_valid = _GEN_9 | ~_GEN_7 & (_GEN_6 | ~_GEN_5 & (_GEN | _ctrl_T_632));
  assign io_rd = _io_rd_output;
  assign io_rd_valid =
    (_GEN_10 | ~_GEN_6 & (_GEN_1 | ~_ctrl_T_77 & _ctrl_T_548)) & (|_io_rd_output);
  assign io_alu_op =
    _ctrl_T_1
      ? 5'h0
      : _ctrl_T_3
          ? 5'h1
          : _ctrl_T_5
              ? 5'h2
              : _ctrl_T_7
                  ? 5'h3
                  : _ctrl_T_9
                      ? 5'h4
                      : _ctrl_T_11
                          ? 5'h5
                          : _ctrl_T_13
                              ? 5'h6
                              : _ctrl_T_15
                                  ? 5'h7
                                  : _ctrl_T_17
                                      ? 5'h8
                                      : _ctrl_T_19
                                          ? 5'h9
                                          : _ctrl_T_21
                                              ? 5'hA
                                              : _ctrl_T_23
                                                  ? 5'hB
                                                  : _ctrl_T_25
                                                      ? 5'hC
                                                      : _ctrl_T_27
                                                          ? 5'hD
                                                          : _ctrl_T_29
                                                              ? 5'hE
                                                              : _ctrl_T_31
                                                                  ? 5'hF
                                                                  : _ctrl_T_33
                                                                      ? 5'h10
                                                                      : _ctrl_T_35
                                                                          ? 5'h11
                                                                          : _ctrl_T_37
                                                                              ? 5'h8
                                                                              : _ctrl_T_39
                                                                                  ? 5'h9
                                                                                  : _ctrl_T_41
                                                                                      ? 5'hA
                                                                                      : _ctrl_T_43
                                                                                          ? 5'h2
                                                                                          : _ctrl_T_45
                                                                                              ? 5'h3
                                                                                              : _ctrl_T_47
                                                                                                  ? 5'h0
                                                                                                  : _ctrl_T_49
                                                                                                      ? 5'h5
                                                                                                      : _ctrl_T_51
                                                                                                          ? 5'h6
                                                                                                          : _ctrl_T_53
                                                                                                              ? 5'h7
                                                                                                              : 5'h0;
  assign io_alu_rs1_sel =
    _GEN_4
      ? 2'h0
      : _ctrl_T_55
          ? 2'h2
          : _ctrl_T_57
              ? 2'h1
              : _GEN_12
                  ? 2'h0
                  : _ctrl_T_75
                      ? 2'h1
                      : _ctrl_T_77 ? 2'h0 : _ctrl_T_548 ? 2'h1 : {~_GEN_11, 1'h0};
  assign io_alu_rs2_sel =
    _GEN_9
      ? 2'h0
      : _GEN_13 | ~(_ctrl_T_75 | ~(_ctrl_T_77 | ~(_ctrl_T_548 | ~_GEN_11))) ? 2'h1 : 2'h2;
  assign io_br_type =
    _ctrl_T_1 | _ctrl_T_3 | _ctrl_T_5 | _ctrl_T_7 | _ctrl_T_9 | _ctrl_T_11 | _ctrl_T_13
    | _ctrl_T_15 | _ctrl_T_17 | _ctrl_T_19 | _ctrl_T_21 | _ctrl_T_23 | _ctrl_T_25
    | _ctrl_T_27 | _ctrl_T_29 | _ctrl_T_31 | _ctrl_T_33 | _ctrl_T_35 | _GEN_13
      ? 4'h0
      : _ctrl_T_75
          ? 4'h1
          : _ctrl_T_77
              ? 4'h2
              : _ctrl_T_548
                  ? 4'h3
                  : _ctrl_T_81
                      ? 4'h4
                      : _ctrl_T_83
                          ? 4'h5
                          : _ctrl_T_85
                              ? 4'h6
                              : _ctrl_T_87
                                  ? 4'h7
                                  : _ctrl_T_89 ? 4'h8 : _ctrl_T_632 ? 4'h9 : 4'h0;
  assign io_mem_type =
    _ctrl_T_1 | _ctrl_T_3 | _ctrl_T_5 | _ctrl_T_7 | _ctrl_T_9 | _ctrl_T_11 | _ctrl_T_13
    | _ctrl_T_15 | _ctrl_T_17 | _ctrl_T_19 | _ctrl_T_21 | _ctrl_T_23 | _ctrl_T_25
    | _ctrl_T_27 | _ctrl_T_29 | _ctrl_T_31 | _ctrl_T_33 | _ctrl_T_35 | _GEN_14
      ? 5'h0
      : _ctrl_T_59
          ? 5'h8
          : _ctrl_T_61
              ? 5'h9
              : _ctrl_T_63
                  ? 5'hA
                  : _ctrl_T_65
                      ? 5'h10
                      : _ctrl_T_67
                          ? 5'h11
                          : _ctrl_T_69
                              ? 5'h12
                              : _ctrl_T_71 ? 5'hC : _ctrl_T_73 ? 5'hD : 5'h0;
  assign io_fu_id =
    _ctrl_T_1 | _ctrl_T_3 | _ctrl_T_5 | _ctrl_T_7 | _ctrl_T_9 | _ctrl_T_11 | _ctrl_T_13
    | _ctrl_T_15 | _ctrl_T_17 | _ctrl_T_19 | _ctrl_T_21
      ? 2'h0
      : _GEN_8
          ? 2'h3
          : _GEN_14
              ? 2'h0
              : _GEN_12 ? 2'h2 : {1'h0, _ctrl_T_75 | _ctrl_T_77 | _ctrl_T_548 | _GEN_11};
endmodule

