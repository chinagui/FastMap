package com.navinfo.dataservice.engine.edit.operation.obj.rdrestriction.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestriction;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionCondition;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionDetail;
import com.navinfo.dataservice.dao.glm.model.rd.restrict.RdRestrictionVia;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.restrict.RdRestrictionSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private RdRestriction restrict;

	private Connection conn;

	public Operation(Connection conn) {

		this.conn = conn;
	}

	public Operation(Command command, Connection conn, RdRestriction restrict) {
		this.command = command;

		this.conn = conn;
		
		this.restrict = restrict;

	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 判断是否存在交限进入线
		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(restrict, ObjStatus.DELETE, restrict.pid());

				return null;
			} else {

				boolean isChanged = restrict.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(restrict, ObjStatus.UPDATE,
							restrict.pid());
				}
			}
		}

		if (content.containsKey("details")) {
			JSONArray details = content.getJSONArray("details");

			for (int i = 0; i < details.size(); i++) {

				JSONObject detailJson = details.getJSONObject(i);

				if (detailJson.containsKey("objStatus")) {

					if (!ObjStatus.INSERT.toString().equals(
							detailJson.getString("objStatus"))) {

						RdRestrictionDetail detail = restrict.detailMap
								.get(detailJson.getInt("pid"));

						if (detail == null) {
							throw new Exception("detailId="
									+ detailJson.getInt("pid") + "的交限detail不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								detailJson.getString("objStatus"))) {
							result.insertObject(detail, ObjStatus.DELETE,
									restrict.pid());

							continue;
						} else if (ObjStatus.UPDATE.toString().equals(
								detailJson.getString("objStatus"))) {

							boolean isChanged = detail
									.fillChangeFields(detailJson);

							if (isChanged) {
								result.insertObject(detail, ObjStatus.UPDATE,
										restrict.pid());
							}
						}
					} else {
						RdRestrictionDetail detail = new RdRestrictionDetail();

						detail.Unserialize(detailJson);

						detail.setPid(PidUtil.getInstance()
								.applyRestrictionDetailPid());

						detail.setRestricPid(restrict.getPid());
						
						CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

						int relationShipType = calLinkOperateUtils.getRelationShipType(conn,
								restrict.getNodePid(), detail.getOutLinkPid());
						
						if(relationShipType == 2)
						{
							
							List<IRow> vias = new ArrayList<IRow>();
							
							List<Integer> viaLinkPids = calLinkOperateUtils.calViaLinks(conn,restrict.getInLinkPid(), restrict.getNodePid(), detail.getOutLinkPid());
							
							if(CollectionUtils.isNotEmpty(viaLinkPids))
							{
								for(int j=0;j<viaLinkPids.size();j++)
								{
									RdRestrictionVia via = new RdRestrictionVia();

									via.setDetailId(detail.getPid());

									via.setSeqNum(j+1);

									via.setLinkPid(viaLinkPids.get(j));

									vias.add(via);
								}
							}
							
							detail.setVias(vias);
						}
						
						detail.setRelationshipType(relationShipType);

						detail.setMesh(restrict.mesh());

						result.insertObject(detail, ObjStatus.INSERT,
								restrict.getPid());

						continue;
					}
				}

				if (detailJson.containsKey("conditions")) {

					int detailId = detailJson.getInt("pid");

					JSONArray conds = detailJson.getJSONArray("conditions");

					for (int j = 0; j < conds.size(); j++) {
						JSONObject cond = conds.getJSONObject(j);

						if (!cond.containsKey("objStatus")) {
							throw new Exception(
									"传入请求内容格式错误，conditions不存在操作类型objType");
						}

						if (!ObjStatus.INSERT.toString().equals(
								cond.getString("objStatus"))) {

							RdRestrictionCondition condition = restrict.conditionMap
									.get(cond.getString("rowId"));

							if (condition == null) {
								throw new Exception("rowId="
										+ cond.getString("rowId")
										+ "的交限condition不存在");
							}

							if (ObjStatus.DELETE.toString().equals(
									cond.getString("objStatus"))) {
								result.insertObject(condition,
										ObjStatus.DELETE, restrict.pid());

							} else if (ObjStatus.UPDATE.toString().equals(
									cond.getString("objStatus"))) {

								boolean isChanged = condition
										.fillChangeFields(cond);

								if (isChanged) {
									result.insertObject(condition,
											ObjStatus.UPDATE, restrict.pid());
								}
							}
						} else {
							RdRestrictionCondition condition = new RdRestrictionCondition();

							condition.Unserialize(cond);

							condition.setDetailId(detailId);

							condition.setMesh(restrict.mesh());

							result.insertObject(condition, ObjStatus.INSERT,
									restrict.pid());
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * 分离节点
	 * @param link 
	 * @param nodePid
	 * @param rdlinks 
	 * @param result
	 * @throws Exception
	 */
	public void departNode(RdLink link, int nodePid, List<RdLink> rdlinks,
			Result result) throws Exception {

		int linkPid = link.getPid();

		// 需要分离节点处理的RdRestriction
		Map<Integer, RdRestriction> restrictionDepart = new HashMap<Integer, RdRestriction>();

		// 需要分离节点处理的RdRestrictionDetail
		Map<Integer, RdRestrictionDetail> detailDepart = new HashMap<Integer, RdRestrictionDetail>();
		
		// 分离节点不处理，跨图幅打断需要处理的RdRestriction
		Map<Integer, RdRestriction> restrictionMesh = null;

		// 分离节点不处理，跨图幅打断需要处理的RdRestrictionDetail
		Map<Integer, RdRestrictionDetail> detailMesh = null;

		if (rdlinks!=null &&rdlinks.size() >1) {			
			
			restrictionMesh = new HashMap<Integer, RdRestriction>();

			detailMesh = new HashMap<Integer, RdRestrictionDetail>();
		}
		
		RdRestrictionSelector selector = new RdRestrictionSelector(
				this.conn);

		// link作为进入线的RdRestriction
		List<RdRestriction> restrictions = selector
				.loadByLink(linkPid, 1, true);
		getInLinkDepartInfo(nodePid, restrictions, restrictionDepart,
				restrictionMesh);

		// link作为退出线的RdRestriction
		restrictions = selector.loadByLink(linkPid, 2, true);

		Map<Integer, RdRestrictionDetail> detailTmp = new HashMap<Integer, RdRestrictionDetail>();

		getOutLinkDepartInfo(nodePid, linkPid, restrictions, detailTmp,
				detailMesh);

		for (RdRestriction restriction : restrictions) {

			if (!detailTmp.containsKey(restriction.getPid())) {

				continue;
			}

			if (restriction.getDetails().size() > 1) {

				RdRestrictionDetail delDetail = detailTmp.get(restriction
						.getPid());

				detailDepart.put(delDetail.getPid(), delDetail);

			} else {

				restrictionDepart.put(restriction.getPid(), restriction);
			}
		}

		for (RdRestrictionDetail delDetail : detailDepart.values()) {

			result.insertObject(delDetail, ObjStatus.DELETE,
					delDetail.pid());
		}

		for (RdRestriction restriction : restrictionDepart.values()) {

			result.insertObject(restriction, ObjStatus.DELETE,
					restriction.pid());
		}

		if (restrictionMesh == null || detailMesh == null) {
			
			return;
		}

		int connectNode = link.getsNodePid() == nodePid ? link.geteNodePid()
				: link.getsNodePid();

		for (RdLink rdlink : rdlinks) {
			
			if (rdlink.getsNodePid() != connectNode
					&& rdlink.geteNodePid() != connectNode) {
				
				continue;
			}

			for (RdRestriction restriction : restrictionMesh.values()) {
				
				restriction.changedFields().put("inLinkPid", rdlink.getPid());

				result.insertObject(restriction, ObjStatus.UPDATE,
						restriction.pid());
			}

			for (RdRestrictionDetail detail : detailMesh.values()) {
				
				detail.changedFields().put("outLinkPid", rdlink.getPid());

				result.insertObject(detail, ObjStatus.UPDATE,
						detail.getRestricPid());
			}
		}
	}

	/**
	 * 获取link作为进入线时交限的信息
	 * 
	 * @param nodePid
	 *            分离点
	 * @param restrictions
	 *            link作为进入线的所有RdRestriction
	 * @param restrictionDepart
	 *            分离点为进入点的交限
	 * @param restrictionMesh
	 *            分离点不是进入点的交限
	 * @throws Exception
	 */
	private void getInLinkDepartInfo(int nodePid,
			List<RdRestriction> restrictions,
			Map<Integer, RdRestriction> restrictionDepart,
			Map<Integer, RdRestriction> restrictionMesh) throws Exception {

		for (RdRestriction restriction : restrictions) {

			if (restriction.getNodePid() == nodePid) {
				restrictionDepart.put(restriction.getPid(), restriction);

			} else if (restrictionMesh != null) {

				restrictionMesh.put(restriction.getPid(), restriction);
			}
		}
	}

	/**
	 * 获取link作为退出线时交限的信息
	 * 
	 * @param nodePid
	 *            分离点
	 * @param linkPid
	 *            分离线
	 * @param restrictions
	 *            link作为退出线的所有RdRestriction
	 * @param detailTmp
	 *            分离点为退出线的进入点的交限
	 * @param detailMesh
	 *            分离点不是退出线的进入点的交限
	 * @throws Exception
	 */
	private void getOutLinkDepartInfo(int nodePid, int linkPid,
			List<RdRestriction> restrictions,
			Map<Integer, RdRestrictionDetail> detailTmp,
			Map<Integer, RdRestrictionDetail> detailMesh) throws Exception {

		RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

		for (RdRestriction restriction : restrictions) {

			for (IRow rowDetail : restriction.getDetails()) {

				RdRestrictionDetail detail = (RdRestrictionDetail) rowDetail;

				// 排除其他退出线
				if (detail.getOutLinkPid() != linkPid) {
					
					continue;
				}

				// 分离node为交限进入点
				if (restriction.getNodePid() == nodePid) {

					detailTmp.put(detail.getRestricPid(), detail);

					continue;
				}

				// 无经过线
				if (detail.getVias().size() == 0) {

					if (detailMesh != null) {

						detailMesh.put(detail.getRestricPid(), detail);
					}

					continue;
				}

				List<Integer> linkPids = new ArrayList<Integer>();

				for (IRow rowVia : detail.getVias()) {

					RdRestrictionVia via = (RdRestrictionVia) rowVia;

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

					detailTmp.put(detail.getRestricPid(), detail);

				} else if (detailMesh != null) {

					detailMesh.put(detail.getRestricPid(), detail);
				}
			}
		}
	}
	
}
