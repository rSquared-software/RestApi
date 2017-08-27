package software.rsquared.restapi.serialization;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;
import software.rsquared.restapi.exceptions.DefaultErrorResponse;
import software.rsquared.restapi.exceptions.RequestException;

import java.io.IOException;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public class JsonErrorDeserializer implements ErrorDeserializer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Config config;

    public JsonErrorDeserializer() {
        this(new Config());
    }

    public JsonErrorDeserializer(Config config) {
        this.config = config;
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(module);
    }

    @Override
    public RequestException read(int responseCode, String content) throws IOException {
        DefaultErrorResponse value = objectMapper.readerFor(config.errorClass).readValue(content);
        return new RequestException(responseCode, value.getName(), value.getMessage(), value.getErrorCode(), value.getErrors());
    }

    @NonNull
    private Class<DefaultErrorResponse> getErrorClass() {
        return DefaultErrorResponse.class;
    }

    public static class Config {
        private Class<? extends DefaultErrorResponse> errorClass = DefaultErrorResponse.class;

        /**
         * Set class of the error model, default: <code>DefaultErrorResponse.class</code>
         */
        public Config setErrorClass(Class<? extends DefaultErrorResponse> errorClass) {
            this.errorClass = errorClass;
            return this;
        }

    }
}
