# Input: Num. of communities, [num nodes from each community]
# ./InfluenceModels -c config_sample.txt -budget 10 -probGraphFile "../hep_LT2.inf" -outdir "output/test2"

# -phase 20 -propModel PageRank -outdir
##### Doesn't work for just 2 nodes.

import os
import sys
import stat
import subprocess
import shutil

num_communities = int(sys.argv[1])
root_dir = 'Communities/';
output_root_dir = 'PageRankCommunities/';
command_1 = './icdm11-simpath-release/InfluenceModels -c icdm11-simpath-release/config_sample.txt -phase 20 -propModel PageRank -probGraphFile ' # Communities/8.txt
command_2 = ' -outdir ' # output/test

# Flush output directory if it already exists. Create a fresh folder.
if os.path.exists(output_root_dir):
	shutil.rmtree(output_root_dir)
os.makedirs(output_root_dir)

for i in range(0,num_communities):
	community_file = root_dir + str(i) + '.txt'
	if os.stat(community_file).st_size == 0:
		# Empty file. 
		continue
	num_nodes = int(sys.argv[i + 2])
	if num_nodes == 0:
		# No nodes need to be selected. 
		continue
	output_folder = output_root_dir + str(i)
	command = command_1 + community_file + command_3 + output_folder
	
	# Executing the command.
	subprocess.call(command, shell=True)

print 'Finished PageRank. Extracting the vertices.'
filename = 'PageRank.txt'
final_output = output_root_dir + 'PageRank_output.txt' 
nodes = []
for folder in os.listdir(output_root_dir):
	output_file = open(output_root_dir + folder + '/' + filename, 'r')
	num_nodes = int(sys.argv[int(folder) + 2])
	output = output_file.read().strip().split('\n')
	output_file.close()
# for line in outpur:
	for i in range(0,num_nodes):
		nodes.append(int(output[i].split()[1]))
nodes.sort()
nodes = map(str, nodes)

final = open(final_output, 'wb')
final.write(' '.join(nodes))
final.close()
