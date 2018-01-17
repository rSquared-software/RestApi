package software.rsquared.restapi;

/**
 * @author Rafał Zajfert
 */
public interface RestApiLogger {

	default void verbose(String tag, String msg){
		debug(tag, msg);
	}

	void debug(String tag, String msg);

	void info(String tag, String msg);

	default void warning(String tag, String msg){
		info(tag, msg);
	}

	default void error(String tag, String msg){
		info(tag, msg);
	}
}
