package com.navinfo.dataservice.engine.edit.operation.obj.adadmin.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.geo.AdAdmin;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @Title: Operation.java
 * @Description: 移动行政区划代表点操作类
 * @author 张小龙
 * @date 2016年4月18日 下午2:36:15
 * @version V1.0
 */
public class Operation implements IOperation {

	private Command command;

	private AdAdmin moveAdmin;

	public Operation(Command command, AdAdmin moveAdmin) {
		this.command = command;

		this.moveAdmin = moveAdmin;
	}

	@Override
	public String run(Result result) throws Exception {

		result.setPrimaryPid(moveAdmin.getPid());

		this.updateAdminGeometry(result);

		return null;
	}

	private void updateAdminGeometry(Result result) throws Exception {
		
		JSONObject geojson = new JSONObject();

		geojson.put("type", "Point");

		geojson.put("coordinates", new double[] { command.getLongitude(), command.getLatitude() });
		
		JSONObject updateContent = new JSONObject();
		
		// 根据经纬度计算图幅ID
		String meshIds[] = CompGeometryUtil.geo2MeshesWithoutBreak(GeoTranslator.geojson2Jts(geojson, 1, 5));

		if (meshIds.length > 1) {
			throw new Exception("不能在图幅线上创建行政区划代表点");
		}
		if (meshIds.length == 1) {
			updateContent.put("meshId", Integer.parseInt(meshIds[0]));
		}
		
		updateContent.put("geometry", geojson);

		updateContent.put("linkPid", command.getLinkPid());

		moveAdmin.fillChangeFields(updateContent);

		result.insertObject(moveAdmin, ObjStatus.UPDATE, moveAdmin.pid());
	}
}
