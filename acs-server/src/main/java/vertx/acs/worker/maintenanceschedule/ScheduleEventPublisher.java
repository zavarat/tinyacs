package vertx.acs.worker.maintenanceschedule;

import io.vertx.core.AbstractVerticle;
import vertx.model.MaintenanceSchedule;
import vertx.util.AcsConstants;

/**
 * Project:  cwmp
 *
 * This class is responsible for publishing "maintenance window open"/"maintenance window close " events for each
 * existing maintenance schedules.
 *
 * @author: ronyang
 */
public class ScheduleEventPublisher extends AbstractVerticle {
    /**
     * A local cache of all the Maintenance Schedules.
     */
    ScheduleCache scheduleCache = new ScheduleCache(
            vertx,
            AcsConstants.VERTX_ADDRESS_MAINTENANCE_SCHEDULE,
            MaintenanceSchedule.DB_COLLECTION_NAME,
            MaintenanceSchedule.class.getSimpleName()
    );

    @Override
    public void start() {
        /**
         * Initialize local cache of all schedules
         */
        scheduleCache = new ScheduleCache(
                vertx,
                AcsConstants.VERTX_ADDRESS_MAINTENANCE_SCHEDULE,
                MaintenanceSchedule.DB_COLLECTION_NAME,
                MaintenanceSchedule.class.getSimpleName()
        );
    }
}
