package com.navinfo.dataservice.engine.edit.operation.topo.breakin.breakrdpoint;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;

public class OpRefRestrict implements IOperation {

	private Command command;

	private Result result;

	public OpRefRestrict(Command command) {
		this.command = command;

	}

	@Override
	public String run(Result result) throws Exception {

		this.result = result;

		this.handleRdRestrictions(command.getRestrictions());

		this.handleRdRestrictionDetails(command.getRestrictionDetails());

		this.handleRdRestrictionVias(command.geListRestrictVias());

		return null;
	}

	// 处理交限进入线
	private void handleRdRestrictions(List<RdRestriction> list)
			throws Exception {

		for (RdRestriction rr : list) {
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

	// 处理交限退出线
	private void handleRdRestrictionDetails(List<RdRestrictionDetail> list)
			throws Exception {

		for (RdRestrictionDetail detail : list) {

			Map<String, Object> changedFields = detail.changedFields();

//			if (detail.igetOutNodePid() == command.getLink1().getsNodePid()
//					|| detail.igetOutNodePid() == command.getLink1()
//							.geteNodePid()) {
//
//				changedFields
//						.put("outLinkPid", command.getLink1().getPid());
//
//			} else {
//				changedFields
//						.put("outLinkPid", command.getLink2().getPid());
//			}

			result.insertObject(detail, ObjStatus.UPDATE, detail.parentPKValue());
		}

	}

	// 处理交限经过线

	private void handleRdRestrictionVias(List<List<Entry<Integer, RdRestrictionVia>>> list)
			throws Exception {

		for (List<Entry<Integer, RdRestrictionVia>> vias : list) {

			for (Entry<Integer, RdRestrictionVia> entry : vias) {
				
				RdRestrictionVia v = entry.getValue();
				
				if (v.getLinkPid() != command.getLinkPid()) {
					Map<String, Object> changedFields = v.changedFields();

					changedFields.put("seqNum", v.getSeqNum() + 1);

					result.insertObject(v, ObjStatus.UPDATE, entry.getKey());
				} else {

					RdRestrictionVia via1 = new RdRestrictionVia();

					RdRestrictionVia via2 = new RdRestrictionVia();

					via1.copy(v);

					via2.copy(v);

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

					result.insertObject(v, ObjStatus.DELETE, entry.getKey());

					result.insertObject(via1, ObjStatus.INSERT, entry.getKey());

					result.insertObject(via2, ObjStatus.INSERT, entry.getKey());

				}
			}
		}
	}
}
