package langtool.web;

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
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static langtool.LangConst.*;

/**
 * Servlet implementation class UploadServlet
 */
@WebServlet(name = "ConvertServlet", urlPatterns = "/convert")
public class ConvertServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConvertServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        return;
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter writer = response.getWriter();
        JSONObject ret = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        List<FileItem> items = null;
        try {
            String savePath = getWorkspacePath(request) + CONVERT + PATH_SPLITER;
            LangToolRuntimeContext.getContext().set(THREAD_LOCAL_KEY_WS, savePath);
            if (!ServletFileUpload.isMultipartContent(request)) {
                throw new IllegalArgumentException("Request is not multipart, please 'multipart/form-data' enctype for your form.");
            }
            String msg = null;
            ServletFileUpload uploadHandler = new ServletFileUpload(new DiskFileItemFactory());
            items = uploadHandler.parseRequest(request);
            ret.put("files", jsonArray);
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

                if (!item.getName().endsWith(".docx")) {
                    detail.put("error", "文件只支持.docx格式哟，可以点击页面上方查看帮助信息。");
                    continue;
                }
                File file = new File(savePath + item.getName());
                if (file.exists()) {
                    file.delete();
                }
                file.createNewFile();
                item.write(file);

                File convertFile = LangTool.convertStoryWord2Excel(file);
                if (convertFile == null) {
                    throw new Exception("未能成功生成excel文件");
                } else {
                    detail.put("name", convertFile.getName());
                    detail.put("url", WORKSPACE_PATH + PATH_SPLITER + request.getSession(true).getId() + PATH_SPLITER + CONVERT + PATH_SPLITER
                            + DONE_PATH + PATH_SPLITER + convertFile.getName());
                }
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

    private String getWorkspacePath(HttpServletRequest request) {
        return request.getServletContext().getRealPath(PATH_SPLITER) + PATH_SPLITER + WORKSPACE_PATH + PATH_SPLITER + request.getSession(true).getId()
                + PATH_SPLITER;
    }

}
