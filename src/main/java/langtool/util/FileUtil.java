package langtool.util;

import static langtool.LangConst.FILES_PATH;
import static langtool.LangConst.PATH_SPLITER;
import static langtool.LangConst.THREAD_LOCAL_KEY_WS;
import static langtool.LangConst.TMP_PATH;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import langtool.LangConst;

public class FileUtil {
	public static void outputFile(HttpServletResponse resp, File file)
			throws UnsupportedEncodingException, FileNotFoundException,
			IOException {
		String contentType = "application/x-msdownload";
		String enc = "utf-8";
		String filename = URLEncoder.encode(file.getName(), enc);
		resp.reset();
		resp.setContentType(contentType);
		resp.addHeader("Content-Disposition", "attachment; filename=\""
				+ filename + "\"");
		int fileLength = (int) file.length();
		resp.setContentLength(fileLength);
		/* 如果文件长度大于0 */
		if (fileLength != 0) {
			/* 创建输入流 */
			InputStream inStream = new FileInputStream(file);
			byte[] buf = new byte[4096];
			/* 创建输出流 */
			ServletOutputStream servletOS = resp.getOutputStream();
			int readLength;
			while (((readLength = inStream.read(buf)) != -1)) {
				servletOS.write(buf, 0, readLength);
			}
			inStream.close();
			servletOS.flush();
			servletOS.close();
		}
	}

	public static void clearDir(File file) {
		// for safety clear
		if (!file.getAbsolutePath().contains(LangConst.WORKSPACE_PATH)) {
			System.out.println("stop clear invalid dir:"
					+ file.getAbsolutePath());
			return;
		}
		if (!file.exists()) {
			return;
		}
		System.out.println("clear dir:" + file.getAbsolutePath());
		if (file.isFile()) {
			file.delete();
		} else {
			if (file.delete()) {
				return;
			}
			for (File f : file.listFiles()) {
				clearDir(f);
			}
			file.delete();
		}

	}

	public static File checkDoneDir() {
		String wsPath = LangToolRuntimeContext.getContext().get(
				THREAD_LOCAL_KEY_WS);
		File subDir = new File(wsPath + PATH_SPLITER + FILES_PATH
				+ PATH_SPLITER + TMP_PATH + PATH_SPLITER);
		if (!subDir.exists()) {
			subDir.mkdirs();
		}
		return subDir;
	}
}
