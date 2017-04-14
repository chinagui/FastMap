package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.create;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;

/***
 * 语音引导创建
 * 
 * @author zhaokk
 * 
 */
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

	/***
	 * 创建语音引导
	 * 
	 * @param result
	 * @return
	 * @throws Exception
	 */
	private String create(Result result) throws Exception {

		RdVoiceguide voiceguide = new RdVoiceguide();
		// 申请pid
		voiceguide.setPid(PidUtil.getInstance().applyRdVoiceguidePid());
		// 进入线
		voiceguide.setInLinkPid(this.command.getInLinkPid());
		// 进入点
		voiceguide.setNodePid(this.command.getNodePid());
		// 语音引导详细信息表
		List<IRow> details = new ArrayList<IRow>();

		// 创建语音引导
		for (int i = 0; i < this.command.getArray().size(); i++) {
			JSONObject obj = this.command.getArray().getJSONObject(i);
			RdVoiceguideDetail detail = new RdVoiceguideDetail();
			detail.setPid(PidUtil.getInstance().applyRdVoiceguideDetailPid());
			detail.setOutLinkPid(obj.getInt("outLinkPid"));
			detail.setVoiceguidePid(voiceguide.getPid());
			// 1 路口关系 2 线线关系
			int relationshipType = obj.getInt("relationshipType");
			detail.setRelationshipType(relationshipType);
			// 线线关系记录经过线 路口关系不记录经过线
			if (relationshipType == 2) {
				this.createVias(obj, detail);
			}

			details.add(detail);
		}
		// 语音引导详细信息表
		voiceguide.setDetails(details);
		// 新增语音引导
		result.insertObject(voiceguide, ObjStatus.INSERT, voiceguide.pid());

		return null;
	}

	/***
	 * 
	 * @param obj
	 *            参数退出线详细信息
	 * @param detail
	 *            语音引导详细信息
	 */
	private void createVias(JSONObject obj, RdVoiceguideDetail detail) {
		JSONArray vias = obj.getJSONArray("vias");
		if (vias != null) {
			int seqNum = 1;
			List<IRow> viaList = new ArrayList<IRow>();
			for (int j = 0; j < vias.size(); j++) {
				int viaLinkPid = vias.getInt(j);
				RdVoiceguideVia via = new RdVoiceguideVia();
				via.setDetailId(detail.getPid());
				via.setLinkPid(viaLinkPid);
				via.setSeqNum(seqNum);
				viaList.add(via);
				seqNum++;
			}
			detail.setVias(viaList);
		}

	}
}
