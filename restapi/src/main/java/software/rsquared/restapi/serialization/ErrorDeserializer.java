package software.rsquared.restapi.serialization;

import java.io.IOException;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface ErrorDeserializer {

	RequestException deserialize(int responseCode, String content) throws IOException;
}
