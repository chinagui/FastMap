package com.navinfo.dataservice.engine.limit.operation.meta.scplateresrdlink.update;

import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.limit.glm.iface.DbType;
import com.navinfo.dataservice.engine.limit.glm.iface.LimitObjType;
import com.navinfo.dataservice.engine.limit.glm.model.meta.ScPlateresRdLink;
import com.navinfo.dataservice.engine.limit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	private String requester;

	private int linkpid = 0;

	private JSONObject content;
	
	private ScPlateresRdLink rdLink;
	
	public int getLinkpid(){
		return this.linkpid;
	}
	
	public JSONObject getContent(){
		return this.content;
	}
	
	public ScPlateresRdLink getRdLink(){
		return this.rdLink;
	}
	
	public void setRdLink(ScPlateresRdLink value){
		this.rdLink = value;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		
		//JSONObject data = json.getJSONObject("data");
		this.linkpid = json.getInt("objId");
		this.content = json.getJSONObject("data");
	}

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
	}

	@Override
	public DbType getDbType() {
		return DbType.LIMITDB;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public LimitObjType getObjType() {
		return LimitObjType.SCPLATERESLINK;
	}
}
