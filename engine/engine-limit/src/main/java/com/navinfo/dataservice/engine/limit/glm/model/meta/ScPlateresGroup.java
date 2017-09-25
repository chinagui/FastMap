package com.navinfo.dataservice.engine.limit.glm.model.meta;

import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.*;
import com.navinfo.dataservice.commons.util.JsonUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ScPlateresGroup implements IObj {

    private String groupId = "";//GROUP_ID

    private String infoIntelId = "";//INFO_INTEL_ID

    private int adAdmin = 0;//AD_ADMIN

    private String principle = "";//PRINCIPLE

    private int groupType = 1;//GROUP_TYPE

    private String uDate = "";//U_DATE

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getInfoIntelId() {
        return infoIntelId;
    }

    public void setInfoIntelId(String infoIntelId) {
        this.infoIntelId = infoIntelId;
    }

    public int getAdAdmin() {
        return adAdmin;
    }

    public void setAdAdmin(int adAdmin) {
        this.adAdmin = adAdmin;
    }

    public String getPrinciple() {
        return principle;
    }

    public void setPrinciple(String principle) {
        this.principle = principle;
    }

    public int getGroupType() {
        return groupType;
    }

    public void setGroupType(int groupType) {
        this.groupType = groupType;
    }

    public String getuDate() {
        return uDate;
    }

    public void setuDate(String uDate) {
        this.uDate = uDate;
    }

    protected ObjStatus status;

    private Map<String, Object> changedFields = new HashMap<>();

    @Override
    public List<IRow> relatedRows() {
        return null;
    }

    @Override
    public String primaryKeyValue() {
        return groupId;
    }

    @Override
    public String primaryKey() {
        return "GROUP_ID";
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
        return "SC_PLATERES_GROUP";
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
        return LimitObjType.SCPLATERESGROUP;
    }

    @Override
    public Map<String, Object> changedFields() {
        return changedFields;
    }

    @Override
    public String parentPKName() {
        return "GROUP_ID";
    }

    @Override
    public String parentPKValue() {
        return null;
    }

    @Override
    public String parentTableName() {
        return "SC_PLATERES_GROUP";
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
