package com.navinfo.dataservice.engine.meta.tmc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.commons.geom.Geojson;
import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;

public class TmcLineTree implements IObj {

	private int tmcId;

	private String name;

	private ObjType type;

	private JSONArray geometry;

	private List<TmcLineTree> children = new ArrayList<>();
	
	public TmcLineTree()
	{
	}

	public TmcLineTree(TmcPoint point) {
		this.setTmcId(point.getTmcId());

		this.setName(point.getTranslateName());

		this.setType(ObjType.TMCPOINT);

		this.setGeometry(point.getGeometry());
	}

	public TmcLineTree(TmcLine line) {
		this.setTmcId(line.getTmcId());

		this.setName(line.getTranslateName());

		this.setType(ObjType.TMCLINE);

		this.setGeometry(line.getGeometry());
	}
	
	public TmcLineTree(TmcArea tmcArea) {
		this.setTmcId(tmcArea.getTmcId());

		this.setName(tmcArea.getTranslateName());

		this.setType(ObjType.TMCAREA);
	}

	public ObjType getType() {
		return type;
	}

	public void setType(ObjType type) {
		this.type = type;
	}

	public int getTmcId() {
		return tmcId;
	}

	public JSONArray getGeometry() {
		return geometry;
	}

	public void setGeometry(JSONArray geometry) {
		this.geometry = geometry;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TmcLineTree> getChildren() {
		return children;
	}

	public void setChildren(List<TmcLineTree> children) {
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
		TmcLineTree copy = (TmcLineTree) row;
		
		this.name = copy.getName();
		
		this.tmcId = copy.getTmcId();
		
		this.type = copy.getType();
		
		this.geometry = copy.getGeometry();
		
		for(TmcLineTree child : copy.getChildren())
		{
			TmcLineTree newChild = new TmcLineTree();
			newChild.copy(child);
			this.children.add(newChild);
		}
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

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {
		JsonConfig jsonConfig = Geojson.geoJsonConfig(0.00001, 5);

		if (objLevel == ObjLevel.FULL || objLevel == ObjLevel.HISTORY) {

			JSONObject json = JSONObject.fromObject(this, jsonConfig);

			return json;
		} else if (objLevel == ObjLevel.BRIEF) {
			JSONObject json = new JSONObject();

			json.put("tmcId", this.tmcId);

			json.put("name", name);
			
			json.put("geometry", geometry);
			
			json.put("type", String.valueOf(type));

			JSONArray array = new JSONArray();

			for (TmcLineTree tree : this.children) {
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

	@Override
	public Map<Class<? extends IRow>, List<IRow>> childList() {
		return null;
	}

	@Override
	public Map<Class<? extends IRow>, Map<String, ?>> childMap() {
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TmcLineTree)
		{
			TmcLineTree compTree = (TmcLineTree) obj;
			
			if(compTree.getTmcId() == this.getTmcId() && compTree.getType().equals(this.getType()))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
}
