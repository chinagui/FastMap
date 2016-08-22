package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.delete;

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
		
		//维护CRFO:如果删除的CRFI属于某个CRFO，要从CRFO组成信息中去掉
		//TODO
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
