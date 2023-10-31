.DEFAULT_GOAL := verilog

WORK_DIR := $(shell pwd)
SCALA_SRCS := $(shell find $(WORK_DIR)/src/main/scala -name "*.scala")

# Verilog
verilog: $(SCALA_SRCS)
	@rm -rf $(WORK_DIR)/verilog
	@mkdir -p $(WORK_DIR)/verilog
	@sbt run

clean-verilog:
	@rm -rf $(WORK_DIR)/verilog

