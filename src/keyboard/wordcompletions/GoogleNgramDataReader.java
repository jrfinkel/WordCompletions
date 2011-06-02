package keyboard.wordcompletions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import edu.stanford.nlp.util.BinaryHeapPriorityQueue;
import edu.stanford.nlp.util.PriorityQueue;

public class GoogleNgramDataReader {

  public static void main(String[] args ) throws Exception {

    PriorityQueue<String> queue = new BinaryHeapPriorityQueue<String>();
    int maxSize = Integer.parseInt(args[args.length-2]);

    Pattern toKeep = Pattern.compile("([a-z]|[A-Z]|[0-9])+([a-z]|[A-Z]|[0-9]|[,-.!])*");

    int lineCount = 0;
    for (int i = 0; i < args.length - 2; i++) {
      BufferedReader in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(args[i]))));
      String line;
      LOOP: while ((line = in.readLine()) != null) {
        if (++lineCount % 1000000 == 0) { System.err.print("."); }
        String[] bits = line.trim().split("\t");
        String word = bits[0];
        String[] words = word.split("\\s+");
        if (words.length > 1) {
          for (String w : words) {
            Matcher m = toKeep.matcher(w);
            if (!m.matches()) { continue LOOP; }
          }
        }

        double count = Math.log(Double.valueOf(bits[1]));
        queue.add(word, -count);
        if (queue.size() > maxSize) { queue.removeFirst(); }
      }
      in.close();
    }
    System.err.println();
    System.err.println();

    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[args.length-1])));
    while (!queue.isEmpty()) {
      out.println((int)(-queue.getPriority())+"\t"+queue.removeFirst());
    }
    out.close();
  }

}
