package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.lane.RdLane;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 详细车道批量操作接口
 * 
 * @author zhaokaikai
 * 
 */
public class Command extends AbstractCommand {

	private String requester;
	private int linkPid;// link号码
	private int laneDir;//车道方向
	private int laneNum;//车道总数
	private List<RdLane> sourceLanes;//原有车道信息
	public List<RdLane> getSourceLanes() {
		return sourceLanes;
	}

	public void setSourceLanes(List<RdLane> sourceLanes) {
		this.sourceLanes = sourceLanes;
	}

	private List<RdLane> lanes;//当前车道信息
	public List<RdLane> getLanes() {
		return lanes;
	}

	public void setLanes(List<RdLane> lanes) {
		this.lanes = lanes;
	}

	public int getLaneDir() {
		return laneDir;
	}

	public void setLaneDir(int laneDir) {
		this.laneDir = laneDir;
	}

	private JSONArray laneInfos;//车道信息表;;

	public int getLinkPid() {
		return linkPid;
	}

	public void setLinkPid(int linkPid) {
		this.linkPid = linkPid;
	}

	public JSONArray getLaneInfos() {
		return laneInfos;
	}

	public void setLaneInfos(JSONArray laneInfos) {
		this.laneInfos = laneInfos;
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
		this.setLaneNum(data.getInt("laneNum"));
		for(int i = 0;i < data.getJSONArray("laneInfos").size(); i++ ){
			lanes = new ArrayList<RdLane>();
			RdLane  lane = new RdLane();
			JSONObject jsonObject = data.getJSONArray("laneInfos").getJSONObject(i);
			lane.setPid(jsonObject.getInt("lanePid"));
			lane.setSeqNum(jsonObject.getInt("seqNum"));
		    lane.setArrowDir(jsonObject.getString("arrowDir"));
		    lane.setLaneNum(this.getLaneNum());
		    lane.setLinkPid(this.getLinkPid());
		    lane.setLaneDir(this.getLaneDir());
		    lanes.add(lane);
		}
        this.setLaneInfos( data.getJSONArray("laneInfos"));
        
	}

	public int getLaneNum() {
		return laneNum;
	}

	public void setLaneNum(int laneNum) {
		this.laneNum = laneNum;
	}
}
