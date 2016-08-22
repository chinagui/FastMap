package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlinkspeedlimit;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private List<Integer> linkPids;
	
	private  int direct;

	private JSONObject speedLimitContent;	
	
	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public JSONObject getSpeedLimitContent() {
		return speedLimitContent;
	}

	public void setSpeedLimitContent(JSONObject speedLimitContent) {
		this.speedLimitContent = speedLimitContent;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	@Override
	public OperType getOperType() {
		return OperType.BATCH;
	}

	@Override
	public ObjType getObjType() {

		return ObjType.RDLINKSPEEDLIMIT;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		
		this.direct = json.getInt("direct");

		JSONArray array = json.getJSONArray("linkPids");

		this.speedLimitContent = json.getJSONObject("linkSpeedLimit");

		linkPids = new ArrayList<Integer>();

		for (int i = 0; i < array.size(); i++) {

			int pid = array.getInt(i);

			if (!linkPids.contains(pid)) {

				linkPids.add(pid);
			}
		}
	}

}
