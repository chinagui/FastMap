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
 * @Title: CmgBuildlink
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物LINK表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuildlink implements IObj {

    /**
     * LINK号码
     */
    private int pid;

    /**
     * 起始节点
     */
    private int sNodePid;

    /**
     * 终止节点
     */
    private int eNodePid;

    /**
     * LINK种别
     */
    private int kind = 1;

    /**
     * LINK坐标
     */
    private Geometry geometry;

    /**
     * LINK长度
     */
    private double length;

    /**
     * 编辑标识
     */
    private int editFlag;

    /**
     * 行记录ID
     */
    private String rowId;

    /**
     * 数据状态
     */
    protected ObjStatus status;

    /**
     * 待处理数据
     */
    private Map<String, Object> changedFields = new HashMap<>();

    /**
     * 建筑物LINK图幅表
     */
    private List<IRow> meshes = new ArrayList<>();

    /**
     * 建筑物LINK图幅表
     */
    public Map<String, CmgBuildlinkMesh> meshMap = new HashMap<>();

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
        return "LINK_PID";
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        Map<Class<? extends IRow>, List<IRow>> map = new HashMap<>();
        map.put(CmgBuildlinkMesh.class, this.meshes);
        return map;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        Map<Class<? extends IRow>, Map<String, ?>> map = new HashMap<>();
        map.put(CmgBuildlinkMesh.class, this.meshMap);
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
        return "CMG_BUILDLINK";
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
        return ObjType.CMGBUILDLINK;
    }

    @Override
    public void copy(final IRow row) {
        CmgBuildlink cmgBuildlink = (CmgBuildlink) row;

        this.sNodePid = cmgBuildlink.sNodePid;
        this.eNodePid = cmgBuildlink.eNodePid;
        this.kind = cmgBuildlink.kind;
        this.geometry = cmgBuildlink.geometry;
        this.length = cmgBuildlink.length;
        this.editFlag = cmgBuildlink.editFlag;
        this.rowId = cmgBuildlink.rowId;

        List<IRow> meshes = new ArrayList<>();
        for (IRow m : cmgBuildlink.meshes) {
            CmgBuildlinkMesh mesh = new CmgBuildlinkMesh();
            mesh.copy(m);
            mesh.setLinkPid(this.pid());
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
        return "LINK_PID";
    }

    @Override
    public int parentPKValue() {
        return this.pid;
    }

    @Override
    public String parentTableName() {
        return "CMG_BUILDLINK";
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
    public void setMesh(final int mesh) {
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
     * Getter method for property <tt>sNodePid</tt>.
     *
     * @return property value of sNodePid
     */
    public int getsNodePid() {
        return sNodePid;
    }

    /**
     * Setter method for property <tt>sNodePid</tt>.
     *
     * @param sNodePid value to be assigned to property sNodePid
     */
    public void setsNodePid(int sNodePid) {
        this.sNodePid = sNodePid;
    }

    /**
     * Getter method for property <tt>eNodePid</tt>.
     *
     * @return property value of eNodePid
     */
    public int geteNodePid() {
        return eNodePid;
    }

    /**
     * Setter method for property <tt>eNodePid</tt>.
     *
     * @param eNodePid value to be assigned to property eNodePid
     */
    public void seteNodePid(int eNodePid) {
        this.eNodePid = eNodePid;
    }

    /**
     * Getter method for property <tt>kind</tt>.
     *
     * @return property value of kind
     */
    public int getKind() {
        return kind;
    }

    /**
     * Setter method for property <tt>kind</tt>.
     *
     * @param kind value to be assigned to property kind
     */
    public void setKind(int kind) {
        this.kind = kind;
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
     * Getter method for property <tt>length</tt>.
     *
     * @return property value of length
     */
    public double getLength() {
        return length;
    }

    /**
     * Setter method for property <tt>length</tt>.
     *
     * @param length value to be assigned to property length
     */
    public void setLength(double length) {
        this.length = length;
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
        return "CmgBuildlink{" + "pid=" + pid + ", sNodePid=" + sNodePid + ", eNodePid=" + eNodePid + ", kind=" + kind + ", geometry=" +
                geometry + ", length=" + length + ", editFlag=" + editFlag + ", rowId='" + rowId + '\'' + ", status=" + status + ", " +
                "changedFields=" + changedFields + ", meshes=" + meshes + ", meshMap=" + meshMap + '}';
    }
}
