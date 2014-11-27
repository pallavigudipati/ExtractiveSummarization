import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class InvertedIndex {

    public int numSentences;
    public HashMap<String, HashMap<Integer, Integer>> index = 
            new HashMap<String, HashMap<Integer, Integer>>();
    public HashMap<String, Double> idfScores = new HashMap<String, Double>();
    public HashMap<String, Double>[] tfIdfScores;
    public int[] maxWordCounts;
    public List<List<String>> rawDocument;

    public static void main(String[] args) {
        List<String> sen1 = Arrays.asList("my","name","is","pal");
        List<String> sen2 = Arrays.asList("my","last-name","is","pal");
        List<List<String>> doc = Arrays.asList(sen1, sen2);

        InvertedIndex index = new InvertedIndex();
        index.createIndex(doc);
        index.generateTfIdfScores();
    }

    /*
     * Makes an index from word to hashmap(sentence no -> count)
     */
    public void createIndex(List<List<String>> document) {
        rawDocument = document;
        numSentences = document.size();
        maxWordCounts = new int[numSentences];
        for (int i = 0; i < document.size(); ++i) {
            for (String word : document.get(i)) {
                if (index.get(word) == null) {
                    index.put(word, new HashMap<Integer, Integer>());
                }
                // TODO: what about duplicates in the same sentence.
                HashMap<Integer, Integer> wordCounts = index.get(word);
                int prevCount = wordCounts.get(i) == null ? 0 : wordCounts.get(i);
                wordCounts.put(i, prevCount + 1);
                if (prevCount + 1 > maxWordCounts[i]) {
                    maxWordCounts[i] = prevCount + 1;
                }
            }
        }
    }

    public TreeMap<Integer, Double> getCentroidScores() {
        HashMap<String, Double> wordCentroidScores = getCentroidScoresForWords();
        TreeMap<Integer, Double> centroidScores = new TreeMap<Integer, Double>();
        int i = 0;
        for (List<String> sentence : rawDocument) {
            double centroidScore = 0.0;
            for (String word : sentence) {
                centroidScore += wordCentroidScores.get(word);
            }
            centroidScores.put(i, centroidScore);
            i += 1;
        }
        return centroidScores;
    }

    private HashMap<String, Double> getCentroidScoresForWords() {
        generateIdfScores();
        HashMap<String, Double> centroidScores = new HashMap<String, Double>();
        for (String word : idfScores.keySet()) {
            double idf = idfScores.get(word);
            HashMap<Integer, Integer> tfs = index.get(word);
            int totalTf = 0;
            for (int sentNum : tfs.keySet()) {
                totalTf += tfs.get(sentNum);
            }
            centroidScores.put(word, idf * totalTf);
        }
        return centroidScores;
    }

    public double[][] getCosineSimilarity() {
        generateTfIdfScores();
        double[][] adjMatrix = new double[numSentences][numSentences];
        double[] modulus = new double[numSentences];

        // Calculating modulus for each node. TODO: Do we need to normalize by length?
        for (int i = 0; i < numSentences; ++i) {
            double modI = 0.0;
            HashMap<String, Double> vectorI = tfIdfScores[i];
            for (String key : vectorI.keySet()) {
                modI += vectorI.get(key) * vectorI.get(key);
            }
            modI = Math.sqrt(modI);
            modulus[i] = modI;
        }
        for (int i = 0; i < numSentences; ++i) {
            double modI = modulus[i];
            HashMap<String, Double> vectorI = tfIdfScores[i];
            // TODO: changing j = 0 to j = i. Check once
            for (int j = i; j < numSentences; j++) {
                double modJ = modulus[j];
                HashMap<String, Double> vectorJ = tfIdfScores[j];
                double dotProduct = 0.0;
                for (String key : vectorI.keySet()) {
                    if (vectorJ.get(key) != null) {
                        dotProduct += vectorI.get(key) * vectorJ.get(key);
                    }
                }
                dotProduct /= (modI * modJ);
                adjMatrix[i][j] = adjMatrix[j][i] = dotProduct;
            }
        }
        return adjMatrix;
    }

    /*
     * Generates the idf scores.
     */
    private void generateIdfScores() {
        for (Entry<String, HashMap<Integer, Integer>> entry : index.entrySet()) {
            double idf = Math.log10((numSentences+0.0) / (entry.getValue().size()+0.0));
            idfScores.put(entry.getKey(), idf);
        }
    }

    private void generateTfIdfScores() {
        generateIdfScores();
        tfIdfScores = new HashMap[numSentences];
        for (int i = 0; i < numSentences; ++i) {
            tfIdfScores[i] = new HashMap<String, Double>();
        }
        for (Entry<String, HashMap<Integer, Integer>> entry : index.entrySet()) {
            double idf = idfScores.get(entry.getKey());
            for (Entry<Integer, Integer> sentence : entry.getValue().entrySet()) {
                double tf = 0.5 + (0.5 * sentence.getValue()) /100.0;  //maxWordCounts[sentence.getKey()];
                // System.out.println("TF"+sentence.getValue());	
                tfIdfScores[sentence.getKey()].put(entry.getKey(), idf * tf);
            }
        }
    }
}
