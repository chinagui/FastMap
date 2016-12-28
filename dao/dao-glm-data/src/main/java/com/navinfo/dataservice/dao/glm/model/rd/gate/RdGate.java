package com.navinfo.dataservice.dao.glm.model.rd.gate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdGate implements IObj {
	
	private int pid;
	private int inLinkPid;
	private int nodePid;
	private int outLinkPid;
	private int type = 2;
	private int dir = 2;
	private int fee = 0;
	private String rowId;
	public Map<String, Object> changedFields = new HashMap<String, Object>();
	private List<IRow> condition = new ArrayList<IRow>();
	public Map<String, RdGateCondition> rdGateConditionMap = new HashMap<String, RdGateCondition>();
	
	protected ObjStatus status;


	@Override
	public String rowId() {
		return rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
		
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

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getDir() {
		return dir;
	}

	public void setDir(int dir) {
		this.dir = dir;
	}

	public int getFee() {
		return fee;
	}

	public void setFee(int fee) {
		this.fee = fee;
	}

	public List<IRow> getCondition() {
		return condition;
	}

	public void setCondition(List<IRow> condition) {
		this.condition = condition;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public String tableName() {
		return "rd_gate";
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
		return ObjType.RDGATE;
	}

	@Override
	public void copy(IRow row) {
		RdGate node = (RdGate) row;
		this.inLinkPid = node.getInLinkPid();
		this.nodePid = node.getNodePid();
		this.outLinkPid = node.getOutLinkPid();
		this.type = node.getType();
		this.dir = node.getDir();
		this.fee = node.getFee();
		this.condition = new ArrayList<IRow>();
		for(IRow form :node.getCondition()){
			RdGateCondition formCopy = new RdGateCondition();
			formCopy.copy(form);
			formCopy.setPid(this.pid());
			this.condition.add(formCopy);
		}
		
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "pid";
	}

	@Override
	public int parentPKValue() {
		return pid;
	}

	@Override
	public String parentTableName() {
		return "rd_gate";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();
		children.add(this.getCondition());
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
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
//		JSONObject json = JSONObject.fromObject(this);

		JSONObject json = JSONObject.fromObject(this,JsonUtils.getStrConfig());
		
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}
		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		return false;
	}

	@Override
	public List<IRow> relatedRows() {
		return null;
	}

	@Override
	public int pid() {
		return pid;
	}

	@Override
	public String primaryKey() {
		return "pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdGateCondition.class, condition);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdGateCondition.class, rdGateConditionMap);
		return childMap;
	}

}
