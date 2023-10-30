import chisel3._
import circt.stage.ChiselStage

object CPU_Main extends App {
    ChiselStage.emitSystemVerilogFile(
        new CPU(0x1c000000), 
        Array("-td", "build/"),
        firtoolOpts = Array("-disable-all-randomization", 
                            "-strip-debug-info",
                            "-strip-fir-debug-info",
                            "-O=release",
                            "--ignore-read-enable-mem",
                            "--lower-memories",
                            "--lowering-options=disallowPackedArrays, disallowLocalVariables",
                            "-o=verilog/",
                            "-split-verilog",
                            "--disable-aggressive-merge-connections",
                            //"--preserve-aggregate=vec"
                            )
    )
}

