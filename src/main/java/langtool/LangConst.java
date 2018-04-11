package langtool;

public class LangConst {

	public static final String PATH_SPLITER = "/";
	public static final String WORKSPACE_PATH = "upload";
	public static final String WORDS_PATH = "words";
	public static final String FILES_PATH = "files";
	public static final String STATS_PATH = "stats";
	public static final String TMP_PATH = "done";

	public static final String THREAD_LOCAL_KEY_WS = "workspace_path";

	public static final String SESSION_KEY_WORDS = "sk_words";
	public static final String SESSION_KEY_WORDS_INDEX = "sk_words_idx";

	public static final String getWorkspacePath(String servletContextRealPath) {
		return servletContextRealPath + LangConst.PATH_SPLITER + LangConst.WORKSPACE_PATH + LangConst.PATH_SPLITER;
	}

}
