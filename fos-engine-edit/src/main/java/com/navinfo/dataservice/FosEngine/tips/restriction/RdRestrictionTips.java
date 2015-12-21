package com.navinfo.dataservice.FosEngine.tips.restriction;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.comm.util.JsonUtils;
import com.navinfo.dataservice.FosEngine.edit.model.ISerializable;
import com.navinfo.dataservice.FosEngine.edit.model.ObjLevel;

/**
 * 交限Tips模型
 */
public class RdRestrictionTips implements ISerializable {

	private JSONArray resId;

	private JSONObject in;

	private JSONArray info;

	private JSONArray o_array;

	public RdRestrictionTips() {

	}

	public JSONArray getResId() {
		return resId;
	}

	public void setResId(JSONArray resId) {
		this.resId = resId;
	}

	public JSONObject getIn() {
		return in;
	}

	public void setIn(JSONObject in) {
		this.in = in;
	}

	public JSONArray getInfo() {
		return info;
	}

	public void setInfo(JSONArray info) {
		this.info = info;
	}

	public JSONArray getO_array() {
		return o_array;
	}

	public void setO_array(JSONArray o_array) {
		this.o_array = o_array;
	}

	@Override
	public JSONObject Serialize(ObjLevel objLevel) throws Exception {

		return JSONObject.fromObject(this,JsonUtils.getStrConfig());
	}

	@Override
	public boolean Unserialize(JSONObject json) throws Exception {

		return false;
	}

}
