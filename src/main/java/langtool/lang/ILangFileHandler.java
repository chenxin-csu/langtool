package langtool.lang;

import langtool.StatsInfo;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface ILangFileHandler {

	/**
	 * 文件翻译
	 *
	 * @param file
	 * @param words
	 * @param wordsIndex
	 * @return
	 * @throws Exception
	 */
	File trans(File file, Map<String, String> words, List<String> wordsIndex) throws Exception;

	/**
	 * 文件统计
	 *
	 * @param file
	 * @return
	 * @throws Exception
	 */
	StatsInfo stats(File file, Map<String, String> params) throws Exception;

	/**
	 * 复制填充
	 *
	 * @param file
	 * @param wordsA
	 * @param wordsB
	 * @return
	 * @throws Exception
	 */
	File fill(File file, Map<String, String> wordsA, Map<String, String> wordsB) throws Exception;

}
