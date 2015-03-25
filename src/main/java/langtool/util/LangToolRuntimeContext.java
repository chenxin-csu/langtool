package langtool.util;

import java.util.HashMap;
import java.util.Map;

public class LangToolRuntimeContext {

	private static final ThreadLocal<LangToolRuntimeContext> contexts = new ThreadLocal<LangToolRuntimeContext>();

	private Map<String, Object> map;

	public static LangToolRuntimeContext getContext() {
		LangToolRuntimeContext context = contexts.get();
		if (context == null) {
			context = new LangToolRuntimeContext();
			contexts.set(context);
		}
		return context;
	}

	public static void clear() {
		contexts.remove();
	}

	public void set(String key, Object obj) {
		if (map == null) {
			map = new HashMap<String, Object>();
		}
		map.put(key, obj);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String key) {
		return map != null ? (T) map.get(key) : null;
	}

	@SuppressWarnings("unchecked")
	public <T> T remove(String key) {
		return map != null ? (T) map.remove(key) : null;
	}

	public boolean containsKey(String key) {
		return map != null && map.containsKey(key);
	}
}
