package software.rsquared.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import software.rsquared.restapi.serialization.JsonDeserializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Rafal Zajfert
 */
public class JsonDeserializerTest {

    private JsonDeserializer mDeserializer;

    @Before
    public void setUp() throws Exception {
        mDeserializer = new JsonDeserializer(new JsonDeserializer.Config());
    }

    @After
    public void tearDown() throws Exception {
        mDeserializer = null;
    }
//    @Test
//    public void start() throws Exception {
//        Object deserialize = mDeserializer.deserialize(this.getClass(), new TypeReference<List<Integer>>() {
//        }, "");
//        assertEquals(null, deserialize);
//    }
//
//    @Test
//    public void readSimply() throws Exception {
//        StringObjectRequest request = new StringObjectRequest();
//        StringObject result = request.read();
//        assertEquals("test string", result.mString);
//        assertEquals(2, result.mBoolean.length);
//        assertEquals(true, result.mBoolean[0]);
//        assertEquals(false, result.mBoolean[1]);
//    }
//
//    @Test
//    public void readComplex() throws Exception {
//        ListRequest b = new ListRequest();
//        List<Set<String>> result = b.read();
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertEquals(2, result.get(0).size());
//        assertEquals(2, result.get(1).size());
//        assertEquals(2, result.get(2).size());
//    }
//
//    @Test
//    public void readComplex2() throws Exception {
//        MapMapRequest b = new MapMapRequest();
//        Map<String, Map<String, String>> result = b.read();
//        System.err.println(result);
//        assertNotNull(result);
//    }
//
//    private class ListRequest extends A<List<Set<String>>>{
//    }
//
//    private class StringObjectRequest extends B<StringObject>{
//    }
//
//    private class MapMapRequest extends A<Map<String, Map<String, String>>>{
//        Map<String, Map<String, String>> read(){
//            try {
//                return mDeserializer.deserialize(getClass(), getResultType(), "{\"a\":{\"b\":\"c\",\"d\":\"e\"},\"b\":{\"c\":\"d\",\"e\":\"f\",\"g\":\"h\"}}");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        private TypeReference getResultType() {
//            return new TypeReference<T>(){};
//        }
//
//    }

//    private class A<T>{
//        T read(){
//            try {
//                return mDeserializer.deserialize(getClass(), getResultType(), "[[\"a\",\"b\"],[\"c\",\"d\"],[\"e\",\"f\"]]");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        private TypeReference getResultType() {
//            return new TypeReference<T>(){};
//        }
//    }
//
//    private class B<T>{
//        T read(){
//            try {
//                return mDeserializer.deserialize(getClass(), getResultType(), "{\"msg\":\"test string\", \"bool\":[\"1\",\"0\"]}");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        private TypeReference getResultType() {
//            return new TypeReference<T>(){};
//        }
//    }

    private static class StringObject{
        @JsonProperty("msg")
        String mString;

        @JsonProperty("bool")
        boolean[] mBoolean;

    }

}