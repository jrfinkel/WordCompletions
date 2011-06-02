package keyboard.wordcompletions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;

public class WordCompletionModel {

	private NGramModel ngramModel;
	final private Map<Character,WordCompletionNode> children = new HashMap<Character,WordCompletionNode>();

	public void addCompletion(String word, double score) {
		char c = word.charAt(0);
		WordCompletionNode n = children.get(c);
		if (n == null) { 
			n = new WordCompletionNode(c, 0); 
			children.put(c, n);
		}
		n.addCompletion(word, score);
	}
	
	public void addCompletion(String typed, String completeWord) {
		char c = typed.charAt(0);
		WordCompletionNode n = children.get(c);
		if (n == null) { 
			n = new WordCompletionNode(c, 0); 
			children.put(c, n);
		}
		n.addCompletion(typed, completeWord);
	}
	
	public Counter<String> getUnigramScores(String prefix) {
		char c = prefix.charAt(0);
		WordCompletionNode n = children.get(c).getNode(prefix);
		Counter<String> completions = n.getAllCompletions();
		Counters.normalize(completions);
		return completions;
	}
	
	public Counter<String> getScores(String line) {
		
		LinkedList<String> words = NGramModel.processLine(line, true);
		Counter<String> completions;
		
		if (line.length() == 0 || line.charAt(line.length()-1) == ' ') {
			completions = new ClassicCounter<String>();
		} else {
			String prefix = words.pollLast();
			completions = getUnigramScores(prefix);
		}
		
		completions.addAll(ngramModel.getScores(completions.keySet(), words));
		
		return completions;
	}
	
	public static WordCompletionModel getModel(String unigramFile, String ngramFile) throws IOException {
		
		WordCompletionModel model = new WordCompletionModel();
		
		BufferedReader in = new BufferedReader(new FileReader(new File(unigramFile)));
		String line;
		Counter<String> unigramCounts = new ClassicCounter<String>();
		while ((line = in.readLine()) != null) {
			if (line.trim().length() == 0) { continue; }
			String[] bits = line.trim().split("\\s+");
			String word = bits[1];			
			double score = 1 + Math.log(1 + Math.log(Integer.valueOf(bits[0])));
			model.addCompletion(word, score);
			unigramCounts.setCount(word,score);
		}	
		in.close();		
		
		model.ngramModel = NGramModel.getModel(ngramFile, model);
		
		return model;
	}
	
	public static void main(String args[]) throws IOException {
		WordCompletionModel model = getModel(args[0], args[1]);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(">> ");
		String line;
		while ((line = in.readLine()) != null) {			
			Counter<String> completions = model.getScores(line);
			System.out.println(Counters.toSortedListWithCounts(completions));
			System.out.print("\n>> ");
		}	
	}
}
