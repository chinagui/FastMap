package com.navinfo.dataservice.dao.glm.model.cmg;

import com.navinfo.dataservice.commons.util.JsonUtils;
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

/**
 * @Title: CmgBuildfaceTenant
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物租户信息表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuildfaceTenant implements IRow {

    /**
     * FACE号码
     */
    private int facePid;

    /**
     * POI号码
     */
    private int poiPid;

    /**
     * 租户标识
     */
    private int flag;

    /**
     * 电话号码
     */
    private String tel;

    /**
     * X坐标
     */
    private double x;

    /**
     * Y坐标
     */
    private double y;

    /**
     * 名称
     */
    private String name;

    /**
     * 楼层
     */
    private String floor;

    /**
     * 来源标识
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
    public void setRowId(final String rowId) {
        this.rowId = rowId;
    }

    @Override
    public String tableName() {
        return "CMG_BUILDFACE_TENANT";
    }

    @Override
    public ObjStatus status() {
        return this.status;
    }

    @Override
    public void setStatus(final ObjStatus os) {
        this.status = os;
    }

    @Override
    public ObjType objType() {
        return ObjType.CMGBUILDFACETENANT;
    }

    @Override
    public void copy(final IRow row) {
        CmgBuildfaceTenant cmgBuildfaceTenant = (CmgBuildfaceTenant) row;

        this.facePid = cmgBuildfaceTenant.facePid;
        this.poiPid = cmgBuildfaceTenant.poiPid;
        this.flag = cmgBuildfaceTenant.flag;
        this.tel = cmgBuildfaceTenant.tel;
        this.x = cmgBuildfaceTenant.x;
        this.y = cmgBuildfaceTenant.y;
        this.name = cmgBuildfaceTenant.name;
        this.floor = cmgBuildfaceTenant.floor;
        this.srcFlag = cmgBuildfaceTenant.srcFlag;
        this.rowId = cmgBuildfaceTenant.rowId;
    }

    @Override
    public Map<String, Object> changedFields() {
        return this.changedFields;
    }

    @Override
    public String parentPKName() {
        return "FACE_PID";
    }

    @Override
    public int parentPKValue() {
        return this.facePid;
    }

    @Override
    public String parentTableName() {
        return "CMG_BUILDFACE";
    }

    @Override
    public List<List<IRow>> children() {
        return null;
    }

    @Override
    public boolean fillChangeFields(final JSONObject json) throws Exception {
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
    public void setMesh(final int mesh) {
    }

    @Override
    public JSONObject Serialize(final ObjLevel objLevel) throws Exception {
        return JSONObject.fromObject(this, JsonUtils.getStrConfig());
    }

    @Override
    public boolean Unserialize(final JSONObject json) throws Exception {
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
     * Getter method for property <tt>facePid</tt>.
     *
     * @return property value of facePid
     */
    public int getFacePid() {
        return facePid;
    }

    /**
     * Setter method for property <tt>facePid</tt>.
     *
     * @param facePid value to be assigned to property facePid
     */
    public void setFacePid(int facePid) {
        this.facePid = facePid;
    }

    /**
     * Getter method for property <tt>poiPid</tt>.
     *
     * @return property value of poiPid
     */
    public int getPoiPid() {
        return poiPid;
    }

    /**
     * Setter method for property <tt>poiPid</tt>.
     *
     * @param poiPid value to be assigned to property poiPid
     */
    public void setPoiPid(int poiPid) {
        this.poiPid = poiPid;
    }

    /**
     * Getter method for property <tt>flag</tt>.
     *
     * @return property value of flag
     */
    public int getFlag() {
        return flag;
    }

    /**
     * Setter method for property <tt>flag</tt>.
     *
     * @param flag value to be assigned to property flag
     */
    public void setFlag(int flag) {
        this.flag = flag;
    }

    /**
     * Getter method for property <tt>tel</tt>.
     *
     * @return property value of tel
     */
    public String getTel() {
        return tel;
    }

    /**
     * Setter method for property <tt>tel</tt>.
     *
     * @param tel value to be assigned to property tel
     */
    public void setTel(String tel) {
        this.tel = tel;
    }

    /**
     * Getter method for property <tt>x</tt>.
     *
     * @return property value of x
     */
    public double getX() {
        return x;
    }

    /**
     * Setter method for property <tt>x</tt>.
     *
     * @param x value to be assigned to property x
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Getter method for property <tt>y</tt>.
     *
     * @return property value of y
     */
    public double getY() {
        return y;
    }

    /**
     * Setter method for property <tt>y</tt>.
     *
     * @param y value to be assigned to property y
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Getter method for property <tt>name</tt>.
     *
     * @return property value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter method for property <tt>name</tt>.
     *
     * @param name value to be assigned to property name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method for property <tt>floor</tt>.
     *
     * @return property value of floor
     */
    public String getFloor() {
        return floor;
    }

    /**
     * Setter method for property <tt>floor</tt>.
     *
     * @param floor value to be assigned to property floor
     */
    public void setFloor(String floor) {
        this.floor = floor;
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
}
