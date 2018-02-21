package software.rsquared.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rafal Zajfert
 */
public class ParameterTest {
    @Test
    public void isFile() throws Exception {
        Parameter parameter = new Parameter("file", "__file{path_to_file}");
        assertTrue(parameter.isFile());
    }

    @Test
    public void getFileName() throws Exception {
        Parameter parameter = new Parameter("file", "__file{path_to_file}");
        assertEquals(parameter.getFilePath(), "path_to_file");

        parameter = new Parameter("file", "path_to_file");
        assertEquals(parameter.getFilePath(), "path_to_file");

    }

    private class Report {
        @JsonProperty("param")
        String param = "test3";

        @JsonProperty("param2")
        long param2 = 3;
    }
}