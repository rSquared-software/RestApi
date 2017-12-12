package software.rsquared.restapi;

import android.support.annotation.NonNull;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import software.rsquared.restapi.exceptions.RequestException;

/**
 * A Future represents the result of an asynchronous computation. This interface wrap default Exception to {@link}
 *
 * @author Rafał Zajfert
 * @see Future
 */
interface RequestFuture<V> extends Future<V> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	V get() throws RequestException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	V get(long timeout, @NonNull TimeUnit unit) throws RequestException;

}
