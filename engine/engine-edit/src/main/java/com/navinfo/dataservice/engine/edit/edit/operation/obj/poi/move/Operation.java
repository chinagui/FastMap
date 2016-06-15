package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.move;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @Title: Operation.java
 * @Description: 移动POI操作类
 * @author 赵凯凯
 * @date 2016年6月15日 下午2:36:15
 * @version V1.0
 */
public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;
	}

	@Override
	public String run(Result result) throws Exception {

		result.setPrimaryPid(this.command.getPid());

		this.updatePoi(result);

		return null;
	}

	private void updatePoi(Result result) throws Exception {
		
		JSONObject geojson = new JSONObject();

		geojson.put("type", "Point");

		geojson.put("coordinates", new double[] { command.getLongitude(), command.getLatitude() });
		
		JSONObject updateContent = new JSONObject();
		
		// 根据经纬度计算图幅ID
		String meshIds[] = CompGeometryUtil.geo2MeshesWithoutBreak(GeoTranslator.geojson2Jts(geojson, 1, 5));

		if (meshIds.length > 1) {
			throw new Exception("不能在图幅线上创建POI");
		}
		if (meshIds.length == 1) {
			updateContent.put("meshId", Integer.parseInt(meshIds[0]));
		}
		
		updateContent.put("geometry", geojson);

		updateContent.put("linkPid", command.getLinkPid());
		
		updateContent.put("xGuide", command.getXguide());
		updateContent.put("yGuide", command.getYguide());

		this.command.getIxPoi().fillChangeFields(updateContent);

		result.insertObject(this.command.getIxPoi(), ObjStatus.UPDATE, this.command.getPid());
	}
}
