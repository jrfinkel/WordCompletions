package keyboard.wordcompletions;

import java.util.HashMap;
import java.util.Map;

import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;

public class WordCompletionNode {

	final public char c;
	final public int depth;
	final public Counter<String> completions = new ClassicCounter<String>();
	
	final private Map<Character,WordCompletionNode> children = new HashMap<Character,WordCompletionNode>();
	public WordCompletionNode getChild(char c) {
		return children.get(c);
	}
	
	public WordCompletionNode (char c, int depth) {
		this.c = c;
		this.depth = depth;
	}
	
	public WordCompletionNode getNode (String prefix) {
		if (prefix.length() == depth+1) { return this; }
		return children.get(prefix.charAt(depth+1)).getNode(prefix);
	}
	
	public void addCompletion (String word, double score) {
		if (word.length() == depth+1) { 
			completions.incrementCount(word, score);
		} else {
			int newDepth = depth+1;
			char nextC = word.charAt(newDepth);
			WordCompletionNode n = children.get(nextC);
			if (n == null) { 
				n = new WordCompletionNode(nextC, newDepth); 
				children.put(nextC, n);
			}
			n.addCompletion(word, score);
		}
	}
	
	public void addCompletion(String typed, String completeWord) {
		if (typed.length() == depth+1) { 
			completions.incrementCount(completeWord);
		} else {
			int newDepth = depth+1;
			char nextC = typed.charAt(newDepth);
			WordCompletionNode n = children.get(nextC);
			if (n == null) { 
				n = new WordCompletionNode(nextC, newDepth); 
				children.put(nextC, n);
			}
			n.addCompletion(typed, completeWord);
		}
	}
	
	public Counter<String> getAllCompletions() {
		Counter<String> all = new ClassicCounter<String>(completions);
		for (WordCompletionNode n : children.values()) {
			all.addAll(n.getAllCompletions());
		}
		return all;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		StringBuilder spaces = new StringBuilder(" ");
		for (int i = 0; i < depth; i++) { sb.append("-"); spaces.append(" "); }
		sb.append(">");
		sb.append(c);
		sb.append("\n");
		sb.append(spaces);
		sb.append(completions);
		sb.append("\n");
		for (WordCompletionNode n : children.values()) {
			sb.append(n.toString());
		}
		
		return sb.toString();
	}
	
}
