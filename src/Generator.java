import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

public class Generator{
    private InputDocument inputDoc;
    
    // compression ratio for the text summary
    // i.e., here compression is 1/3 of the original text
    private double compressRatio = 0.3;
    
    private String[] keywords = null;
    
    private TermCollection termCollection;
    
    public void loadFile(String inputFile){
        inputDoc = new InputDocument();
        inputDoc.loadFile(inputFile);
    }
    
    public void setKeywords(String[] keywords){
        
        List<String> processedTermList = new ArrayList<String>();
        TermPreprocessor tp = new TermPreprocessor();
        
        String resultTerm = null;
        for(String term : keywords){
            resultTerm = tp.preprocess(term);
            
            if(resultTerm !=null)
                processedTermList.add(resultTerm);
        }
        
        this.keywords = processedTermList.toArray(new String[processedTermList.size()]);
        
    }
    
    public String generateSummary(){
        String[] significantSentences = generateSignificantSentences();
        return Arrays.toString(significantSentences);
    }
    
    public String[] generateSignificantSentences(){
        String[] allSentences = getAllSentences();
        double[] scores = calcAllSentenceScores(allSentences);
        double[] sortedScores = new double[scores.length];
        System.arraycopy( scores, 0, sortedScores, 0, scores.length );
        Arrays.sort(sortedScores);
        String[] significantSentences = new String[(int)Math.ceil(allSentences.length*compressRatio)];
        int j = 0;
        for(int i = 0; i < allSentences.length; ++i){
            if(scores[i] > sortedScores[scores.length - significantSentences.length - 1]){
                significantSentences[j] = allSentences[i];
                ++j;
            }
        }
        return significantSentences;
    }
    
    public String[] getAllSentences(){
        return inputDoc.getAllSentences();
    }
    
    public double[] calcAllSentenceScores(String[] sentences){
        double[] scores = new double[sentences.length];
        TermCollectionProcessor tc = new TermCollectionProcessor();
        tc.insertAllTerms(inputDoc.getAllTerms());
        tc.computeRelativeFrequencies();
        for(int i = 0; i < sentences.length; ++i){
            double score = 0;
            TextExtractor words = new TextExtractor();
            words.setText(sentences[i]);
            for(String word : words.extractTerms()){
                String processed = tc.preprocess(word);
                if(processed != null){
                    Word w = tc.findWordByValue(processed);
                    score += w.getRelativeFrequency();
                }
            }
            scores[i] = score;
        }
        return scores;
    }
}
