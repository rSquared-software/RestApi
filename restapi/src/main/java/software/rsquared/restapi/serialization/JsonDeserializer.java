package software.rsquared.restapi.serialization;

import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import software.rsquared.restapi.exceptions.DeserializationException;

/**
 * Default implementation of response {@link Deserializer deserializer}
 *
 * @author Rafał Zajfert
 */
public class JsonDeserializer implements Deserializer {

	protected final ObjectMapper objectMapper = new ObjectMapper();
	protected final Config config;

	public JsonDeserializer() {
		this(new Config());
	}

	public JsonDeserializer(@NonNull Config config) {
		this.config = config;
		objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		SimpleModule module = new SimpleModule();
		setupModule(module);
		objectMapper.registerModule(module);
	}

	@CallSuper
	protected void setupModule(SimpleModule module) {
		if (config.timeInSeconds) {
			module.addDeserializer(Calendar.class, new com.fasterxml.jackson.databind.JsonDeserializer<Calendar>() {
				@Override
				public Calendar deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
				public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
			public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
			public Boolean deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
			public boolean[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
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
	public <T> T read(Class<?> requestClass, String content) throws IOException {
		Type superclass = requestClass.getGenericSuperclass();
		while (!(superclass instanceof ParameterizedType) && requestClass.getSuperclass() != null) {
			requestClass = requestClass.getSuperclass();
			superclass = requestClass.getGenericSuperclass();
		}
		if (superclass != null && superclass instanceof ParameterizedType) {
			return readObject(getParameterClasses((ParameterizedType) superclass).get(0), content);
		} else {
			throw new DeserializationException("Unknown parameter response class for " + requestClass.getSimpleName());
		}

	}

	protected <T> T readObject(TypeDescription description, String content) throws IOException {
		if (TextUtils.isEmpty(content)) {
			content = getEmptyJson(description.type);
		}

		if (description.parameters != null) {
			return objectMapper.readerFor(getJavaType(description)).readValue(content);
		} else {
			return objectMapper.readerFor(description.type).readValue(content);
		}
	}

	@Nullable
	protected JavaType getJavaType(TypeDescription description) {
		if (description.parameters != null) {
			if (Collection.class.isAssignableFrom(description.type)) {
				//noinspection unchecked
				return objectMapper.getTypeFactory().constructCollectionType((Class<? extends Collection>) description.type, getJavaType(description.parameters.get(0)));
			} else if (Map.class.isAssignableFrom(description.type)) {
				//noinspection unchecked
				return objectMapper.getTypeFactory().constructMapType((Class<? extends Map>) description.type, getJavaType(description.parameters.get(0)), getJavaType(description.parameters.get(1)));
			} else {
				JavaType[] types = new JavaType[description.parameters.size()];
				for (int i = 0; i < description.parameters.size(); i++) {
					types[i] = getJavaType(description.parameters.get(i));
				}
				return objectMapper.getTypeFactory().constructParametricType(description.type, types);
			}
		} else {
			return objectMapper.getTypeFactory().constructSimpleType(description.type, null);
		}
	}

	protected String getEmptyJson(@NonNull Object object) {
		if (object instanceof Class && isArray((Class<?>) object)) {
			return "[]";
		} else {
			return "{}";
		}
	}

	protected boolean isArray(@NonNull Class<?> clazz) {
		return Collection.class.isAssignableFrom(clazz) || clazz.isArray();
	}

	protected List<TypeDescription> getParameterClasses(@NonNull ParameterizedType type) {
		List<TypeDescription> descriptions = new ArrayList<>();
		Type[] arguments = type.getActualTypeArguments();
		for (int i = 0; i < arguments.length; i++) {
			Type subType = arguments[i];
			if (subType instanceof Class) {
				descriptions.add(new TypeDescription((Class<?>) subType));
			} else if (subType instanceof ParameterizedType) {
				descriptions.add(new TypeDescription(((Class<?>) ((ParameterizedType) subType).getRawType()), getParameterClasses((ParameterizedType) subType)));
			}
		}
		return descriptions;
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

	protected class TypeDescription {
		Class<?> type;
		List<TypeDescription> parameters;

		public TypeDescription(Class<?> type) {
			this.type = type;
		}

		public TypeDescription(Class<?> type, List<TypeDescription> parameters) {
			this.type = type;
			this.parameters = parameters;
		}
	}
}
