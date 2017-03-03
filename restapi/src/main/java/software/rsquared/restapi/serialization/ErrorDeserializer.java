package software.rsquared.restapi.serialization;

import software.rsquared.restapi.exceptions.RequestException;

import java.io.IOException;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface ErrorDeserializer {

    RequestException read(int responseCode, String content) throws IOException;
}
