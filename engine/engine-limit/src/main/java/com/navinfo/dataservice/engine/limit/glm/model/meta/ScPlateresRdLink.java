package com.navinfo.dataservice.engine.limit.glm.model.meta;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IObj;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScPlateresRdLink implements IObj {

    private int linkPid;   //LINK_PID
    private int linkDir;//            LIMIT_DIR
    private String geometryId = "";//    GEOMETRY_ID
    private Geometry geometryRdLink;//    GEOMETRY_RDLINK

    public int getLinkPid() {
        return linkPid;
    }

    public void setLinkPid(int linkPid) {
        this.linkPid = linkPid;
    }

    public int getLinkDir() {
        return linkDir;
    }

    public void setLinkDir(int linkDir) {
        this.linkDir = linkDir;
    }

    public String getGeometryId() {
        return geometryId;
    }

    public void setGeometryId(String geometryId) {
        this.geometryId = geometryId;
    }

    public Geometry getGeometryRdLink() {
        return geometryRdLink;
    }

    public void setGeometryRdLink(Geometry geometryRdLink) {
        this.geometryRdLink = geometryRdLink;
    }

    protected ObjStatus status;

    private Map<String, Object> changedFields = new HashMap<>();


    @Override
    public List<IRow> relatedRows() {
        return null;
    }

    @Override
    public String primaryKeyValue() {
        return String.valueOf(linkPid);
    }

    @Override
    public String primaryKey() {
        return "LINK_PID";
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        return null;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        return null;
    }


    @Override
    public String tableName() {
        return "SC_PLATERES_RDLINK";
    }

    @Override
    public ObjStatus status() {

        return status;
    }

    @Override
    public void setStatus(ObjStatus os) {
        status = os;
    }

    @Override
    public LimitObjType objType() {
        return LimitObjType.SCPLATERESRDLINK;
    }

    @Override
    public Map<String, Object> changedFields() {
        return changedFields;
    }

    @Override
    public String parentPKName() {
        return "GEOMETRY_ID";
    }

    @Override
    public String parentPKValue() {
        return null;
    }

    @Override
    public String parentTableName() {
        return "SC_PLATERES_GEOMETRY";
    }

    @Override
    public List<List<IRow>> children() {
        return null;
    }

    @Override
    public boolean fillChangeFields(JSONObject json) throws Exception {
        Iterator keys = json.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            if (json.get(key) instanceof JSONArray) {
                continue;
            }
            if (!"objStatus".equals(key)) {

                Field field = this.getClass().getDeclaredField(key);

                field.setAccessible(true);

                Object objValue = field.get(this);

                String oldValue;

                if (objValue == null) {
                    oldValue = "null";
                } else {
                    oldValue = String.valueOf(objValue);
                }

                String newValue = json.getString(key);

                if (!newValue.equals(oldValue)) {
                    Object value = json.get(key);

                    if (value instanceof String) {
                        changedFields.put(key, newValue.replace("'", "''"));
                    } else {
                        changedFields.put(key, value);
                    }
                }
            }
        }

        return changedFields.size() > 0;
    }


    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {
        JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());
        if (objLevel == ObjLevel.HISTORY) {
            json.remove("status");
        }
        return json;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {
        Iterator keys = json.keys();

        while (keys.hasNext()) {

            String key = (String) keys.next();

            if (!"objStatus".equals(key)) {

                Field f = this.getClass().getDeclaredField(key);

                f.setAccessible(true);

                f.set(this, json.get(key));
            }

        }
        return true;
    }

}
