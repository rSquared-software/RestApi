package software.rsquared.restapi;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;

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

	@Override
	protected Call createRequest(HttpUrl url) throws IOException {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.addHeader(CONTENT_TYPE, getMediaType().toString());
		for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		builder.post(getRequestBody());
		return httpClient.newCall(builder.build());
	}
}
