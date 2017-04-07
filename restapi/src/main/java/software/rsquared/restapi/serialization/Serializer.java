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

    <T> List<Parameter> serialize(@Nullable T object);

    <T> List<Parameter> serialize(@NonNull String name, @Nullable T object);
}
