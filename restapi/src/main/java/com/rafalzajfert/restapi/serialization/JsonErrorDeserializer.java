package com.rafalzajfert.restapi.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rafalzajfert.restapi.exceptions.DefaultErrorResponse;
import com.rafalzajfert.restapi.exceptions.RequestException;

import java.io.IOException;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public class JsonErrorDeserializer implements ErrorDeserializer {

    private final ObjectMapper mObjectMapper = new ObjectMapper();

    public JsonErrorDeserializer() {
        mObjectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        SimpleModule module = new SimpleModule();
        mObjectMapper.registerModule(module);
    }

    @Override
    public RequestException read(int responseCode, String content) throws IOException {
        DefaultErrorResponse value = mObjectMapper.readerFor(DefaultErrorResponse.class).readValue(content);
        return new RequestException(responseCode, value.getName(), value.getMessage(), value.getErrorCode(), value.getErrors());
    }
}
