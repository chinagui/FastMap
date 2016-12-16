package com.navinfo.dataservice.engine.edit.operation.obj.rddirectroute.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectroute;
import com.navinfo.dataservice.dao.glm.model.rd.directroute.RdDirectrouteVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.directroute.RdDirectrouteSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

public class Operation implements IOperation {

	private Command command;

	private Connection conn = null;

	public Operation(Command command) {
		this.command = command;

	}

	public Operation(Connection conn) {
		this.conn = conn;

	}

	@Override
	public String run(Result result) throws Exception {

		return this.update(result);

	}

	private String update(Result result) throws Exception {

		RdDirectroute directroute = command.getDirectroute();

		JSONObject content = command.getContent();

		if (!content.containsKey("objStatus")) {

			return null;
		}

		result.setPrimaryPid(directroute.pid());

		if (ObjStatus.DELETE.toString().equals(content.getString("objStatus"))) {

			result.insertObject(directroute, ObjStatus.DELETE,
					directroute.pid());

			return null;

		} else if (ObjStatus.UPDATE.toString().equals(
				content.getString("objStatus"))) {

			boolean isChanged = directroute.fillChangeFields(content);

			if (isChanged) {

				result.insertObject(directroute, ObjStatus.UPDATE,
						directroute.pid());
			}
		}

		return null;
	}

	/**
	 * 打断link维护警示信息
	 * 
	 * @param oldLinkPid
	 *            被打断的link
	 * @param newLinks
	 *            新生成的link组
	 * @param result
	 * @param conn
	 * @throws Exception
	 */
	public void breakRdLink(RdLink oldLink, List<RdLink> newLinks, Result result)
			throws Exception {

		if (conn == null) {
			return;
		}

		RdDirectrouteSelector selector = new RdDirectrouteSelector(conn);

		List<RdDirectroute> directroutes = selector.loadByInOutLink(
				oldLink.getPid(), true);

		for (RdDirectroute directroute : directroutes) {

			if (directroute.getInLinkPid() == oldLink.getPid()) {

				breakInLink(directroute, newLinks, result);
			}

			if (directroute.getOutLinkPid() == oldLink.getPid()) {

				breakOutLink(directroute, oldLink, newLinks, result);
			}
		}

		directroutes = selector.loadByPassLink(oldLink.getPid(), true);

		for (RdDirectroute directroute : directroutes) {

			breakPassLink(directroute, oldLink, newLinks, result);
		}

	}

	/**
	 * 处理link为进入线的顺行
	 * 
	 * @param directroute
	 * @param newLinks
	 * @param result
	 */
	private void breakInLink(RdDirectroute directroute, List<RdLink> newLinks,
			Result result) {

		for (RdLink link : newLinks) {

			if (directroute.getNodePid() == link.getsNodePid()
					|| directroute.getNodePid() == link.geteNodePid()) {

				directroute.changedFields().put("inLinkPid", link.getPid());

				result.insertObject(directroute, ObjStatus.UPDATE,
						directroute.pid());
			}
		}
	}

	/**
	 * 处理link为退出线的顺行
	 * 
	 * @param directroute
	 * @param oldLink
	 * @param newLinks
	 * @param result
	 * @throws Exception
	 */
	private void breakOutLink(RdDirectroute directroute, RdLink oldLink,
			List<RdLink> newLinks, Result result) throws Exception {

		int connectionNodePid = 0;

		// 无经过线进入点为连接点；有经过线最后一个经过线与退出线的连接点为连接点
		if (directroute.getVias().size() == 0) {

			connectionNodePid = directroute.getNodePid();

		} else {

			// 任意经过线组的最后一个经过线
			RdDirectrouteVia lastVia = (RdDirectrouteVia) directroute.getVias()
					.get(0);

			for (IRow rowVia : directroute.getVias()) {

				RdDirectrouteVia via = (RdDirectrouteVia) rowVia;

				if (lastVia.getGroupId() == via.getGroupId()
						&& lastVia.getSeqNum() < via.getSeqNum()) {

					lastVia = via;
				}
			}

			RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

			List<Integer> linkPids = rdLinkSelector.loadLinkPidByNodePid(
					oldLink.getsNodePid(), false);

			if (linkPids.contains(lastVia.getLinkPid())) {

				connectionNodePid = oldLink.getsNodePid();

			} else {

				connectionNodePid = oldLink.geteNodePid();
			}
		}

		if (connectionNodePid == 0) {
			return;
		}

		for (RdLink link : newLinks) {
			if (connectionNodePid == link.getsNodePid()
					|| connectionNodePid == link.geteNodePid()) {

				directroute.changedFields().put("outLinkPid", link.getPid());

				result.insertObject(directroute, ObjStatus.UPDATE,
						directroute.pid());
				break;
			}
		}

	}

