package com.rafalzajfert.restapi.sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rafalzajfert.androidlogger.Logger;
import com.rafalzajfert.restapi.GetRequest;
import com.rafalzajfert.restapi.RestApi;
import com.rafalzajfert.restapi.exceptions.RequestException;
import com.rafalzajfert.restapi.listeners.ResponseListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RestApi.setConfiguration(new RestApi.Configuration()
                .setScheme(RestApiConfiguration.HTTP)
                .setHost("sem-api.projectown.net")
        );

//        RestApi.execute(new GetVersion(), new ResponseListener<Version>() {
//            @Override
//            public void onSuccess(Version result) {
//                Logger.error(result);
//            }
//
//            @Override
//            public void onFailed(RequestException e) {
//                Logger.error(e);
//            }
//        });

//        RestApi.pool(RestApi.THREAD_POOL_EXECUTOR)
//                .add(new Request(), 1, new PostTask())
//                .add(new Request(), 1, new PostTask())
//                .add(new Request(), 2, new PostTask())
//                .execute(new PoolResponseListener<>() {
//                    public void onSuccess(Map<Integer, Object> results) {
//
//                    }
//
//                    public void onFailed(RequestException e) {
//
//                    }
//                });


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
