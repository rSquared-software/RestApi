package com.rafalzajfert.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rafalzajfert.androidlogger.Logger;

import java.io.IOException;

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
    protected Response request() throws IOException {
        HttpUrl url = getUrl();
        RestApi.getLogger().i("[DELETE]", this.getClass().getSimpleName() + "\n" + url);
        okhttp3.Request request = createRequest(url, getRequestBody());
        return mHttpClient.newCall(request).execute();
    }


    /**
     * Creates Request based on {@code url} and {@code body}
     */
    @NonNull
    private okhttp3.Request createRequest(@NonNull HttpUrl url, @Nullable RequestBody body) {
        Builder requestBuilder = new Builder()
                .url(url)
                .addHeader(CONTENT_TYPE, getMediaType().toString());
        if (body != null) {
            requestBuilder.delete(body);
        } else {
            requestBuilder.delete();
        }
        return requestBuilder.build();
    }
}
