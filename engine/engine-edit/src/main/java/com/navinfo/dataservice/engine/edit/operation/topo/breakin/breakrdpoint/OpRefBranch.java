package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;

public class OpRefBranch implements IOperation {

	private Command command;

	private Result result;

	public OpRefBranch(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleRdBranchsIn(command.getInBranchs());
		
		this.handleRdBranchsOut(command.getOutBranchs());
		
		this.handleRdBranchVias(command.getBranchVias());

		return null;
	}

	// 处理交限进入线
	private void handleRdBranchsIn(List<RdBranch> list)
			throws Exception {

		for (RdBranch branch : list) {
			Map<String, Object> changedFields = branch.changedFields();

			int inLinkPid = 0;
//			if (branch.getNodePid() == command.getLink1().getsNodePid()) {
//
//				inLinkPid = command.getLink1().getPid();
//
//			} else {
//				inLinkPid = command.getLink2().getPid();
//			}

			changedFields.put("inLinkPid", inLinkPid);

			result.insertObject(branch, ObjStatus.UPDATE, branch.pid());

		}
	}

	// 处理交限退出线
	private void handleRdBranchsOut(List<RdBranch> list)
			throws Exception {

		for (RdBranch branch : list) {

			Map<String, Object> changedFields = branch.changedFields();

//			if (branch.igetOutNodePid() == command.getLink1().getsNodePid()
//					|| branch.igetOutNodePid() == command.getLink1()
//							.geteNodePid()) {
//
//				changedFields
//						.put("outLinkPid", command.getLink1().getPid());
//
//			} else {
//				changedFields
//						.put("outLinkPid", command.getLink2().getPid());
//			}

			result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
		}

	}

	// 处理交限经过线

	private void handleRdBranchVias(List<List<RdBranchVia>> list)
			throws Exception {

		for (List<RdBranchVia> vias : list) {

			for (RdBranchVia v : vias) {
				if (v.getLinkPid() != command.getLinkPid()) {
					Map<String, Object> changedFields = v.changedFields();

					changedFields.put("seqNum", v.getSeqNum() + 1);

					result.insertObject(v, ObjStatus.UPDATE, v.parentPKValue());
				} else {

					RdBranchVia via1 = new RdBranchVia();

					RdBranchVia via2 = new RdBranchVia();

					via1.copy(v);

					via2.copy(v);
					
					via1.setBranchPid(v.getBranchPid());
					
					via2.setBranchPid(v.getBranchPid());

//					if (v.igetsNodePid() == command.getLink1().getsNodePid()
//							|| v.igetsNodePid() == command.getLink1()
//									.geteNodePid()) {
//						via1.setLinkPid(command.getLink1().getPid());
//						via2.setLinkPid(command.getLink2().getPid());
//
//					} else {
//						via1.setLinkPid(command.getLink2().getPid());
//						via2.setLinkPid(command.getLink1().getPid());
//					}

					via2.setSeqNum(via2.getSeqNum() + 1);

					result.insertObject(v, ObjStatus.DELETE, v.parentPKValue());

					result.insertObject(via1, ObjStatus.INSERT, via1.parentPKValue());

					result.insertObject(via2, ObjStatus.INSERT, via1.parentPKValue());

				}
			}
		}
	}
}
