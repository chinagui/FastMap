package com.navinfo.dataservice.engine.limit.glm.model.mate;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ScPlateresManoeuvre implements IRow {


    private Map<String, Object> changedFields = new HashMap<>();


    @Override
    public String tableName() {
        return null;
    }

    @Override
    public ObjStatus status() {
        return null;
    }

    @Override
    public void setStatus(ObjStatus os) {

    }

    @Override
    public LimitObjType objType() {
        return null;
    }

    @Override
    public Map<String, Object> changedFields() {
        return null;
    }

    @Override
    public String parentPKName() {
        return null;
    }

    @Override
    public int parentPKValue() {
        return 0;
    }

    @Override
    public String parentTableName() {
        return null;
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
            } else {
                if (!"objStatus".equals(key)) {

                    Field field = this.getClass().getDeclaredField(key);

                    field.setAccessible(true);

                    Object objValue = field.get(this);

                    String oldValue ;

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
