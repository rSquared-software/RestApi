package com.rafalzajfert.restapi.serialization;

import com.rafalzajfert.restapi.exceptions.RequestException;

import java.io.IOException;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface ErrorDeserializer {

    RequestException read(int responseCode, String content) throws IOException;
}
