package com.rafalzajfert.restapi.sample;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rafalzajfert.androidlogger.Logger;
import com.rafalzajfert.restapi.GetRequest;
import com.rafalzajfert.restapi.RestApi;
import com.rafalzajfert.restapi.exceptions.RequestException;
import com.rafalzajfert.restapi.listeners.ResponseListener;
import com.rafalzajfert.restapi.listeners.ResponsePoolListener;

import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RestApi.setConfiguration(new RestApi.Config()
                .setScheme(RestApi.Config.HTTP)
                .setHost("api.rafalzajfert.com")
        );

        RestApi.execute(new GetVersion(), new ResponseListener<Version>() {
            @Override
            public void onSuccess(Version result) {
                Logger.debug(result);
            }

            @Override
            public void onFailed(RequestException e) {
                Logger.error(e);
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Version version = RestApi.executeSync(new GetVersion());
                    Logger.debug(version);
                } catch (RequestException e) {
                    Logger.error(e);
                }
            }
        }).start();



        RestApi.pool(RestApi.THREAD_POOL_EXECUTOR)
                .add(new GetVersion(), 1)
                .add(new GetVersion(), 2)
                .add(new GetVersion(), 3)
                .execute(new ResponsePoolListener() {
                    @Override
                    public void onTaskSuccess(Object result, int requestCode) {

                    }

                    @Override
                    public void onSuccess(@NonNull Map<Integer, Object> result) {

                    }

                    @Override
                    public boolean onFailed(RequestException e, int requestCode) {
                        return false;
                    }
                });

//TODO next futures
//        RestApi.get(new TypeReference<List<User>>() {}, "v1", "user")
//                .withAccessToken()
//                .with("username", "Rafal")
//                .with("min-age", 21)
//                .execute(new ResponseListener<>() {
//                    public void onSuccess(List<User> user) {
//
//                    }
//
//                    public void onFailed(RequestException e) {
//
//                    }
//                });
//
//        RestApi.post(SuccessResponse.class, "v1", "user", "update")
//                .withUrlParam("id", 2)
//                .with("username", "Rafal")
//                .with("min-age", 21)
//                .execute(new ResponseListener<>() {
//                    public void onSuccess(SuccessResponse success) {
//
//                    }
//
//                    public void onFailed(RequestException e) {
//
//                    }
//                });
    }

    private class GetVersion extends GetRequest<Version>{

        @Override
        protected void prepareRequest() {
            setUrlSegments("api", "version");
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version{
        @JsonProperty("latest_version")
        private ApiVersion version;

        @Override
        public String toString() {
            return "Version{" +
                    "version=" + version +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiVersion{
        @JsonProperty("api_version")
        private String apiVersion;
        @JsonProperty("expiration_date")
        private Calendar expirationDate;

        @Override
        public String toString() {
            return "ApiVersion{" +
                    "apiVersion='" + apiVersion + '\'' +
                    ", expirationDate=" + expirationDate +
                    '}';
        }
    }
}
