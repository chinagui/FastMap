package com.navinfo.dataservice.dao.glm.model.rd.laneconnexity;

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

public class RdLaneConnexity implements IObj {
	
	private int pid;

	private String rowId;
	
	private int inLinkPid;
	
	private int nodePid;
	
	private String laneInfo;
	
	private int conflictFlag;
	
	private int kgFlag;
	
	private int laneNum;
	
	private int leftExtend;
	
	private int rightExtend;
	
	private List<IRow> topos = new ArrayList<IRow>();

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	public Map<Integer,RdLaneTopology> topologyMap = new HashMap<Integer,RdLaneTopology>();
	
	public Map<String,RdLaneVia> viaMap = new HashMap<String,RdLaneVia>();

	public RdLaneConnexity() {

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

	public int getKgFlag() {
		return kgFlag;
	}

	public void setKgFlag(int kgFlag) {
		this.kgFlag = kgFlag;
	}

	public String getLaneInfo() {
		return laneInfo;
	}

	public void setLaneInfo(String laneInfo) {
		this.laneInfo = laneInfo;
	}

	public int getConflictFlag() {
		return conflictFlag;
	}

	public void setConflictFlag(int conflictFlag) {
		this.conflictFlag = conflictFlag;
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}

	public int getLeftExtend() {
		return leftExtend;
	}

	public void setLeftExtend(int leftExtend) {
		this.leftExtend = leftExtend;
	}

	public int getRightExtend() {
		return rightExtend;
	}

	public void setRightExtend(int rightExtend) {
		this.rightExtend = rightExtend;
	}

	public List<IRow> getTopos() {
		return topos;
	}

	public void setTopos(List<IRow> topos) {
		this.topos = topos;
	}

	@Override
	public String tableName() {

		return "rd_lane_connexity";
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

		return ObjType.RDLANECONNEXITY;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {
		return JSONObject.fromObject(this);	
//		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "topos":
					topos.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdLaneTopology row = new RdLaneTopology();

						row.Unserialize(jo);

						topos.add(row);
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

	@Override
	public void copy(IRow row) {

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

		children.add(this.getTopos());

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_lane_connexity";
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
