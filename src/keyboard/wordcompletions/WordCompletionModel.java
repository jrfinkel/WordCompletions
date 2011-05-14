package keyboard.wordcompletions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;

public class WordCompletionModel {

	private NGramModel ngramModel;
	final private Map<Character,WordCompletionNode> children = new HashMap<Character,WordCompletionNode>();

	private void addCompletion(String word, double score) {
		char c = word.charAt(0);
		WordCompletionNode n = children.get(c);
		if (n == null) { 
			n = new WordCompletionNode(c, 0); 
			children.put(c, n);
		}
		n.addCompletion(word, score);
	}
	
	private void addCompletion(String typed, String completeWord) {
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
		return n.getAllCompletions();
	}
	
	public static WordCompletionModel getModel(String unigramFile, String ngramFile) throws IOException {
		
		WordCompletionModel model = new WordCompletionModel();
		
		BufferedReader in = new BufferedReader(new FileReader(new File(unigramFile)));
		String line;
		while ((line = in.readLine()) != null) {
			if (line.trim().length() == 0) { continue; }
			//System.err.println(line);
			String[] bits = line.trim().split("\\s+");
			String word = bits[1];			
			double score = 1 + Math.log(1 + Math.log(Integer.valueOf(bits[0])));
			model.addCompletion(word, score);
		}	
		in.close();		
		
		model.ngramModel = NGramModel.getModel(ngramFile);
		
		return model;
	}
	
	public static void main(String args[]) throws IOException {
		WordCompletionModel model = getModel(args[0], args[1]);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		System.out.print(">> ");
		String line;
		while ((line = in.readLine()) != null) {			
			LinkedList<String> words = NGramModel.processLine(line);
			Counter<String> completions;
			
			if (line.charAt(line.length()-1) == ' ') {
				completions = model.ngramModel.getBigramPrediction(words.peek());
			} else {
				String prefix = words.poll();
				completions = model.getUnigramScores(prefix);
			}
			List<String> prevWords = new ArrayList<String>(words);
			
			for (String s : completions.keySet()) {
				double unigramScore = completions.getCount(s);
				double ngramScore = 2*model.ngramModel.getScore(s, prevWords);
				System.out.print(s+" ==> "+unigramScore+" + "+ngramScore+" = "+(unigramScore+ngramScore));
				completions.incrementCount(s, ngramScore);
				System.out.println(" << "+completions.getCount(s)+" >> ");
			}
			
			List<String> ordered = Counters.toSortedList(completions);
			int i = 0;
			for (String word : ordered) {
				System.out.print(word+"="+completions.getCount(word)+" ");
				if (++i == 10) { break; }
			}
			System.out.print("\n>> ");
		}	
	}
}
