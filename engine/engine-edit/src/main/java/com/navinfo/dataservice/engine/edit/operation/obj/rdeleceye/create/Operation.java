package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create;

import org.json.JSONException;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.navicommons.geo.computation.MeshUtils;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {
		String resultMsg = null;

		createRdElectroniceye(result);

		return resultMsg;
	}

	/*
	 * 创建电子眼信息并保存
	 */
	public void createRdElectroniceye(Result result) throws Exception, JSONException {
		RdElectroniceye eleceye = new RdElectroniceye();

		eleceye.setPid(PidUtil.getInstance().applyElectroniceyePid());

		eleceye.setLinkPid(command.getLinkPid());

		eleceye.setDirect(command.getDirect());

		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { command.getLongitude(), command.getLatitude() });

		eleceye.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));

		String[] meshes = MeshUtils.point2Meshes(command.getLongitude(), command.getLatitude());

		eleceye.setMesh(Integer.valueOf(meshes[0]));

		result.insertObject(eleceye, ObjStatus.INSERT, eleceye.pid());
	}

}
