package com.navinfo.dataservice.engine.edit.edit.operation.topo.breakpoint;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.engine.edit.edit.model.ObjStatus;
import com.navinfo.dataservice.engine.edit.edit.model.Result;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.engine.edit.edit.model.bean.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.engine.edit.edit.operation.IOperation;

public class OpRefLaneConnexity implements IOperation {

	private Command command;

	private Result result;

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

	// 处理交限进入线
	private void handleRdLaneConnexity(List<RdLaneConnexity> list)
			throws Exception {

		for (RdLaneConnexity rr : list) {
			Map<String, Object> changedFields = rr.changedFields();

			int inLinkPid = 0;
			if (rr.getNodePid() == command.getLink1().getsNodePid()) {

				inLinkPid = command.getLink1().getPid();

			} else {
				inLinkPid = command.getLink2().getPid();
			}

			changedFields.put("inLinkPid", inLinkPid);

			result.insertObject(rr, ObjStatus.UPDATE);

		}
	}

	// 处理交限退出线
	private void handleRdLaneTopos(List<RdLaneTopology> list)
			throws Exception {

		for (RdLaneTopology topo : list) {

			Map<String, Object> changedFields = topo.changedFields();

			if (topo.igetOutNodePid() == command.getLink1().getsNodePid()
					|| topo.igetOutNodePid() == command.getLink1()
							.geteNodePid()) {

				changedFields
						.put("outLinkPid", command.getLink1().getPid());

			} else {
				changedFields
						.put("outLinkPid", command.getLink2().getPid());
			}

			result.insertObject(topo, ObjStatus.UPDATE);
		}

	}

	// 处理交限经过线

	private void handleRdLaneVias(List<List<RdLaneVia>> list)
			throws Exception {

		for (List<RdLaneVia> vias : list) {

			for (RdLaneVia v : vias) {
				if (v.getLinkPid() != command.getLinkPid()) {
					Map<String, Object> changedFields = v.changedFields();

					changedFields.put("seqNum", v.getSeqNum() + 1);

					result.insertObject(v, ObjStatus.UPDATE);
				} else {

					RdLaneVia via1 = new RdLaneVia();

					RdLaneVia via2 = new RdLaneVia();

					via1.copy(v);

					via2.copy(v);

					if (v.igetsNodePid() == command.getLink1().getsNodePid()
							|| v.igetsNodePid() == command.getLink1()
									.geteNodePid()) {
						via1.setLinkPid(command.getLink1().getPid());
						via2.setLinkPid(command.getLink2().getPid());

					} else {
						via1.setLinkPid(command.getLink2().getPid());
						via2.setLinkPid(command.getLink1().getPid());
					}

					via2.setSeqNum(via2.getSeqNum() + 1);

					result.insertObject(v, ObjStatus.DELETE);

					result.insertObject(via1, ObjStatus.INSERT);

					result.insertObject(via2, ObjStatus.INSERT);

				}
			}
		}
	}
}
