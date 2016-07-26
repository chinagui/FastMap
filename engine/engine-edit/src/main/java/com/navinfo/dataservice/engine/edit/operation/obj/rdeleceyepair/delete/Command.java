package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceyepair.delete;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePair;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdEleceyePart;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{

	private String requester;
	
	private int groupId;
	
	private List<RdEleceyePart> parts;
	
	private RdEleceyePair pair;


	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public List<RdEleceyePart> getParts() {
		return parts;
	}

	public void setParts(List<RdEleceyePart> parts) {
		this.parts = parts;
	}

	public RdEleceyePair getPair() {
		return pair;
	}

	public void setPair(RdEleceyePair pair) {
		this.pair = pair;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}

	@Override
	public String getRequester() {
		return this.requester;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDELECEYEPAIR;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		this.groupId = json.getInt("objId");
	}

}
