package software.rsquared.restapi;

import org.junit.Test;

import static org.junit.Assert.*;

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

}