.DEFAULT_GOAL := verilog

WORK_DIR := $(shell pwd)
SCALA_SRCS := $(shell find $(WORK_DIR)/src/main/scala -name "*.scala")

# Verilog
verilog: $(SCALA_SRCS)
	@sbt run

