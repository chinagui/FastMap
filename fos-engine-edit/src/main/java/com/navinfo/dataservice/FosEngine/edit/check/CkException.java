package com.navinfo.dataservice.FosEngine.edit.check;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ISerializable;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;

public class CkException implements ISerializable {

	private int exceptionId;

	private String ruleId;

	private String taskName;

	private int status;

	private int groupId;

	private int rank;

	private String situation;

	private String information;

	private String suggestion;

	private String geometry;

	private String targets;

	private String additionInfo;

	private String memo;

	private String createDate;

	private String updateDate;

	private int meshId;

	private int scopeFlag=1;

	private String provinceName;

	private int mapScale;

	private String reserved;

	private String extended;

	private String taskId;

	private String qaTaskId;

	private int qaStatus=2;

	private String worker;

	private String qaWorker;

	private String memo1;

	private String memo2;

	private String memo3;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public CkException() {

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		if (objLevel == ObjLevel.FULL) {
			JSONObject json = JSONObject.fromObject(this);

			return json;
		} else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();

			json.put("situation", this.situation);

			json.put("status", this.status);

			json.put("rank", this.rank);

			json.put("geometry", this.geometry);

			json.put("information", this.information);

			json.put("targets", this.targets);

			json.put("createDate", this.createDate);

			json.put("updateDate", this.updateDate);

			json.put("worker", this.worker);

			json.put("qaWorker", this.qaWorker);

			json.put("qaStatus", this.qaStatus);

			json.put("ruleId", this.ruleId);

			return json;
		}
		return null;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		return false;
	}

	public int getExceptionId() {
		return exceptionId;
	}

	public void setExceptionId(int exceptionId) {
		this.exceptionId = exceptionId;
	}

	public String getRuleId() {
		return ruleId;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
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

	public String getGeometry() {
		return geometry;
	}

	public void setGeometry(String geometry) {
		this.geometry = geometry;
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

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
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

	public String getMemo1() {
		return memo1;
	}

	public void setMemo1(String memo1) {
		this.memo1 = memo1;
	}

	public String getMemo2() {
		return memo2;
	}

	public void setMemo2(String memo2) {
		this.memo2 = memo2;
	}

	public String getMemo3() {
		return memo3;
	}

	public void setMemo3(String memo3) {
		this.memo3 = memo3;
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
}
