package com.navinfo.dataservice.dao.glm.model.rd.branch;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class RdSeriesbranch implements IRow {

	private int branchPid;
	
	private int type;
	
	private int voiceDir;
	
	private String patternCode;
	
	private String arrowCode;
	
	private int arrowFlag;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	protected  ObjStatus status;

	public String getRowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdSeriesbranch() {

	}

	public String getArrowCode() {
		return arrowCode;
	}

	public void setArrowCode(String arrowCode) {
		this.arrowCode = arrowCode;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getVoiceDir() {
		return voiceDir;
	}

	public void setVoiceDir(int voiceDir) {
		this.voiceDir = voiceDir;
	}

	public String getPatternCode() {
		return patternCode;
	}

	public void setPatternCode(String patternCode) {
		this.patternCode = patternCode;
	}

	public int getArrowFlag() {
		return arrowFlag;
	}

	public void setArrowFlag(int arrowFlag) {
		this.arrowFlag = arrowFlag;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
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

	@Override
	public String tableName() {

		return "rd_seriesbranch";
	}

	@Override
	public ObjStatus status() {

		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status=os;
	}

	@Override
	public ObjType objType() {

		return ObjType.RDSERIESBRANCH;
	}


	@Override
	public void copy(IRow row) {

	}
	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "branch_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getBranchPid();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_branch";
	}

	@Override
	public String rowId() {

		return this.getRowId();
	}

	public int getBranchPid() {
		return branchPid;
	}

	public void setBranchPid(int branchPid) {
		this.branchPid = branchPid;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {
				continue;
			}  else {
				if (!"objStatus".equals(key)) {
					
					Field field = this.getClass().getDeclaredField(key);
					
					field.setAccessible(true);
					
					Object objValue = field.get(this);
					
					String oldValue = null;
					
					if (objValue == null){
						oldValue = "null";
					}else{
						oldValue = String.valueOf(objValue);
					}
					
					String newValue = json.getString(key);
					
					if (!newValue.equals(oldValue)){
						Object value = json.get(key);
						
						if(value instanceof String){
							changedFields.put(key, newValue.replace("'", "''"));
						}
						else{
							changedFields.put(key, value);
						}

					}

					
				}
			}
		}
		
		if (changedFields.size() >0){
			return true;
		}else{
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

}
