package com.navinfo.dataservice.engine.edit.operation.obj.rdvoiceguide.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguide;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideDetail;
import com.navinfo.dataservice.dao.glm.model.rd.voiceguide.RdVoiceguideVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.voiceguide.RdVoiceguideSelector;

public class Operation implements IOperation {

	private Command command = null;

	private Connection conn;

	public Operation(Command command) {

		this.command = command;
	}
	
	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		String msg = null;

		msg = update(result);

		return null;

	}

	/**
	 * 语音引导只处理详细信息表的修改和删除操作
	 * 
	 * @param result
	 * @return
	 */
	private String update(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 详细信息无变化 不处理
		if (!content.containsKey("details")) {

			return null;
		}

		result.setPrimaryPid(command.getVoiceguide().pid());

		JSONArray details = content.getJSONArray("details");

		int deleteCount = 0;

		for (int i = 0; i < details.size(); i++) {

			JSONObject json = details.getJSONObject(i);

			if (json.containsKey("objStatus")
					&& ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {

				deleteCount++;
			}
		}

		if (deleteCount == command.getVoiceguide().getDetails().size()) {

			result.insertObject(command.getVoiceguide(), ObjStatus.DELETE,
					command.getVoiceguide().pid());

			return null;
		}

		for (int i = 0; i < details.size(); i++) {

			JSONObject json = details.getJSONObject(i);

			// detail未标记变化 不处理
			if (!json.containsKey("objStatus")) {
				continue;
			}

			RdVoiceguideDetail detail = command.getVoiceguide().detailMap
					.get(json.getString("rowId"));

			if (detail == null) {
				throw new Exception("rowId=" + json.getString("rowId")
						+ "的RdVoiceguideDetail不存在");
			}

			if (ObjStatus.DELETE.toString().equals(json.getString("objStatus"))) {

				result.insertObject(detail, ObjStatus.DELETE, command
						.getVoiceguide().pid());

				continue;
			}
			if (ObjStatus.UPDATE.toString().equals(json.getString("objStatus"))) {

				boolean isChanged = detail.fillChangeFields(json);

				if (isChanged) {
					result.insertObject(detail, ObjStatus.UPDATE, command
							.getVoiceguide().pid());
				}
			}
		}

		return null;
	}

	public void breakRdLink(RdLink deleteLink, List<RdLink> newLinks,
			Result result) throws Exception {
		if (this.command != null || conn == null) {

			return;
		}

		RdVoiceguideSelector selector = new RdVoiceguideSelector(conn);

		// link为进入线
		List<RdVoiceguide> voiceguides = selector.loadRdVoiceguideByLinkPid(
				deleteLink.getPid(), 1, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			breakInLink(voiceguide, newLinks, result);
		}

		// link为退出线
		voiceguides = selector.loadRdVoiceguideByLinkPid(deleteLink.getPid(),
				2, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			breakOutLink(deleteLink, voiceguide, newLinks, result);
		}

		// link为经过线
		voiceguides = selector.loadRdVoiceguideByLinkPid(deleteLink.getPid(),
				3, true);

		for (RdVoiceguide voiceguide : voiceguides) {

			breakPassLink(deleteLink, voiceguide, newLinks, result);
		}

	}

	private void breakInLink(RdVoiceguide voiceguide, List<RdLink> newLinks,
			Result result) {

		for (RdLink link : newLinks) {

			if (link.getsNodePid() == voiceguide.getNodePid()
					|| link.geteNodePid() == voiceguide.getNodePid()) {

				voiceguide.changedFields().put("inLinkPid", link);

				result.insertObject(voiceguide, ObjStatus.DELETE,
						voiceguide.pid());

				break;
			}

		}
	}

	private void breakOutLink(RdLink deleteLink, RdVoiceguide voiceguide,
			List<RdLink> newLinks, Result result) throws Exception {

		for (IRow rowDetail : voiceguide.getDetails()) {

			RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;

			// 排除其他详细信息
			if (detail.getOutLinkPid() != deleteLink.getPid()) {

				continue;
			}

			int connectionNodePid = 0;

			if (detail.getVias().size() == 0) {

				connectionNodePid = voiceguide.getNodePid();

			} else {

				RdVoiceguideVia lastVia = (RdVoiceguideVia) detail.getVias()
						.get(0);

				for (IRow rowVia : detail.getVias()) {

					RdVoiceguideVia via = (RdVoiceguideVia) rowVia;

					if (lastVia.getGroupId() == via.getGroupId()
							&& lastVia.getSeqNum() < via.getSeqNum()) {

						lastVia = via;
					}
				}

				RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

				List<Integer> linkPids = rdLinkSelector.loadLinkPidByNodePid(
						deleteLink.getsNodePid(), false);

				connectionNodePid = linkPids.contains(lastVia.getLinkPid()) ? deleteLink
						.getsNodePid() : deleteLink.geteNodePid();
			}

			if (connectionNodePid == 0) {

				continue;
			}

			for (RdLink link : newLinks) {

				if (link.getsNodePid() == connectionNodePid
						|| link.geteNodePid() == connectionNodePid) {

					detail.changedFields().put("outLinkPid", link);

					result.insertObject(detail, ObjStatus.UPDATE,
							voiceguide.pid());

					break;
				}
			}

		}
	}

	private void breakPassLink(RdLink deleteLink, RdVoiceguide voiceguide,
			List<RdLink> newLinks, Result result) throws Exception {

		for (IRow rowDetail : voiceguide.getDetails()) {

			RdVoiceguideDetail detail = (RdVoiceguideDetail) rowDetail;

			// 对经过线分组
			Map<Integer, List<RdVoiceguideVia>> viaGroupId = new HashMap<Integer, List<RdVoiceguideVia>>();

			for (IRow row : detail.getVias()) {

				RdVoiceguideVia via = (RdVoiceguideVia) row;

				if (viaGroupId.get(via.getGroupId()) == null) {
					viaGroupId.put(via.getGroupId(),
							new ArrayList<RdVoiceguideVia>());
				}

				viaGroupId.get(via.getGroupId()).add(via);
			}

			// 分组处理经过线
			for (int key : viaGroupId.keySet()) {

				// 经过线组
				List<RdVoiceguideVia> viaGroup = viaGroupId.get(key);

				RdVoiceguideVia breakVia = null;

				for (RdVoiceguideVia via : viaGroup) {

					if (via.getLinkPid() == deleteLink.getPid()) {

						breakVia = via;

						break;
					}
				}

				// 经过线组的link未被打断，不处理
				if (breakVia == null) {

					continue;
				}

				// 与进入线或前一个经过线的连接点
				int connectionNodePid = 0;

				// 打断的是第一个经过线link
				if (breakVia.getSeqNum() == 1) {

					connectionNodePid = voiceguide.getNodePid();

				} else {

					int preLinkPid = 0;

					for (RdVoiceguideVia via : viaGroup) {

						if (via.getSeqNum() == breakVia.getSeqNum() - 1) {

							preLinkPid = via.getLinkPid();

							break;
						}
					}

					RdLinkSelector rdLinkSelector = new RdLinkSelector(
							this.conn);

					List<Integer> linkPids = rdLinkSelector
							.loadLinkPidByNodePid(deleteLink.getsNodePid(),
									false);

					connectionNodePid = linkPids.contains(preLinkPid) ? deleteLink
							.getsNodePid() : deleteLink.geteNodePid();
				}

				if (newLinks.get(0).getsNodePid() == connectionNodePid
						|| newLinks.get(0).geteNodePid() == connectionNodePid) {

					for (int i = 0; i < newLinks.size(); i++) {

						RdVoiceguideVia newVia = new RdVoiceguideVia();

						newVia.setDetailId(breakVia.getDetailId());

						newVia.setGroupId(breakVia.getGroupId());

						newVia.setLinkPid(newLinks.get(i).getPid());

						newVia.setSeqNum(breakVia.getSeqNum() + i);

						result.insertObject(newVia, ObjStatus.INSERT,
								newVia.getDetailId());
					}

				} else {

					for (int i = newLinks.size(); i > 0; i--) {

						RdVoiceguideVia newVia = new RdVoiceguideVia();

						newVia.setDetailId(breakVia.getDetailId());

						newVia.setGroupId(breakVia.getGroupId());

						newVia.setLinkPid(newLinks.get(i - 1).getPid());

						newVia.setSeqNum(breakVia.getSeqNum() + newLinks.size()
								- i);

						result.insertObject(newVia, ObjStatus.INSERT,
								newVia.getDetailId());
					}
				}

				result.insertObject(breakVia, ObjStatus.DELETE,
						breakVia.getDetailId());

				// 维护后续经过线序号
				for (RdVoiceguideVia via : viaGroup) {

					if (via.getSeqNum() > breakVia.getSeqNum()) {

						via.changedFields().put("seqNum",
								via.getSeqNum() + newLinks.size() - 1);

						result.insertObject(via, ObjStatus.UPDATE,
								via.getDetailId());
					}
				}
			}
		}
	}

}
