package com.navinfo.dataservice.engine.edit.operation.obj.rdvariablespeed.update;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeed;
import com.navinfo.dataservice.dao.glm.model.rd.variablespeed.RdVariableSpeedVia;
import com.navinfo.dataservice.dao.glm.selector.rd.variablespeed.RdVariableSpeedSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RdVariableSpeed variableSpeed;

	private Connection conn;
	
	public Operation(Connection conn) {
		this.conn = conn;
	}

	public Operation(Command command, RdVariableSpeed variableSpeed, Connection conn) {
		this.command = command;

		this.variableSpeed = variableSpeed;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			boolean isChanged = variableSpeed.fillChangeFields(content);

			if (isChanged) {
				result.insertObject(variableSpeed, ObjStatus.UPDATE, variableSpeed.pid());
			}
		}

		// 接续线子表
		if (content.containsKey("vias")) {
			updateVias(result, content);
		}

		return null;

	}

	/**
	 * @param result
	 * @param content
	 */
	private void updateVias(Result result, JSONObject content) {
		JSONArray subObj = content.getJSONArray("vias");

		for (IRow row : variableSpeed.getVias()) {
			RdVariableSpeedVia via = (RdVariableSpeedVia) row;
			if (subObj == null) {
				result.insertObject(via, ObjStatus.DELETE, via.getLinkPid());
			} else if (!subObj.contains(via.getLinkPid())) {
				result.insertObject(via, ObjStatus.DELETE, via.getLinkPid());
			} else {
				subObj.remove((Integer) via.getLinkPid());
			}
		}
		for (int i = 0; i < subObj.size(); i++) {

			RdVariableSpeedVia via = new RdVariableSpeedVia();

			via.setLinkPid(subObj.getInt(i));

			via.setVspeedPid(variableSpeed.getPid());

			result.insertObject(via, ObjStatus.INSERT, via.getLinkPid());
		}
	}

	/**
	 * 线的打断维护可变限速关系，分下面几种情况 1.打断进入线,选取和进入点连接的线作为新的进入线
	 * 2.打断退出线，选取和进入点连接的线作为退出线，另一条线作为接续线（无论原来有没有接续线） 3.打断接续线，新的线都均作为接续线
	 * 
	 * @param oldLink
	 *            旧的线
	 * @param newLinks
	 *            新生成的线
	 * @param result
	 *            结果集
	 * @throws Exception
	 */
	public void breakLine(RdLink oldLink, List<RdLink> newLinks, Result result) throws Exception {

		int oldLinkPid = oldLink.getPid();

		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(conn);

		// 第一种打断场景：打断进入线
		List<RdVariableSpeed> rdVariableSpeeds = selector.loadRdVariableSpeedByParam(oldLinkPid, null, null, true);

		hanldBreakInLink(rdVariableSpeeds, newLinks, result);

		// 第二种打断场景：打断退出线
		List<RdVariableSpeed> rdVariableSpeedList = selector.loadRdVariableSpeedByParam(null, null, oldLinkPid, true);

		hanldBreakOutLink(rdVariableSpeedList, newLinks, result);

		// 第三种打断场景：打断接续线
		List<RdVariableSpeed> rdVariableViaSpeedList = selector.loadRdVariableSpeedByViaLinkPid(oldLinkPid, true);

		hanldBreakViaLink(oldLink, rdVariableViaSpeedList, newLinks, result);
	}

	/**
	 * @param oldLink
	 * @param rdVariableSpeedList
	 * @param newLinks
	 * @param result
	 */
	private void hanldBreakViaLink(RdLink oldLink, List<RdVariableSpeed> rdVariableViaSpeedList, List<RdLink> newLinks,
			Result result) {
		for (RdVariableSpeed rdVariableSpeed : rdVariableViaSpeedList) {
			List<IRow> viaList = rdVariableSpeed.getVias();

			for (IRow row : viaList) {
				RdVariableSpeedVia via = (RdVariableSpeedVia) row;

				if (via.getLinkPid() == oldLink.getPid()) {

					//删除原始线作为经过线的情况
					result.insertObject(via, ObjStatus.DELETE, via.getLinkPid());
					
					int oldSNodePid = oldLink.getsNodePid();
					
					for (RdLink newLink : newLinks) {
						if (newLink.getsNodePid() == oldSNodePid || newLink.geteNodePid() == oldSNodePid) {
							RdVariableSpeedVia rdVariableSpeedVia = new RdVariableSpeedVia();

							rdVariableSpeedVia.setLinkPid(newLink.getPid());

							rdVariableSpeedVia.setSeqNum(via.getSeqNum());

							rdVariableSpeedVia.setVspeedPid(rdVariableSpeed.getPid());

							result.insertObject(rdVariableSpeedVia, ObjStatus.INSERT, via.getLinkPid());
						} else {
							RdVariableSpeedVia rdVariableSpeedVia2 = new RdVariableSpeedVia();

							rdVariableSpeedVia2.setLinkPid(newLink.getPid());

							rdVariableSpeedVia2.setSeqNum(via.getSeqNum() + 1);

							rdVariableSpeedVia2.setVspeedPid(rdVariableSpeed.getPid());

							result.insertObject(rdVariableSpeedVia2, ObjStatus.INSERT, via.getLinkPid());
						}
					}
				} else {
					// 更新其他接续线的seqNum
					via.changedFields().put("seqNum", via.getSeqNum() + 1);

					result.insertObject(via, ObjStatus.UPDATE, via.getLinkPid());
				}
			}
		}
	}

	/**
	 * @param rdVariableSpeeds
	 * @param newLinks
	 * @param result
	 */
	private void hanldBreakOutLink(List<RdVariableSpeed> rdVariableSpeeds, List<RdLink> newLinks, Result result) {
		for (RdVariableSpeed rdVariableSpeed : rdVariableSpeeds) {
			int nodePid = rdVariableSpeed.getNodePid();

			if (newLinks.size() == 2) {
				for (RdLink link : newLinks) {

					if (link.getsNodePid() == nodePid || link.geteNodePid() == nodePid) {
						rdVariableSpeed.changedFields().put("outLinkPid", link.getPid());
						result.insertObject(rdVariableSpeed, ObjStatus.UPDATE, rdVariableSpeed.getPid());
					} else {
						// 新线未和node联通的作为接续线
						RdVariableSpeedVia rdVariableSpeedVia = new RdVariableSpeedVia();

						rdVariableSpeedVia.setLinkPid(link.getPid());

						rdVariableSpeedVia.setSeqNum(1);

						rdVariableSpeedVia.setVspeedPid(rdVariableSpeed.getPid());

						result.insertObject(rdVariableSpeed, ObjStatus.INSERT, rdVariableSpeed.getPid());

						// 更新其他接续link的序号
						List<IRow> rdViaList = rdVariableSpeed.getVias();

						for (int i = 0; i < rdViaList.size(); i++) {
							RdVariableSpeedVia rdVia = (RdVariableSpeedVia) rdViaList.get(i);

							rdVia.changedFields().put("seqNum", i + 2);

							result.insertObject(rdVia, ObjStatus.UPDATE, rdVia.getLinkPid());
						}
					}
				}
			}
		}
	}

	/**
	 * @param rdVariableSpeeds
	 * @param newLinks
	 */
	private void hanldBreakInLink(List<RdVariableSpeed> rdVariableSpeeds, List<RdLink> newLinks, Result result) {
		for (RdVariableSpeed rdVariableSpeed : rdVariableSpeeds) {
			int nodePid = rdVariableSpeed.getNodePid();

			for (RdLink link : newLinks) {
				if (newLinks.size() == 2) {
					if (link.getsNodePid() == nodePid || link.geteNodePid() == nodePid) {
						rdVariableSpeed.changedFields().put("inLinkPid", link.getPid());
						result.insertObject(rdVariableSpeed, ObjStatus.UPDATE, rdVariableSpeed.getPid());
						break;
					}
				}
			}
		}
	}

}
