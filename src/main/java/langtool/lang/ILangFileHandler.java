package langtool.lang;

import java.io.File;
import java.lang.Character.UnicodeBlock;
import java.util.List;
import java.util.Map;
import java.util.Set;

import langtool.StatsInfo;

public interface ILangFileHandler {

	/**
	 * 文件翻译
	 * 
	 * @param file
	 * @param words
	 * @param wordsIdx
	 * @return
	 * @throws Exception
	 */
	File trans(File file, Map<String, String> words, List<String> wordsIndex)
			throws Exception;

	/**
	 * 文件统计
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	StatsInfo stats(File file, Set<UnicodeBlock> langSet) throws Exception;
}
