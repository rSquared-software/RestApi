package software.rsquared.restapi;

import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @see TypeReference
 */
class RequestTypeReference<T> extends TypeReference<T> {
	private Type requestType;

	RequestTypeReference(Request<T> request) {
		Type superClass = request.getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) { // sanity check, should never happen
			throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
		}
		requestType = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	@Override
	public Type getType() {
		return requestType;
	}
}
