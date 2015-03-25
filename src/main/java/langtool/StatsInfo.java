package langtool;

import java.util.Map;

public class StatsInfo {

	// 总字数
	private String fileName;
	private int fileType;
	private long totalWords;
	private Map<String, Map<String, Long>> excelDetailMap;

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public long getTotalWords() {
		return totalWords;
	}

	public void setTotalWords(long totalWords) {
		this.totalWords = totalWords;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Map<String, Map<String, Long>> getExcelDetailMap() {
		return excelDetailMap;
	}

	public void setExcelDetailMap(Map<String, Map<String, Long>> excelDetailMap) {
		this.excelDetailMap = excelDetailMap;
	}

	@Override
	public String toString() {
		return "StatsInfo [fileName=" + fileName + ", totalWords=" + totalWords
				+ ", excelDetailMap=" + excelDetailMap + "]";
	}

}
