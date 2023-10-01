import os
import sys
filelist = os.listdir(sys.argv[1])
verilog_path = sys.argv[2] + "/"
past_file_list = os.listdir(verilog_path)
for file in past_file_list :
    if file.endswith(".sv"):
        os.remove(verilog_path + file)
for file in filelist:
    if file.endswith(".sv"):
        filepath = os.path.join(sys.argv[1], file)
        break
    
    

with open(filepath, "r") as fp:
    lines = fp.readlines()
    write_flag = False
    for line in lines: 
        if line.startswith("module"): 
            name = line.split("(")[0].split("module ")[1]
            fp_module = open(verilog_path + name + ".sv", "w")
            fp_module.write(line)
            write_flag = True
        elif line.startswith("endmodule"):
            fp_module.write(line)
            write_flag = False
            fp_module.close()
        elif write_flag:
            fp_module.write(line)
            


