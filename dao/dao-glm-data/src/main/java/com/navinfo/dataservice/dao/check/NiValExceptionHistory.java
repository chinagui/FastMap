package com.navinfo.dataservice.dao.check;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.vividsolutions.jts.geom.Geometry;

public class NiValExceptionHistory implements IObj {

	private int valExceptionId;

	private String ruleid;

	private String taskName;

	private int groupid;

	private int level;

	private String situation;

	private String information;

	private String suggestion;

	private Geometry location;

	private String targets;

	private String additionInfo;

	private int delFlag;

	private String created;

	private String updated;

	private int meshId;

	private int scopeFlag = 1;

	private String provinceName;

	private int mapScale;

	private String reserved;

	private String extended;

	private String taskId;

	private String qaTaskId;

	private int qaStatus = 2;

	private String worker;

	private String qaWorker;

	private int logType;

	private String md5Code;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public NiValExceptionHistory() {

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {
			JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

			JSONObject json = JSONObject.fromObject(this, jsonConfig);

			return json;
		} else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();

			json.put("location",
					GeoTranslator.jts2Geojson(location, 0.00001, 5));

			return json;
		}
		return null;

	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		return false;
	}

	public String getRuleid() {
		return ruleid;
	}

	public void setRuleid(String ruleId) {
		this.ruleid = ruleId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getGroupid() {
		return groupid;
	}

	public void setGroupid(int groupId) {
		this.groupid = groupId;
	}

	public String getSituation() {
		return situation;
	}

	public void setSituation(String situation) {
		this.situation = situation;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

	public String getTargets() {
		return targets;
	}

	public void setTargets(String targets) {
		this.targets = targets;
	}

	public String getAdditionInfo() {
		return additionInfo;
	}

	public void setAdditionInfo(String additionInfo) {
		this.additionInfo = additionInfo;
	}

	public int getMeshId() {
		return meshId;
	}

	public void setMeshId(int meshId) {
		this.meshId = meshId;
	}

	public int getScopeFlag() {
		return scopeFlag;
	}

	public void setScopeFlag(int scopeFlag) {
		this.scopeFlag = scopeFlag;
	}

	public String getProvinceName() {
		return provinceName;
	}

	public void setProvinceName(String provinceName) {
		this.provinceName = provinceName;
	}

	public int getMapScale() {
		return mapScale;
	}

	public void setMapScale(int mapScale) {
		this.mapScale = mapScale;
	}

	public String getReserved() {
		return reserved;
	}

	public void setReserved(String reserved) {
		this.reserved = reserved;
	}

	public String getExtended() {
		return extended;
	}

	public void setExtended(String extended) {
		this.extended = extended;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public String getQaTaskId() {
		return qaTaskId;
	}

	public void setQaTaskId(String qaTaskId) {
		this.qaTaskId = qaTaskId;
	}

	public int getQaStatus() {
		return qaStatus;
	}

	public void setQaStatus(int qaStatus) {
		this.qaStatus = qaStatus;
	}

	public String getWorker() {
		return worker;
	}

	public void setWorker(String worker) {
		this.worker = worker;
	}

	public String getQaWorker() {
		return qaWorker;
	}

	public void setQaWorker(String qaWorker) {
		this.qaWorker = qaWorker;
	}

	public Map<String, Object> changedFields() {

		return changedFields;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public Geometry getLocation() {
		return location;
	}

	public void setLocation(Geometry location) {
		this.location = location;
	}

	public int getDelFlag() {
		return delFlag;
	}

	public void setDelFlag(int delFlag) {
		this.delFlag = delFlag;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getUpdated() {
		return updated;
	}

	public void setUpdated(String updated) {
		this.updated = updated;
	}

	public int getLogType() {
		return logType;
	}

	public void setLogType(int logType) {
		this.logType = logType;
	}

	public int getValExceptionId() {
		return valExceptionId;
	}

	public void setValExceptionId(int valExceptionId) {
		this.valExceptionId = valExceptionId;
	}

	@Override
	public String tableName() {

		return "ni_val_exception";
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

		return null;
	}

	@Override
	public void copy(IRow row) {

	}

	@Override
	public String parentPKName() {

		return "val_exception_id";
	}

	@Override
	public int parentPKValue() {

		return valExceptionId;
	}

	@Override
	public String parentTableName() {

		return "ni_val_exception";
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		return false;
	}

	@Override
	public int mesh() {

		return this.meshId;
	}

	@Override
	public void setMesh(int mesh) {
		this.meshId = mesh;

	}

	@Override
	public List<IRow> relatedRows() {

		return null;
	}

	@Override
	public int pid() {
		return this.valExceptionId;
	}

	@Override
	public String primaryKey() {
		return "val_exception_id";
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

	public String getMd5Code() {
		return md5Code;
	}

	public void setMd5Code(String md5Code) {
		this.md5Code = md5Code;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IRow#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.dao.glm.iface.IObj#childMap()
	 */
	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		// TODO Auto-generated method stub
		return null;
	}

}
