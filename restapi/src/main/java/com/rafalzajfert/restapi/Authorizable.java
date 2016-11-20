package com.rafalzajfert.restapi;

/**
 * If Request implements this interface then to the rest address will be added {@link Request#ACCESS_TOKEN} parameter from {@link RestAuthorizationService}
 *
 * @author Rafał Zajfert
 */
@SuppressWarnings("WeakerAccess")
public interface Authorizable {
}
