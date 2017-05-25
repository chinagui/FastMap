package com.navinfo.dataservice.dao.glm.model.cmg;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: CmgBuildingName
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物名称表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuildingName implements IObj {

    /**
     * 名称号码
     */
    private int pid;

    /**
     * 建筑物号码
     */
    private int buildingPid;

    /**
     * 名称组号
     */
    private int nameGroupid = 1;

    /**
     * 语言代码
     */
    private String langCode;

    /**
     * 建筑物全称
     */
    private String fullName;

    /**
     * 建筑物基本名
     */
    private String baseName;

    /**
     * 建筑物楼号
     */
    private String buildNumber;

    /**
     * 建筑物全称发音
     */
    private String fullNamePhonetic;

    /**
     * 建筑物基本名发音
     */
    private String baseNamePhonetic;

    /**
     * 建筑物楼号发音
     */
    private String buildNumPhonetic;

    /**
     * 名称来源
     */
    private int srcFlag;

    /**
     * 行记录ID
     */
    private String rowId;

    /**
     * 待处理数据
     */
    private Map<String, Object> changedFields = new HashMap<>();

    /**
     * 数据状态
     */
    protected ObjStatus status;

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
        return "CMG_BUILDING_NAME";
    }

    @Override
    public ObjStatus status() {
        return this.status;
    }

    @Override
    public void setStatus(ObjStatus os) {
        this.status = os;
    }

    @Override
    public ObjType objType() {
        return ObjType.CMGBUILDINGNAME;
    }

    @Override
    public void copy(IRow row) {
        CmgBuildingName cmgBuildingName = (CmgBuildingName) row;

        this.buildingPid = cmgBuildingName.buildingPid;
        this.nameGroupid = cmgBuildingName.nameGroupid;
        this.langCode = cmgBuildingName.langCode;
        this.fullName = cmgBuildingName.fullName;
        this.baseName = cmgBuildingName.baseName;
        this.buildNumber = cmgBuildingName.buildNumber;
        this.fullNamePhonetic = cmgBuildingName.fullNamePhonetic;
        this.baseNamePhonetic = cmgBuildingName.baseNamePhonetic;
        this.buildNumPhonetic = cmgBuildingName.buildNumPhonetic;
        this.srcFlag = cmgBuildingName.srcFlag;
        this.rowId = cmgBuildingName.rowId;
    }

    @Override
    public Map<String, Object> changedFields() {
        return this.changedFields;
    }

    @Override
    public String parentPKName() {
        return "BUILDING_PID";
    }

    @Override
    public int parentPKValue() {
        return this.buildingPid;
    }

    @Override
    public String parentTableName() {
        return "CMG_BUILDING";
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
        return changedFields.size() > 0;
    }

    @Override
    public int mesh() {
        return 0;
    }

    @Override
    public void setMesh(int mesh) {
    }

    @Override
    public JSONObject Serialize(ObjLevel objLevel) throws Exception {
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
                f.set(this, json.get(key));
            }
        }
        return true;
    }


    /**
     * Getter method for property <tt>pid</tt>.
     *
     * @return property value of pid
     */
    public int getPid() {
        return pid;
    }

    /**
     * Setter method for property <tt>pid</tt>.
     *
     * @param pid value to be assigned to property pid
     */
    public void setPid(int pid) {
        this.pid = pid;
    }


    /**
     * Getter method for property <tt>buildingPid</tt>.
     *
     * @return property value of buildingPid
     */
    public int getBuildingPid() {
        return buildingPid;
    }

    /**
     * Setter method for property <tt>buildingPid</tt>.
     *
     * @param buildingPid value to be assigned to property buildingPid
     */
    public void setBuildingPid(int buildingPid) {
        this.buildingPid = buildingPid;
    }

    /**
     * Getter method for property <tt>nameGroupid</tt>.
     *
     * @return property value of nameGroupid
     */
    public int getNameGroupid() {
        return nameGroupid;
    }

    /**
     * Setter method for property <tt>nameGroupid</tt>.
     *
     * @param nameGroupid value to be assigned to property nameGroupid
     */
    public void setNameGroupid(int nameGroupid) {
        this.nameGroupid = nameGroupid;
    }

    /**
     * Getter method for property <tt>langCode</tt>.
     *
     * @return property value of langCode
     */
    public String getLangCode() {
        return langCode;
    }

    /**
     * Setter method for property <tt>langCode</tt>.
     *
     * @param langCode value to be assigned to property langCode
     */
    public void setLangCode(String langCode) {
        this.langCode = langCode;
    }

    /**
     * Getter method for property <tt>fullName</tt>.
     *
     * @return property value of fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Setter method for property <tt>fullName</tt>.
     *
     * @param fullName value to be assigned to property fullName
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Getter method for property <tt>baseName</tt>.
     *
     * @return property value of baseName
     */
    public String getBaseName() {
        return baseName;
    }

    /**
     * Setter method for property <tt>baseName</tt>.
     *
     * @param baseName value to be assigned to property baseName
     */
    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    /**
     * Getter method for property <tt>buildNumber</tt>.
     *
     * @return property value of buildNumber
     */
    public String getBuildNumber() {
        return buildNumber;
    }

    /**
     * Setter method for property <tt>buildNumber</tt>.
     *
     * @param buildNumber value to be assigned to property buildNumber
     */
    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    /**
     * Getter method for property <tt>fullNamePhonetic</tt>.
     *
     * @return property value of fullNamePhonetic
     */
    public String getFullNamePhonetic() {
        return fullNamePhonetic;
    }

    /**
     * Setter method for property <tt>fullNamePhonetic</tt>.
     *
     * @param fullNamePhonetic value to be assigned to property fullNamePhonetic
     */
    public void setFullNamePhonetic(String fullNamePhonetic) {
        this.fullNamePhonetic = fullNamePhonetic;
    }

    /**
     * Getter method for property <tt>baseNamePhonetic</tt>.
     *
     * @return property value of baseNamePhonetic
     */
    public String getBaseNamePhonetic() {
        return baseNamePhonetic;
    }

    /**
     * Setter method for property <tt>baseNamePhonetic</tt>.
     *
     * @param baseNamePhonetic value to be assigned to property baseNamePhonetic
     */
    public void setBaseNamePhonetic(String baseNamePhonetic) {
        this.baseNamePhonetic = baseNamePhonetic;
    }

    /**
     * Getter method for property <tt>buildNumPhonetic</tt>.
     *
     * @return property value of buildNumPhonetic
     */
    public String getBuildNumPhonetic() {
        return buildNumPhonetic;
    }

    /**
     * Setter method for property <tt>buildNumPhonetic</tt>.
     *
     * @param buildNumPhonetic value to be assigned to property buildNumPhonetic
     */
    public void setBuildNumPhonetic(String buildNumPhonetic) {
        this.buildNumPhonetic = buildNumPhonetic;
    }

    /**
     * Getter method for property <tt>srcFlag</tt>.
     *
     * @return property value of srcFlag
     */
    public int getSrcFlag() {
        return srcFlag;
    }

    /**
     * Setter method for property <tt>srcFlag</tt>.
     *
     * @param srcFlag value to be assigned to property srcFlag
     */
    public void setSrcFlag(int srcFlag) {
        this.srcFlag = srcFlag;
    }

    /**
     * Getter method for property <tt>rowId</tt>.
     *
     * @return property value of rowId
     */
    public String getRowId() {
        return rowId;
    }

    @Override
    public String toString() {
        return "CmgBuildingName{" + "nameId=" + pid + ", buildingPid=" + buildingPid + ", nameGroupid=" + nameGroupid + ", " +
                "langCode='" + langCode + '\'' + ", fullName='" + fullName + '\'' + ", baseName='" + baseName + '\'' + ", buildNumber='"
                + buildNumber + '\'' + ", fullNamePhonetic='" + fullNamePhonetic + '\'' + ", baseNamePhonetic='" + baseNamePhonetic +
                '\'' + ", buildNumPhonetic='" + buildNumPhonetic + '\'' + ", srcFlag=" + srcFlag + ", rowId='" + rowId + '\'' + ", " +
                "changedFields=" + changedFields + ", status=" + status + '}';
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
         return "NAME_ID";
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
