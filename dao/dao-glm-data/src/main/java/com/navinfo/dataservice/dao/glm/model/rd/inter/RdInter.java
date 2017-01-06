/**
 * 
 */
package com.navinfo.dataservice.dao.glm.model.rd.inter;

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

/** 
* @ClassName: RdInter 
* @author Zhang Xiaolong
* @date 2016年8月3日 上午11:39:20 
* @Description: CRF交叉点模型
*/
public class RdInter implements IObj {
	
	private int pid;
	
	private String rowId;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	private List<IRow> links = new ArrayList<>();
	
	private List<IRow> nodes = new ArrayList<>();
	protected ObjStatus status;
	public Map<String, RdInterLink> linkMap = new HashMap<String, RdInterLink>();
	
	public Map<String, RdInterNode> nodeMap = new HashMap<String, RdInterNode>();
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public List<IRow> getLinks() {
		return links;
	}

	public void setLinks(List<IRow> links) {
		this.links = links;
	}

	public List<IRow> getNodes() {
		return nodes;
	}

	public void setNodes(List<IRow> nodes) {
		this.nodes = nodes;
	}

	public String getRowId() {
		return rowId;
	}

	@Override
	public String rowId() {
		return this.rowId;
	}

	@Override
	public void setRowId(String rowId) {
		this.rowId = rowId;
	}

	@Override
	public String tableName() {
		return "RD_INTER";
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
		return ObjType.RDINTER;
	}

	@Override
	public void copy(IRow row) {
		RdInter inter = (RdInter) row;

		this.pid = inter.pid;

		this.links = new ArrayList<IRow>();

		for (IRow r : inter.links) {

			RdInterLink link = new RdInterLink();

			link.copy(r);

			this.links.add(link);
		}

		this.nodes = new ArrayList<IRow>();

		for (IRow r : inter.nodes) {

			RdInterNode node = new RdInterNode();

			node.copy(r);

			this.nodes.add(node);
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
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "RD_INTER";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<>();

		children.add(this.nodes);

		children.add(this.links);

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

						if (value instanceof String) {
							changedFields.put(key, newValue.replace("'", "''"));
						} else {
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

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
//		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());
//
//		return json;
		JSONObject json = JSONObject.fromObject(this,JsonUtils.getStrConfig());
		
		if (objLevel == ObjLevel.HISTORY) {
			json.remove("status");
		}
		return json;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		@SuppressWarnings("rawtypes")
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

						RdInterLink row = new RdInterLink();

						row.Unserialize(jo);

						links.add(row);
					}

					break;
				case "nodes":
					nodes.clear();

					ja = json.getJSONArray(key);

					for (int i = 0; i < ja.size(); i++) {
						JSONObject jo = ja.getJSONObject(i);

						RdInterNode row = new RdInterNode();

						row.Unserialize(jo);

						nodes.add(row);
					}

					break;
				}
			} else if (!"objStatus".equals(key)) {

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
	public int pid() {
		return this.pid;
	}

	@Override
	public String primaryKey() {
		return "pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdInterNode.class, nodes);
		childList.put(RdInterLink.class, links);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdInterNode.class, nodeMap);
		childMap.put(RdInterLink.class, linkMap);
		return childMap;
	}

}
