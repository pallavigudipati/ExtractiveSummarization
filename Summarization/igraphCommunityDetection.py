from igraph import *
import sys
import os
import shutil
cwdPath=os.getcwd()
cwdPathSplit=cwdPath.split("/")
#print cwdPathSplit
if cwdPathSplit[-1]=="Summarization":
	dirPath=cwdPath+"/Communities"
	testFile=open("test.txt","w")
	testFile.write("Success")
	#print dirPath
else:
	dirPath="/home/vgtomahawk/7thSem/CS6370/NLPProject/Summarization/Communities"
fileList=os.listdir(dirPath)
for fileName in fileList:
	os.remove(dirPath+"/"+fileName)
print "Cleaned the Communities Folder"
#End of Clean

#Create output directory for community node counts
communityNodeCountsDir="CommunityNodeCounts/"
if os.path.exists(communityNodeCountsDir):
	shutil.rmtree(communityNodeCountsDir)
os.makedirs(communityNodeCountsDir)
communityNodeCountsFile=open(communityNodeCountsDir+"communityNodeCounts.txt","w")
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
	#print edgeCount
	edgeCount+=1
	if edgeCount>10000:
		break
#vertexCluster=g.community_infomap(edge_weights="weight")
#for clusterInstance in vertexCluster:
#	print clusterInstance
vertexDendrogram=g.community_fastgreedy(weights="weight")
vertexClustering=vertexDendrogram.as_clustering()
vertexClusterIndex=0
for vertexCluster in vertexClustering:
	print vertexCluster
	communityFile=open("Communities/"+str(vertexClusterIndex)+".txt","w")
	for sourceIndex in range(len(vertexCluster)):
		for destinationIndex in range(len(vertexCluster)):
			source=vertexCluster[sourceIndex]
			destination=vertexCluster[destinationIndex]
			if destination>source:
				edgeKey=str(source)+"_"+str(destination)
				if edgeKey not in edgeMap:
					continue
				edgeWeight=edgeMap[edgeKey]
				communityFile.write(str(source)+" "+str(destination)+" "+str(edgeWeight)+"\n")
	communityFile.close()
	communityNodeCountsFile.write(str(len(vertexCluster))+"\n")
	#Note: activate the below code for 0-indexed communities
	"""
	communityGraph=Graph()
	communityGraph.add_vertices(len(vertexCluster))
	for source in range(len(vertexCluster)):
		for destination in range(len(vertexCluster)):
			if destination>=source:
				if (str(vertexCluster[source])+"_"+str(vertexCluster[destination])) in edgeMap:
					communityGraph.add_edges([(source,destination),])
	"""
	vertexClusterIndex+=1	
#Find top 10 nodes 
PRList=g.pagerank(weights="weight")
IndicedPRList=[(i,PRList[i]) for i in range(len(PRList))]
IndicedPRList.sort(key=lambda tup: tup[1])
IndicedPRList.reverse()
print "Summary according to Global PageRank"
print [elem[0] for elem in IndicedPRList[:20]]
