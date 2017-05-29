package software.rsquared.restapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.rsquared.restapi.serialization.JsonDeserializer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Rafal Zajfert
 */
public class JsonDeserializerTest {

    private JsonDeserializer mDeserializer;

    @Before
    public void setUp() throws Exception {
        mDeserializer = new JsonDeserializer(new JsonDeserializer.Config().setIntBoolean(true));
    }

    @After
    public void tearDown() throws Exception {
        mDeserializer = null;
    }

    @Test
    public void readSimply() throws Exception {
        StringObjectRequest request = new StringObjectRequest();
        StringObject result = request.read();
        assertEquals("test string", result.mString);
        assertEquals(2, result.mBoolean.length);
    }

    @Test
    public void readComplex() throws Exception {
        ListRequest b = new ListRequest();
        List<Set<String>> result = b.read();
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(2, result.get(0).size());
        assertEquals(2, result.get(1).size());
        assertEquals(2, result.get(2).size());
    }

    private class ListRequest extends A<List<Set<String>>>{
    }

    private class StringObjectRequest extends B<StringObject>{
    }

    private class A<T>{
        T read(){
            try {
                return mDeserializer.read(getClass(), "[[\"a\",\"b\"],[\"c\",\"d\"],[\"e\",\"f\"]]");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class B<T>{
        T read(){
            try {
                return mDeserializer.read(getClass(), "{\"msg\":\"test string\", \"bool\":[\"1\",\"0\"]}");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class StringObject{
        @JsonProperty("msg")
        String mString;

        @JsonProperty("bool")
        boolean[] mBoolean;

    }

}