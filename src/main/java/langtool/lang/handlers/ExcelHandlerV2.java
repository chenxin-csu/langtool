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
import org.apache.poi.ss.usermodel.ConditionType;
import org.apache.poi.xssf.usermodel.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCfRule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;

import static langtool.LangConst.PATH_SPLITER;
import static langtool.LangConst.TMP_PATH;

public class ExcelHandlerV2 implements ILangFileHandler {

    // @Override
    public File trans(File file, Map<String, String> words, List<String> wordsIdx) throws Exception {
        FileOutputStream fos = null;
        XSSFWorkbook wb = null;
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
            //支持条件格式
            for (int i = 0; i < sheet.getSheetConditionalFormatting().getNumConditionalFormattings(); i++) {

                XSSFConditionalFormatting condFmt = sheet.getSheetConditionalFormatting().getConditionalFormattingAt(i);
                for (int j = 0; j < condFmt.getNumberOfRules(); j++) {
                    XSSFConditionalFormattingRule rule = condFmt.getRule(j);
                    XSSFConditionalFormattingRule newRule;

                    if (rule.getConditionType() == ConditionType.CELL_VALUE_IS) {
                        newRule = sheet.getSheetConditionalFormatting().createConditionalFormattingRule(rule.getComparisonOperation(),
                                replaceWords(words, wordsIdx, rule.getFormula1()), replaceWords(words, wordsIdx, rule.getFormula2()));
                    } else if (rule.getConditionType() == ConditionType.FORMULA) {
                        newRule = sheet.getSheetConditionalFormatting()
                                .createConditionalFormattingRule(replaceWords(words, wordsIdx, rule.getFormula1()));
                    } else if (rule.getConditionType() == ConditionType.FILTER) {
                        Method method = rule.getClass().getDeclaredMethod("getCTCfRule");
                        method.setAccessible(true);
                        CTCfRule ctCfRule = (CTCfRule) method.invoke(rule);
                        ctCfRule.setText(replaceWords(words, wordsIdx, ctCfRule.getText()));

                        String[] formulaArr = ctCfRule.getFormulaArray();
                        for (int k = 0; k < formulaArr.length; k++) {
                            formulaArr[k] = replaceWords(words, wordsIdx, formulaArr[k]);
                        }
                        ctCfRule.setFormulaArray(formulaArr);
                        newRule = rule;
                    } else {
                        continue;
                    }

                    condFmt.setRule(j, newRule);
                }
            }

            //支持shape中的文本框
            List<XSSFShape> shapes = sheet.getDrawingPatriarch().getShapes();
            for (XSSFShape shape : shapes) {
                if (shape instanceof XSSFSimpleShape) {
                    String text = ((XSSFSimpleShape) shape).getText();
                    if (!StringUtil.isEmpty(text)) {
                        ((XSSFSimpleShape) shape).setText(replaceWords(words, wordsIdx, text));
                    }
                }
            }

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

            int rowOffset = 0;
            if (flags.size() > 0 && flagRow != null) {
                //删除占位行
                sheet.shiftRows(1, sheet.getLastRowNum(), -1);
            }

            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                int colNum = row.getLastCellNum();
                for (int j = 0; j < colNum; j++) {
                    XSSFCell newCol = row.getCell(j);
                    if (newCol == null) {
                        continue;
                    }

                    if (flags.containsValue(j) && !StringUtil.isEmpty(newCol.getRawValue())) {
                        continue;
                    }
                    XSSFComment comment = newCol.getCellComment();
                    if (comment != null) {
                        XSSFRichTextString richTextString = comment.getString();
                        richTextString.setString(replaceWords(words, wordsIdx, richTextString.getString()));
                    }

                    String rawStr;
                    if (newCol.getCellTypeEnum() == CellType.FORMULA)
                        rawStr = newCol.getRawValue();
                    else if (newCol.getCellTypeEnum() == CellType.STRING) {
                        rawStr = newCol.getStringCellValue();
                    } else {
                        continue;
                    }

                    if (rawStr == null || rawStr.trim() == "") {
                        continue;
                    }

                    XSSFCell destCol;
                    if (flags.size() == 0) {
                        //直接原单元格翻译
                        destCol = newCol;
                    } else if (flags.containsKey(j)) {
                        destCol = row.getCell(flags.get(j));
                        if (destCol == null) {
                            destCol = row.createCell(flags.get(j));
                            LangTool.copyCell(row.getCell(j), destCol);
                        }
                    } else {
                        continue;
                    }

                    String doneStr = rawStr;
                    doneStr = replaceWords(words, wordsIdx, doneStr);
                    destCol.setCellValue(doneStr);

                    if (destCol.getCellTypeEnum() == CellType.FORMULA) {
                        destCol.setCellFormula(replaceWords(words, wordsIdx, destCol.getCellFormula()));
                    }
                }

            }
            wb.write(fos);
            return xls;
        } catch (

                Exception e) {
            e.printStackTrace();
            throw new Exception("[error]" + e.getMessage());
        } finally {
            try {
                fos.close();
                wb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private String replaceWords(Map<String, String> words, List<String> wordsIdx, String doneStr) {
        if (doneStr == null)
            return null;
        for (String word : wordsIdx) {
            if (!doneStr.contains(word)) {
                continue;
            }
            doneStr = doneStr.replaceAll(LangTool.quote(word), LangTool.quote(words.get(word)));
        }
        return doneStr;
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
