package software.rsquared.restapi;

/**
 * Object contains pair, name and value of the request parameter
 *
 * @author Rafał Zajfert
 */
@SuppressWarnings("unused")
public class Parameter {

    @SuppressWarnings("WeakerAccess")
    public static final String FILE_REGEX = "^_file\\{(.+)\\}$";
    private String name;

    private Object value;

    Parameter() {
    }

    public Parameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    //region Getters and Setters
    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    Object getValue() {
        return value;
    }

    void setValue(Object value) {
        this.value = value;
    }
    //endregion

    /**
     * Checks if value is path to file (string matches "_file{...}")
     */
    boolean isFile() {
        return value instanceof String && ((String) value).matches(FILE_REGEX);
    }

    String getFilePath() {
        return ((String) value).replaceAll(FILE_REGEX, "$1");
    }
}
