package keyboard.wordcompletions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NGramNode {

	final private Map<String,NGramNode> children = new HashMap<String,NGramNode>();
	private double score = 0;
	
	public double score() { return score; }

	public void finishSetup() {
		//if (score == 0) { score = 0; }
		//else { score = 1 + Math.log(1 + Math.log(score)); }
	
		for (NGramNode n : children.values()) {
			n.finishSetup();
		}
	}
	
	public void addNGram(LinkedList<String> prevWords, double count) {
		
		if (prevWords.isEmpty()) {
			this.score += count;
		} else {
			String prevWord = prevWords.poll();
			NGramNode child = children.get(prevWord);
			if (child == null) {
				child = new NGramNode();
				children.put(prevWord,child);
			}
			child.addNGram(prevWords, count);
		}
	}
	
	public double prune(double threshold) {
		
		double score = this.score;
		
		Set<String> toRemove = new HashSet<String>();
		for (String word : children.keySet()) {
			NGramNode n = children.get(word);
			double s = n.prune(threshold);
			if (s <= threshold) { toRemove.add(word); }
			else { score += s; }
		}
		
		for (String word : toRemove) {
			children.remove(word);
		}
		
		if (threshold > 1.0) {
			score /= threshold;
		}
		
		return score;
	}
	
	Object scoreCacheKey = null;
	double scoreCacheVal = 0.0;
	
	public double getScore(List<String> prevWords, int pos) {
		
		double score = this.score;
		
		if (pos < prevWords.size()) {
			String prevWord = prevWords.get(pos);
			NGramNode child = children.get(prevWord);
			if (child != null) {
				double cScore = child.getScore(prevWords, pos+1);
				//System.out.print(prevWord+" ("+cScore+") --> ( + "+count+" = ) ");
				score += cScore;
			}
		}
		
		return score;
	}
	
}


