package langtool;

import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("serial")
public enum CharacterUnifiyEnum {

	CN_ONLY(new HashSet<Character.UnicodeBlock>() {
		{
			add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
		}
	}), JP_CN(new HashSet<Character.UnicodeBlock>() {
		{
			add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
			add(Character.UnicodeBlock.HIRAGANA);
			add(Character.UnicodeBlock.KATAKANA);
		}
	}), ;
	private Set<Character.UnicodeBlock> ubs = new HashSet<UnicodeBlock>();

	private CharacterUnifiyEnum(Set<Character.UnicodeBlock> set) {
		ubs.addAll(set);
	}

	public Set<Character.UnicodeBlock> getCharacterUbs() {
		return ubs;
	}

	public static long statsCharacterCnt(Set<UnicodeBlock> set, String line) {
		char[] chars = line.toCharArray();
		long cnt = 0l;
		for (int i = 0; i < chars.length; i++) {
			if (set.contains(Character.UnicodeBlock.of(chars[i]))) {
				cnt++;
			}
		}
		return cnt;
	}

}
