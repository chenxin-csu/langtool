package langtool.util;

import java.lang.Character.UnicodeBlock;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import langtool.CharacterUnifiyEnum;
import langtool.TupleUtil;
import langtool.TwoTuple;

public class StatsUtil {
	/**
	 * 用于将Excel表格中列号字母转成列索引，从1对应A开始
	 * 
	 * @param column
	 *            列号
	 * @return 列索引
	 */
	public static int columnToIndex(String column) {
		if (!column.matches("[A-Z]+")) {
			try {
				throw new Exception("Invalid parameter");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		int index = 0;
		char[] chars = column.toUpperCase().toCharArray();
		for (int i = 0; i < chars.length; i++) {
			index += ((int) chars[i] - (int) 'A' + 1)
					* (int) Math.pow(26, chars.length - i - 1);
		}
		return index - 1;
	}

	/**
	 * 用于将excel表格中列索引转成列号字母，从A对应1开始
	 * 
	 * @param index
	 *            列索引
	 * @return 列号
	 */
	public static String indexToColumn(int index) {
		if (index <= 0) {
			try {
				throw new Exception("Invalid parameter");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		String column = "";
		do {
			index--;
			column = ((char) (index % 26 + (int) 'A')) + column;
			index = (int) ((index - index % 26) / 26);
		} while (index > 0);
		return column;
	}

	public static Set<Integer> getExcelLangColIndexFromParams(
			Map<String, String> params) {
		Set<Integer> idxSet = new HashSet<Integer>();
		if (!params.containsKey("check_stats_col")
				|| !"on".equals(params.get("check_stats_col"))) {
			return idxSet;
		}
		
		for (Entry<String, String> entry : params.entrySet()) {
			if ("stats_col".equals(entry.getKey())) {
				String _cols = entry.getValue();
				String[] _tmp = _cols.split("\\|");
				for (int i = 0; i < _tmp.length; i++) {
					idxSet.add(columnToIndex(_tmp[i]));
				}
			}
		}
		return idxSet;
	}

	public static TwoTuple<Set<UnicodeBlock>, Set<String>> getCharacterMatcherFromParams(
			Map<String, String> params) {
		Set<UnicodeBlock> ubSet = new HashSet<Character.UnicodeBlock>();
		Set<String> regSet = new HashSet<String>();
		TwoTuple<Set<UnicodeBlock>, Set<String>> ret = TupleUtil.tuple(ubSet,
				regSet);
		if (params == null || params.size() == 0) {
			return ret;
		}

		for (CharacterUnifiyEnum config : CharacterUnifiyEnum.values()) {
			if (params.containsKey(config.getCheckboxName())) {
				if (config.isUseReg()) {
					regSet.add(config.getRegex());
				} else {
					ubSet.addAll(config.getCharacterUbs());
				}
			}
		}
		return ret;

	}
}
