package software.rsquared.restapi;

import software.rsquared.restapi.exceptions.InitialRequirementsException;

/**
 * @author Rafal Zajfert
 */
@SuppressWarnings("WeakerAccess")
public interface InitialRequirements {
	void onCheckRequirements(Request<?> request) throws InitialRequirementsException;
}
