package com.navinfo.dataservice.dao.glm.model.cmg;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @Title: CmgBuildface
 * @Package: com.navinfo.dataservice.dao.glm.model.cmg
 * @Description: 建筑物FACE表
 * @Author: Crayeres
 * @Date: 2017/4/8
 * @Version: V1.0
 */
public class CmgBuildface implements IObj {

    /**
     * FACE号码
     */
    private int pid;

    /**
     * 建筑物号码
     */
    private int buildingPid;

    /**
     * 能否拔高
     */
    private int massing;

    /**
     * 建筑物高度
     */
    private double height;

    /**
     * 高度精确度
     */
    private double heightAcuracy = 0.5;

    /**
     * 高度值来源
     */
    private int heightSource = 1;

    /**
     * 数据来源
     */
    private int dataSource = 3;

    /**
     * 墙面材料
     */
    private int wallMaterial = 1;

    /**
     * FACE坐标
     */
    private Geometry geometry;

    /**
     * FACE面积
     */
    private double area;

    /**
     * FACE周长
     */
    private double perimeter;

    /**
     * 图幅号码
     */
    private int meshId;

    /**
     * 编辑标识
     */
    private int editFlag = 1;

    /**
     * 创建时间(格式"YYYY/MM/DD HH:mm:ss")
     */
    private Date createTime;

    /**
     * 行记录ID
     */
    private String rowId;

    /**
     * 建筑物租户信息表
     */
    private List<IRow> tenants = new ArrayList<>();

    /**
     * 建筑物租户信息表
     */
    public Map<String, CmgBuildfaceTenant> tenantMap = new HashMap<>();

    /**
     * 建筑物面的拓扑关系表
     */
    private List<IRow> topos = new ArrayList<>();

    /**
     * 建筑物面的拓扑关系表
     */
    public Map<String, CmgBuildfaceTopo> topoMap = new HashMap<>();

    /**
     * 待修改数据
     */
    private Map<String, Object> changedFields = new HashMap<>();

