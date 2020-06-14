package langtool.lang.handlers;

import langtool.CharacterUnifiyEnum;
import langtool.LangTool;
import langtool.StatsInfo;
import langtool.lang.FileTypeConst;
import langtool.lang.ILangFileHandler;
import langtool.util.FileUtil;
import langtool.util.StringUtil;
import langtool.util.XWPFRunBean;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static langtool.LangConst.TRANS_FILES;
import static langtool.LangConst.PATH_SPLITER;
import static langtool.LangConst.DONE_PATH;

public class WordHandler implements ILangFileHandler {

	// @Override
	public File trans(File file, Map<String, String> words,
					  List<String> wordsIdx) throws Exception {
		FileUtil.checkDoneDir(TRANS_FILES);
		String docName = file.getAbsolutePath().replace(file.getName(), "");
		docName += DONE_PATH + PATH_SPLITER + file.getName();
		docName = docName.substring(0, docName.indexOf(".doc"));
		File doc = new File(docName + "_"
				+ (new SimpleDateFormat("yyyyMMdd翻译")).format(new Date())
				+ ".doc");
		if (doc.exists()) {
			doc.delete();
		}
		doc.createNewFile();

		FileInputStream fis = new FileInputStream(file);
		XWPFDocument xwpfDoc = new XWPFDocument(fis);
		Iterator<XWPFParagraph> iterator = xwpfDoc.getParagraphsIterator();
		XWPFParagraph para;
		while (iterator.hasNext()) {
			para = iterator.next();
			List<XWPFRun> runs = para.getRuns();
			for (int i = 0; i < runs.size(); i++) {
				XWPFRun run = runs.get(i);
				String runText = run.toString();
				if (StringUtil.isEmpty(runText)) {
					continue;
				}
				String doneStr = tranText(runText, words, wordsIdx);
				if (runText.equals(doneStr)) {
					continue;
				}
				XWPFRunBean bean = new XWPFRunBean();
				cloneXWPFRun(run, bean, runText);
				para.removeRun(i);
				XWPFRun newRun = para.insertNewRun(i);
				cloneXWPFRun(bean, newRun, doneStr);

			}
		}
		OutputStream os = new FileOutputStream(doc);
		xwpfDoc.write(os);
		os.close();
		fis.close();
		return doc;
	}

	private void cloneXWPFRun(XWPFRunBean run, XWPFRun destRun, String newText) {
		destRun.setColor(run.getColor());
		destRun.setBold(run.isBold());
		destRun.setFontFamily(run.getFontFamily());
		if (run.getFontSize() > 0) {
			destRun.setFontSize(run.getFontSize());
		}
		destRun.setItalic(run.isItalic());
		destRun.setStrike(run.isStrike());
		destRun.setSubscript(run.getSubscript());
		destRun.setUnderline(run.getUnderline());
		destRun.setText(newText);
		destRun.setTextPosition(run.getTextPosition());
	}

	private void cloneXWPFRun(XWPFRun run, XWPFRunBean destRun, String newText) {
		destRun.setColor(run.getColor());
		destRun.setBold(run.isBold());
		destRun.setFontFamily(run.getFontFamily());
		if (run.getFontSize() > 0) {
			destRun.setFontSize(run.getFontSize());
		}
		destRun.setItalic(run.isItalic());
		destRun.setStrike(run.isStrike());
		destRun.setSubscript(run.getSubscript());
		destRun.setUnderline(run.getUnderline());
		destRun.setTextPosition(run.getTextPosition());
	}

	private String tranText(String rawStr, Map<String, String> words,
							List<String> wordsIdx) {
		String doneText = new String(rawStr);
		for (String word : wordsIdx) {
			System.out.println(doneText);
			doneText = doneText.replaceAll(LangTool.quote(word),
					LangTool.quote(words.get(word)));
			System.out.println(doneText);
		}
		return doneText;
	}

	@Override
	public StatsInfo stats(File file, Map<String, String> params)
			throws Exception {
		FileInputStream fis = new FileInputStream(file);
		XWPFDocument xwpfDoc = new XWPFDocument(fis);
		Iterator<XWPFParagraph> iterator = xwpfDoc.getParagraphsIterator();
		XWPFParagraph para;
		long totalWords = 0l;
		while (iterator.hasNext()) {
			para = iterator.next();
			List<XWPFRun> runs = para.getRuns();
			for (int i = 0; i < runs.size(); i++) {
				XWPFRun run = runs.get(i);
				String runText = run.toString();
				if (StringUtil.isEmpty(runText)) {
					continue;
				}
				totalWords += CharacterUnifiyEnum.statsCharacterCnt(params,
						runText);
			}
		}
		fis.close();
		StatsInfo info = new StatsInfo();
		info.setTotalWords(totalWords);
		info.setFileName(file.getName());
		info.setFileType(FileTypeConst.FILE_TYPE_WORD);
		return info;
	}

	@Override
	public File fill(File file, Map<String, String> wordsA, Map<String, String> wordsB) throws Exception {
		throw new RuntimeException("暂不支持Word格式填充表格");
	}
}