	/**
	 * 处理link为经过线的顺行
	 * 
	 * @param directroute
	 * @param oldLink
	 * @param newLinks
	 * @param result
	 * @throws Exception
	 */
	private void breakPassLink(RdDirectroute directroute, RdLink oldLink,
			List<RdLink> newLinks, Result result) throws Exception {

		if (directroute.getVias().size() == 0) {
			return;
		}

		// 对经过线分组
		Map<Integer, List<RdDirectrouteVia>> viaGroupId = new HashMap<Integer, List<RdDirectrouteVia>>();

		for (IRow row : directroute.getVias()) {
			RdDirectrouteVia via = (RdDirectrouteVia) row;

			if (viaGroupId.get(via.getGroupId()) == null) {
				viaGroupId.put(via.getGroupId(),
						new ArrayList<RdDirectrouteVia>());
			}

			viaGroupId.get(via.getGroupId()).add(via);
		}

		// 分组处理经过线
		for (int key : viaGroupId.keySet()) {

			// 经过线组
			List<RdDirectrouteVia> viaGroup = viaGroupId.get(key);

			RdDirectrouteVia oldVia = null;

			for (RdDirectrouteVia via : viaGroup) {

				if (via.getLinkPid() == oldLink.getPid()) {
					oldVia = via;

					break;
				}
			}

			// 经过线组的link未被打断，不处理
			if (oldVia == null) {
				continue;
			}

			// 与进入线或前一个经过线的连接点
			int connectionNodePid = 0;

			// 打断的是第一个经过线link
			if (oldVia.getSeqNum() == 1) {

				connectionNodePid = directroute.getNodePid();

			} else {

				int preLinkPid = 0;

				for (RdDirectrouteVia via : viaGroup) {

					if (via.getSeqNum() == oldVia.getSeqNum() - 1) {

						preLinkPid = via.getLinkPid();

						break;
					}
				}

				RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

				List<Integer> linkPids = rdLinkSelector.loadLinkPidByNodePid(
						oldLink.getsNodePid(), false);

				connectionNodePid = linkPids.contains(preLinkPid) ? oldLink
						.getsNodePid() : oldLink.geteNodePid();
			}

			if (newLinks.get(0).getsNodePid() == connectionNodePid
					|| newLinks.get(0).geteNodePid() == connectionNodePid) {

				for (int i = 0; i < newLinks.size(); i++) {

					RdDirectrouteVia newVia = new RdDirectrouteVia();

					newVia.setPid(oldVia.getPid());

					newVia.setGroupId(oldVia.getGroupId());

					newVia.setLinkPid(newLinks.get(i).getPid());

					newVia.setSeqNum(oldVia.getSeqNum() + i);

					result.insertObject(newVia, ObjStatus.INSERT,
							newVia.getPid());
				}

			} else {

				for (int i = newLinks.size(); i > 0; i--) {
					RdDirectrouteVia newVia = new RdDirectrouteVia();

					newVia.setPid(oldVia.getPid());

					newVia.setGroupId(oldVia.getGroupId());

					newVia.setLinkPid(newLinks.get(i - 1).getPid());

					newVia.setSeqNum(oldVia.getSeqNum() + newLinks.size() - i);

					result.insertObject(newVia, ObjStatus.INSERT,
							newVia.getPid());
				}
			}

			result.insertObject(oldVia, ObjStatus.DELETE, oldVia.getPid());

			// 处理后续经过线序号
			for (RdDirectrouteVia via : viaGroup) {

				if (via.getSeqNum() > oldVia.getSeqNum()) {

					via.changedFields().put("seqNum",
							via.getSeqNum() + newLinks.size() - 1);

					result.insertObject(via, ObjStatus.UPDATE, via.getPid());
				}
			}
		}
	}
	
