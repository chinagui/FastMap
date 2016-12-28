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
			boolean ischanged = false;
			if ((!kindcode.equals("230218")&&!kindcode.equals("230227"))||uRecord==2) {
				return result;
			}
			if (kindcode.equals("230218")) {
				JSONArray dataArray = new JSONArray();
				if (poi.getChargingstations()==null||poi.getChargingstations().size()==0) {
					return result;
				}
				IxPoiChargingStation poiChargingStation = (IxPoiChargingStation) poi.getChargingstations().get(0);
				if (poi.getChargingstations().size()>0) {
					if (poiChargingStation.getOpenHour() != null) {
						poiChargingStation.setOpenHour(ExcelReader.f2h(poiChargingStation.getOpenHour()));
						ischanged = true;
					}
					if (poiChargingStation.getParkingInfo() != null) {
						poiChargingStation.setParkingInfo(ExcelReader.f2h(poiChargingStation.getParkingInfo()));
						ischanged = true;
					}
					if (!ischanged) {
						return result;
					}
					JSONObject changeFields = poiChargingStation.Serialize(null);
					changeFields.put("objStatus", ObjStatus.UPDATE.toString());
					changeFields.remove("uDate");
					dataArray.add(changeFields);
					result.put("chargingstations", dataArray);
				}
			} else if (kindcode.equals("230227")) {
				JSONArray dataArray = new JSONArray();
				List<IRow> poiChargingPlots = poi.getChargingplots();
				for (IRow plot:poiChargingPlots) {
					IxPoiChargingPlot poiChargingPlot = (IxPoiChargingPlot) plot;
					if (poiChargingPlot.getPower() != null) {
						poiChargingPlot.setPower(ExcelReader.f2h(poiChargingPlot.getPower()));
						ischanged = true;
					}
					if (poiChargingPlot.getVoltage() != null) {
						poiChargingPlot.setVoltage(ExcelReader.f2h(poiChargingPlot.getVoltage()));
						ischanged = true;
					}
					if (poiChargingPlot.getCurrent() != null) {
						poiChargingPlot.setCurrent(ExcelReader.f2h(poiChargingPlot.getCurrent()));
						ischanged = true;
					}
					if (poiChargingPlot.getPrices() != null) {
						poiChargingPlot.setPrices(ExcelReader.f2h(poiChargingPlot.getPrices()));
						ischanged = true;
					}
					if (poiChargingPlot.getParkingNum() != null) {
						poiChargingPlot.setParkingNum(ExcelReader.f2h(poiChargingPlot.getParkingNum()));
						ischanged = true;
					}
					if (poiChargingPlot.getManufacturer() != null) {
						poiChargingPlot.setManufacturer(ExcelReader.f2h(poiChargingPlot.getManufacturer()));
						ischanged = true;
					}
					if (poiChargingPlot.getPlotNum() != null) {
						poiChargingPlot.setPlotNum(ExcelReader.f2h(poiChargingPlot.getPlotNum()));
						ischanged = true;
					}
					if (poiChargingPlot.getFactoryNum() != null) {
						poiChargingPlot.setFactoryNum(ExcelReader.f2h(poiChargingPlot.getFactoryNum()));
						ischanged = true;
					}
					if (poiChargingPlot.getProductNum() != null) {
						poiChargingPlot.setProductNum(ExcelReader.f2h(poiChargingPlot.getProductNum()));
						ischanged = true;
					}
					if (!ischanged) {
						return result;
					}
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
