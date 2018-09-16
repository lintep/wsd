package nlp.preprocess.fa;

public class NormalizerUtils {

    public static boolean isHarkat(char ch) {
        return (ch >= '\u064b' && ch <= '\u065f');
    }

    public static boolean isSurrogate(char ch) {
        return ch >= Character.MIN_SURROGATE;
    }

    public static boolean isControl(char ch) {
        return ch <= '\u001F';
    }

    public static boolean isTatweel(char ch) {
        return ch == '\u0640';
    }

    public static boolean isWhite(char ch) {
        return Character.isWhitespace(ch) || ch == '\u200c' || // Nim-Fasele
                ch == '\ufffd'; // Replace char
    }

}
