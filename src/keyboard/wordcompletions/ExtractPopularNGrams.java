package keyboard.wordcompletions;

import java	.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.StringUtils;

public class ExtractPopularNGrams {

  public static void main(String[] args) throws Exception {

    Pattern punctPattern = Pattern.compile("(\\p{Punct})");
    Counter<String> counter = new ClassicCounter<String>();
    int maxLength = Integer.parseInt(args[1]);
    BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));

    String line;
    while ((line = in.readLine()) != null) {
      line = line.trim();
      List<String> words = new ArrayList<String>();
      words.add("<BOT>");
      for (String word : punctPattern.matcher(line.trim()).replaceAll(" $1 ").split("\\s+")) {
        words.add(word);
      }

      for (int end = 1; end < words.size(); end++) {
        List<String> ngram = words.subList((int)Math.max(0,end-maxLength), end);
        counter.incrementCount(StringUtils.join(ngram, " "));
      }
    }

    Set<String> keys = counter.keySet();

    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[args.length-1])));
    for (String k : keys) {
      out.println((int)(counter.getCount(k))+"\t"+k);
    }
    out.close();

  }

}
