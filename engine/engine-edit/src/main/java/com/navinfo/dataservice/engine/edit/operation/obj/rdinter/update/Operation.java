package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

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

	public Operation(Command command) {
		this.command = command;
		this.rdInter = this.command.getRdInter();
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 不编辑主表信息

		// node子表
		if (content.containsKey("nodes")) {
			updateNode(result, content);
		}

		// link子表
		if (content.containsKey("links")) {
			updateLink(result, content);
		}

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
		JSONArray subObj = content.getJSONArray("nodes");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					RdInterNode row = rdInter.nodeMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的RdInterNode不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, rdInter.pid());
						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, rdInter.pid());
						}
					}
				} else {
					RdInterNode rdInterNode = new RdInterNode();

					rdInterNode.setNodePid(json.getInt("nodePid"));

					rdInterNode.setPid(rdInter.getPid());

					result.insertObject(rdInterNode, ObjStatus.INSERT, rdInterNode.getPid());
				}
			}
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
		JSONArray subObj = content.getJSONArray("links");

		for (int i = 0; i < subObj.size(); i++) {

			JSONObject json = subObj.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(json.getString("objStatus"))) {

					RdInterLink row = rdInter.linkMap.get(json.getString("rowId"));

					if (row == null) {
						throw new Exception("rowId=" + json.getString("rowId") + "的RdInterLink不存在");
					}

					if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {
						result.insertObject(row, ObjStatus.DELETE, rdInter.pid());
						continue;
					} else if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

						boolean isChanged = row.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(row, ObjStatus.UPDATE, rdInter.pid());
						}
					}
				} else {
					RdInterLink rdInterLink = new RdInterLink();

					rdInterLink.setLinkPid(json.getInt("linkPid"));

					rdInterLink.setPid(rdInter.getPid());

					result.insertObject(rdInterLink, ObjStatus.INSERT, rdInterLink.getPid());
				}
			}
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
			result.insertObject(interLink, ObjStatus.DELETE, interLink.getLinkPid());

			List<Integer> oldNodePidList = new ArrayList<>();

			oldNodePidList.add(oldLink.getsNodePid());

			oldNodePidList.add(oldLink.geteNodePid());

			// 新增rd_inter_link表中数据
			for (RdLink link : newLinks) {

				if (!oldNodePidList.contains(link.getsNodePid())) {
					RdInterNode interNode = new RdInterNode();

					interNode.setPid(interLink.getPid());

					interNode.setNodePid(link.getsNodePid());

					result.insertObject(interNode, ObjStatus.INSERT, interNode.getNodePid());
				}
				if (!oldNodePidList.contains(link.geteNodePid())) {
					RdInterNode interNode = new RdInterNode();

					interNode.setPid(interLink.getPid());

					interNode.setNodePid(link.geteNodePid());

					result.insertObject(interNode, ObjStatus.INSERT, interNode.getNodePid());
				}

				RdInterLink newInterLink = new RdInterLink();

				newInterLink.setPid(interLink.getPid());

				newInterLink.setLinkPid(link.getPid());

				result.insertObject(newInterLink, ObjStatus.INSERT, interLink.getLinkPid());
			}
		}
	}

	/**
	 * @param linkPid
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLink(RdLink link, Result result) throws Exception {
		RdInterSelector interSelector = new RdInterSelector(conn);

		String nodePids = link.getsNodePid() + "," + link.geteNodePid();

		// 根据nodePids查询组成的CRF交叉点
		List<RdInter> rdInters = interSelector.loadInterByNodePid(nodePids, true);

		if (CollectionUtils.isNotEmpty(rdInters)) {
			List<Integer> nodePidList = new ArrayList<>();

			nodePidList.add(link.getsNodePid());

			nodePidList.add(link.geteNodePid());

			for (RdInter rdInter : rdInters) {
				// 删除的线单独参与某一个CRF交叉点的时候，删除线需要删除主表对象以及所有子表数据
				List<IRow> nodes = rdInter.getNodes();

				if (nodes.size() == 1) {
					RdInterNode interNode = (RdInterNode) nodes.get(0);

					if (nodePidList.contains(interNode.getNodePid())) {
						result.insertObject(rdInter, ObjStatus.DELETE, rdInter.getPid());
						break;
					}
				} else if (nodes.size() == 2) {
					RdInterNode interNode_1 = (RdInterNode) nodes.get(0);

					RdInterNode interNode_2 = (RdInterNode) nodes.get(1);
					if (nodePidList.contains(interNode_1.getNodePid())
							&& nodePidList.contains(interNode_2.getNodePid())) {
						result.insertObject(rdInter, ObjStatus.DELETE, rdInter.getPid());
						break;
					}
				}
				//只删除子表数据
				List<IRow> links = rdInter.getLinks();
				
				deleteInterNodeAndInterLink(link,nodes,links,result);
			}
		}
	}

	/**
	 * 删除子表数据
	 * @param link 原始删除的link
	 * @param nodes crf交叉点组成node
	 * @param links crf交叉点组成link
	 * @param result 结果集
	 */
	private void deleteInterNodeAndInterLink(RdLink link, List<IRow> nodes, List<IRow> links, Result result) {

		for (IRow row : nodes) {
			RdInterNode interNode = (RdInterNode) row;
			if (interNode.getNodePid() == link.getsNodePid() || interNode.getNodePid() == link.geteNodePid()) {
				result.insertObject(interNode, ObjStatus.DELETE, interNode.getNodePid());
			}
		}
		for(IRow row : links)
		{
			RdInterLink interLink = (RdInterLink)row;
			if(interLink.getLinkPid() == link.getPid())
			{
				result.insertObject(interLink, ObjStatus.DELETE, interLink.getLinkPid());
			}
		}
	}
}
