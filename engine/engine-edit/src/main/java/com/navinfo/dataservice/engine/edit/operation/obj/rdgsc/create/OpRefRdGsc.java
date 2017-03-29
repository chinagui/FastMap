package com.navinfo.dataservice.engine.edit.operation.obj.rdgsc.create;

import java.sql.Connection;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;

public class OpRefRdGsc {
	
	private Connection conn;
	
	public OpRefRdGsc(Connection conn) {
		this.conn = conn;
	}
	/**
	 * 处理组成立交的rdlink关联的同一线其他组成link几何
	 * 
	 * @param link
	 * @param newLinkGeoJson
	 * @param dbId
	 * @param result
	 * @throws Exception
	 */
	public void handleSameLink(RdLink link, JSONObject newLinkGeoJson,
			int dbId, Result result) throws Exception {
		
		com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation operation = new com.navinfo.dataservice.engine.edit.operation.obj.rdsamelink.update.Operation(
				this.conn);

		operation.createGscForSamelink(link, newLinkGeoJson, dbId, result);
	}
}
