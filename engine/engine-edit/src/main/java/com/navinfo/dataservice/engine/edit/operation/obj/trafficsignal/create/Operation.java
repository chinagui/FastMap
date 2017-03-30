package com.navinfo.dataservice.engine.edit.operation.obj.trafficsignal.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCross;
import com.navinfo.dataservice.dao.glm.model.rd.cross.RdCrossNode;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.trafficsignal.RdTrafficsignal;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;


public class Operation implements IOperation {

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String msg = null;

		// 组装数据：key：cross的nodePid,value:node组成的link
		Map<Integer, List<Integer>> nodeLinkPidMap = getNodeLinkMap();

		createRdTrafficSignal(result, nodeLinkPidMap);

		return msg;
	}

	private Map<Integer, List<Integer>> getNodeLinkMap() throws Exception {
		// key：nodePid，value：node组成的Link
		Map<Integer, List<Integer>> nodeLinkPidMap = new HashMap<>();

		// 复合路口和简单路口统一处理
		List<IRow> nodes = this.command.getCross().getNodes();

		RdLinkSelector linkSelector = new RdLinkSelector(this.conn);
		
		for (IRow row : nodes) {
			// link form：50为交叉口道路
			List<RdLink> links = linkSelector.loadInLinkByNodePid(((RdCrossNode) row).getNodePid(), 50, true);

			List<Integer> linkPidList = new ArrayList<>();

			for (RdLink link : links) {
				Collection<List<Integer>> values = nodeLinkPidMap.values();

				for (List<Integer> pidList : values) {
					if (pidList.contains(link.getPid())) {
						continue;
					}
				}
				linkPidList.add(link.getPid());
			}

			if (linkPidList.size()>0) {
				nodeLinkPidMap.put(((RdCrossNode) row).getNodePid(), linkPidList);
			}
			
		}

		return nodeLinkPidMap;
	}

	/**
	 * @param nodeLinkPidMap
	 * @throws Exception
	 */
	private void createRdTrafficSignal(Result result, Map<Integer, List<Integer>> nodeLinkPidMap) throws Exception {
		if (nodeLinkPidMap.size() > 0) {
			// 复合路口和简单路口通用写法
			for (Map.Entry<Integer, List<Integer>> entry : nodeLinkPidMap.entrySet()) {
				int nodePid = entry.getKey();

				List<Integer> linkPidList = entry.getValue();

				for (int linkPid : linkPidList) {
					RdTrafficsignal rdTrafficsignal = createRdTrafficSignal(nodePid, linkPid);

					result.insertObject(rdTrafficsignal, ObjStatus.INSERT, rdTrafficsignal.pid());
				}
			}
			// 维护路口关系
			RdCross cross = this.command.getCross();

			cross.changedFields().put("signal", 1);

			result.insertObject(cross, ObjStatus.UPDATE, cross.pid());
		} else {
			throw new Exception("该路口没有进入线可以创建信号灯");
		}

	}

	/**
	 * @param linkPid
	 *            进入线Pid
	 * @throws Exception
	 */
	private RdTrafficsignal createRdTrafficSignal(int nodePid, int linkPid) throws Exception {
		RdTrafficsignal rdTrafficsignal = new RdTrafficsignal();

		rdTrafficsignal.setPid(PidUtil.getInstance().applyRdTrafficsignalPid());

		rdTrafficsignal.setLinkPid(linkPid);

		rdTrafficsignal.setNodePid(nodePid);

		// 默认为受控制
		rdTrafficsignal.setFlag(1);

		return rdTrafficsignal;

	}

}
