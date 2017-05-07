package software.rsquared.restapi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Rafal Zajfert
 */
public class ParameterTest {
    @Test
    public void isFile() throws Exception {
        Parameter parameter = new Parameter("file", "_file{path_to_file}");
        assertTrue(parameter.isFile());
    }

    @Test
    public void getFileName() throws Exception {
        Parameter parameter = new Parameter("file", "_file{path_to_file}");
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