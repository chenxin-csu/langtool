package langtool.lang;

import java.io.File;
import java.util.List;
import java.util.Map;

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
	StatsInfo stats(File file, Map<String, String> params) throws Exception;
}
