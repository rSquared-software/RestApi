package software.rsquared.restapi;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.SocketTimeoutException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import software.rsquared.restapi.exceptions.AccessTokenException;
import software.rsquared.restapi.exceptions.RequestException;
import software.rsquared.restapi.listeners.ErrorCallback;
import software.rsquared.restapi.listeners.RequestListener;

/**
 * A cancellable asynchronous computation. This implementation converts all Exception to the {@link RequestException} and allows listen to the end of execution
 *
 * @author Rafał Zajfert
 * @see RequestFuture
 * @see FutureTask
 */
public class RequestFutureTask<T> extends FutureTask<T> implements RequestFuture<T> {

	@Nullable
	private static Handler handler;
	private final ErrorCallback errorCallback;
	@Nullable
	private RequestListener<T> listener;


	@Deprecated
	public RequestFutureTask(@NonNull Callable<T> callable) {
		super(callable);
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public RequestFutureTask(@NonNull Runnable runnable, T result) {
		super(runnable, result);
		throw new UnsupportedOperationException();
	}

	/**
	 * Creates a FutureTask that will, upon running, execute the given Callable.
	 *
	 * @param callable the callable task
	 */
	public RequestFutureTask(@NonNull Callable<T> callable, @Nullable ErrorCallback errorCallback) {
		super(callable);
		this.errorCallback = errorCallback;
	}

	/**
	 * Creates a FutureTask that will, upon running, execute the given Callable.
	 *
	 * @param callable the callable task
	 * @param listener the listener that will be called when execution finished
	 */
	public RequestFutureTask(@NonNull Callable<T> callable, ErrorCallback errorCallback, @Nullable RequestListener<T> listener) {
		super(callable);
		this.errorCallback = errorCallback;
		this.listener = listener;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get() throws RequestException {
		try {
			return super.get();
		} catch (ExecutionException | AccessTokenException | InterruptedException e) {
			RequestException exception = parseException(e);
			if (errorCallback != null) {
				errorCallback.onError(exception);
			}
			throw exception;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T get(long timeout, @NonNull TimeUnit unit) throws RequestException {
		try {
			return super.get(timeout, unit);
		} catch (ExecutionException | AccessTokenException | InterruptedException | TimeoutException e) {
			RequestException exception = parseException(e);
			if (errorCallback != null) {
				errorCallback.onError(exception);
			}
			throw exception;
		}
	}

	@Override
	public void run() {
		if (listener != null) {
			getHandler().post(() -> listener.onPreExecute());
		}
		super.run();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void done() {
		if (listener != null) {
			getHandler().post(() -> {
				if (RequestFutureTask.this.isCancelled()) {
					listener.onCanceled();
				} else {
					try {
						listener.onSuccess(get());
					} catch (RequestException e) {
						listener.onFailed(e);
					}
				}
				listener.onPostExecute();
			});
		}
		super.done();
	}

	/**
	 * Convert all type of the exception to {@link RuntimeException} instance.
	 * <p>
	 * <b>Note:</b> {@link RuntimeException} will be thrown immediately.
	 *
	 * @param e instance of exception that should be wrapped
	 */
	@NonNull
	private RequestException parseException(Exception e) {
		Throwable cause = e.getCause();
		if (cause != null) {
			if (cause instanceof RequestException) {
				return (RequestException) cause;
			} else if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof SocketTimeoutException || cause instanceof TimeoutException) {
				return new RequestException((Exception) cause);
			} else {
				Throwable subCause = cause.getCause();
				if (subCause != null) {
					if (subCause instanceof SocketTimeoutException || subCause instanceof TimeoutException) {
						return new RequestException((Exception) subCause);
					}
				}

			}
		}
		return new RequestException(e);
	}

	/**
	 * Get handler for the main looper
	 */
	@NonNull
	private Handler getHandler() {
		if (handler == null) {
			handler = new Handler(Looper.getMainLooper());
		}
		return handler;
	}

}
