package nlp.preprocess.fa;

import nlp.preprocess.NormalizerInterface;

import java.io.Serializable;

import static nlp.preprocess.fa.NormalizerUtils.*;

/**
 * Created by Saeed on 1/2/2015.
 */
public class SimpleNormalizer implements NormalizerInterface,Serializable {
	static final char[] table = new char[256 * 256];
	private final static char[][] char2replace = {
			{'۰', '0'},//
			{'۱', '1'},//
			{'۲', '2'},//
			{'۳', '3'},//
			{'۴', '4'},//
			{'۵', '5'},//
			{'۶', '6'},//
			{'۷', '7'},//
			{'۸', '8'},//
			{'۹', '9'},//
			{'٠', '0'},//
			{'١', '1'},//
			{'٢', '2'},//
			{'٣', '3'},//
			{'٤', '4'},//
			{'٥', '5'},//
			{'٦', '6'},//
			{'٧', '7'},//
			{'٨', '8'},//
			{'٩', '9'},//
			{'أ', 'ا'},// Arabic Alef with Hamza Above
			{'إ', 'ا'},// Arabic Alef with Hamza Below
			{'ؤ', 'و'},// Arabic Waw with Hamza Above
			{'ۇ', 'و'},// Arabic U
			{'ۈ', 'و'},// Arabic Yu
			{'\u08aa', 'ر'},// Arabic Reh with Loop
			{'ك', 'ک'},// Arabic Kaf
			{'\u06aa', 'ک'},// Arabic Swash Kaf (Keh jangulaki)
			{'ي', 'ی'},// Arabic Yeh
			{'ى', 'ی'},// Arabic Alef Maksura
			{'\u0620', 'ی'},// Kashmiri Yeh
			{'ھ', 'ه'}, // Heh Doachashmee
			{'\u0629', 'ه'}, // Arabic Tah Marbuta (Heh do noghteh)
			{'ۀ', 'ه'}, // Arabic Heh with Yeh Above
			{'ە', 'ه'}, // Arabic Ae
			{'ء', ' '}, // Arabic Hamza Seems unsafe
			{'\u202C',' '} // unchecked char specific
	};

	static {
		for (int i = 0; i < table.length; i++)
			table[i] = 0;
		for (int i = 0; i < char2replace.length; i++)
			table[char2replace[i][0]] = char2replace[i][1];
	}

	public String normalize(String s) {
		String str = s.toLowerCase();
		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (isHarkat(ch) || isTatweel(ch)) // removes harkats and Tatweels
				continue;

			if (isSurrogate(ch)) // converts surrogates to space
				ch = ' ';

			if (isControl(ch)) // converts Control Chars to space
				ch = ' ';

			if (isWhite(ch)) // converts White Chars to space
				ch = ' ';

			if (ch < table.length && table[ch] != 0)
				ch = table[ch];

			builder.append(ch);
		}

		return builder.toString();
	}

}
