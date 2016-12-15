package com.navinfo.dataservice.dao.fcc;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.util.JsonUtils;

/**
 * 查询对象模型
 */
public class SearchSnapshot implements ISerializable {

	private String t;

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
		this.setI(json.getString("i"));
		this.setG(json.getJSONArray("g"));
		this.setT(json.getString("t"));
		this.setM(json.getJSONObject("m"));
		return false;
	}

	public JSONArray getG() {
		return g;
	}

	public void setG(JSONArray g) {
		this.g = g;
	}

	/**
	 * @return the t
	 */
	public String getT() {
		return t;
	}

	/**
	 * @param t the t to set
	 */
	public void setT(String t) {
		this.t = t;
	}
	
	

}
