package langtool.util;

import langtool.LangConst;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

import static langtool.LangConst.*;

public class FileUtil {
	public static void outputFile(HttpServletResponse resp, File file) throws UnsupportedEncodingException, FileNotFoundException, IOException {
		String contentType = "application/x-msdownload";
		String enc = "utf-8";
		String filename = URLEncoder.encode(file.getName(), enc);
		resp.reset();
		resp.setContentType(contentType);
		resp.addHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
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
			System.out.println("stop clear invalid dir:" + file.getAbsolutePath());
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
		String wsPath = LangToolRuntimeContext.getContext().get(THREAD_LOCAL_KEY_WS);
        File subDir = new File(wsPath + PATH_SPLITER + FILES + PATH_SPLITER + TMP_PATH + PATH_SPLITER);
        if (!subDir.exists()) {
			subDir.mkdirs();
		}
		return subDir;
	}
}
