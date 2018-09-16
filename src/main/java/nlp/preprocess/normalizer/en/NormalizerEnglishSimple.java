package nlp.preprocess.normalizer.en;

import java.util.HashMap;
import java.util.Map.Entry;

import tools.util.Str;

public class NormalizerEnglishSimple {

	
	private static final String CharacterNormalizationFileAddress = tools.util.File.getParent(tools.util.File.getParent(tools.util.File.getCurrentDirectory()))+"/NLP/src/nlp/preprocess/normalizer/en/charNormalization.en";
	private HashMap<Character, Character> charToChar;
	private HashMap<Character, Character> charToCharCaseSensetive;
	char currentChar;
	StringBuilder rawText;
	
	public NormalizerEnglishSimple() {
		charToChar = tools.util.file.Reader
				.getKeyValueCharacterCharacterFromTextFile(CharacterNormalizationFileAddress, ' ', "\t", false);
		charToCharCaseSensetive=new HashMap<Character, Character>(charToChar.size());
		char tempChar;
		int caseSensitiveDelta='A'-'a';
		for (Entry<Character, Character> entry: charToChar.entrySet()) {
			tempChar=entry.getValue();
			if(tempChar>='A' && tempChar<='Z')
				tempChar=(char)(tempChar-caseSensitiveDelta);
			charToCharCaseSensetive.put(entry.getKey(), tempChar);
		}
		rawText=new StringBuilder();
	}
	
	public String normalize(String temString,boolean caseSensitive) {
		rawText.setLength(0);
		for (int i = 0; i < temString.length(); i++) {
			currentChar = temString.charAt(i);
			if (this.charToChar.containsKey(currentChar)){
				if(caseSensitive)
					rawText.append(this.charToCharCaseSensetive.get(currentChar));
				else
					rawText.append(this.charToChar.get(currentChar));
			}
			else
				rawText.append(' ');
		}
		return rawText.toString();
	}
}
