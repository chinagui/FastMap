package com.navinfo.dataservice.dao.glm.model.lu;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNodeMesh;
import com.vividsolutions.jts.geom.Geometry;

public class LuLink implements IObj {

    private int pid;

    private String rowId;

    private int sNodePid;

    private int eNodePid;

    private Geometry geometry;

    private double length;

    private int editFlag = 1;

    private List<IRow> linkKinds = new ArrayList<IRow>();

    public Map<String, LuLinkKind> linkKindMap = new HashMap<String, LuLinkKind>();

    private List<IRow> meshes = new ArrayList<IRow>();

    public Map<String, LuLinkMesh> meshMap = new HashMap<String, LuLinkMesh>();

    private Map<String, Object> changedFields = new HashMap<String, Object>();

    public LuLink() {
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
        return "lu_link";
    }

    @Override
    public ObjStatus status() {
        return null;
    }

    @Override
    public void setStatus(ObjStatus os) {
    }

    @Override
    public ObjType objType() {
        return ObjType.LULINK;
    }

    @Override
    public void copy(IRow row) {
        LuLink sourceLink = (LuLink) row;
        this.eNodePid = sourceLink.eNodePid;
        this.geometry = sourceLink.geometry;
        this.length = sourceLink.length;
        this.rowId = sourceLink.rowId;
        this.sNodePid = sourceLink.sNodePid;
        this.meshes = new ArrayList<IRow>();
        for (IRow mesh : sourceLink.meshes) {
            LuLinkMesh linkMesh = new LuLinkMesh();
            linkMesh.copy(mesh);
            linkMesh.setLinkPid(this.pid);
            this.meshes.add(linkMesh);
        }
        for (IRow kind : sourceLink.linkKinds) {
            LuLinkKind linkKind = new LuLinkKind();
            linkKind.copy(kind);
            linkKind.setLinkPid(this.pid());
            this.linkKinds.add(linkKind);
        }
    }

    @Override
    public Map<String, Object> changedFields() {
        return this.changedFields;
    }

    @Override
    public String parentPKName() {
        return "link_pid";
    }

    @Override
    public int parentPKValue() {
        return this.pid;
    }

    @Override
    public String parentTableName() {
        return "lu_link";
    }

    @Override
    public List<List<IRow>> children() {
        List<List<IRow>> children = new ArrayList<List<IRow>>();
        children.add(this.meshes);
        children.add(this.linkKinds);
        return children;
    }

    @Override
    public boolean fillChangeFields(JSONObject json) throws Exception {
        @SuppressWarnings("rawtypes")
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

        if (changedFields.size() > 0) {
            return true;
        } else {
            return false;
        }
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
        JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

        JSONObject json = JSONObject.fromObject(this, jsonConfig);

        return json;
    }

    @Override
    public boolean Unserialize(JSONObject json) throws Exception {
        @SuppressWarnings("rawtypes")
        Iterator keys = json.keys();

        while (keys.hasNext()) {
            String key = (String) keys.next();

            JSONArray ja = null;

            if (json.get(key) instanceof JSONArray) {

                switch (key) {
                    case "meshes":

                        meshes.clear();

                        ja = json.getJSONArray(key);

                        for (int i = 0; i < ja.size(); i++) {
                            JSONObject jo = ja.getJSONObject(i);

                            RdNodeMesh row = new RdNodeMesh();

                            row.Unserialize(jo);

                            meshes.add(row);
                        }
                        break;
                    default:
                        break;
                }

            } else if ("geometry".equals(key)) {

                Geometry jts = GeoTranslator.geojson2Jts(json.getJSONObject(key), 100000, 0);

                this.setGeometry(jts);

            } else {
                Field f = this.getClass().getDeclaredField(key);

                f.setAccessible(true);

                f.set(this, json.get(key));
            }
        }

        return true;
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
        return "link_pid";
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getsNodePid() {
        return sNodePid;
    }

    public void setsNodePid(int sNodePid) {
        this.sNodePid = sNodePid;
    }

    public int geteNodePid() {
        return eNodePid;
    }

    public void seteNodePid(int eNodePid) {
        this.eNodePid = eNodePid;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public List<IRow> getMeshes() {
        return meshes;
    }

    public void setMeshes(List<IRow> meshes) {
        this.meshes = meshes;
    }

    public String getRowId() {
        return rowId;
    }

    public int getEditFlag() {
        return editFlag;
    }

    public void setEditFlag(int editFlag) {
        this.editFlag = editFlag;
    }

    public List<IRow> getLinkKinds() {
        return linkKinds;
    }

    public void setLinkKinds(List<IRow> linkKinds) {
        this.linkKinds = linkKinds;
    }

    @Override
    public Map<Class<? extends IRow>, List<IRow>> childList() {
        Map<Class<? extends IRow>, List<IRow>> childMap = new HashMap<>();
        childMap.put(LuLinkKind.class, linkKinds);
        childMap.put(LuLinkMesh.class, meshes);
        return childMap;
    }

    @Override
    public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
        Map<Class<? extends IRow>, Map<String, ?>> childMap = new HashMap<Class<? extends IRow>, Map<String, ?>>();
        childMap.put(LuLinkKind.class, linkKindMap);
        childMap.put(LuLinkMesh.class, meshMap);
        return childMap;
    }

}
