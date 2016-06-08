package com.navinfo.dataservice.engine.edit.edit.operation.topo.departadnode;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
public class Command extends AbstractCommand {

	private int linkPid;

	private String requester;

	private int eNodePid = -1;

	private int sNodePid = -1;

	private double slon;

	private double slat;

	private double elon;

	private double elat;

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		JSONObject data = json.getJSONObject("data");

		this.linkPid = data.getInt("linkPid");
        //获取移动后起点信息
		if (data.containsKey("sNodePid")) {
			this.sNodePid = data.getInt("sNodePid");

			this.slon = Math.round(data.getDouble("slon") * 100000) / 100000.0;

			this.slat = Math.round(data.getDouble("slat") * 100000) / 100000.0;
		}
        //获取移动后起点信息
		if (data.containsKey("eNodePid")) {
			this.sNodePid = data.getInt("eNodePid");

			this.elon = Math.round(data.getDouble("elon") * 100000) / 100000.0;

			this.elat = Math.round(data.getDouble("elat") * 100000) / 100000.0;
		}

		this.setSubTaskId(json.getInt("subTaskId"));
	}

	public int getLinkPid() {
		return linkPid;
	}

	public int geteNodePid() {
		return eNodePid;
	}

	public int getsNodePid() {
		return sNodePid;
	}

	public double getSlon() {
		return slon;
	}

	public double getSlat() {
		return slat;
	}

	public double getElon() {
		return elon;
	}

	public double getElat() {
		return elat;
	}

	@Override
	public OperType getOperType() {
		return OperType.DEPART;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.ADLINK;
	}

}
