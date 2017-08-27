package software.rsquared.restapi.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Calendar;

import software.rsquared.androidlogger.Logger;
import software.rsquared.restapi.GetRequest;
import software.rsquared.restapi.MockFactory;
import software.rsquared.restapi.Request;
import software.rsquared.restapi.RestApi;
import software.rsquared.restapi.RestApiConfiguration;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RestApi.setConfiguration(new RestApiConfiguration()
                        .setScheme(RestApiConfiguration.HTTP)
                        .setHost("api.host.com")
                        .setMockFactory(new CustomMockFactory())
//                .setPort(80)
//                .setInitialRequirements(new InitialRequirements() {
//                    @Override
//                    public void onCheckRequirements(Request<?> request) throws InitialRequirementsException {
//                        if (!isOnline()){
//                            throw new InitialRequirementsException("Offline!");
//                        }
//                    }
//                })
//                .setRestAuthorizationService(UserService.getInstance())
//                .setTimeout(60000)
//                .setLogLevel(Level.VERBOSE)
//                .addHeader("lang", "en")
//                .setAuthorization("user", "pass123")
//                .setDeserializer(new JsonDeserializer(new JsonDeserializer.Config()
//                        .setTimeInSeconds(true)))
//                .setSerializer(new JsonSerializer(new JsonSerializer.Config()
//                        .setTimeInSeconds(true)
//                        .setIntBoolean(true)
//                        .setNullValues(true)))
//                .setErrorDeserializer(new JsonErrorDeserializer(new JsonErrorDeserializer.Config()
//                        .setErrorClass(DefaultErrorResponse.class)))
        );

        GetVersion getVersion = new GetVersion();
        RestApi.execute(getVersion, new RequestListener<Version>() {
            @Override
            public void onSuccess(Version result) {
                Logger.error("onSuccess");
                Logger.debug(result);
            }

            @Override
            public void onFailed(RequestException e) {
                Logger.error(e);
            }
        });

//        try {
//            Version version = RestApi.executeSync(new GetVersion());
//        } catch (RequestException e) {
//            Logger.error(e);
//        }

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Version version = RestApi.executeSync(new GetVersion());
//                    Logger.debug(version);
//                } catch (RequestException e) {
//                    Logger.error(e);
//                }
//            }
//        }).start();

//
//
//        RestApi.pool(RestApi.SERIAL_EXECUTOR)
//                .add(new GetVersion(), 1)
//                .add(new GetVersion(), 2)
//                .add(new GetVersion(), 3)
//                .execute(new RequestPoolListener() {
//                    @Override
//                    public void onSuccess(@NonNull Map<Integer, Object> result) {
//
//                    }
//
//                    @Override
//                    public boolean onFailed(RequestException e, int requestCode) {
//                        return false;
//                    }
//                });

//TODO next features
//        RestApi.get(new TypeReference<List<User>>() {}, "v1", "user")
//                .withAccessToken()
//                .with("username", "Rafal")
//                .with("min-age", 21)
//                .execute(new RequestListener<>() {
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
//                .execute(new RequestListener<>() {
//                    public void onSuccess(SuccessResponse success) {
//
//                    }
//
//                    public void onFailed(RequestException e) {
//
//                    }
//                });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version {
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
    public static class ApiVersion {
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

    public class CustomMockFactory extends MockFactory {
        @Override
        public <T> T getMockResponse(Request<T> request) {
            if (BuildConfig.DEBUG) {
                if (request instanceof GetVersion) {
                    return (T) getVersion();
                }
            }
            return null;
        }

        @NonNull
        private Version getVersion() {
            Version version = new Version();
            ApiVersion apiVersion = new ApiVersion();
            apiVersion.apiVersion = "1.0.0";
            apiVersion.expirationDate = Calendar.getInstance();
            apiVersion.expirationDate.setTimeInMillis(System.currentTimeMillis() + 100000);
            version.version = apiVersion;
            return version;
        }
    }

    private class GetVersion extends GetRequest<Version> {

        @Override
        protected void prepareRequest() {
            setUrlSegments("api", "version");
        }
    }
}
