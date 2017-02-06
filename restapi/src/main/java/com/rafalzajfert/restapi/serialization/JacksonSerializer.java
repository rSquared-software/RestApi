package com.rafalzajfert.restapi.serialization;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rafalzajfert.restapi.Parameter;
import com.rafalzajfert.restapi.RestObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Rafal Zajfert
 */
public class JacksonSerializer implements Serializer {
    private final ObjectMapper mObjectMapper = new ObjectMapper();
    private final Config mConfig;

    public JacksonSerializer() {
        this(new Config());
    }

    public JacksonSerializer(@NonNull Config config) {
        mConfig = config;
        mObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        SimpleModule module = new SimpleModule();
        setupModule(module);
        mObjectMapper.registerModule(module);
        if (!mConfig.mNullValues) {
            mObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
    }

    @CallSuper
    protected void setupModule(SimpleModule module) {
        module.addSerializer(File.class, new JsonSerializer<File>() {
            @Override
            public void serialize(File value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                gen.writeString("_file{" + value.getAbsolutePath() + "}");
            }
        });

        if (mConfig.mIntBoolean) {
            module.addSerializer(Boolean.class, new JsonSerializer<Boolean>() {
                @Override
                public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    gen.writeNumber(value ? 1 : 0);
                }
            });
            module.addSerializer(boolean.class, new JsonSerializer<Boolean>() {
                @Override
                public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    gen.writeNumber(value ? 1 : 0);
                }
            });
            module.addSerializer(boolean[].class, new JsonSerializer<boolean[]>() {
                @Override
                public void serialize(boolean[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    int[] array = new int[value.length];
                    for (int i = 0; i < value.length; i++) {
                        Boolean aBoolean = value[i];
                        array[i] = aBoolean ? 1 : 0;
                    }
                    gen.writeArray(array, 0, array.length);
                }
            });
        }
        if (mConfig.mTimeInSeconds) {
            module.addSerializer(Calendar.class, new JsonSerializer<Calendar>() {
                @Override
                public void serialize(Calendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    if (value != null) {
                        gen.writeNumber(value.getTimeInMillis() / 1000);
                    }
                }
            });
            module.addSerializer(Date.class, new JsonSerializer<Date>() {
                @Override
                public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    if (value != null) {
                        gen.writeNumber(value.getTime() / 1000);
                    }
                }
            });
        }

    }

    @Override
    public <T> List<Parameter> serialize(T object) {
        String name = null;
        if (isRestObject(object.getClass())) {
            name = getObjectName(object);
        }
        return serialize(name, object);
    }


    @Override
    public <T> List<Parameter> serialize(@Nullable String name, T object) {
        List<Parameter> parameters = new ArrayList<>();
        JsonNode jsonNode = mObjectMapper.valueToTree(object);
        addParameter(name, jsonNode, parameters);
        return parameters;
    }

    private void addParameter(String name, JsonNode jsonNode, List<Parameter> parameters) {
        if (jsonNode.isArray()) {
            for (int i = 0; i < jsonNode.size(); i++) {
                JsonNode child = jsonNode.get(i);
                if (!child.isNull()) {
                    String key = name == null ? "[" + i + "]" : name + "[" + i + "]";
                    addParameter(key, child, parameters);
                }
            }
        } else if (jsonNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = name == null ? entry.getKey() : name + "[" + entry.getKey() + "]";
                addParameter(key, entry.getValue(), parameters);
            }
        } else {
            parameters.add(new Parameter(name, jsonNode.asText()));
        }
    }

    private <T> String getObjectName(T object) {
        RestObject restObject = object.getClass().getAnnotation(RestObject.class);
        if (!TextUtils.isEmpty(restObject.value())) {
            return restObject.value();
        } else {
            return object.getClass().getSimpleName();
        }
    }

    private boolean isRestObject(Class<?> aClass) {
        return aClass.getAnnotation(RestObject.class) != null;
    }

    public static class Config {
        private boolean mTimeInSeconds;
        private boolean mIntBoolean;
        private boolean mNullValues;

        /**
         * Set true if time should be serialized to unix time seconds
         */
        public Config setTimeInSeconds(boolean timeInSeconds) {
            mTimeInSeconds = timeInSeconds;
            return this;
        }

        /**
         * Set true if booleans should be serialized to 0 / 1
         */
        public Config setIntBoolean(boolean intBoolean) {
            mIntBoolean = intBoolean;
            return this;
        }

        /**
         * Set true if non null values should be serialized
         */
        public Config setNullValues(boolean nonNullValues) {
            mNullValues = nonNullValues;
            return this;
        }
    }
}
