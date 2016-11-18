package com.navinfo.dataservice.engine.edit.operation.obj.tmc.create;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	protected Logger log = Logger.getLogger(this.getClass());

	private String requester;
	
	private int tmcId;
	
	private int loctableId;
	
	private int locDirect;
	
	private int direct;

	private List<Integer> linkPids;

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDTMCLOCATION;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");
		
		this.tmcId = data.getInt("tmcId");
		
		this.locDirect = data.getInt("locDirect");
		
		this.direct = data.getInt("direct");
		
		this.loctableId = data.getInt("loctableId");

		JSONArray array = data.getJSONArray("linkPids");
		
		linkPids = new ArrayList<Integer>();

		for (int i = 0; i < array.size(); i++) {

			int pid = array.getInt(i);

			if (!linkPids.contains(pid)) {

				linkPids.add(pid);
			}
		}
	}

	public int getTmcId() {
		return tmcId;
	}

	public void setTmcId(int tmcId) {
		this.tmcId = tmcId;
	}

	public int getLocDirect() {
		return locDirect;
	}

	public void setLocDirect(int locDirect) {
		this.locDirect = locDirect;
	}

	public int getDirect() {
		return direct;
	}

	public void setDirect(int direct) {
		this.direct = direct;
	}

	public int getLoctableId() {
		return loctableId;
	}

	public void setLoctableId(int loctableId) {
		this.loctableId = loctableId;
	}
}
