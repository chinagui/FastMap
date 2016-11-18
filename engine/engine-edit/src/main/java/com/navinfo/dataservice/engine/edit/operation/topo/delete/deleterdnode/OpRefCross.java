package com.navinfo.dataservice.engine.edit.operation.topo.delete.deleterdnode;

import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossLink;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;

public class OpRefCross implements IOperation {

	private Command command;
	
	public OpRefCross(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		List<Integer> nodePids = command.getNodePids();

		for (RdCross cross : command.getCrosses()) {
			deleteCross(cross, result, nodePids);
		}

		return null;
	}

	private void deleteCross(RdCross cross, Result result, List<Integer> nodePids) {

		List<IRow> nodes = new ArrayList<IRow>();
		
		//不删除的路口点
		List<RdCrossNode> otherCrossNode = new ArrayList<>();
		
		boolean isDeleteMainNode = false;

		// 判断线上是否还存在其他路口：1.存在,将关系表数据删除2.不存在，直接讲路口删除
		for (IRow row : cross.getNodes()) {
			RdCrossNode node = (RdCrossNode) row;

			if (nodePids.contains(node.getNodePid())) {
				if(node.getIsMain() == 1)
				{
					isDeleteMainNode = true;
				}
				nodes.add(node);
			}
			else
			{
				otherCrossNode.add(node);
			}
		}

		if (nodes.size() == cross.getNodes().size()) {
			result.insertObject(cross, ObjStatus.DELETE, cross.pid());
		} else {
			for (IRow node : nodes) {
				result.insertObject(node, ObjStatus.DELETE, cross.pid());
			}

			for (IRow row : cross.getLinks()) {
				RdCrossLink link = (RdCrossLink) row;

				if (command.getLinkPids().contains(link.getLinkPid())) {
					result.insertObject(link, ObjStatus.DELETE, cross.pid());
					break;
				}
			}
			
			//删除复合路口的一个点位后，剩余点位中选取某一个自动点位作为主点，并且如果只剩一个点，则将路口维护为简单路口
			int otherCrossNodeSize = otherCrossNode.size();
			
			if(otherCrossNode.size()>0)
			{
				if(otherCrossNodeSize == 1)
				{
					//维护为简单路口
					cross.changedFields().put("type", 0);
					result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
				}
				//删除主点后需要重新赋值主点
				if(isDeleteMainNode)
				{
					RdCrossNode crossNode = otherCrossNode.get(0);
					
					crossNode.changedFields().put("isMain", 1);
					
					result.insertObject(crossNode, ObjStatus.UPDATE, crossNode.getPid());
				}
			}
		}
	}
}
