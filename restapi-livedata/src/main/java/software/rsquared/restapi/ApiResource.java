package software.rsquared.restapi;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import software.rsquared.restapi.exceptions.RequestException;

import static software.rsquared.restapi.ApiResource.Status.CANCELED;
import static software.rsquared.restapi.ApiResource.Status.FAILED;
import static software.rsquared.restapi.ApiResource.Status.LOADING;
import static software.rsquared.restapi.ApiResource.Status.SUCCESS;

/**
 * @author Rafał Zajfert
 */
public class ApiResource<T> {
	@NonNull
	public final Status status;

	@Nullable
	public final RequestException exception;

	@Nullable
	public final T data;

	public ApiResource(@NonNull Status status, @Nullable T data, @Nullable RequestException exception) {
		this.status = status;
		this.data = data;
		this.exception = exception;
	}

	public static <T> ApiResource<T> success(@Nullable T data) {
		return new ApiResource<>(SUCCESS, data, null);
	}

	public static <T> ApiResource<T> fail(RequestException exception) {
		return new ApiResource<>(FAILED, null, exception);
	}

	public static <T> ApiResource<T> loading() {
		return new ApiResource<>(LOADING, null, null);
	}

	public static <T> ApiResource<T> cancel() {
		return new ApiResource<>(CANCELED, null, null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		ApiResource<?> resource = (ApiResource<?>) o;

		if (status != resource.status) {
			return false;
		}
		if (exception != null ? !exception.equals(resource.exception) : resource.exception != null) {
			return false;
		}
		return data != null ? data.equals(resource.data) : resource.data == null;
	}

	@Override
	public int hashCode() {
		int result = status.hashCode();
		result = 31 * result + (exception != null ? exception.hashCode() : 0);
		result = 31 * result + (data != null ? data.hashCode() : 0);
		return result;
	}

	public enum Status {
		SUCCESS,
		FAILED,
		CANCELED,
		LOADING
	}
}
