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
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
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

		boolean isChanged = variableSpeed.fillChangeFields(content);

		if (isChanged) {
			result.insertObject(variableSpeed, ObjStatus.UPDATE, variableSpeed.pid());
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
				result.insertObject(via, ObjStatus.DELETE, via.getVspeedPid());
			} else if (!subObj.contains(via.getLinkPid())) {
				result.insertObject(via, ObjStatus.DELETE, via.getVspeedPid());
			} else {
				subObj.remove((Integer) via.getLinkPid());
			}
		}
		for (int i = 0; i < subObj.size(); i++) {

			RdVariableSpeedVia via = new RdVariableSpeedVia();

			via.setLinkPid(subObj.getInt(i));

			via.setVspeedPid(variableSpeed.getPid());

			result.insertObject(via, ObjStatus.INSERT, via.getVspeedPid());
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

			boolean hasFindStartLink = false;

			for (IRow row : viaList) {
				RdVariableSpeedVia via = (RdVariableSpeedVia) row;

				if (via.getLinkPid() == oldLink.getPid()) {

					// 删除原始线作为经过线的情况
					result.insertObject(via, ObjStatus.DELETE, via.getVspeedPid());

					int oldSNodePid = oldLink.getsNodePid();

					for (RdLink newLink : newLinks) {
						if (newLink.getsNodePid() == oldSNodePid || newLink.geteNodePid() == oldSNodePid) {
							RdVariableSpeedVia rdVariableSpeedVia = new RdVariableSpeedVia();

							rdVariableSpeedVia.setLinkPid(newLink.getPid());

							rdVariableSpeedVia.setSeqNum(via.getSeqNum());

							rdVariableSpeedVia.setVspeedPid(rdVariableSpeed.getPid());

							result.insertObject(rdVariableSpeedVia, ObjStatus.INSERT, via.getVspeedPid());

							hasFindStartLink = true;
						} else {
							RdVariableSpeedVia rdVariableSpeedVia2 = new RdVariableSpeedVia();

							rdVariableSpeedVia2.setLinkPid(newLink.getPid());

							rdVariableSpeedVia2.setSeqNum(via.getSeqNum() + 1);

							rdVariableSpeedVia2.setVspeedPid(rdVariableSpeed.getPid());

							result.insertObject(rdVariableSpeedVia2, ObjStatus.INSERT, via.getVspeedPid());
						}
					}
				} else if (hasFindStartLink) {
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

						result.insertObject(rdVariableSpeedVia, ObjStatus.INSERT, rdVariableSpeedVia.getVspeedPid());

						// 更新其他接续link的序号
						List<IRow> rdViaList = rdVariableSpeed.getVias();

						for (int i = 0; i < rdViaList.size(); i++) {
							RdVariableSpeedVia rdVia = (RdVariableSpeedVia) rdViaList.get(i);

							rdVia.changedFields().put("seqNum", i + 2);

							result.insertObject(rdVia, ObjStatus.UPDATE, rdVia.getVspeedPid());
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

	
	/**
	 * 分离节点，暂不考虑库跨图幅的情况
	 * 
	 * @param link
	 * @param nodePid
	 * @param rdlinks
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
			Result result) throws Exception {

		int linkPid = link.getPid();

		RdVariableSpeedSelector selector = new RdVariableSpeedSelector(this.conn);

		// link为退出线或进入线的RdVariableSpeed
		List<RdVariableSpeed> speeds = selector.loadRdVariableSpeedByLinkPid(linkPid, true);

		for (RdVariableSpeed speed : speeds) {

			if (speed.getNodePid() == nodePid) {

				result.insertObject(speed, ObjStatus.DELETE, speed.getPid());

			} else if(speed.getOutLinkPid()==linkPid){
				// 删除link为退出线的可变限速接续link
				for (IRow row : speed.getVias()) {

					result.insertObject(row, ObjStatus.DELETE, speed.getPid());
				}
			}
		}
		// link为接续link的RdVariableSpeed
		speeds = selector.loadRdVariableSpeedByViaLinkPid(linkPid, true);

		if (speeds.size() == 0) {
			return;
		}

		RdLinkSelector RdLinkSelector = new RdLinkSelector(this.conn);

		for (RdVariableSpeed speed : speeds) {

			int currSeqNum = 1;

			for (IRow Row : speed.getVias()) {

				RdVariableSpeedVia via = (RdVariableSpeedVia) Row;

				if (via.getLinkPid() == linkPid) {

					currSeqNum = via.getSeqNum();

					break;
				}
			}

			RdLink preLink = null;

			if (currSeqNum == 1) {

				preLink = (RdLink) RdLinkSelector.loadById(speed.getOutLinkPid(),
						true, true);
			} else {

				for (IRow Row : speed.getVias()) {

					RdVariableSpeedVia via = (RdVariableSpeedVia) Row;

					if (via.getSeqNum() == currSeqNum - 1) {

						preLink = (RdLink) RdLinkSelector.loadById(
								via.getLinkPid(), true, true);

						break;
					}
				}
			}

			int flagSeqNum = currSeqNum;

			if (preLink.getsNodePid() == nodePid
					|| preLink.geteNodePid() == nodePid) {

				flagSeqNum = currSeqNum - 1;
			}

			for (IRow row : speed.getVias()) {

				RdVariableSpeedVia via = (RdVariableSpeedVia) row;

				if (via.getSeqNum() > flagSeqNum) {

					result.insertObject(row, ObjStatus.DELETE, speed.getPid());
				}
			}
		}
	}

}
