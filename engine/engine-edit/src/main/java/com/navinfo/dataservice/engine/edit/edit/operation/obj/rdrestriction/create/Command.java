package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdrestriction.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Command extends AbstractCommand {

	private String requester;
	
	private int inLinkPid;

	private int nodePid;

	private List<Integer> outLinkPids;

	private String restricInfos;

	public int getInLinkPid() {
		return inLinkPid;
	}

	public void setInLinkPid(int inLinkPid) {
		this.inLinkPid = inLinkPid;
	}

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	public List<Integer> getOutLinkPids() {
		return outLinkPids;
	}

	public void setOutLinkPids(List<Integer> outLinkPids) {
		this.outLinkPids = outLinkPids;
	}
	
	public String getRestricInfos() {
		return restricInfos;
	}

	public void setRestricInfos(String restricInfos) {
		this.restricInfos = restricInfos;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDRESTRICTION;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public void createGlmList() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		JSONObject data = json.getJSONObject("data");

		this.nodePid = data.getInt("nodePid");

		this.inLinkPid = data.getInt("inLinkPid");
		
		outLinkPids = new ArrayList<Integer>();
		
		if (data.containsKey("outLinkPids")) {
			JSONArray array = data.getJSONArray("outLinkPids");

			for (int i = 0; i < array.size(); i++) {

				int pid = array.getInt(i);

				if (!outLinkPids.contains(pid)) {
					outLinkPids.add(pid);
				}
			}
		}
		
		if(data.containsKey("infos"))
		{
			restricInfos = data.getString("infos");
		}
	}
	
}
