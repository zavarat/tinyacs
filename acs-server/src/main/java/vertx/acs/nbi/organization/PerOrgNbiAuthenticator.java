package vertx.acs.nbi.organization;

import vertx.model.Organization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.util.Base64;

/**
 * Project:  cwmp
 *
 * Abstract Per-Organization Authenticator that is used to authenticate NBI client requests.
 *
 * @author: ronyang
 */
public class PerOrgNbiAuthenticator extends Organization{
    private static final Logger log = LoggerFactory.getLogger(PerOrgNbiAuthenticator.class.getName());

    public String basicAuthString;

    /**
     * Constructor by a JSON Object.
     *
     * @param jsonObject
     */
    public PerOrgNbiAuthenticator(JsonObject jsonObject) {
        super(jsonObject);
        basicAuthString = getBasicAuthString(jsonObject);
        log.info("Org " + name + " (id:" + id + "), Auth String: " + basicAuthString);
    }

    /**
     * Static method to get Basic Auth String from raw Organization JSON Object.
     *
     * @param jsonObject
     */
    public static String getBasicAuthString(JsonObject jsonObject) {
        String authString = jsonObject.getString(FIELD_NAME_API_CLIENT_USERNAME)
                + ":" + jsonObject.getString(FIELD_NAME_API_CLIENT_PASSWORD);
        return "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
    }
}
