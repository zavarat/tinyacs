package vertx.util;

import io.vertx.redis.RedisClient;
import vertx.VertxRedisUtils;
import vertx.model.Cpe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Project:  cwmp
 *
 * Util Methods related to CPE Discovery.
 *
 * Mainly around adding to/removing from the CPE Discovery (Redis) Queue.
 *
 * @author: ronyang
 */
public class CpeDiscoveryUtils {
    private static final Logger log = LoggerFactory.getLogger(CpeDiscoveryUtils.class.getName());

    /**
     * Add a new CPE into the queue.
     *
     * @param redisClient
     * @param aNewCpe
     */
    public static void addToQueue(RedisClient redisClient, final JsonObject aNewCpe) {
        VertxRedisUtils.rpush(
                redisClient,
                AcsConstants.REDIS_KEY_CPE_DISCOVERY_QUEUE,
                aNewCpe.encode(),
                new Handler<Long>() {
                    @Override
                    public void handle(Long listLength) {
                        if (listLength > 0) {
                            /**
                             * After device op is stored into Redis, try send a connection-request to
                             * the CPE if needed.
                             */
                            log.info("Successfully added " + aNewCpe.getString(Cpe.DB_FIELD_NAME_SN));
                        } else {
                            log.error("Failed to add " + aNewCpe.getString(Cpe.DB_FIELD_NAME_SN) + "!");
                        }
                    }
                }
        );
    }

    /**
     * Read (and remove) a new CPE from the queue.
     *
     * If read one successfully, call the provided handler.
     *
     * @param redisClient
     * @param handler
     */
    public static void readFromQueue(RedisClient redisClient, Handler<String> handler) {
        VertxRedisUtils.lpop(
                redisClient,
                AcsConstants.REDIS_KEY_CPE_DISCOVERY_QUEUE,
                handler
        );
    }
}
