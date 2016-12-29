package com.navinfo.dataservice.dao.glm.model.rd.cross;

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

public class RdCross implements IObj {
	
	private int pid;

	private int type;

	private int signal;

	private int electroeye;

	private int kgFlag;

	private List<IRow> links = new ArrayList<IRow>();

	private List<IRow> names = new ArrayList<IRow>();

	private List<IRow> nodes = new ArrayList<IRow>();

	private String rowId;
	
	protected ObjStatus status;

	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	public Map<String, RdCrossLink> linkMap = new HashMap<String, RdCrossLink>();
	
	public Map<String, RdCrossName> nameMap = new HashMap<String, RdCrossName>();
	
	public Map<String, RdCrossNode> nodeMap = new HashMap<String, RdCrossNode>();

	public RdCross() {

	}

	public List<IRow> getLinks() {
		return links;
	}

	public void setLinks(List<IRow> links) {
		this.links = links;
	}

	public List<IRow> getNames() {
		return names;
	}

	public void setNames(List<IRow> names) {
		this.names = names;
	}

	public List<IRow> getNodes() {
		return nodes;
	}

	public void setNodes(List<IRow> nodes) {
		this.nodes = nodes;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSignal() {
		return signal;
	}

	public void setSignal(int signal) {
		this.signal = signal;
	}

	public int getElectroeye() {
		return electroeye;
	}

	public void setElectroeye(int electroeye) {
		this.electroeye = electroeye;
	}

	public int getKgFlag() {
		return kgFlag;
	}

	public void setKgFlag(int kgFlag) {
		this.kgFlag = kgFlag;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public String tableName() {

		return "rd_cross";
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

		return ObjType.RDCROSS;
	}

	@Override
	public void copy(IRow row) {

		RdCross cross = (RdCross) row;

		this.type = cross.type;

		this.signal = cross.signal;

		this.electroeye = cross.electroeye;

		this.kgFlag = cross.kgFlag;

		this.links = new ArrayList<IRow>();

		for (IRow r : cross.links) {

			RdCrossLink rCopy = new RdCrossLink();

			rCopy.setPid(this.getPid());

			rCopy.copy(r);

			this.links.add(rCopy);
		}

		this.nodes = new ArrayList<IRow>();

		for (IRow r : cross.nodes) {

			RdCrossNode rCopy = new RdCrossNode();

			rCopy.setPid(this.getPid());

			rCopy.copy(r);

			this.nodes.add(rCopy);
		}

		this.names = new ArrayList<IRow>();

		for (IRow r : cross.names) {

			RdCrossName rCopy = new RdCrossName();

			rCopy.setPid(this.getPid());

			rCopy.copy(r);

			this.names.add(rCopy);
		}
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) {

//		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
		JSONObject json = JSONObject.fromObject(this,JsonUtils.getStrConfig());
		
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}
		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {


		Iterator keys = json.keys();

		while (keys.hasNext()) {
			String key = (String) keys.next();

			JSONArray ja = null;

			if (json.get(key) instanceof JSONArray) {

				switch (key) {
				case "links":
					links.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdCrossLink row = new RdCrossLink();

						row.Unserialize(jo);

						links.add(row);
					}

					break;
				case "names":

					names.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdCrossName row = new RdCrossName();

						row.Unserialize(jo);

						names.add(row);
					}

					break;

				case "nodes":

					nodes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdCrossNode row = new RdCrossNode();

						row.Unserialize(jo);

						nodes.add(row);
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
	public List<IRow> relatedRows() {

		return null;
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

		children.add(this.getLinks());

		children.add(this.getNames());

		children.add(this.getNodes());

		return children;
	}

	@Override
	public String parentTableName() {

		return "rd_cross";
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
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdCrossName.class, names);
		childList.put(RdCrossNode.class, nodes);
		childList.put(RdCrossLink.class, links);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdCrossName.class, nameMap);
		childMap.put(RdCrossNode.class, nodeMap);
		childMap.put(RdCrossLink.class, linkMap);
		return childMap;
	}

}