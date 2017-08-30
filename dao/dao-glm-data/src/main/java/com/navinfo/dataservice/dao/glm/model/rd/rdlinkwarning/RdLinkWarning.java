package com.navinfo.dataservice.dao.glm.model.rd.rdlinkwarning;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.*;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RdLinkWarning implements IObj {

    private int pid;

    private int linkPid;

    private int direct;//标牌作用方向

    private Geometry geometry;//标牌坐标

    private String typeCode;//标牌类型

    private int validDis = 0;//有效距离

    private int warnDis = 0;//预告距离

    private String timeDomain;//时间段

    private long vehicle = 0;//车辆类型

    private String descript;//文字说明

    private int meshId = 0;//图幅号码

    private int editFlag = 0;//编辑标识

    private String rowId;

    protected ObjStatus status;

    private Map<String, Object> changedFields = new HashMap<>();

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

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public int getValidDis() {
        return validDis;
    }

    public void setValidDis(int validDis) {
        this.validDis = validDis;
    }

    public int getWarnDis() {
        return warnDis;
    }

    public void setWarnDis(int warnDis) {
        this.warnDis = warnDis;
    }

    public String getTimeDomain() {
        return timeDomain;
    }

    public void setTimeDomain(String timeDomain) {
        this.timeDomain = timeDomain;
    }

    public long getVehicle() {
        return vehicle;
    }

    public void setVehicle(long vehicle) {
        this.vehicle = vehicle;
    }

    public String getDescript() {
        return descript;
    }

    public void setDescript(String descript) {
        this.descript = descript;
    }

    public int getMeshId() {
        return meshId;
    }

    public void setMeshId(int meshId) {
        this.meshId = meshId;
    }

    public int getEditFlag() {
        return editFlag;
    }

    public void setEditFlag(int editFlag) {
        this.editFlag = editFlag;
    }

    @Override
    public String rowId() {
        return rowId;
    }

    @Override
    public void setRowId(String rowId) {
        this.rowId = rowId;

    }

    @Override
    public String tableName() {

        return "rd_link_warning";
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

        return ObjType.RDLINKWARNING;
    }

    @Override
    public void copy(IRow row) {


    }

    @Override
    public Map<String, Object> changedFields() {
        return changedFields;
    }

    @Override
    public String parentPKName() {

        return "pid";
    }

    @Override
    public int parentPKValue() {
        return this.getPid();
    }

    @Override
    public String parentTableName() {
        return "rd_link_warning";
    }

    @Override
    public List<List<IRow>> children() {
        return null;
    }

    @Override
    public boolean fillChangeFields(JSONObject json) throws Exception {

        Iterator keys = json.keys();

        boolean handleGeo = false;

        while (keys.hasNext()) {

            String key = (String) keys.next();

            if (json.get(key) instanceof JSONArray) {

                continue;
            }

            if ("longitude".equals(key) || "latitude".equals(key)) {

                if (handleGeo) {

                    continue;
                }

                double longitude = json.getDouble("longitude");

                double latitude = json.getDouble("latitude");

                Geometry geometry = GeoTranslator.transform(GeoTranslator.createPoint(new Coordinate(longitude, latitude)), 1, 5);

                String wkt = GeoTranslator.jts2Wkt(geometry);

                String oldwkt = GeoTranslator.jts2Wkt(this.geometry, 0.00001, 5);

                if (!wkt.equals(oldwkt)) {

                    changedFields.put("geometry", GeoTranslator.jts2Geojson(geometry));
                }

                handleGeo = true;

            } else if (!"objStatus".equals(key)) {

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
    public int mesh() {
        return meshId;
    }

    @Override
    public void setMesh(int mesh) {
        this.meshId = mesh;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {

        JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

        JSONObject json = JSONObject.fromObject(this, jsonConfig);

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

            if ("geometry".equals(key)) {

                Geometry jts = GeoTranslator.geojson2Jts(
                        json.getJSONObject(key), 100000, 0);

                this.setGeometry(jts);

            } else if (!"objStatus".equals(key)) {

                Field f = this.getClass().getDeclaredField(key);

                f.setAccessible(true);

                f.set(this, json.get(key));
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

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IRow#childMap()
     */
    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.navinfo.dataservice.dao.glm.iface.IObj#childMap()
     */
    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        // TODO Auto-generated method stub
        return null;
    }
}
