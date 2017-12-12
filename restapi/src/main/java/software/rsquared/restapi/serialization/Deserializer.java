package software.rsquared.restapi.serialization;

import java.io.IOException;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface Deserializer {

	<T> T read(Class<?> requestClass, String content) throws IOException;
}
