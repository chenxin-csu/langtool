package cli.tools;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static langtool.LangConst.PATH_SPLITER;
import static langtool.LangConst.TMP_PATH;

/**
 *
 * 保留表头及前三行台词
 *
 * Created by jackie on 2020/6/10.
 */
public class HeadExcels {

	public static void main(String[] args) {
		headExcels(new File("/Users/jackie/Desktop/台词"), 3);
	}

	public static void headExcels(File dir, int head) {
		if (!dir.isDirectory()) {
			System.err.println("dir is not a directory");
			return;
		}
		File[] files = dir.listFiles();
		if (files.length == 0) {
			System.out.println("no file under " + dir.getAbsolutePath());
			return;
		}

		List<File> fileList = Arrays.asList(files);
		Collections.sort(fileList, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareTo(f2.getName());
			}
		});

		FileOutputStream fos = null;
		XSSFWorkbook nwb = new XSSFWorkbook();
		try {
			String xlsName = dir.getAbsolutePath();
			String doneDirPath = xlsName + TMP_PATH;
			xlsName += TMP_PATH + PATH_SPLITER + "台词整理.xlsx";

			File doneDir = new File(doneDirPath);
			if (!doneDir.exists()) {
				doneDir.mkdirs();
			}

			File xls = new File(xlsName);
			if (xls.exists()) {
				xls.delete();
			}
			xls.createNewFile();
			fos = new FileOutputStream(xls);

			for (File file : fileList) {
				if (file.getName().endsWith(".xlsx")) {
					doHeadXSSF(file, head, nwb);
				} else if (file.getName().endsWith(".xls")) {
					doHeadHSSF(file, head, nwb);
				}
			}
			nwb.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
				nwb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void doHeadXSSF(File file, int head, XSSFWorkbook nwb) {
		XSSFWorkbook wb = null;
		try {
			wb = new XSSFWorkbook(file);
			String fileName = file.getName().trim();
			XSSFSheet sheet = wb.getSheetAt(0);
			String sheetName = getSheetName(fileName);
			System.out.println("write file " + file.getAbsolutePath() + " sheet: " + sheetName);

			XSSFSheet nSheet = nwb.createSheet(sheetName);
			int nRowIdx = 0;
			for (int j = 0; j < sheet.getLastRowNum(); j++) {
				XSSFRow row = sheet.getRow(j);
				if (row == null) {
					continue;
				}

				if (row.getCell(0) != null && CellType.NUMERIC == row.getCell(0).getCellTypeEnum()) {
					double val = row.getCell(0).getNumericCellValue();
					if (val > head) {
						break;
					}
				}

				XSSFRow nRow = nSheet.createRow(nRowIdx);
				nRowIdx++;

				for (int k = 0; k < row.getLastCellNum(); k++) {
					XSSFCell nc = nRow.createCell(k);
					nc.copyCellFrom(row.getCell(k), new CellCopyPolicy((new CellCopyPolicy.Builder()).cellFormula(false).cellStyle(false)
							.cellValue(true).copyHyperlink(false).mergedRegions(false).condenseRows(false).rowHeight(false).build()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static String getSheetName(String fileName) {
		String sheetName = null;
		if (fileName.contains("_台词")) {
			sheetName = fileName.substring(0, fileName.indexOf("_台词"));
		} else if (fileName.contains("台词")) {
			sheetName = fileName.substring(0, fileName.indexOf("台词"));
		} else if (fileName.contains("_20")) {
			sheetName = fileName.substring(0, fileName.indexOf("_20"));
		} else if (fileName.contains("-")) {
			sheetName = fileName.substring(0, fileName.indexOf("-"));
		} else {
			sheetName = fileName.substring(0, fileName.indexOf(".x"));
		}
		return sheetName;
	}

	private static void doHeadHSSF(File file, int head, XSSFWorkbook nwb) {
		HSSFWorkbook wb = null;
		try {
			wb = new HSSFWorkbook(new FileInputStream(file));
			String fileName = file.getName().trim();
			HSSFSheet sheet = wb.getSheetAt(0);
			String sheetName = getSheetName(fileName);
			System.out.println("write file " + file.getAbsolutePath() + " sheet: " + sheetName);

			XSSFSheet nSheet = nwb.createSheet(sheetName);
			int nRowIdx = 0;
			for (int j = 0; j < sheet.getLastRowNum(); j++) {
				HSSFRow row = sheet.getRow(j);
				if (row == null) {
					continue;
				}

				if (row.getCell(0) != null && CellType.NUMERIC == row.getCell(0).getCellTypeEnum()) {
					double val = row.getCell(0).getNumericCellValue();
					if (val > head) {
						break;
					}
				}

				XSSFRow nRow = nSheet.createRow(nRowIdx);
				nRowIdx++;

				for (int k = 0; k < row.getLastCellNum(); k++) {
					XSSFCell nc = nRow.createCell(k);
					nc.copyCellFrom(row.getCell(k), new CellCopyPolicy((new CellCopyPolicy.Builder()).cellFormula(false).cellStyle(false)
							.cellValue(true).copyHyperlink(false).mergedRegions(false).condenseRows(false).rowHeight(false).build()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				wb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
