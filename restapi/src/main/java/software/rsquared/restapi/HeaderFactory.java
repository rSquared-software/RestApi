package software.rsquared.restapi;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rafalo on 18.01.2018.
 */
public interface HeaderFactory {
	default Map<String, String> getHeaders(Request request){
		return new HashMap<>();
	}
}
