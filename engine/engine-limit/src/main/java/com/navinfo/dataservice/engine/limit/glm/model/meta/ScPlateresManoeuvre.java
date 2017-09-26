package com.navinfo.dataservice.engine.limit.glm.model.meta;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.engine.limit.glm.iface.IObj;
import com.navinfo.dataservice.engine.limit.glm.iface.IRow;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class ScPlateresManoeuvre implements IObj {


    private int manoeuvreId;//MANOEUVRE_ID
    private String groupId;//GROUP_ID
    private String vehicle;//VEHICLE
    private String attribution;//ATTRIBUTION
    private String restrict;//RESTRICT
    private int tempPlate;//TEMP_PLATE
    private String tempPlateNum;//TEMP_PLATE_NUM
    private int charSwitch;//CHAR_SWITCH
    private String charToNum;//CHAR_TO_NUM
    private String tailNumber;//TAIL_NUMBER
    private String platecolor;//PLATECOLOR
    private String energyType;//ENERGY_TYPE
    private String gasEmisstand;//GAS_EMISSTAND
    private int seatnum;//SEATNUM
    private double vehicleLength;//VEHICLE_LENGTH
    private double resWeigh;//RES_WEIGH
    private double resAxleLoad;//RES_AXLE_LOAD
    private int resAxleCount;//RES_AXLE_COUNT
    private String startDate;//START_DATE
    private String endDate;//END_DATE
    private String resDatetype;//RES_DATETYPE
    private String time;//TIME
    private String specFlag;//SPEC_FLAG


    public int getManoeuvreId() {
        return manoeuvreId;
    }

    public void setManoeuvreId(int manoeuvreId) {
        this.manoeuvreId = manoeuvreId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getRestrict() {
        return restrict;
    }

    public void setRestrict(String restrict) {
        this.restrict = restrict;
    }

    public int getTempPlate() {
        return tempPlate;
    }

    public void setTempPlate(int tempPlate) {
        this.tempPlate = tempPlate;
    }

    public String getTempPlateNum() {
        return tempPlateNum;
    }

    public void setTempPlateNum(String tempPlateNum) {
        this.tempPlateNum = tempPlateNum;
    }

    public int getCharSwitch() {
        return charSwitch;
    }

    public void setCharSwitch(int charSwitch) {
        this.charSwitch = charSwitch;
    }

    public String getCharToNum() {
        return charToNum;
    }

    public void setCharToNum(String charToNum) {
        this.charToNum = charToNum;
    }

    public String getTailNumber() {
        return tailNumber;
    }

    public void setTailNumber(String tailNumber) {
        this.tailNumber = tailNumber;
    }

    public String getPlatecolor() {
        return platecolor;
    }

    public void setPlatecolor(String platecolor) {
        this.platecolor = platecolor;
    }

    public String getEnergyType() {
        return energyType;
    }

    public void setEnergyType(String energyType) {
        this.energyType = energyType;
    }

    public String getGasEmisstand() {
        return gasEmisstand;
    }

    public void setGasEmisstand(String gasEmisstand) {
        this.gasEmisstand = gasEmisstand;
    }

    public int getSeatnum() {
        return seatnum;
    }

    public void setSeatnum(int seatnum) {
        this.seatnum = seatnum;
    }

    public double getVehicleLength() {
        return vehicleLength;
    }

    public void setVehicleLength(double vehicleLength) {
        this.vehicleLength = vehicleLength;
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

    public int getResAxleCount() {
        return resAxleCount;
    }

    public void setResAxleCount(int resAxleCount) {
        this.resAxleCount = resAxleCount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getResDatetype() {
        return resDatetype;
    }

    public void setResDatetype(String resDatetype) {
        this.resDatetype = resDatetype;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getSpecFlag() {
        return specFlag;
    }

    public void setSpecFlag(String specFlag) {
        this.specFlag = specFlag;
    }

    protected ObjStatus status;


    private Map<String, Object> changedFields = new HashMap<>();


    @Override
    public List<IRow> relatedRows() {
        return null;
    }

    @Override
    public String primaryKeyValue() {
        return String.valueOf(manoeuvreId);//实际并不是该表的主键值
    }

    @Override
    public String primaryKey() {
        return "MANOEUVRE_ID";//实际并不是该表的主键
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
        return "SC_PLATERES_MANOEUVRE";
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
        return LimitObjType.SCPLATERESMANOEUVRE;
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
        return groupId;
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
