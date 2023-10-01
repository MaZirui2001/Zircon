module Reg_Rename(
  input        clock,
               reset,
  input  [4:0] io_rj_0,
               io_rj_1,
               io_rj_2,
               io_rj_3,
               io_rk_0,
               io_rk_1,
               io_rk_2,
               io_rk_3,
               io_rd_0,
               io_rd_1,
               io_rd_2,
               io_rd_3,
  input        io_rd_valid_0,
               io_rd_valid_1,
               io_rd_valid_2,
               io_rd_valid_3,
               io_rename_en_0,
               io_rename_en_1,
               io_rename_en_2,
               io_rename_en_3,
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
               io_arch_rat_0,
               io_arch_rat_1,
               io_arch_rat_2,
               io_arch_rat_3,
               io_arch_rat_4,
               io_arch_rat_5,
               io_arch_rat_6,
               io_arch_rat_7,
               io_arch_rat_8,
               io_arch_rat_9,
               io_arch_rat_10,
               io_arch_rat_11,
               io_arch_rat_12,
               io_arch_rat_13,
               io_arch_rat_14,
               io_arch_rat_15,
               io_arch_rat_16,
               io_arch_rat_17,
               io_arch_rat_18,
               io_arch_rat_19,
               io_arch_rat_20,
               io_arch_rat_21,
               io_arch_rat_22,
               io_arch_rat_23,
               io_arch_rat_24,
               io_arch_rat_25,
               io_arch_rat_26,
               io_arch_rat_27,
               io_arch_rat_28,
               io_arch_rat_29,
               io_arch_rat_30,
               io_arch_rat_31,
               io_arch_rat_32,
               io_arch_rat_33,
               io_arch_rat_34,
               io_arch_rat_35,
               io_arch_rat_36,
               io_arch_rat_37,
               io_arch_rat_38,
               io_arch_rat_39,
               io_arch_rat_40,
               io_arch_rat_41,
               io_arch_rat_42,
               io_arch_rat_43,
               io_arch_rat_44,
               io_arch_rat_45,
               io_arch_rat_46,
               io_arch_rat_47,
               io_arch_rat_48,
               io_arch_rat_49,
               io_arch_rat_50,
               io_arch_rat_51,
               io_arch_rat_52,
               io_arch_rat_53,
               io_arch_rat_54,
               io_arch_rat_55,
               io_arch_rat_56,
               io_arch_rat_57,
               io_arch_rat_58,
               io_arch_rat_59,
               io_arch_rat_60,
               io_arch_rat_61,
               io_arch_rat_62,
               io_arch_rat_63,
  input  [5:0] io_head_arch_0,
               io_head_arch_1,
               io_head_arch_2,
               io_head_arch_3,
  output [5:0] io_prj_0,
               io_prj_1,
               io_prj_2,
               io_prj_3,
               io_prk_0,
               io_prk_1,
               io_prk_2,
               io_prk_3,
               io_pprd_0,
               io_pprd_1,
               io_pprd_2,
               io_pprd_3,
  output       io_free_list_empty
);

  wire [5:0] _free_list_io_alloc_preg_0;
  wire [5:0] _free_list_io_alloc_preg_1;
  wire [5:0] _free_list_io_alloc_preg_2;
  wire [5:0] _free_list_io_alloc_preg_3;
  wire [5:0] _crat_io_prj_1;
  wire [5:0] _crat_io_prj_2;
  wire [5:0] _crat_io_prj_3;
  wire [5:0] _crat_io_prk_1;
  wire [5:0] _crat_io_prk_2;
  wire [5:0] _crat_io_prk_3;
  wire [5:0] _crat_io_pprd_1;
  wire [5:0] _crat_io_pprd_2;
  wire [5:0] _crat_io_pprd_3;
  wire       _rd_valid_temp_2_T = io_rd_2 == io_rd_3;
  wire       _rd_valid_temp_1_T = io_rd_1 == io_rd_2;
  wire       _rd_valid_temp_1_T_4 = io_rd_1 == io_rd_3;
  wire       _rd_valid_temp_0_T = io_rd_0 == io_rd_1;
  wire       _rd_valid_temp_0_T_4 = io_rd_0 == io_rd_2;
  wire       _rd_valid_temp_0_T_8 = io_rd_0 == io_rd_3;
  CRat crat (
    .clock           (clock),
    .reset           (reset),
    .io_rj_0         (io_rj_0),
    .io_rj_1         (io_rj_1),
    .io_rj_2         (io_rj_2),
    .io_rj_3         (io_rj_3),
    .io_rk_0         (io_rk_0),
    .io_rk_1         (io_rk_1),
    .io_rk_2         (io_rk_2),
    .io_rk_3         (io_rk_3),
    .io_rd_0         (io_rd_0),
    .io_rd_1         (io_rd_1),
    .io_rd_2         (io_rd_2),
    .io_rd_3         (io_rd_3),
    .io_rd_valid_0
      (io_rd_valid_0 & ~(_rd_valid_temp_0_T & io_rd_valid_1)
       & ~(_rd_valid_temp_0_T_4 & io_rd_valid_2)
       & ~(_rd_valid_temp_0_T_8 & io_rd_valid_3)),
    .io_rd_valid_1
      (io_rd_valid_1 & ~(_rd_valid_temp_1_T & io_rd_valid_2)
       & ~(_rd_valid_temp_1_T_4 & io_rd_valid_3)),
    .io_rd_valid_2   (io_rd_valid_2 & ~(_rd_valid_temp_2_T & io_rd_valid_3)),
    .io_rd_valid_3   (io_rd_valid_3),
    .io_alloc_preg_0 (_free_list_io_alloc_preg_0),
    .io_alloc_preg_1 (_free_list_io_alloc_preg_1),
    .io_alloc_preg_2 (_free_list_io_alloc_preg_2),
    .io_alloc_preg_3 (_free_list_io_alloc_preg_3),
    .io_arch_rat_0   (io_arch_rat_0),
    .io_arch_rat_1   (io_arch_rat_1),
    .io_arch_rat_2   (io_arch_rat_2),
    .io_arch_rat_3   (io_arch_rat_3),
    .io_arch_rat_4   (io_arch_rat_4),
    .io_arch_rat_5   (io_arch_rat_5),
    .io_arch_rat_6   (io_arch_rat_6),
    .io_arch_rat_7   (io_arch_rat_7),
    .io_arch_rat_8   (io_arch_rat_8),
    .io_arch_rat_9   (io_arch_rat_9),
    .io_arch_rat_10  (io_arch_rat_10),
    .io_arch_rat_11  (io_arch_rat_11),
    .io_arch_rat_12  (io_arch_rat_12),
    .io_arch_rat_13  (io_arch_rat_13),
    .io_arch_rat_14  (io_arch_rat_14),
    .io_arch_rat_15  (io_arch_rat_15),
    .io_arch_rat_16  (io_arch_rat_16),
    .io_arch_rat_17  (io_arch_rat_17),
    .io_arch_rat_18  (io_arch_rat_18),
    .io_arch_rat_19  (io_arch_rat_19),
    .io_arch_rat_20  (io_arch_rat_20),
    .io_arch_rat_21  (io_arch_rat_21),
    .io_arch_rat_22  (io_arch_rat_22),
    .io_arch_rat_23  (io_arch_rat_23),
    .io_arch_rat_24  (io_arch_rat_24),
    .io_arch_rat_25  (io_arch_rat_25),
    .io_arch_rat_26  (io_arch_rat_26),
    .io_arch_rat_27  (io_arch_rat_27),
    .io_arch_rat_28  (io_arch_rat_28),
    .io_arch_rat_29  (io_arch_rat_29),
    .io_arch_rat_30  (io_arch_rat_30),
    .io_arch_rat_31  (io_arch_rat_31),
    .io_arch_rat_32  (io_arch_rat_32),
    .io_arch_rat_33  (io_arch_rat_33),
    .io_arch_rat_34  (io_arch_rat_34),
    .io_arch_rat_35  (io_arch_rat_35),
    .io_arch_rat_36  (io_arch_rat_36),
    .io_arch_rat_37  (io_arch_rat_37),
    .io_arch_rat_38  (io_arch_rat_38),
    .io_arch_rat_39  (io_arch_rat_39),
    .io_arch_rat_40  (io_arch_rat_40),
    .io_arch_rat_41  (io_arch_rat_41),
    .io_arch_rat_42  (io_arch_rat_42),
    .io_arch_rat_43  (io_arch_rat_43),
    .io_arch_rat_44  (io_arch_rat_44),
    .io_arch_rat_45  (io_arch_rat_45),
    .io_arch_rat_46  (io_arch_rat_46),
    .io_arch_rat_47  (io_arch_rat_47),
    .io_arch_rat_48  (io_arch_rat_48),
    .io_arch_rat_49  (io_arch_rat_49),
    .io_arch_rat_50  (io_arch_rat_50),
    .io_arch_rat_51  (io_arch_rat_51),
    .io_arch_rat_52  (io_arch_rat_52),
    .io_arch_rat_53  (io_arch_rat_53),
    .io_arch_rat_54  (io_arch_rat_54),
    .io_arch_rat_55  (io_arch_rat_55),
    .io_arch_rat_56  (io_arch_rat_56),
    .io_arch_rat_57  (io_arch_rat_57),
    .io_arch_rat_58  (io_arch_rat_58),
    .io_arch_rat_59  (io_arch_rat_59),
    .io_arch_rat_60  (io_arch_rat_60),
    .io_arch_rat_61  (io_arch_rat_61),
    .io_arch_rat_62  (io_arch_rat_62),
    .io_arch_rat_63  (io_arch_rat_63),
    .io_predict_fail (io_predict_fail),
    .io_prj_0        (io_prj_0),
    .io_prj_1        (_crat_io_prj_1),
    .io_prj_2        (_crat_io_prj_2),
    .io_prj_3        (_crat_io_prj_3),
    .io_prk_0        (io_prk_0),
    .io_prk_1        (_crat_io_prk_1),
    .io_prk_2        (_crat_io_prk_2),
    .io_prk_3        (_crat_io_prk_3),
    .io_pprd_0       (io_pprd_0),
    .io_pprd_1       (_crat_io_pprd_1),
    .io_pprd_2       (_crat_io_pprd_2),
    .io_pprd_3       (_crat_io_pprd_3)
  );
  Free_List free_list (
    .clock                  (clock),
    .reset                  (reset),
    .io_rd_valid_0          (io_rd_valid_0),
    .io_rd_valid_1          (io_rd_valid_1),
    .io_rd_valid_2          (io_rd_valid_2),
    .io_rd_valid_3          (io_rd_valid_3),
    .io_commit_en_0         (io_commit_en_0),
    .io_commit_en_1         (io_commit_en_1),
    .io_commit_en_2         (io_commit_en_2),
    .io_commit_en_3         (io_commit_en_3),
    .io_commit_pprd_valid_0 (io_commit_pprd_valid_0),
    .io_commit_pprd_valid_1 (io_commit_pprd_valid_1),
    .io_commit_pprd_valid_2 (io_commit_pprd_valid_2),
    .io_commit_pprd_valid_3 (io_commit_pprd_valid_3),
    .io_commit_pprd_0       (io_commit_pprd_0),
    .io_commit_pprd_1       (io_commit_pprd_1),
    .io_commit_pprd_2       (io_commit_pprd_2),
    .io_commit_pprd_3       (io_commit_pprd_3),
    .io_predict_fail        (io_predict_fail),
    .io_head_arch_0         (io_head_arch_0),
    .io_head_arch_1         (io_head_arch_1),
    .io_head_arch_2         (io_head_arch_2),
    .io_head_arch_3         (io_head_arch_3),
    .io_alloc_preg_0        (_free_list_io_alloc_preg_0),
    .io_alloc_preg_1        (_free_list_io_alloc_preg_1),
    .io_alloc_preg_2        (_free_list_io_alloc_preg_2),
    .io_alloc_preg_3        (_free_list_io_alloc_preg_3),
    .io_empty               (io_free_list_empty)
  );
  assign io_prj_1 =
    io_rd_valid_0 & io_rd_0 == io_rj_1 ? _free_list_io_alloc_preg_0 : _crat_io_prj_1;
  assign io_prj_2 =
    io_rd_valid_1 & io_rd_1 == io_rj_2
      ? _free_list_io_alloc_preg_1
      : io_rd_valid_0 & io_rd_0 == io_rj_2 ? _free_list_io_alloc_preg_0 : _crat_io_prj_2;
  assign io_prj_3 =
    io_rd_valid_2 & io_rd_2 == io_rj_3
      ? _free_list_io_alloc_preg_2
      : io_rd_valid_1 & io_rd_1 == io_rj_3
          ? _free_list_io_alloc_preg_1
          : io_rd_valid_0 & io_rd_0 == io_rj_3
              ? _free_list_io_alloc_preg_0
              : _crat_io_prj_3;
  assign io_prk_1 =
    io_rd_valid_0 & io_rd_0 == io_rk_1 ? _free_list_io_alloc_preg_0 : _crat_io_prk_1;
  assign io_prk_2 =
    io_rd_valid_1 & io_rd_1 == io_rk_2
      ? _free_list_io_alloc_preg_1
      : io_rd_valid_0 & io_rd_0 == io_rk_2 ? _free_list_io_alloc_preg_0 : _crat_io_prk_2;
  assign io_prk_3 =
    io_rd_valid_2 & io_rd_2 == io_rk_3
      ? _free_list_io_alloc_preg_2
      : io_rd_valid_1 & io_rd_1 == io_rk_3
          ? _free_list_io_alloc_preg_1
          : io_rd_valid_0 & io_rd_0 == io_rk_3
              ? _free_list_io_alloc_preg_0
              : _crat_io_prk_3;
  assign io_pprd_1 =
    io_rd_valid_0 & _rd_valid_temp_0_T ? _free_list_io_alloc_preg_0 : _crat_io_pprd_1;
  assign io_pprd_2 =
    io_rd_valid_1 & _rd_valid_temp_1_T
      ? _free_list_io_alloc_preg_1
      : io_rd_valid_0 & _rd_valid_temp_0_T_4
          ? _free_list_io_alloc_preg_0
          : _crat_io_pprd_2;
  assign io_pprd_3 =
    io_rd_valid_2 & _rd_valid_temp_2_T
      ? _free_list_io_alloc_preg_2
      : io_rd_valid_1 & _rd_valid_temp_1_T_4
          ? _free_list_io_alloc_preg_1
          : io_rd_valid_0 & _rd_valid_temp_0_T_8
              ? _free_list_io_alloc_preg_0
              : _crat_io_pprd_3;
endmodule
