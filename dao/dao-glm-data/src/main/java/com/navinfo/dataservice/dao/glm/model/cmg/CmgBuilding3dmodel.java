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
 * @Title: CmgBuilding3dmodel
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物的3DLandMark模型表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuilding3dmodel implements IObj {

    /**
     * 模型号码
     */
    private int pid;

    /**
     * 建筑物号码
     */
    private int buildingPid;

    /**
     * 分辨率
     */
    private int resolution;

    /**
     * 模型文件名
     */
    private String modelName;

    /**
     * 材质文件名
     */
    private String materialName;

    /**
     * 纹理文件名
     */
    private String textureName;

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
        return "CMG_BUILDING_3DMODEL";
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
        return ObjType.CMGBUILDING3DMODEL;
    }

    @Override
    public void copy(IRow row) {
        CmgBuilding3dmodel cmgBuilding3dmodel = (CmgBuilding3dmodel) row;

        this.buildingPid = cmgBuilding3dmodel.buildingPid;
        this.resolution = cmgBuilding3dmodel.resolution;
        this.modelName = cmgBuilding3dmodel.modelName;
        this.materialName = cmgBuilding3dmodel.materialName;
        this.textureName = cmgBuilding3dmodel.textureName;
        this.rowId = cmgBuilding3dmodel.rowId;
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
     * Getter method for property <tt>resolution</tt>.
     *
     * @return property value of resolution
     */
    public int getResolution() {
        return resolution;
    }

    /**
     * Setter method for property <tt>resolution</tt>.
     *
     * @param resolution value to be assigned to property resolution
     */
    public void setResolution(int resolution) {
        this.resolution = resolution;
    }

    /**
     * Getter method for property <tt>modelName</tt>.
     *
     * @return property value of modelName
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Setter method for property <tt>modelName</tt>.
     *
     * @param modelName value to be assigned to property modelName
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    /**
     * Getter method for property <tt>materialName</tt>.
     *
     * @return property value of materialName
     */
    public String getMaterialName() {
        return materialName;
    }

    /**
     * Setter method for property <tt>materialName</tt>.
     *
     * @param materialName value to be assigned to property materialName
     */
    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    /**
     * Getter method for property <tt>textureName</tt>.
     *
     * @return property value of textureName
     */
    public String getTextureName() {
        return textureName;
    }

    /**
     * Setter method for property <tt>textureName</tt>.
     *
     * @param textureName value to be assigned to property textureName
     */
    public void setTextureName(String textureName) {
        this.textureName = textureName;
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
        return "CmgBuilding3dmodel{" + "modelId=" + pid + ", buildingPid=" + buildingPid + ", resolution=" + resolution + ", " +
                "modelName='" + modelName + '\'' + ", materialName='" + materialName + '\'' + ", textureName='" + textureName + '\'' +
                ", rowId='" + rowId + '\'' + ", changedFields=" + changedFields + ", status=" + status + '}';
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
        return "MODEL_ID";
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
