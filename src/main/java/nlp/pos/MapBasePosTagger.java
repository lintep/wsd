package nlp.pos;

import nlp.preprocess.PosTaggerInterface;
import nlp.text.Utils;
import tools.util.file.Reader;

import java.util.*;

/**
 * Created by Saeed on 12/31/2016.
 */
public class MapBasePosTagger implements PosTaggerInterface {

    Set<Character> punctuationsChar;

    HashMap<String, String> tokenPos;

    public MapBasePosTagger(String mapFileAddress){
        tokenPos = Reader.getKeyValueFromTextFile(mapFileAddress, true, "\t");
        punctuationsChar=new HashSet<>();
        punctuationsChar.addAll(Utils.TokenSplitterNumChar);
        punctuationsChar.addAll(Utils.TokenFixSplitterChar);
        punctuationsChar.addAll(Utils.SentenceFixSplitterChar);
        System.out.println("token pos count: "+tokenPos.size());
    }

    @Override
    public List<String> getPos(List<String> tokens) {
        List<String> result=new ArrayList<>();
        for (String token : tokens) {
            String pos="UNKNOWN";
            if(token.length()==1 && punctuationsChar.contains(token.charAt(0))){
                pos="PUNC";
            }
            else if(tokenPos.containsKey(token)){
                pos=tokenPos.get(token);
            }
            result.add(pos);
        }
        return result;
    }

}
