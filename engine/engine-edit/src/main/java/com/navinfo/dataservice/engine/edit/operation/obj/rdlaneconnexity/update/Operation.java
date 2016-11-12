package com.navinfo.dataservice.engine.edit.operation.obj.rdlaneconnexity.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneConnexity;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneTopology;
import com.navinfo.dataservice.dao.glm.model.rd.laneconnexity.RdLaneVia;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.laneconnexity.RdLaneConnexitySelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RdLaneConnexity lane;

	private Connection conn;

	public Operation(Connection conn) {

		this.conn = conn;
	}

	public Operation(Command command, RdLaneConnexity lane, Connection conn) {
		this.command = command;

		this.lane = lane;

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(lane, ObjStatus.DELETE, lane.pid());

				return null;
			} else {

				if (content.containsKey("laneInfo")) {
					if (content.getString("laneInfo").length() == 0) {
						result.insertObject(lane, ObjStatus.DELETE, lane.pid());

						return null;
					}
					this.caleRdlaneForRdLaneconnexity(result);
				}

				boolean isChanged = lane.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(lane, ObjStatus.UPDATE, lane.pid());
				}
			}
		}

		if (content.containsKey("topos")) {

			Set<Integer> topoPids = new HashSet<Integer>();

			for (Integer topoPid : lane.topologyMap.keySet()) {
				topoPids.add(topoPid);
			}

			JSONArray topos = content.getJSONArray("topos");

			for (int i = 0; i < topos.size(); i++) {

				JSONObject json = topos.getJSONObject(i);

				int topoPid = 0;
				
				if (json.containsKey("objStatus")) {
					
					if (!ObjStatus.INSERT.toString().equals(
							json.getString("objStatus"))) {

						RdLaneTopology topo = lane.topologyMap.get(json
								.getInt("pid"));

						if (topo == null) {
							throw new Exception("pid=" + json.getInt("pid")
									+ "的rd_lane_topology不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								json.getString("objStatus"))) {

							topoPids.remove(topo.getPid());

							result.insertObject(topo, ObjStatus.DELETE,
									lane.pid());
							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								json.getString("objStatus"))) {

							topo.fillChangeFields(json);
							
							if(topo.changedFields().containsKey("outLinkPid"))
							{
								int inNodePid = lane.getNodePid();
										
								int outLinkPid = json.getInt("outLinkPid");
								
								CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

								int relationShipType = calLinkOperateUtils.getRelationShipType(conn,
										inNodePid, outLinkPid);
								
								topo.changedFields().put("relationshipType", relationShipType);
							}

							if (!topo.changedFields().isEmpty()) {
								result.insertObject(topo, ObjStatus.UPDATE,
										lane.pid());
							}
						}
					} else {
						RdLaneTopology topo = new RdLaneTopology();

						topo.Unserialize(json);

						topo.setPid(PidUtil.getInstance()
								.applyLaneTopologyPid());

						topo.setConnexityPid(lane.getPid());

						topo.setMesh(lane.mesh());
						
						topoPid = topo.getPid();
						
						int inNodePid = lane.getNodePid();
						
						int outLinkPid = topo.getOutLinkPid();
						
						CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

						int relationShipType = calLinkOperateUtils.getRelationShipType(conn,
								inNodePid, outLinkPid);
						
						topo.setRelationshipType(relationShipType);
						
						result.insertObject(topo, ObjStatus.INSERT, lane.pid());

						topoPids.add(topo.getPid());
					}
				}

				if (json.containsKey("vias")) {
					JSONArray vias = json.getJSONArray("vias");

					for (int j = 0; j < vias.size(); j++) {

						JSONObject viajson = vias.getJSONObject(j);

						if (viajson.containsKey("objStatus")) {

							if (!ObjStatus.INSERT.toString().equals(
									viajson.getString("objStatus"))) {

								RdLaneVia via = lane.viaMap.get(viajson
										.getString("rowId"));

								if (via == null) {
									throw new Exception("rowId="
											+ viajson.getString("rowId")
											+ "的rd_lane_via不存在");
								}

								if (ObjStatus.DELETE.toString().equals(
										viajson.getString("objStatus"))) {
									result.insertObject(via, ObjStatus.DELETE,
											lane.pid());

									continue;
								} else if (ObjStatus.UPDATE.toString().equals(
										viajson.getString("objStatus"))) {

									boolean isChanged = via
											.fillChangeFields(viajson);

									if (isChanged) {
										result.insertObject(via,
												ObjStatus.UPDATE, lane.pid());
									}
								}
							} else {
								RdLaneVia via = new RdLaneVia();

								via.setSeqNum(viajson.getInt("seqNum"));
								
								via.setGroupId(viajson.getInt("groupId"));
								
								via.setLinkPid(viajson.getInt("linkPid"));
								
								if(viajson.containsKey("topologyId") && viajson.getInt("topologyId") !=0)
								{
									via.setTopologyId(viajson.getInt("topologyId"));
								}
								else if(json.containsKey("pid") && json.getInt("pid") !=0)
								{
									via.setTopologyId(json.getInt("pid"));
								}
								else
								{
									via.setTopologyId(topoPid);
								}

								via.setMesh(lane.mesh());

								result.insertObject(via, ObjStatus.INSERT,
										lane.pid());

								continue;
							}
						}

					}
				}
			}

			if (topoPids.size() == 0) {

				result.clear();

				result.insertObject(lane, ObjStatus.DELETE, lane.pid());
			}
		}

		return null;

	}

	/***
	 * 修改车信维护车道信息
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void caleRdlaneForRdLaneconnexity(Result result) throws Exception {
		String laneInfo = command.getContent().getString("laneInfo");

		/*
		 * if (StringUtils.isNotEmpty(laneInfo)) { String lanes =
		 * laneInfo.replace("[", "").replace("]", ""); List<String> laneList =
		 * Arrays.asList(lanes.split(","));
		 * com.navinfo.dataservice.engine.edit.operation
		 * .topo.batch.batchrdlane.Operation operation = new
		 * com.navinfo.dataservice
		 * .engine.edit.operation.topo.batch.batchrdlane.Operation( conn);
		 * operation.setLanInfos(laneList); operation.setConnexity(lane);
		 * operation.refRdLaneForRdLaneconnexity(result);
		 * 
		 * }
		 */

	}

	public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
			Result result) throws Exception {

		int linkPid = link.getPid();

		// 需要分离节点处理的RdLaneConnexity
		Map<Integer, RdLaneConnexity> connexityDepart = new HashMap<Integer, RdLaneConnexity>();

		// 需要分离节点处理的RdLaneTopology
		Map<Integer, RdLaneTopology> topologyDepart = new HashMap<Integer, RdLaneTopology>();

		//跨图幅打断需要处理的RdLaneConnexity
		Map<Integer, RdLaneConnexity> connexityMesh = null;

		//跨图幅打断需要处理的RdLaneTopology
		Map<Integer, RdLaneTopology> topologyMesh = null;

		if (rdlinks != null && rdlinks.size() > 1) {

			connexityMesh = new HashMap<Integer, RdLaneConnexity>();

			topologyMesh = new HashMap<Integer, RdLaneTopology>();
		}

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(
				this.conn);

		// link作为进入线的RdLaneConnexity
		List<RdLaneConnexity> laneConnexitys = selector.loadByLink(linkPid, 1,
				true);

		getInLinkDepartInfo(nodePid, laneConnexitys, connexityDepart,
				connexityMesh);

		// link作为退出线的RdLaneConnexity
		laneConnexitys = selector.loadByLink(linkPid, 2, true);

		Map<Integer, RdLaneTopology> topologyTmp = new HashMap<Integer, RdLaneTopology>();

		getOutLinkDepartInfo(nodePid, linkPid, laneConnexitys, topologyTmp,
				topologyMesh);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (!topologyTmp.containsKey(laneConnexity.getPid())) {

				continue;
			}

			if (laneConnexity.getTopos().size() > 1) {

				RdLaneTopology delTopology = topologyTmp.get(laneConnexity
						.getPid());

				topologyDepart.put(delTopology.getPid(), delTopology);

			} else {

				connexityDepart.put(laneConnexity.getPid(), laneConnexity);
			}
		}

		for (RdLaneTopology delTopology : topologyDepart.values()) {

			result.insertObject(delTopology, ObjStatus.DELETE,
					delTopology.parentPKValue());
		}

		for (RdLaneConnexity laneConnexity : connexityDepart.values()) {

			result.insertObject(laneConnexity, ObjStatus.DELETE,
					laneConnexity.pid());
		}

		if (connexityMesh == null || topologyMesh == null) {

			return;
		}

		int connectNode = link.getsNodePid() == nodePid ? link.geteNodePid()
				: link.getsNodePid();

		for (RdLink rdlink : rdlinks) {

			if (rdlink.getsNodePid() != connectNode
					&& rdlink.geteNodePid() != connectNode) {

				continue;
			}

			for (RdLaneConnexity laneConnexity : connexityMesh.values()) {

				laneConnexity.changedFields().put("inLinkPid", rdlink.getPid());

				result.insertObject(laneConnexity, ObjStatus.UPDATE,
						laneConnexity.pid());
			}

			for (RdLaneTopology laneTopology : topologyMesh.values()) {

				laneTopology.changedFields().put("outLinkPid", rdlink.getPid());

				result.insertObject(laneTopology, ObjStatus.UPDATE,
						laneTopology.parentPKValue());
			}
		}
	}

	/**
	 * 获取link作为进入线时车信的信息
	 * 
	 * @param nodePid
	 *            分离点
	 * @param laneConnexitys
	 *            link作为进入线的所有RdLaneConnexity
	 * @param connexityDepart
	 *            分离点为进入点的车信
	 * @param connexityMesh
	 *            分离点不是进入点的车信
	 * @throws Exception
	 */
	private void getInLinkDepartInfo(int nodePid,
			List<RdLaneConnexity> laneConnexitys,
			Map<Integer, RdLaneConnexity> connexityDepart,
			Map<Integer, RdLaneConnexity> connexityMesh) throws Exception {

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			if (laneConnexity.getNodePid() == nodePid) {
				
				connexityDepart.put(laneConnexity.getPid(), laneConnexity);

			} else if (connexityMesh != null) {

				connexityMesh.put(laneConnexity.getPid(), laneConnexity);
			}
		}
	}

	/**
	 * 获取link作为退出线时车信的信息
	 * 
	 * @param nodePid
	 *            分离点
	 * @param linkPid
	 *            分离线
	 * @param laneConnexitys
	 *            link作为退出线的所有RdLaneConnexity
	 * @param topologyTmp
	 *            分离点为退出线的进入点的车信
	 * @param topologyMesh
	 *            分离点不是退出线的进入点的车信
	 * @throws Exception
	 */
	private void getOutLinkDepartInfo(int nodePid, int linkPid,
			List<RdLaneConnexity> laneConnexitys,
			Map<Integer, RdLaneTopology> topologyTmp,
			Map<Integer, RdLaneTopology> topologyMesh) throws Exception {

		RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			for (IRow rowTopology : laneConnexity.getTopos()) {

				RdLaneTopology topo = (RdLaneTopology) rowTopology;

				// 排除其他退出线
				if (topo.getOutLinkPid() != linkPid) {
					continue;
				}

				// 分离node为车信进入点
				if (laneConnexity.getNodePid() == nodePid) {

					topologyTmp.put(topo.getConnexityPid(), topo);

					continue;
				}

				// 无经过线
				if (topo.getVias().size() == 0) {

					if (topologyMesh != null) {

						topologyMesh.put(topo.getConnexityPid(), topo);
					}

					continue;
				}

				List<Integer> linkPids = new ArrayList<Integer>();

				for (IRow rowVia : topo.getVias()) {

					RdLaneVia via = (RdLaneVia) rowVia;

					if (!linkPids.contains(via.getLinkPid())) {

						linkPids.add(via.getLinkPid());
					}
				}

				List<IRow> linkViaRows = rdLinkSelector.loadByIds(linkPids,
						true, false);

				boolean isConnect = false;

				for (IRow rowLink : linkViaRows) {

					RdLink rdLink = (RdLink) rowLink;

					// 经过线挂接与退出线的分离node挂接
					if (rdLink.geteNodePid() == nodePid
							|| rdLink.getsNodePid() == nodePid) {

						isConnect = true;

						break;
					}
				}

				if (isConnect) {

					topologyTmp.put(topo.getConnexityPid(), topo);

				} else if (topologyMesh != null) {

					topologyMesh.put(topo.getConnexityPid(), topo);
				}
			}
		}
	}
	

	public void breakRdLink(RdLink deleteLink, List<RdLink> newLinks,
			Result result) throws Exception {
		if (this.command != null || conn == null) {

			return;
		}

		RdLaneConnexitySelector selector = new RdLaneConnexitySelector(conn);

		// link为进入线
		List<RdLaneConnexity> laneConnexitys = selector.loadByLink(
				deleteLink.getPid(), 1, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			breakInLink(laneConnexity, newLinks, result);
		}

		// link为退出线
		laneConnexitys = selector.loadByLink(deleteLink.getPid(),
				2, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			breakOutLink(deleteLink, laneConnexity, newLinks, result);
		}

		// link为经过线
		laneConnexitys = selector.loadByLink(deleteLink.getPid(),
				3, true);

		for (RdLaneConnexity laneConnexity : laneConnexitys) {

			breakPassLink(deleteLink, laneConnexity, newLinks, result);
		}

	}

	private void breakInLink(RdLaneConnexity laneConnexity, List<RdLink> newLinks,
			Result result) {

		for (RdLink link : newLinks) {

			if (link.getsNodePid() == laneConnexity.getNodePid()
					|| link.geteNodePid() == laneConnexity.getNodePid()) {

				laneConnexity.changedFields().put("inLinkPid", link.getPid());

				result.insertObject(laneConnexity, ObjStatus.UPDATE,
						laneConnexity.pid());

				break;
			}

		}
	}

	private void breakOutLink(RdLink deleteLink, RdLaneConnexity laneConnexity,
			List<RdLink> newLinks, Result result) throws Exception {

		for (IRow rowTopo : laneConnexity.getTopos()) {

			RdLaneTopology topo = (RdLaneTopology) rowTopo;

			// 排除其他详细信息
			if (topo.getOutLinkPid() != deleteLink.getPid()) {

				continue;
			}

			int connectionNodePid = 0;

			if (topo.getVias().size() == 0) {

				connectionNodePid = laneConnexity.getNodePid();

			} else {

				RdLaneVia lastVia = (RdLaneVia) topo.getVias()
						.get(0);

				for (IRow rowVia : topo.getVias()) {

					RdLaneVia via = (RdLaneVia) rowVia;

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

					topo.changedFields().put("outLinkPid", link.getPid());

					result.insertObject(topo, ObjStatus.UPDATE,
							laneConnexity.pid());

					break;
				}
			}

		}
	}

	private void breakPassLink(RdLink deleteLink, RdLaneConnexity laneConnexity,
			List<RdLink> newLinks, Result result) throws Exception {

		for (IRow rowTopo : laneConnexity.getTopos()) {

			RdLaneTopology topo = (RdLaneTopology) rowTopo;

			// 对经过线分组
			Map<Integer, List<RdLaneVia>> viaGroupId = new HashMap<Integer, List<RdLaneVia>>();

			for (IRow row : topo.getVias()) {

				RdLaneVia via = (RdLaneVia) row;

				if (viaGroupId.get(via.getGroupId()) == null) {
					viaGroupId.put(via.getGroupId(),
							new ArrayList<RdLaneVia>());
				}

				viaGroupId.get(via.getGroupId()).add(via);
			}

			// 分组处理经过线
			for (int key : viaGroupId.keySet()) {

				// 经过线组
				List<RdLaneVia> viaGroup = viaGroupId.get(key);

				RdLaneVia breakVia = null;

				for (RdLaneVia via : viaGroup) {

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

					connectionNodePid = laneConnexity.getNodePid();

				} else {

					int preLinkPid = 0;

					for (RdLaneVia via : viaGroup) {

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

						RdLaneVia newVia = new RdLaneVia();

						newVia.setTopologyId(breakVia.getTopologyId());

						newVia.setGroupId(breakVia.getGroupId());

						newVia.setLinkPid(newLinks.get(i).getPid());

						newVia.setSeqNum(breakVia.getSeqNum() + i);

						result.insertObject(newVia, ObjStatus.INSERT,
								topo.parentPKValue());
					}

				} else {

					for (int i = newLinks.size(); i > 0; i--) {

						RdLaneVia newVia = new RdLaneVia();

						newVia.setTopologyId(breakVia.getTopologyId());

						newVia.setGroupId(breakVia.getGroupId());

						newVia.setLinkPid(newLinks.get(i - 1).getPid());

						newVia.setSeqNum(breakVia.getSeqNum() + newLinks.size()
								- i);

						result.insertObject(newVia, ObjStatus.INSERT,
								topo.parentPKValue());
					}
				}

				result.insertObject(breakVia, ObjStatus.DELETE,
						topo.parentPKValue());

				// 维护后续经过线序号
				for (RdLaneVia via : viaGroup) {

					if (via.getSeqNum() > breakVia.getSeqNum()) {

						via.changedFields().put("seqNum",
								via.getSeqNum() + newLinks.size() - 1);

						result.insertObject(via, ObjStatus.UPDATE,
								topo.parentPKValue());
					}
				}
			}
		}
	}

}
