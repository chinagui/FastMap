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
 * @Title: CmgBuildlinkMesh
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物LINK图幅表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuildlinkMesh implements IRow {

    /**
     * LINK号码
     */
    private int linkPid;

    /**
     * 图幅号码
     */
    private int meshId;

    /**
     * 行记录ID
     */
    private String rowId;

    /**
     * 待修改数据
     */
    public Map<String, Object> changedFields = new HashMap<>();

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
        return "CMG_BUILDLINK_MESH";
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
        return ObjType.CMGBUILDLINKMESH;
    }

    @Override
    public void copy(final IRow row) {
        CmgBuildlinkMesh mesh = (CmgBuildlinkMesh) row;

        this.linkPid = mesh.linkPid;
        this.meshId = mesh.meshId;
        this.rowId = mesh.rowId;
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
        return this.linkPid;
    }

    @Override
    public String parentTableName() {
        return "CMG_BUILDLINK";
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
        return this.meshId;
    }

    @Override
    public void setMesh(final int mesh) {
        this.meshId = mesh;
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
     * Getter method for property <tt>linkPid</tt>.
     *
     * @return property value of linkPid
     */
    public int getLinkPid() {
        return linkPid;
    }

    /**
     * Setter method for property <tt>linkPid</tt>.
     *
     * @param linkPid value to be assigned to property linkPid
     */
    public void setLinkPid(int linkPid) {
        this.linkPid = linkPid;
    }

    /**
     * Getter method for property <tt>meshId</tt>.
     *
     * @return property value of meshId
     */
    public int getMeshId() {
        return meshId;
    }

    /**
     * Setter method for property <tt>meshId</tt>.
     *
     * @param meshId value to be assigned to property meshId
     */
    public void setMeshId(int meshId) {
        this.meshId = meshId;
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
