package langtool.web;

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

import static langtool.LangConst.*;

/**
 * Servlet implementation class OpServlet
 */
@WebServlet("/OpServlet")
public class OpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public OpServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String fileName = req.getParameter("file");
		String filePath = req.getServletContext().getRealPath(PATH_SPLITER) + PATH_SPLITER + WORKSPACE_PATH + PATH_SPLITER
				+ req.getSession(true).getId() + PATH_SPLITER + WORDS + PATH_SPLITER + fileName;
		(new File(filePath)).delete();
		JSONObject ret = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		JSONObject detail = new JSONObject();
		jsonArray.put(detail);
		try {
			ret.put("files", jsonArray);
			detail.put(fileName, true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		PrintWriter writer = resp.getWriter();
		writer.write(ret.toString());
		writer.flush();
		writer.close();
	}

}
