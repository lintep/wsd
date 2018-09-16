package nlp.preprocess.fa;

import java.util.Arrays;

public class TokenizerUtils {
    static final String[] Conjunctives = new String[] { "و", "به",
            "با", "در", "از", "های", "ها", "این", "ما", "می", "را", "برای",
            "است", "شده", "که", "شما", "درباره", "کنید", "تا", "باشد", "بر",
            "یا", "شد", "آن", "شود", "همه", "من", "هم", "هر", "کرده", "کرد",
            "دیگر", "روی", "بین", "کردن", "بی", "دارد", "پیش", "کند", "بود",
            "زیر", "نیست", "اند", "کنند", "خواهد", "هستند", "شوید", "هایی",
            "رو", "گفت", "تر", "داد", "بوده", "آنها", "گرفته", "نمایید",
            "نشده", "کنیم", "باشید", "دهد", "اید", "اینکه", "شوند",
            "هایی", "نیز", "اما", "رو", "گفت",
            "تر", "ترین", "ندارد", "باز", };

    static {
        Arrays.sort(Conjunctives);
    }

    static public boolean isConjunctive(String s) {
        return Arrays.binarySearch(Conjunctives, s) >= 0;
    }

}
