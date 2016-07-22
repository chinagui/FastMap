package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

import net.sf.json.JSONObject;

public class Command extends AbstractCommand{
	
	protected Logger log = Logger.getLogger(this.getClass());
	
	private String requester;
	
	private int nodePid;
	
	private Map<Integer,List<Integer>> nodeLinkPidMap;
	
	private RdCross cross;

	public int getNodePid() {
		return nodePid;
	}

	public void setNodePid(int nodePid) {
		this.nodePid = nodePid;
	}

	@Override
	public OperType getOperType() {
		return OperType.CREATE;
	}
	
	@Override
	public ObjType getObjType() {
		return ObjType.RDTRAFFICSIGNAL;
	}

	@Override
	public String getRequester() {
		return requester;
	}

	public Map<Integer, List<Integer>> getNodeLinkPidMap() {
		return nodeLinkPidMap;
	}

	public void setNodeLinkPidMap(Map<Integer, List<Integer>> nodeLinkPidMap) {
		this.nodeLinkPidMap = nodeLinkPidMap;
	}

	public Command(JSONObject json, String requester) throws Exception{
		this.requester = requester;

		this.setDbId(json.getInt("dbId"));
		
		JSONObject data = json.getJSONObject("data");

		this.nodePid = data.getInt("nodePid");
	}

	public RdCross getCross() {
		return cross;
	}

	public void setCross(RdCross cross) {
		this.cross = cross;
	}
	
}
