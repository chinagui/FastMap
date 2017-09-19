package com.navinfo.dataservice.engine.limit.glm.model.mate;

import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.ObjLevel;
import com.navinfo.dataservice.engine.limit.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.ObjType;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by ly on 2017/9/18.
 */
public class ScPlateresRdLink  implements IRow{

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
    public ObjType objType() {
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
        return false;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {
        return null;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {
        return false;
    }
}
