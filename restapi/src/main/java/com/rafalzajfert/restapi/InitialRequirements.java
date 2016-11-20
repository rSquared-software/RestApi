package com.rafalzajfert.restapi;

import com.rafalzajfert.restapi.exceptions.InitialRequirementsException;

/**
 * @author Rafal Zajfert
 */
@SuppressWarnings("WeakerAccess")
public interface InitialRequirements {
    void onCheckRequirements(Request<?> request) throws InitialRequirementsException;
}
