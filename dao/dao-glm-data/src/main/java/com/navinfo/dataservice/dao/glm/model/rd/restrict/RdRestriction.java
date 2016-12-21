package com.navinfo.dataservice.dao.glm.model.rd.restrict;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossName;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;

public class RdRestriction implements IObj {
	
	private int pid;

	private int inLinkPid;

	private int nodePid;

	private String restricInfo;

	private int kgFlag;

	private List<IRow> details = new ArrayList<IRow>();

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	public Map<Integer,RdRestrictionDetail> detailMap = new HashMap<Integer,RdRestrictionDetail>();
	
	public Map<String,RdRestrictionCondition> conditionMap = new HashMap<String,RdRestrictionCondition>();

	public RdRestriction() {

	}
	
	public String getRowId() {
		return rowId;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public String getRestricInfo() {
		return restricInfo;
	}

	public void setRestricInfo(String restricInfo) {
		this.restricInfo = restricInfo;
	}

	public int getKgFlag() {
		return kgFlag;
	}

	public void setKgFlag(int kgFlag) {
		this.kgFlag = kgFlag;
	}

	@Override
	public String tableName() {

		return "rd_restriction";
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

		return ObjType.RDRESTRICTION;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {
		
		return JSONObject.fromObject(this);
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "details":
					details.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdRestrictionDetail row = new RdRestrictionDetail();

						row.Unserialize(jo);

						details.add(row);
					}

					break;

				default:
					break;
				}

			} else {
				if (!"objStatus".equals(key)) {
					Field f = this.getClass().getDeclaredField(key);
					f.setAccessible(true);
					f.set(this, json.get(key));
				}
			}
		}

		return true;
	}

	@Override
	public List<IRow> relatedRows() {

		return null;
	}

	public List<IRow> getDetails() {
		return details;
	}

	public void setDetails(List<IRow> details) {
		this.details = details;
		
		
	}

	@Override
	public void copy(IRow row) {

		RdRestriction restrict = (RdRestriction) row;

		this.inLinkPid = restrict.inLinkPid;

		this.nodePid = restrict.nodePid;

		this.restricInfo = restrict.restricInfo;

		this.kgFlag = restrict.kgFlag;

		this.details = new ArrayList<IRow>();
		
		for (IRow detail : restrict.details) {

			RdRestrictionDetail detailCopy = new RdRestrictionDetail();

			detailCopy.setRestricPid(this.getPid());

			detailCopy.copy(detail);

			this.details.add(detailCopy);
		}

	}

	@Override
	public Map<String, Object> changedFields() {

		return changedFields;
	}

	@Override
	public int pid() {

		return this.getPid();
	}

	@Override
	public String parentPKName() {

		return "pid";
	}

	@Override
	public int parentPKValue() {

		return this.getPid();
	}

	@Override
	public List<List<IRow>> children() {

		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(this.getDetails());

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_restriction";
	}

	@Override
	public String rowId() {

		return rowId;
	}

	@Override
	public void setRowId(String rowId) {

		this.rowId = rowId;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		
		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			if (!"restricInfo".equals(key) && json.get(key) instanceof JSONArray) {
				continue;
			}  else {
				if ( !"objStatus".equals(key)) {
					
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

	@Override
	public String primaryKey() {
		return "pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		return null;
	}
}
