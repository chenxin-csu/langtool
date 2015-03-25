package langtool;

import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import langtool.util.StatsUtil;

@SuppressWarnings("serial")
public enum CharacterUnifiyEnum {

	CN_ONLY("check_lang_cn", new HashSet<Character.UnicodeBlock>() {
		{
			add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
		}
	}), JP_CN("check_lang_cjk", new HashSet<Character.UnicodeBlock>() {
		{
			add(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS);
			add(Character.UnicodeBlock.HIRAGANA);
			add(Character.UnicodeBlock.KATAKANA);
		}
	}), EN("check_lang_en", "[a-zA-Z]"), NUMBER("check_lang_num", "[0-9]"),

	;
	private String checkboxName;
	private String regex;
	private boolean useReg;
	private Set<Character.UnicodeBlock> ubs = new HashSet<UnicodeBlock>();

	private CharacterUnifiyEnum(String checkboxName,
			Set<Character.UnicodeBlock> set) {
		this.checkboxName = checkboxName;
		ubs.addAll(set);
		this.useReg = false;
	}

	private CharacterUnifiyEnum(String checkboxName, String regex) {
		this.checkboxName = checkboxName;
		this.regex = regex;
		this.useReg = true;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public boolean isUseReg() {
		return useReg;
	}

	public void setUseReg(boolean useReg) {
		this.useReg = useReg;
	}

	public String getCheckboxName() {
		return checkboxName;
	}

	public void setCheckboxName(String checkboxName) {
		this.checkboxName = checkboxName;
	}

	public Set<Character.UnicodeBlock> getUbs() {
		return ubs;
	}

	public Set<Character.UnicodeBlock> getCharacterUbs() {
		return ubs;
	}

	public static long statsCharacterCnt(Map<String, String> params, String line) {
		char[] chars = line.toCharArray();
		long cnt = 0l;
		TwoTuple<Set<UnicodeBlock>, Set<String>> matcher = StatsUtil
				.getCharacterMatcherFromParams(params);
		Set<UnicodeBlock> unifySet = matcher.first;
		Set<String> regSet = matcher.second;
		for (int i = 0; i < chars.length; i++) {
			if (unifySet.contains(Character.UnicodeBlock.of(chars[i]))) {
				cnt++;
			} else {
				for (String regex : regSet) {
					if (("" + chars[i]).matches(regex)) {
						cnt++;
					}
				}
			}

		}
		return cnt;
	}

}
