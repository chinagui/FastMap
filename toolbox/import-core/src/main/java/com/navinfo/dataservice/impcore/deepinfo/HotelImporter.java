package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;

import org.springframework.util.StringUtils;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiHotel;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiRestaurant;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import com.navinfo.dataservice.impcore.exception.DataErrorException;

import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class HotelImporter {
	public static int run(Connection conn,
			Statement stmt, JSONObject poi) throws Exception {

		JSONObject hotObj = poi.getJSONObject("hotel");

		if (JSONUtils.isNull(hotObj)) {
			return 0;
		}

		IxPoiHotel hot = new IxPoiHotel();
		
		hot.setPid(PidUtil.getInstance().applyPoiHotelId());

		hot.setPoiPid(poi.getInt("pid"));
		
		hot.setCreditCard(JsonUtils.getString(hotObj, "creditCards"));
		
		hot.setRating(JsonUtils.getInt(hotObj, "rating"));
		
		String inTime = JsonUtils.getString(hotObj, "checkInTime");
		if(StringUtils.isEmpty(inTime)){
			hot.setCheckinTime("14:00");
		}else{
			hot.setCheckinTime(inTime);
		}
		String outTime = JsonUtils.getString(hotObj,"checkOutTime");
		if(StringUtils.isEmpty(outTime)){
			hot.setCheckoutTime("12:00");
		}else{
			hot.setCheckoutTime(outTime);
		}
		
		hot.setRoomCount(JsonUtils.getInt(hotObj, "roomCount"));
		
		hot.setRoomType(JsonUtils.getString(hotObj, "roomType"));
		
		hot.setRoomPrice(JsonUtils.getString(hotObj, "roomPrice"));
		
		hot.setBreakfast(JsonUtils.getInt(hotObj, "breakfast"));
		
		hot.setService(JsonUtils.getString(hotObj, "service"));
		
		hot.setParking(JsonUtils.getInt(hotObj, "parking"));
		
		hot.setLongDescription(JsonUtils.getString(hotObj, "description"));
		
		hot.setOpenHour(JsonUtils.getString(hotObj, "openHour"));

		BasicOperator operator = new BasicOperator(conn,
				hot);

		operator.insertRow2Sql(stmt);

		return 1;
	}
}
