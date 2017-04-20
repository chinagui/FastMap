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
 * @Title: CmgBuilding3dicon
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物的3DLandMark图标表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuilding3dicon implements IRow {

    /**
     * 建筑物号码
     */
    private int buildingPid;

    /**
     * 图标宽度
     */
    private int width = 64;

    /**
     * 图标高度
     */
    private int height = 64;

    /**
     * 图标文件名
     */
    private String iconName;

    /**
     * 通道文件名
     */
    private String alphaName;

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
        return "CMG_BUILDING_3DICON";
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
        return ObjType.CMGBUILDING3DICON;
    }

    @Override
    public void copy(IRow row) {
        CmgBuilding3dicon cmgBuilding3dicon = (CmgBuilding3dicon) row;

        this.buildingPid = cmgBuilding3dicon.buildingPid;
        this.width = cmgBuilding3dicon.width;
        this.height = cmgBuilding3dicon.height;
        this.iconName = cmgBuilding3dicon.iconName;
        this.alphaName = cmgBuilding3dicon.alphaName;
        this.rowId = cmgBuilding3dicon.rowId;
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
        return "CMG_BUILDING_3DICON";
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
     * Getter method for property <tt>width</tt>.
     *
     * @return property value of width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Setter method for property <tt>width</tt>.
     *
     * @param width value to be assigned to property width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Getter method for property <tt>height</tt>.
     *
     * @return property value of height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Setter method for property <tt>height</tt>.
     *
     * @param height value to be assigned to property height
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Getter method for property <tt>iconName</tt>.
     *
     * @return property value of iconName
     */
    public String getIconName() {
        return iconName;
    }

    /**
     * Setter method for property <tt>iconName</tt>.
     *
     * @param iconName value to be assigned to property iconName
     */
    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    /**
     * Getter method for property <tt>alphaName</tt>.
     *
     * @return property value of alphaName
     */
    public String getAlphaName() {
        return alphaName;
    }

    /**
     * Setter method for property <tt>alphaName</tt>.
     *
     * @param alphaName value to be assigned to property alphaName
     */
    public void setAlphaName(String alphaName) {
        this.alphaName = alphaName;
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
        return "CmgBuilding3dicon{" + "buildingPid=" + buildingPid + ", width=" + width + ", height=" + height + ", iconName='" +
                iconName + '\'' + ", alphaName='" + alphaName + '\'' + ", rowId='" + rowId + '\'' + ", changedFields=" + changedFields +
                ", status=" + status + '}';
    }
}
