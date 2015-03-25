package langtool.web;

import java.io.File;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import langtool.LangConst;
import langtool.util.FileUtil;

/**
 * Application Lifecycle Listener implementation class LangToolSessionListener
 *
 */
@WebListener
public class LangToolSessionListener implements HttpSessionListener {

	/**
	 * Default constructor.
	 */
	public LangToolSessionListener() {
		System.out.println("start listening sessions...");
	}

	/**
	 * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
	 */
	public void sessionCreated(HttpSessionEvent se) {
		System.out.println("session created:" + se.getSession().getId());
	}

	/**
	 * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
	 */
	public void sessionDestroyed(HttpSessionEvent se) {
		System.out.println("session destroyed:" + se.getSession().getId());
		FileUtil.clearDir(new File((LangConst.getWorkspacePath(se.getSession()
				.getServletContext().getRealPath(LangConst.PATH_SPLITER))
				+ se.getSession().getId() + LangConst.PATH_SPLITER)));

	}

}
