package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.crf.RdInterSelector;

/**
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:02
 * @Description: TODO
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command) {
		this.command = command;
	}

	public Operation(Connection conn) {
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		result.insertObject(command.getRdInter(), ObjStatus.DELETE, command.getPid());

		// 维护CRFO:如果删除的CRFI属于某个CRFO，要从CRFO组成信息中去掉
		com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.delete.Operation(
				conn);

		List<Integer> pidList = new ArrayList<>();

		pidList.add(command.getPid());

		operation.deleteByType(pidList, ObjType.RDINTER, result);

		return null;
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
						// 维护CRF对象
						com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
								conn);
						operation.updateRdObjectForRdInter(rdInter, result);
						break;
					}
				} else if (nodes.size() == 2) {
					RdInterNode interNode_1 = (RdInterNode) nodes.get(0);

					RdInterNode interNode_2 = (RdInterNode) nodes.get(1);
					if (nodePidList.contains(interNode_1.getNodePid())
							&& nodePidList.contains(interNode_2.getNodePid())) {
						result.insertObject(rdInter, ObjStatus.DELETE, rdInter.getPid());
						// 维护CRF对象
						com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
								conn);
						operation.updateRdObjectForRdInter(rdInter, result);
						break;
					}
				}
				// 只删除子表数据
				List<IRow> links = rdInter.getLinks();

				deleteInterNodeAndInterLink(link, nodes, links, result);
			}
		}
	}

	/**
	 * 删除子表数据
	 * 
	 * @param link
	 *            原始删除的link
	 * @param nodes
	 *            crf交叉点组成node
	 * @param links
	 *            crf交叉点组成link
	 * @param result
	 *            结果集
	 */
	private void deleteInterNodeAndInterLink(RdLink link, List<IRow> nodes, List<IRow> links, Result result) {

		for (IRow row : nodes) {
			RdInterNode interNode = (RdInterNode) row;
			if (interNode.getNodePid() == link.getsNodePid() || interNode.getNodePid() == link.geteNodePid()) {
				result.insertObject(interNode, ObjStatus.DELETE, interNode.getPid());
			}
		}
		for (IRow row : links) {
			RdInterLink interLink = (RdInterLink) row;
			if (interLink.getLinkPid() == link.getPid()) {
				result.insertObject(interLink, ObjStatus.DELETE, interLink.getPid());
			}
		}
	}

	/**
	 * 删除link对CRF交叉点的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdInterInfectData(int linkPid, Connection conn) throws Exception {

		RdInterSelector selector = new RdInterSelector(conn);

		List<RdInter> intersList = selector.loadRdInterByOutLinkPid(linkPid, true);

		List<RdInter> deleteInterList = new ArrayList<>();

		for (RdInter rdInter : intersList) {
			List<IRow> inters = rdInter.getLinks();

			if (inters.size() == 1) {
				deleteInterList.add(rdInter);
			}
		}

		List<AlertObject> alertList = new ArrayList<>();

		for (RdInter inter : deleteInterList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(inter.objType());

			alertObj.setPid(inter.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除link对CRF交叉点的更新影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getUpdateRdInterInfectData(int linkPid, Connection conn) throws Exception {

		RdInterSelector selector = new RdInterSelector(conn);

		List<RdInter> intersList = selector.loadRdInterByOutLinkPid(linkPid, true);

		List<RdInter> updateInterList = new ArrayList<>();

		for (RdInter rdInter : intersList) {
			List<IRow> inters = rdInter.getLinks();

			if (inters.size() > 1) {
				updateInterList.add(rdInter);
			}
		}

		List<AlertObject> alertList = new ArrayList<>();

		for (RdInter inter : updateInterList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(inter.objType());

			alertObj.setPid(inter.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
