package com.navinfo.dataservice.engine.edit.operation.topo.depart.updowndepartlink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.node.RdNode;
import com.navinfo.dataservice.engine.edit.operation.AbstractCommand;

/**
 * @author zhaokk 上下线分离基础参数类
 */
public class Command extends AbstractCommand {

	private String requester;
	private int projectId;
	private double distance;
	private List<Integer> linkPids;
	private List<RdLink> links;
	
	private RdNode sNode;
	private RdNode eNode;

	// 右侧link映射。Integer：原linkpid RdLink；新生成link
	private Map<Integer, RdLink> leftLinkMapping = new HashMap<Integer, RdLink>();

	// 左侧link映射。Integer：原linkpid RdLink；新生成link
	private Map<Integer, RdLink> rightLinkMapping = new HashMap<Integer, RdLink>();

	public List<RdLink> getLinks() {
		return links;
	}

	public void setLinks(List<RdLink> links) {
		this.links = links;
	}

	public double getDistance() {
		return distance;
	}

	public List<Integer> getLinkPids() {
		return linkPids;
	}

	public void setLinkPids(List<Integer> linkPids) {
		this.linkPids = linkPids;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public int getProjectId() {
		return projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public RdNode getsNode() {
		return sNode;
	}

	public void setsNode(RdNode sNode) {
		this.sNode = sNode;
	}
	
	public RdNode geteNode() {
		return eNode;
	}

	public void seteNode(RdNode eNode) {
		this.eNode = eNode;
	}

	public Map<Integer, RdLink> getLeftLinkMapping() {
		return leftLinkMapping;
	}

	public void setLeftLinkMapping(Map<Integer, RdLink> leftLinkMapping) {
		this.leftLinkMapping = leftLinkMapping;
	}

	public Map<Integer, RdLink> getRightLinkMapping() {
		return rightLinkMapping;
	}

	public void setRightLinkMapping(Map<Integer, RdLink> rightLinkMapping) {
		this.rightLinkMapping = rightLinkMapping;
	}

	@Override
	public OperType getOperType() {

		return OperType.UPDOWNDEPART;
	}

	@Override
	public ObjType getObjType() {
		return ObjType.RDLINK;
	}

	@Override
	public String getRequester() {

		return requester;
	}

	public Command(JSONObject json, String requester) {
		this.requester = requester;
		this.setDbId(json.getInt("dbId"));
		// 移动距离
		this.distance = json.getInt("distance");
		// 获取要上下线分离的linkPids
		JSONObject data = json.getJSONObject("data");
		JSONArray array = data.getJSONArray("linkPids");

		linkPids = new ArrayList<Integer>();

		for (int i = 0; i < array.size(); i++) {

			int pid = Integer.valueOf(array.getString(i));

			if (!linkPids.contains(pid)) {
				linkPids.add(pid);
			}
		}

	}

}
