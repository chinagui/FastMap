package com.navinfo.dataservice.impcore.charge;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;

import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;

import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class CharingPlotImporter {
	public static String[] kcs = new String[] { "230227" };

	public static int run(Connection conn, Statement stmt, JSONObject poi)
			throws Exception {

		JSONObject plotObj = poi.getJSONObject("chargingPole");

		if (JSONUtils.isNull(plotObj)) {
			return 0;
		}
		Set<String> kcSets = new HashSet<String>();
		CollectionUtils.addAll(kcSets, kcs);
		String kindCode = poi.getString("kindCode");
		if (!kcSets.contains(kindCode)) {
			return 0;
		}

		IxPoiChargingPlot chargingPlot = new IxPoiChargingPlot();

		chargingPlot.setPoiPid(poi.getInt("pid"));
		chargingPlot.setGroupId(JsonUtils.getInt(plotObj, "groupId"));
		chargingPlot.setCount(JsonUtils.getInt(plotObj, "count"));
		chargingPlot.setAcdc(JsonUtils.getInt(plotObj, "acdc"));
		chargingPlot.setPlugType(JsonUtils.getString(plotObj, "plugType"));
		chargingPlot.setPower(JsonUtils.getString(plotObj, "power"));
		chargingPlot.setVoltage(JsonUtils.getString(plotObj, "voltage"));
		chargingPlot.setCurrent(JsonUtils.getString(plotObj, "current"));
		chargingPlot.setMode(JsonUtils.getInt(plotObj, "mode"));
		chargingPlot.setPlugNum(JsonUtils.getInt(plotObj, "plugNum"));
		chargingPlot.setPrices(JsonUtils.getString(plotObj, "prices"));
		chargingPlot.setOpenType(JsonUtils.getString(plotObj, "openType"));
		chargingPlot.setAvailableState(JsonUtils.getInt(plotObj,
				"availableState"));
		chargingPlot.setManufacturer(JsonUtils.getString(plotObj,
				"manufacturer"));
		chargingPlot.setFactoryNum(JsonUtils.getString(plotObj, "factoryNum"));
		chargingPlot.setPlotNum(JsonUtils.getString(plotObj, "plotNum"));
		chargingPlot.setProductNum(JsonUtils.getString(plotObj, "productNum"));
		chargingPlot.setParkingNum(JsonUtils.getString(plotObj, "parkingNum"));
		chargingPlot.setFloor(JsonUtils.getInt(plotObj, "floor"));
		chargingPlot.setLocationType(JsonUtils.getInt(plotObj, "locationType"));

		chargingPlot.setPayment(JsonUtils.getString(plotObj, "payment"));

		BasicOperator operator = new BasicOperator(conn, chargingPlot);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
