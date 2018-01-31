package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Method;


/**
 * @author Rafał Zajfert
 */
final class RestApiLoggerFactory {

	private RestApiLoggerFactory() {
	}

	public static RestApiLogger create() {
		RestApiLogger logger;
		try {
			Class<?> loggerClass = Class.forName("software.rsquared.androidlogger.Logger");

			try {
				Class<?> loggerTagClass = Class.forName("software.rsquared.androidlogger.Tag");
				Method verbose = loggerClass.getMethod("verbose", loggerTagClass, Object.class);
				Method debug = loggerClass.getMethod("debug", loggerTagClass, Object.class);
				Method info = loggerClass.getMethod("info", loggerTagClass, Object.class);
				Method createTag = loggerTagClass.getMethod("create", String.class);
				logger = new R2LoggerTagged(createTag, verbose, debug, info);
			} catch (Exception e) {
				Method verbose = loggerClass.getMethod("verbose", Object.class);
				Method debug = loggerClass.getMethod("debug", Object.class);
				Method info = loggerClass.getMethod("info", Object.class);
				logger = new R2Logger(verbose, debug, info);
			}
		} catch (Exception e) {
			logger = null;
		}

		if (logger == null) {
			logger = new AndroidLogLogger();
		}
		return logger;
	}

	private static class R2LoggerTagged implements RestApiLogger {
		private final Method createTag;
		private final Method verbose;
		private final Method debug;
		private final Method info;

		R2LoggerTagged(@NonNull Method createTag, Method verbose, Method debug, Method info) {
			this.createTag = createTag;
			this.verbose = verbose;
			this.debug = debug;
			this.info = info;
		}

		@Override
		public void verbose(String tag, String msg) {
			try {
				verbose.invoke(null, createTag.invoke(null, tag), msg);
			} catch (Exception e) {
				Log.v(tag, msg);
			}
		}

		@Override
		public void debug(String tag, String msg) {
			try {
				debug.invoke(null, createTag.invoke(null, tag), msg);
			} catch (Exception e) {
				Log.d(tag, msg);
			}
		}

		@Override
		public void info(String tag, String msg) {
			try {
				info.invoke(null, createTag.invoke(null, tag), msg);
			} catch (Exception e) {
				Log.i(tag, msg);
			}
		}

	}

	private static class R2Logger implements RestApiLogger {
		private final Method verbose;
		private final Method debug;
		private final Method info;

		R2Logger(Method verbose, Method debug, Method info) {
			this.verbose = verbose;
			this.debug = debug;
			this.info = info;
		}

		@Override
		public void verbose(String tag, String msg) {
			try {
				verbose.invoke(null, msg);
			} catch (Exception e) {
				Log.v(tag, msg);
			}
		}

		@Override
		public void debug(String tag, String msg) {
			try {
				debug.invoke(null, msg);
			} catch (Exception e) {
				Log.d(tag, msg);
			}
		}

		@Override
		public void info(String tag, String msg) {
			try {
				info.invoke(null, msg);
			} catch (Exception e) {
				Log.i(tag, msg);
			}
		}

	}


	private static class AndroidLogLogger implements RestApiLogger {

		@Override
		public void verbose(String tag, String msg) {
			Log.v(tag, msg);
		}

		@Override
		public void debug(String tag, String msg) {
			Log.d(tag, msg);
		}

		@Override
		public void info(String tag, String msg) {
			Log.i(tag, msg);
		}
	}
}
