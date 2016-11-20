package com.rafalzajfert.restapi.serialization;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface Deserializer {

    <T> T read(Class<?> requestClass, String content) throws IOException;
}
