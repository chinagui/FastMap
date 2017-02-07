package com.navinfo.dataservice.dao.glm.model.rd.link;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class RdLinkSpeedlimit implements IRow {

    private String rowId;

    public int getSpeedType() {
        return speedType;
    }

    public void setSpeedType(int speedType) {
        this.speedType = speedType;
    }

    public int getFromSpeedLimit() {
        return fromSpeedLimit;
    }

    public void setFromSpeedLimit(int fromSpeedLimit) {
        this.fromSpeedLimit = fromSpeedLimit;
    }

    public int getToSpeedLimit() {
        return toSpeedLimit;
    }

    public void setToSpeedLimit(int toSpeedLimit) {
        this.toSpeedLimit = toSpeedLimit;
    }

    public int getSpeedClass() {
        return speedClass;
    }

    public void setSpeedClass(int speedClass) {
        this.speedClass = speedClass;
    }

    public int getFromLimitSrc() {
        return fromLimitSrc;
    }

    public void setFromLimitSrc(int fromLimitSrc) {
        this.fromLimitSrc = fromLimitSrc;
    }

    public int getToLimitSrc() {
        return toLimitSrc;
    }

    public void setToLimitSrc(int toLimitSrc) {
        this.toLimitSrc = toLimitSrc;
    }

    public int getSpeedDependent() {
        return speedDependent;
    }

    public void setSpeedDependent(int speedDependent) {
        this.speedDependent = speedDependent;
    }

    public int getSpeedClassWork() {
        return speedClassWork;
    }

    public void setSpeedClassWork(int speedClassWork) {
        this.speedClassWork = speedClassWork;
    }

    private int speedType;

    private int fromSpeedLimit;

    private int toSpeedLimit;

    private int speedClass;

    private int fromLimitSrc;

    private int toLimitSrc;

    private int speedDependent;

    private String timeDomain;

    private int speedClassWork = 1;

    private int linkPid;

    protected ObjStatus status;

    private Map<String, Object> changedFields = new HashMap<String, Object>();

    public int getLinkPid() {
        return linkPid;
    }

    public void setLinkPid(int linkPid) {
        this.linkPid = linkPid;
    }

    public RdLinkSpeedlimit() {

    }

    public String getTimeDomain() {
        return timeDomain;
    }

    public void setTimeDomain(String timeDomain) {
        this.timeDomain = timeDomain;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) {

        if (objLevel == ObjLevel.FULL) {
            fromSpeedLimit /= 10;
            toSpeedLimit /= 10;
        }

        return JSONObject.fromObject(this, JsonUtils.getStrConfig());
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {

        Iterator keys = json.keys();

        while (keys.hasNext()) {

            String key = (String) keys.next();

            if (!"objStatus".equals(key)) {

                Field f = this.getClass().getDeclaredField(key);

                f.setAccessible(true);

                if ("fromSpeedLimit".equals(key) || "toSpeedLimit".equals(key)) {
                    int value = json.getInt(key);

                    f.set(this, value * 10);
                } else {
                    f.set(this, json.get(key));
                }
            }

        }
        return true;
    }

    @Override
    public String tableName() {
        return "rd_link_speedlimit";
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
    public ObjType objType() {
        return ObjType.RDLINKSPEEDLIMIT;
    }

    @Override
    public void copy(IRow row) {
        RdLinkSpeedlimit sourceLimit = (RdLinkSpeedlimit) row;

        this.setSpeedType(sourceLimit.getSpeedType());

        this.setFromSpeedLimit(sourceLimit.getFromSpeedLimit());

        this.setToSpeedLimit(sourceLimit.getToSpeedLimit());

        this.setSpeedClass(sourceLimit.getSpeedClass());

        this.setFromLimitSrc(sourceLimit.getFromLimitSrc());

        this.setToLimitSrc(sourceLimit.getToLimitSrc());

        this.setSpeedDependent(sourceLimit.getSpeedDependent());

        this.setTimeDomain(sourceLimit.getTimeDomain());

        this.setSpeedClassWork(sourceLimit.getSpeedClassWork());

        this.setRowId(sourceLimit.getRowId());

        this.setMesh(sourceLimit.mesh());
    }

    @Override
    public Map<String, Object> changedFields() {
        return changedFields;
    }

    @Override
    public String parentPKName() {
        return "link_pid";
    }

    @Override
    public int parentPKValue() {
        return this.getLinkPid();
    }

    @Override
    public List<List<IRow>> children() {
        return null;
    }

    @Override
    public String parentTableName() {
        return "rd_link";
    }

    @Override
    public String rowId() {
        return rowId;
    }

    @Override
    public boolean fillChangeFields(JSONObject json) throws Exception {

        Iterator keys = json.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            if (json.get(key) instanceof JSONArray) {
                continue;
            } else {
                if (!"objStatus".equals(key)) {

                    Field field = this.getClass().getDeclaredField(key);

                    field.setAccessible(true);

                    Object objValue = field.get(this);

                    String oldValue = null;

                    if (objValue == null) {
                        oldValue = "null";
                    } else {
                        oldValue = String.valueOf(objValue);
                    }

                    String newValue = json.getString(key);

                    if (!newValue.equals(oldValue)) {
                        if (key.equals("fromSpeedLimit") || key.equals("toSpeedLimit")) {

                            int limit = json.getInt(key) * 10;

                            newValue = String.valueOf(limit);

                            if (!newValue.equals(oldValue)) {
                                changedFields.put(key, limit);
                            }
                        } else {
                            Object value = json.get(key);

                            if (value instanceof String) {
                                changedFields.put(key, newValue.replace("'", "''"));
                            } else {
                                changedFields.put(key, value);
                            }

                        }
                    }

                }
            }
        }

        if (changedFields.size() > 0) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public int mesh() {
        return 0;
    }

    @Override
    public void setMesh(int mesh) {
    }
}
