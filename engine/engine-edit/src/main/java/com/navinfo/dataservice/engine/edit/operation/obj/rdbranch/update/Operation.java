package com.navinfo.dataservice.engine.edit.operation.obj.rdbranch.update;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchVia;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboardName;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.branch.RdBranchSelector;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.engine.edit.utils.CalLinkOperateUtils;

public class Operation implements IOperation {

	private Command command;

	private RdBranch branch;

	private Connection conn;

	public Operation(Command command, RdBranch branch, Connection conn) {
		this.command = command;

		this.branch = branch;

		this.conn = conn;
	}

	public Operation(Connection conn) {

		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 主表是否变化
		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(branch, ObjStatus.DELETE, branch.pid());

				return null;
			} else {

				updateLinkInfo(content, result);

				boolean isChanged = branch.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
				}
			}
		}

		// 更新分歧详细信息表(0-4)
		updateBranchDetail(result, content);

		// 更新分歧实景图 (5)
		updateBranchRealimage(result, content);

		// 更新实景看板(6)
		updateSignasreal(result, content);

		// 更新连续分歧(7)
		updateSeriesbranch(result, content);

		// 更新大路口交叉点(8)
		updateSchematic(result, content);

		// 更新方向看板(9)
		updateSignboard(result, content);

		return null;
	}

	/**
	 * 更新退出线、经过线、关系类型
	 * 
	 * @param content
	 * @param result
	 * @throws Exception
	 */
	private void updateLinkInfo(JSONObject content, Result result)
			throws Exception {

		// 前台未修改退出线，直接返回
		if (!content.containsKey("outLinkPid")) {
			return;
		}

		int outPid = content.getInt("outLinkPid");

		// 前台修改退出线与当前退出线相同，直接返回
		if (this.branch.getOutLinkPid() == outPid) {
			return;
		}

		CalLinkOperateUtils calLinkOperateUtils = new CalLinkOperateUtils();

		int relationShipType = calLinkOperateUtils.getRelationShipType(conn,
				this.branch.getNodePid(), outPid);

		// 前台未修改关系类型且关系类型改变
		if (this.branch.getRelationshipType() != relationShipType
				&& !content.containsKey("relationshipType")) {

			content.put("relationshipType", relationShipType);
		}

		List<Integer> viaLinks = calLinkOperateUtils.calViaLinks(conn,
				this.branch.getInLinkPid(), this.branch.getNodePid(), outPid);
		// 删除原经过线
		for (IRow row : this.branch.getVias()) {
			result.insertObject(row, ObjStatus.DELETE, branch.pid());
		}

		int seqNum = 1;

		// 重新设置经过线
		for (Integer linkPid : viaLinks) {
			RdBranchVia via = new RdBranchVia();

			via.setBranchPid(branch.getPid());

			via.setLinkPid(linkPid);

			via.setSeqNum(seqNum);

			via.setMesh(this.branch.mesh());

			seqNum++;

			result.insertObject(via, ObjStatus.INSERT, branch.pid());
		}

	}

	/**
	 * 更新分歧详细信息表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateBranchDetail(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("details")) {
			return;
		}

		JSONArray details = content.getJSONArray("details");

		for (int i = 0; i < details.size(); i++) {

			JSONObject json = details.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdBranchDetail detail = branch.detailMap.get(json
							.getInt("pid"));

					if (detail == null) {
						throw new Exception("detailId=" + json.getInt("pid")
								+ "的rd_branch_detail不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(detail, ObjStatus.DELETE,
								branch.getPid());

						continue;

					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						if (json.containsKey("arrowCode")) {
							String arrowCode = json.getString("arrowCode");

							if (StringUtils.isNotEmpty(arrowCode)
									&& arrowCode.length() > 10) {
								throw new Exception("分歧箭头图号码超过了10位");
							}

						} else if (json.containsKey("patternCode")) {
							String patternCode = json.getString("patternCode");

							if (StringUtils.isNotEmpty(patternCode)
									&& patternCode.length() > 10) {
								throw new Exception("分歧模式图号码超过了10位");
							}
						}
						boolean isChanged = detail.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(detail, ObjStatus.UPDATE,
									branch.pid());
						}
					}
				} else {
					RdBranchDetail detail = new RdBranchDetail();

					detail.Unserialize(json);

					detail.setPid(PidUtil.getInstance().applyBranchDetailId());

					detail.setBranchPid(branch.getPid());

					detail.setMesh(branch.mesh());

					result.insertObject(detail, ObjStatus.INSERT, branch.pid());

					continue;
				}
			}

			if (json.containsKey("names")) {

				int detailId = json.getInt("pid");

				RdBranchDetail detail = branch.detailMap.get(detailId);

				JSONArray names = json.getJSONArray("names");

				for (int j = 0; j < names.size(); j++) {
					JSONObject cond = names.getJSONObject(j);

					if (!cond.containsKey("objStatus")) {
						throw new Exception(
								"传入请求内容格式错误，conditions不存在操作类型objStatus");
					}

					if (!ObjStatus.INSERT.toString().equals(
							cond.getString("objStatus"))) {

						RdBranchName name = detail.nameMap.get(cond
								.getInt("pid"));

						if (name == null) {
							throw new Exception("pid=" + cond.getInt("pid")
									+ "的rd_branch_name不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								cond.getString("objStatus"))) {
							result.insertObject(name, ObjStatus.DELETE,
									branch.getPid());

						} else if (ObjStatus.UPDATE.toString().equals(
								cond.getString("objStatus"))) {

							boolean isChanged = name.fillChangeFields(cond);

							if (isChanged) {
								result.insertObject(name, ObjStatus.UPDATE,
										branch.pid());
							}
						}
					} else {
						RdBranchName name = new RdBranchName();

						name.Unserialize(cond);

						name.setDetailId(detailId);

						name.setMesh(branch.mesh());

						name.setPid(PidUtil.getInstance().applyBranchNameId());

						result.insertObject(name, ObjStatus.INSERT,
								branch.pid());
					}
				}
			}
		}
	}

	/**
	 * 更新方向看板表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateSignboard(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("signboards")) {
			return;
		}

		JSONArray signboards = content.getJSONArray("signboards");

		for (int i = 0; i < signboards.size(); i++) {

			JSONObject json = signboards.getJSONObject(i);

			if (json.containsKey("objStatus")) {

				if (!ObjStatus.INSERT.toString().equals(
						json.getString("objStatus"))) {

					RdSignboard signboard = branch.signboardMap.get(json
							.getInt("pid"));

					if (signboard == null) {
						throw new Exception("SIGNBOARD_ID="
								+ json.getInt("pid") + "的RdSignboard不存在");
					}

					if (ObjStatus.DELETE.toString().equals(
							json.getString("objStatus"))) {
						result.insertObject(signboard, ObjStatus.DELETE,
								branch.getPid());

						continue;
					} else if (ObjStatus.UPDATE.toString().equals(
							json.getString("objStatus"))) {

						boolean isChanged = signboard.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(signboard, ObjStatus.UPDATE,
									branch.pid());
						}
					}
				} else {
					RdSignboard signboard = new RdSignboard();

					signboard.Unserialize(json);

					signboard.setPid(PidUtil.getInstance().applyRdSignboard());

					signboard.setBranchPid(branch.getPid());

					signboard.setMesh(branch.mesh());

					result.insertObject(signboard, ObjStatus.INSERT,
							branch.pid());

					continue;
				}
			}

			if (json.containsKey("names")) {

				int signboardId = json.getInt("pid");

				RdSignboard signboard = branch.signboardMap.get(signboardId);

				JSONArray names = json.getJSONArray("names");

				for (int j = 0; j < names.size(); j++) {
					JSONObject cond = names.getJSONObject(j);

					if (!cond.containsKey("objStatus")) {
						throw new Exception(
								"传入请求内容格式错误，conditions不存在操作类型objStatus");
					}

					if (!ObjStatus.INSERT.toString().equals(
							cond.getString("objStatus"))) {

						for (IRow row : signboard.getNames()) {
							RdSignboardName name = (RdSignboardName) row;

							signboard.nameMap.put(name.getPid(), name);
						}

						RdSignboardName name = signboard.nameMap.get(cond
								.getInt("pid"));

						if (name == null) {
							throw new Exception("NAME_ID=" + cond.getInt("pid")
									+ "的RdSignboardName不存在");
						}

						if (ObjStatus.DELETE.toString().equals(
								cond.getString("objStatus"))) {
							result.insertObject(name, ObjStatus.DELETE,
									branch.getPid());

						} else if (ObjStatus.UPDATE.toString().equals(
								cond.getString("objStatus"))) {

							boolean isChanged = name.fillChangeFields(cond);

							if (isChanged) {
								result.insertObject(name, ObjStatus.UPDATE,
										branch.pid());
							}
						}
					} else {
						RdSignboardName name = new RdSignboardName();

						name.Unserialize(cond);

						name.setSignboardId(signboardId);

						name.setMesh(branch.mesh());

						name.setPid(PidUtil.getInstance()
								.applyRdSignboardName());

						result.insertObject(name, ObjStatus.INSERT,
								branch.pid());
					}
				}
			}
		}
	}

	/**
	 * 更新实景看板表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateSignasreal(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("signasreals")) {
			return;
		}

		JSONArray signasreals = content.getJSONArray("signasreals");

		for (int i = 0; i < signasreals.size(); i++) {

			JSONObject json = signasreals.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				RdSignasreal signasreal = branch.signasrealMap.get(json
						.getInt("pid"));

				if (signasreal == null) {
					throw new Exception("SIGNBOARD_ID=" + json.getInt("pid")
							+ "的RdSignasreal不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {
					result.insertObject(signasreal, ObjStatus.DELETE,
							branch.getPid());

					continue;
				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = signasreal.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(signasreal, ObjStatus.UPDATE,
								branch.pid());
					}
				}
			} else {
				RdSignasreal signasreal = new RdSignasreal();

				signasreal.Unserialize(json);

				signasreal.setPid(PidUtil.getInstance().applyRdSignasreal());

				signasreal.setBranchPid(branch.getPid());

				signasreal.setMesh(branch.mesh());

				result.insertObject(signasreal, ObjStatus.INSERT, branch.pid());
			}
		}
	}

	/**
	 * 更新大路口交叉点图形表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateSchematic(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("schematics")) {
			return;
		}

		JSONArray schematics = content.getJSONArray("schematics");

		for (int i = 0; i < schematics.size(); i++) {

			JSONObject json = schematics.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				RdBranchSchematic schematic = branch.schematicMap.get(json
						.getInt("pid"));

				if (schematic == null) {
					throw new Exception("SCHEMATIC_ID=" + json.getInt("pid")
							+ "的RdBranchSchematic不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {
					result.insertObject(schematic, ObjStatus.DELETE,
							branch.getPid());
					continue;
				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = schematic.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(schematic, ObjStatus.UPDATE,
								branch.pid());
					}
				}
			} else {
				RdBranchSchematic schematic = new RdBranchSchematic();

				schematic.Unserialize(json);

				schematic.setPid(PidUtil.getInstance().applyBranchSchematic());

				schematic.setBranchPid(branch.getPid());

				schematic.setMesh(branch.mesh());

				result.insertObject(schematic, ObjStatus.INSERT, branch.pid());
			}
		}
	}

	/**
	 * 更新分歧实景图表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateBranchRealimage(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("realimages")) {
			return;
		}

		JSONArray realimages = content.getJSONArray("realimages");

		for (int i = 0; i < realimages.size(); i++) {

			JSONObject json = realimages.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				RdBranchRealimage realimage = branch.realimageMap.get(json
						.getString("rowId"));

				if (realimage == null) {
					throw new Exception("ROWID=" + json.getString("rowId")
							+ "的RdBranchRealimage不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {
					result.insertObject(realimage, ObjStatus.DELETE,
							branch.getPid());

					continue;
				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = realimage.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(realimage, ObjStatus.UPDATE,
								branch.pid());
					}
				}
			} else {
				RdBranchRealimage realimage = new RdBranchRealimage();

				realimage.Unserialize(json);

				realimage.setBranchPid(branch.getPid());

				realimage.setMesh(branch.mesh());

				result.insertObject(realimage, ObjStatus.INSERT, branch.pid());
			}
		}
	}

	/**
	 * 更新连续分歧表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */

	private void updateSeriesbranch(Result result, JSONObject content)
			throws Exception {
		if (!content.containsKey("seriesbranches")) {
			return;
		}

		JSONArray seriesbranches = content.getJSONArray("seriesbranches");

		for (int i = 0; i < seriesbranches.size(); i++) {

			JSONObject json = seriesbranches.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				RdSeriesbranch seriesbranche = branch.seriesbranchMap.get(json
						.getString("rowId"));

				if (seriesbranche == null) {
					throw new Exception("ROWID=" + json.getString("rowId")
							+ "的RdSeriesbranch不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {
					result.insertObject(seriesbranche, ObjStatus.DELETE,
							branch.getPid());

					continue;
				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = seriesbranche.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(seriesbranche, ObjStatus.UPDATE,
								branch.pid());
					}
				}
			} else {
				RdSeriesbranch seriesbranche = new RdSeriesbranch();

				seriesbranche.Unserialize(json);

				seriesbranche.setBranchPid(branch.getPid());

				seriesbranche.setMesh(branch.mesh());

				result.insertObject(seriesbranche, ObjStatus.INSERT,
						branch.pid());
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

		int linkPid = link.getPid();

		// 跨图幅处理的以link为进入线RdBranch
		Map<Integer, RdBranch> branchInLink = null;

		// 跨图幅处理的以link为退出线RdBranch
		Map<Integer, RdBranch> branchOutLink = null;

		if (rdlinks != null && rdlinks.size() > 1) {

			branchInLink = new HashMap<Integer, RdBranch>();

			branchOutLink = new HashMap<Integer, RdBranch>();
		}

		RdBranchSelector selector = new RdBranchSelector(this.conn);

		List<RdBranch> branchs = new ArrayList<RdBranch>();

		// link作为进入线的RdBranch
		branchs.addAll(selector.loadByLinkPid(linkPid, 1, true));

		// link作为 退出线的RdBranch
		branchs.addAll(selector.loadByLinkPid(linkPid, 2, true));

		for (RdBranch branch : branchs) {

			if (branch.getNodePid() == nodePid) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());

				continue;
			}

			// 分离node是经过线和退出线的连接node
			if (branch.getVias().size() > 0
					&& branch.getOutLinkPid() == linkPid
					&& isConnect(branch, nodePid)) {

				result.insertObject(branch, ObjStatus.DELETE, branch.getPid());

				continue;

			}

			if (branchInLink != null && branch.getInLinkPid() == linkPid) {

				branchInLink.put(branch.getPid(), branch);

			} else if (branchOutLink != null
					&& branch.getOutLinkPid() == linkPid) {

				branchOutLink.put(branch.getPid(), branch);
			}
		}

		if (branchOutLink == null || branchInLink == null) {

			return;
		}

		int connectNode = link.getsNodePid() == nodePid ? link.geteNodePid()
				: link.getsNodePid();

		for (RdLink rdlink : rdlinks) {

			if (rdlink.getsNodePid() != connectNode
					&& rdlink.geteNodePid() != connectNode) {

				continue;
			}
			for (RdBranch branch : branchInLink.values()) {

				branch.changedFields().put("inLinkPid", rdlink.getPid());

				result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
			}

			for (RdBranch branch : branchOutLink.values()) {

				branch.changedFields().put("outLinkPid", rdlink.getPid());

				result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
			}
		}
	}

	private boolean isConnect(RdBranch branch, int nodePid) throws Exception {

		RdLinkSelector rdLinkSelector = new RdLinkSelector(this.conn);

		List<Integer> linkPids = new ArrayList<Integer>();

		for (IRow rowVia : branch.getVias()) {

			RdBranchVia via = (RdBranchVia) rowVia;

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

	/**
	 * 打断link维护
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

		RdBranchSelector selector = new RdBranchSelector(conn);

		List<RdBranch> branchs = selector.loadByLinkPid(oldLink.getPid(), 1,
				true);

		for (RdBranch branch : branchs) {

			if (branch.getInLinkPid() == oldLink.getPid()) {

				breakInLink(branch, newLinks, result);
			}
		}

		branchs = selector.loadByLinkPid(oldLink.getPid(), 2, true);

		for (RdBranch branch : branchs) {

			if (branch.getInLinkPid() == oldLink.getPid()) {

				breakInLink(branch, newLinks, result);
			}

			if (branch.getOutLinkPid() == oldLink.getPid()) {

				breakOutLink(branch, oldLink, newLinks, result);
			}
		}

		branchs = selector.loadByLinkPid(oldLink.getPid(), 3, true);

		for (RdBranch branch : branchs) {

			breakPassLink(branch, oldLink, newLinks, result);
		}

	}

	/**
	 * 处理link为进入线
	 * 
	 * @param branch
	 * @param newLinks
	 * @param result
	 */
	private void breakInLink(RdBranch branch, List<RdLink> newLinks,
			Result result) {

		for (RdLink link : newLinks) {

			if (branch.getNodePid() == link.getsNodePid()
					|| branch.getNodePid() == link.geteNodePid()) {

				branch.changedFields().put("inLinkPid", link.getPid());

				result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
			}
		}
	}

	/**
	 * 处理link为退出线
	 * 
	 * @param branch
	 * @param oldLink
	 * @param newLinks
	 * @param result
	 * @throws Exception
	 */
	private void breakOutLink(RdBranch branch, RdLink oldLink,
			List<RdLink> newLinks, Result result) throws Exception {

		int connectionNodePid = 0;

		// 无经过线进入点为连接点；有经过线最后一个经过线与退出线的连接点为连接点
		if (branch.getVias().size() == 0) {

			connectionNodePid = branch.getNodePid();

		} else {

			// 任意经过线组的最后一个经过线
			RdBranchVia lastVia = (RdBranchVia) branch.getVias().get(0);

			for (IRow rowVia : branch.getVias()) {

				RdBranchVia via = (RdBranchVia) rowVia;

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

				branch.changedFields().put("outLinkPid", link.getPid());

				result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
				break;
			}
		}

	}

	/**
	 * 处理link为经过线
	 * 
	 * @param branch
	 * @param oldLink
	 * @param newLinks
	 * @param result
	 * @throws Exception
	 */
	private void breakPassLink(RdBranch branch, RdLink oldLink,
			List<RdLink> newLinks, Result result) throws Exception {

		if (branch.getVias().size() == 0) {
			return;
		}

		// 对经过线分组
		Map<Integer, List<RdBranchVia>> viaGroupId = new HashMap<Integer, List<RdBranchVia>>();

		for (IRow row : branch.getVias()) {
			RdBranchVia via = (RdBranchVia) row;

			if (viaGroupId.get(via.getGroupId()) == null) {
				viaGroupId.put(via.getGroupId(), new ArrayList<RdBranchVia>());
			}

			viaGroupId.get(via.getGroupId()).add(via);
		}

		// 分组处理经过线
		for (int key : viaGroupId.keySet()) {

			// 经过线组
			List<RdBranchVia> viaGroup = viaGroupId.get(key);

			RdBranchVia oldVia = null;

			for (RdBranchVia via : viaGroup) {

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

				connectionNodePid = branch.getNodePid();

			} else {

				int preLinkPid = 0;

				for (RdBranchVia via : viaGroup) {

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

					RdBranchVia newVia = new RdBranchVia();

					newVia.setBranchPid(oldVia.getBranchPid());

					newVia.setGroupId(oldVia.getGroupId());

					newVia.setLinkPid(newLinks.get(i).getPid());

					newVia.setSeqNum(oldVia.getSeqNum() + i);

					result.insertObject(newVia, ObjStatus.INSERT,
							newVia.getBranchPid());
				}

			} else {

				for (int i = newLinks.size(); i > 0; i--) {
					RdBranchVia newVia = new RdBranchVia();

					newVia.setBranchPid(oldVia.getBranchPid());

					newVia.setGroupId(oldVia.getGroupId());

					newVia.setLinkPid(newLinks.get(i - 1).getPid());

					newVia.setSeqNum(oldVia.getSeqNum() + newLinks.size() - i);

					result.insertObject(newVia, ObjStatus.INSERT,
							newVia.getBranchPid());
				}
			}

			result.insertObject(oldVia, ObjStatus.DELETE, oldVia.getBranchPid());

			// 处理后续经过线序号
			for (RdBranchVia via : viaGroup) {

				if (via.getSeqNum() > oldVia.getSeqNum()) {

					via.changedFields().put("seqNum",
							via.getSeqNum() + newLinks.size() - 1);

					result.insertObject(via, ObjStatus.UPDATE,
							via.getBranchPid());
				}
			}
		}
	}

}
