import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.util.StringUtils;

public class Summarizer {

    public static int numSentences;
    public int numSentencesHuman;
    public List<String> rawDocument = new ArrayList<String>();
    public List<List<String>> stemmedSentences = new ArrayList<List<String>>();
    public List<String> rawDocumentHuman = new ArrayList<String>();
    public List<List<String>> stemmedSentencesHuman = new ArrayList<List<String>>();
    public List<List<String>> summarySentences = new ArrayList<List<String>>();
    
    public static void main(String[] args) throws IOException {
    	String fileName="TreasureIsland.txt";
    	String fileNameHuman="TreasureIsland.txt";
    	Summarizer summarizer = new Summarizer();
        summarizer.getRawAndStemmed(fileName);
        summarizer.printSentenceGraph(0.05);
        summarizer.runCommunityDetection();
        summarizer.runInfluenceMaximization();
        // TODO: Attach IM part.
        summarizer.printSummary("IM/IM_output.txt");
        summarizer.getRawAndStemmedHuman(fileNameHuman);
        double ROUGEScore=summarizer.computeROUGEScore();
        System.out.println(ROUGEScore);
        System.out.println("Total Sentences in Document:"+numSentences);
    }
    
    public double computeROUGEScore()
    {
    	HashMap<String,Integer> humanBigrams=new HashMap<String,Integer>();
    	HashMap<String,Integer> automaticBigrams=new HashMap<String,Integer>();
    	for(List<String> stemmedSentenceHuman: stemmedSentencesHuman)
    	{
    		for(int j=0;j<stemmedSentenceHuman.size()-1;j++)
    		{
    			String bigram=stemmedSentenceHuman.get(j)+" "+stemmedSentenceHuman.get(j+1);
    			humanBigrams.put(bigram, 1);
    		}
    	}
    	for(List<String> summarySentence: summarySentences)
    	{
    		for(int j=0;j<summarySentence.size()-1;j++)
    		{
    			String bigram=summarySentence.get(j)+" "+summarySentence.get(j+1);
    			automaticBigrams.put(bigram, 1);
    		}
    	}
    	int overlap=0;
    	int totalBigrams=humanBigrams.size();
    	System.out.println("Overlapping Bigrams");
    	for(String humanBigram:humanBigrams.keySet())
    	{
    		if(automaticBigrams.containsKey(humanBigram))
    		{
    			overlap+=1;
    			System.out.println(humanBigram);
    		}
    	}
    	double ROUGEScore=(overlap+0.0)/(totalBigrams+0.0);
    	System.out.println(overlap);
    	System.out.println(totalBigrams);
    	return ROUGEScore;
    }
    public void runInfluenceMaximization() throws IOException
    {
    	int totalGraphSize=0;
    	int summaryLimit=10; //TODO:Add as global parameter
    	/*Read node counts from a file*/
    	BufferedReader br = new BufferedReader(new FileReader("CommunityNodeCounts/communityNodeCounts.txt"));
    	ArrayList<Integer> communityNodeCounts=new ArrayList<Integer>();

    	String line;
    	while ((line = br.readLine()) != null) {
    	   // process the line.
    		int nodeCount = Integer.parseInt(line);
    		communityNodeCounts.add(nodeCount);
    		totalGraphSize+=nodeCount;
    	}
    	br.close();
    	/*Compute sentence budget*/
    	ArrayList<Integer> sentenceBudgets=new ArrayList<Integer>();
    	ArrayList<Integer> admittedCommunities=new ArrayList<Integer>();
    	int effectiveGraphSize=0;
    	for(int i=0;i<communityNodeCounts.size();i++)
    	{
    		int nodeCount=communityNodeCounts.get(i);
    		double nodeFraction = (nodeCount+0.0)/(totalGraphSize+0.0);
    		double exactFractionalWeight = nodeFraction*summaryLimit;
    		if(exactFractionalWeight>=1.0)
    		{
    			effectiveGraphSize+=nodeCount;
    			admittedCommunities.add(i);
    		}
    	}
    	System.out.println(admittedCommunities);
    	for(int i=0;i<communityNodeCounts.size();i++)
    	{
    		if(admittedCommunities.contains(i))
    		{
    			int nodeCount=communityNodeCounts.get(i);
    			double nodeFraction = (nodeCount+0.0)/(effectiveGraphSize+0.0);
    			double exactFractionalWeight = nodeFraction*summaryLimit;
    			sentenceBudgets.add((int)Math.floor(exactFractionalWeight));
    			System.out.println(Math.floor(exactFractionalWeight));
    		}
    		else
    		{
    			sentenceBudgets.add(0);
    		}
    		
    	}
    	System.out.println(sentenceBudgets);
        List<String> command = new ArrayList<String>();
        command.add("python");
        command.add("influence_maximization.py");
        command.add(Integer.toString(communityNodeCounts.size()));
        for(Integer sentenceBudget:sentenceBudgets)
        {
        	command.add(Integer.toString(sentenceBudget));
        }
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
        commandExecutor = new SystemCommandExecutor(command);
        try {
            int result = commandExecutor.executeCommand();
        } catch (Exception e) {
            System.out.println("Error in influence maximization: " + e.getMessage());
        }
        System.out.println("Influence Maximization: Finished");    	
    	return;
    }
    // Merging it into one function so that the the whole graph is iterated only once. 
    public void getRawAndStemmed(String fileName) {
        DocumentPreprocessor dp = new DocumentPreprocessor(fileName);
        Stemmer stemmer = new Stemmer();
        for (List sentence : dp) {
            // System.out.println(sentence);
            rawDocument.add(StringUtils.join(sentence, " "));
            List<String> stemmedSentence = new ArrayList<String>();
            for (Object word : sentence) {
                stemmer.add(word.toString().toCharArray(), word.toString().length());
                stemmer.stem();
                stemmedSentence.add(stemmer.toString());
            }
            stemmedSentences.add(stemmedSentence);
        }
        numSentences = rawDocument.size();
    }

