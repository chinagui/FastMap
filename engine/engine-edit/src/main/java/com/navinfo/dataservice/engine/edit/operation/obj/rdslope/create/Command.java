package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.create;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 坡度操作参数
 * 
 * @author zhaokaikai
 * 
 */
public class Command extends AbstractCommand {

	private String requester;
	private int inNodePid;// 进入点
	private int outLinkPid;// 退出线

	private double length;// 总长度
	private List<Integer> seriesLinkPids;// 持续links

	public int getInNodePid() {
		return inNodePid;
	}

	public void setInNodePid(int inNodePid) {
		this.inNodePid = inNodePid;
	}

	public int getOutLinkPid() {
		return outLinkPid;
	}

	public void setOutLinkPid(int outLinkPid) {
		this.outLinkPid = outLinkPid;
	}

	public List<Integer> getSeriesLinkPids() {
		return seriesLinkPids;
	}

	public void setSeriesLinkPids(List<Integer> seriesLinkPids) {
		this.seriesLinkPids = seriesLinkPids;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDSLOPE;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		JSONObject data = json.getJSONObject("data");
		this.setDbId(json.getInt("dbId"));
		this.setInNodePid(data.getInt("nodePid"));
		this.setOutLinkPid(data.getInt("linkPid"));
		if (data.containsKey("linkPids")) {
			this.setLength(data.getDouble("length"));
			seriesLinkPids = new ArrayList<Integer>();
			JSONArray array = data.getJSONArray("linkPids");
			for (int i = 0; i < array.size(); i++) {
				int pid = array.getInt(i);
				if (!this.getSeriesLinkPids().contains(pid)) {
					this.getSeriesLinkPids().add(pid);
				}
			}
		}

	}

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}
}
