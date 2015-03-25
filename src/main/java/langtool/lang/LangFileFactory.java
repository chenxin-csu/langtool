package langtool.lang;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import langtool.lang.handlers.ExcelHandler;
import langtool.lang.handlers.TextHandler;
import langtool.lang.handlers.WordHandler;

public class LangFileFactory {

	private static final Map<String, ILangFileHandler> map = new HashMap<String, ILangFileHandler>();
	private static final String COMMON = "common";

	static {
		init();
	}

	private static void init() {
		if (map.size() > 0) {
			return;
		}
		synchronized (LangFileFactory.class) {
			if (map.size() > 0) {
				return;
			}
			for (LangFileType type : LangFileType.values()) {
				try {
					map.put(type.getExtensionName(), type.getHandlerClass()
							.newInstance());
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static ILangFileHandler getHandler(String fileName) {
		for (Entry<String, ILangFileHandler> entry : map.entrySet()) {
			if (fileName.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return map.get(COMMON);
	}

	public enum LangFileType {
		DOC("word", WordHandler.class, ".doc"),
		XLS("excel", ExcelHandler.class, ".xls"),
		TEXT("text", TextHandler.class, COMMON),

		;
		private String type;
		private Class<? extends ILangFileHandler> handlerClass;
		private String extensionName;

		private LangFileType(String type,
				Class<? extends ILangFileHandler> handlerClass,
				String extensionName) {
			this.type = type;
			this.handlerClass = handlerClass;
			this.extensionName = extensionName;
		}

		public String getType() {
			return type;
		}

		public Class<? extends ILangFileHandler> getHandlerClass() {
			return handlerClass;
		}

		public String getExtensionName() {
			return extensionName;
		}

	}

}
