package keyboard.wordcompletions;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.util.StringUtils;

public class RemoveSGML {


  public static void main(String[] args) throws Exception {

    String content = IOUtils.slurpFile(args[0]);
    Pattern p = Pattern.compile("<[^>}*>", Pattern.DOTALL);
    Matcher m = p.matcher(content);
    content = m.replaceAll("");
    StringUtils.printToFile(new File(args[1]), content);

  }

}
