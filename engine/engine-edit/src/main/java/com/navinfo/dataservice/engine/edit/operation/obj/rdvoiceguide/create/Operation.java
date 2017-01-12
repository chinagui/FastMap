package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

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

		msg = create(result);

		return msg;
	}

	private String create(Result result) throws Exception {

		RdVoiceguide voiceguide = new RdVoiceguide();

		voiceguide.setPid(PidUtil.getInstance().applyRdVoiceguidePid());

		voiceguide.setInLinkPid(this.command.getInLinkPid());

		voiceguide.setNodePid(this.command.getNodePid());

		List<IRow> details = new ArrayList<IRow>();

		CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils(conn);

		// 创建RdVoiceguideDetail
		for (int outLinkPid : this.command.getOutLinkPids()) {

			RdVoiceguideDetail detail = new RdVoiceguideDetail();

			detail.setPid(PidUtil.getInstance().applyRdVoiceguideDetailPid());
			
			detail.setOutLinkPid(outLinkPid);

			detail.setVoiceguidePid(voiceguide.getPid());

			detail.setRelationshipType(calLinkOperateUtils.getRelationShipType(
					command.getNodePid(), outLinkPid));

			List<Integer> viaLinks = calLinkOperateUtils.calViaLinks(conn,
					command.getInLinkPid(), command.getNodePid(), outLinkPid);

			int seqNum = 1;

			List<IRow> vias = new ArrayList<IRow>();

			// 创建RdVoiceguideVia
			for (Integer linkPid : viaLinks) {

				RdVoiceguideVia via = new RdVoiceguideVia();

				via.setDetailId(detail.getPid());

				via.setLinkPid(linkPid);

				via.setSeqNum(seqNum);

				vias.add(via);

				seqNum++;
			}

			detail.setVias(vias);

			details.add(detail);
		}

		voiceguide.setDetails(details);

		result.insertObject(voiceguide, ObjStatus.INSERT, voiceguide.pid());

		return null;
	}

}
