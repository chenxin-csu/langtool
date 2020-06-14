package langtool.lang.handlers;

import static langtool.LangConst.PATH_SPLITER;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;

import langtool.CharacterUnifiyEnum;
import langtool.LangTool;
import langtool.StatsInfo;
import langtool.lang.FileTypeConst;
import langtool.lang.ILangFileHandler;
import langtool.util.FileUtil;

/**
 * @author xin.chen
 */
public class TextHandler implements ILangFileHandler {

	@Override
	public File trans(File file, Map<String, String> words,
					  List<String> wordsIdx) throws Exception {
		FileReader fr = new FileReader(file);
		BufferedReader br = new BufferedReader(fr);
		String line = null;
		File subDir = FileUtil.checkDoneDir();
		File txt = new File(subDir.getAbsolutePath() + PATH_SPLITER
				+ file.getName());
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
					doneStr = doneStr.replaceAll(LangTool.quote(word),
							LangTool.quote(words.get(word)));
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
	public StatsInfo stats(File file, Map<String, String> params)
			throws Exception {
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
		return null;
	}
}
