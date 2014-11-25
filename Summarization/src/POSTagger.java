import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.process.*;
public class POSTagger {
    public HashMap<String, String> pennToInternal;
    public MaxentTagger tagger;

    public POSTagger() {
        tagger = new MaxentTagger("pos-model/english-bidirectional-distsim.tagger");
        loadMappings();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        POSTagger posTagger = new POSTagger();
        MaxentTagger tagger = new MaxentTagger("pos-model/english-bidirectional-distsim.tagger");
        posTagger.loadMappings();
        String example = "It is Max's girl";
        long startTime = System.currentTimeMillis();
        String[] taggedString = posTagger.tagSentence(example);
        long endTime = System.currentTimeMillis();
        System.out.println(taggedString);
        System.out.println(endTime - startTime);
        DocumentPreprocessor dp=new DocumentPreprocessor("GiftOfTheMagi.txt");
        Stemmer stemmer=new Stemmer();
        List<List<String>> stemmedSentences=new ArrayList<List<String>>();
        for(List sentence: dp)
        {
        	System.out.println(sentence);
        	List stemmedSentence=new ArrayList<String>();
        	for(Object word:sentence)
        	{
                stemmer.add(word.toString().toCharArray(),word.toString().length());
                stemmer.stem();
                stemmedSentence.add(stemmer.toString());
        	}
        	stemmedSentences.add(stemmedSentence);
        	System.out.println(stemmedSentence);
        }
        List<String> sen1 = Arrays.asList("my","name","is","pal","and","this","is","not","a","good","place","to","converse");
        List<String> sen2 = Arrays.asList("my","last-name","is","pal");
        List<String> sen4=  Arrays.asList("He","is","is","a","joker");
        List<String> sen3 = Arrays.asList("There","was","a","great","man","named","Arthur");
        List<List<String>> doc = stemmedSentences;//Arrays.asList(sen1, sen2, sen3,sen4);
        InvertedIndex index = new InvertedIndex();
        index.createIndex(doc);
        index.generateTfIdfScores();
        System.out.println(index.tfIdfScores[0]);
        System.out.println(index.tfIdfScores[1]);
        System.out.println(index.tfIdfScores[2]);
        System.out.println(index.tfIdfScores[3]);
        System.out.println(index.idfScores.get("is"));
        double[][] adjMatrix=new double[doc.size()][doc.size()];
        for(int i=0;i<doc.size();i++)
        {
        	double modI=0.0;
        	HashMap<String,Double> vectorI=index.tfIdfScores[i];
        	for(String key:vectorI.keySet())
        	{
        		modI+=vectorI.get(key)*vectorI.get(key);
        	}
        	modI=Math.sqrt(modI);
        	for(int j=0;j<doc.size();j++)
        	{
        		double modJ=0.0;
            	HashMap<String,Double> vectorJ=index.tfIdfScores[j];
            	for(String key:vectorJ.keySet())
            	{
            		modJ+=vectorJ.get(key)*vectorJ.get(key);
            	}
            	modJ=Math.sqrt(modJ);
            	double dotProduct=0.0;
            	for(String key:vectorI.keySet())
            	{
            		if(vectorJ.get(key)!=null)
            		{
            			dotProduct+=vectorI.get(key)*vectorJ.get(key);
            		}
            	}
            	dotProduct/=(modI*modJ);
            	if(i==j)
            	{
            		System.out.println(dotProduct);
            	}
            	adjMatrix[i][j]=dotProduct;
        	}
        }
        /*
        for(int i=0;i<doc.size();i++)
        {
        	for(int j=0;j<doc.size();j++)
        	{
        		System.out.print(adjMatrix[i][j]+" ");
        	}
        	System.out.println();
        }*/
        /*Write graph to file*/
		PrintWriter writer = new PrintWriter("sentenceGraph.txt", "UTF-8");
        for(int i=0;i<doc.size();i++)
        {
        	for(int j=i;j<doc.size();j++)
        	{
        		if(adjMatrix[i][j]>=0.05)
        		{
        			writer.println(i+" "+j+" "+adjMatrix[i][j]);
        		}
        	}
        }
        writer.close();
        /*Write out all the sentences to a file for visual inspection*/
        writer = new PrintWriter("sentenceLog.txt", "UTF-8");
        for(int i=0;i<doc.size();i++)
        {
        	writer.println(i+" : "+doc.get(i));
        	
        }
        writer.close();
        /*Do the community detection*/
        String INPUT_LINE_GRAPH = "sentenceGraph.txt";
    	String MODULARITY_FILE = "modularityMatrix/matrix.txt";
    	String INFLATION = "1.5";
    	String MCL_FILE = "MCL/mcl.txt";
    	List<String> command = new ArrayList<String>();
    	command.add("mcl");
    	command.add(INPUT_LINE_GRAPH);
    	command.add("-I");
    	command.add(INFLATION);
    	command.add("--abc");
    	command.add("-o");
    	command.add(MCL_FILE);
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(command);
        int result = commandExecutor.executeCommand();
        System.out.println("end");
        /*Call the python community detection code*/
        command=new ArrayList<String>();
        command.add("python");
        command.add("igraphCommunityDetection.py");
        command.add("sentenceGraph.txt");
        commandExecutor = new SystemCommandExecutor(command);
        result = commandExecutor.executeCommand();
    }

    public void loadMappings() {
        String filename = "pos-model/penn_to_internal.txt";
        pennToInternal = new HashMap<String, String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = null;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                pennToInternal.put(parts[0], parts[1]);
            }
            reader.close();
        } catch (Exception e) {
            System.out.println("Not able to read files");
        }
    }

    public String[] tagSentence(String sentence) {
        String pennSentence = tagger.tagString(sentence);
        List<String> internalSentence = new ArrayList<String>();
        String[] words = pennSentence.split(" ");
        for (String word : words) {
            String[] tag = word.split("_");
            internalSentence.add(tag[0] + "_" + pennToInternal.get(tag[1]));
        }
        return internalSentence.toArray(new String[internalSentence.size()]);
    }
}
