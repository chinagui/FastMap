package com.navinfo.dataservice.engine.edit.operation.obj.rdeleceye.create;

import java.sql.Connection;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.dao.glm.iface.IOperation;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.eleceye.RdElectroniceye;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.dataservice.dao.pidservice.PidService;

import net.sf.json.JSONObject;

public class Operation implements IOperation {
	
	private Command command;
	
	private Connection conn;
	
	public Operation(Command command, Connection conn){
		this.command = command;
		this.conn = conn;
	}

	@Override
	public String run(Result result) throws Exception {
		String resultMsg = null;
		
		RdElectroniceye eleceye = new RdElectroniceye();
		
		eleceye.setPid(PidService.getInstance().applyElectroniceyePid());
		
		eleceye.setLinkPid(command.getLinkPid());
		
		eleceye.setDirect(command.getDirect());
		
		JSONObject geoPoint = new JSONObject();

		geoPoint.put("type", "Point");

		geoPoint.put("coordinates", new double[] { command.getLongitude(),
				command.getLatitude() });
		
		eleceye.setGeometry(GeoTranslator.geojson2Jts(geoPoint, 100000, 0));
		
		
		int meshId = new RdLinkSelector(conn).loadById(command.getLinkPid(), false).mesh();
		eleceye.setMeshId(meshId);
		
		eleceye.setMesh(meshId);
		
		result.insertObject(eleceye, ObjStatus.INSERT, eleceye.parentPKValue());
		
		return resultMsg;
	}

}
