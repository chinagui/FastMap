package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.delete;

import java.sql.Connection;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuildface;
import com.navinfo.dataservice.dao.glm.model.cmg.CmgBuilding;
import com.navinfo.dataservice.dao.glm.selector.cmg.CmgBuildfaceSelector;

public class Operation implements IOperation {

	/**
	 * 参数
	 */
	private Command command;

	/**
	 * 数据库连接
	 */
	private Connection conn;

	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		deleteBuilding(result);

		return null;
	}

	/**
	 * 创建 建筑物要素
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void deleteBuilding(Result result) throws Exception {

		result.insertObject(command.getBuilding(), ObjStatus.DELETE, command
				.getBuilding().pid());

		updateCmgFace(result, command.getBuilding().pid());
	}

	/**
	 * 更新关联cmgface
	 * 
	 * @param result
	 */
	private void updateCmgFace(Result result ,int BuildingPid) throws Exception {

		CmgBuildfaceSelector selector = new CmgBuildfaceSelector(this.conn);

		List<CmgBuildface> faces = selector.loadFaceByBuildingPid(BuildingPid,true);
	
		JSONObject updateContent = new JSONObject();

		updateContent.put("buildingPid", 0);

		for (CmgBuildface face : faces) {

			boolean changed = face.fillChangeFields(updateContent);

			if (changed) {

				result.insertObject(face, ObjStatus.UPDATE, face.getPid());
			}
		}
	}
}
