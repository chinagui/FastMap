package com.navinfo.dataservice.control.row.batch.util;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

import net.sf.json.JSONObject;

public interface IBatch {

	public JSONObject run(IxPoi poi, Connection conn, JSONObject json) throws Exception;
}
