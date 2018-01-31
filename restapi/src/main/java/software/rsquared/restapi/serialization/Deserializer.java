package software.rsquared.restapi.serialization;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface Deserializer {

	<T> T deserialize(Class<?> requestClass, TypeReference<T> resultType, String content) throws IOException;
}
