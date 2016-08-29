package com.navinfo.dataservice.engine.edit.operation.topo.batch.batchrdlane;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
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
	private List<Integer> linkPids = new ArrayList<Integer>();
	private List<IRow> links;
	private int laneDir;// 车道方向

	public List<IRow> getLinks() {
		return links;
	}

	public void setLinks(List<IRow> links) {
		this.links = links;
	}

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public int getLaneDir() {
		return laneDir;
	}

	public void setLaneDir(int laneDir) {
		this.laneDir = laneDir;
	}

	private JSONArray laneInfos;// 车道信息表;;

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
		for (int i = 0; i < data.getJSONArray("linkPids").size(); i++) {
			this.getLinkPids().add((data.getJSONArray("linkPids").getInt(i)));
		}
		this.setLaneDir(data.getInt("laneDir"));
		this.setLaneInfos(data.getJSONArray("laneInfos"));

	}

}
