# Input: Num. of communities, [num nodes from each community]
# ./InfluenceModels -c config_sample.txt -budget 10 -probGraphFile "../hep_LT2.inf" -outdir "output/test2"

##### Doesn't work for just 2 nodes.

import os
import sys
import stat
import subprocess
import shutil

num_communities = int(sys.argv[1])
output_root_dir = 'IMGlobal/';
command_1 = './icdm11-simpath-release/InfluenceModels -c icdm11-simpath-release/config_sample.txt -budget ' # 0
command_2 = ' -probGraphFile ' # Communities/8.txt
command_3 = ' -outdir ' # output/test

# Flush output directory if it already exists. Create a fresh folder.
if os.path.exists(output_root_dir):
	shutil.rmtree(output_root_dir)
os.makedirs(output_root_dir)


community_file = 'sentenceGraphUndirected.txt'
num_nodes = int(sys.argv[2])
output_folder = output_root_dir + str(0)
command = command_1 + str(num_nodes) + command_2 + community_file + command_3 + output_folder
	
# Executing the command.
subprocess.call(command, shell=True)

print 'Finished IM. Extracting the vertices.'
filename = 'LT_SimPath_4_0.001.txt'
final_output = output_root_dir + 'IM_output.txt' 
nodes = []
for folder in os.listdir(output_root_dir):
	output_file = open(output_root_dir + folder + '/' + filename, 'r')
	output = output_file.read().strip().split('\n')
	output_file.close()
	for line in output:
		nodes.append(int(line.split()[0]))
nodes.sort()
nodes = map(str, nodes)

final = open(final_output, 'wb')
final.write(' '.join(nodes))
final.close()
