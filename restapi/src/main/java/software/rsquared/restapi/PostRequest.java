package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Post request
 *
 * @author Rafal Zajfert
 * @see Request
 */
@SuppressWarnings("unused")
public abstract class PostRequest<T> extends Request<T> {

	protected PostRequest() {
	}

	@NonNull
	@Override
	protected Call createRequest(OkHttpClient client, HttpUrl url, Map<String, String> headers, RequestBody requestBody) throws RequestException {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.addHeader(CONTENT_TYPE, getMediaType().toString());
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		builder.post(getRequestBody());
		return client.newCall(builder.build());
	}

	@Override
	protected void putParameter(@Nullable Object value) {
		super.putParameter(value);
	}

	@Override
	protected void putParameter(@NonNull String name, @Nullable Object value) {
		super.putParameter(name, value);
	}
}
