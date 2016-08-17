package com.navinfo.dataservice.engine.edit.operation.obj.rdlane.delete;

import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ICommand;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
/***
 * 详细车道删除
 * @author zhaokk
 *
 */
public class Command extends AbstractCommand implements ICommand {

	private String requester;
	private int pid;
	private RdLane rdLane;
    private List<RdLane> lanes;

	public List<RdLane> getLanes() {
		return lanes;
	}

	public void setLanes(List<RdLane> lanes) {
		this.lanes = lanes;
	}

	public RdLane getRdLane() {
		return rdLane;
	}

	public void setRdLane(RdLane rdLane) {
		this.rdLane = rdLane;
	}

	@Override
	public OperType getOperType() {
		return OperType.DELETE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDLANE;
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
