package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.delete;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.AlertObject;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;
import com.navinfo.dataservice.dao.glm.selector.rd.voiceguide.RdVoiceguideSelector;

public class Operation implements IOperation {

	private Command command = null;

	private Connection conn = null;

	public Operation(Command command) {

		this.command = command;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = delete(result, command.getVoiceguide());

		return msg;
	}

	private String delete(Result result, RdVoiceguide voiceguide) {

		result.insertObject(voiceguide, ObjStatus.DELETE, voiceguide.pid());

		return null;
	}

	public void deleteByLink(int linkPid, Result result) throws Exception {

		if (this.command != null || conn == null) {

			return;
		}

		RdVoiceguideSelector selector = new RdVoiceguideSelector(conn);

		// link为进入线
		List<RdVoiceguide> voiceguides = selector.loadRdVoiceguideByLinkPid(linkPid, 1, true);

		for (RdVoiceguide voiceguide : voiceguides) {
			delete(result, voiceguide);
		}

		// link为退出线
		voiceguides = selector.loadRdVoiceguideByLinkPid(linkPid, 2, true);

		deleteByOutLink(linkPid, voiceguides, result);

		// link为经过线
		voiceguides = selector.loadRdVoiceguideByLinkPid(linkPid, 3, true);

		deleteByPassLink(linkPid, voiceguides, result);
	}

	/**
	 * 根据路口pid删除路口关系的语音引导
	 * 
	 * @param crossPid
	 * @param result
	 * @throws Exception
	 */
	public void deleteByCross(int crossPid, Result result) throws Exception {
		RdVoiceguideSelector selector = new RdVoiceguideSelector(conn);

		List<RdVoiceguide> voiceguides = selector.getVoiceGuideByCrossPid(crossPid, true);

		for (RdVoiceguide voiceguide : voiceguides) {
			delete(result, voiceguide);
		}
	}

	private void deleteByOutLink(int linkPid, List<RdVoiceguide> voiceguides, Result result) {

		for (RdVoiceguide voiceguide : voiceguides) {

			List<RdVoiceguideDetail> deleteDetails = new ArrayList<RdVoiceguideDetail>();

			// 获取link对应的详细信息
			for (IRow row : voiceguide.getDetails()) {

				RdVoiceguideDetail detail = (RdVoiceguideDetail) row;

				if (detail.getOutLinkPid() == linkPid) {

					deleteDetails.add(detail);
				}
			}

			// 语音引导的详细信息均被删除时需要同时删除该语音引导
			if (deleteDetails.size() == voiceguide.getDetails().size()) {

				delete(result, voiceguide);
			} else {
				// 删除详细信息
				for (RdVoiceguideDetail detail : deleteDetails) {
					result.insertObject(detail, ObjStatus.DELETE, voiceguide.pid());
				}
			}

		}
	}

	private void deleteByPassLink(int linkPid, List<RdVoiceguide> voiceguides, Result result) {

		for (RdVoiceguide voiceguide : voiceguides) {

			List<RdVoiceguideDetail> deleteDetails = new ArrayList<RdVoiceguideDetail>();

			List<RdVoiceguideVia> deleteVias = new ArrayList<RdVoiceguideVia>();

			for (IRow rowDetail : voiceguide.getDetails()) {

				RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;

				// 需要删除的经过线组
				Set<Integer> deleteGroupId = new HashSet<Integer>();

				// 所有经过线组
				Set<Integer> sumGroupId = new HashSet<Integer>();

				for (IRow rowVia : detail.getVias()) {
					RdVoiceguideVia via = (RdVoiceguideVia) rowVia;

					if (via.getLinkPid() == linkPid) {
						deleteGroupId.add(via.getGroupId());
					}

					sumGroupId.add(via.getGroupId());
				}

				// 需要删除的经过线组数与总经过线组一致，删除该详细信息
				if (sumGroupId.size() == deleteGroupId.size()) {

					deleteDetails.add(detail);

				} else {
					// 按组删除经过线
					for (IRow rowVia : detail.getVias()) {

						RdVoiceguideVia via = (RdVoiceguideVia) rowVia;

						if (deleteGroupId.contains(via.getGroupId())) {
							deleteVias.add(via);
						}
					}
				}
			}

			// 语音引导的详细信息均被删除时需要同时删除该语音引导
			if (deleteDetails.size() == voiceguide.getDetails().size()) {

				delete(result, voiceguide);
			} else {

				// 删除经过线
				for (RdVoiceguideVia via : deleteVias) {
					result.insertObject(via, ObjStatus.DELETE, voiceguide.pid());
				}
				// 删除详细信息
				for (RdVoiceguideDetail detail : deleteDetails) {
					result.insertObject(detail, ObjStatus.DELETE, voiceguide.pid());
				}
			}

		}
	}

	/**
	 * 删除link对语音引导的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteRdVoiceguideInfectData(int linkPid, Connection conn) throws Exception {

		RdVoiceguideSelector voiceguideSelector = new RdVoiceguideSelector(conn);

		List<RdVoiceguide> voiceGuideList = voiceguideSelector.loadRdVoiceguideByLinkPid(linkPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdVoiceguide voiceguide : voiceGuideList) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(voiceguide.objType());

			alertObj.setPid(voiceguide.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}

	/**
	 * 删除路口对语音引导的删除影响
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<AlertObject> getDeleteCrossVoiceGuideInfectData(int crossPid) throws Exception {

		RdVoiceguideSelector selector = new RdVoiceguideSelector(conn);

		List<RdVoiceguide> voiceguides = selector.getVoiceGuideByCrossPid(crossPid, true);

		List<AlertObject> alertList = new ArrayList<>();

		for (RdVoiceguide voiceguide : voiceguides) {

			AlertObject alertObj = new AlertObject();

			alertObj.setObjType(voiceguide.objType());

			alertObj.setPid(voiceguide.getPid());

			alertObj.setStatus(ObjStatus.DELETE);

			if(!alertList.contains(alertObj))
			{
				alertList.add(alertObj);
			}
		}

		return alertList;
	}
}
