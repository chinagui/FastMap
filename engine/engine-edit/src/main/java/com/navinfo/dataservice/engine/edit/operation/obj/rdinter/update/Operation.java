package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:27
 * @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;

	private RdInter rdInter;

	private Connection conn;

	public Operation(Command command,Connection conn) {
		this.command = command;
		this.rdInter = this.command.getRdInter();
		this.conn = conn;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 不编辑主表信息
		JSONArray subNodeObj = content.getJSONArray("nodes");
		
		JSONArray subLinkObj = content.getJSONArray("links");
		
		if(subLinkObj.size() ==0 && subNodeObj.size() == 0)
		{
			com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Command command = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Command(
					rdInter.getPid(),rdInter);
			
			com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete.Operation(
					command,conn);
			
			operation.run(result);
			
			return null;
		}
		
		// node子表
		if (content.containsKey("nodes")) {
			updateNode(result, content);
		}

		// link子表
		if (content.containsKey("links")) {
			updateLink(result, content);
		}
		
		result.setPrimaryPid(rdInter.parentPKValue());
		
		return null;
	}

	/**
	 * 跟新rd_inter_node子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateNode(Result result, JSONObject content) throws Exception {
		JSONArray subObj = this.command.getNodeArray();

		for (IRow interNode : rdInter.getNodes()) {
			RdInterNode node = (RdInterNode) interNode;

			if (!subObj.contains(node.getNodePid())) {
				result.insertObject(node, ObjStatus.DELETE, rdInter.pid());
			} else {
				subObj.remove((Integer)node.getNodePid());
			}
		}
		for (int i = 0; i < subObj.size(); i++) {

			RdInterNode rdInterNode = new RdInterNode();

			rdInterNode.setNodePid(subObj.getInt(i));

			rdInterNode.setPid(rdInter.getPid());

			result.insertObject(rdInterNode, ObjStatus.INSERT, rdInterNode.getPid());
		}

	}

	/**
	 * 更新link子表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateLink(Result result, JSONObject content) throws Exception {
		JSONArray subObj = this.command.getLinkArray();

		for (IRow interLink : rdInter.getLinks()) {
			RdInterLink link = (RdInterLink) interLink;

			if (!subObj.contains(link.getLinkPid())) {
				result.insertObject(interLink, ObjStatus.DELETE, rdInter.pid());
			} else {
				subObj.remove((Integer)link.getLinkPid());
			}
		}

		for (int i = 0; i < subObj.size(); i++) {

			RdInterLink rdInterLink = new RdInterLink();

			rdInterLink.setLinkPid(subObj.getInt(i));

			rdInterLink.setPid(rdInter.getPid());

			result.insertObject(rdInterLink, ObjStatus.INSERT, rdInterLink.getPid());
		}
	}

	/**
	 * 打断link维护CRF交叉点
	 * 
	 * @param oldLink
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void breakRdLink(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {
		if (conn == null) {
			return;
		}

		RdInterSelector interSelector = new RdInterSelector(conn);

		RdInterLink interLink = interSelector.loadByLinkPid(oldLink.getPid(), true);

		if (interLink != null) {
			result.insertObject(interLink, ObjStatus.DELETE, interLink.getPid());
			
			List<Integer> hasAddNodePidList = new ArrayList<>();

			List<Integer> oldNodePidList = new ArrayList<>();

			oldNodePidList.add(oldLink.getsNodePid());

			oldNodePidList.add(oldLink.geteNodePid());

			// 新增rd_inter_link表中数据
			for (RdLink link : newLinks) {

				if (!oldNodePidList.contains(link.getsNodePid()) && !hasAddNodePidList.contains(link.getsNodePid())) {
					RdInterNode interNode = new RdInterNode();

					interNode.setPid(interLink.getPid());

					interNode.setNodePid(link.getsNodePid());

					result.insertObject(interNode, ObjStatus.INSERT, interNode.getPid());
					
					hasAddNodePidList.add(interNode.getNodePid());
				}
				if (!oldNodePidList.contains(link.geteNodePid()) && !hasAddNodePidList.contains(link.getsNodePid())) {
					RdInterNode interNode = new RdInterNode();

					interNode.setPid(interLink.getPid());

					interNode.setNodePid(link.geteNodePid());

					result.insertObject(interNode, ObjStatus.INSERT, interNode.getPid());
					
					hasAddNodePidList.add(interNode.getNodePid());
				}

				RdInterLink newInterLink = new RdInterLink();

				newInterLink.setPid(interLink.getPid());

				newInterLink.setLinkPid(link.getPid());

				result.insertObject(newInterLink, ObjStatus.INSERT, interLink.getPid());
			}
		}
	}
}
