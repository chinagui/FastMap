package com.navinfo.dataservice.dao.glm.model.cmg;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: CmgBuilding
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物要素表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuilding implements IObj {

    /**
     * 建筑物号码
     */
    private int pid;

    /**
     * 建筑物种别
     */
    private String kind;

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

    /**
     * 建筑物名称表
     */
    private List<IRow> names = new ArrayList<>();

    /**
     * 建筑物名称表
     */
    public Map<String, CmgBuildingName> nameMap = new HashMap<>();

    /**
     * 建筑物的3DLandMark模型表
     */
    private List<IRow> build3dmodels = new ArrayList<>();

    /**
     * 建筑物的3DLandMark模型表
     */
    public Map<String, CmgBuilding3dmodel> build3dmodelMap = new HashMap<>();

    /**
     * 建筑物的3DLandMark图标表
     */
    private List<IRow> build3dicons = new ArrayList<>();

    /**
     * 建筑物的3DLandMark图标表
     */
    public Map<String, CmgBuilding3dicon> build3diconMap = new HashMap<>();

    /**
     * 建筑物与POI关系表
     */
    private List<IRow> pois = new ArrayList<>();

    /**
     * 建筑物与POI关系表
     */
    public Map<String, CmgBuildingPoi> poiMap = new HashMap<>();

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
        return "PID";
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        Map<Class<? extends IRow>, List<IRow>> map = new HashMap<>();
        map.put(CmgBuildingName.class, this.names);
        map.put(CmgBuilding3dmodel.class, this.build3dmodels);
        map.put(CmgBuilding3dicon.class, this.build3dicons);
        map.put(CmgBuildingPoi.class, this.pois);
        return map;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        Map<Class<? extends IRow>, Map<String, ?>> map = new HashMap<>();
        map.put(CmgBuildingName.class, this.nameMap);
        map.put(CmgBuilding3dmodel.class, this.build3dmodelMap);
        map.put(CmgBuilding3dicon.class, this.build3diconMap);
        map.put(CmgBuildingPoi.class, this.poiMap);
        return map;
    }

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
        return "CMG_BUILDING";
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
        return ObjType.CMGBUILDING;
    }

    @Override
    public void copy(IRow row) {
        CmgBuilding cmgBuilding = (CmgBuilding) row;

        this.pid = cmgBuilding.pid;
        this.kind = cmgBuilding.kind;
        this.rowId = cmgBuilding.rowId;

        List<IRow> names = new ArrayList<>();
        for (IRow n : cmgBuilding.names) {
            CmgBuildingName name = new CmgBuildingName();
            name.copy(n);
            names.add(name);
        }
        this.names = names;

        List<IRow> build3dmodels = new ArrayList<>();
        for (IRow m : cmgBuilding.build3dmodels) {
            CmgBuilding3dmodel build3dmodel = new CmgBuilding3dmodel();
            build3dmodel.copy(m);
            build3dmodels.add(build3dmodel);
        }
        this.build3dmodels = build3dmodels;

        List<IRow> build3dicons = new ArrayList<>();
        for (IRow i : cmgBuilding.build3dicons) {
            CmgBuilding3dicon build3dicon = new CmgBuilding3dicon();
            build3dicon.copy(i);
            build3dicons.add(build3dicon);
        }
        this.build3dicons = build3dicons;

        List<IRow> pois = new ArrayList<>();
        for (IRow p : cmgBuilding.pois) {
            CmgBuildingPoi poi = new CmgBuildingPoi();
            poi.copy(p);
            pois.add(poi);
        }
        this.pois = pois;
    }

    @Override
    public Map<String, Object> changedFields() {
        return this.changedFields;
    }

    @Override
    public String parentPKName() {
        return "PID";
    }

    @Override
    public int parentPKValue() {
        return this.pid;
    }

    @Override
    public String parentTableName() {
        return "CMGbuildBUILDING";
    }

    @Override
    public List<List<IRow>> children() {
        List<List<IRow>> list = new ArrayList<>();
        list.add(this.names);
        list.add(this.build3dmodels);
        list.add(this.build3dicons);
        list.add(this.pois);
        return list;
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
     * Getter method for property <tt>kind</tt>.
     *
     * @return property value of kind
     */
    public String getKind() {
        return kind;
    }

    /**
     * Setter method for property <tt>kind</tt>.
     *
     * @param kind value to be assigned to property kind
     */
    public void setKind(String kind) {
        this.kind = kind;
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
