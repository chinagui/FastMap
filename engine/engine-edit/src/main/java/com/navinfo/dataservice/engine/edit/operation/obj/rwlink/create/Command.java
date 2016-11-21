package com.navinfo.dataservice.engine.edit.operation.obj.rwlink.create;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import com.vividsolutions.jts.geom.Geometry;

public class Command extends AbstractCommand {

	protected Logger log = Logger.getLogger(this.getClass());

	private String requester;

	private Geometry geometry;

	private int eNodePid;

	private int sNodePid;

	private int kind = 1;

	private int form;

	private JSONArray catchLinks;

	public int getKind() {
		return kind;
	}

	public int getForm() {
		return form;
	}

	public void setForm(int form) {
		this.form = form;
	}

	public void setKind(int kind) {
		this.kind = kind;
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
		return ObjType.RWLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
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

		try {
			this.geometry = GeoTranslator.geojson2Jts(
					data.getJSONObject("geometry"), 1, 5);
		} catch (Exception e) {
			String msg = e.getLocalizedMessage();

			log.error(e.getMessage(), e);

			if (msg.contains("found 1 - must be 0 or >= 2")) {
				throw new Exception("线至少包含两个点");
			} else {
				throw new Exception(msg);
			}
		}

		if (data.containsKey("kind")) {
			this.kind = data.getInt("kind");
		}

		if (data.containsKey("form")) {
			this.form = data.getInt("form");
		}

		if (data.containsKey("catchLinks")) {

			JSONArray jsonArray = JSONArray.fromObject(data
					.getJSONArray("catchLinks"));
			this.catchLinks = jsonArray;

		} else {
			this.catchLinks = new JSONArray();
		}
	}

}
