package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.Response;

/**
 * Get request
 *
 * @author Rafal Zajfert
 * @see Request
 */
public abstract class GetRequest<E> extends Request<E> {

	protected GetRequest() {
	}

	@Override
	protected Response request(HttpUrl url) throws IOException {
		return httpClient.newCall(createRequest(url)).execute();
	}

	/**
	 * @deprecated Get request doesn't have body, use {@link #putUrlParameter(String, Object)} instead
	 */
	@Override
	@Deprecated
	protected void putParameter(@NonNull String name, Object value) {
		super.putUrlParameter(name, value);
	}

	/**
	 * Same as {@link #putUrlParameter(Object)}
	 */
	@Override
	protected void putParameter(@Nullable Object value) {
		super.putParameter(value);
	}

	@NonNull
	private okhttp3.Request createRequest(HttpUrl url) {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.get();
		for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		return builder.build();
	}
}
