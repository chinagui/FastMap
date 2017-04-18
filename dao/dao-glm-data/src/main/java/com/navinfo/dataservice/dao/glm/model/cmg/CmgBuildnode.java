package com.navinfo.dataservice.dao.glm.model.cmg;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: CmgBuildnode
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物NODE表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuildnode implements IObj {

    /**
     * NODE号码
     */
    private int pid;

    /**
     * NODE形态
     */
    private int form;

    /**
     * NODE坐标
     */
    private Geometry geometry;

    /**
     * 编辑标识
     */
    private int editFlag = 1;

    /**
     * 行记录ID
     */
    private String rowId;

    /**
     * 数据状态
     */
    protected ObjStatus status;

    /**
     * 待修改数据
     */
    private Map<String, Object> changedFields = new HashMap<>();

    /**
     * 建筑物NODE图幅表
     */
    private List<IRow> meshes = new ArrayList<>();

    /**
     * 建筑物NODE图幅表
     */
    public Map<String, CmgBuildnodeMesh> meshMap = new HashMap<>();

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
        return "NODE_PID";
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        Map<Class<? extends IRow>, List<IRow>> map = new HashMap<>();
        map.put(CmgBuildnodeMesh.class, this.meshes);
        return map;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        Map<Class<? extends IRow>, Map<String, ?>> map = new HashMap<>();
        map.put(CmgBuildnodeMesh.class, this.meshMap);
        return map;
    }

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
        return "CMG_BUILDNODE";
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
        return ObjType.CMGBUILDNODE;
    }

    @Override
    public void copy(final IRow row) {
        CmgBuildnode cmgBuildnode = (CmgBuildnode) row;

        this.form = cmgBuildnode.form;
        this.geometry = cmgBuildnode.geometry;
        this.editFlag = cmgBuildnode.editFlag;
        this.rowId = cmgBuildnode.rowId;

        List<IRow> meshes = new ArrayList<>();
        for (IRow m : cmgBuildnode.meshes) {
            CmgBuildnodeMesh mesh = new CmgBuildnodeMesh();
            mesh.copy(m);
            mesh.setNodePid(this.pid());
            meshes.add(mesh);
        }
        this.meshes = meshes;
    }

    @Override
    public Map<String, Object> changedFields() {
        return this.changedFields;
    }

    @Override
    public String parentPKName() {
        return "NODE_PID";
    }

    @Override
    public int parentPKValue() {
        return this.pid;
    }

    @Override
    public String parentTableName() {
        return "CMG_BUILDNODE";
    }

    @Override
    public List<List<IRow>> children() {
        List<List<IRow>> list = new ArrayList<>();
        list.add(this.meshes);
        return list;
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
    public void setMesh(int mesh) {
    }

    @Override
    public JSONObject Serialize(final ObjLevel objLevel) throws Exception {
        JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);
        return JSONObject.fromObject(this, jsonConfig);
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
     * Getter method for property <tt>form</tt>.
     *
     * @return property value of form
     */
    public int getForm() {
        return form;
    }

    /**
     * Setter method for property <tt>form</tt>.
     *
     * @param form value to be assigned to property form
     */
    public void setForm(int form) {
        this.form = form;
    }

    /**
     * Getter method for property <tt>geometry</tt>.
     *
     * @return property value of geometry
     */
    public Geometry getGeometry() {
        return geometry;
    }

    /**
     * Setter method for property <tt>geometry</tt>.
     *
     * @param geometry value to be assigned to property geometry
     */
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    /**
     * Getter method for property <tt>editFlag</tt>.
     *
     * @return property value of editFlag
     */
    public int getEditFlag() {
        return editFlag;
    }

    /**
     * Setter method for property <tt>editFlag</tt>.
     *
     * @param editFlag value to be assigned to property editFlag
     */
    public void setEditFlag(int editFlag) {
        this.editFlag = editFlag;
    }

    /**
     * Getter method for property <tt>rowId</tt>.
     *
     * @return property value of rowId
     */
    public String getRowId() {
        return rowId;
    }

    /**
     * Getter method for property <tt>meshes</tt>.
     *
     * @return property value of meshes
     */
    public List<IRow> getMeshes() {
        return meshes;
    }

    /**
     * Setter method for property <tt>meshes</tt>.
     *
     * @param meshes value to be assigned to property meshes
     */
    public void setMeshes(List<IRow> meshes) {
        this.meshes = meshes;
    }

    @Override
    public String toString() {
        return "CmgBuildnode{" + "pid=" + pid + ", form=" + form + ", geometry=" + geometry + ", editFlag=" + editFlag + ", rowId='" +
                rowId + '\'' + ", status=" + status + ", changedFields=" + changedFields + ", meshes=" + meshes + ", meshMap=" + meshMap + '}';
    }
}
