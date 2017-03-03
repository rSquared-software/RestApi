package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


import java.io.IOException;
import java.util.Map;

import okhttp3.*;
import okhttp3.Request.Builder;

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
    protected Response request(HttpUrl url) throws IOException {
        return mHttpClient.newCall(createRequest(url, getRequestBody())).execute();
    }


    /**
     * Creates Request based on {@code url} and {@code body}
     */
    @NonNull
    private okhttp3.Request createRequest(@NonNull HttpUrl url, @Nullable RequestBody body) {
        Builder requestBuilder = new Builder()
                .url(url)
                .addHeader(CONTENT_TYPE, getMediaType().toString());
        for (Map.Entry<String, String> entry : getHeaders().entrySet()) {
            requestBuilder.addHeader(entry.getKey(), entry.getValue());
        }
        if (body != null) {
            requestBuilder.delete(body);
        } else {
            requestBuilder.delete();
        }
        return requestBuilder.build();
    }
}
