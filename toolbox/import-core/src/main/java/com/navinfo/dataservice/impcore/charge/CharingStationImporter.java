package com.navinfo.dataservice.impcore.charge;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;

import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;

import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class CharingStationImporter {
	public static String[] kcs = new String[] { "230218" };

	public static int run(Connection conn, Statement stmt, JSONObject poi)
			throws Exception {

		JSONObject stationObj = poi.getJSONObject("chargingStation");

		if (JSONUtils.isNull(stationObj)) {
			return 0;
		}
		Set<String> kcSets = new HashSet<String>();
		CollectionUtils.addAll(kcSets, kcs);
		String kindCode = poi.getString("kindCode");
		if (!kcSets.contains(kindCode)) {
			return 0;
		}

		IxPoiChargingStation station = new IxPoiChargingStation();

		station.setPid(PidUtil.getInstance().applyPoiChargingstationId());

		station.setPoiPid(poi.getInt("pid"));
		station.setChargingType(JsonUtils.getInt(stationObj, "type"));

		station.setChangeBrands(JsonUtils.getString(stationObj, "changeBrands"));

		station.setChangeOpenType(JsonUtils.getString(stationObj,
				"changeOpenType"));
		station.setChargingNum(JsonUtils.getInt(stationObj, "chargingNum"));
		station.setServiceProv(JsonUtils.getString(stationObj, "servicePro"));
		station.setParkingInfo(JsonUtils.getString(stationObj, "parkingInfo"));
		station.setOpenHour(JsonUtils.getString(stationObj, "openHour"));
		station.setParkingFees(JsonUtils.getInt(stationObj, "parkingFees"));
		station.setAvailableState(JsonUtils
				.getInt(stationObj, "availableState"));

		BasicOperator operator = new BasicOperator(conn, station);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
