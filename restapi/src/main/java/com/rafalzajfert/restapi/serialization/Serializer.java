package com.rafalzajfert.restapi.serialization;

import android.support.annotation.Nullable;

import com.rafalzajfert.restapi.Parameter;

import java.io.IOException;
import java.util.List;

/**
 * TODO: Documentation
 *
 * @author Rafał Zajfert
 */
public interface Serializer {

    <T> List<Parameter> serialize(T object);

    <T> List<Parameter> serialize(@Nullable String name, T object);
}
