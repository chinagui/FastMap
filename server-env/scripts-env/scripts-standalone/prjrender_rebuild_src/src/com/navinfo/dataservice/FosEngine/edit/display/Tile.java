package com.navinfo.dataservice.FosEngine.edit.display;

import com.navinfo.dataservice.FosEngine.edit.model.ISerializable;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 瓦片数据模型
 */
public class Tile implements ISerializable {

	private int t;

	private String i;

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

	private JSONArray g;

	private JSONObject m;
	
	public Tile() {

	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		return JSONObject.fromObject(this);
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
