package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.update;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding3dicon;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding3dmodel;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildingName;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildingPoi;

public class Operation implements IOperation {

	/**
	 * 参数
	 */
	private Command command;

	public Operation(Command command) {

		this.command = command;
	}

	/**
	 * 执行操作
	 * 
	 * @param result
	 *            操作结果
	 * @return 操作后的对象
	 * @throws Exception
	 */
	@Override
	public String run(Result result) throws Exception {

		update(result);

		return null;
	}

	private void update(Result result) throws Exception {

		JSONObject content = command.getContent();

		// 更新主表
		if (content.containsKey("objStatus")) {

			if (ObjStatus.UPDATE.toString().equals(
					content.getString("objStatus"))) {

				boolean isChanged = command.getBuilding().fillChangeFields(
						content);

				if (isChanged) {

					result.insertObject(command.getBuilding(),
							ObjStatus.UPDATE, command.getBuilding().pid());
				}
			}
		}
		
		updateBuildingPoi(result, content, command.getBuilding());
		
		updateBuildingName(result, content, command.getBuilding());
		
		updateBuilding3DICON(result, content, command.getBuilding());
		
		updateBuilding3DModel(result, content, command.getBuilding());
	}

	/**
	 * 更新建筑物与 POI 关系表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateBuildingPoi(Result result, JSONObject content,
			CmgBuilding building) throws Exception {
		if (!content.containsKey("pois")) {
			return;
		}

		JSONArray pois = content.getJSONArray("pois");

		for (int i = 0; i < pois.size(); i++) {

			JSONObject json = pois.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				CmgBuildingPoi poi = building.poiMap.get(json
						.getString("rowId"));

				if (poi == null) {
					throw new Exception("ROWID=" + json.getString("rowId")
							+ "的CmgBuildingPoi不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {
					result.insertObject(poi, ObjStatus.DELETE,
							building.getPid());
					continue;
				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = poi.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(poi, ObjStatus.UPDATE,
								building.pid());
					}
				}
			} else {
				CmgBuildingPoi poi = new CmgBuildingPoi();

				poi.Unserialize(json);

				poi.setBuildingPid(building.getPid());

				poi.setMesh(building.mesh());

				result.insertObject(poi, ObjStatus.INSERT, building.pid());
			}
		}
	}

	/**
	 * 更新 建筑物名称表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateBuildingName(Result result, JSONObject content,
			CmgBuilding building) throws Exception {
		if (!content.containsKey("names")) {
			return;
		}

		JSONArray names = content.getJSONArray("names");

		for (int i = 0; i < names.size(); i++) {

			JSONObject json = names.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				CmgBuildingName name = building.nameMap.get(json
						.getString("rowId"));

				if (name == null) {
					throw new Exception("ROWID=" + json.getString("rowId")
							+ "的CmgBuildingName不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {
					result.insertObject(name, ObjStatus.DELETE,
							building.getPid());
					continue;
				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = name.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(name, ObjStatus.UPDATE,
								building.pid());
					}
				}
			} else {
				CmgBuildingName name = new CmgBuildingName();

				name.Unserialize(json);

				name.setBuildingPid(building.getPid());

				name.setMesh(building.mesh());

				name.setPid(PidUtil.getInstance().applyCmgBuildingNamePid());

				result.insertObject(name, ObjStatus.INSERT, building.pid());
			}
		}
	}

	/**
	 * 更新 建筑物的 3DLandMark 图标表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateBuilding3DICON(Result result, JSONObject content,
			CmgBuilding building) throws Exception {
		if (!content.containsKey("build3dicons")) {
			return;
		}

		JSONArray build3dicons = content.getJSONArray("build3dicons");

		for (int i = 0; i < build3dicons.size(); i++) {

			JSONObject json = build3dicons.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				CmgBuilding3dicon build3dicon = building.build3diconMap
						.get(json.getString("rowId"));

				if (build3dicon == null) {
					throw new Exception("ROWID=" + json.getString("rowId")
							+ "的CmgBuilding3dicon不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {

					result.insertObject(build3dicon, ObjStatus.DELETE,
							building.getPid());
					continue;

				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = build3dicon.fillChangeFields(json);

					if (isChanged) {
						result.insertObject(build3dicon, ObjStatus.UPDATE,
								building.pid());
					}
				}
			} else {
				CmgBuilding3dicon build3dicon = new CmgBuilding3dicon();

				build3dicon.Unserialize(json);

				build3dicon.setBuildingPid(building.getPid());

				build3dicon.setMesh(building.mesh());

				result.insertObject(build3dicon, ObjStatus.INSERT,
						building.pid());
			}
		}
	}

	/**
	 * 更新 建筑物的 3DLandMark 模型表
	 * 
	 * @param result
	 * @param content
	 * @throws Exception
	 */
	private void updateBuilding3DModel(Result result, JSONObject content,
			CmgBuilding building) throws Exception {

		if (!content.containsKey("build3dmodels")) {
			return;
		}

		JSONArray build3dmodels = content.getJSONArray("build3dmodels");

		for (int i = 0; i < build3dmodels.size(); i++) {

			JSONObject json = build3dmodels.getJSONObject(i);

			if (!json.containsKey("objStatus")) {
				continue;
			}

			if (!ObjStatus.INSERT.toString()
					.equals(json.getString("objStatus"))) {

				CmgBuilding3dmodel build3dmodel = building.build3dmodelMap
						.get(json.getString("rowId"));

				if (build3dmodel == null) {

					throw new Exception("ROWID=" + json.getString("rowId")
							+ "的CmgBuilding3dmodel不存在");
				}

				if (ObjStatus.DELETE.toString().equals(
						json.getString("objStatus"))) {

					result.insertObject(build3dmodel, ObjStatus.DELETE,
							building.getPid());

					continue;

				} else if (ObjStatus.UPDATE.toString().equals(
						json.getString("objStatus"))) {

					boolean isChanged = build3dmodel.fillChangeFields(json);

					if (isChanged) {

						result.insertObject(build3dmodel, ObjStatus.UPDATE,
								building.pid());
					}
				}
			} else {
				CmgBuilding3dmodel build3dmodel = new CmgBuilding3dmodel();

				build3dmodel.Unserialize(json);

				build3dmodel.setBuildingPid(building.getPid());

				build3dmodel.setMesh(building.mesh());

				build3dmodel.setPid(PidUtil.getInstance().applyCmgBuilding3dmodelPid());

				result.insertObject(build3dmodel, ObjStatus.INSERT,
						building.pid());
			}
		}
	}
}
