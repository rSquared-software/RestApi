package software.rsquared.restapi;

import okhttp3.Credentials;

/**
 * This class store request basic authorization data
 * @author Rafal Zajfert
 */
class BasicAuthorization {

    private String user;

    private String password;

    BasicAuthorization(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Returns an auth credential for the Basic scheme.
     */
    String getBasicAuthorization(){
        return Credentials.basic(user, password);
    }

    /**
     * Returns user name
     */
    String getUser() {
        return user;
    }

    /**
     * Returns password
     */
    String getPassword() {
        return password;
    }
}