    /**
     * 数据状态
     */
    private ObjStatus status;

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
        return "FACE_PID";
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        Map<Class<? extends IRow>, List<IRow>> map = new HashMap<>();
        map.put(CmgBuildfaceTenant.class, this.tenants);
        map.put(CmgBuildfaceTopo.class, this.topos);
        return map;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        Map<Class<? extends IRow>, Map<String, ?>> map = new HashMap<>();
        map.put(CmgBuildfaceTenant.class, this.tenantMap);
        map.put(CmgBuildfaceTopo.class, this.topoMap);
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
        return "CMG_BUILDFACE";
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
        return ObjType.CMGBUILDFACE;
    }

    @Override
    public void copy(final IRow row) {
        CmgBuildface cmgBuildface = (CmgBuildface) row;

        this.buildingPid = cmgBuildface.buildingPid;
        this.massing = cmgBuildface.massing;
        this.height = cmgBuildface.height;
        this.heightAcuracy = cmgBuildface.heightAcuracy;
        this.heightSource = cmgBuildface.heightSource;
        this.dataSource = cmgBuildface.dataSource;
        this.wallMaterial = cmgBuildface.wallMaterial;
        this.geometry = cmgBuildface.geometry;
        this.area = cmgBuildface.area;
        this.perimeter = cmgBuildface.perimeter;
        this.meshId = cmgBuildface.meshId;
        this.editFlag = cmgBuildface.editFlag;
        this.createTime = cmgBuildface.createTime;
        this.rowId = cmgBuildface.rowId;

        List<IRow> tenants = new ArrayList<>();
        for (IRow t : cmgBuildface.tenants) {
            CmgBuildfaceTenant tenant = new CmgBuildfaceTenant();
            tenant.copy(t);
            tenant.setFacePid(this.pid());
            tenants.add(tenant);
        }
        this.tenants = tenants;

        List<IRow> topos = new ArrayList<>();
        for (IRow t : cmgBuildface.topos) {
            CmgBuildfaceTopo topo = new CmgBuildfaceTopo();
            topo.copy(t);
            topo.setFacePid(this.pid());
            topos.add(topo);
        }
        this.topos = topos;
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
        return this.pid;
    }

    @Override
    public String parentTableName() {
        return "CMG_BUILDFACE";
    }

    @Override
    public List<List<IRow>> children() {
        List<List<IRow>> list = new ArrayList<>();
        list.add(this.tenants);
        list.add(this.topos);
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
        return this.meshId;
    }

    @Override
    public void setMesh(final int mesh) {
        this.meshId = mesh;
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
     * Getter method for property <tt>massing</tt>.
     *
     * @return property value of massing
     */
    public int getMassing() {
        return massing;
    }

    /**
     * Setter method for property <tt>massing</tt>.
     *
     * @param massing value to be assigned to property massing
     */
    public void setMassing(int massing) {
        this.massing = massing;
    }

    /**
     * Getter method for property <tt>height</tt>.
     *
     * @return property value of height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Setter method for property <tt>height</tt>.
     *
     * @param height value to be assigned to property height
     */
    public void setHeight(double height) {
        this.height = height;
    }

    /**
     * Getter method for property <tt>heightAcuracy</tt>.
     *
     * @return property value of heightAcuracy
     */
    public double getHeightAcuracy() {
        return heightAcuracy;
    }

    /**
     * Setter method for property <tt>heightAcuracy</tt>.
     *
     * @param heightAcuracy value to be assigned to property heightAcuracy
     */
    public void setHeightAcuracy(double heightAcuracy) {
        this.heightAcuracy = heightAcuracy;
    }

    /**
     * Getter method for property <tt>heightSource</tt>.
     *
     * @return property value of heightSource
     */
    public int getHeightSource() {
        return heightSource;
    }

    /**
     * Setter method for property <tt>heightSource</tt>.
     *
     * @param heightSource value to be assigned to property heightSource
     */
    public void setHeightSource(int heightSource) {
        this.heightSource = heightSource;
    }

    /**
     * Getter method for property <tt>dataSource</tt>.
     *
     * @return property value of dataSource
     */
    public int getDataSource() {
        return dataSource;
    }

    /**
     * Setter method for property <tt>dataSource</tt>.
     *
     * @param dataSource value to be assigned to property dataSource
     */
    public void setDataSource(int dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Getter method for property <tt>wallMaterial</tt>.
     *
     * @return property value of wallMaterial
     */
    public int getWallMaterial() {
        return wallMaterial;
    }

    /**
     * Setter method for property <tt>wallMaterial</tt>.
     *
     * @param wallMaterial value to be assigned to property wallMaterial
     */
    public void setWallMaterial(int wallMaterial) {
        this.wallMaterial = wallMaterial;
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
     * Getter method for property <tt>area</tt>.
     *
     * @return property value of area
     */
    public double getArea() {
        return area;
    }

    /**
     * Setter method for property <tt>area</tt>.
     *
     * @param area value to be assigned to property area
     */
    public void setArea(double area) {
        this.area = area;
    }

    /**
     * Getter method for property <tt>perimeter</tt>.
     *
     * @return property value of perimeter
     */
    public double getPerimeter() {
        return perimeter;
    }

    /**
     * Setter method for property <tt>perimeter</tt>.
     *
     * @param perimeter value to be assigned to property perimeter
     */
    public void setPerimeter(double perimeter) {
        this.perimeter = perimeter;
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
     * Getter method for property <tt>createTime</tt>.
     *
     * @return property value of createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * Setter method for property <tt>createTime</tt>.
     *
     * @param createTime value to be assigned to property createTime
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
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
     * Getter method for property <tt>tenants</tt>.
     *
     * @return property value of tenants
     */
    public List<IRow> getTenants() {
        return tenants;
    }

    /**
     * Setter method for property <tt>tenants</tt>.
     *
     * @param tenants value to be assigned to property tenants
     */
    public void setTenants(List<IRow> tenants) {
        this.tenants = tenants;
    }

    /**
     * Getter method for property <tt>topos</tt>.
     *
     * @return property value of topos
     */
    public List<IRow> getTopos() {
        return topos;
    }

    /**
     * Setter method for property <tt>topos</tt>.
     *
     * @param topos value to be assigned to property topos
     */
    public void setTopos(List<IRow> topos) {
        this.topos = topos;
    }

    @Override
    public String toString() {
        return "CmgBuildface{" + "pid=" + pid + ", buildingPid=" + buildingPid + ", massing=" + massing + ", height=" + height + ", " +
                "heightAcuracy=" + heightAcuracy + ", heightSource=" + heightSource + ", dataSource=" + dataSource + ", wallMaterial=" +
                wallMaterial + ", geometry=" + geometry + ", area=" + area + ", perimeter=" + perimeter + ", meshId=" + meshId + ", " +
                "editFlag=" + editFlag + ", createTime=" + createTime + ", rowId='" + rowId + '\'' + ", tenants=" + tenants + ", " +
                "tenantMap=" + tenantMap + ", topos=" + topos + ", topoMap=" + topoMap + ", changedFields=" + changedFields + ", " +
                "status=" + status + '}';
    }
}
