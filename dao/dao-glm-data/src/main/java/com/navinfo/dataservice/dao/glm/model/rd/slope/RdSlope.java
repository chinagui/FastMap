package com.navinfo.dataservice.dao.glm.model.rd.slope;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

/***
 * 坡度模型
 *
 * @author zhaokk
 *
 */
public class RdSlope implements IObj {

    private String rowId;

    private int pid;// 坡度号码

    private int nodePid;// NODE 号码

    private int linkPid;//LINK 号码

    private int type = 1; // 坡度类型 0 未调查1 水平2 上坡 3 下坡

    private int angle = 0;// 坡度角度
    private Map<String, Object> changedFields = new HashMap<String, Object>();
    public Map<String, RdSlopeVia> rdSlopeMap = new HashMap<String, RdSlopeVia>();
    private List<IRow> slopeVias = new ArrayList<IRow>();

    protected ObjStatus status;

    public List<IRow> getSlopeVias() {
        return slopeVias;
    }

    public void setSlopeVias(List<IRow> slopeVias) {
        this.slopeVias = slopeVias;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int nodePid) {
        this.pid = nodePid;
    }

    @Override
    public String tableName() {

        return "rd_slope";
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

        return ObjType.RDSLOPE;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {

        JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

        JSONObject json = JSONObject.fromObject(this, jsonConfig);

        return json;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {

        return true;
    }

    @Override
    public List<IRow> relatedRows() {

        return null;
    }

    @Override
    public void copy(IRow row) {

    }

    @Override
    public Map<String, Object> changedFields() {

        return changedFields;
    }

    @Override
    public int pid() {

        return this.getPid();
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
    public List<List<IRow>> children() {

        List<List<IRow>> children = new ArrayList<List<IRow>>();
        children.add(this.getSlopeVias());
        return children;
    }

    @Override
    public String parentTableName() {

        return "rd_slope";
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
    public boolean fillChangeFields(JSONObject json) throws Exception {

        @SuppressWarnings("rawtypes") Iterator keys = json.keys();

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

    public int getNodePid() {
        return nodePid;
    }

    public void setNodePid(int nodePid) {
        this.nodePid = nodePid;
    }

    public int getLinkPid() {
        return linkPid;
    }

    public void setLinkPid(int linkPid) {
        this.linkPid = linkPid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public String getRowId() {
        return rowId;
    }

    @Override
    public int mesh() {
        return 0;
    }

    @Override
    public void setMesh(int mesh) {
    }

    @Override
    public String primaryKey() {
        return "pid";
    }

    /* (non-Javadoc)
     * @see com.navinfo.dataservice.dao.glm.iface.IRow#childMap()
     */
    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        Map<Class<? extends IRow>, List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
        childList.put(RdSlopeVia.class, slopeVias);
        return childList;
    }

    /* (non-Javadoc)
     * @see com.navinfo.dataservice.dao.glm.iface.IObj#childMap()
     */
    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
        childMap.put(RdSlopeVia.class, rdSlopeMap);
        return childMap;
    }
}
