package com.rafalzajfert.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rafalzajfert.androidlogger.Logger;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Post request
 *
 * @see Request
 * @author Rafal Zajfert
 */
@SuppressWarnings("unused")
public abstract class PostRequest<T> extends Request<T> {

    protected PostRequest() {
    }

    @Override
    protected Response request() throws IOException {
        HttpUrl url = getUrl();
        Logger.info("[POST]", this.getClass().getSimpleName() + "\n" + url);
        okhttp3.Request request = createRequest(url, getRequestBody());
        return mHttpClient.newCall(request).execute();
    }

    @NonNull
    private okhttp3.Request createRequest(@NonNull HttpUrl url, @Nullable RequestBody body) {
        return new okhttp3.Request.Builder()
                .url(url)
                .addHeader(CONTENT_TYPE, getMediaType().toString())
                .post(body).build();
    }
}
