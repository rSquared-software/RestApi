package software.rsquared.restapi;

import okhttp3.Credentials;

/**
 * This class store request basic authorization data
 * @author Rafal Zajfert
 */
class BasicAuthorization {

    private String mUser;

    private String mPassword;

    BasicAuthorization(String user, String password) {
        mUser = user;
        mPassword = password;
    }

    /**
     * Returns an auth credential for the Basic scheme.
     */
    String getBasicAuthorization(){
        return Credentials.basic(mUser, mPassword);
    }

    /**
     * Returns user name
     */
    String getUser() {
        return mUser;
    }

    /**
     * Returns password
     */
    String getPassword() {
        return mPassword;
    }
}
