package nlp.similarity;

import nlp.similarity.type.LanguageModelType;

import java.io.IOException;

/**
 * Created by Saeed on 12/22/14.
 */
public class TestCorpusBaseSimilarity {
    public static void main(String[] args) throws IOException {
        System.out.println("hi: ");
        CorpusBaseSimilarity corpusBaseSimilarity=new CorpusBaseSimilarity("resources/ctext");
//        System.out.println(sentenceSimilarity.getTFIDFCosinSimilarity("it is a book", "it a is  a book a book"));
        String q="Where I can buy good oil for massage ?";
        String c1="I  ve done it once at the Sharq Village & Spa ... It  s great";
        String c2="You might be able to find Body Massage Oil in Body Shop at Landmark or City Centre , and if they do have it there , its guaranteed to be good for massage , as some places sell duplicates , so watch out as this could cause allergies to the skin . Good Luck !";

        System.out.println("unigramCosineScore");
        System.out.println(corpusBaseSimilarity.cosineSimilarity(q, q, LanguageModelType.UNIGRAM));
        System.out.println(corpusBaseSimilarity.cosineSimilarity(q, c2, LanguageModelType.UNIGRAM));

        System.out.println("bigramCosineScore");
        System.out.println(corpusBaseSimilarity.cosineSimilarity(q, q, LanguageModelType.BIGRAM));
        System.out.println(corpusBaseSimilarity.cosineSimilarity(q, c2, LanguageModelType.BIGRAM));

        System.out.println("trigramCosineScore");
        System.out.println(corpusBaseSimilarity.cosineSimilarity(q, q, LanguageModelType.TRIGRAM));
        System.out.println(corpusBaseSimilarity.cosineSimilarity(q, c2, LanguageModelType.TRIGRAM));
    }

}
