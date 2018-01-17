package software.rsquared.restapi;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.HttpUrl;

/**
 * Patch request
 *
 * @author Rafal Zajfert
 * @see Request
 */
@SuppressWarnings("unused")
public abstract class PatchRequest<T> extends Request<T> {

	protected PatchRequest() {
	}

	@Override
	protected Call createRequest(HttpUrl url) throws IOException {
		okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
				.url(url)
				.addHeader(CONTENT_TYPE, getMediaType().toString());
		for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
			builder.addHeader(entry.getKey(), entry.getValue());
		}
		builder.patch(getRequestBody());
		return httpClient.newCall(builder.build());
	}
}
