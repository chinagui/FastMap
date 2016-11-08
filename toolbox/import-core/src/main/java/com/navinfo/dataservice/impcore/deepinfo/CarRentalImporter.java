package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiCarrental;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import com.navinfo.dataservice.impcore.exception.DataErrorException;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class CarRentalImporter {
	public static int run(Connection conn,
			Statement stmt, JSONObject poi) throws Exception {

		JSONObject crObj = poi.getJSONObject("rental");

		if (JSONUtils.isNull(crObj)) {
			return 0;
		}

		IxPoiCarrental cr = new IxPoiCarrental();
		

		cr.setPoiPid(poi.getInt("pid"));

		cr.setOpenHour(JsonUtils.getString(crObj, "openHour"));
		
		cr.setAddress(JsonUtils.getString(crObj, "adressDes"));
		
		cr.setHowToGo(JsonUtils.getString(crObj, "howToGo"));
		
		String kindCode = poi.getString("kindCode");

		if("200201".equals(kindCode)){
			cr.setWebSite(JsonUtils.getString(poi, "website"));
		}

		BasicOperator operator = new BasicOperator(conn,
				cr);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
