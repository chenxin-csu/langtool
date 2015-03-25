package langtool.web;

import java.io.File;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import langtool.LangConst;
import langtool.util.FileUtil;

/**
 * Application Lifecycle Listener implementation class LangToolServerListener
 *
 */
@WebListener
public class LangToolServerListener implements ServletContextListener {

	/**
	 * Default constructor.
	 */
	public LangToolServerListener() {
		System.out.println("start listening context...");
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent sce) {
		System.out.println("context inited");
		FileUtil.clearDir(new File((LangConst.getWorkspacePath(sce
				.getServletContext().getRealPath(LangConst.PATH_SPLITER)))));
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent sce) {
		System.out.println("context destroyed");
	}

}
