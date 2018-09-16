package nlp.preprocess;

import java.util.List;

/**
 * Created by Saeed on 12/30/2016.
 */
public interface TokenizerInterface {

    List<String> tokenize(String text);

}
