package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdspeedlimit.create;

import java.sql.Connection;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.model.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;

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
		
		result.insertObject(limit, ObjStatus.INSERT);
		
		return msg;
	}
	
}
