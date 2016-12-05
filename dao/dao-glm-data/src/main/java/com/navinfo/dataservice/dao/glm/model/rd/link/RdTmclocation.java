/**
 * 
 */
package com.navinfo.dataservice.dao.glm.model.rd.link;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
* @ClassName: RdTmclocation 
* @author Zhang Xiaolong
* @date 2016年11月12日 下午4:21:25 
* @Description: TODO
*/
public class RdTmclocation implements IObj {

	private int pid;
	
	private int tmcId;
	
	private int loctableId;
	
	private String rowId;
	
	private Map<String, Object> changedFields = new HashMap<String, Object>();
	
	private List<IRow> links = new ArrayList<>();
	
	public Map<String, RdTmclocationLink> linkMap = new HashMap<String, RdTmclocationLink>();
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public List<IRow> getLinks() {
		return links;
	}
	
	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public int getLoctableId() {
		return loctableId;
	}

	public void setLoctableId(int loctableId) {
		this.loctableId = loctableId;
	}

	public void setLinks(List<IRow> links) {
		this.links = links;
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
		return "RD_TMCLOCATION";
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
		return ObjType.RDTMCLOCATION;
	}

	@Override
	public void copy(IRow row) {
		RdTmclocation tmclocation = (RdTmclocation) row;

		this.tmcId = tmclocation.tmcId;
		
		this.loctableId = tmclocation.loctableId;

		this.rowId = tmclocation.rowId;
		
		this.links = new ArrayList<IRow>();
		
		for (IRow r : tmclocation.links) {

			RdTmclocationLink link = new RdTmclocationLink();

			link.copy(r);
			
			link.setGroupId(tmclocation.getPid());

			this.links.add(link);
		}

	}

	@Override
	public Map<String, Object> changedFields() {
		return changedFields;
	}

	@Override
	public String parentPKName() {
		return "group_id";
	}

	@Override
	public int parentPKValue() {
		return this.pid;
	}

	@Override
	public String parentTableName() {
		return "RD_TMCLOCATION";
	}

	@Override
	public List<List<IRow>> children() {
		List<List<IRow>> children = new ArrayList<>();

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
		JSONObject json = JSONObject.fromObject(this, Geojson.geoJsonConfig(0.00001, 5));

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
		return "group_id";
	}

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		Map<Class<? extends IRow>,List<IRow>> childList = new HashMap<Class<? extends IRow>, List<IRow>>();
		childList.put(RdTmclocationLink.class, links);
		return childList;
	}

	@Override
	public Map<Class<? extends IRow>,Map<String,?>> childMap() {
		Map<Class<? extends IRow>,Map<String,?>> childMap = new HashMap<Class<? extends IRow>,Map<String,?>>();
		childMap.put(RdTmclocationLink.class, linkMap);
		return childMap;
	}

}
