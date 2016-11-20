package com.rafalzajfert.restapi.exceptions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.rafalzajfert.androidlogger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * TODO Dokumentacja
 *
 * @author Rafal Zajfert
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefaultErrorResponse {

	/**
	 * The name of the error
	 */
	@JsonProperty("name")
	private String mName;

	/**
	 * Message of the error
	 */
	@JsonProperty("message")
	private String mMessage;

	/**
	 * Custom error code (not response code)
	 */
	@JsonProperty("code")
	private int mErrorCode;

	/**
	 * Map with fields errors
	 */
	@JsonProperty("errors")
	private Map<String, String[]> mErrors = new HashMap<>();

	@JsonSetter("errors")
	public void setErrors(Map<String, List<Object>> errors) {
		if (errors == null) {
			return;
		}
		mErrors.putAll(parseErrorsMap(null, errors));

	}

	private Map<String, String[]> parseErrorsMap(String objectName, Map<String, List<Object>> errors) {
		Map<String, String[]> resultMap = new HashMap<>();

		for (Map.Entry<String, List<Object>> entry : errors.entrySet()) {
			if (entry.getValue() != null && !entry.getValue().isEmpty()) {
				if (entry.getValue().get(0) instanceof String) {
					String[] values = new String[entry.getValue().size()];
					for (int i = 0; i < entry.getValue().size(); i++) {
						values[i] = String.valueOf(entry.getValue().get(0));
					}
					String key = entry.getKey();
					if (objectName != null) {
						key = objectName + "[" + key + "]";
					}

					resultMap.put(key, values);
				} else if (entry.getValue().get(0) instanceof Map) {
					String key = entry.getKey();
					if (objectName != null) {
						key = objectName + "[" + key + "]";
					}
					for (Object map : entry.getValue()) {
						//noinspection unchecked
						resultMap.putAll(parseErrorsMap(key, (Map<String, List<Object>>) map));
					}
				}
			}
		}
		return resultMap;
	}

	public String getName() {
		return mName;
	}

	public String getMessage() {
		return mMessage;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public Map<String, String[]> getErrors() {
		return mErrors;
	}
}
