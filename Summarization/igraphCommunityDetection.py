from igraph import *
import sys

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
	edgeMap[str(source)+"_"+str(destination)]=1
	edgeMap[str(destination)+"_"+str(source)]=1
	print edgeCount
	edgeCount+=1
	if edgeCount>10000:
		break
#vertexCluster=g.community_infomap(edge_weights="weight")
#for clusterInstance in vertexCluster:
#	print clusterInstance
vertexDendrogram=g.community_fastgreedy(weights="weight")
vertexClustering=vertexDendrogram.as_clustering()
for vertexCluster in vertexClustering:
	print vertexCluster
	communityGraph=Graph()
	communityGraph.add_vertices(len(vertexCluster))
	for source in range(len(vertexCluster)):
		for destination in range(len(vertexCluster)):
			if destination>=source:
				if (str(vertexCluster[source])+"_"+str(vertexCluster[destination])) in edgeMap:
					communityGraph.add_edges([(source,destination),])

PRList=g.pagerank(weights="weight")
IndicedPRList=[(i,PRList[i]) for i in range(len(PRList))]
IndicedPRList.sort(key=lambda tup: tup[1])
IndicedPRList.reverse()
print "Summary according to Global PageRank"
print [elem[0] for elem in IndicedPRList[:10]]
