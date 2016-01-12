package com.navinfo.dataservice.FosEngine.edit.operation.obj.rdspeedlimit.create;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.FosEngine.edit.model.ObjStatus;
import com.navinfo.dataservice.FosEngine.edit.model.Result;
import com.navinfo.dataservice.FosEngine.edit.model.bean.rd.speedlimit.RdSpeedlimit;
import com.navinfo.dataservice.FosEngine.edit.operation.IOperation;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.service.PidService;
import com.vividsolutions.jts.geom.Geometry;

public class Operation implements IOperation {

	private Command command;

	public Operation(Command command) {
		this.command = command;

	}
	
	@Override
	public String run(Result result) throws Exception {
		
		String msg = null;
		
		RdSpeedlimit limit = new RdSpeedlimit();
		
		limit.setPid(PidService.getInstance().applySpeedLimitPid());
		
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
