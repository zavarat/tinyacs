package vertx.cache;

import vertx.model.CpeGroup;
import vertx.util.AcsConstants;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * Project:  cwmp
 *
 * An in-memory cache that contains all the groups
 *
 * @author: ronyang
 */
public class GroupCache extends AbstractLocalCache {
    /**
     * Constructor.
     *
     * @param vertx
     * @param crudEventAddress
     * @param dbCollectionName
     * @param cachedObjectType
     */
    public GroupCache(Vertx vertx, String crudEventAddress, String dbCollectionName, String cachedObjectType) {
        super(vertx, crudEventAddress, dbCollectionName, cachedObjectType);
    }

    /**
     * Build actual index String by the raw JSON Object.
     *
     * @param jsonObject
     */
    @Override
    public String getIndexString(JsonObject jsonObject) {
        return jsonObject.getString(AcsConstants.FIELD_NAME_ID);
    }

    /**
     * Get a POJO by JSON Object.
     *
     * @param jsonObject
     */
    @Override
    public Object getPojoByJsonObject(JsonObject jsonObject) throws Exception {
        CpeGroup group = new CpeGroup(jsonObject);

        log.debug("Group: " + jsonObject);
        return group;
    }

    /**
     * Get Group POJO from Hash Map.
     *
     * @param groupId
     * @return
     */
    public CpeGroup getGroupById(String groupId) {
        return (CpeGroup)hashMap.get(groupId);
    }
}
