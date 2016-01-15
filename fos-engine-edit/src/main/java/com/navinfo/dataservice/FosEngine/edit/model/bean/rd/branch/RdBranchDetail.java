package com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.IObj;
import com.navinfo.dataservice.FosEngine.edit.model.IRow;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;
import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.commons.util.JsonUtils;

public class RdBranchDetail implements IObj {
	
	private int mesh;

	private int pid;

	private int branchPid;

	private int voiceDir;
	
	private int estabType;
	
	private int nameKind;
	
	private String exitNum;
	
	private int branchType;
	
	private String patternCode;
	
	private String arrowCode;
	
	private int arrowFlag;
	
	private int guideCode;
	
	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	private List<IRow> names = new ArrayList<IRow>();
	
	public Map<Integer, RdBranchName> nameMap = new HashMap<Integer, RdBranchName>();

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdBranchDetail() {

	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getArrowCode() {
		return arrowCode;
	}

	public void setArrowCode(String arrowCode) {
		this.arrowCode = arrowCode;
	}

	public int getVoiceDir() {
		return voiceDir;
	}

	public void setVoiceDir(int voiceDir) {
		this.voiceDir = voiceDir;
	}

	public int getEstabType() {
		return estabType;
	}

	public void setEstabType(int estabType) {
		this.estabType = estabType;
	}

	public int getNameKind() {
		return nameKind;
	}

	public void setNameKind(int nameKind) {
		this.nameKind = nameKind;
	}

	public String getExitNum() {
		return exitNum;
	}

	public void setExitNum(String exitNum) {
		this.exitNum = exitNum;
	}

	public int getBranchType() {
		return branchType;
	}

	public void setBranchType(int branchType) {
		this.branchType = branchType;
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

	public int getGuideCode() {
		return guideCode;
	}

	public void setGuideCode(int guideCode) {
		this.guideCode = guideCode;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
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

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "names":
					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdBranchName row = new RdBranchName();

						row.Unserialize(jo);

						names.add(row);
					}

					break;

				default:
					break;
				}

			} else {
				Field f = this.getClass().getDeclaredField(key);

				f.setAccessible(true);

				f.set(this, json.get(key));
			}
		}

		return true;
	}

	@Override
	public String tableName() {

		return "rd_branch_detail";
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

		return ObjType.RDBRANCHDETAIL;
	}


	@Override
	public void copy(IRow row) {

	}
	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String primaryKey() {

		return "branch_pid";
	}

	@Override
	public int primaryValue() {

		return this.getBranchPid();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String primaryTableName() {

		return "rd_branch";
	}

	@Override
	public String rowId() {

		return this.rowId;
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
						changedFields.put(key, json.get(key));
						
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
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		// TODO Auto-generated method stub
		return this.getPid();
	}

	@Override
	public int mesh() {
		// TODO Auto-generated method stub
		return mesh;
	}

	@Override
	public void setMesh(int mesh) {
		// TODO Auto-generated method stub
		this.mesh=mesh;
	}

}
