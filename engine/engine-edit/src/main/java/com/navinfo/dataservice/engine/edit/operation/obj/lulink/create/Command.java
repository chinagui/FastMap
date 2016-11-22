package com.navinfo.dataservice.engine.edit.operation.obj.lulink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand {

	private String requester;

	private Geometry geometry;

	private int eNodePid;

	private int sNodePid;

	private JSONArray catchLinks;

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public int geteNodePid() {
		return eNodePid;
	}

	public void seteNodePid(int eNodePid) {
		this.eNodePid = eNodePid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public void setsNodePid(int sNodePid) {
		this.sNodePid = sNodePid;
	}

	public JSONArray getCatchLinks() {
		return catchLinks;
	}

	public void setCatchLinks(JSONArray catchLinks) {
		this.catchLinks = catchLinks;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.LULINK;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");

		this.eNodePid = data.getInt("eNodePid");

		this.sNodePid = data.getInt("sNodePid");
		this.geometry = GeoTranslator.geojson2Jts(
				data.getJSONObject("geometry"), 1, 5);
		// 获取土地利用线挂接的LuLink 和LuNode
		if (data.containsKey("catchLinks")) {

			JSONArray jsonArray = JSONArray.fromObject(data
					.getJSONArray("catchLinks"));
			this.catchLinks = jsonArray;
		} else {
			this.catchLinks = new JSONArray();
		}
	}

}
