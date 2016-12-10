package com.navinfo.dataservice.engine.edit.operation.obj.rdlink.sideRoad.create;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private double distance;

	private double sideType;
	
	private int sNodePid;
	
	public int getSNodePid() {
		
		return sNodePid;
	}

	/**
	 * 1:双侧，2右侧，3左侧
	 * 
	 * @return
	 */
	public double getSideType() {
		return sideType;
	}

	public void setSideType(double sideType) {
		this.sideType = sideType;
	}

	private List<Integer> linkPids;
	private List<RdLink> links;

	public List<RdLink> getLinks() {
		return links;
	}

	public void setLinks(List<RdLink> links) {
		this.links = links;
	}

	public double getDistance() {
		return distance;
	}

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	@Override
	public OperType getOperType() {

		return OperType.CREATESIDEROAD;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}

	@Override
	public String getRequester() {

		return requester;
	}

	public Command(JSONObject json, String requester) {

		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		// 移动距离
		this.distance = json.getDouble("distance");
		// 1: 两侧；2：右侧、3左侧
		this.sideType = json.getInt("sideType");
		
		if (json.containsKey("sNodePid")) {

			this.sNodePid = json.getInt("sNodePid");
		}
		// 获取要上下线分离的linkPids
		JSONObject data = json.getJSONObject("data");
		JSONArray array = data.getJSONArray("linkPids");

		linkPids = new ArrayList<Integer>();

		for (int i = 0; i < array.size(); i++) {

			int pid = Integer.valueOf(array.getString(i));

			if (!linkPids.contains(pid)) {
				linkPids.add(pid);
			}
		}
	}
}
