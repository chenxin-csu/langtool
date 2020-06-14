package langtool;

public class LangConst {

	public static final String PATH_SPLITER = "/";
	public static final String WORKSPACE_PATH = "upload";
	public static final String WORDS = "words";
	public static final String FILES = "files";
	public static final String STATS = "stats";
	public static final String FILLS = "fills";
	public static final String TMP_PATH = "done";

	public static final String THREAD_LOCAL_KEY_WS = "workspace_path";

	public static final String SESSION_KEY_WORDS = "sk_words";
	public static final String SESSION_KEY_WORDS_INDEX = "sk_words_idx";
	public static final String SESSION_KEY_FILLS_A = "sk_fills_a";
	public static final String SESSION_KEY_FILLS_B = "sk_fills_b";

	public static final String getWorkspacePath(String servletContextRealPath) {
		return servletContextRealPath + LangConst.PATH_SPLITER + LangConst.WORKSPACE_PATH + LangConst.PATH_SPLITER;
	}

}
