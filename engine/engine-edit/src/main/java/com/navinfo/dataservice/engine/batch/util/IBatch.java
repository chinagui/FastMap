package com.navinfo.dataservice.engine.batch.util;

import java.sql.Connection;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONObject;

public interface IBatch {

	public JSONObject run(IxPoi poi, Connection conn, JSONObject json,EditApiImpl editApiImpl) throws Exception;
}
