package com.navinfo.dataservice.engine.edit.operation.obj.rdlanetopo.delete;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLaneTopoDetail;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
/***
 * 车道联通信息删除
 * @author zhaokk
 *
 */
public class Command extends AbstractCommand implements ICommand {

	private String requester;
	private int pid;
	private RdLaneTopoDetail detail;

	public RdLaneTopoDetail getDetail() {
		return detail;
	}

	public void setDetail(RdLaneTopoDetail detail) {
		this.detail = detail;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDLANETOPODETAIL;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;
        this.pid = json.getInt("objId");
		this.setDbId(json.getInt("dbId"));
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

}
