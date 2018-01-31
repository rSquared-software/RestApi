package software.rsquared.restapi.serialization;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import software.rsquared.restapi.Parameter;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface Serializer {

	void serialize(@NonNull List<Parameter> parameters, @NonNull String name, @Nullable Object object);
}
