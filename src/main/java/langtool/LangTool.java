package langtool;

import langtool.lang.FileTypeConst;
import langtool.lang.ILangFileHandler;
import langtool.lang.LangFileFactory;
import langtool.util.StringUtil;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.util.SystemOutLogger;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LangTool {

	private final static Map<String, String> WORDS = new TreeMap<String, String>();
	private final static List<String> WORDS_INDEX = new ArrayList<String>();

	public static void clear() {
		WORDS.clear();
		WORDS_INDEX.clear();
	}

	public static void load(File wordFile, boolean force) throws Exception {
		if (WORDS.size() > 0 && !force) {
			return;
		}
		synchronized (LangTool.class) {
			if (WORDS.size() > 0 && !force) {
				return;
			}

			if (force) {
				clear();
			}

			if (!wordFile.exists() || !wordFile.isFile()) {
				throw new Exception("目标：" + wordFile.getAbsolutePath() + "不是有效词库文件，检查一下吧~");
			}
			loadWords(wordFile, WORDS, WORDS_INDEX, false);
		}

	}

	private static void loadWords(File file, Map<String, String> wordsMap, List<String> wordsIndexList, boolean append) throws Exception {
		if (!append) {
			wordsMap.clear();
			wordsIndexList.clear();
		}
		XSSFWorkbook wordWb = new XSSFWorkbook(file);
		XSSFSheet sheet = wordWb.getSheetAt(0);
		System.out.println("words rows：" + sheet.getLastRowNum());
		for (int i = 0; i <= sheet.getLastRowNum(); i++) {
			XSSFRow row = sheet.getRow(i);
			// System.out.println("load line:" + i);
			if (row == null) {
				continue;
			}
			int colNum = row.getLastCellNum();
			for (int j = 0; j < colNum; ) {
				// System.out.println("load col:" + j);
				if (row.getCell(j) == null || row.getCell(j + 1) == null) {
					j += 2;
					continue;
				}
				XSSFCell c1 = row.getCell(j++);
				XSSFCell c2 = row.getCell(j++);
				if (c1.getCellTypeEnum() != CellType.STRING || c2.getCellTypeEnum() != CellType.STRING) {
					continue;
				}
				String col1 = c1.getStringCellValue().trim();
				String col2 = c2.getStringCellValue().trim();
				if (StringUtil.isEmpty(col2)) {
					continue;
				}

				// 转义
				// col1 = quote(col1);
				// col2 = quote(col2);

				wordsMap.put(col1, col2);
				wordsIndexList.add(col1);
				System.out.println(col1 + "|" + col2);
			}
		}
		wordWb.close();
		System.out.println("begin sort");
		Collections.sort(wordsIndexList, new Comparator<String>() {
			// @Override
			public int compare(String o1, String o2) {
				return o2.length() - o1.length();
			}
		});
		System.out.println("词库总数：" + wordsMap.size());
	}


	private static void loadFillSrc(File[] files, Map<String, String> wordsMapA, Map<String, String> wordsMapB) throws Exception {
		if (files.length != 2) {
			throw new RuntimeException("原文件、翻译文件缺一不可哦，已上传文件数: " + files.length);
		}
		wordsMapA.clear();
		wordsMapB.clear();

		List<String> aList = new ArrayList<>();
		List<String> bList = new ArrayList<>();

		for (int i = 0; i < 2; i++) {
			File file = files[i];
			FileInputStream fis = new FileInputStream(file);
			XWPFDocument xwpfDoc = new XWPFDocument(fis);
			Iterator<XWPFParagraph> iterator = xwpfDoc.getParagraphsIterator();
			XWPFParagraph para;
			boolean start = false;
			String line = "";
			while (iterator.hasNext()) {
				para = iterator.next();
				List<XWPFRun> runs = para.getRuns();
				String paraLine = "";
				for (int j = 0; j < runs.size(); j++) {
					paraLine += runs.get(j).toString();
				}

				if (StringUtil.isEmpty(paraLine)) {
					if (start) {
						start = false;
						if (i == 0) {
							aList.add(line);
						} else {
							bList.add(line);
						}
						line = "";
					}
					continue;
				}

				if (paraLine.startsWith("＠")) {
					if (start) {
						if (i == 0) {
							aList.add(line);
						} else {
							bList.add(line);
						}
						line = "";
					} else {
						start = true;
					}
					continue;
				}

				if (start) {
					if (StringUtil.isEmpty(line)) {
						line = paraLine;
					} else {
						line += "\\n" + paraLine;
					}
				}
			}
			fis.close();
		}

		if (aList.size() != bList.size()) {
//            int max = Math.max(aList.size(), bList.size());
//            for (int i = 0; i < max; i++) {
//                if (i >= aList.size()) {
//                    System.out.println("null\t=>\t" + bList.get(i));
//                } else if (i >= bList.size()) {
//                    System.out.println(aList.get(i) + "\t=>\tnull");
//                } else {
//                    System.out.println(aList.get(i) + "\t=>\t" + bList.get(i));
//                }
//                System.out.println("----------------------------------------------");
//            }
			throw new RuntimeException("原文件和翻译文件 @对话 不能一一对应，文件 “" + files[0].getName() + "” 对话数：" + aList.size() + "， 文件 “" + files[1].getName() + "” 对话数：" + bList.size());
		}

		for (int i = 0; i < aList.size(); i++) {
			wordsMapA.put(aList.get(i), bList.get(i));
			wordsMapB.put(bList.get(i), aList.get(i));
		}
	}


	public static void copyCell(XSSFCell rawCell, XSSFCell destCell) {
		destCell.setCellType(rawCell.getCellTypeEnum());
		destCell.setCellComment(rawCell.getCellComment());
		CellStyle destStyle = destCell.getRow().getSheet().getWorkbook().createCellStyle();
		destStyle.cloneStyleFrom(rawCell.getCellStyle());
		destCell.setCellStyle(destStyle);
		destCell.setHyperlink(rawCell.getHyperlink());
		switch (rawCell.getCellTypeEnum()) {
			case BOOLEAN:
				destCell.setCellValue(rawCell.getBooleanCellValue());
				break;
			case STRING:
				try {
					XSSFRichTextString rawRich = rawCell.getRichStringCellValue();
					if (rawRich != null && rawRich.hasFormatting()) {
						XSSFRichTextString richTextString = new XSSFRichTextString();
						for (int i = 0; i < rawCell.getRichStringCellValue().length(); i++) {
							richTextString.applyFont(i, i + 1, rawCell.getRichStringCellValue().getFontAtIndex(i));
						}
						destCell.setCellValue(richTextString);
					} else {
						destCell.setCellValue(rawCell.getStringCellValue());
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				break;
			case NUMERIC:
				destCell.setCellValue(rawCell.getNumericCellValue());
				break;
			case FORMULA:
				destCell.setCellFormula(rawCell.getCellFormula());
				destCell.setCellValue(rawCell.getRawValue());
			case BLANK:
				break;
			default:
				System.err.println("unknown cell type:" + rawCell.getCellTypeEnum());
		}

	}


	// 从session读取词库翻译文件
	@SuppressWarnings("unchecked")
	public static File transFile(File file, HttpSession session) throws Exception {
		if (!file.isFile()) {
			return null;
		}

		ILangFileHandler langHandler = LangFileFactory.getHandler(file.getName());
		if (langHandler == null) {
			return null;
		}
		return langHandler.trans(file, (Map<String, String>) session.getAttribute(LangConst.SESSION_KEY_WORDS),
				(List<String>) session.getAttribute(LangConst.SESSION_KEY_WORDS_INDEX));
	}

	// 初始化词库入session
	public static void loadWords(File wordsDir, HttpSession session) throws Exception {
		Map<String, String> wordsCache = new TreeMap<String, String>();
		List<String> wordsIndexCache = new ArrayList<String>();
		for (File file : wordsDir.listFiles()) {
			loadWords(file, wordsCache, wordsIndexCache, true);
		}
		session.setAttribute(LangConst.SESSION_KEY_WORDS, wordsCache);
		session.setAttribute(LangConst.SESSION_KEY_WORDS_INDEX, wordsIndexCache);
	}

	// 初始化填充原文件入session
	public static void loadFillSrc(File wordsDir, HttpSession session) throws Exception {
		Map<String, String> aCache = new TreeMap<>();
		Map<String, String> bCache = new TreeMap<>();
		loadFillSrc(wordsDir.listFiles(), aCache, bCache);
		session.setAttribute(LangConst.SESSION_KEY_FILLS_A, aCache);
		session.setAttribute(LangConst.SESSION_KEY_FILLS_B, bCache);
	}

	public static String quote(String str) {
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\{", "\\\\\\{").replaceAll("\\}", "\\\\\\}").replaceAll("\\$", "\\\\\\$")
				.replaceAll("\\(", "\\\\\\(").replaceAll("\\)", "\\\\\\)").replaceAll("\\*", "\\\\\\*").replaceAll("\\+", "\\\\\\+")
				.replaceAll("\\^", "\\\\\\^").replaceAll("\\[", "\\\\\\[").replaceAll("\\]", "\\\\\\]").replaceAll("\\?", "\\\\\\?")
				.replaceAll("\\|", "\\\\\\|");
	}

	public static void main(String[] args) {
		System.out.println(LangTool.quote("{}"));
	}

	// 统计文件信息
	public static TwoTuple<StatsInfo, JSONObject> statsFile(File file, Map<String, String> params) throws Exception {
		if (!file.isFile()) {
			return null;
		}

		ILangFileHandler langHandler = LangFileFactory.getHandler(file.getName());
		if (langHandler == null) {
			return null;
		}
		StatsInfo info = langHandler.stats(file, params);
		JSONObject statsJson = new JSONObject();
		statsJson.put("totalCnt", info.getTotalWords());
		statsJson.put("fileType", info.getFileType());
		if (info.getFileType() == FileTypeConst.FILE_TYPE_EXCEL) {
			JSONArray details = new JSONArray();
			for (Entry<String, Map<String, Long>> entry : info.getExcelDetailMap().entrySet()) {
				JSONObject sheetSub = new JSONObject();
				JSONArray sub = new JSONArray();
				long totalCnt = 0;
				for (Entry<String, Long> entry1 : entry.getValue().entrySet()) {
					JSONObject col = new JSONObject();
					col.put("colName", entry1.getKey() + "列");
					col.put("cnt", entry1.getValue());
					totalCnt += entry1.getValue();
					sub.put(col);
				}
				sheetSub.put("sheetName", "【" + entry.getKey() + "】" + totalCnt);
				sheetSub.put("sheetDetail", sub);
				details.put(sheetSub);
			}
			statsJson.put("colDetail", details);
		}
		return TupleUtil.tuple(info, statsJson);
	}

	public static File fillFile(File file, HttpSession session) throws Exception {
		if (!file.isFile()) {
			return null;
		}

		ILangFileHandler langHandler = LangFileFactory.getHandler(file.getName());
		if (langHandler == null) {
			return null;
		}
		return langHandler.trans(file, (Map<String, String>) session.getAttribute(LangConst.SESSION_KEY_WORDS),
				(List<String>) session.getAttribute(LangConst.SESSION_KEY_WORDS_INDEX));
	}

}
