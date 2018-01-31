package software.rsquared.restapi;

import android.os.Environment;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.rsquared.restapi.serialization.JsonSerializer;
import software.rsquared.restapi.serialization.ObjectToFormSerializer;
import software.rsquared.restapi.serialization.ObjectToJsonSerializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Rafal Zajfert
 */
public class JsonSerializerTest {

    private ObjectToFormSerializer mSerializer;

    @Before
    public void setUp() throws Exception {
        mSerializer = new ObjectToFormSerializer(new ObjectToFormSerializer.Config().setIntBoolean(true).setTimeInSeconds(true));
    }

    @After
    public void tearDown() throws Exception {
        mSerializer = null;
    }

    @Test
    public void serializeModel() throws Exception {
//        List<Parameter> parameters = new ArrayList<>();
//        mSerializer.serialize(parameters, new RequestNoName());
//        assertEquals(parameters.size(), 15);
//        assertEquals(parameters.get(0).getName(), "param");
//        assertEquals(parameters.get(0).getValue(), "test1");
//        assertEquals(parameters.get(1).getName(), "object[param]");
//        assertEquals(parameters.get(1).getValue(), "test2");
//        assertEquals(parameters.get(2).getName(), "object[file]");
//        assertTrue(parameters.get(2).isFile());
//        assertEquals(parameters.get(3).getName(), "object[files][0]");
//        assertTrue(parameters.get(3).isFile());
//        assertEquals(parameters.get(4).getName(), "object[files][1]");
//        assertTrue(parameters.get(4).isFile());
//        assertEquals(parameters.get(5).getName(), "object[files][2]");
//        assertTrue(parameters.get(5).isFile());
//        assertEquals(parameters.get(6).getName(), "object[report][param]");
//        assertEquals(parameters.get(6).getValue(), "test3");
//        assertEquals(parameters.get(7).getName(), "object[report][param2]");
//        assertEquals(parameters.get(7).getValue(), "3");
//        assertEquals(parameters.get(8).getName(), "object[report][files][0]");
//        assertTrue(parameters.get(8).isFile());
//        assertEquals(parameters.get(9).getName(), "object[report][files][1]");
//        assertTrue(parameters.get(9).isFile());
//        assertEquals(parameters.get(10).getName(), "object[report][files][2]");
//        assertTrue(parameters.get(10).isFile());
//        assertEquals(parameters.get(11).getName(), "file");
//        assertTrue(parameters.get(11).isFile());
//        assertEquals(parameters.get(12).getName(), "files[0]");
//        assertTrue(parameters.get(12).isFile());
//        assertEquals(parameters.get(13).getName(), "files[1]");
//        assertTrue(parameters.get(13).isFile());
//        assertEquals(parameters.get(14).getName(), "files[2]");
//        assertTrue(parameters.get(14).isFile());
    }

    @Test
    public void serializeModelWithName() throws Exception {
//        List<Parameter> parameters = new ArrayList<>();
//        mSerializer.serialize(parameters, new Request());
//        assertEquals(parameters.size(), 15);
//        assertEquals(parameters.get(0).getName(), "request_name[param]");
//        assertEquals(parameters.get(0).getValue(), "test1");
//        assertEquals(parameters.get(1).getName(), "request_name[object][param]");
//        assertEquals(parameters.get(1).getValue(), "test2");
//        assertEquals(parameters.get(2).getName(), "request_name[object][file]");
//        assertTrue(parameters.get(2).isFile());
//        assertEquals(parameters.get(3).getName(), "request_name[object][files][0]");
//        assertTrue(parameters.get(3).isFile());
//        assertEquals(parameters.get(4).getName(), "request_name[object][files][1]");
//        assertTrue(parameters.get(4).isFile());
//        assertEquals(parameters.get(5).getName(), "request_name[object][files][2]");
//        assertTrue(parameters.get(5).isFile());
//        assertEquals(parameters.get(6).getName(), "request_name[object][report][param]");
//        assertEquals(parameters.get(6).getValue(), "test3");
//        assertEquals(parameters.get(7).getName(), "request_name[object][report][param2]");
//        assertEquals(parameters.get(7).getValue(), "3");
//        assertEquals(parameters.get(8).getName(), "request_name[object][report][files][0]");
//        assertTrue(parameters.get(8).isFile());
//        assertEquals(parameters.get(9).getName(), "request_name[object][report][files][1]");
//        assertTrue(parameters.get(9).isFile());
//        assertEquals(parameters.get(10).getName(), "request_name[object][report][files][2]");
//        assertTrue(parameters.get(10).isFile());
//        assertEquals(parameters.get(11).getName(), "request_name[file]");
//        assertTrue(parameters.get(11).isFile());
//        assertEquals(parameters.get(12).getName(), "request_name[files][0]");
//        assertTrue(parameters.get(12).isFile());
//        assertEquals(parameters.get(13).getName(), "request_name[files][1]");
//        assertTrue(parameters.get(13).isFile());
//        assertEquals(parameters.get(14).getName(), "request_name[files][2]");
//        assertTrue(parameters.get(14).isFile());
    }

