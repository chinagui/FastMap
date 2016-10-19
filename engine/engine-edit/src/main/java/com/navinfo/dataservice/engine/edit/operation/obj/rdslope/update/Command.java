package com.navinfo.dataservice.engine.edit.operation.obj.rdslope.update;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.slope.RdSlope;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;
/**
 * @author zhaokk
 * 修改坡度参数基础类 
 */
public class Command extends AbstractCommand {

	private String requester;

	private JSONObject content;
	
	private int outLinkPid;
	
	private List<Integer> seriesLinkPids;// 持续links
	
	private RdSlope slope;
	
	public RdSlope getSlope() {
		return slope;
	}

	public void setSlope(RdSlope slope) {
		this.slope = slope;
	}

	private int pid;
	
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

	@Override
	public OperType getOperType() {
		return OperType.UPDATE;
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
		this.setDbId(json.getInt("dbId"));
		this.content = json.getJSONObject("data");
		this.pid = this.content.getInt("pid");
		if(this.content.containsKey("linkPid")){
			this.setOutLinkPid(this.content.getInt("linkPid"));
		}
		if(this.content.containsKey("linkPids")){
			seriesLinkPids = new ArrayList<Integer>();
			JSONArray array = this.content.getJSONArray("linkPids");
			for (int i = 0; i < array.size(); i++) {
				int pid = array.getInt(i);
				if (!this.getSeriesLinkPids().contains(pid)) {
					this.getSeriesLinkPids().add(pid);
				}
			}
		}

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

}
