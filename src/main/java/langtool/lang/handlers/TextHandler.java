package langtool.lang.handlers;

import langtool.CharacterUnifiyEnum;
import langtool.LangTool;
import langtool.StatsInfo;
import langtool.lang.FileTypeConst;
import langtool.lang.ILangFileHandler;
import langtool.util.FileUtil;

import java.io.*;
import java.util.List;
import java.util.Map;

import static langtool.LangConst.TRANS_FILES;
import static langtool.LangConst.PATH_SPLITER;

/**
 * @author xin.chen
 */
public class TextHandler implements ILangFileHandler {

	@Override
	public File trans(File file, Map<String, String> words, List<String> wordsIdx) throws Exception {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		File subDir = FileUtil.checkDoneDir(TRANS_FILES);
		File txt = new File(subDir.getAbsolutePath() + PATH_SPLITER + file.getName());
		if (txt.exists()) {
			txt.delete();
		}
		txt.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(txt));
		String doneStr = null;
		while ((line = br.readLine()) != null) {
			doneStr = new String(line);
			for (String word : wordsIdx) {
				if (doneStr.contains(word)) {
					doneStr = doneStr.replaceAll(LangTool.quote(word), LangTool.quote(words.get(word)));
				}
			}
			bw.write(doneStr + "\r\n");
		}
		br.close();
		bw.flush();
		bw.close();
		return txt;
	}

	@Override
	public StatsInfo stats(File file, Map<String, String> params) throws Exception {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		long totalWords = 0l;
		while ((line = br.readLine()) != null) {
			totalWords += CharacterUnifiyEnum.statsCharacterCnt(params, line);
		}
		br.close();
		StatsInfo info = new StatsInfo();
		info.setFileName(file.getName());
		info.setTotalWords(totalWords);
		info.setFileType(FileTypeConst.FILE_TYPE_TEXT);
		return info;
	}

	@Override
	public File fill(File file, Map<String, String> wordsA, Map<String, String> wordsB) throws Exception {
		throw new RuntimeException("暂不支持文本文档格式填充表格");
	}
}
