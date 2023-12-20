# .DEFAULT_GOAL := verilog

WORK_DIR := $(shell pwd)
SCALA_SRCS := $(shell find $(WORK_DIR)/src/main/scala -name "*.scala")
MODE ?= sim

all: verilog
verilog: $(SCALA_SRCS)
	@mkdir -p $(WORK_DIR)/verilog
	@rm -rf $(WORK_DIR)/verilog/*
	@sbt "runMain CPU_Main" -Dmode=$(MODE) --batch

clean-verilog:
	@rm -rf $(WORK_DIR)/verilog

