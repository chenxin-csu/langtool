package langtool.web;

import langtool.LangConst;
import langtool.LangTool;
import langtool.StatsInfo;
import langtool.TwoTuple;
import langtool.util.FileUtil;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static langtool.LangConst.*;

/**
 * Servlet implementation class UploadServlet
 */
@WebServlet("/UploadServlet")
public class UploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadServlet() {
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
			String savePath = getWorkspacePath(request);
			LangToolRuntimeContext.getContext().set(THREAD_LOCAL_KEY_WS, savePath);
			if ("clear".equals(type)) {
				FileUtil.clearDir(new File(savePath));// 清理工作区
				clearSession(session);// 清理词库
				return;
			}
			if (!ServletFileUpload.isMultipartContent(request)) {
				throw new IllegalArgumentException("Request is not multipart, please 'multipart/form-data' enctype for your form.");
			}
			boolean hasError = false;
			String msg = null;
			if ("files".equals(type)) {
				// check words uploaded
				File wordsDir = new File(savePath + WORDS_PATH + PATH_SPLITER);
				if (!wordsDir.exists() || !wordsDir.isDirectory() || wordsDir.listFiles().length <= 0) {
					hasError = true;
					msg = "请先上传词库文件";
				}
				savePath += FILES_PATH + PATH_SPLITER;
			} else if ("words".equals(type)) {
				savePath += WORDS_PATH + PATH_SPLITER;
			} else if ("stats".equals(type)) {
				savePath += STATS_PATH + PATH_SPLITER;
			}

			ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
			items = uploadHandler.parseRequest(request);
			ret.put("files", jsonArray);
			JSONObject detail = null;
			File saveDir = new File(savePath);
			if (!saveDir.exists()) {
				saveDir.mkdirs();
			}
			List<StatsInfo> statsList = null;
			Map<String, String> params = new HashMap<String, String>();
			for (FileItem item : items) {
				if (item.isFormField()) {
					String formname = item.getFieldName();
					String formvalue = item.getString();
					formname = new String(formname.getBytes(), "UTF-8");
					formvalue = new String(formvalue.getBytes(), "UTF-8");
					params.put(formname, formvalue);
				}
			}
			System.out.println(params);
			for (FileItem item : items) {
				if (!item.isFormField()) {
					detail = new JSONObject();
					jsonArray.put(detail);
					detail.put("name", item.getName());
					detail.put("size", item.getSize());
					if (hasError) {
						detail.put("error", msg);
						continue;
					}
					if (("words").equals(type) && !item.getName().endsWith(".xlsx")) {
						detail.put("error", "词库文件只支持.xlsx格式哟，可以点击页面上方查看帮助信息。");
						continue;
					}
					File file = new File(savePath + item.getName());
					if (file.exists()) {
						file.delete();
					}
					file.createNewFile();
					item.write(file);
					if ("words".equals(type)) {
						detail.put("name", item.getName());
						detail.put("deleteUrl", "op?op=del&file=" + file.getName());
						detail.put("deleteType", "DELETE");
					} else if ("files".equals(type)) {
						System.out.println("trans:" + file.getName());
						File transFile = LangTool.transFile(file, session);
						if (transFile == null) {
							throw new Exception("未能成功生成翻译后文件");
						} else {
							detail.put("name", transFile.getName());
							detail.put("url", WORKSPACE_PATH + PATH_SPLITER + request.getSession(true).getId() + PATH_SPLITER + FILES_PATH
									+ PATH_SPLITER + TMP_PATH + PATH_SPLITER + transFile.getName());
						}
					} else if ("stats".equals(type)) {
						System.out.println("stats:" + file.getName());
						TwoTuple<StatsInfo, JSONObject> tuple = LangTool.statsFile(file, params);
						if (statsList == null) {
							statsList = new ArrayList<StatsInfo>();
						}
						statsList.add(tuple.first);
						detail.put("name", file.getName());
						detail.put("stats", tuple.second);
					}
				}
			}
			if ("stats".equals(type) && statsList != null) {
				long totalWords = 0l;
				for (StatsInfo info : statsList) {
					totalWords += info.getTotalWords();
				}
				ret.put("totalWords", totalWords);
			}
			if (!hasError && "words".equals(type)) {
				loadWords(session, saveDir);
			}

		} catch (Exception e) {
			e.printStackTrace();
			ret = new JSONObject();
			JSONArray error = new JSONArray();
			try {
				ret.put("files", error);
				for (FileItem item : items) {
					JSONObject f = new JSONObject();
					f.put("name", item.getName());
					f.put("size", item.getSize());
					f.put("error", "【出错啦】请尝试检查文件格式：" + e.getMessage());
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
	private void loadWords(HttpSession session, File wordsDir) throws Exception {
		clearSession(session);
		LangTool.loadWords(wordsDir, session);
	}

	private void clearSession(HttpSession session) {
		session.removeAttribute(LangConst.SESSION_KEY_WORDS);
		session.removeAttribute(LangConst.SESSION_KEY_WORDS_INDEX);
	}

	private String getWorkspacePath(HttpServletRequest request) {
		return request.getServletContext().getRealPath(PATH_SPLITER) + PATH_SPLITER + WORKSPACE_PATH + PATH_SPLITER + request.getSession(true).getId()
				+ PATH_SPLITER;
	}

}
