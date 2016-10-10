package com.navinfo.dataservice.engine.edit.operation.obj.rdsamenode.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameLinkPart;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNode;
import com.navinfo.dataservice.dao.glm.model.rd.same.RdSameNodePart;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.same.RdSameNodeSelector;

/**
 * 
 * @ClassName: Operation
 * @author Zhang Xiaolong
 * @date 2016年7月20日 下午7:39:02
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

		result.insertObject(command.getRdSameNode(), ObjStatus.DELETE, command.getPid());

		// 删除存在同一线关系的同一点，则同时删除同一线关系
		// TODO
		return null;
	}

	/**
	 *  删除线维护同一关系
	 * @param sNodePid 删除link的起点
	 * @param eNodePid 删除link的终点
	 * @param tableName 表名称
	 * @param result
	 * @throws Exception
	 */
	public void deleteByLink(int sNodePid,int eNodePid, String tableName, Result result) throws Exception {
		if (conn == null) {
			return;
		}
		RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);

		List<RdSameNode> sameNodes = sameNodeSelector.loadSameNodeByNodePids(sNodePid+","+eNodePid, tableName, true);
		
		if(CollectionUtils.isNotEmpty(sameNodes))
		{
			for(RdSameNode sameNode : sameNodes)
			{
				// 删除线后对应删除点，如果同一关系组成点只剩一个点，需要删除主表对象以及所有子表数据
				List<IRow> parts = sameNode.getParts();
				
				if(parts.size() == 2)
				{
					result.insertObject(sameNode, ObjStatus.DELETE, sameNode.getPid());
				}
				else if(parts.size()>2)
				{
					deleteSameNodePart(sNodePid,eNodePid,parts,result);
				}
			}
		}
	}
	
	/**
	 * 删除子表数据
	 * @param sNodePid link的起点
	 * @param eNodePid link的终点
	 * @param parts 子表
	 * @param result 结果集
	 */
	private void deleteSameNodePart(int sNodePid,int eNodePid, List<IRow> parts, Result result) {
		for (IRow row : parts) {
			RdSameNodePart nodePart = (RdSameNodePart) row;
			if (nodePart.getNodePid() == sNodePid || nodePart.getNodePid() == eNodePid) {
				result.insertObject(nodePart, ObjStatus.DELETE, nodePart.getNodePid());
			}
		}
	}
	
	/**
	 * 删除link对同一点的影响
	 * @return
	 * @throws Exception 
	 */
	public List<AlertObject> getDeleteLinkSameNodeInfectData(int sNodePid,int eNodePid,String tableName,Connection conn) throws Exception {
		
		RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);
		
		List<AlertObject> alertList = new ArrayList<>();

		List<RdSameNode> sameNodes = sameNodeSelector.loadSameNodeByNodePids(sNodePid+","+eNodePid, tableName, true);
		
		if(CollectionUtils.isNotEmpty(sameNodes))
		{
			for(RdSameNode sameNode : sameNodes)
			{
				// 删除线后对应删除点，如果同一关系组成点只剩一个点，需要删除主表对象以及所有子表数据
				List<IRow> parts = sameNode.getParts();
				
				if(parts.size() == 2)
				{
					AlertObject alertObj = new AlertObject();

					alertObj.setObjType(sameNode.objType());

					alertObj.setPid(sameNode.getPid());

					alertObj.setStatus(ObjStatus.DELETE);

					alertList.add(alertObj);
				}
				else if(parts.size()>2)
				{
					AlertObject alertObj = new AlertObject();

					alertObj.setObjType(sameNode.objType());

					alertObj.setPid(sameNode.getPid());

					alertObj.setStatus(ObjStatus.UPDATE);

					alertList.add(alertObj);
				}
			}
		}

		return alertList;
	}
	
	public void deleteByUpDownPartLink(int sNodePid, int eNodePid,
			List<RdLink> targetLinks, Result result) throws Exception {
		// 目标link之间的挂接node
		List<Integer> nodePids = new ArrayList<Integer>();

		// 目标linkPid
		List<Integer> targetLinkPids = new ArrayList<Integer>();

		for (RdLink link : targetLinks) {

			if (!targetLinkPids.contains(link.getPid())) {
				targetLinkPids.add(link.getPid());
			}

			if (!nodePids.contains(link.getsNodePid())) {

				nodePids.add(link.getsNodePid());
			}

			if (!nodePids.contains(link.geteNodePid())) {

				nodePids.add(link.geteNodePid());
			}

			// 过滤端点
			if (nodePids.contains(sNodePid)) {

				nodePids.remove((Integer) sNodePid);
			}

			if (nodePids.contains(eNodePid)) {

				nodePids.remove((Integer) eNodePid);
			}
		}
		
		List<RdSameNode> delSameNodes =new  ArrayList<RdSameNode>();
		
		RdSameNodeSelector sameNodeSelector = new RdSameNodeSelector(conn);
		
		if (nodePids.size() > 0) {
			
			String ids = StringUtils.getInteStr(nodePids);			

			// 目标link串中间点上的同一点关系全部删除；
			delSameNodes = sameNodeSelector.loadSameNodeByNodePids(ids,
					"RD_NODE", true);
		}

		// 目标link串的两个端点如果有同一点关系，若该点参与的同一node关系组中的所有node挂接的link中均不参与另一同一线关系，则删除该同一点；
		List<RdSameNode> sameNodes = sameNodeSelector.loadSameNodeByNodePids(
				String.valueOf(sNodePid), "RD_NODE", true);

		if (sameNodes.size() > 0) {

			boolean haveSamelink = haveSamelink(sNodePid, targetLinkPids);

			if (!haveSamelink) {
				delSameNodes.addAll(sameNodes);
			}
		}

		sameNodes = sameNodeSelector.loadSameNodeByNodePids(
				String.valueOf(eNodePid), "RD_NODE", true);

		if (sameNodes.size() > 0) {

			boolean haveSamelink = haveSamelink(eNodePid, targetLinkPids);

			if (!haveSamelink) {
				delSameNodes.addAll(sameNodes);
			}
		}

		for (RdSameNode sameNode : sameNodes) {

			result.insertObject(sameNode, ObjStatus.DELETE, sameNode.getPid());
		}
	}

	private boolean haveSamelink(int nodePid, List<Integer> targetLinkPids)
			throws Exception {
		
		RdLinkSelector linkSelector = new RdLinkSelector(this.conn);

		RdSameLinkSelector sameLinkSelector = new RdSameLinkSelector(this.conn);

		List<Integer> linkPids = linkSelector.loadLinkPidByNodePid(nodePid,
				true);

		for (int linkPid : linkPids) {

			if (targetLinkPids.contains(linkPid)) {

				continue;
			}

			RdSameLinkPart sameLinkPart = sameLinkSelector.loadLinkPartByLink(
					linkPid, "RD_LINK", true);

			if (sameLinkPart != null) {
				return true;
			}
		}

		return false;
	}
}
