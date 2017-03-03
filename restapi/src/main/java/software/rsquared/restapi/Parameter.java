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
    private String mName;

    private Object mValue;

    Parameter() {
    }

    public Parameter(String name, Object value) {
        mName = name;
        mValue = value;
    }

    //region Getters and Setters
    String getName() {
        return mName;
    }

    void setName(String name) {
        mName = name;
    }

    Object getValue() {
        return mValue;
    }

    void setValue(Object value) {
        mValue = value;
    }
    //endregion

    /**
     * Checks if value is path to file (string matches "_file{...}")
     */
    boolean isFile() {
        return mValue instanceof String && ((String) mValue).matches(FILE_REGEX);
    }

    String getFilePath() {
        return ((String) mValue).replaceAll(FILE_REGEX, "$1");
    }
}
