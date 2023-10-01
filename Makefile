.DEFAULT_GOAL := verilog

WORK_DIR := $(shell pwd)
BUILD_DIR := $(WORK_DIR)/build
VERILOG_DIR := $(WORK_DIR)/verilog
SCALA_SRCS := $(shell find $(WORK_DIR)/src/main/scala -name "*.scala")
VERILOG_SRCS := $(shell find $(WORK_DIR)/src/main/resources -name "*.sv")
RESOURCES_DIR := $(WORK_DIR)/src/main/resources

# Verilog
verilog: $(SCALA_SRCS) $(VERILOG_SRCS)
	@rm -rf $(BUILD_DIR)
	@mkdir -p $(BUILD_DIR)
	@sbt run
	@python3 $(WORK_DIR)/scripts/split-module.py $(BUILD_DIR) $(VERILOG_DIR)
# @cp $(RESOURCES_DIR)/*.sv $(VERILOG_DIR)

