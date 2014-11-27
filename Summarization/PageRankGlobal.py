from igraph import *
import sys
import os
import shutil

inputFile=open(sys.argv[1])
addedNodes={}
#Read the graph from the file
lineIndex=0
inputGraphNodes={}
for line in inputFile:
	words=line.split()
	source=int(words[0])
	destination=int(words[1])
	weight=float(words[2])
	inputGraphNodes[source]=1
	inputGraphNodes[destination]=1

g=Graph()
g.add_vertices(len(inputGraphNodes.keys()))
inputFile=open(sys.argv[1])
edgeCount=0
edgeMap={}
for line in inputFile:
	words=line.split()
	source=int(words[0])
	destination=int(words[1])
	edgeWeight=float(words[2])
	g.add_edges([(source,destination),])
	g.es[edgeCount]["weight"]=edgeWeight
	edgeMap[str(source)+"_"+str(destination)]=edgeWeight
	edgeMap[str(destination)+"_"+str(source)]=edgeWeight
	edgeCount+=1
	if edgeCount>10000:
		break
 
PRList=g.pagerank(weights="weight")
IndicedPRList=[(i,PRList[i]) for i in range(len(PRList))]
IndicedPRList.sort(key=lambda tup: tup[1])
IndicedPRList.reverse()
print "Summary according to Global PageRank"
print [elem[0] for elem in IndicedPRList[:20]]
pageRankNodesDir="PageRankNodesDir/"
if os.path.exists(pageRankNodesDir):
	shutil.rmtree(pageRankNodesDir)
os.makedirs(pageRankNodesDir)
pageRankNodesFile=open(pageRankNodesDir+"pageRankNodes.txt","w")
for elem in IndicedPRList:
	pageRankNodesFile.write(str(elem[0])+"\n")
