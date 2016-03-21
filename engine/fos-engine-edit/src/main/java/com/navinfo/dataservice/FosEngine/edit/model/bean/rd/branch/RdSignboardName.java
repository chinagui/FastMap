package com.navinfo.dataservice.FosEngine.edit.model.bean.rd.branch;

import java.lang.reflect.Field;
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

public class RdSignboardName implements IObj {

	private int mesh;
	
	private int pid;

	private int seqNum=1;

	private int nameGroupid=1;
	
	private int signboardId;
	
	private int nameClass;
	
	private String langCode;
	
	private int codeType;
	
	private String name;
	
	private String phonetic;
	
	private int srcFlag;
	
	private String voiceFile;
	
	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	public RdSignboardName() {

	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public int getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(int nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public int getSignboardId() {
		return signboardId;
	}

	public void setSignboardId(int signboardId) {
		this.signboardId = signboardId;
	}

	public int getNameClass() {
		return nameClass;
	}

	public void setNameClass(int nameClass) {
		this.nameClass = nameClass;
	}

	public String getLangCode() {
		return langCode;
	}

	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}

	public int getCodeType() {
		return codeType;
	}

	public void setCodeType(int codeType) {
		this.codeType = codeType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhonetic() {
		return phonetic;
	}

	public void setPhonetic(String phonetic) {
		this.phonetic = phonetic;
	}

	public int getSrcFlag() {
		return srcFlag;
	}

	public void setSrcFlag(int srcFlag) {
		this.srcFlag = srcFlag;
	}

	public String getVoiceFile() {
		return voiceFile;
	}

	public void setVoiceFile(String voiceFile) {
		this.voiceFile = voiceFile;
	}

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

		return "rd_signboard_name";
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

		return ObjType.RDSIGNBOARDNAME;
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

		return "signboard_id";
	}

	@Override
	public int parentPKValue() {

		return this.getSignboardId();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_signboard";
	}

	@Override
	public String rowId() {

		return this.rowId;
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
	public List<IRow> relatedRows() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int pid() {
		// TODO Auto-generated method stub
		return pid;
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

	@Override
	public String primaryKey() {
		// TODO Auto-generated method stub
		return "name_id";
	}

}
