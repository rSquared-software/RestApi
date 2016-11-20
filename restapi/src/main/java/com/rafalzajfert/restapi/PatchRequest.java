package com.rafalzajfert.restapi;

import android.support.annotation.NonNull;

import com.rafalzajfert.androidlogger.Logger;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    protected Response request() throws IOException {
        HttpUrl url = getUrl();
        Logger.info("[PATCH]", this.getClass().getSimpleName() + "\n" + url);
        okhttp3.Request request = createRequest(url, getRequestBody());
        return mHttpClient.newCall(request).execute();
    }
    @NonNull
    private okhttp3.Request createRequest(HttpUrl url, RequestBody body) {
        return new okhttp3.Request.Builder()
                    .url(url)
                    .addHeader(CONTENT_TYPE, getMediaType().toString())
                    .patch(body).build();
    }
}
