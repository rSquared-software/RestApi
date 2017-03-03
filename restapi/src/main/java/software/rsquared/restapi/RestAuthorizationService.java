package software.rsquared.restapi;

/**
 * @author Rafal Zajfert
 */
@SuppressWarnings("WeakerAccess")
public interface RestAuthorizationService {
    boolean isLogged();

    boolean isTokenValid();

    void refreshToken();

    void logout();

    Authorization getAuthorization();
}
