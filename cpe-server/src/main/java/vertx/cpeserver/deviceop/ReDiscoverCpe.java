package vertx.cpeserver.deviceop;

import vertx.cpeserver.session.CwmpSession;
import vertx.cwmp.CwmpException;
import vertx.model.Cpe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Project:  cwmp
 *
 * Re-discover known CPE.
 *
 * Usually triggered by Informs with "0 BOOTSTRAP" event code.
 *
 * @author: ronyang
 */
public class ReDiscoverCpe {
    private static final Logger log = LoggerFactory.getLogger(ReDiscoverCpe.class.getName());

    /**
     * Start a CPE Re-Discovery Process.
     * @param session
     */
    public static void start(CwmpSession session) {
        log.info("Starting Re-Discovery process for CPE " + session.cpeKey);

        // Mark the boolean flags
        session.cpe.bNeedDiscovery = true;
        session.cpe.bDiscoveryDone = true;

        try {
            // Re-push the initial provisioning
            SetParameterValuesNbi.startNbiProvisioning(
                    session,
                    session.cpe.cpeJsonObj.getJsonObject(Cpe.DB_FIELD_NAME_INITIAL_PROVISIONING)
            );

            // Overwrite Connection Request Username/Password, and also enable notifications
            ManagementServerBootstrap.start(session);

            /**
             * Read the Calix ONT Registration ID (i.e. "RONTA") if any
             */
            GetRegistrationId.start(session);

            /**
             * Try to enable passive notification on change counter
             */
            EnableNotifOnChangeCounter.start(session);
        } catch (CwmpException e) {
            e.printStackTrace();
        }
    }
}
