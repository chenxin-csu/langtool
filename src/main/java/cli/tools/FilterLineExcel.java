package cli.tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static langtool.LangConst.PATH_SPLITER;
import static langtool.LangConst.TMP_PATH;

/**
 * Created by jackie on 2020/6/11.
 */
public class FilterLineExcel {

	private static List<String> filter0;
	private static List<String> filter1;
	static {
		filter0 = Arrays.asList("姓名", "名字", "阵营", "职业", "身高", "体重", "生日", "动物", "爱好", "性格", "自我介绍");
		filter1 = Arrays.asList("名字", "阵营", "职业", "身高", "体重", "生日", "动物", "爱好", "角色经历", "自我介绍");
	}

	public static void main(String[] args) {
		filterLines(new File("/Users/jackie/Desktop/角色图鉴（人设+立绘+性格+技能）.xlsx"));
	}

	public static void filterLines(File file) {
		XSSFWorkbook wb = null;

		XSSFWorkbook nwb = null;
		FileOutputStream fos = null;

		try {

			String xlsName = file.getAbsolutePath().replace(file.getName(), "");
			String doneDirPath = xlsName + TMP_PATH;
			xlsName += TMP_PATH + PATH_SPLITER + file.getName();
			xlsName = xlsName.substring(0, xlsName.indexOf(".x"));

			File doneDir = new File(doneDirPath);
			if (!doneDir.exists()) {
				doneDir.mkdirs();
			}
			File xls = new File(xlsName.trim() + "_整理_.xlsx");
			if (xls.exists()) {
				xls.delete();
			}

			xls.createNewFile();

			System.out.println("write file " + xls.getAbsolutePath());

			fos = new FileOutputStream(xls);
			wb = new XSSFWorkbook(file);
			nwb = new XSSFWorkbook();

			for (int i = 0; i < wb.getNumberOfSheets(); i++) {
				XSSFSheet sheet = wb.getSheetAt(i);
				if ("目录索引".equals(sheet.getSheetName().trim())) {
					continue;
				}
				System.out.println("handle sheet:" + sheet.getSheetName());
				XSSFSheet nSheet = nwb.createSheet(sheet.getSheetName());

				int nRowIdx = 0;
				for (int j = 0; j <= sheet.getLastRowNum(); j++) {
					XSSFRow row = sheet.getRow(j);
					if (row == null || row.getCell(0) == null) {
						continue;
					}

					if (row.getCell(0).getCellTypeEnum() == CellType.STRING) {
						String key = row.getCell(0).getStringCellValue();
						if (StringUtils.isEmpty(key)) {
							continue;
						}
						boolean save = false;
						List<String> filter = null;
						if ("十二生肖".equals(sheet.getSheetName().trim())) {
							filter = filter1;
						} else {
							filter = filter0;
						}

						for (String f : filter) {
							if (key.contains(f)) {
								save = true;
								break;
							}
						}

						if (!save) {
							continue;
						}

						XSSFRow nRow = nSheet.createRow(nRowIdx);
						nRowIdx++;
						if (!"十二生肖".equals(sheet.getSheetName().trim()) && "生日".equals(key) && row.getCell(1).getCellTypeEnum() != CellType.STRING) {
							XSSFCell nCell0 = nRow.createCell(0);
							XSSFCell nCell1 = nRow.createCell(1);
							nCell0.setCellType(row.getCell(0).getCellTypeEnum());
							nCell0.setCellValue(row.getCell(0).getStringCellValue());

							nCell1.setCellType(CellType.STRING);
                            Date date = row.getCell(1).getDateCellValue();
                            SimpleDateFormat fmt = new SimpleDateFormat("M月d日");
							nCell1.setCellValue(fmt.format(date));

						} else {
							nRow.copyRowFrom(row, new CellCopyPolicy((new CellCopyPolicy.Builder()).cellFormula(true).cellStyle(false).cellValue(true)
									.copyHyperlink(true).mergedRegions(false).condenseRows(false).rowHeight(false).build()));
						}
					}
				}
				if (nRowIdx != 9) {
					System.out.println("nRowIdx:" + nRowIdx);
				}
			}
			nwb.write(fos);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
				wb.close();
				nwb.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private static void copyCell(XSSFCell rawCell, XSSFCell destCell) {
		destCell.setCellType(rawCell.getCellTypeEnum());
		switch (rawCell.getCellTypeEnum()) {
		case BOOLEAN:
			destCell.setCellValue(rawCell.getBooleanCellValue());
			break;
		case STRING:
			destCell.setCellValue(rawCell.getStringCellValue());
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
}
