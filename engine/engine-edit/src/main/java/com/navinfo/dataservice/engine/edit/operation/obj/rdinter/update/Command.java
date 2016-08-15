package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
* @ClassName: Command 
* @author Zhang Xiaolong
* @date 2016年7月20日 下午7:39:18 
* @Description: TODO
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;
	
	private JSONArray nodeArray;
	
	private JSONArray linkArray;
	
	private RdInter rdInter;
	
	private int pid;
	
	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public JSONObject getContent() {
		return content;
	}

	public void setContent(JSONObject content) {
		this.content = content;
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDINTER;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public RdInter getRdInter() {
		return rdInter;
	}

	public void setRdInter(RdInter rdInter) {
		this.rdInter = rdInter;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");
		
		this.pid = this.content.getInt("pid");
		
		if(content.containsKey("nodes"))
		{
			nodeArray = content.getJSONArray("nodes");
		}
		if(content.containsKey("links"))
		{
			linkArray = content.getJSONArray("links");
		}
	}

	public JSONArray getNodeArray() {
		return nodeArray;
	}

	public void setNodeArray(JSONArray nodeArray) {
		this.nodeArray = nodeArray;
	}

	public JSONArray getLinkArray() {
		return linkArray;
	}

	public void setLinkArray(JSONArray linkArray) {
		this.linkArray = linkArray;
	}
}
