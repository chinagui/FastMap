package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;

	private int inLinkPid;

	private int nodePid;
	
	private int outLinkPid;

	private List<Integer> vias = new ArrayList<>();

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public List<Integer> getVias() {
		return vias;
	}

	public void setVias(List<Integer> vias) {
		this.vias = vias;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDVARIABLESPEED;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");

		this.nodePid = data.getInt("nodePid");

		this.inLinkPid = data.getInt("inLinkPid");
		
		this.outLinkPid = data.getInt("outLinkPid");
		
		if(data.containsKey("vias"))
		{
			JSONArray array = data.getJSONArray("vias");
			
			if(array != null)
			{
				for (int i = 0; i < array.size(); i++) {

					int pid = array.getInt(i);

					if (!vias.contains(pid)) {
						vias.add(pid);
					}
				}
			}
		}
	}

}
