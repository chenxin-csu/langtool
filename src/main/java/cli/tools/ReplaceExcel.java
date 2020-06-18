package cli.tools;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;

/**
 * Created by jackie on 2020/6/11.
 */
public class ReplaceExcel {

    private static Map<String, Words> wordsMap = new HashMap<>();

    private static class Words {
        private String name;
        private String work;
        private String side;
        private String ani;

        public Words(String name, String work, String side, String ani) {
            this.name = name;
            this.work = work;
            this.side = side;
            this.ani = ani;
        }

        public String getAni() {
            return ani;
        }

        public void setAni(String ani) {
            this.ani = ani;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getWork() {
            return work;
        }

        public void setWork(String work) {
            this.work = work;
        }

        public String getSide() {
            return side;
        }

        public void setSide(String side) {
            this.side = side;
        }
    }

    public static void main(String[] args) throws Exception {

        File f = new File("/Users/jackie/Desktop/words.txt");
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = null;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            wordsMap.put(fields[1], new Words(fields[1], fields[2], fields[3], fields[0]));
        }

        filterLines(new File("/Users/jackie/Desktop/qy/done/角色图鉴整理.xlsx"));
    }

    public static void filterLines(File file) {
        XSSFWorkbook wb = null;
        FileOutputStream fos = null;
        try {
            File xls = new File(file.getAbsolutePath() + "_change.xlsx");
            if (xls.exists()) {
                xls.delete();
            }
            xls.createNewFile();
            fos = new FileOutputStream(xls);
            wb = new XSSFWorkbook(file);
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                XSSFSheet sheet = wb.getSheetAt(i);
                if ("十二生肖".equals(sheet.getSheetName().trim())) {
                    continue;
                }
                String name = sheet.getRow(0).getCell(1).getStringCellValue();
                if (wordsMap.containsKey(name)) {
                    Words w = wordsMap.get(name);
                    String addon = "";
                    if (!sheet.getRow(3).getCell(1).getStringCellValue().equals(w.ani)) {
                        addon = sheet.getSheetName() + "： 名字和动物不匹配(依然进行了替换)： 名字：" + name + " 新版：" + w.name + " 动物：" + sheet.getRow(3).getCell(1).getStringCellValue() + " 新版：" + w.ani;
                    }

                    System.out.println(sheet.getSheetName() + "： 名字 " + w.name + " 阵营: " + sheet.getRow(4).getCell(1).getStringCellValue() + "=>" + w.side + " 职业: " + sheet.getRow(5).getCell(1).getStringCellValue() + "=>" + w.work + " **** " + addon);
                    sheet.getRow(4).getCell(1).setCellValue(w.side);
                    sheet.getRow(5).getCell(1).setCellValue(w.work);
                } else {
                    System.out.println(sheet.getSheetName() + "： 一致");
                }
            }
            wb.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                wb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
