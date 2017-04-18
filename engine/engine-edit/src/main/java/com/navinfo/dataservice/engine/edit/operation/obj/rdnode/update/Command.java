package com.navinfo.dataservice.engine.edit.operation.obj.rdnode.update;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;	

	private JSONArray updateContents;

	private int pid;
	
	private RdNode node;

	private Map<Integer, RdNode> rdNodeMap = new HashMap<Integer, RdNode>();	
	
	public RdNode getNode() {
		return node;
	}

	public void setNode(RdNode node) {
		this.node = node;
	}
	
	/**
	 * 被修改的node映射关系。Integer：nodePid, RdNode：node对象
	 * @return
	 */
	public Map<Integer, RdNode> getNodeMap() {

		return rdNodeMap;
	}

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

	/**
	 * 修改内容，批量编辑node使用
	 */
	public JSONArray getUpdateContents() {
		return updateContents;
	}	

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDNODE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));

		this.content = json.getJSONObject("data");
		
		this.pid = this.content.getInt("pid");

	}
	public Command(JSONObject json, String requester,RdNode node) {
		this(json,requester);
		this.node =node;
	}
	//批量编辑使用
	public Command(JSONArray json) {
		this.updateContents = json;	
	}

}
