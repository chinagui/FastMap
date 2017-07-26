package com.navinfo.dataservice.dao.glm.model.rd.tollgate;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RdTollgatePassage implements IRow {

    private int pid;
    private int seqNum = 1;
    private int tollForm;
    private int cardType;
    private long vehicle = 0;

    private int laneType = 0;

    private String rowId;
    public Map<String, Object> changedFields = new HashMap<String, Object>();

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public int getTollForm() {
        return tollForm;
    }

    public void setTollForm(int tollForm) {
        this.tollForm = tollForm;
    }

    public int getCardType() {
        return cardType;
    }

    public void setCardType(int cardType) {
        this.cardType = cardType;
    }

    public int getLaneType() {
        return laneType;
    }

    public void setLaneType(int laneType) {
        this.laneType = laneType;
    }

    public String getRowId() {
        return rowId;
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {
        JSONObject json = JSONObject.fromObject(this);

        return json;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {
        Iterator keys = json.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            if("objStatus".equals(key))
                continue;

            JSONArray ja = null;

            Field f = this.getClass().getDeclaredField(key);

            f.setAccessible(true);

            f.set(this, json.get(key));
        }

        return true;
    }

    public long getVehicle() {
        return vehicle;
    }

    public void setVehicle(long vehicle) {
        this.vehicle = vehicle;
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
        return "rd_tollgate_passage";
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
        return ObjType.RDTOLLGATEPASSAGE;
    }

    @Override
    public void copy(IRow row) {
        RdTollgatePassage tollgate = (RdTollgatePassage) row;
        this.seqNum = tollgate.getSeqNum();
        this.tollForm = tollgate.getTollForm();
        this.cardType = tollgate.getCardType();
        this.vehicle = tollgate.vehicle;
        this.laneType = tollgate.laneType;

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
        return "rd_tollgate";
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
        return 0;
    }

    @Override
    public void setMesh(int mesh) {

    }

}
