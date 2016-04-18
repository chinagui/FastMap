package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.create;

import java.sql.Connection;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.commons.util.MeshUtils;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

import net.sf.json.JSONObject;

/**
 * 
* @Title: Operation.java 
* @Description: 新增行政区划代表点操作类
* @author 张小龙   
* @date 2016年4月18日 下午2:31:50 
* @version V1.0
 */
public class Operation implements IOperation {

	private Command command;

	private Connection conn;
	
	public Operation(Command command,Connection conn) {
		this.command = command;
		
		this.conn = conn;

	}
	
	@Override
	public String run(Result result) throws Exception {
		
		//根据经纬度计算图幅ID
		String meshId = MeshUtils.lonlat2Mesh(command.getLongitude(), command.getLatitude());
		
		String msg = null;
		
		AdAdmin adAdmin = new AdAdmin();
		
		if(meshId != null)
		{
			adAdmin.setMesh(Integer.parseInt(meshId));
		}
		
		adAdmin.setPid(PidService.getInstance().applyAdAdminPid());
		
		result.setPrimaryPid(adAdmin.getPid());
		
		//构造几何对象
		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { command.getLongitude(),
				command.getLatitude() });
		
		adAdmin.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
		
		result.insertObject(adAdmin, ObjStatus.INSERT, adAdmin.pid());
		
		return msg;
	}
	
}
