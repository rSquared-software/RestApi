package software.rsquared.restapi;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.RequestListener;
import software.rsquared.restapi.listeners.SyncRequestListener;
import software.rsquared.restapi.serialization.Deserializer;
import software.rsquared.restapi.serialization.ErrorDeserializer;
import software.rsquared.restapi.serialization.JsonDeserializer;
import software.rsquared.restapi.serialization.JsonErrorDeserializer;
import software.rsquared.restapi.serialization.ObjectToFormSerializer;
import software.rsquared.restapi.serialization.Serializer;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public class RestApi {

	private long timeout = 60 * 1000;
	private String scheme;
	private String host;
	private int port = -1;

	@Nullable
	private BasicAuthorization basicAuthorization;

	@NonNull
	private ErrorDeserializer errorDeserializer = new JsonErrorDeserializer();

	@NonNull
	private Deserializer deserializer = new JsonDeserializer();

	@NonNull
	private Serializer serializer = new ObjectToFormSerializer();

	@Nullable
	private MockFactory mockFactory;

	@Nullable
	private HeaderFactory headerFactory;

	private boolean enableTls12OnPreLollipop = false;

	private final ArrayList<Checker> checkerArrayList = new ArrayList<>();

//	private Executor networkExecutor;

//	private Executor uiExecutor;

	private RestApi() {
	}

	public static class Builder {

		private final RestApi restApi;

		public Builder() {
			restApi = new RestApi();
		}

		public RestApi build() {
			return restApi;
		}

		public Builder setUrl(@NonNull String url) {
			return setUrl(Uri.parse(url));
		}

		public Builder setUrl(@NonNull Uri uri) {
			restApi.scheme = uri.getScheme();
			restApi.host = uri.getHost();
			restApi.port = uri.getPort();
			return this;
		}

		public Builder setBasicAuthorization(BasicAuthorization basicAuth) {
			restApi.basicAuthorization = basicAuth;
			return this;
		}

		public Builder setSerializer(Serializer serializer) {
			restApi.serializer = serializer;
			return this;
		}

		public Builder setDeserializer(Deserializer deserializer) {
			restApi.deserializer = deserializer;
			return this;
		}

		public Builder setErrorDeserializer(ErrorDeserializer errorDeserializer) {
			restApi.errorDeserializer = errorDeserializer;
			return this;
		}

		public Builder setTimeout(long millis) {
			restApi.timeout = millis;
			return this;
		}

		public Builder enableTls12OnPreLollipop(boolean enable) {
			restApi.enableTls12OnPreLollipop = enable;
			return this;
		}

		public Builder addChecker(@NonNull Checker checker) {
			restApi.checkerArrayList.add(checker);
			return this;
		}

	}

	public long getTimeout() {
		return timeout;
	}

	public String getScheme() {
		return scheme;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Nullable
	public BasicAuthorization getBasicAuthorization() {
		return basicAuthorization;
	}

	@NonNull
	public ErrorDeserializer getErrorDeserializer() {
		return errorDeserializer;
	}

	@NonNull
	public Deserializer getDeserializer() {
		return deserializer;
	}

	@NonNull
	public Serializer getSerializer() {
		return serializer;
	}

	@Nullable
	public MockFactory getMockFactory() {
		return mockFactory;
	}

	public boolean isEnableTls12OnPreLollipop() {
		return enableTls12OnPreLollipop;
	}

	@NonNull
	public ArrayList<Checker> getCheckerArrayList() {
		return checkerArrayList;
	}

	@Deprecated //moze trzeba przepisać
	public static <E> void execute(@NonNull Request<E> request, @Nullable RequestListener<E> listener) {
		request.execute(listener);
	}

	@Deprecated //moze trzeba przepisać
	public static <E> E executeSync(@NonNull Request<E> request) throws RequestException {
		return request.execute().get();
	}

	@Nullable
	@Deprecated //moze trzeba przepisać
	public static <E> E executeSync(@NonNull Request<E> request, @Nullable SyncRequestListener<E> listener) {
		try {
			return request.execute(listener).get();
		} catch (RequestException e) {
			//exceptions should be caught in listener
			return null;
		}
	}

}
