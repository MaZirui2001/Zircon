// Generated by CIRCT firtool-1.56.1
module Prev_Decode(
  input  [31:0] io_insts_pack_IF_0_pc,
                io_insts_pack_IF_0_inst,
  input         io_insts_pack_IF_0_inst_valid,
                io_insts_pack_IF_0_predict_jump,
  input  [31:0] io_insts_pack_IF_0_pred_npc,
  input         io_insts_pack_IF_0_pred_valid,
  input  [31:0] io_insts_pack_IF_1_pc,
                io_insts_pack_IF_1_inst,
  input         io_insts_pack_IF_1_inst_valid,
                io_insts_pack_IF_1_predict_jump,
  input  [31:0] io_insts_pack_IF_1_pred_npc,
  input         io_insts_pack_IF_1_pred_valid,
  input  [31:0] io_insts_pack_IF_2_pc,
                io_insts_pack_IF_2_inst,
  input         io_insts_pack_IF_2_inst_valid,
                io_insts_pack_IF_2_predict_jump,
  input  [31:0] io_insts_pack_IF_2_pred_npc,
  input         io_insts_pack_IF_2_pred_valid,
  input  [31:0] io_insts_pack_IF_3_pc,
                io_insts_pack_IF_3_inst,
  input         io_insts_pack_IF_3_inst_valid,
                io_insts_pack_IF_3_predict_jump,
  input  [31:0] io_insts_pack_IF_3_pred_npc,
  input         io_insts_pack_IF_3_pred_valid,
  output [31:0] io_insts_pack_PD_0_pc,
                io_insts_pack_PD_0_inst,
  output        io_insts_pack_PD_0_inst_valid,
                io_insts_pack_PD_0_predict_jump,
  output [31:0] io_insts_pack_PD_0_pred_npc,
                io_insts_pack_PD_1_pc,
                io_insts_pack_PD_1_inst,
  output        io_insts_pack_PD_1_inst_valid,
                io_insts_pack_PD_1_predict_jump,
  output [31:0] io_insts_pack_PD_1_pred_npc,
                io_insts_pack_PD_2_pc,
                io_insts_pack_PD_2_inst,
  output        io_insts_pack_PD_2_inst_valid,
                io_insts_pack_PD_2_predict_jump,
  output [31:0] io_insts_pack_PD_2_pred_npc,
                io_insts_pack_PD_3_pc,
                io_insts_pack_PD_3_inst,
  output        io_insts_pack_PD_3_inst_valid,
                io_insts_pack_PD_3_predict_jump,
  output [31:0] io_insts_pack_PD_3_pred_npc,
  output        io_pred_fix,
  output [31:0] io_pred_fix_target,
  output        io_pred_fix_is_bl,
  output [31:0] io_pred_fix_pc_plus_4
);

  wire [31:0] inst_pack_pd_3_pred_npc;
  wire        pred_fix_3;
  wire [31:0] inst_pack_pd_2_pred_npc;
  wire        pred_fix_2;
  wire [31:0] inst_pack_pd_1_pred_npc;
  wire        pred_fix_1;
  wire [31:0] inst_pack_pd_0_pred_npc;
  wire        pred_fix_0;
  reg  [31:0] casez_tmp;
  reg         casez_tmp_0;
  reg  [31:0] casez_tmp_1;
  wire [1:0]  pred_index = pred_fix_0 ? 2'h0 : pred_fix_1 ? 2'h1 : {1'h1, ~pred_fix_2};
  always_comb begin
    casez (pred_index)
      2'b00:
        casez_tmp = inst_pack_pd_0_pred_npc;
      2'b01:
        casez_tmp = inst_pack_pd_1_pred_npc;
      2'b10:
        casez_tmp = inst_pack_pd_2_pred_npc;
      default:
        casez_tmp = inst_pack_pd_3_pred_npc;
    endcase
  end // always_comb
  wire        _GEN = io_insts_pack_IF_0_inst[31:30] == 2'h1;
  wire [1:0]  jump_type_0 =
    _GEN
      ? (io_insts_pack_IF_0_inst[29:26] == 4'h4 | io_insts_pack_IF_0_inst[29:26] == 4'h5
           ? 2'h1
           : {io_insts_pack_IF_0_inst[29:26] > 4'h5
                & io_insts_pack_IF_0_inst[29:28] != 2'h3,
              1'h0})
      : 2'h0;
  wire        _GEN_0 = io_insts_pack_IF_1_inst[31:30] == 2'h1;
  wire [1:0]  jump_type_1 =
    _GEN_0
      ? (io_insts_pack_IF_1_inst[29:26] == 4'h4 | io_insts_pack_IF_1_inst[29:26] == 4'h5
           ? 2'h1
           : {io_insts_pack_IF_1_inst[29:26] > 4'h5
                & io_insts_pack_IF_1_inst[29:28] != 2'h3,
              1'h0})
      : 2'h0;
  wire        _GEN_1 = io_insts_pack_IF_2_inst[31:30] == 2'h1;
  wire [1:0]  jump_type_2 =
    _GEN_1
      ? (io_insts_pack_IF_2_inst[29:26] == 4'h4 | io_insts_pack_IF_2_inst[29:26] == 4'h5
           ? 2'h1
           : {io_insts_pack_IF_2_inst[29:26] > 4'h5
                & io_insts_pack_IF_2_inst[29:28] != 2'h3,
              1'h0})
      : 2'h0;
  wire        _GEN_2 = io_insts_pack_IF_3_inst[31:30] == 2'h1;
  wire [1:0]  jump_type_3 =
    _GEN_2
      ? (io_insts_pack_IF_3_inst[29:26] == 4'h4 | io_insts_pack_IF_3_inst[29:26] == 4'h5
           ? 2'h1
           : {io_insts_pack_IF_3_inst[29:26] > 4'h5
                & io_insts_pack_IF_3_inst[29:28] != 2'h3,
              1'h0})
      : 2'h0;
  wire [31:0] _inst_pack_pd_0_pred_npc_T_16 = io_insts_pack_IF_0_pc + 32'h4;
  wire [31:0] _inst_pack_pd_1_pred_npc_T_16 = io_insts_pack_IF_1_pc + 32'h4;
  wire [31:0] _inst_pack_pd_2_pred_npc_T_16 = io_insts_pack_IF_2_pc + 32'h4;
  wire [31:0] _inst_pack_pd_3_pred_npc_T_16 = io_insts_pack_IF_3_pc + 32'h4;
  always_comb begin
    casez (pred_index)
      2'b00:
        casez_tmp_0 = _GEN & io_insts_pack_IF_0_inst[29:26] == 4'h5;
      2'b01:
        casez_tmp_0 = _GEN_0 & io_insts_pack_IF_1_inst[29:26] == 4'h5;
      2'b10:
        casez_tmp_0 = _GEN_1 & io_insts_pack_IF_2_inst[29:26] == 4'h5;
      default:
        casez_tmp_0 = _GEN_2 & io_insts_pack_IF_3_inst[29:26] == 4'h5;
    endcase
  end // always_comb
  always_comb begin
    casez (pred_index)
      2'b00:
        casez_tmp_1 = _inst_pack_pd_0_pred_npc_T_16;
      2'b01:
        casez_tmp_1 = _inst_pack_pd_1_pred_npc_T_16;
      2'b10:
        casez_tmp_1 = _inst_pack_pd_2_pred_npc_T_16;
      default:
        casez_tmp_1 = _inst_pack_pd_3_pred_npc_T_16;
    endcase
  end // always_comb
  wire        _GEN_3 = jump_type_0 == 2'h1;
  wire        _GEN_4 = jump_type_0 == 2'h2;
  assign pred_fix_0 =
    io_insts_pack_IF_0_inst_valid
    & (_GEN_3
         ? ~io_insts_pack_IF_0_predict_jump
           | inst_pack_pd_0_pred_npc != io_insts_pack_IF_0_pred_npc
         : _GEN_4
           & (io_insts_pack_IF_0_pred_valid
                ? io_insts_pack_IF_0_predict_jump
                  & inst_pack_pd_0_pred_npc != io_insts_pack_IF_0_pred_npc
                : io_insts_pack_IF_0_inst[25]));
  wire        inst_pack_pd_0_predict_jump =
    io_insts_pack_IF_0_inst_valid
      ? _GEN_3
        | (_GEN_4 & ~io_insts_pack_IF_0_pred_valid
             ? io_insts_pack_IF_0_inst[25]
             : io_insts_pack_IF_0_predict_jump)
      : io_insts_pack_IF_0_predict_jump;
  assign inst_pack_pd_0_pred_npc =
    io_insts_pack_IF_0_inst_valid
      ? (_GEN_3
           ? io_insts_pack_IF_0_pc
             + {{4{io_insts_pack_IF_0_inst[9]}},
                io_insts_pack_IF_0_inst[9:0],
                io_insts_pack_IF_0_inst[25:10],
                2'h0}
           : _GEN_4
               ? (io_insts_pack_IF_0_pred_valid
                    ? (io_insts_pack_IF_0_predict_jump
                         ? io_insts_pack_IF_0_pc
                           + {{14{io_insts_pack_IF_0_inst[25]}},
                              io_insts_pack_IF_0_inst[25:10],
                              2'h0}
                         : io_insts_pack_IF_0_pred_npc)
                    : io_insts_pack_IF_0_inst[25]
                        ? io_insts_pack_IF_0_pc
                          + {{14{io_insts_pack_IF_0_inst[25]}},
                             io_insts_pack_IF_0_inst[25:10],
                             2'h0}
                        : _inst_pack_pd_0_pred_npc_T_16)
               : io_insts_pack_IF_0_pred_npc)
      : io_insts_pack_IF_0_pred_npc;
  wire        _GEN_5 = jump_type_1 == 2'h1;
  wire        _GEN_6 = jump_type_1 == 2'h2;
  assign pred_fix_1 =
    io_insts_pack_IF_1_inst_valid
    & (_GEN_5
         ? ~io_insts_pack_IF_1_predict_jump
           | inst_pack_pd_1_pred_npc != io_insts_pack_IF_1_pred_npc
         : _GEN_6
           & (io_insts_pack_IF_1_pred_valid
                ? io_insts_pack_IF_1_predict_jump
                  & inst_pack_pd_1_pred_npc != io_insts_pack_IF_1_pred_npc
                : io_insts_pack_IF_1_inst[25]));
  wire        inst_pack_pd_1_predict_jump =
    io_insts_pack_IF_1_inst_valid
      ? _GEN_5
        | (_GEN_6 & ~io_insts_pack_IF_1_pred_valid
             ? io_insts_pack_IF_1_inst[25]
             : io_insts_pack_IF_1_predict_jump)
      : io_insts_pack_IF_1_predict_jump;
  assign inst_pack_pd_1_pred_npc =
    io_insts_pack_IF_1_inst_valid
      ? (_GEN_5
           ? io_insts_pack_IF_1_pc
             + {{4{io_insts_pack_IF_1_inst[9]}},
                io_insts_pack_IF_1_inst[9:0],
                io_insts_pack_IF_1_inst[25:10],
                2'h0}
           : _GEN_6
               ? (io_insts_pack_IF_1_pred_valid
                    ? (io_insts_pack_IF_1_predict_jump
                         ? io_insts_pack_IF_1_pc
                           + {{14{io_insts_pack_IF_1_inst[25]}},
                              io_insts_pack_IF_1_inst[25:10],
                              2'h0}
                         : io_insts_pack_IF_1_pred_npc)
                    : io_insts_pack_IF_1_inst[25]
                        ? io_insts_pack_IF_1_pc
                          + {{14{io_insts_pack_IF_1_inst[25]}},
                             io_insts_pack_IF_1_inst[25:10],
                             2'h0}
                        : _inst_pack_pd_1_pred_npc_T_16)
               : io_insts_pack_IF_1_pred_npc)
      : io_insts_pack_IF_1_pred_npc;
  wire        _GEN_7 = jump_type_2 == 2'h1;
  wire        _GEN_8 = jump_type_2 == 2'h2;
  assign pred_fix_2 =
    io_insts_pack_IF_2_inst_valid
    & (_GEN_7
         ? ~io_insts_pack_IF_2_predict_jump
           | inst_pack_pd_2_pred_npc != io_insts_pack_IF_2_pred_npc
         : _GEN_8
           & (io_insts_pack_IF_2_pred_valid
                ? io_insts_pack_IF_2_predict_jump
                  & inst_pack_pd_2_pred_npc != io_insts_pack_IF_2_pred_npc
                : io_insts_pack_IF_2_inst[25]));
  wire        inst_pack_pd_2_predict_jump =
    io_insts_pack_IF_2_inst_valid
      ? _GEN_7
        | (_GEN_8 & ~io_insts_pack_IF_2_pred_valid
             ? io_insts_pack_IF_2_inst[25]
             : io_insts_pack_IF_2_predict_jump)
      : io_insts_pack_IF_2_predict_jump;
  assign inst_pack_pd_2_pred_npc =
    io_insts_pack_IF_2_inst_valid
      ? (_GEN_7
           ? io_insts_pack_IF_2_pc
             + {{4{io_insts_pack_IF_2_inst[9]}},
                io_insts_pack_IF_2_inst[9:0],
                io_insts_pack_IF_2_inst[25:10],
                2'h0}
           : _GEN_8
               ? (io_insts_pack_IF_2_pred_valid
                    ? (io_insts_pack_IF_2_predict_jump
                         ? io_insts_pack_IF_2_pc
                           + {{14{io_insts_pack_IF_2_inst[25]}},
                              io_insts_pack_IF_2_inst[25:10],
                              2'h0}
                         : io_insts_pack_IF_2_pred_npc)
                    : io_insts_pack_IF_2_inst[25]
                        ? io_insts_pack_IF_2_pc
                          + {{14{io_insts_pack_IF_2_inst[25]}},
                             io_insts_pack_IF_2_inst[25:10],
                             2'h0}
                        : _inst_pack_pd_2_pred_npc_T_16)
               : io_insts_pack_IF_2_pred_npc)
      : io_insts_pack_IF_2_pred_npc;
  wire        _GEN_9 = jump_type_3 == 2'h1;
  wire        _GEN_10 = jump_type_3 == 2'h2;
  assign pred_fix_3 =
    io_insts_pack_IF_3_inst_valid
    & (_GEN_9
         ? ~io_insts_pack_IF_3_predict_jump
           | inst_pack_pd_3_pred_npc != io_insts_pack_IF_3_pred_npc
         : _GEN_10
           & (io_insts_pack_IF_3_pred_valid
                ? io_insts_pack_IF_3_predict_jump
                  & inst_pack_pd_3_pred_npc != io_insts_pack_IF_3_pred_npc
                : io_insts_pack_IF_3_inst[25]));
  assign inst_pack_pd_3_pred_npc =
    io_insts_pack_IF_3_inst_valid
      ? (_GEN_9
           ? io_insts_pack_IF_3_pc
             + {{4{io_insts_pack_IF_3_inst[9]}},
                io_insts_pack_IF_3_inst[9:0],
                io_insts_pack_IF_3_inst[25:10],
                2'h0}
           : _GEN_10
               ? (io_insts_pack_IF_3_pred_valid
                    ? (io_insts_pack_IF_3_predict_jump
                         ? io_insts_pack_IF_3_pc
                           + {{14{io_insts_pack_IF_3_inst[25]}},
                              io_insts_pack_IF_3_inst[25:10],
                              2'h0}
                         : io_insts_pack_IF_3_pred_npc)
                    : io_insts_pack_IF_3_inst[25]
                        ? io_insts_pack_IF_3_pc
                          + {{14{io_insts_pack_IF_3_inst[25]}},
                             io_insts_pack_IF_3_inst[25:10],
                             2'h0}
                        : _inst_pack_pd_3_pred_npc_T_16)
               : io_insts_pack_IF_3_pred_npc)
      : io_insts_pack_IF_3_pred_npc;
  wire        inst_valid_1 =
    io_insts_pack_IF_0_inst_valid & io_insts_pack_IF_1_inst_valid
    & ~inst_pack_pd_0_predict_jump;
  wire        inst_valid_2 =
    inst_valid_1 & io_insts_pack_IF_2_inst_valid & ~inst_pack_pd_1_predict_jump;
  assign io_insts_pack_PD_0_pc = io_insts_pack_IF_0_pc;
  assign io_insts_pack_PD_0_inst = io_insts_pack_IF_0_inst;
  assign io_insts_pack_PD_0_inst_valid = io_insts_pack_IF_0_inst_valid;
  assign io_insts_pack_PD_0_predict_jump = inst_pack_pd_0_predict_jump;
  assign io_insts_pack_PD_0_pred_npc = inst_pack_pd_0_pred_npc;
  assign io_insts_pack_PD_1_pc = io_insts_pack_IF_1_pc;
  assign io_insts_pack_PD_1_inst = io_insts_pack_IF_1_inst;
  assign io_insts_pack_PD_1_inst_valid = inst_valid_1;
  assign io_insts_pack_PD_1_predict_jump = inst_pack_pd_1_predict_jump;
  assign io_insts_pack_PD_1_pred_npc = inst_pack_pd_1_pred_npc;
  assign io_insts_pack_PD_2_pc = io_insts_pack_IF_2_pc;
  assign io_insts_pack_PD_2_inst = io_insts_pack_IF_2_inst;
  assign io_insts_pack_PD_2_inst_valid = inst_valid_2;
  assign io_insts_pack_PD_2_predict_jump = inst_pack_pd_2_predict_jump;
  assign io_insts_pack_PD_2_pred_npc = inst_pack_pd_2_pred_npc;
  assign io_insts_pack_PD_3_pc = io_insts_pack_IF_3_pc;
  assign io_insts_pack_PD_3_inst = io_insts_pack_IF_3_inst;
  assign io_insts_pack_PD_3_inst_valid =
    inst_valid_2 & io_insts_pack_IF_3_inst_valid & ~inst_pack_pd_2_predict_jump;
  assign io_insts_pack_PD_3_predict_jump =
    io_insts_pack_IF_3_inst_valid
      ? _GEN_9
        | (_GEN_10 & ~io_insts_pack_IF_3_pred_valid
             ? io_insts_pack_IF_3_inst[25]
             : io_insts_pack_IF_3_predict_jump)
      : io_insts_pack_IF_3_predict_jump;
  assign io_insts_pack_PD_3_pred_npc = inst_pack_pd_3_pred_npc;
  assign io_pred_fix = |{pred_fix_3, pred_fix_2, pred_fix_1, pred_fix_0};
  assign io_pred_fix_target = casez_tmp;
  assign io_pred_fix_is_bl = casez_tmp_0;
  assign io_pred_fix_pc_plus_4 = casez_tmp_1;
endmodule

