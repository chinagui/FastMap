package com.navinfo.dataservice.engine.edit.operation.obj.cmg.building.create;

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

	/**
	 * 初始化新增操作类
	 * 
	 * @param command
	 *            参数
	 */
	public Operation(Command command, Connection conn) {
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {

		createBuilding(result);

		return null;
	}

	/**
	 * 创建 建筑物要素
	 * 
	 * @param result
	 * @throws Exception
	 */
	private void createBuilding(Result result) throws Exception {
		
		CmgBuilding cmgBuilding = new CmgBuilding();

		cmgBuilding.setPid(PidUtil.getInstance().applyCmgBuildingPid());

		cmgBuilding.setKind(command.getKind());

		result.insertObject(cmgBuilding, ObjStatus.INSERT, cmgBuilding.pid());
		
		updateCmgFace( result,  cmgBuilding.getPid());
	}

	/**
	 * 更新关联cmgface
	 * 
	 * @param result
	 */
	private void updateCmgFace(Result result, int buildingPid) throws Exception {

		if (command.getFacePids().size() < 1) {
			return;
		}

		CmgBuildfaceSelector selector = new CmgBuildfaceSelector(this.conn);

		List<IRow> faceRows = selector.loadByIds(command.getFacePids(), true,
				false);

		JSONObject updateContent = new JSONObject();

		updateContent.put("buildingPid", buildingPid);

		for (IRow faceRow : faceRows) {

			CmgBuildface face = (CmgBuildface) faceRow;

			boolean changed = face.fillChangeFields(updateContent);

			if (changed) {
				
				result.insertObject(face, ObjStatus.UPDATE, face.getPid());
			}
		}
	}
}
