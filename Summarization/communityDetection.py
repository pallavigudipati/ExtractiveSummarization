import snap
import sys

inputFile=open(sys.argv[1])
addedNodes={}
G1=snap.TUNGraph.New()
#Read the graph from the file
lineIndex=0
for line in inputFile:
	words=line.split()
	source=int(words[0])
	destination=int(words[1])
	weight=float(words[2])
	if source not in addedNodes:
		G1.AddNode(source)
		addedNodes[source]=1
	if destination not in addedNodes:
		G1.AddNode(destination)
		addedNodes[destination]=1
	G1.AddEdge(source,destination)
	#if lineIndex>1000:
	#	break
	lineIndex+=1
#Run the CNM Community Detection Algorithm. Note that this neglects weights
CmtyV=snap.TCnComV()
modularity=snap.CommunityGirvanNewman(G1,CmtyV)
for Cmty in CmtyV:
	print "Community:"
	for NI in Cmty:
		print NI

