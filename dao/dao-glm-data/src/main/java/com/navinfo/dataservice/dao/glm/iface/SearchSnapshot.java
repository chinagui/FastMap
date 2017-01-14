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
		
		if(objLevel == ObjLevel.BRIEF)
		{
			JSONObject obj = new JSONObject();
			
			obj.put("i", this.getI());
			
			obj.put("t", this.getT());
			
			obj.put("g", this.getG());
			
			obj.put("m", this.getM());
			
			return obj;
		}
		else
		{
			return JSONObject.fromObject(this,JsonUtils.getStrConfig());
		}
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {
		this.setI(json.getInt("i"));
		this.setG(json.getJSONArray("g"));
		this.setT(json.getInt("t"));
		this.setM(json.getJSONObject("m"));
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
