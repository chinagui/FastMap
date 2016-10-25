package com.navinfo.dataservice.impcore.deepinfo;

import java.sql.Connection;
import java.sql.Statement;

import com.navinfo.dataservice.bizcommons.service.PidUtil;
import com.navinfo.dataservice.commons.util.JsonUtils;
import com.navinfo.dataservice.dao.glm.iface.Result;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.operator.BasicOperator;

import com.navinfo.dataservice.impcore.exception.DataErrorException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class BusinessTimeImporter {
	public static int run(Connection conn,
			Statement stmt, JSONObject poi) throws Exception {

		JSONArray btObjs = poi.getJSONArray("businessTime");

		if (JSONUtils.isNull(btObjs)) {
			return 0;
		}
		
		for(Object obj:btObjs){
			JSONObject btObj = (JSONObject)obj;

			IxPoiBusinessTime bt = new IxPoiBusinessTime();

			bt.setPoiPid(poi.getInt("pid"));
			
			bt.setMonSrt(JsonUtils.getString(btObj, "monStart"));
			
			bt.setMonEnd(JsonUtils.getString(btObj, "monEnd"));
			
			bt.setWeekInYearSrt(JsonUtils.getString(btObj, "weekStartYear"));
			bt.setWeekInYearEnd(JsonUtils.getString(btObj, "weekEndYear"));
			
			bt.setWeekInMonthSrt(JsonUtils.getString(btObj, "weekStartMonth"));
			bt.setWeekInMonthEnd(JsonUtils.getString(btObj, "weekEndMonth"));
			
			bt.setVaildWeek(JsonUtils.getString(btObj, "validWeek"));
			bt.setDaySrt(JsonUtils.getString(btObj, "dayStart"));
			bt.setDayEnd(JsonUtils.getString(btObj, "dayEnd"));
			bt.setTimeSrt(JsonUtils.getString(btObj, "timeStart"));
			bt.setTimeDue(JsonUtils.getString(btObj, "timeDuration"));
			

			BasicOperator operator = new BasicOperator(conn,
					bt);

			operator.insertRow2Sql(stmt);
		}

		return 1;
	}
}