	/**
	 * 分离节点
	 * 
	 * @param link
	 * @param nodePid
	 * @param rdlinks
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
			Result result) throws Exception {

		List<Integer> nodePids = new ArrayList<Integer>();

		nodePids.add(nodePid);

		departNode(link, nodePids, rdlinks, result);
	}
	
	/**
	 * 分离节点
	 * 
	 * @param link
	 * @param nodePid
	 * @param rdlinks
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, List<Integer> nodePids,
			List<RdLink> rdlinks, Result result) throws Exception {

		int linkPid = link.getPid();

		// 跨图幅处理的link为进入线的RdDirectroute
		Map<Integer, RdDirectroute> directrouteInLink = null;

		// 跨图幅处理的link为退出线的RdDirectroute
		Map<Integer, RdDirectroute> directrouteOutLink = null;

		if (rdlinks != null && rdlinks.size() > 1) {

			directrouteInLink = new HashMap<Integer, RdDirectroute>();

			directrouteOutLink = new HashMap<Integer, RdDirectroute>();
		}

		RdDirectrouteSelector selector = new RdDirectrouteSelector(this.conn);

		for (int nodePid : nodePids) {

			// 在link上的RdDirectroute
			List<RdDirectroute> directroutes = selector.loadByInOutLink(
					linkPid, true);

			for (RdDirectroute directroute : directroutes) {
				if (directroute.getNodePid() == nodePid) {
					result.insertObject(directroute, ObjStatus.DELETE,
							directroute.getPid());
					continue;
				}
				// 分离node是经过线和退出线的连接node
				if (directroute.getVias().size() > 0
						&& directroute.getOutLinkPid() == linkPid) {

					if (isConnect(directroute, nodePid)) {

						result.insertObject(directroute, ObjStatus.DELETE,
								directroute.getPid());

						continue;
					}
				}
				if (directrouteInLink != null
						&& directroute.getInLinkPid() == linkPid) {
					directrouteInLink.put(directroute.getPid(), directroute);
				} else if (directrouteOutLink != null
						&& directroute.getOutLinkPid() == linkPid) {
					directrouteOutLink.put(directroute.getPid(), directroute);
				}
			}

			if (directrouteOutLink == null || directrouteInLink == null) {

				return;
			}

			int connectNode = link.getsNodePid() == nodePid ? link
					.geteNodePid() : link.getsNodePid();

			for (RdLink rdlink : rdlinks) {

				if (rdlink.getsNodePid() != connectNode
						&& rdlink.geteNodePid() != connectNode) {

					continue;
				}

				for (RdDirectroute directroute : directrouteInLink.values()) {

					directroute.changedFields().put("inLinkPid",
							rdlink.getPid());

					result.insertObject(directroute, ObjStatus.UPDATE,
							directroute.pid());
				}

				for (RdDirectroute directroute : directrouteOutLink.values()) {

					directroute.changedFields().put("outLinkPid",
							rdlink.getPid());

					result.insertObject(directroute, ObjStatus.UPDATE,
							directroute.pid());
				}
			}
		}
	}
	
	
	private boolean isConnect(RdDirectroute directroute, int nodePid) throws Exception {

		RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

		List<Integer> linkPids = new ArrayList<Integer>();

		for (IRow rowVia : directroute.getVias()) {

			RdDirectrouteVia via = (RdDirectrouteVia) rowVia;

			if (!linkPids.contains(via.getLinkPid())) {

				linkPids.add(via.getLinkPid());
			}
		}

		List<IRow> linkViaRows = rdLinkSelector
				.loadByIds(linkPids, true, false);

		boolean isConnect = false;

		for (IRow rowLink : linkViaRows) {

			RdLink rdLink = (RdLink) rowLink;

			// 经过线与退出线的分离node挂接
			if (rdLink.geteNodePid() == nodePid
					|| rdLink.getsNodePid() == nodePid) {

				isConnect = true;

				break;
			}
		}

		return isConnect;
	}
}