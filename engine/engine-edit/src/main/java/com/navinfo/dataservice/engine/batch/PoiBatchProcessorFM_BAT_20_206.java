package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_206 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			String kindcode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			if ((!kindcode.equals("230218")&&!kindcode.equals("230227"))||uRecord==2) {
				return result;
			}
			if (kindcode.equals("230218")) {
				JSONArray dataArray = new JSONArray();
				IxPoiChargingStation poiChargingStation = (IxPoiChargingStation) poi.getChargingstations().get(0);
				poiChargingStation.setOpenHour(ExcelReader.h2f(poiChargingStation.getOpenHour()));
				poiChargingStation.setParkingInfo(ExcelReader.h2f(poiChargingStation.getParkingInfo()));
				JSONObject changeFields = poiChargingStation.Serialize(null);
				changeFields.put("objStatus", ObjStatus.UPDATE.toString());
				changeFields.remove("uDate");
				dataArray.add(changeFields);
				result.put("chargingstations", dataArray);
			} else if (kindcode.equals("230227")) {
				JSONArray dataArray = new JSONArray();
				List<IRow> poiChargingPlots = poi.getChargingplots();
				for (IRow plot:poiChargingPlots) {
					IxPoiChargingPlot poiChargingPlot = (IxPoiChargingPlot) plot;
					poiChargingPlot.setPower(ExcelReader.h2f(poiChargingPlot.getPower()));
					poiChargingPlot.setVoltage(ExcelReader.h2f(poiChargingPlot.getVoltage()));
					poiChargingPlot.setCurrent(ExcelReader.h2f(poiChargingPlot.getCurrent()));
					poiChargingPlot.setPrices(ExcelReader.h2f(poiChargingPlot.getPrices()));
					poiChargingPlot.setParkingNum(ExcelReader.h2f(poiChargingPlot.getParkingNum()));
					poiChargingPlot.setManufacturer(ExcelReader.h2f(poiChargingPlot.getManufacturer()));
					poiChargingPlot.setPlotNum(ExcelReader.h2f(poiChargingPlot.getPlotNum()));
					poiChargingPlot.setFactoryNum(ExcelReader.h2f(poiChargingPlot.getFactoryNum()));
					poiChargingPlot.setProductNum(ExcelReader.h2f(poiChargingPlot.getProductNum()));
					JSONObject changeFields = poiChargingPlot.Serialize(null);
					changeFields.put("objStatus", ObjStatus.UPDATE.toString());
					changeFields.remove("uDate");
					dataArray.add(changeFields);
				}
				result.put("chargingplots", dataArray);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
		
	}

}
