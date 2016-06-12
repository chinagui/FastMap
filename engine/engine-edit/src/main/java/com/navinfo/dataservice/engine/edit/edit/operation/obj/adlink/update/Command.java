package com.navinfo.dataservice.engine.edit.edit.operation.obj.adlink.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractProcess;
/**
 * @author zhaokk
 *新建行政区划线参数基础类 
 */
public class Command extends AbstractCommand  {

	private String requester;

//	private int projectId;
	
	private int linkPid;
	
	private JSONObject updateContent;

	public int getLinkPid() {
		return linkPid;
	}


	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}


//	public int getProjectId() {
//		return projectId;
//	}


	public JSONObject getUpdateContent() {
		return updateContent;
	}


	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.ADLINK;
	}

	@Override
	public String getRequester() {
		return requester;
	}
	
	public Command(JSONObject json, String requester) {
		this.requester = requester;

//		this.projectId = json.getInt("projectId");
		this.setDbId(json.getInt("subTaskId"));
		
		this.updateContent = json.getJSONObject("data");
		
		this.linkPid = this.updateContent.getInt("pid");
	}

}
