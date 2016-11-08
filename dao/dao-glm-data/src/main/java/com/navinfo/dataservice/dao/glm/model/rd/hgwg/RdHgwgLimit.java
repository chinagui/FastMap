package com.navinfo.dataservice.dao.glm.model.rd.hgwg;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.*;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.collections.map.HashedMap;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 限高限重
 * Created by chaixin on 2016/11/7 0007.
 */
public class RdHgwgLimit implements IObj {

    private int pid;

    private String rowId;

    private int linkPid;

    private int direct;

    private double resHigh;

    private double resWeigh;

    private double resAxleLoad;

    private double resWidth;

    private int meshId;

    private Geometry geometry;

    private Map<String, Object> changedFields = new HashedMap();

    @Override
    public List<IRow> relatedRows() {
        return null;
    }

    @Override
    public int pid() {
        return this.pid;
    }

    @Override
    public String primaryKey() {
        return "pid";
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
    public String rowId() {
        return this.rowId;
    }

    @Override
    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    @Override
    public String tableName() {
        return "RD_HGWG_LIMIT";
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
        return ObjType.RDHGWGLIMIT;
    }

    @Override
    public void copy(IRow row) {
        RdHgwgLimit limit = (RdHgwgLimit) row;
        limit.rowId = this.rowId;
        limit.linkPid = this.linkPid;
        limit.direct = this.direct;
        limit.resHigh = this.resHigh;
        limit.resWeigh = this.resWeigh;
        limit.resAxleLoad = this.resAxleLoad;
        limit.resWidth = this.resWidth;
        limit.meshId = this.meshId;
        limit.geometry = this.geometry;
    }

    @Override
    public Map<String, Object> changedFields() {
        return this.changedFields;
    }

    @Override
    public String parentPKName() {
        return "pid";
    }

    @Override
    public int parentPKValue() {
        return this.pid;
    }

    @Override
    public String parentTableName() {
        return "RD_HGWH_LIMIT";
    }

    @Override
    public List<List<IRow>> children() {
        return null;
    }

    @Override
    public boolean fillChangeFields(JSONObject json) throws Exception {
        @SuppressWarnings("rawtypes")
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
        if (changedFields.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int mesh() {
        return this.meshId;
    }

    @Override
    public void setMesh(int mesh) {
        this.meshId = mesh;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {
        JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());
        return json;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {
        @SuppressWarnings("rawtypes")
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONArray ja = null;
            if (!"objStatus".equals(key)) {
                Field f = this.getClass().getDeclaredField(key);
                f.setAccessible(true);
                f.set(this, json.get(key));
            }
        }
        return true;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getRowId() {
        return rowId;
    }

    public int getLinkPid() {
        return linkPid;
    }

    public void setLinkPid(int linkPid) {
        this.linkPid = linkPid;
    }

    public int getDirect() {
        return direct;
    }

    public void setDirect(int direct) {
        this.direct = direct;
    }

    public double getResHigh() {
        return resHigh;
    }

    public void setResHigh(double resHigh) {
        this.resHigh = resHigh;
    }

    public double getResWeigh() {
        return resWeigh;
    }

    public void setResWeigh(double resWeigh) {
        this.resWeigh = resWeigh;
    }

    public double getResAxleLoad() {
        return resAxleLoad;
    }

    public void setResAxleLoad(double resAxleLoad) {
        this.resAxleLoad = resAxleLoad;
    }

    public double getResWidth() {
        return resWidth;
    }

    public void setResWidth(double resWidth) {
        this.resWidth = resWidth;
    }

    public int getMeshId() {
        return meshId;
    }

    public void setMeshId(int meshId) {
        this.meshId = meshId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}
