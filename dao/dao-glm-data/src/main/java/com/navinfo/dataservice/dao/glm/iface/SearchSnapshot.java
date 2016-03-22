package com.navinfo.dataservice.dao.glm.iface;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;

/**
 * 查询对象模型
 */
public class SearchSnapshot implements ISerializable {

	private int t;

	private String i;
	
	private JSONArray g;

	private JSONObject m;

	public String getI() {
		return i;
	}

	public void setI(String i) {
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

		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		return false;
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
