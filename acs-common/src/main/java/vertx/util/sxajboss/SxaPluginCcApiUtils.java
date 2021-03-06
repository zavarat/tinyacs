package vertx.util.sxajboss;

import vertx.model.Cpe;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

/**
 * Project:  cwmp
 *
 * SXA JBoss Plugin-CC API Utils
 *
 * @author: ronyang
 */
public class SxaPluginCcApiUtils extends SxaJBossApiUtils{
    private static final Logger log = LoggerFactory.getLogger(SxaPluginCcApiUtils.class);

    /**
     * Constants
     */
    public static final String CWMP_PLUGIN_CC_MODULE_NAME = "plugin-cc";
    public static final String CPE_DISCOVERY_URL_PATH = "/cpe-discovery";
    public static final String CPE_DELETE_URL_PATH = "/cpe-delete/";
    public static final String PASSWORD = "cwmp";

    /**
     * Notify SXA Plugin-CC that a new CPE has been discovered.
     * @param cpe
     */
    public static void sendCpeDiscoveryToPluginCc(final Cpe cpe) {
        /**
         * Build request payload
         */
        JsonObject payload = new JsonObject();
        payload.put("id", cpe.getCpeKey());
        payload.put("ip", cpe.deviceId.ipAddress == null? "" : cpe.deviceId.ipAddress);
        payload.put("mac", cpe.deviceId.macAddress == null? "" : cpe.deviceId.macAddress);
        payload.put("ser-no", cpe.deviceId.sn);
        payload.put("model", cpe.deviceId.modelName == null? "" : cpe.deviceId.modelName);
        payload.put("mfr", cpe.deviceId.manufacturer);
        payload.put("sw-ver", cpe.deviceId.swVersion == null? "" : cpe.deviceId.swVersion);
        payload.put("reg-id", cpe.deviceId.registrationId == null? "" : cpe.deviceId.registrationId);
        payload.put("prod-class", cpe.deviceId.productClass == null? "" : cpe.deviceId.productClass);
        payload.put("prov-code", cpe.getParamValue("InternetGatewayDevice.DeviceInfo.ProvisioningCode"));
        payload.put("persistent-data", cpe.getParamValue("InternetGatewayDevice.DeviceConfig.PersistentData"));
        payload.put("last-comm-time", cpe.informTime / 1000);

        // Send it
        sendRequest(
            CWMP_PLUGIN_CC_MODULE_NAME,
            HttpMethod.POST,
            CPE_DISCOVERY_URL_PATH,
            cpe.deviceId.orgId,     // use "orgId" as username
            PASSWORD,
            payload.encode(),
            new Handler<HttpClientResponse>() {
                @Override
                public void handle(HttpClientResponse response) {
                    /**
                     * Simply log the response
                     */
                    if (response.statusCode() == HttpResponseStatus.OK.code() ||
                            response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                        log.info(cpe.getCpeKey() + ": Successfully sent CPE discovery notification to SXA Plugin-CC.");
                    } else {
                        log.error(cpe.getCpeKey() + ": Failed to send CPE discovery notification to SXA Plugin-CC!"
                                + " HTTP Status " + response.statusCode() + " " + response.statusMessage());
                    }
                }
            },
            new Handler<Throwable>() {
                @Override
                public void handle(Throwable exception) {
                    log.error(cpe.getCpeKey() + ": Failed to send CPE discovery notification to SXA Plugin-CC due to"
                            + " exception " + exception.getMessage() + "!");
                }
            });
    }

    /**
     * Delete CPE.
     *
     * @param orgId
     * @param cpeKey
     * @param handler   Custom Handler provided by caller.
     *                  On success, the string result will null;
     *                  On failures, the string result will contain the error details.
     */
    public static void deleteCpe(
            String orgId,
            final String cpeKey,
            final Handler<String> handler) {
        sendRequest(
                CWMP_PLUGIN_CC_MODULE_NAME,
                HttpMethod.DELETE,
                CPE_DELETE_URL_PATH + cpeKey,
                orgId,     // use "orgId" as username
                PASSWORD,
                null,
                new Handler<HttpClientResponse>() {
                    @Override
                    public void handle(HttpClientResponse response) {
                        /**
                         * Simply log the response
                         */
                        if (response.statusCode() == HttpResponseStatus.OK.code() ||
                                response.statusCode() == HttpResponseStatus.NO_CONTENT.code()) {
                            log.info(cpeKey + ": Successfully sent CPE Delete request to SXA Plugin-CC.");
                            handler.handle(null);
                        } else {
                            handler.handle(cpeKey + ": Failed to send CPE Delete request to SXA Plugin-CC!"
                                    + " HTTP Status " + response.statusCode() + " " + response.statusMessage());
                        }
                    }
                },
                new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable exception) {
                        handler.handle(cpeKey + ": Failed to send CPE Delete request to SXA Plugin-CC due to"
                                + " exception " + exception.getMessage() + "!");
                    }
                });
    }
}
