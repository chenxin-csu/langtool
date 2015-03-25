package langtool.lang.handlers;

import static langtool.LangConst.PATH_SPLITER;
import static langtool.LangConst.TMP_PATH;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Character.UnicodeBlock;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import langtool.CharacterUnifiyEnum;
import langtool.LangTool;
import langtool.StatsInfo;
import langtool.lang.FileTypeConst;
import langtool.lang.ILangFileHandler;
import langtool.util.FileUtil;
import langtool.util.StringUtil;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelHandler implements ILangFileHandler {

	// @Override
	public File trans(File file, Map<String, String> words,
			List<String> wordsIdx) throws Exception {
		FileOutputStream fos = null;
		XSSFWorkbook wb = null;
		XSSFWorkbook newWb = null;
		try {
			FileUtil.checkDoneDir();
			String xlsName = file.getAbsolutePath().replace(file.getName(), "");
			xlsName += TMP_PATH + PATH_SPLITER + file.getName();

			xlsName = xlsName.substring(0, xlsName.indexOf(".x"));

			File xls = new File(xlsName + "_"
					+ (new SimpleDateFormat("yyyyMMdd翻译")).format(new Date())
					+ ".xlsx");
			if (xls.exists()) {
				xls.delete();
			}
			xls.createNewFile();
			fos = new FileOutputStream(xls);
			wb = new XSSFWorkbook(file);
			XSSFSheet sheet = wb.getSheetAt(0);
			newWb = new XSSFWorkbook();
			XSSFSheet newSheet = newWb.createSheet();
			XSSFRow flagRow = sheet.getRow(0);
			Map<Integer, Integer> flags = new HashMap<Integer, Integer>();
			int firstRowCellNum = flagRow.getLastCellNum();

			for (int i = 0; i < firstRowCellNum; i++) {
				XSSFCell cell = flagRow.getCell(i);
				if (cell == null) {
					continue;
				}
				String c = cell.getStringCellValue();
				if (c != null && !"".equals(c.trim()) && c.contains("lang")) {
					String[] tmp = c.split(":");
					if (tmp.length > 1) {
						flags.put(i, columnToIndex(tmp[1].trim().toUpperCase()));
					} else {
						flags.put(i, i + 1);
					}
				}
			}
			for (int i = 1; i <= sheet.getLastRowNum(); i++) {
				XSSFRow newRow = newSheet.createRow(i - 1);
				XSSFRow row = sheet.getRow(i);
				if (row == null) {
					continue;
				}
				int colNum = row.getLastCellNum();
				for (int j = 0; j < colNum; j++) {
					XSSFCell newCol = newRow.getCell(j);
					if (newCol == null) {
						newCol = newRow.createCell(j);
					} else if (flags.containsValue(j)
							&& !StringUtil.isEmpty(newCol.getStringCellValue())) {
						continue;
					}
					LangTool.copyCell(row.getCell(j), newCol);
					if (flags.containsKey(j)) {
						String rawStr = row.getCell(j).getStringCellValue();
						XSSFCell destCol = newRow.getCell(flags.get(j));
						if (destCol == null) {
							destCol = newRow.createCell(flags.get(j));
						}
						String doneStr = new String(rawStr);
						for (String word : wordsIdx) {
							if (!doneStr.contains(word)) {
								continue;
							}
							doneStr = doneStr.replaceAll(LangTool.quote(word),
									LangTool.quote(words.get(word)));
							destCol.setCellValue(doneStr);
						}
					}
				}

			}
			newWb.write(fos);
			return xls;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("[error]" + e.getMessage());
		} finally {
			try {
				fos.close();
				wb.close();
				newWb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * 用于将Excel表格中列号字母转成列索引，从1对应A开始
	 * 
	 * @param column
	 *            列号
	 * @return 列索引
	 */
	private static int columnToIndex(String column) {
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
	private static String indexToColumn(int index) {
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

	public static void main(String[] args) {
		String c = "ABC";
		System.out.println(c);
		int idx = columnToIndex(c.toUpperCase());
		System.out.println(idx + 1);
		System.out.println(indexToColumn(idx + 1));

	}

	@Override
	public StatsInfo stats(File file, Set<UnicodeBlock> langSet)
			throws Exception {
		XSSFWorkbook wb = null;
		long wordCnt = 0l;
		Map<String, Map<String, Long>> detailMap = new HashMap<String, Map<String, Long>>();
		try {
			wb = new XSSFWorkbook(file);
			int sheetCnt = wb.getNumberOfSheets();
			for (int sheetIdx = 0; sheetIdx < sheetCnt; sheetIdx++) {
				XSSFSheet sheet = wb.getSheetAt(sheetIdx);
				Map<String, Long> sheetStatsMap = new HashMap<String, Long>();
				detailMap.put(sheet.getSheetName(), sheetStatsMap);

				// flag
				XSSFRow flagRow = sheet.getRow(0);
				Set<Integer> flags = new HashSet<Integer>();
				int firstRowCellNum = flagRow.getLastCellNum();

				for (int i = 0; i < firstRowCellNum; i++) {
					XSSFCell cell = flagRow.getCell(i);
					if (cell == null) {
						continue;
					}
					String c = cell.getStringCellValue();
					if (!StringUtil.isEmpty(c) && c.contains("lang")) {
						flags.add(i);
					}
				}
				//

				XSSFRow row = null;
				int rowStart = flags.size() > 0 ? 1 : 0;
				flags.add(5);// TODO
				flags.add(6);
				for (int i = rowStart; i <= sheet.getLastRowNum(); i++) {
					row = sheet.getRow(i);
					if (row == null) {
						continue;
					}
					int colNum = row.getLastCellNum();
					XSSFCell cell = null;
					for (int j = 0; j < colNum; j++) {
						if (flags.size() > 0 && !flags.contains(j)) {
							continue;
						}
						cell = row.getCell(j);
						if (cell == null) {
							continue;
						}
						String line = cell.getStringCellValue();
						if (StringUtil.isEmpty(line)) {
							continue;
						}
						long lineWordsCnt = CharacterUnifiyEnum
								.statsCharacterCnt(langSet, line);
						wordCnt += lineWordsCnt;
						String charColName = indexToColumn(j+1);
						Long colWordCnt = sheetStatsMap.get(charColName);
						if (colWordCnt == null) {
							colWordCnt = 0l;

						}
						colWordCnt += lineWordsCnt;
						sheetStatsMap.put(charColName, colWordCnt);
					}
				}
			}

			StatsInfo stats = new StatsInfo();
			stats.setFileName(file.getName());
			stats.setTotalWords(wordCnt);
			stats.setExcelDetailMap(detailMap);
			stats.setFileType(FileTypeConst.FILE_TYPE_EXCEL);
			System.out.println(stats);
			return stats;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("[error]" + e.getMessage());
		} finally {
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
