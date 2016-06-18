package com.navinfo.dataservice.engine.edit.edit.operation.obj.poiparent.update;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.engine.edit.edit.operation.AbstractCommand;

public class Command extends AbstractCommand  implements ICommand{


	private String requester;


	private JSONObject content;
	
	/**
	 * 子poi的pid
	 */
	private Integer objId;	
	
	/**
	 * 父poi的pid
	 */
	private Integer parentPid;


	public Integer getObjId() {
		return objId;
	}

	public void setObjId(Integer objId) {
		this.objId = objId;
	}

	public Integer getParentPid() {
		return parentPid;
	}

	public void setParentPid(Integer parentPid) {
		this.parentPid = parentPid;
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
		return ObjType.IXPOIEVENT;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		
		this.setObjId(json.getInt("objId"));
		
		this.setParentPid(json.getInt("parentPid"));

	}

}
