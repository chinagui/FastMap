package com.navinfo.dataservice.control.row.batch;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

import net.sf.json.JSONObject;

public abstract class IBatch {

	
	public abstract JSONObject run(IxPoi poi)  throws Exception  ;
}