    public void getRawAndStemmedHuman(String fileName) {
        DocumentPreprocessor dp = new DocumentPreprocessor(fileName);
        Stemmer stemmer = new Stemmer();
        for (List sentence : dp) {
            // System.out.println(sentence);
            rawDocumentHuman.add(StringUtils.join(sentence, " "));
            List<String> stemmedSentence = new ArrayList<String>();
            for (Object word : sentence) {
                stemmer.add(word.toString().toCharArray(), word.toString().length());
                stemmer.stem();
                stemmedSentence.add(stemmer.toString());
            }
            stemmedSentencesHuman.add(stemmedSentence);
        }
        numSentencesHuman = rawDocumentHuman.size();
    }
    
    public void printSentenceGraph(double threshold) {
        InvertedIndex index = new InvertedIndex();
        index.createIndex(stemmedSentences);
        double[][] weightMatrix = index.getCosineSimilarity();
        try {
            PrintWriter writer = new PrintWriter("sentenceGraph.txt", "UTF-8");
            for (int i = 0; i < numSentences; i++) {
                for (int j = i; j < numSentences; j++) {
                    if (weightMatrix[i][j] >= threshold) {
                        writer.println(i + " " + j + " " + weightMatrix[i][j]);
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Not able to print sentenceGraph: " + e.getMessage());
        }
        System.out.println("Sentence Graph Creation: Finished");
    }

    // Calls the python community detection code.
    public void runCommunityDetection() {
        List<String> command = new ArrayList<String>();
        command.add("python");
        command.add("igraphCommunityDetection.py");
        command.add("sentenceGraph.txt");
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
        commandExecutor = new SystemCommandExecutor(command);
        try {
            int result = commandExecutor.executeCommand();
        } catch (Exception e) {
            System.out.println("Error in community detection: " + e.getMessage());
        }
        System.out.println("Community Detection: Finished");
    }

    // Retrieves the output from IM and print the final summary.
    public void printSummary(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            System.out.println("Summary Extraction: Finished");
            if (line != null) {
                String[] nodes = line.split(" ");
                for (String node : nodes) {
                    System.out.println(rawDocument.get(Integer.parseInt(node)));
                    summarySentences.add(stemmedSentences.get(Integer.parseInt(node)));
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving results from IM: " + e.getMessage());
        }
    }
}
