package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;

/**
 * Head request
 *
 * @author Rafal Zajfert
 * @see Request
 */
public abstract class HeadRequest<E> extends Request<E> {

	protected HeadRequest() {
	}

	@Override
	protected Call createRequest(HttpUrl url) throws IOException {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.head();
		for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		return httpClient.newCall(builder.build());
	}

	/**
	 * @deprecated Head request doesn't have body, use {@link #putUrlParameter(String, Object)} instead
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
}
