# RestApi

##Gradle Dependency (jCenter)

```Gradle
dependencies {
    compile 'software.rsquared:rest-api:1.1.1'
}
```

[ ![Download](https://api.bintray.com/packages/rafalzajfert/maven/rest-api/images/download.svg) ](https://bintray.com/rafalzajfert/maven/rest-api/_latestVersion)

##Usage

###Configuration

```java
RestApi.setConfiguration(new RestApi.Config()
                .setScheme(RestApi.Config.HTTP)
                .setHost("api.host.com")
                .setPort(80)
                .setInitialRequirements(new InitialRequirements() {

                    //this check will be executed before every request
                    @Override
                    public void onCheckRequirements(Request<?> request) throws InitialRequirementsException {
                        if (!isOnline()){
                            throw new InitialRequirementsException("Offline!");
                        }
                    }
                })
                .setRestAuthorizationService(UserService.getInstance())
                .setTimeout(60000)
                .setLogLevel(Level.VERBOSE)
                .addHeader("lang", "en") //header that will be added to every request
                .setAuthorization("user", "pass123") //basic auth
                .setDeserializer(new JsonDeserializer(new JsonDeserializer.Config()
                        .setTimeInSeconds(true)))
                .setSerializer(new JsonSerializer(new JsonSerializer.Config()
                        .setTimeInSeconds(true)
                        .setIntBoolean(true)
                        .setSerializeNullValues(true)))
                .setErrorDeserializer(new JsonErrorDeserializer(new JsonErrorDeserializer.Config()
                        .setErrorClass(DefaultErrorResponse.class)))
        );
```

###Model
If you use default json serializer/deserializer then model should be created with Jackson annotations e.g.:
```java
@JsonIgnoreProperties(ignoreUnknown = true)
public class Version {
    @JsonProperty("version_code")
    private int code;

    @JsonProperty("version_name")
    private String name;

    ...
}
```

###Requests
Requests should extends one of the class: _**GetRequest, PostRequest, DeleteRequest, PatchRequest, PutRequest**_ e.g.:
```java
public class SetVersion extends PostRequest<BooleanResponse> implements Authorizable {

    private final Version newVersion;

    public SetVersion(Version newVersion){
        this.newVersion = newVersion;
    }

    @Override
    protected void prepareRequest() {
        setUrlSegments("api", "version");

        putUrlParameter("user", "user");
        putParameter("version", newVersion);
    }
}
```

###Execution
```java
RestApi.execute(new SetVersion(version), new RequestListener<BooleanResponse>() {
    @Override
    public void onSuccess(BooleanResponse result) {
        Logger.debug("Success!!");
        Logger.debug(result);
    }

    @Override
    public void onFailed(RequestException e) {
        Logger.error(e);
    }
});
```
or
```java
try {
    Version version = RestApi.executeSync(new GetVersion());
} catch (RequestException e) {
    Logger.error(e);
}
```


####Pool execution
```java
RestApi.pool(RestApi.SERIAL_EXECUTOR) //or RestApi.THREAD_POOL_EXECUTOR
        .add(new GetVersion(), 1)
        .add(new SetVersion(version), 2)
        .execute(new RequestPoolListener() {
            @Override
            public void onTaskSuccess(Object result, int requestCode) {
                //called for every request after sucessfully execution
            }

            @Override
            public void onSuccess(@NonNull Map<Integer, Object> result) {
                //called when all request finished succesfully or onFailed method returns false for failed executions
                //results can be obtained by the requestCodes from the 'result' map
            }

            @Override
            public boolean onFailed(RequestException e, int requestCode) {
                //called for every failed requests. If return true then all unfinished requests will be stopped and onSuccess method will not be invoked
                return false;
            }
        });
```

##Developed By

 * Rafal Zajfert - <rz@rsquared.software>

##License

    Copyright 2017 rSquared s.c. R. Orlik, R. Zajfert

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
