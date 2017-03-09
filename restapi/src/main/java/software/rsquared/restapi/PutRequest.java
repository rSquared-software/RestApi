package software.rsquared.restapi;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.Map;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Put request
 *
 * @author Rafal Zajfert
 * @see Request
 */
@SuppressWarnings("unused")
public abstract class PutRequest<T> extends Request<T> {

    protected PutRequest() {
    }

    @Override
    protected Response request(HttpUrl url) throws IOException {
        return httpClient.newCall(createRequest(url, getRequestBody())).execute();
    }

    @NonNull
    private okhttp3.Request createRequest(HttpUrl url, RequestBody body) {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder()
                .url(url)
                .addHeader(CONTENT_TYPE, getMediaType().toString());
        for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
            builder.addHeader(entry.getKey(), entry.getValue());
        }
        return builder.put(body).build();
    }
}
