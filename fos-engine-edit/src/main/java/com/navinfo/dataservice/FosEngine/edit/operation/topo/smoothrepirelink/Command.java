package com.navinfo.dataservice.FosEngine.edit.operation.topo.smoothrepirelink;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjType;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.link.RdLink;
import com.navinfo.dataservice.FosEngine.edit.operation.ICommand;
import com.navinfo.dataservice.FosEngine.edit.operation.OperType;

public class Command implements ICommand {
	
	private String requester;

	private int linkPid;
	
	private RdLink link;
	
	private int projectId;
	
	private JSONObject updateContent;
	
	public Command(JSONObject json,String requester) {
		this.requester = requester;

		this.projectId = json.getInt("projectId");
		
		this.updateContent = json.getJSONObject("data");
		
		this.linkPid = this.updateContent.getInt("pid");
	}
	

	public JSONObject getUpdateContent() {
		return updateContent;
	}


	public void setUpdateContent(JSONObject updateContent) {
		this.updateContent = updateContent;
	}


	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public RdLink getLink() {
		return link;
	}

	public void setLink(RdLink link) {
		this.link = link;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public void setRequester(String requester) {
		this.requester = requester;
	}

	@Override
	public OperType getOperType() {
		
		return OperType.SMOOTHREPIRELINK;
	}

	@Override
	public String getRequester() {
		
		return requester;
	}

	@Override
	public ObjType getObjType() {
		
		return ObjType.RDLINK;
	}

}
