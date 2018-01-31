package software.rsquared.restapi;

/**
 * Object contains pair, name and value of the request parameter
 *
 * @author Rafał Zajfert
 */
@SuppressWarnings("unused")
public class Parameter {

	public static final String FILE_PREFIX = "__file";
	private static final String FILE_REGEX = "^" + FILE_PREFIX + "\\{(.+)\\}$";

	private String name;

	private Object value;

	Parameter() {
	}

	public Parameter(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	//region Getters and Setters
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	//endregion

	/**
	 * Checks if value is path to file (string matches "__file{...}")
	 */
	public boolean isFile() {
		return value instanceof String && ((String) value).matches(FILE_REGEX);
	}

	public String getFilePath() {
		return ((String) value).replaceAll(FILE_REGEX, "$1");
	}

}
