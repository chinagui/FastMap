package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import com.navinfo.dataservice.impcore.exception.DataErrorException;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class GasStationImporter {
	public static String[] kcs = new String[]{"230215","230216","230217"}; 
	
	
	public static int run(Result result, Connection conn,
			Statement stmt, JSONObject poi) throws Exception {

		JSONObject gasStation = poi.getJSONObject("gasStation");

		if (JSONUtils.isNull(gasStation)) {
			return 0;
		}
		Set<String> kcSets = new HashSet<String>();
		CollectionUtils.addAll(kcSets, kcs);
		String kindCode = poi.getString("kindCode");
		if(!kcSets.contains(kindCode)){
			return 0;
		}

		IxPoiGasstation gas = new IxPoiGasstation();

		gas.setPid(PidUtil.getInstance().applyPoiGasstationId());

		gas.setPoiPid(poi.getInt("pid"));

		gas.setServiceProv(JsonUtils.getString(gasStation, "servicePro"));

		if (gas.getServiceProv() != null && gas.getServiceProv().length() > 2) {
			throw new DataErrorException("servicePro length too long");
		}

		gas.setFuelType(JsonUtils.getString(gasStation, "fuelType"));

		if (gas.getFuelType() != null && gas.getFuelType().length() > 50) {
			throw new DataErrorException("fuelType length too long");
		}

		gas.setOilType(JsonUtils.getString(gasStation, "oilType"));

		if (gas.getOilType() != null && gas.getOilType().length() > 50) {
			throw new DataErrorException("oilType length too long");
		}

		gas.setEgType(JsonUtils.getString(gasStation, "egType"));

		if (gas.getEgType() != null && gas.getEgType().length() > 50) {
			throw new DataErrorException("egType length too long");
		}

		gas.setMgType(JsonUtils.getString(gasStation, "mgType"));

		if (gas.getMgType() != null && gas.getMgType().length() > 50) {
			throw new DataErrorException("mgType length too long");
		}

		gas.setPayment(JsonUtils.getString(gasStation, "payment"));

		if (gas.getPayment() != null && gas.getPayment().length() > 50) {
			throw new DataErrorException("payment length too long");
		}

		gas.setService(JsonUtils.getString(gasStation, "service"));

		if (gas.getService() != null && gas.getService().length() > 20) {
			throw new DataErrorException("service length too long");
		}

		gas.setOpenHour(JsonUtils.getString(gasStation, "openHour"));

		if (gas.getService() != null && gas.getService().length() > 254) {
			throw new DataErrorException("openHour length too long");
		}

		BasicOperator operator = new BasicOperator(conn,
				gas);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