    @Test
    public void serializeArray() throws Exception {
//        List<String> objects = new ArrayList<>();
//        objects.add("a");
//        objects.add("b");
//        objects.add("c");
//        List<Parameter> parameters = new ArrayList<>();
//        mSerializer.serialize(parameters, objects);
//        assertEquals(parameters.size(), 3);
//        assertEquals(parameters.get(0).getName(), "[0]");
//        assertEquals(parameters.get(0).getValue(), "a");
//        assertEquals(parameters.get(1).getName(), "[1]");
//        assertEquals(parameters.get(1).getValue(), "b");
//        assertEquals(parameters.get(2).getName(), "[2]");
//        assertEquals(parameters.get(2).getValue(), "c");
    }

    @Test
    public void serializeArrayWithName() throws Exception {
//        String[][] array = new String[3][];
//        array[0] = new String[2];
//        array[0][0] = "a";
//        array[0][1] = "b";
//        array[2] = new String[2];
//        array[2][1] = "c";
//        List<Parameter> parameters = new ArrayList<>();
//        mSerializer.serialize(parameters, "strings", array);
//        assertEquals(parameters.size(), 3);
//        assertEquals(parameters.get(0).getName(), "strings[0][0]");
//        assertEquals(parameters.get(0).getValue(), "a");
//        assertEquals(parameters.get(1).getName(), "strings[0][1]");
//        assertEquals(parameters.get(1).getValue(), "b");
//        assertEquals(parameters.get(2).getName(), "strings[2][1]");
//        assertEquals(parameters.get(2).getValue(), "c");
    }

    @Test
    public void serializeCalendar() throws Exception {
//        Calendar calendar = Calendar.getInstance();
//        List<Parameter> parameters = new ArrayList<>();
//        mSerializer.serialize(parameters, "date", calendar);
//        assertEquals(parameters.size(), 1);
//        assertEquals(parameters.get(0).getName(), "date");
//        assertEquals(parameters.get(0).getValue(), String.valueOf(calendar.getTimeInMillis()/1000));
    }

    @Test
    public void serializeBoolean() throws Exception {
//        boolean[] array = new boolean[2];
//        array[0] = true;
//        array[1] = false;
//        List<Parameter> parameters = new ArrayList<>();
//        mSerializer.serialize(parameters, "bool", array);
//        assertEquals(parameters.size(), 2);
//        assertEquals(parameters.get(0).getName(), "bool[0]");
//        assertEquals(parameters.get(0).getValue(), "1");
//        assertEquals(parameters.get(1).getName(), "bool[1]");
//        assertEquals(parameters.get(1).getValue(), "0");
    }

    private static class RequestNoName {
        @JsonProperty("param")
        String param = "test1";

        @JsonProperty("object")
        ObjectClass object = new ObjectClass();

        @JsonProperty("file")
        File file = new File(Environment.getExternalStorageDirectory(), "test.png");

        @JsonProperty("files")
        File[] files = new File[]{new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png")};
    }

    @RestObject("request_name")
    private static class Request {
        @JsonProperty("param")
        String param = "test1";

        @JsonProperty("object")
        ObjectClass object = new ObjectClass();

        @JsonProperty("file")
        File file = new File(Environment.getExternalStorageDirectory(), "test.png");

        @JsonProperty("files")
        File[] files = new File[]{new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png")};
    }

    private static class ObjectClass{

        @JsonProperty("param")
        String param = "test2";

        @JsonProperty("file")
        File file = new File(Environment.getExternalStorageDirectory(), "test.png");

        @JsonProperty("files")
        File[] files = new File[]{new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png")};

        @JsonProperty("report")
        Report report = new Report();

    }

    private static class Report{

        @JsonProperty("param")
        String param = "test3";

        @JsonProperty("param2")
        long param2 = 3;

        @JsonProperty("files")
        File[] files = new File[]{new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png"),
                new File(Environment.getExternalStorageDirectory(), "test.png")};
    }
}