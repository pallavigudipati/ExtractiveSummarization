import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class InvertedIndex {

    public int numSentences;
    public HashMap<String, HashMap<Integer, Integer>> index = 
            new HashMap<String, HashMap<Integer, Integer>>();
    public HashMap<String, Double> idfScores = new HashMap<String, Double>();
    public HashMap<String, Double>[] tfIdfScores;
    public int[] maxWordCounts;

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

    /*
     * Generates the idf scores.
     */
    private void generateIdfScores() {
        for (Entry<String, HashMap<Integer, Integer>> entry : index.entrySet()) {
            double idf = Math.log10((numSentences+0.0) / (entry.getValue().size()+0.0));
            idfScores.put(entry.getKey(), idf);
        }
    }

    public void generateTfIdfScores() {
        generateIdfScores();
        tfIdfScores = new HashMap[numSentences];
        for (int i = 0; i < numSentences; ++i) {
            tfIdfScores[i] = new HashMap<String, Double>();
        }
        for (Entry<String, HashMap<Integer, Integer>> entry : index.entrySet()) {
            double idf = idfScores.get(entry.getKey());
            for (Entry<Integer, Integer> sentence : entry.getValue().entrySet()) {
                double tf = 0.5 + (0.5 * sentence.getValue()) /100.0;  //maxWordCounts[sentence.getKey()];
                System.out.println("TF"+sentence.getValue());	
                tfIdfScores[sentence.getKey()].put(entry.getKey(), idf * tf);
            }
        }
    }
    
}
