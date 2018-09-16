package nlp.analyzer.data.text;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.CharsRef;

public class SlidingWindow {
	private final CharArraySet dictionary;
	private final int maxWindowSize;
	private final CharsRef matchedTerm;
	private final int[] endOffsets;
	
	private CharsRef joinedTerm;
	private boolean isMatched;
	private int size;
	
	public SlidingWindow(CharArraySet dictionary, int maxWindowSize) {
		this.dictionary = dictionary;
		this.maxWindowSize = maxWindowSize;
		this.joinedTerm = new CharsRef();
		this.matchedTerm = new CharsRef();
		endOffsets = new int[maxWindowSize];
		size = 0;
	}
	
	private static void growForConcat(CharsRef ref, int additionalLength) {
		int finalRequiredSize = ref.offset + ref.length + additionalLength;
		
		ref.chars = ArrayUtil.grow(ref.chars, finalRequiredSize);
	}
	
	private void concat(CharsRef ref1, CharsRef ref2) {
		growForConcat(ref1, ref2.length);
		
		int offset = ref1.offset;
		
		offset += ref1.length;
		
		System.arraycopy(ref2.chars, ref2.offset, ref1.chars, offset, ref2.length);
		offset += ref2.length;
		
		ref1.length += ref2.length;
	}
	
	public boolean add(CharsRef token) {
		concat(joinedTerm, token);
		endOffsets[size++] = joinedTerm.length;
		if (size == maxWindowSize) {
			return match();
		}
		return false;
	}
	
	/**
	 * @return true if has more tokens
	 */
	public boolean finalizeWindow() {
		if (size == 0)
			return false;
		boolean matched = match();
		if (matched)
			return true;
		if (size == 0)
			return false;
		return true;
	}
	
	private boolean match() {
		shallowCopy(joinedTerm, matchedTerm);
		for(int currentOffset = size - 1; currentOffset >= 0; currentOffset --) {
			matchedTerm.length = endOffsets[currentOffset];
			boolean contains = dictionary.contains(matchedTerm.chars, matchedTerm.offset, matchedTerm.length);
			if (contains) {
				slide(currentOffset + 1);
				return isMatched = true;
			}
		}
		slide(1);
		return isMatched = false;
	}
	
	private void slide(int num) {
		int offset = endOffsets[num - 1];
		subCharsRefReUse(joinedTerm, offset);
		
		for (int i = num; i < endOffsets.length; i++) {
			endOffsets[i - num] = endOffsets[i] - offset;
		}
		size -= num;
	}
	
	public boolean isMatched() {
		return isMatched;
	}
	
	public CharsRef getMatchedTerm() {
		return isMatched ? matchedTerm : null;
	}
	
	public void reset() {
		joinedTerm.length = 0;
		size = 0;
	}
	
	public static void subCharsRefReUse(CharsRef ref, int offset) {
		subCharsRefReUse(ref, offset, ref.length - offset);
	}
	
	public static void subCharsRefReUse(CharsRef ref, int offset, int length) {
		ref.offset += offset;
		ref.length = length;
	}
	
	public static void shallowCopy(CharsRef from, CharsRef to) {
		shallowCopy(from.chars, from.offset, from.length, to);
	}
	
	public static void shallowCopy(CharTermAttribute from, CharsRef to) {
		shallowCopy(from.buffer(), 0, from.length(), to);
	}
	
	public static void shallowCopy(char[] fromChars, int fromOffset, int fromLength, CharsRef to) {
		to.chars = fromChars;
		to.offset = fromOffset;
		to.length = fromLength;
	}
}