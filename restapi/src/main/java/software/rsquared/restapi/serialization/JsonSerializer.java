package software.rsquared.restapi.serialization;

import android.support.annotation.NonNull;

import java.util.List;

import software.rsquared.restapi.Parameter;

/**
 * @author Rafal Zajfert
 */
public interface JsonSerializer extends Serializer {
	String toJsonString(@NonNull List<Parameter> parameters);
}
