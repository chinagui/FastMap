package com.navinfo.dataservice.engine.edit.operation.obj.rdroad.update;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.road.RdRoad;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	protected Logger log = Logger.getLogger(this.getClass());

	private String requester;
	
	private int pid;

	private List<Integer> linkPids;

	RdRoad road;	

	public int getPid() {
		return pid;
	}

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public RdRoad getRoad() {
		return road;
	}

	public void setRoad(RdRoad road) {
		this.road = road;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDROAD;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");
		
		this.pid = data.getInt("pid");

		JSONArray array = data.getJSONArray("linkPids");

		linkPids = new ArrayList<Integer>();

		for (int i = 0; i < array.size(); i++) {

			int pid = array.getInt(i);

			if (!linkPids.contains(pid)) {

				linkPids.add(pid);
			}
		}
	}
}
