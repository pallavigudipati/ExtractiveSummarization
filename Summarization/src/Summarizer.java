import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

public class Summarizer {

    public int numSentences;
    public int summaryLimit = 10;
    public int numSentencesHuman;
    public List<String> rawDocument = new ArrayList<String>();
    public List<List<String>> stemmedSentences = new ArrayList<List<String>>();
    public List<String> rawDocumentHuman = new ArrayList<String>();
    public List<List<String>> stemmedSentencesHuman = new ArrayList<List<String>>();
    public List<List<String>> summarySentences = new ArrayList<List<String>>();
    public List<List<String>> baselineSummarySentences = new ArrayList<List<String>>();
    public List<List<String>> globalInfluenceSummarySentences = new ArrayList<List<String>>();
    public List<List<String>> centroidBasedSummarySentences = new ArrayList<List<String>>();
    public List<List<String>> lemmatizedSentences = new ArrayList<List<String>>();
    
	public static void main(String[] args) throws IOException {
		
		double averageROUGEScore=0.0;
		double averageROUGEScoreBaseline=0.0;
		double averageROUGEScoreGlobalInfluence=0.0;
		double averageROUGEScoreCentroid=0.0;
		double numberOfRuns=0;
		for(int runs=0;runs<1000;runs++)
		{
			int i=9404003+runs;
			String fileName = "PapersDataset/"+i+"_body.txt";
			String fileNameHuman = "PapersDataset/"+i+"_abstract.txt";
			File f1=new File(fileName);
			File f2=new File(fileNameHuman);
			System.out.println("FileIndex:"+i);
			if(i==9405001 || i==9408015 || i==9412005 || i==9505001 || i==9505024 || i==9505031 || i==9512003 || i== 9604005 || i==9604013)
			{
				continue;
			}
			if(!f1.exists() || !f2.exists())
			{
				continue;
			}
			Summarizer summarizer = new Summarizer();
			summarizer.getRawAndStemmed(fileName);
			summarizer.getRawAndStemmedHuman(fileNameHuman);
			if(summarizer.stemmedSentencesHuman.size()<5)
			{
				continue;
			}
			summarizer.printSentenceGraphIdf(0.05);
			summarizer.printUndirectedSentenceGraphIdf(0.05);
			summarizer.runCommunityDetection();
			summarizer.runInfluenceMaximization();
			summarizer.runInfluenceMaximizationGlobal();
			summarizer.printSummary("IM/IM_output.txt");
			summarizer.printCentroidBasedSummary();
			summarizer.printGlobalInfluenceSummary("IMGlobal/IM_output.txt");
			
			double ROUGEScore = summarizer.computeROUGEUnigramScore(summarizer.summarySentences);
			double ROUGEScoreGlobalInfluence = summarizer.computeROUGEUnigramScore(summarizer.globalInfluenceSummarySentences);
			int summarySentenceLength=summarizer.computeSummarySentenceLength();
			summarizer.getBaselineSummary(summarySentenceLength);
			double ROUGEScoreBaseline = summarizer.computeROUGEUnigramScore(summarizer.baselineSummarySentences);
			double ROUGEScoreCentroid = summarizer.computeROUGEUnigramScore(summarizer.centroidBasedSummarySentences);
			System.out.println(ROUGEScore);
			System.out.println(ROUGEScoreBaseline);
			System.out.println(ROUGEScoreGlobalInfluence);
			averageROUGEScore+=ROUGEScore;
			averageROUGEScoreBaseline+=ROUGEScoreBaseline;
			averageROUGEScoreGlobalInfluence+=ROUGEScoreGlobalInfluence;
			averageROUGEScoreCentroid+=ROUGEScoreCentroid;
			numberOfRuns+=1.0;
			System.out.println("Total Sentences in Document:" + summarizer.numSentences);
			System.out.println("Number of Runs:"+numberOfRuns);
			System.out.println("AverageROUGEScore"+averageROUGEScore/numberOfRuns);
			System.out.println("AverageROUGEScoreBaseline:"+averageROUGEScoreBaseline/numberOfRuns);
			System.out.println("AverageROUGEScoreGlobalInfluence:"+averageROUGEScoreGlobalInfluence/numberOfRuns);
			System.out.println("AverageROUGEScoreCentroid:"+averageROUGEScoreCentroid/numberOfRuns);
		}
		averageROUGEScore/=numberOfRuns;
		averageROUGEScoreBaseline/=numberOfRuns;
		averageROUGEScoreGlobalInfluence/=numberOfRuns;
		averageROUGEScoreCentroid/=numberOfRuns;
		System.out.println("AverageROUGEScore"+averageROUGEScore);
		System.out.println("AverageROUGEScoreBaseline:"+averageROUGEScoreBaseline);
		System.out.println("AverageROUGEScoreGlobalInfluence:"+averageROUGEScoreGlobalInfluence);
		System.out.println("AverageROUGEScoreCentroid:"+averageROUGEScoreCentroid);
		System.out.println("NumberOfRuns:"+numberOfRuns);
	}

