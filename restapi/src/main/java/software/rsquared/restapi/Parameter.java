package software.rsquared.restapi;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

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
     * Checks if value is path to file (string matches "_file{...}")
     */
    public boolean isFile() {
        return value instanceof String && ((String) value).matches(FILE_REGEX);
    }

    public String getFilePath() {
        return ((String) value).replaceAll(FILE_REGEX, "$1");
    }

}
