package software.rsquared.restapi;

import android.support.annotation.NonNull;

import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import software.rsquared.restapi.exceptions.RequestException;

/**
 * Head request
 *
 * @author Rafal Zajfert
 * @see Request
 */
public abstract class HeadRequest<E> extends Request<E> {

	protected HeadRequest() {
	}

	@NonNull
	@Override
	protected Call createRequest(OkHttpClient client, HttpUrl url, Map<String, String> headers, RequestBody requestBody) throws RequestException {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.head();
		for (Map.Entry<String, String> entry : headers.entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		return client.newCall(builder.build());
	}
}
