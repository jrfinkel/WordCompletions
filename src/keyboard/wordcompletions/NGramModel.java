package keyboard.wordcompletions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.TwoDimensionalCounter;

public class NGramModel {

	final private Map<String,NGramNode> children = new HashMap<String,NGramNode>();
	final private TwoDimensionalCounter<String,String> bigrams = new TwoDimensionalCounter<String,String>();
	
	public void addNGram(LinkedList<String> words, double score) {
	
		String word = words.poll();
		
		if (words.size() == 1) {
			String prevWord = words.peek();
			bigrams.incrementCount(prevWord, word, score);
		}
		
		NGramNode child = children.get(word);
		if (child == null) {
			child = new NGramNode();
			children.put(word,child);
		}
		child.addNGram(words, score);
		
	}

	public Counter<String> getBigramPrediction(String prevWord) {
		return bigrams.getCounter(prevWord);
	}
	
	public void prune(double threshold) {		
		Set<String> toRemove = new HashSet();
		for (String word : children.keySet()) {
			NGramNode n = children.get(word);
			double s = n.prune(threshold);
			if (s < threshold) { toRemove.add(word); }
		}
		for (String n : toRemove) {
			children.remove(n);
		}
		System.out.println();
	}
	
	public double getScore(String word, List<String> prevWords) {
		NGramNode n = children.get(word);
		if (n == null) { return 0.0; }
		else { 
			double score = n.getScore(prevWords, 0); 
			//System.out.println(" [[ "+word+" / "+score+" ]] ");
			return score;
		}
	}
	
	public static LinkedList<String> processLine(String line) {
		String[] words = line.split("\\s+");
		
		LinkedList<String> output = new LinkedList<String>();
		for (int i = words.length-1; i >= 0; i--) {
			output.add(words[i]);
		}
		
		return output;
	}	
	
	public static NGramModel getModel(String modelFile) throws IOException {
		NGramModel model = new NGramModel();
		
		BufferedReader in = new BufferedReader(new FileReader(new File(modelFile)));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.trim().length() == 0) { continue; }
			String[] bits = line.trim().split("\\s+", 2);
			double score = Double.valueOf(bits[0]);
			LinkedList<String> words = processLine(bits[1]);
			model.addNGram(words, score);
		}	
		in.close();
	
		model.prune(1);
		
		for (NGramNode n : model.children.values()) {
			n.finishSetup();
		}		
		
		for (String prevWord : model.bigrams.firstKeySet()) {
			Counter<String> counts = model.bigrams.getCounter(prevWord);
			for (String word : counts.keySet()) {
				model.bigrams.setCount(prevWord, word, 1 + Math.log(1 + Math.log(counts.getCount(word))));
			}
		}
		
		return model;
	}
	
	public static void main(String[] args) throws IOException {

		NGramModel model = getModel(args[0]);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(">> ");
		String line;
		while ((line = in.readLine()) != null) {		
			LinkedList<String> words = processLine(line);
			String word = words.poll();
			List<String> prevWords = new ArrayList(words);
			System.out.println(model.getScore(word, prevWords));
			System.out.print(">> ");
		}
		
	}

}
