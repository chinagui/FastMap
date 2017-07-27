package com.navinfo.dataservice.dao.glm.iface;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;

/**
 * 查询对象模型
 */
public class SearchSnapshot implements ISerializable {

	private int t;

	private int i;

	private int dbId;

	private JSONArray g;

	private JSONObject m;

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public JSONObject getM() {
		return m;
	}

	public void setM(JSONObject m) {
		this.m = m;
	}

	public SearchSnapshot() {
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		if (objLevel == ObjLevel.BRIEF) {
			JSONObject obj = new JSONObject();
			if (this.getI() != 0) {
				obj.put("i", this.getI());
			}
			obj.put("t", this.getT());

			obj.put("g", this.getG());

			obj.put("m", this.getM());
			obj.put("d", this.getDbId());

			return obj;
		} else {
			return JSONObject.fromObject(this, JsonUtils.getStrConfig());
		}
	}

	public int getDbId() {
		return dbId;
	}

	public void setDbId(int dbId) {
		this.dbId = dbId;
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		if (json.containsKey("i")) {
			this.setI(json.getInt("i"));
		}
		this.setG(json.getJSONArray("g"));
		this.setT(json.getInt("t"));
		this.setM(json.getJSONObject("m"));
		this.setDbId(json.getInt("d"));
		return false;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj instanceof SearchSnapshot) {
			SearchSnapshot snapshot = (SearchSnapshot) obj;

			if (snapshot.getI() == this.getI()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}

	public JSONArray getG() {
		return g;
	}

	public void setG(JSONArray g) {
		this.g = g;
	}

}
