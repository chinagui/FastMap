package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;

public class OpRefLaneConnexity implements IOperation {

	private Command command;

	private Result result;

	private Connection conn;

	public OpRefLaneConnexity(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleRdLaneConnexity(command.getLaneConnextys());

		this.handleRdLaneTopos(command.getLaneTopologys());

		this.handleRdLaneVias(command.getLaneVias());

		return null;
	}

	// 处理进入线
	private void handleRdLaneConnexity(List<RdLaneConnexity> list) throws Exception {

		for (RdLaneConnexity rr : list) {
			Map<String, Object> changedFields = rr.changedFields();

			int inLinkPid = 0;
//			if (rr.getNodePid() == command.getLink1().getsNodePid()) {
//
//				inLinkPid = command.getLink1().getPid();
//
//			} else {
//				inLinkPid = command.getLink2().getPid();
//			}

			changedFields.put("inLinkPid", inLinkPid);

			result.insertObject(rr, ObjStatus.UPDATE, rr.pid());

		}
	}

	// 处理退出线
	private void handleRdLaneTopos(List<RdLaneTopology> list) throws Exception {

		for (RdLaneTopology topo : list) {

			Map<String, Object> changedFields = topo.changedFields();

//			if (topo.igetOutNodePid() == command.getLink1().getsNodePid()
//					|| topo.igetOutNodePid() == command.getLink1().geteNodePid()) {
//
//				changedFields.put("outLinkPid", command.getLink1().getPid());
//
//			} else {
//				changedFields.put("outLinkPid", command.getLink2().getPid());
//			}

			result.insertObject(topo, ObjStatus.UPDATE, topo.parentPKValue());
		}

	}

	// 处理经过线
	private void handleRdLaneVias(List<List<Entry<Integer, RdLaneVia>>> list) throws Exception {

		for (List<Entry<Integer, RdLaneVia>> vias : list) {

			for (Entry<Integer, RdLaneVia> entry : vias) {

				RdLaneVia v = entry.getValue();

				if (v.getLinkPid() != command.getLinkPid()) {
					Map<String, Object> changedFields = v.changedFields();

					changedFields.put("seqNum", v.getSeqNum() + 1);

					result.insertObject(v, ObjStatus.UPDATE, entry.getKey());
				} else {

					RdLaneVia via1 = new RdLaneVia();

					RdLaneVia via2 = new RdLaneVia();

					via1.copy(v);

					via2.copy(v);

//					if (v.igetsNodePid() == command.getLink1().getsNodePid()
//							|| v.igetsNodePid() == command.getLink1().geteNodePid()) {
//						via1.setLinkPid(command.getLink1().getPid());
//						via2.setLinkPid(command.getLink2().getPid());
//
//					} else {
//						via1.setLinkPid(command.getLink2().getPid());
//						via2.setLinkPid(command.getLink1().getPid());
//					}

					via2.setSeqNum(via2.getSeqNum() + 1);

					via1.setTopologyId(v.getTopologyId());

					via2.setTopologyId(v.getTopologyId());

					result.insertObject(v, ObjStatus.DELETE, entry.getKey());

					result.insertObject(via1, ObjStatus.INSERT, entry.getKey());

					result.insertObject(via2, ObjStatus.INSERT, entry.getKey());

				}
			}
		}
	}
}
