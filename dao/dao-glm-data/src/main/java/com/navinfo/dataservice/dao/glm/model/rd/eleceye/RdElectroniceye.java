package com.navinfo.dataservice.dao.glm.model.rd.eleceye;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.json.DateJsonValueProcessor;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/**
 * @author zhangyt
 * @Title: RdElectroniceye.java
 * @Prject: dao-glm-data
 * @Package: com.navinfo.dataservice.dao.glm.model.rd.eleceye
 * @Description: 电子眼表
 * @date: 2016年7月20日 下午5:38:58
 * @version: v1.0
 */
public class RdElectroniceye implements IObj {

    private int pid;

    private String rowId;

    private int linkPid;

    private int direct;

    private int kind;

    private int location;

    private double angle;

    private int speedLimit;

    private int verifiedFlag;

    private int meshId;

    private Geometry geometry;

    private String srcFlag = "1";

    private Date creationDate;

    private int highViolation;
    
    protected ObjStatus eyeStatus;
    
    private Map<String, Object> changedFields = new HashMap<String, Object>();

    private List<IRow> parts = new ArrayList<IRow>();

    public Map<String, RdEleceyePart> partMap = new HashMap<String, RdEleceyePart>();

    private List<IRow> pairs = new ArrayList<IRow>();

    public Map<Integer, RdEleceyePair> pairMap = new HashMap<Integer, RdEleceyePair>();

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
        return "rd_electroniceye";
    }

    @Override
    public ObjStatus status() {
        return eyeStatus;
    }

    @Override
    public void setStatus(ObjStatus os) {
    	this.eyeStatus = os;
    }

    @Override
    public ObjType objType() {
        return ObjType.RDELECTRONICEYE;
    }

    @Override
    public void copy(IRow row) {
        RdElectroniceye source = (RdElectroniceye) row;
        this.pid = source.pid;
        this.linkPid = source.linkPid;
        this.direct = source.direct;
        this.kind = source.kind;
        this.location = source.location;
        this.angle = source.angle;
        this.speedLimit = source.speedLimit;
        this.verifiedFlag = source.verifiedFlag;
        this.meshId = source.meshId;
        this.geometry = source.geometry;
        this.srcFlag = source.srcFlag;
        this.creationDate = source.creationDate;
        this.highViolation = source.highViolation;
        this.parts = new ArrayList<IRow>();
        for (IRow r : source.parts) {
            RdEleceyePart part = new RdEleceyePart();
            part.copy(r);
            this.parts.add(part);
        }
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
        return "rd_electroniceye";
    }

    @Override
    public List<List<IRow>> children() {
        List<List<IRow>> children = new ArrayList<List<IRow>>();
        // children.add(this.parts);
        return children;
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
                        if (key.equals("speedLimit")) {
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
        return this.meshId;
    }

    @Override
    public void setMesh(int mesh) {
        this.meshId = mesh;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {
        JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
        // 设置序列化的date格式
        jsonConfig.registerJsonValueProcessor(java.util.Date.class, new DateJsonValueProcessor("yyyy-MM-dd hh:mm:ss"));
        if (objLevel == ObjLevel.FULL) {
            speedLimit /= 10;
        }
        JSONObject json = JSONObject.fromObject(this, jsonConfig);
        json.remove("eyeStatus");
        return json;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {
        Iterator keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            JSONArray ja = null;
            if (json.get(key) instanceof JSONArray) {
                switch (key) {
                    case "parts":
                        parts.clear();
                        ja = json.getJSONArray(key);
                        for (int i = 0; i < ja.size(); i++) {
                            JSONObject jo = ja.getJSONObject(i);
                            RdEleceyePart row = new RdEleceyePart();
                            row.Unserialize(jo);
                            parts.add(row);
                        }
                        break;
                    case "pairs":
                        pairs.clear();
                        ja = json.getJSONArray(key);
                        for (int i = 0; i < ja.size(); i++) {
                            JSONObject jo = ja.getJSONObject(i);
                            RdEleceyePair row = new RdEleceyePair();
                            row.Unserialize(jo);
                            pairs.add(row);
                        }
                        break;
                }
            } else {
                Field f = this.getClass().getDeclaredField(key);
                f.setAccessible(true);
                if ("speedLimit".equals(key)) {
                    f.set(this, json.getInt(key) * 10);
                } else {
                    f.set(this, json.get(key));
                }
            }
        }
        return true;
    }

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

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
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

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
    }

    public int getVerifiedFlag() {
        return verifiedFlag;
    }

    public void setVerifiedFlag(int verifiedFlag) {
        this.verifiedFlag = verifiedFlag;
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

    public String getSrcFlag() {
        return srcFlag;
    }

    public void setSrcFlag(String srcFlag) {
        this.srcFlag = srcFlag;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getHighViolation() {
        return highViolation;
    }

    public void setHighViolation(int highViolation) {
        this.highViolation = highViolation;
    }

    public List<IRow> getParts() {
        return parts;
    }

    public void setParts(List<IRow> parts) {
        this.parts = parts;
    }

    public String getRowId() {
        return rowId;
    }

    public List<IRow> getPairs() {
        return pairs;
    }

    public void setPairs(List<IRow> pairs) {
        this.pairs = pairs;
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        return null;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        return null;
    }

}
