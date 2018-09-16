package nlp.preprocess;

import java.util.List;

/**
 * Created by Saeed on 12/30/2016.
 */
public interface PosTaggerInterface {

    List<String> getPos(List<String> tokens);

}
