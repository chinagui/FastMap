package com.navinfo.dataservice.dao.glm.model.ad.zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

import com.alibaba.druid.util.StringUtils;
import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

public class AdAdminTree implements IObj {

	private int regionId;

	private String name;

	private String type;

	private AdAdminGroup group;

	private AdAdminPart part;

	private List<AdAdminTree> children = new ArrayList<>();

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AdAdminGroup getGroup() {
		return group;
	}

	public void setGroup(AdAdminGroup group) {
		this.group = group;
	}

	public AdAdminPart getPart() {
		return part;
	}

	public void setPart(AdAdminPart part) {
		this.part = part;
	}

	public List<AdAdminTree> getChildren() {
		return children;
	}

	public void setChildren(List<AdAdminTree> children) {
		this.children = children;
	}

	@Override
	public String rowId() {
		return null;
	}

	@Override
	public void setRowId(String rowId) {
	}

	@Override
	public String tableName() {
		return null;
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
		return null;
	}

	@Override
	public void copy(IRow row) {
	}

	@Override
	public Map<String, Object> changedFields() {
		return null;
	}

	@Override
	public String parentPKName() {
		return null;
	}

	@Override
	public int parentPKValue() {
		return 0;
	}

	@Override
	public String parentTableName() {
		return null;
	}

	@Override
	public List<List<IRow>> children() {
		return null;
	}

	@Override
	public boolean fillChangeFields(JSONObject json) throws Exception {
		return false;
	}

	@Override
	public int mesh() {
		return 0;
	}

	@Override
	public void setMesh(int mesh) {
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {

			JSONObject json = JSONObject.fromObject(this, jsonConfig);

			return json;
		} else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();

			json.put("regionId", regionId);

			json.put("name", name);

			if (!StringUtils.isEmpty(type)) {
				json.put("type", type);
			}

			if (group != null) {
				json.put("group", group.Serialize(ObjLevel.BRIEF));
			}

			if (part != null) {
				json.put("part", part.Serialize(ObjLevel.BRIEF));
			}

			JSONArray array = new JSONArray();

			for (AdAdminTree tree : this.children) {
				array.add(tree.Serialize(ObjLevel.BRIEF));
			}

			json.put("children", array);

			return json;
		}
		return JSONObject.fromObject(this, jsonConfig);
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
		return 0;
	}

	@Override
	public String primaryKey() {
		return null;
	}

}
