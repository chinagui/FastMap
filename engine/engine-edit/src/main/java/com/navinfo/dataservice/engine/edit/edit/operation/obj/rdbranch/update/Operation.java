package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdbranch.update;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchDetail;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchName;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchRealimage;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdBranchSchematic;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSeriesbranch;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignasreal;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboard;
import com.navinfo.dataservice.dao.glm.model.rd.branch.RdSignboardName;
import com.navinfo.dataservice.dao.pidservice.PidService;

public class Operation implements IOperation {

	private Command command;

	private RdBranch branch;

	public Operation(Command command, RdBranch branch) {
		this.command = command;

		this.branch = branch;
	}

	@Override
	public String run(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 判断是否存在交限进入线
		if (content.containsKey("objStatus")) {

			if (ObjStatus.DELETE.toString().equals(
					content.getString("objStatus"))) {
				result.insertObject(branch, ObjStatus.DELETE, branch.pid());

				return null;
			} else {

				boolean isChanged = branch.fillChangeFields(content);

				if (isChanged) {
					result.insertObject(branch, ObjStatus.UPDATE, branch.pid());
				}
			}
		}

		// 更新分歧详细信息表
		UpdateBranchDetail(result, content);

		// 更新方向看板
		UpdateSignboard(result, content);

		// 更新实景看板
		UpdateSignasreal(result, content);

		// 更新大路口交叉点
		UpdateSchematic(result, content);

		// 更新分歧实景图
		UpdateBranchRealimage(result, content);

		// 更新连续分歧
		UpdateSeriesbranch(result, content);

		return null;
	}

	/**
	 * 更新分歧详细信息表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void UpdateBranchDetail(Result result, JSONObject content)
			throws Exception {
		if (content.containsKey("details")) {
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

						boolean isChanged = detail.fillChangeFields(json);

						if (isChanged) {
							result.insertObject(detail, ObjStatus.UPDATE,
									branch.pid());
						}
					}
				} else {
					RdBranchDetail detail = new RdBranchDetail();

					detail.Unserialize(json);

					detail.setPid(PidService.getInstance()
							.applyBranchDetailId());

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

						name.setPid(PidService.getInstance()
								.applyBranchNameId());

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
	private void UpdateSignboard(Result result, JSONObject content)
			throws Exception {
		if (content.containsKey("signboards")) {
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

					signboard.setPid(PidService.getInstance()
							.applyRdSignboard());

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

						name.setPid(PidService.getInstance()
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
	private void UpdateSignasreal(Result result, JSONObject content)
			throws Exception {
		if (content.containsKey("signasreals")) {
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

				signasreal.setPid(PidService.getInstance().applyRdSignasreal());

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
	private void UpdateSchematic(Result result, JSONObject content)
			throws Exception {
		if (content.containsKey("schematics")) {
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

				schematic.setPid(PidService.getInstance()
						.applyBranchSchematic());

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
	private void UpdateBranchRealimage(Result result, JSONObject content)
			throws Exception {
		if (content.containsKey("realimages")) {
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

	private void UpdateSeriesbranch(Result result, JSONObject content)
			throws Exception {
		if (content.containsKey("seriesbranches")) {
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
}
