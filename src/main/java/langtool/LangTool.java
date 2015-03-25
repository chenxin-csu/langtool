package langtool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpSession;

import langtool.lang.FileTypeConst;
import langtool.lang.ILangFileHandler;
import langtool.lang.LangFileFactory;
import langtool.util.StringUtil;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

public class LangTool {

	private static ExecutorService exe = Executors.newFixedThreadPool(10);

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
				throw new Exception("目标：" + wordFile.getAbsolutePath()
						+ "不是有效词库文件，检查一下吧~");
			}
			loadWords(wordFile, WORDS, WORDS_INDEX, false);
		}

	}

	private static void loadWords(File file, Map<String, String> wordsMap,
			List<String> wordsIndexList, boolean append) throws Exception {
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
			for (int j = 0; j < colNum;) {
				// System.out.println("load col:" + j);
				if (row.getCell(j) == null || row.getCell(j + 1) == null) {
					j += 2;
					continue;
				}
				XSSFCell c1 = row.getCell(j++);
				XSSFCell c2 = row.getCell(j++);
				if (c1.getCellType() != XSSFCell.CELL_TYPE_STRING
						|| c2.getCellType() != XSSFCell.CELL_TYPE_STRING) {
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

	static void trans(final File targetFile) throws Exception {
		exe.execute(new Runnable() {
			// @Override
			public void run() {
				try {
					System.out.println("开始翻译：" + targetFile.getName() + "...");
					transFile(targetFile, 3, 4);
					System.out.println("完成翻译：" + targetFile.getName());
				} catch (Exception e) {
					System.out.println("【错误】翻译出错：" + targetFile.getName() + ":"
							+ e.getMessage());
					e.printStackTrace();
				}
			}
		});

	}

	public final static File transFile(File file, int rawColIdx, int destColIdx)
			throws InvalidFormatException, IOException {
		String xlsName = file.getAbsolutePath().substring(0,
				file.getAbsolutePath().indexOf(".x"));
		File xls = new File(xlsName + "_"
				+ (new SimpleDateFormat("yyyyMMdd翻译")).format(new Date())
				+ ".xlsx");
		if (xls.exists()) {
			xls.delete();
		}
		xls.createNewFile();
		FileOutputStream fos = new FileOutputStream(xls);
		XSSFWorkbook wb = new XSSFWorkbook(file);
		XSSFSheet sheet = wb.getSheetAt(0);

		XSSFWorkbook newWb = new XSSFWorkbook();
		XSSFSheet newSheet = newWb.createSheet();
		for (int i = 1; i < sheet.getLastRowNum(); i++) {
			XSSFRow newRow = newSheet.createRow(i);
			XSSFRow row = sheet.getRow(i);
			for (int j = 0; j < rawColIdx - 1; j++) {
				XSSFCell newCol = newRow.createCell(j);
				copyCell(row.getCell(j), newCol);
			}
			XSSFCell newCol2 = newRow.createCell(rawColIdx - 1);
			XSSFCell newCol3 = newRow.createCell(destColIdx - 1);

			String rawStr = row.getCell(2).getStringCellValue();
			newCol2.setCellValue(rawStr);

			// TODO trans
			String doneStr = new String(rawStr);
			for (String word : WORDS_INDEX) {
				doneStr = doneStr.replaceAll(word, WORDS.get(word));
			}
			newCol3.setCellValue(doneStr);
			// System.out.println("[INFO]trans [" + rawStr + "] to [" + doneStr
			// + "]");
		}
		newWb.write(fos);
		fos.close();
		wb.close();
		newWb.close();
		return xls;
	}

	public static void copyCell(XSSFCell rawCell, XSSFCell destCell) {
		destCell.setCellType(rawCell.getCellType());
		switch (rawCell.getCellType()) {
		case XSSFCell.CELL_TYPE_BOOLEAN:
			destCell.setCellValue(rawCell.getBooleanCellValue());
			break;
		case XSSFCell.CELL_TYPE_STRING:
			destCell.setCellValue(rawCell.getStringCellValue());
			break;
		case XSSFCell.CELL_TYPE_NUMERIC:
			destCell.setCellValue(rawCell.getNumericCellValue());
			break;
		case XSSFCell.CELL_TYPE_BLANK:
			break;
		default:
			System.err.println("unknown cell type:" + rawCell.getCellType());
		}
	}

	// public static void main(String[] args) {
	// try {
	// final String WORDS_TABLE = args[0];
	// final String TARGET_FILE_DIR = args[1];
	//
	// System.out.println("读取词库：" + WORDS_TABLE);
	// System.out.println("目标文件或文件夹：" + TARGET_FILE_DIR);
	// System.out.println("开始加载词库...");
	// load(new File(WORDS_TABLE), false);
	// System.out.println("加载词库完成");
	// System.out.println("开始翻译目标文件...");
	// File fileOrDir = new File(TARGET_FILE_DIR);
	// if (fileOrDir.isFile()) {
	// trans(fileOrDir);
	// } else {
	// for (File file : fileOrDir.listFiles()) {
	// trans(file);
	// }
	// }
	//
	// } catch (Exception e) {
	// System.err.println("【错误】发生错误，操作中断，错误信息：" + e.getMessage());
	// e.printStackTrace();
	// } finally {
	// try {
	// exe.shutdown();
	// exe.awaitTermination(10, TimeUnit.MINUTES);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// System.out.println("执行结束，程序退出。");
	// }
	//
	// }

	// 从session读取词库翻译文件
	@SuppressWarnings("unchecked")
	public static File transFile(File file, HttpSession session)
			throws Exception {
		if (!file.isFile()) {
			return null;
		}

		ILangFileHandler langHandler = LangFileFactory.getHandler(file
				.getName());
		if (langHandler == null) {
			return null;
		}
		return langHandler.trans(file, (Map<String, String>) session
				.getAttribute(LangConst.SESSION_KEY_WORDS),
				(List<String>) session
						.getAttribute(LangConst.SESSION_KEY_WORDS_INDEX));
	}

	// 初始化词库入session
	public static void loadWords(File wordsDir, HttpSession session)
			throws Exception {
		Map<String, String> wordsCache = new TreeMap<String, String>();
		List<String> wordsIndexCache = new ArrayList<String>();
		for (File file : wordsDir.listFiles()) {
			loadWords(file, wordsCache, wordsIndexCache, true);
		}
		session.setAttribute(LangConst.SESSION_KEY_WORDS, wordsCache);
		session.setAttribute(LangConst.SESSION_KEY_WORDS_INDEX, wordsIndexCache);
	}

	public static String quote(String str) {
		return str.replaceAll("\\\\", "\\\\\\\\").replaceAll("\\{", "\\\\\\{")
				.replaceAll("\\}", "\\\\\\}").replaceAll("\\$", "\\\\\\$")
				.replaceAll("\\(", "\\\\\\(").replaceAll("\\)", "\\\\\\)")
				.replaceAll("\\*", "\\\\\\*").replaceAll("\\+", "\\\\\\+")
				.replaceAll("\\^", "\\\\\\^").replaceAll("\\[", "\\\\\\[")
				.replaceAll("\\]", "\\\\\\]").replaceAll("\\?", "\\\\\\?")
				.replaceAll("\\|", "\\\\\\|");
	}

	public static void main(String[] args) {
		System.out.println(LangTool.quote("{}"));
	}

	// 统计文件信息
	public static TwoTuple<StatsInfo, JSONObject> statsFile(File file,
			Set<UnicodeBlock> langSet, HttpSession session) throws Exception {
		if (!file.isFile()) {
			return null;
		}

		ILangFileHandler langHandler = LangFileFactory.getHandler(file
				.getName());
		if (langHandler == null) {
			return null;
		}
		StatsInfo info = langHandler.stats(file, langSet);
		JSONObject statsJson = new JSONObject();
		statsJson.put("totalCnt", info.getTotalWords());
		statsJson.put("fileType",info.getFileType());
		if (info.getFileType() == FileTypeConst.FILE_TYPE_EXCEL) {
			JSONArray details = new JSONArray();
			for (Entry<String, Map<String, Long>> entry : info
					.getExcelDetailMap().entrySet()) {
				JSONObject sheetSub = new JSONObject();
				JSONArray sub = new JSONArray();
				for (Entry<String, Long> entry1 : entry.getValue().entrySet()) {
					JSONObject col = new JSONObject();
					col.put("colName", entry1.getKey() + "列");
					col.put("cnt", entry1.getValue());
					sub.put(col);
				}
				sheetSub.put("sheetName", entry.getKey());
				sheetSub.put("sheetDetail", sub);
				details.put(sheetSub);
			}
			statsJson.put("colDetail", details);
		}
		return TupleUtil.tuple(info, statsJson);
	}

}
