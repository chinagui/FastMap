package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import com.navinfo.dataservice.impcore.exception.DataErrorException;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class RestaurantImporter {
	public static int run(Result result, Connection conn,
			Statement stmt, JSONObject poi) throws Exception {

		JSONObject resObj = poi.getJSONObject("foodtypes");

		if (JSONUtils.isNull(resObj)) {
			return 0;
		}

		IxPoiRestaurant res = new IxPoiRestaurant();
		
		res.setPid(PidUtil.getInstance().applyPoiRestaurantId());

		res.setPoiPid(poi.getInt("pid"));
		
		res.setFoodType(JsonUtils.getString(resObj, "foodtype"));
		
		res.setCreditCard(JsonUtils.getString(resObj, "creditCards"));
		
		res.setAvgCost(JsonUtils.getInt(resObj, "avgCost"));
		
		res.setParking(JsonUtils.getInt(resObj, "parking"));
		
		res.setOpenHour(JsonUtils.getString(resObj, "openHour"));


		BasicOperator operator = new BasicOperator(conn,
				res);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
