package com.navinfo.dataservice.engine.edit.operation.obj.rdinter.create;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInter;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterLink;
import com.navinfo.dataservice.dao.glm.model.rd.inter.RdInterNode;

import net.sf.json.JSONArray;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		createRdInter(result);

		return msg;
	}

	/**
	 * @param nodeLinkPidMap
	 * @throws Exception
	 */
	private void createRdInter(Result result) throws Exception {

		RdInter rdInter = new RdInter();

		rdInter.setPid(PidUtil.getInstance().applyRdInterPid());

		JSONArray linkArray = this.command.getLinkArray();
		if (linkArray != null) {
			// 设置子表rd_inter_link
			for (int i = 0; i < linkArray.size(); i++) {
				int linkPid = linkArray.getInt(i);

				RdInterLink interLink = new RdInterLink();

				interLink.setLinkPid(linkPid);

				interLink.setPid(rdInter.getPid());

				interLink.setSeqNum(i + 1);

				rdInter.getLinks().add(interLink);
			}
		}

		JSONArray nodeArray = this.command.getNodeArray();
		// 设置子表rd_inter_node
		for (int i = 0; i < nodeArray.size(); i++) {
			int nodePid = nodeArray.getInt(i);

			RdInterNode interNode = new RdInterNode();

			interNode.setPid(rdInter.getPid());

			interNode.setNodePid(nodePid);

			rdInter.getNodes().add(interNode);
		}
		//判断是否存在组成要素
		if(CollectionUtils.isNotEmpty(rdInter.getNodes()) || CollectionUtils.isNotEmpty(rdInter.getLinks()))
		result.insertObject(rdInter, ObjStatus.INSERT, rdInter.getPid());
	}
}
