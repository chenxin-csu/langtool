package langtool.web;

import langtool.LangConst;
import langtool.LangTool;
import langtool.util.LangToolRuntimeContext;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static langtool.LangConst.*;

/**
 * Servlet implementation class UploadServlet
 */
@WebServlet("/FillServlet")
public class FillServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FillServlet() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(true);
		PrintWriter writer = response.getWriter();
		JSONObject ret = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		List<FileItem> items = null;
		try {
			String type = request.getParameter("type");
			if (!FILLS_SRC.equals(type) && !FILLS_DST.equals(type)) {
				throw new IllegalArgumentException("invalid fill type:" + type);
			}
			String savePath = getWorkspacePath(request) + FILLS + PATH_SPLITER;
			LangToolRuntimeContext.getContext().set(THREAD_LOCAL_KEY_WS, savePath);
			if (!ServletFileUpload.isMultipartContent(request)) {
				throw new IllegalArgumentException("Request is not multipart, please 'multipart/form-data' enctype for your form.");
			}
			String msg = null;
			File fillSrcDir = new File(savePath + FILLS_SRC + PATH_SPLITER);
			boolean hasError = false;
			if (FILLS_DST.equals(type)) {
				if (!fillSrcDir.exists() || !fillSrcDir.isDirectory() || fillSrcDir.listFiles().length != 2) {
					hasError = true;
					msg = "请先上传原文件和翻译文件";
				}
			}
			savePath += type + PATH_SPLITER;
			ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
			items = uploadHandler.parseRequest(request);
			ret.put(TRANS_FILES, jsonArray);
			JSONObject detail = null;
			File saveDir = new File(savePath);
			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}

			for (FileItem item : items) {
				detail = new JSONObject();
				jsonArray.put(detail);
				detail.put("name", item.getName());
				detail.put("size", item.getSize());

				if (hasError) {
					detail.put("error", msg);
					continue;
				}

				if ((FILLS_SRC).equals(type) && !item.getName().endsWith(".docx")) {
					detail.put("error", "原文件和翻译文件只支持.docx格式哟，可以点击页面上方查看帮助信息。");
					continue;
				}
				if ((FILLS_DST).equals(type) && !item.getName().endsWith(".xlsx")) {
					detail.put("error", "填充表格只支持.xlsx格式哟，可以点击页面上方查看帮助信息。");
					continue;
				}
				File file = new File(savePath + item.getName());
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();
				item.write(file);
				if (FILLS_SRC.equals(type)) {
					detail.put("name", item.getName());
					detail.put("deleteUrl", "op?op=del&file=" + file.getName());
					detail.put("deleteType", "DELETE");
				} else if (FILLS_DST.equals(type)) {
					loadFillSrc(session, fillSrcDir);
					File fillFile = LangTool.fillFile(file, session);
					if (fillFile == null) {
						throw new Exception("未能成功生成翻译后文件");
					} else {
						detail.put("name", fillFile.getName());
						detail.put("url", WORKSPACE_PATH + PATH_SPLITER + request.getSession(true).getId() + PATH_SPLITER + FILLS + PATH_SPLITER
								+ FILLS_DST + PATH_SPLITER + DONE_PATH + PATH_SPLITER + fillFile.getName());
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = new JSONObject();
			JSONArray error = new JSONArray();
			try {
				ret.put(TRANS_FILES, error);
				for (FileItem item : items) {
					JSONObject f = new JSONObject();
					f.put("name", item.getName());
					f.put("size", item.getSize());
					f.put("error", "【出错啦】" + e.getMessage());
					error.put(f);
				}
			} catch (JSONException e1) {
				e1.printStackTrace();
			}

		} finally {
			LangToolRuntimeContext.clear();
			response.setContentType("application/json");
			writer.write(ret.toString());
			writer.flush();
			writer.close();
		}
	}

	// 刷新session中的词库
	private void loadFillSrc(HttpSession session, File wordsDir) throws Exception {
		clearSession(session);
		LangTool.loadFillSrc(wordsDir, session);
	}

	private void clearSession(HttpSession session) {
		session.removeAttribute(LangConst.SESSION_KEY_FILLS_A);
		session.removeAttribute(LangConst.SESSION_KEY_FILLS_B);
	}

	private String getWorkspacePath(HttpServletRequest request) {
		return request.getServletContext().getRealPath(PATH_SPLITER) + PATH_SPLITER + WORKSPACE_PATH + PATH_SPLITER + request.getSession(true).getId()
				+ PATH_SPLITER;
	}

}
