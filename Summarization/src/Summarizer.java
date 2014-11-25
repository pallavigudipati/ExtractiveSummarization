import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.process.DocumentPreprocessor;


public class Summarizer {

    public int numSentences;
    public List<String> rawDocument = new ArrayList<String>();
    public List<List<String>> stemmedSentences = new ArrayList<List<String>>();

    public static void main(String[] args) {
        Summarizer summarizer = new Summarizer();
        summarizer.getRawAndStemmed("TreasureIsland.txt");
        summarizer.printSentenceGraph(0.05);
        summarizer.runCommunityDetection();
    }
    
    
    public void runInfluenceMaximization()
    {
    	return;
    }
    // Merging it into one function so that the the whole graph is iterated only once. 
    public void getRawAndStemmed(String fileName) {
        DocumentPreprocessor dp = new DocumentPreprocessor(fileName);
        Stemmer stemmer = new Stemmer();
        for (List sentence : dp) {
            // System.out.println(sentence);
            rawDocument.add(sentence.toString());
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
    }
}
