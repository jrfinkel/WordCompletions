package keyboard.wordcompletions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;

public class NGramModel {

	private static Pattern punctPattern = Pattern.compile("(\\p{Punct})");
	
	public static LinkedList<String> processLine(String line, boolean inApp) {
		line = line.trim();
		if (inApp) {
			line = punctPattern.matcher(line).replaceAll(" $1 ");
		}
		String[] words = line.split("\\s+");
		
		LinkedList<String> output = new LinkedList<String>();
		if (inApp) { output.add("<BOT>"); }
		for (String word : words) {
			if (word.length() > 0) {
				output.addLast(word);
			}
		}
		
		return output;
	}
	
	final private Map<String,NGramNode> bigrams = new HashMap<String,NGramNode>();
	
	private void addNGram(LinkedList<String> words, double score) {
	
		String word = words.pollLast();		
		String prevWord = words.pollLast();
		
		if (prevWord != null) {
			NGramNode child = bigrams.get(prevWord);
			if (child == null) {
				child = new NGramNode(prevWord);
				bigrams.put(prevWord,child);
			}
			child.addNGram(word, words, score);
		}
	}

	public Counter<String> getBigramPrediction(String prevWord) {
		Counter<String> bigramScores = bigrams.get(prevWord).getWordCounts();
		Counters.normalize(bigramScores);
		return bigramScores;
	}
	
	public Counter<String> getScores(Set<String> possibleWords, LinkedList<String> prevWords) {
		String prevWord = prevWords.removeLast();
		NGramNode node = bigrams.get(prevWord);
		if (node == null) { return new ClassicCounter<String>(); }
		
		return node.getScores(possibleWords, prevWords);
	}	
	
	public static NGramModel getModel(String modelFile, WordCompletionModel unigramModel) throws IOException {
		NGramModel model = new NGramModel();
		
		BufferedReader in = new BufferedReader(new FileReader(new File(modelFile)));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.trim().length() == 0) { continue; }
			String[] bits = line.trim().split("\\s+", 2);
			double score = Double.valueOf(bits[0]);
			LinkedList<String> words = processLine(bits[1], false);
			if (unigramModel != null) { unigramModel.addCompletion(words.peek(), 1.0); }
			model.addNGram(words, score);
		}	
		in.close();	
		return model;
	}
	
	public static void main(String[] args) throws IOException {

		NGramModel model = getModel(args[0], null);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("prev words >> ");
		String line;
		while ((line = in.readLine()) != null) {		
			LinkedList<String> prevWords = processLine(line, true);
			System.out.print("potential words >> ");
			Set<String> potentialWords = new HashSet<String>(processLine(in.readLine(), false));
			Counter<String> scores = model.getScores(potentialWords, prevWords);
			System.out.println(Counters.toSortedListWithCounts(scores));
			System.out.print("prev words >> ");
		}
		
	}

}
