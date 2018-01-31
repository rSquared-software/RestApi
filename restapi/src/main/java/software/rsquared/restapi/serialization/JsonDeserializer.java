package software.rsquared.restapi.serialization;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Default implementation of response {@link Deserializer deserializer}
 *
 * @author Rafał Zajfert
 */
public class JsonDeserializer implements Deserializer {

	protected final ObjectMapper objectMapper;
	protected final Config config;

	public JsonDeserializer() {
		this(new Config());
	}

	public JsonDeserializer(@NonNull Config config) {
		this.config = config;
		this.objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		SimpleModule module = new SimpleModule();
		setupModule(module);
		objectMapper.registerModule(module);
	}

	public JsonDeserializer(@NonNull ObjectMapper objectMapper) {
		this.config = new Config();
		this.objectMapper = objectMapper;
	}


	@CallSuper
	protected void setupModule(SimpleModule module) {
		if (config.timeInSeconds) {
			module.addDeserializer(Calendar.class, new com.fasterxml.jackson.databind.JsonDeserializer<Calendar>() {
				@Override
				public Calendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
					if (TextUtils.isEmpty(p.getText())) {
						return null;
					}
					long value = p.getLongValue();
					Calendar calendar = Calendar.getInstance();
					calendar.setTimeInMillis(value * 1000);
					return calendar;
				}
			});
			module.addDeserializer(Date.class, new com.fasterxml.jackson.databind.JsonDeserializer<Date>() {
				@Override
				public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
					if (TextUtils.isEmpty(p.getText())) {
						return null;
					}
					long value = p.getLongValue();
					return new Date(value * 1000);
				}
			});
		}
		module.addDeserializer(Boolean.class, new com.fasterxml.jackson.databind.JsonDeserializer<Boolean>() {
			@Override
			public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
				if (TextUtils.isEmpty(p.getText())) {
					return null;
				}
				String valueText = p.getText();
				try {
					return Integer.parseInt(valueText) > 0;
				} catch (NumberFormatException e) {
					return Boolean.parseBoolean(valueText);
				}
			}
		});
		module.addDeserializer(boolean.class, new com.fasterxml.jackson.databind.JsonDeserializer<Boolean>() {
			@Override
			public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
				if (TextUtils.isEmpty(p.getText())) {
					throw new IOException("Cannot deserialize null primitive value");
				}
				String valueText = p.getText();
				try {
					return Integer.parseInt(valueText) > 0;
				} catch (NumberFormatException ignored) {
					return Boolean.parseBoolean(valueText);
				}
			}
		});
		module.addDeserializer(boolean[].class, new com.fasterxml.jackson.databind.JsonDeserializer<boolean[]>() {
			@Override
			public boolean[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
				String[] value = p.readValueAs(String[].class);
				if (value == null) {
					return null;
				}
				boolean[] array = new boolean[value.length];
				for (int i = 0; i < value.length; i++) {
					String valueText = value[i];
					try {
						array[i] = Integer.parseInt(valueText) > 0;
					} catch (NumberFormatException ignored) {
						array[i] = Boolean.parseBoolean(valueText);
					}
				}
				return array;
			}
		});

	}

	@Override
	public <T> T deserialize(Class<?> requestClass, TypeReference<T> resultType, String content) throws IOException {
		if (TextUtils.isEmpty(content)) {
			content = getEmptyJson(resultType.getType());
		}
		return objectMapper.readerFor(resultType).readValue(content);
	}

	protected String getEmptyJson(Type type) {
		if (isArray(type)) {
			return "[]";
		} else {
			return "{}";
		}
	}

	protected boolean isArray(Type type) {
		if (type.getClass() == Class.class) {
			Class<?> aClass = (Class<?>) type;
			return Collection.class.isAssignableFrom(aClass) || aClass.isArray();
		}
		if (type instanceof GenericArrayType) {
			return true;
		}
		if (type instanceof ParameterizedType) {
			return isArray(((ParameterizedType) type).getRawType());
		}
		return false;
	}

	public static class Config {
		private boolean timeInSeconds;

		/**
		 * Set true if time should be deserialized from unix time seconds. This works with {@link Date} and {@link Calendar}
		 */
		public Config setTimeInSeconds(boolean timeInSeconds) {
			this.timeInSeconds = timeInSeconds;
			return this;
		}
	}
}
