package software.rsquared.restapi.serialization;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import software.rsquared.restapi.Parameter;

import java.util.List;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface Serializer {

    <T> void serialize(@NonNull List<Parameter> parameters, @Nullable T object);

    <T> void serialize(@NonNull List<Parameter> parameters, @NonNull String name, @Nullable T object);
}
