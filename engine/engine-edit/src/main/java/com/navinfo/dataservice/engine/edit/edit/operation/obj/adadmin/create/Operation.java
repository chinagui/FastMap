package com.navinfo.dataservice.engine.edit.edit.operation.obj.adadmin.create;

import java.sql.Connection;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.ad.zone.AdAdmin;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

import net.sf.json.JSONObject;

public class Operation implements IOperation {

	private Command command;

	private Connection conn;
	public Operation(Command command,Connection conn) {
		this.command = command;
		
		this.conn = conn;

	}
	
	@Override
	public String run(Result result) throws Exception {
		
		int meshId = new RdLinkSelector(conn).loadById(command.getLinkPid(), true).mesh();
		
		String msg = null;
		
		AdAdmin adAdmin = new AdAdmin();
		
		adAdmin.setMesh(meshId);
		
		adAdmin.setMeshId(meshId);
		
		adAdmin.setPid(PidService.getInstance().applyAdAdminPid());
		
		result.setPrimaryPid(adAdmin.getPid());
		
		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { command.getLongitude(),
				command.getLatitude() });
		
		adAdmin.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
		
		//默认为不可编辑
		adAdmin.setEditFlag(0);
		
		
		result.insertObject(adAdmin, ObjStatus.INSERT, adAdmin.pid());
		
		return msg;
	}
	
}
