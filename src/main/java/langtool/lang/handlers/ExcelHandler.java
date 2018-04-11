package langtool.lang.handlers;

import langtool.CharacterUnifiyEnum;
import langtool.LangTool;
import langtool.StatsInfo;
import langtool.lang.FileTypeConst;
import langtool.lang.ILangFileHandler;
import langtool.util.FileUtil;
import langtool.util.StatsUtil;
import langtool.util.StringUtil;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static langtool.LangConst.PATH_SPLITER;
import static langtool.LangConst.TMP_PATH;

public class ExcelHandler implements ILangFileHandler {

	// @Override
	public File trans(File file, Map<String, String> words, List<String> wordsIdx) throws Exception {
		FileOutputStream fos = null;
		XSSFWorkbook wb = null;
		XSSFWorkbook newWb = null;
		try {
			FileUtil.checkDoneDir();
			String xlsName = file.getAbsolutePath().replace(file.getName(), "");
			xlsName += TMP_PATH + PATH_SPLITER + file.getName();

			xlsName = xlsName.substring(0, xlsName.indexOf(".x"));

			File xls = new File(xlsName + "_" + (new SimpleDateFormat("yyyyMMdd翻译")).format(new Date()) + ".xlsx");
			if (xls.exists()) {
				xls.delete();
			}
			xls.createNewFile();
			fos = new FileOutputStream(xls);
			wb = new XSSFWorkbook(file);
			XSSFSheet sheet = wb.getSheetAt(0);
			newWb = new XSSFWorkbook();
			XSSFSheet newSheet = newWb.createSheet(sheet.getSheetName());
			XSSFRow flagRow = sheet.getRow(0);
			Map<Integer, Integer> flags = new HashMap<Integer, Integer>();
			int firstRowCellNum = flagRow == null ? 0 : flagRow.getLastCellNum();

		    //如果没有设置flagRow,即没有新增lang行,则直接原文替换翻译
			for (int i = 0; i < firstRowCellNum; i++) {
				XSSFCell cell = flagRow.getCell(i);
				if (cell == null || cell.getCellTypeEnum() != CellType.STRING) {
					continue;
				}
				String c = cell.getStringCellValue();
				if (c != null && !"".equals(c.trim()) && (c.trim().equals("lang") || c.trim().startsWith("lang:"))) {
					String[] tmp = c.split(":");
					if (tmp.length > 1) {
						flags.put(i, StatsUtil.columnToIndex(tmp[1].trim().toUpperCase()));
					} else {
						flags.put(i, i + 1);
					}
				}
			}

			if(flags.size() == 0 && flagRow != null){
				//添加占位行
				sheet.shiftRows(0,sheet.getLastRowNum(),1);
				sheet.createRow(0);
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
					} else if (flags.containsValue(j) && !StringUtil.isEmpty(newCol.getStringCellValue())) {
						continue;
					}
					LangTool.copyCell(row.getCell(j), newCol);
					XSSFCell destCol = null;
					String rawStr = row.getCell(j).getStringCellValue();
					if (flags.size() == 0) {
						//直接原单元格翻译
						destCol = newCol;
					} else if (flags.containsKey(j)) {
						destCol = newRow.getCell(flags.get(j));
						if (destCol == null) {
							destCol = newRow.createCell(flags.get(j));
						}
					}else{
						continue;
					}

					String doneStr = new String(rawStr);
					for (String word : wordsIdx) {
						if (!doneStr.contains(word)) {
							continue;
						}
						doneStr = doneStr.replaceAll(LangTool.quote(word), LangTool.quote(words.get(word)));
						destCol.setCellValue(doneStr);
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

	public static void main(String[] args) {
		String c = "ABC";
		System.out.println(c);
		int idx = StatsUtil.columnToIndex(c.toUpperCase());
		System.out.println(idx + 1);
		System.out.println(StatsUtil.indexToColumn(idx + 1));

	}

	@Override
	public StatsInfo stats(File file, Map<String, String> params) throws Exception {
		XSSFWorkbook wb = null;
		long wordCnt = 0l;
		Map<String, Map<String, Long>> detailMap = new HashMap<String, Map<String, Long>>();
		try {
			wb = new XSSFWorkbook(file);
			int sheetCnt = wb.getNumberOfSheets();
			for (int sheetIdx = 0; sheetIdx < sheetCnt; sheetIdx++) {
				XSSFSheet sheet = wb.getSheetAt(sheetIdx);
				// flag
				XSSFRow flagRow = sheet.getRow(0);
				if (flagRow == null) {
					continue;
				}
				Map<String, Long> sheetStatsMap = new TreeMap<String, Long>();
				detailMap.put(sheet.getSheetName(), sheetStatsMap);
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
				XSSFRow row = null;
				int rowStart = flags.size() > 0 ? 1 : 0;
				flags.addAll(StatsUtil.getExcelLangColIndexFromParams(params));
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
						long lineWordsCnt = CharacterUnifiyEnum.statsCharacterCnt(params, line);
						wordCnt += lineWordsCnt;
						String charColName = StatsUtil.indexToColumn(j + 1);
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
