package keyboard.wordcompletions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;

public class NGramNode {

  final public String ngram;
  private Counter<String> wordCounts = new ClassicCounter<String>();	
  final private Map<String,NGramNode> children = new HashMap<String,NGramNode>();

  public String toString() {
    return ngram+" --> "+wordCounts;
  }

  public NGramNode (String ngram) {
    this.ngram = ngram;
  }

  public NGramNode getChild(String word) {
    return children.get(word);
  }

  public double getCount(String word) {
    return wordCounts.getCount(word);
  }

  public double totalCount() {
    return wordCounts.totalCount();
  }

  public Counter<String> getWordCounts() {
    return new ClassicCounter(wordCounts);
  }

  public void addNGram(String word, LinkedList<String> prevWords, double count) {
    wordCounts.incrementCount(word, count);

    if (!prevWords.isEmpty()) {
      String prevWord = prevWords.removeLast();
      NGramNode child = children.get(prevWord);
      if (child == null) {
        child = new NGramNode(prevWord+" "+ngram);
        children.put(prevWord,child);
      }
      child.addNGram(word, prevWords, count);
    }
  }

  public Counter<String> getScores(Set<String> possibleWords, LinkedList<String> prevWords) {
    Counter<String> scores = new ClassicCounter<String>();
    if (possibleWords.isEmpty()) {
      scores.addAll(wordCounts);
    }

    for (String word : possibleWords) {
      scores.incrementCount(word, getCount(word));
    }

    if (scores.totalCount() > 0.0) {
      Counters.normalize(scores);
    } else {
      return scores;			
    }

    if (prevWords.isEmpty()) { return scores; }
    String w = prevWords.removeLast();	

    NGramNode node = getChild(w);

    if (node == null) { return scores; }	

    scores.addAll(node.getScores(possibleWords, prevWords));

    return scores;
  }

}


