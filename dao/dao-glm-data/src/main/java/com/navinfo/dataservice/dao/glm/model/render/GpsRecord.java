package com.navinfo.dataservice.dao.glm.model.render;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import java.util.List;
import java.util.Map;

/**
 * @Title: GpsRecord
 * @Package: com.navinfo.dataservice.dao.glm.model.render
 * @Description: 测线
 * @Author: zl
 * @Date: 2017/6/27
 */
public class GpsRecord implements IObj {

    /**
     * 测线号码
     */
    private int gpsrecordId;

    /**
     * 标记号码
     */
    private int markId;

    /**
     * 坐标
     */
    private Geometry geometry;

    /**
     * 测线来源
     */
    private int source;

    /**
     * 道路名称
     */
    private String name;

    private String tableName;
    
    private int laneNum;
    
    private int kind;
    
    private int meshId;
    
    private String memo;
    
    private int fieldSource;

	public int getGpsrecordId() {
		return gpsrecordId;
	}

	public void setGpsrecordId(int gpsrecordId) {
		this.gpsrecordId = gpsrecordId;
	}

	public int getMarkId() {
		return markId;
	}

	public void setMarkId(int markId) {
		this.markId = markId;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

	public int getKind() {
		return kind;
	}

	public void setKind(int kind) {
		this.kind = kind;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public int getFieldSource() {
		return fieldSource;
	}

	public void setFieldSource(int fieldSource) {
		this.fieldSource = fieldSource;
	}

	@Override
	public String rowId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRowId(String rowId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String tableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ObjStatus status() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setStatus(ObjStatus os) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ObjType objType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void copy(IRow row) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> changedFields() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String parentPKName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int parentPKValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String parentTableName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<List<IRow>> children() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int mesh() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String primaryKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}
    
    
    
    

   
}
