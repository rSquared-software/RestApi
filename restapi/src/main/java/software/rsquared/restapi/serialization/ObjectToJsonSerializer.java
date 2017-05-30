package software.rsquared.restapi.serialization;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import software.rsquared.restapi.Parameter;
import software.rsquared.restapi.RestObject;
import software.rsquared.restapi.exceptions.SerializationException;

/**
 * @author Rafal Zajfert
 */
public class ObjectToJsonSerializer implements JsonSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Config config;

    public ObjectToJsonSerializer() {
        this(new Config());
    }

    public ObjectToJsonSerializer(@NonNull Config config) {
        this.config = config;
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        SimpleModule module = new SimpleModule();
        setupModule(module);
        objectMapper.registerModule(module);
        if (!this.config.nullValues) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        if (config.disableAutoDetect) {
            objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        }
    }

    private static boolean isEmpty(CharSequence text) {
        return text == null || text.length() <= 0;
    }

    @CallSuper
    protected void setupModule(SimpleModule module) {
        module.addSerializer(File.class, new com.fasterxml.jackson.databind.JsonSerializer<File>() {
            @Override
            public void serialize(File value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                gen.writeString("_file{" + value.getAbsolutePath() + "}");
            }
        });

        if (config.intBoolean) {
            module.addSerializer(Boolean.class, new com.fasterxml.jackson.databind.JsonSerializer<Boolean>() {
                @Override
                public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    if (value != null) {
                        gen.writeNumber(value ? 1 : 0);
                    }
                }
            });
            module.addSerializer(boolean.class, new com.fasterxml.jackson.databind.JsonSerializer<Boolean>() {
                @Override
                public void serialize(Boolean value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    gen.writeNumber(value != null && value ? 1 : 0);
                }
            });
            module.addSerializer(boolean[].class, new com.fasterxml.jackson.databind.JsonSerializer<boolean[]>() {
                @Override
                public void serialize(boolean[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    if (value == null) {
                        return;
                    }
                    int[] array = new int[value.length];
                    for (int i = 0; i < value.length; i++) {
                        Boolean aBoolean = value[i];
                        array[i] = aBoolean ? 1 : 0;
                    }
                    gen.writeArray(array, 0, array.length);
                }
            });
        }
        if (config.timeInSeconds) {
            module.addSerializer(Calendar.class, new com.fasterxml.jackson.databind.JsonSerializer<Calendar>() {
                @Override
                public void serialize(Calendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
                    if (value != null) {
                        gen.writeNumber(value.getTimeInMillis() / 1000);
                    }
                }
            });
            module.addSerializer(Date.class, new com.fasterxml.jackson.databind.JsonSerializer<Date>() {
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
    public <T> void serialize(@NonNull List<Parameter> parameters, T object) {
        if (object != null) {
            String name = null;
            if (isRestObject(object.getClass())) {
                name = getObjectName(object);
            }
            serialize(parameters, name, object);
        }
    }

    @Override
    public <T> void serialize(@NonNull List<Parameter> parameters, @Nullable String name, T object) {
        if (object != null) {
            JsonNode newNode = objectMapper.valueToTree(object);
            if (parameters.isEmpty()) {
                if (isEmpty(name)) {
                    parameters.add(new Parameter(name, newNode));
                } else {
                    ObjectNode objectNode = objectMapper.createObjectNode();
                    objectNode.replace(name, newNode);
                    parameters.add(new Parameter(null, objectNode));
                }
            } else {
                ObjectNode oldNode = (ObjectNode) parameters.get(0).getValue();
                if (isEmpty(name)) {
                    if (newNode.isObject()) {
                        ObjectNode objectNode = (ObjectNode) newNode;
                        for (Iterator<Map.Entry<String, JsonNode>> iterator = objectNode.fields(); iterator.hasNext(); ) {
                            Map.Entry<String, JsonNode> next = iterator.next();
                            oldNode.replace(next.getKey(), next.getValue());
                        }
                    } else {
                        throw new SerializationException("Unknown property name");
                    }
                } else {
                    oldNode.replace(name, newNode);
                }
            }
        }
    }

    @Override
    public String toJsonString(@NonNull List<Parameter> parameters) {
        if (parameters.isEmpty()) {
            return "";
        }
        ObjectNode node = (ObjectNode) parameters.get(0).getValue();
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e);
        }
    }

    private <T> String getObjectName(T object) {
        RestObject restObject = object.getClass().getAnnotation(RestObject.class);
        if (!isEmpty(restObject.value())) {
            return restObject.value();
        } else {
            return object.getClass().getSimpleName();
        }
    }

    private boolean isRestObject(Class<?> aClass) {
        return aClass.getAnnotation(RestObject.class) != null;
    }

    public static class Config {
        private boolean timeInSeconds;
        private boolean intBoolean;
        private boolean nullValues;
        private boolean disableAutoDetect;

        /**
         * Set true if time should be serialized to unix time seconds
         */
        public Config setTimeInSeconds(boolean timeInSeconds) {
            this.timeInSeconds = timeInSeconds;
            return this;
        }

        /**
         * Set true if booleans should be serialized to 0 / 1
         */
        public Config setIntBoolean(boolean intBoolean) {
            this.intBoolean = intBoolean;
            return this;
        }

        /**
         * Set true if non null values should be serialized
         */
        public Config setSerializeNullValues(boolean nonNullValues) {
            nullValues = nonNullValues;
            return this;
        }

        /**
         * Set true if you want to disable property auto detect (only annotated property will be serialized)
         */
        public Config setDisableAutoDetect(boolean disable) {
            disableAutoDetect = disable;
            return this;
        }
    }
}
