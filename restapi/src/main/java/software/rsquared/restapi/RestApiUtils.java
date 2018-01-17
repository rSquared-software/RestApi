package software.rsquared.restapi;

import android.support.annotation.Nullable;


/**
 * @author Rafał Zajfert
 */
class RestApiUtils {

	static String getClassCodeLine(String requestClassName) {
		StackTraceElement stackTraceElement = getStackTraceElement(requestClassName);
		if (stackTraceElement != null) {
			return "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")";
		} else {
			return "(Request:1)";
		}
	}

	@Nullable
	private static StackTraceElement getStackTraceElement(String requestClassName) {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		if (elements == null || elements.length <= 2) {
			return null;
		}

		for (int i = 2; i < elements.length; i++) {
			StackTraceElement element = elements[i];
			String className = element.getClassName();
			if (!isExcludedClass(className) && !(className.equals(requestClassName) && "<init>".equals(element.getMethodName()))) {
				return element;
			}
		}
		return null;
	}


	private static boolean isExcludedClass(String className) {
		return className.startsWith("software.rsquared.restapi");
	}
}
