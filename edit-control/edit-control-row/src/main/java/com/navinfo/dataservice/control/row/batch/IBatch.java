package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

import net.sf.json.JSONObject;

public abstract class IBatch {

	
	public abstract JSONObject run(IxPoi poi,Connection conn,JSONObject json)  throws Exception  ;
}
