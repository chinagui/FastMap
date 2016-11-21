package com.navinfo.dataservice.engine.edit.operation.obj.zonelink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author zhaokk 新建Zone线参数基础类
 */
public class Command extends AbstractCommand {

	private String requester;
	// ZONE线的几何
	private Geometry geometry;
	// ZONE线的终点pid
	private int eNodePid;
	// ZONE线的起点pid
	private int sNodePid;
	// ZONE线挂接的NODE和LINK
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

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.ZONELINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public JSONArray getCatchLinks() {
		return catchLinks;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		JSONObject data = json.getJSONObject("data");

		this.eNodePid = data.getInt("eNodePid");

		this.sNodePid = data.getInt("sNodePid");
		this.geometry = GeoTranslator.geojson2Jts(
				data.getJSONObject("geometry"), 1, 5);
		// ZONE线挂接的ZONELINK 和ZONENODE
		if (data.containsKey("catchLinks")) {

			JSONArray jsonArray = JSONArray.fromObject(data
					.getJSONArray("catchLinks"));
			this.catchLinks = jsonArray;
		} else {
			this.catchLinks = new JSONArray();
		}
	}

}
