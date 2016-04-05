package com.navinfo.dataservice.engine.edit.edit.operation.obj.rdspeedlimit.create;

import java.sql.Connection;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;

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
		
		RdSpeedlimit limit = new RdSpeedlimit();
		
		limit.setMesh(meshId);
		
		limit.setMeshId(meshId);
		
		limit.setPid(PidService.getInstance().applySpeedLimitPid());
		
		result.setPrimaryPid(limit.getPid());
		
		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { command.getLongitude(),
				command.getLatitude() });
		
		limit.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
		
		limit.setDirect(command.getDirect());
		
		limit.setLinkPid(command.getLinkPid());
		
		result.insertObject(limit, ObjStatus.INSERT, limit.pid());
		
		return msg;
	}
	
}
