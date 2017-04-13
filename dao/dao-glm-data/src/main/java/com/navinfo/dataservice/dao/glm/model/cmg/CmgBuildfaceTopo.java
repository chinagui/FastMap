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
 * @Title: CmgBuildfaceTopo
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物面的拓扑关系表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuildfaceTopo implements IRow {

    /**
     * FACE号码
     */
    private int facePid;

    /**
     * LINK序号
     */
    private int seqNum = 1;

    /**
     * LINK号码
     */
    private int linkPid;

    /**
     * 行记录ID
     */
    private String rowId;

    /**
     * 待修改数据
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
        return "CMG_BUILDFACE_TOPO";
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
        return ObjType.CMGBUILDFACETOPO;
    }

    @Override
    public void copy(final IRow row) {
        CmgBuildfaceTopo cmgBuildfaceTopo = (CmgBuildfaceTopo) row;

        this.facePid = cmgBuildfaceTopo.facePid;
        this.seqNum = cmgBuildfaceTopo.seqNum;
        this.linkPid = cmgBuildfaceTopo.linkPid;
        this.rowId = cmgBuildfaceTopo.rowId;
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
     * Getter method for property <tt>seqNum</tt>.
     *
     * @return property value of seqNum
     */
    public int getSeqNum() {
        return seqNum;
    }

    /**
     * Setter method for property <tt>seqNum</tt>.
     *
     * @param seqNum value to be assigned to property seqNum
     */
    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
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
     * Getter method for property <tt>rowId</tt>.
     *
     * @return property value of rowId
     */
    public String getRowId() {
        return rowId;
    }
}
