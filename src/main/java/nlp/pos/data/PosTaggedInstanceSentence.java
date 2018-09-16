package nlp.pos.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saeed on 1/23/15.
 */
public class PosTaggedInstanceSentence {
    int id;
    TokenPos[] tokenPosArray;

    public PosTaggedInstanceSentence(int id, TokenPos[] tokenPosesArray) {
        this.id=id;
        this.tokenPosArray=tokenPosesArray;
    }

    public int getId() {
        return id;
    }

    public TokenPos[] getTokenPosArray() {
        return tokenPosArray;
    }

    public void load(int id, TokenPos[] tokenPosList) {
        this.id = id;
        this.tokenPosArray = tokenPosList;
    }


    public String getTokensAsString(){
        StringBuilder tempStringBuilder=new StringBuilder();
        tempStringBuilder.setLength(0);
        for (TokenPos tokenPos:tokenPosArray){
            tempStringBuilder.append(tokenPos.token);
            tempStringBuilder.append(" ");
        }
        if(tempStringBuilder.length()>0)
            tempStringBuilder.setLength(tempStringBuilder.length()-1);
        else
            System.out.println("NO -> id:"+this.id);
        return tempStringBuilder.toString();
    }

    public List<String> getCompoundTokens(){
        ArrayList<String> result=new ArrayList<String>();
        for (TokenPos tokenPos:tokenPosArray){
            if(tokenPos.token.indexOf(' ')>0)
                result.add(tokenPos.token);
        }
        return result;
    }
       
}
