package keyboard.wordcompletions;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;

public class ExtractPopularNGrams {

	public static void main(String[] args) throws Exception {

		Counter<String> counter = new ClassicCounter<String>();
		
		int cutoff = Integer.parseInt(args[1]);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(args[0])));
		String line;
		while ((line = in.readLine()) != null) {
			String[] words = line.trim().split("\\s+");
			for (int i = 1; i < words.length; i++) {
				String ngram = words[i];
				for (int j = 1; j <= 5; j++) {
					int k = i-j;
					if (k < 0 || words[k].length() >= 15) { break; }
					ngram = words[k]+" "+ngram;
					counter.incrementCount(ngram);
				}
			}
		}

		Set<String> keys = Counters.keysAbove(counter, cutoff);
		
		Pattern wordP = Pattern.compile("[a-zA-Z',. ]+");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(args[args.length-1])));
		for (String k : keys) {
			Matcher m = wordP.matcher(k);
			if (m.matches()) {
				out.println((int)(counter.getCount(k))+"\t"+k);
			}
		}
		out.close();

	}

}
