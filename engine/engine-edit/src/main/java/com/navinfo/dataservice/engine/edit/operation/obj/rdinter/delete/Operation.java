package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;
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

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
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
	public void deleteByLink(List<Integer> linkPidList, List<Integer> nodePidList, Result result) throws Exception {
		RdInterSelector interSelector = new RdInterSelector(conn);

		// 根据nodePids查询组成的CRF交叉点
		List<RdInter> rdInters = new ArrayList<>();
		
		if(CollectionUtils.isNotEmpty(nodePidList))
		{
			String nodePids = StringUtils.getInteStr(nodePidList);
			
			// 根据nodePids查询组成的CRF交叉点
			rdInters = interSelector.loadInterByNodePid(nodePids, true);
		}
		else
		{
			rdInters = interSelector.loadRdInterByOutLinkPid(linkPidList, true);
		}

		if (CollectionUtils.isNotEmpty(rdInters)) {

			for (RdInter rdInter : rdInters) {

				List<Integer> interNodePidList = new ArrayList<>();

				// 删除的线单独参与某一个CRF交叉点的时候，删除线需要删除主表对象以及所有子表数据
				List<IRow> nodes = rdInter.getNodes();

				for (IRow row : nodes) {
					RdInterNode interNode = (RdInterNode) row;

					interNodePidList.add(interNode.getNodePid());
				}

				if (nodePidList.containsAll(interNodePidList)) {
					result.insertObject(rdInter, ObjStatus.DELETE, rdInter.getPid());
					// 维护CRF对象
					com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdobject.update.Operation(
							conn);
					operation.updateRdObjectForRdInter(rdInter, result);
				} else {
					// 只删除子表数据
					for (IRow row : nodes) {
						RdInterNode interNode = (RdInterNode) row;

						if (nodePidList.contains(interNode.getNodePid())) {
							result.insertObject(interNode, ObjStatus.DELETE, interNode.getPid());
						}
					}
					List<IRow> links = rdInter.getLinks();
					for (IRow row : links) {
						RdInterLink interLink = (RdInterLink) row;
						if (linkPidList.contains(interLink.getLinkPid())) {
							result.insertObject(interLink, ObjStatus.DELETE, interLink.getPid());
						}
					}
				}
			}
		}
	}

	/**
	 * 删除link对CRF交叉点的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdInterInfectData(List<Integer> linkPidList, List<Integer> nodePidList,
			Connection conn) throws Exception {
		RdInterSelector interSelector = new RdInterSelector(conn);

		List<RdInter> deleteInterList = new ArrayList<>();

		List<RdInter> rdInters = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(nodePidList))
		{
			String nodePids = StringUtils.getInteStr(nodePidList);
			
			// 根据nodePids查询组成的CRF交叉点
			rdInters = interSelector.loadInterByNodePid(nodePids, true);
		}
		else
		{
			rdInters = interSelector.loadRdInterByOutLinkPid(linkPidList, true);
		}

		if (CollectionUtils.isNotEmpty(rdInters)) {

			for (RdInter rdInter : rdInters) {

				List<Integer> interNodePidList = new ArrayList<>();

				// 删除的线单独参与某一个CRF交叉点的时候，删除线需要删除主表对象以及所有子表数据
				List<IRow> nodes = rdInter.getNodes();

				for (IRow row : nodes) {
					RdInterNode interNode = (RdInterNode) row;

					interNodePidList.add(interNode.getNodePid());
				}

				if (nodePidList.containsAll(interNodePidList)) {
					deleteInterList.add(rdInter);
				}
			}
		}

		List<AlertObject> alertList = new ArrayList<>();

		for (RdInter inter : deleteInterList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(inter.objType());

			alertObj.setPid(inter.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if (!alertList.contains(alertObj)) {
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
	public List<AlertObject> getUpdateRdInterInfectData(List<Integer> linkPidList, List<Integer> nodePidList,
			Connection conn) throws Exception {

		RdInterSelector interSelector = new RdInterSelector(conn);
		
		List<RdInter> rdInters = new ArrayList<>();

		List<RdInter> updateInterList = new ArrayList<>();
		
		if(CollectionUtils.isNotEmpty(nodePidList))
		{
			String nodePids = StringUtils.getInteStr(nodePidList);
			
			// 根据nodePids查询组成的CRF交叉点
			rdInters = interSelector.loadInterByNodePid(nodePids, true);
		}
		else
		{
			rdInters = interSelector.loadRdInterByOutLinkPid(linkPidList, true);
		}

		if (CollectionUtils.isNotEmpty(rdInters)) {

			for (RdInter rdInter : rdInters) {

				List<Integer> interNodePidList = new ArrayList<>();

				// 删除的线单独参与某一个CRF交叉点的时候，删除线需要删除主表对象以及所有子表数据
				List<IRow> nodes = rdInter.getNodes();

				for (IRow row : nodes) {
					RdInterNode interNode = (RdInterNode) row;

					interNodePidList.add(interNode.getNodePid());
				}

				if (!nodePidList.containsAll(interNodePidList)) {
					List<IRow> links = rdInter.getLinks();

					for (IRow row : links) {
						RdInterLink link = (RdInterLink) row;

						if (linkPidList.contains(link.getLinkPid())) {
							updateInterList.add(rdInter);
							break;
						}
					}
				}
			}
		}

		List<AlertObject> alertList = new ArrayList<>();

		for (RdInter inter : updateInterList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(inter.objType());

			alertObj.setPid(inter.getPid());

			alertObj.setStatus(ObjStatus.UPDATE);

			if (!alertList.contains(alertObj)) {
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
