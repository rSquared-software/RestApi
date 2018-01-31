package software.rsquared.restapi;

import android.support.annotation.NonNull;

import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;

/**
 * Get request
 *
 * @author Rafal Zajfert
 * @see Request
 */
public abstract class GetRequest<E> extends Request<E> {

	protected GetRequest() {
	}

	@NonNull
	@Override
	protected Call createRequest(OkHttpClient client, HttpUrl url, Map<String, String> headers, RequestBody requestBody) {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.get();
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		return client.newCall(builder.build());
	}
}
