package com.enn;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Properties;


public class Summarizer {
    private final String content;

    //constructor that accepts the extracted content
    public Summarizer(String content){
        this.content = content;
    }


    //SUMMARIZATION MODES//

    // Flash Mode - Uses Sumy (LexRank) for extractive summarization
    public String FlashModeSummarize(){
        return runPythonSummarization("flash_mode");
    }

    //Insight Mode - Uses Stanford CoreNLP for sentence segmentation
    public String insightModeSummarize(){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CoreDocument document = new CoreDocument(content);
        pipeline.annotate(document);

        StringBuilder output = new StringBuilder();
        output.append("📰 STRUCTURED ARTICLE VIEW:\n\n");

        for (CoreSentence sentence : document.sentences()){
            String sentText= sentence.text();
            List<String> nerTags = sentence.nerTags();

            if (nerTags.contains("DATE") || nerTags.contains("PERSON") || nerTags.contains("ORGANIZATION")) {
                output.append("• ").append(sentText).append("\n");
            } else if (sentText.length() > 50) { // skip very short/weak sentences
                output.append("  ").append(sentText).append("\n");
            }
        }

        return output.toString().trim();


    }

    // Deep Dive Mode - Uses DJL (BERT) for abstractive summarization
    public String deepDiveModeSummarize(){
        return runPythonSummarization("deep_dive_mode");
    }

    // Call Python script for Sumy LexRank or DJL BERT summarization
    private String runPythonSummarization(String mode){
        try{
            ProcessBuilder pb = new ProcessBuilder("python","summarizer.py", mode, content);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder summary = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                summary.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                return "Python script exited with code: " + exitCode + "\nOutput:\n" + summary;
            }

            return summary.toString().trim();
        } catch (Exception e) {
            return "Error running summarization: " + e.getMessage();
        }
    }
}