package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.dao.glm.operator.poi.deep.IxPoiParkingOperator;
import com.navinfo.dataservice.dao.pidservice.PidService;
import com.navinfo.dataservice.impcore.exception.DataErrorException;

public class ParkingImporter {
	
	public static int run(Result result, Connection conn, Statement stmt,
			JSONObject poi) throws Exception {

		JSONObject parkings = poi.getJSONObject("parkings");

		if (JSONUtils.isNull(parkings)) {
			return 0;
		}

		IxPoiParking parking = new IxPoiParking();

		int pid = poi.getInt("pid");

		parking.setPoiPid(pid);

		parking.setPid(PidService.getInstance().applyPoiParkingsId());

		parking.setParkingType(JsonUtils.getString(parkings, "buildingType"));

		if (parking.getParkingType() != null
				&& parking.getParkingType().length() > 10) {
			throw new DataErrorException("parkingType length too long");
		}

		parking.setTollStd(JsonUtils.getString(parkings, "tollStd"));

		if (parking.getTollStd() != null && parking.getTollStd().length() > 20) {
			throw new DataErrorException("tollStd length too long");
		}

		parking.setTollDes(JsonUtils.getString(parkings, "tollDes"));

		if (parking.getTollDes() != null && parking.getTollDes().length() > 254) {
			throw new DataErrorException("tollDes length too long");
		}

		parking.setTollWay(JsonUtils.getString(parkings, "tollWay"));

		if (parking.getTollWay() != null && parking.getTollWay().length() > 20) {
			throw new DataErrorException("tollWay length too long");
		}

		parking.setPayment(JsonUtils.getString(parkings, "payment"));

		if (parking.getPayment() != null && parking.getPayment().length() > 20) {
			throw new DataErrorException("payment length too long");
		}

		parking.setRemark(JsonUtils.getString(parkings, "remark"));

		if (parking.getRemark() != null && parking.getRemark().length() > 30) {
			throw new DataErrorException("remark length too long");
		}

		parking.setOpenTiime(JsonUtils.getString(parkings, "openTime"));

		if (parking.getOpenTiime() != null
				&& parking.getOpenTiime().length() > 254) {
			throw new DataErrorException("openTime length too long");
		}

		parking.setTotalNum(parkings.getInt("totalNum"));

		parking.setResHigh(parkings.getDouble("resHigh"));

		parking.setResWidth(parkings.getDouble("resWidth"));

		parking.setResWeigh(parkings.getDouble("resWeigh"));

		parking.setVehicle(parkings.getInt("vehicle"));

		parking.setHaveSpecialplace(JsonUtils.getString(parkings,
				"haveSpecialPlace"));

		if (parking.getHaveSpecialplace() != null
				&& parking.getHaveSpecialplace().length() > 4) {
			throw new DataErrorException("haveSpecialPlace length too long");
		}

		parking.setWomenNum(parkings.getInt("womenNum"));

		parking.setHandicapNum(parkings.getInt("handicapNum"));

		parking.setMiniNum(parkings.getInt("miniNum"));

		parking.setVipNum(parkings.getInt("vipNum"));

		IxPoiParkingOperator operator = new IxPoiParkingOperator(conn, parking);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
