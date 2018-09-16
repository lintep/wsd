package nlp.wsd.co.scorer;

import nlp.languagemodel.LanguageModelScorer;

import java.util.Map;

/**
 * Created by Saeed on 10/23/2016.
 */
public class BigramScorer {

    LanguageModelScorer languageModelScorer;

    public BigramScorer(LanguageModelScorer languageModelScorer){
        this.languageModelScorer=languageModelScorer;
    }

    public double getScore1(BigramScoreFunction bigramScoreFunction,String senseLemma,String wordLemma) throws
            Exception {
        double coScore = languageModelScorer.getScore(senseLemma, wordLemma);
        if(coScore>0) {
            switch (bigramScoreFunction) {
                case LINEAR:
                    return coScore;

                case LINEAR_NORMALIZED_WORDDF:
                    return coScore / languageModelScorer.getScore(wordLemma);

                case LINEAR_NORMALIZED_LOGWORDDF:
                    return coScore / (Math.log(languageModelScorer.getScore(wordLemma)));

                case LINEAR_NORMALIZED_WORDDF_SENSEDF:
                    return coScore / (languageModelScorer.getScore(wordLemma) * languageModelScorer.getScore(senseLemma));


                case LINEAR_NORMALIZED_WORDDF_LOGSENSEDF:
                    return coScore / (languageModelScorer.getScore(wordLemma) * Math.log(languageModelScorer.getScore
                            (senseLemma)));

                case LINEAR_NORMALIZED_LOGWORDDF_SENSEDF:
                    return coScore / (Math.log(languageModelScorer.getScore(wordLemma)) * languageModelScorer.getScore
                            (senseLemma));

                case LINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF:
                    return coScore / (Math.log(languageModelScorer.getScore(wordLemma)) * Math.log(languageModelScorer.getScore
                            (senseLemma)));

                case LOGLINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF:
                    return Math.log(coScore) / (Math.log(languageModelScorer.getScore(wordLemma)) + Math.log
                            (languageModelScorer.getScore(senseLemma)));
            }
        }
        return -1;
    }

    public double getScore(BigramScoreFunction bigramScoreFunction, Map<String,Double> lemmaVectorI, Map<String,Double>
            lemmaVectorII) {
        double score=0;
        for (Map.Entry<String, Double> lemmaIWeight : lemmaVectorI.entrySet()) {
            for (Map.Entry<String, Double> lemmaIIWeight : lemmaVectorII.entrySet()) {
                try {
                    double w1=lemmaIWeight.getValue();
                    double w2=lemmaIIWeight.getValue();
                    if(lemmaIWeight.getKey().equals(lemmaIIWeight.getKey())){
                        score+=w1*w2*getScore1(bigramScoreFunction,lemmaIWeight.getKey(),lemmaIWeight.getKey());
                    }
                    else{
                        score+=w1*w2*getScore1(bigramScoreFunction,lemmaIWeight.getKey(),lemmaIWeight.getKey());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return score;
    }

    public enum BigramScoreFunction {
        LINEAR("LINEAR"),
        LINEAR_NORMALIZED_WORDDF("LINEAR_NORMALIZED_WORDDF"),
        LINEAR_NORMALIZED_WORDDF_SENSEDF("LINEAR_NORMALIZED_WORDDF_SENSEDF"),
        LINEAR_NORMALIZED_WORDDF_LOGSENSEDF("LINEAR_NORMALIZED_WORDDF_LOGSENSEDF"),
        LINEAR_NORMALIZED_LOGWORDDF("LINEAR_NORMALIZED_LOGWORDDF"),
        LINEAR_NORMALIZED_LOGWORDDF_SENSEDF("LINEAR_NORMALIZED_LOGWORDDF_SENSEDF"),
        LINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF("LINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF"),
        LOGLINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF("LOGLINEAR_NORMALIZED_LOGWORDDF_LOGSENSEDF"),
        ;

        String string;

        BigramScoreFunction(String string) {
            string = string;
        }

        public String getString() {
            return string;
        }
    }

}
