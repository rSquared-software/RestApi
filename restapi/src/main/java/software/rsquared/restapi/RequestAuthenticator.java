package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rafał Zajfert
 */
public abstract class RequestAuthenticator {

	private final List<Pair<String, Object>> parameters = new ArrayList<>();
	private final List<Pair<String, Object>> queryParameters = new ArrayList<>();
	private final List<Pair<String, String>> headers = new ArrayList<>();

	protected abstract void addAuthorization();

	/**
	 * Adds parameter to request body
	 */
	protected final void putParameter(@NonNull String name, @Nullable Object value) {
		parameters.add(new Pair<>(name, value));
	}

	/**
	 * Adds parameter to request body
	 */
	protected final void putParameter(@Nullable Object value) {
		parameters.add(new Pair<>(null, value));
	}

	protected final void addHeader(@NonNull String name, @NonNull String value) {
		headers.add(new Pair<>(name, value));
	}

	/**
	 * Adds parameter to URL's query string.
	 */
	protected final void putQueryParameter(@NonNull String name, @Nullable Object value) {
		queryParameters.add(new Pair<>(name, value));
	}

	/**
	 * Adds parameter to URL's query string.
	 */
	protected final void putQueryParameter(@Nullable Object value) {
		queryParameters.add(new Pair<>(null, value));
	}

	final List<Pair<String, Object>> getParameters() {
		return parameters;
	}

	final List<Pair<String, Object>> getQueryParameters() {
		return queryParameters;
	}

	final List<Pair<String, String>> getHeaders() {
		return headers;
	}
}
