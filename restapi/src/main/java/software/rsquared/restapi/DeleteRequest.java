package software.rsquared.restapi;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.RequestBody;

/**
 * Delete request
 *
 * @author Rafal Zajfert
 * @see Request
 */
@SuppressWarnings("unused")
public abstract class DeleteRequest<T> extends Request<T> {

	protected DeleteRequest() {
	}

	@Override
	protected Call createRequest(HttpUrl url) throws IOException {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.addHeader(CONTENT_TYPE, getMediaType().toString());
		for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		RequestBody body = getRequestBody();
		if (body != null) {
			builder.delete(body);
		} else {
			builder.delete();
		}
		return httpClient.newCall(builder.build());
	}
}
