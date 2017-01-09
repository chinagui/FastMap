package com.navinfo.dataservice.dao.glm.model.rd.link;

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

public class RdLinkName implements IRow {

	private String rowId;

	private int seqNum = 1;

	private int nameType;

	private int nameClass = 1;

	private int linkPid;

	private int nameGroupid;

	private String inputTime;

	private int srcFlag = 9;

	private int routeAtt;

	private int code;
	
	protected String name;
	protected ObjStatus status;

	public String getInputTime() {
		return inputTime;
	}

	public void setInputTime(String inputTime) {
		this.inputTime = inputTime;
	}

	public int getSrcFlag() {
		return srcFlag;
	}

	public void setSrcFlag(int srcFlag) {
		this.srcFlag = srcFlag;
	}

	public int getRouteAtt() {
		return routeAtt;
	}

	public void setRouteAtt(int routeAtt) {
		this.routeAtt = routeAtt;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	public int getNameGroupid() {
		return nameGroupid;
	}

	public void setNameGroupid(int nameGroupid) {
		this.nameGroupid = nameGroupid;
	}

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public RdLinkName() {

	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public int getNameType() {
		return nameType;
	}

	public void setNameType(int nameType) {
		this.nameType = nameType;
	}

	public int getNameClass() {
		return nameClass;
	}

	public void setNameClass(int nameClass) {
		this.nameClass = nameClass;
	}

	public String getRowId() {
		return rowId;
	}

	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

		if (objLevel == ObjLevel.HISTORY) {
			json.remove("name");
			json.remove("status");
		}

		return json;
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

		return "rd_link_name";
	}

	@Override
	public ObjStatus status() {

		return status;
	}

	@Override
	public void setStatus(ObjStatus os) {
		status = os;
	}

	@Override
	public ObjType objType() {

		return ObjType.RDLINKNAME;
	}

	@Override
	public void copy(IRow row) {

		RdLinkName nameSource = (RdLinkName) row;

		this.setNameGroupid(nameSource.getNameGroupid());
		
		this.setSeqNum(nameSource.getSeqNum());
		
		this.setNameClass(nameSource.getNameClass());
		
		this.setInputTime(nameSource.getInputTime());

		this.setNameType(nameSource.getNameType());
		
		this.setSrcFlag(nameSource.getSrcFlag());
		
		this.setRouteAtt(nameSource.getRouteAtt());
		
		this.setCode(nameSource.getCode());

		this.setRowId(nameSource.getRowId());		

		this.setMesh(row.mesh());
		
		this.name = nameSource.getName();
	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public String parentPKName() {

		return "link_pid";
	}

	@Override
	public int parentPKValue() {

		return this.getLinkPid();
	}

	@Override
	public List<List<IRow>> children() {

		return null;
	}

	@Override
	public String parentTableName() {

		return "rd_link";
	}

	@Override
	public String rowId() {

		return rowId;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if ("name".equals(key)) {
				continue;
			}

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
}
