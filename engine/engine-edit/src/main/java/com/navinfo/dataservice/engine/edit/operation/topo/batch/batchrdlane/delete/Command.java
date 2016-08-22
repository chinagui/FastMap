package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane.delete;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
import net.sf.json.JSONObject;

/**
 * 详细车道批量删除接口
 * 
 * @author zhaokaikai
 * 
 */
public class Command extends AbstractCommand {

	private String requester;

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public List<RdLane> getLanes() {
		return lanes;
	}

	public void setLanes(List<RdLane> lanes) {
		this.lanes = lanes;
	}

	private int linkPid;
	private List<RdLane> lanes;

	private int laneDir;// 车道方向

	public int getLaneDir() {
		return laneDir;
	}

	public void setLaneDir(int laneDir) {
		this.laneDir = laneDir;
	}

	@Override
	public OperType getOperType() {
		return OperType.BATCH;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLANE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		JSONObject data = json.getJSONObject("data");
		this.setDbId(json.getInt("dbId"));
		this.setLinkPid(data.getInt("linkPid"));
		this.setLaneDir(data.getInt("laneDir"));

	}

}
