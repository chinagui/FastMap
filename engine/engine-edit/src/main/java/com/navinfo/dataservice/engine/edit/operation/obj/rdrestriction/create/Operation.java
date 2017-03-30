package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Check check;

	private Command command;

	private Connection conn;

	public Operation(Command command, Connection conn, Check check) {
		this.command = command;

		this.conn = conn;

		this.check = check;
	}

	@Override
	public String run(Result result) throws Exception {

		RdRestriction restrict = new RdRestriction();
		restrict.setPid(PidUtil.getInstance().applyRestrictionPid());
		result.setPrimaryPid(restrict.getPid());
		int inNodePid = command.getNodePid();
		int inLinkPid = command.getInLinkPid();
		restrict.setInLinkPid(inLinkPid);
		restrict.setNodePid(inNodePid);
		List<IRow> details = new ArrayList<>();
		details.addAll(createDetail(restrict));
		restrict.setDetails(details);
		restrict.setRestricInfo(command.getRestricInfos());
		result.insertObject(restrict, ObjStatus.INSERT, restrict.pid());
		return null;
	}

	/**
	 * 创建Detial对象
	 * 
	 * @param restrict
	 * @return
	 * @throws Exception
	 */
	public List<RdRestrictionDetail> createDetail(RdRestriction restrict)
			throws Exception {

		List<RdRestrictionDetail> details = new ArrayList<>();

		for (int i = 0; i < this.command.getCalOutLinkObjs().size(); i++) {

			JSONObject obj = this.command.getCalOutLinkObjs().getJSONObject(i);
			RdRestrictionDetail detail = new RdRestrictionDetail();
			int outLinkPid = obj.getInt("outLinkPid");
			String info = obj.getString("arrow");
			int relationshipType = obj.getInt("relationshipType");
			detail.setOutLinkPid(outLinkPid);
			if (info.contains("[")) {
				detail.setFlag(2);
			} else {
				detail.setFlag(1);
			}

			detail.setMesh(restrict.mesh());

			detail.setPid(PidUtil.getInstance().applyRestrictionDetailPid());

			detail.setRestricPid(restrict.getPid());

			detail.setRestricInfo(CalLinkOperateUtils.calIntInfo(info));

			detail.setRelationshipType(relationshipType);

			if (relationshipType == 1) {
				check.checkGLM26017(conn, command.getNodePid());

				check.checkGLM08033(conn, command.getInLinkPid(), outLinkPid);
			}

			int seqNum = 1;

			List<IRow> vias = new ArrayList<IRow>();
			if (obj.containsKey("vias") && relationshipType == 2) {
				JSONArray viaArray = obj.getJSONArray("vias");
				if (viaArray.size() > 0) {
					for (int j = 0; j < viaArray.size(); j++) {
						int viaLinkPid = viaArray.getInt(j);
						RdRestrictionVia via = new RdRestrictionVia();

						via.setMesh(restrict.mesh());

						via.setDetailId(detail.getPid());

						via.setSeqNum(seqNum);

						via.setLinkPid(viaLinkPid);

						vias.add(via);

						seqNum++;
					}
				}
				detail.setVias(vias);
			}

			details.add(detail);

			// 创建交限时间段和车辆限制信息
			if (command.getRestricType() == 1) {
				detail.setConditions(createRdRestrictionConditions(detail));
			}
		}
		return details;
	}

	private List<IRow> createRdRestrictionConditions(RdRestrictionDetail detail) {
		List<IRow> conditions = new ArrayList<>();
		RdRestrictionCondition condition = new RdRestrictionCondition();
		condition.setDetailId(detail.pid());
		condition.setVehicle(4);
		conditions.add(condition);
		return conditions;
	}
}
