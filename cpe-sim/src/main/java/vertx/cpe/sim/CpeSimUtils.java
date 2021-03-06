package vertx.cpe.sim;

import io.vertx.ext.mongo.MongoClient;
import vertx.VertxConstants;
import vertx.VertxMongoUtils;
import vertx.VertxUtils;
import vertx.model.Cpe;
import vertx.util.AcsConstants;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.http.auth.AUTH;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;

import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

/**
 * Project:  cwmp-parent
 *
 * Profile Management Util Methods.
 *
 * @author: ronyang
 */
public class CpeSimUtils {
    private static final Logger log = LoggerFactory.getLogger(CpeSimUtils.class.getName());

    /**
     * A JSON that contains the default CPE meta data
     */
    public static final String DEFAULT_CPE_DATA_FILE = "cpeDefault.json";

    /**
     * Default CPE Meta Data
     */
    public static JsonObject DEFAULT_CPE_DATA = null;

    /**
     * Shared Session Map
     */
    public static Set<String> allSessions = new HashSet<String>();

    /**
     * Get Default CPE Meta Data from resource file.
     */
    public static void initDefaultCpeData(Vertx vertx) {
        vertx.fileSystem().readFile(
                DEFAULT_CPE_DATA_FILE,
                new Handler<AsyncResult<Buffer>>() {
                    public void handle(AsyncResult<Buffer> ar) {
                        if (ar.succeeded()) {
                            try {
                                DEFAULT_CPE_DATA = new JsonObject(ar.result().toString());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            log.error(VertxUtils.highlightWithHashes(
                                    "File " + DEFAULT_CPE_DATA_FILE + " does not exist!" + " (" + ar.cause() + ")"));
                        }
                    }
                });

        // Init shared session set
//        vertx.sharedData().getLocalMap("map").put(CpeSimConstants.SHARED_SESSION_SET,new HashSet<String>());
//        allSessions = (Set<String>)vertx.sharedData().getLocalMap("map").get(CpeSimConstants.SHARED_SESSION_SET);
    }

    /**
     * Get the connection request URL for a CPE.
     *
     * @param cpeKey
     * @return
     */
    public static String getConnReqUrl(String cpeKey) {
        return "http://" + VertxUtils.getLocalHostname() + ":" + CpeSimConstants.HTTP_SERVICE_REQ_PORT + "/connreq/"
                + cpeKey.replace("-", "/");
    }

    /**
     * get HTTP Basic Auth String
     */
    private static String basicAuthString = null;
    public static String getBasicAuthString() {
        if (basicAuthString == null) {
            String authString = CpeSimConstants.ACS_USERNAME + ":" + CpeSimConstants.ACS_PASSWORD;
            basicAuthString = "Basic " + Base64.getEncoder().encodeToString(authString.getBytes());
        }

        return basicAuthString;
    }

    /**
     * Build a JSON Object for a given SN (and CPE key).
     *
     * @param sn
     * @param orgId
     * @param oui
     * @return
     */
    public static JsonObject getDefaultCpeDataObjectBySn(long sn, String orgId, String oui) {
        /**
         * Query Failed! Use default CPE Meta Data
         */
        JsonObject cpe = CpeSimUtils.DEFAULT_CPE_DATA.copy();

        /**
         * Overwrite IP and MAC Addresses
         */
        cpe.put(Cpe.DB_FIELD_NAME_IP_ADDRESS, CpeSimUtils.snToIpAddress(sn));
        cpe.put(Cpe.DB_FIELD_NAME_MAC_ADDRESS, CpeSimUtils.snToMacAddress(sn));

        /**
         * Overwrite the CPE Key/SN/Connection Request URL
         */
        String cpeKey = Cpe.getCpeKey(orgId, oui, CpeSimUtils.snToHexString(sn));
        cpe.put("_id", cpeKey);
        cpe.put(Cpe.DB_FIELD_NAME_SN, CpeSimUtils.snToHexString(sn));
        cpe.put(Cpe.DB_FIELD_NAME_CONNREQ_URL, CpeSimUtils.getConnReqUrl(cpeKey));

        JsonObject igd = cpe.getJsonObject(Cpe.DB_FIELD_NAME_PARAM_VALUES)
                .getJsonObject(Cpe.INTERNET_GATEWAY_DEVICE_ROOT);

        igd.getJsonObject("ManagementServer")
                .put("URL", CpeSimConstants.ACS_URL)
                .put("ConnectionRequestURL", CpeSimUtils.getConnReqUrl(cpeKey));

        igd.getJsonObject("DeviceInfo")
                .put("SerialNumber", CpeSimUtils.snToHexString(sn));

        return cpe;
    }

    /**
     * Build a Simple JSON Object for a given SN (and CPE key).
     *
     * Used when populating the DB.
     *
     * @param sn
     * @param orgId
     * @param oui
     * @return
     */
    public static JsonObject getSimpleCpeDataObjectBySn(long sn, String orgId, String oui) {
        /**
         * Query Failed! Use default CPE Meta Data
         */
        JsonObject cpe = CpeSimUtils.DEFAULT_CPE_DATA.copy();

        /**
         * Overwrite IP and MAC Addresses
         */
        cpe.put(Cpe.DB_FIELD_NAME_IP_ADDRESS, CpeSimUtils.snToIpAddress(sn));
        cpe.put(Cpe.DB_FIELD_NAME_MAC_ADDRESS, CpeSimUtils.snToMacAddress(sn));

        /**
         * Overwrite the CPE Key/SN/Connection Request URL
         */
        String cpeKey = Cpe.getCpeKey(orgId, oui, CpeSimUtils.snToHexString(sn));
        cpe.put("_id", cpeKey);
        cpe.put(Cpe.DB_FIELD_NAME_SN, CpeSimUtils.snToHexString(sn));
        cpe.put(Cpe.DB_FIELD_NAME_CONNREQ_URL, CpeSimUtils.getConnReqUrl(cpeKey));

        return cpe;
    }

    /**
     * A friendly wrapper to send async HTTP request.
     *
     * @param url           The URL String without the hostname and port #
     * @param httpClient
     * @param httpMethod    HTTP Method (GET/PUT/POST/DELETE)
     * @param cookie
     * @param authResponse
     * @param payload
     */
    public static void sendHttpRequest(
            String url,
            HttpClient  httpClient,
            HttpMethod httpMethod,
            String cookie,
            String authResponse,
            String payload,
            Handler<HttpClientResponse> handler) {
//        log.info("Sending payload to " + getUrlFromHttpClient(httpClient) + url + ":\n" + payload);

        // Build the request
        HttpClientRequest request = httpClient.request((io.vertx.core.http.HttpMethod.valueOf(httpMethod.name())), url, handler);

        // Content Type
        request.headers().set("Content-Type", "text/xml");

        // Auth Response
        if (authResponse != null) {
            request.headers().set(AUTH.WWW_AUTH_RESP, authResponse);
        }

        // Cookie(s)
        if (cookie != null) {
            log.info("Cookie:" + cookie);
            request.headers().set("Cookie", cookie);
        } else {
            // Add HTTP Basic Auth Header if no cookie
            //request.headers().set("Authorization", getBasicAuthString());
        }

        // Payload
        if (payload != null) {
            request.headers().set("Content-Length", String.valueOf(payload.length()));
            request.write(payload);
        } else {
            request.headers().set("Content-Length", "0");
        }

        request.end();
    }

    /**
     * Get the [host:port] from HTTP Client.
     *
     * @param client
     * @return
     */
//    public static String getUrlFromHttpClient(HttpClient client) {
//        return "http://" + client.getHost() + ":" + client.getPort();
//    }

    /**
     * Find a single CPE by CPE Key/Id
     */
    public static void findCpeById(MongoClient mongoClient, String cpeKey, Handler<JsonObject> handler) {
        try{
            VertxMongoUtils.findOne(mongoClient,CpeSimConstants.MONGO_CPE_SIM__COLLECTION,new JsonObject().put("_id", cpeKey),handler,null);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Update a single CPE by CPE Key/Id
     */
    public static void updateCpeById(
            MongoClient mongoClient,
            String cpeKey,
            JsonObject updates,
            Handler handler) {

        /**
         * Convert update object
         */
        JsonObject newUpdates = new JsonObject();
        JsonObject newSets = new JsonObject();
        JsonObject sets = updates.getJsonObject("$set");
        if (sets != null) {
            for (String fieldName : sets.fieldNames()) {
                String value = sets.getValue(fieldName).toString();
                newSets.put(fieldName + "._value", value);
            }

            newUpdates.put("$set", newSets);
        }
        if (updates.containsKey("$unset")) {
            newUpdates.put("$unset", updates.getJsonObject("$unset"));
        }
        if (updates.containsKey("$currentDate")) {
            newUpdates.put("$currentDate", updates.getJsonObject("$currentDate"));
        }

        try{
            VertxMongoUtils.updateWithMatcher(mongoClient,CpeSimConstants.MONGO_CPE_SIM__COLLECTION,new JsonObject().put("_id", cpeKey),newUpdates,handler);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Persist CPE Meta Data.
     *
     * @param mongoClient
     * @param cpe
     */
    public static void persistCpe(MongoClient mongoClient, String id, JsonObject cpe, boolean bIsNew) {
        log.info("Saving CPE...\n" + cpe.encodePrettily());
        try {
            if (bIsNew) {
                VertxMongoUtils.save(
                        mongoClient,
                        CpeSimConstants.MONGO_CPE_SIM__COLLECTION,
                        cpe,
                        null
                );
            } else {
                VertxMongoUtils.updateWithMatcher(
                        mongoClient,
                        CpeSimConstants.MONGO_CPE_SIM__COLLECTION,
                        new JsonObject().put(AcsConstants.FIELD_NAME_ID, id),
                        cpe,
                        null
                );
            }
        } catch (Exception ex) {

        }
    }

    /**
     * Get the max object number.
     *
     * @param deviceModelName
     * @param objectPath
     * @return
     */
    public static int getMaxInstanceIndex(String deviceModelName, String objectPath) {
        if (deviceModelName.equals("844RG")) {
            if (objectPath.endsWith("WLANConfiguration.")) {
                // Up to 16 WIFI instances
                return 16;
            }
            if (objectPath.endsWith("LANDevice.")) {
                // Up to 16 LANDevice instances
                return 16;
            }

            return 16;
        }

        //log.error("Unsupported device model " + deviceModelName + "!");
        return 16;
    }

    /**
     * Convert CPE Sn (long value) to HEX String
     * @param sn
     * @return
     */
    public static String snToHexString(long sn) {
        return String.format("%012X", sn);
    }

    /**
     * Convert CPE Sn (long value) to IP Address String
     * @param sn
     * @return
     */
    public static String snToIpAddress(long sn) {
        int ip = (int)(sn & 0xFFFFFFFF);
        int byte3 = (ip >> 24) & 0xFF;
        int byte2 = (ip >> 16) & 0xFF;
        int byte1 = (ip >> 8) & 0xFF;
        int byte0 = ip & 0xFF;

        return byte3 + "." + byte2 + "." + byte1 + "." + byte0;
    }

    /**
     * Convert CPE Sn (long value) to Subnet Mask String
     * @param sn
     * @return
     */
    public static String snToSubnetMask(long sn) {
        int ip = (int)(sn & 0xFFFFFFFF);
        int byte3 = (ip >> 24) & 0xFF;
        int byte2 = (ip >> 16) & 0xFF;
        int byte1 = (ip >> 8) & 0xFF;

        return byte3 + "." + byte2 + "." + byte1 + ".0";
    }

    /**
     * Convert CPE Sn (long value) to MAC Address String
     * @param sn
     * @return
     */
    public static String snToMacAddress(long sn) {
        long byte5 = (sn >> 40) & 0xFF;
        long byte4 = (sn >> 32) & 0xFF;
        long byte3 = (sn >> 24) & 0xFF;
        long byte2 = (sn >> 16) & 0xFF;
        long byte1 = (sn >> 8) & 0xFF;
        long byte0 = sn & 0xFF;

        return byte5 + ":" + byte4 + ":" + byte3 + ":" + byte2 + ":" + byte1 + ":" + byte0;
    }
    /**
     * Get CPE Up Time (in seconds).
     *
     * @return
     */
    public static long getUpTime() {
        return (System.currentTimeMillis() - CpeSimMainVertice.startTime) / 1000;
    }
}
