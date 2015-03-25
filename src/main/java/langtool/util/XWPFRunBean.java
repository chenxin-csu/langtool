package langtool.util;

import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;

public class XWPFRunBean {
	private String color;
	private boolean bold;
	private String fontFamily;
	private int fontSize;
	private boolean italic;
	private boolean strike;
	private VerticalAlign subscript;
	private UnderlinePatterns underline;
	private int textPosition;

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public boolean isBold() {
		return bold;
	}

	public void setBold(boolean bold) {
		this.bold = bold;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public boolean isItalic() {
		return italic;
	}

	public void setItalic(boolean italic) {
		this.italic = italic;
	}

	public boolean isStrike() {
		return strike;
	}

	public void setStrike(boolean strike) {
		this.strike = strike;
	}

	public VerticalAlign getSubscript() {
		return subscript;
	}

	public void setSubscript(VerticalAlign subscript) {
		this.subscript = subscript;
	}

	public UnderlinePatterns getUnderline() {
		return underline;
	}

	public void setUnderline(UnderlinePatterns underline) {
		this.underline = underline;
	}

	public int getTextPosition() {
		return textPosition;
	}

	public void setTextPosition(int textPosition) {
		this.textPosition = textPosition;
	}

}