    public int computeSummarySentenceLength() {
        int sentenceLength = 0;
        for (List<String> summarySentence : summarySentences) {
            sentenceLength += summarySentence.size();
        }
        return sentenceLength;
    }

    public void getBaselineSummary(int summarySentenceLength)
            throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(
                "PageRankNodesDir/pageRankNodes.txt"));

        int baselineSummaryLength = 0;
        String line;
        int index = 0;
        System.out.println("Page Rank Summary");
        while ((line = br.readLine()) != null) {
            int selectedNode = Integer.parseInt(line);
            baselineSummarySentences.add(stemmedSentences.get(selectedNode));
            // System.out.println(rawDocument.get(selectedNode));
            index += 1;
            baselineSummaryLength += stemmedSentences.get(selectedNode).size();
            if (index >= summaryLimit
                    || baselineSummaryLength >= summarySentenceLength) {
                if (index > summaryLimit) {
                    baselineSummarySentences.remove(baselineSummarySentences
                            .size() - 1);
                }
                break;
            }
        }
        br.close();
    }

    public double computeROUGEScore() {
        HashMap<String, Integer> humanBigrams = new HashMap<String, Integer>();
        HashMap<String, Integer> automaticBigrams = new HashMap<String, Integer>();
        for (List<String> stemmedSentenceHuman : stemmedSentencesHuman) {
            for (int j = 0; j < stemmedSentenceHuman.size() - 1; j++) {
                String bigram = stemmedSentenceHuman.get(j) + " "
                        + stemmedSentenceHuman.get(j + 1);
                humanBigrams.put(bigram, 1);
            }
        }
        for (List<String> summarySentence : summarySentences) {
            for (int j = 0; j < summarySentence.size() - 1; j++) {
                String bigram = summarySentence.get(j) + " "
                        + summarySentence.get(j + 1);
                automaticBigrams.put(bigram, 1);
            }
        }
        int overlap = 0;
        int totalBigrams = humanBigrams.size();
        // System.out.println("Overlapping Bigrams");
        for (String humanBigram : humanBigrams.keySet()) {
            if (automaticBigrams.containsKey(humanBigram)) {
                overlap += 1;
            }
        }
        double ROUGEScore = (overlap + 0.0) / (totalBigrams + 0.0);
        System.out.println(overlap);
        System.out.println(totalBigrams);
        return ROUGEScore;
    }

    public double computeROUGEUnigramScore(List<List<String>> summarySentences) {
        HashMap<String, Integer> humanBigrams = new HashMap<String, Integer>();
        HashMap<String, Integer> automaticBigrams = new HashMap<String, Integer>();
        for (List<String> stemmedSentenceHuman : stemmedSentencesHuman) {
            for (int j = 0; j < stemmedSentenceHuman.size(); j++) {
                String bigram = stemmedSentenceHuman.get(j);
                humanBigrams.put(bigram, 1);
            }
        }
        for (List<String> summarySentence : summarySentences) {
            for (int j = 0; j < summarySentence.size(); j++) {
                String bigram = summarySentence.get(j);
                automaticBigrams.put(bigram, 1);
            }
        }
        int overlap = 0;
        int totalBigrams = humanBigrams.size();
        for (String humanBigram : humanBigrams.keySet()) {
            if (automaticBigrams.containsKey(humanBigram)) {
                overlap += 1;
            }
        }
        double ROUGEScore = (overlap + 0.0) / (totalBigrams + 0.0);
        // System.out.println(overlap);
        // System.out.println(totalBigrams);
        return ROUGEScore;
    }

    public void runInfluenceMaximizationGlobal() throws IOException {
        List<String> command = new ArrayList<String>();
        command.add("python");
        command.add("influence_maximization_global.py");
        command.add(Integer.toString(1));
        command.add(Integer.toString(summaryLimit));
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(
                command);
        commandExecutor = new SystemCommandExecutor(command);
        try {
            int result = commandExecutor.executeCommand();
        } catch (Exception e) {
            System.out.println("Error in Global influence maximization: "
                    + e.getMessage());
        }
        System.out.println("Global Influence Maximization: Finished");
    }

    public void runInfluenceMaximization() throws IOException {
        int totalGraphSize = 0;
        // int summaryLimit = 7; // TODO:Add as global parameter
        /* Read node counts from a file */
        BufferedReader br = new BufferedReader(new FileReader(
                "CommunityNodeCounts/communityNodeCounts.txt"));
        ArrayList<Integer> communityNodeCounts = new ArrayList<Integer>();

        String line;
        while ((line = br.readLine()) != null) {
            // process the line.
            int nodeCount = Integer.parseInt(line);
            communityNodeCounts.add(nodeCount);
            totalGraphSize += nodeCount;
        }
        br.close();
        /* Compute sentence budget */
        ArrayList<Integer> sentenceBudgets = new ArrayList<Integer>();
        ArrayList<Integer> admittedCommunities = new ArrayList<Integer>();
        int effectiveGraphSize = 0;
        for (int i = 0; i < communityNodeCounts.size(); i++) {
            int nodeCount = communityNodeCounts.get(i);
            double nodeFraction = (nodeCount + 0.0) / (totalGraphSize + 0.0);
            double exactFractionalWeight = nodeFraction * summaryLimit;
            if (exactFractionalWeight >= 1.0) {
                effectiveGraphSize += nodeCount;
                admittedCommunities.add(i);
            }
        }
        // System.out.println(admittedCommunities);
        for (int i = 0; i < communityNodeCounts.size(); i++) {
            if (admittedCommunities.contains(i)) {
                int nodeCount = communityNodeCounts.get(i);
                double nodeFraction = (nodeCount + 0.0)
                        / (effectiveGraphSize + 0.0);
                double exactFractionalWeight = nodeFraction * summaryLimit;
                sentenceBudgets.add((int) Math.floor(exactFractionalWeight));
                // System.out.println(Math.floor(exactFractionalWeight));
            } else {
                sentenceBudgets.add(0);
            }
        }
        // System.out.println(sentenceBudgets);
        List<String> command = new ArrayList<String>();
        command.add("python");
        command.add("influence_maximization.py");
        command.add(Integer.toString(communityNodeCounts.size()));
        for (Integer sentenceBudget : sentenceBudgets) {
            command.add(Integer.toString(sentenceBudget));
        }
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(
                command);
        commandExecutor = new SystemCommandExecutor(command);
        try {
            int result = commandExecutor.executeCommand();
        } catch (Exception e) {
            System.out.println("Error in influence maximization: "
                    + e.getMessage());
        }
        System.out.println("Influence Maximization: Finished");
        return;
    }

    // Merging it into one function so that the the whole graph is iterated only
    // once.
    public void getRawAndStemmed(String fileName) {
        DocumentPreprocessor dp = new DocumentPreprocessor(fileName);
        Stemmer stemmer = new Stemmer();
        for (List sentence : dp) {
            // System.out.println(sentence);
            rawDocument.add(StringUtils.join(sentence, " "));
            List<String> stemmedSentence = new ArrayList<String>();
            for (Object word : sentence) {
                stemmer.add(word.toString().toCharArray(), word.toString()
                        .length());
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
                stemmer.add(word.toString().toCharArray(), word.toString()
                        .length());
                stemmer.stem();
                stemmedSentence.add(stemmer.toString());
            }
            stemmedSentencesHuman.add(stemmedSentence);
        }
        numSentencesHuman = rawDocumentHuman.size();
    }

    public void getRawAndLemmatized(String fileName) {
        DocumentPreprocessor dp = new DocumentPreprocessor(fileName);
        // String document = "";
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        for (List sentence : dp) {
            String rawSentence = StringUtils.join(sentence, " ");
            rawDocument.add(rawSentence);
            List<String> lemmatizedSentence = new ArrayList<String>();
            Annotation document = new Annotation(rawSentence);
            pipeline.annotate(document);
            CoreMap sentenceParsed = document.get(SentencesAnnotation.class)
                    .get(0);
            for (CoreLabel token : sentenceParsed.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the
                // list of lemmas
                lemmatizedSentence.add(token.get(LemmaAnnotation.class));
            }
            lemmatizedSentences.add(lemmatizedSentence);
        }
        numSentences = rawDocument.size();
    }

    public void printSentenceGraphIdf(double threshold) {
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
            System.out.println("Not able to print sentenceGraph: "
                    + e.getMessage());
        }
        System.out.println("Sentence Graph Creation: Finished");
    }

    public void printCentroidBasedSummary() {
        InvertedIndex index = new InvertedIndex();
        index.createIndex(stemmedSentences);
        TreeMap<Integer, Double> centroidScores = index.getCentroidScores();
        int i=0;
        for (int entry : centroidScores.descendingKeySet()) {
            System.out.println(rawDocument.get(entry));
            centroidBasedSummarySentences.add(stemmedSentences.get(entry));
            i+=1;
            if(i>=summaryLimit)
            {
            	break;
            }
        }
    }

    public void printUndirectedSentenceGraphIdf(double threshold) {
        InvertedIndex index = new InvertedIndex();
        index.createIndex(stemmedSentences);
        double[][] weightMatrix = index.getCosineSimilarity();
        try {
            PrintWriter writer = new PrintWriter("sentenceGraphUndirected.txt",
                    "UTF-8");
            for (int i = 0; i < numSentences; i++) {
                for (int j = 0; j < numSentences; j++) {
                    if (weightMatrix[i][j] >= threshold) {
                        writer.println(i + " " + j + " " + weightMatrix[i][j]);
                    }
                }
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("Not able to print Undirected sentenceGraph: "
                    + e.getMessage());
        }
        System.out.println("Undirected Sentence Graph Creation: Finished");
    }

    public void printSentenceGraphWN(double threshold, int type) {
        WordNetSimilarity wnSimilarity = new WordNetSimilarity(
                lemmatizedSentences);
        double[][] weightMatrix = wnSimilarity.getCosineSimilarity(type);
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
            System.out.println("Not able to print sentenceGraph: "
                    + e.getMessage());
        }
        System.out.println("Sentence Graph Creation: Finished");
    }

    // Calls the python community detection code.
    public void runCommunityDetection() {
        List<String> command = new ArrayList<String>();
        command.add("python");
        command.add("igraphCommunityDetection.py");
        command.add("sentenceGraph.txt");
        SystemCommandExecutor commandExecutor = new SystemCommandExecutor(
                command);
        commandExecutor = new SystemCommandExecutor(command);
        try {
            int result = commandExecutor.executeCommand();
        } catch (Exception e) {
            System.out.println("Error in community detection: "
                    + e.getMessage());
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
                    // System.out.println(rawDocument.get(Integer.parseInt(node)));
                    summarySentences.add(stemmedSentences.get(Integer
                            .parseInt(node)));
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving results from IM: "
                    + e.getMessage());
        }
    }

    public void printGlobalInfluenceSummary(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line = reader.readLine();
            System.out.println("Summary Extraction: Finished");
            if (line != null) {
                String[] nodes = line.split(" ");
                for (String node : nodes) {
                    // System.out.println(rawDocument.get(Integer.parseInt(node)));
                    globalInfluenceSummarySentences.add(stemmedSentences
                            .get(Integer.parseInt(node)));
                }
            }
        } catch (Exception e) {
            System.out.println("Error retrieving results from IM: "
                    + e.getMessage());
        }
    }
}
