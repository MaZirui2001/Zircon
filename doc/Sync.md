# Vivado 综合说明

本项目可生成可综合代码，并可以使用Vivado工具进行综合实现。

## 基本电路信息

* 开发板型号：xc7a200tfbg676-2L
* 最大时钟频率：110MHZ

## 综合选项

| 命令                       | 值                   |
| -------------------------- | -------------------- |
| -flatten_hierarchy         | rebuilt              |
| -gated_clock_conversion    | auto                 |
| -bufg                      | 12                   |
| -directive                 | PerformenceOptimized |
| -retiming                  | √                   |
| -no_retiming               | ×                   |
| -fsm_extraction            | one_hot              |
| -keep-equivalent_registers | √                   |
| -resource-sharing          | on                   |
| -control_set_opt_threshold | auto                 |
| -no-lc                     | ×                   |
| -no_srlextract             | ×                   |
| -shreg_min_size            | 5                    |
| -max_bram                  | -1                   |
| -max_uram                  | -1                   |
| -max_dsp                   | -1                   |
| -max_bram_cascade_height   | -1                   |
| -max_uram_cascade_height   | -1                   |
| -cascade_dsp               | auto                 |
| -assert                    | ×                   |
| -incremental_mode          | off                  |
| More Options               |                      |

## 实现选项

| 阶段                          | 策略                         |
| ----------------------------- | ---------------------------- |
| Opt Design                    | ExploreArea                  |
| Power Opt Design              | Off                          |
| Place Design                  | Explore                      |
| Post-Place Phys Opt Design    | ExploreWithAggressiveHoldFix |
| Route Design                  | AggressiveExplore            |
| Post-Route Phys Opt Design(?) | ExploreWithAggressiveHoldFix |
