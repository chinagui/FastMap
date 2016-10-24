package com.navinfo.dataservice.dao.glm.model.rd.rw;

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
 * 铁路:要素表
 * @author zhangxiaolong
 *
 */
public class RwFeature implements IObj {

	private int pid;

	private String rowId;

	private Map<String, Object> changedFields = new HashMap<String, Object>();

	private List<IRow> links = new ArrayList<IRow>();

	public Map<String, RwLink> rwLinkMap = new HashMap<String, RwLink>();

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getRowId() {
		return rowId;
	}
	
	public List<IRow> getLinks() {
		return links;
	}

	public void setLinks(List<IRow> links) {
		this.links = links;
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
	public String tableName() {
		return "rw_feature";
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
		return ObjType.RWFEATURE;
	}

	@Override
	public void copy(IRow row) {
		RwFeature sourceLink = (RwFeature) row;
		
		this.setPid(sourceLink.getPid());
		
		this.setRowId(sourceLink.getRowId());
		
		List<IRow> rwLinkSources = sourceLink.getLinks();

		List<IRow> rwLinks = new ArrayList<IRow>();

		for (IRow fs : rwLinkSources) {

			RwLink sourceRw = (RwLink) fs;
			
			RwLink f = new RwLink();

			f.copy(fs);
			
			f.setPid(sourceRw.getPid());

			rwLinks.add(f);
		}
		
		this.setLinks(rwLinks);
	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "feature_pid";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return this.tableName();
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<List<IRow>>();

		children.add(links);

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
		JSONObject json = JSONObject.fromObject(this, JsonUtils.getStrConfig());

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

						RwLink row = new RwLink();

						row.Unserialize(jo);

						links.add(row);
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
		return "feature_pid";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RwLink.class, links);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RwLink.class,rwLinkMap);
		return childMap;
	}

}
