package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_188 implements IBatch {
	
	private List<String> serviceList = new ArrayList<String>();
	
	public PoiBatchProcessorFM_BAT_20_188() {
		serviceList.add("0");
		serviceList.add("1");
		serviceList.add("2");
		serviceList.add("3");
		serviceList.add("4");
		serviceList.add("5");
		serviceList.add("6");
		serviceList.add("7");
		serviceList.add("8");
		serviceList.add("9");
		serviceList.add("10");
		serviceList.add("11");
		serviceList.add("12");
		serviceList.add("13");
		serviceList.add("14");
		serviceList.add("15");
		serviceList.add("16");
		serviceList.add("17");
		serviceList.add("18");
	}

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			String kindcode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			List<IRow> children = poi.getChildren();
			if (!kindcode.equals("230218") || uRecord ==2 || children.size() == 0) {
				return result;
			}
			if (poi.getChargingstations().size() == 0) {
				return result;
			}
			IxPoiChargingStation poiChargingStation = (IxPoiChargingStation) poi.getChargingstations().get(0);
			String serviceProv = poiChargingStation.getServiceProv();
			if (serviceProv == null || serviceList.contains(serviceProv)) {
				return result;
			}
			
			List<Integer> pidList = new ArrayList<Integer>();
			for (IRow child : children) {
				IxPoiChildren ixPoiChildren = (IxPoiChildren) child;
				pidList.add(ixPoiChildren.getChildPoiPid());
			}
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			List<IRow> childPois = ixPoiSelector.loadByIds(pidList, false, true);
			
			for (IRow child:childPois) {
				IxPoi childPoi = (IxPoi) child;
				List<IRow> childPlots = childPoi.getChargingplots();
				JSONArray dataArray = new JSONArray();
				for (IRow childPlotRow:childPlots) {
					IxPoiChargingPlot childPlot = (IxPoiChargingPlot) childPlotRow;
					childPlot.setOpenType(serviceProv);
					JSONObject fields = childPlot.Serialize(null);
					fields.put("objStatus", ObjStatus.UPDATE.toString());
					fields.remove("uDate");
					dataArray.add(fields);
				}
				JSONObject poiObj = new JSONObject();
				JSONObject changeFields = new JSONObject();
				changeFields.put("chargingplots", dataArray);
				changeFields.put("pid", childPoi.getPid());
				changeFields.put("rowId", childPoi.getRowId());
				poiObj.put("change", changeFields);
				poiObj.put("pid", childPoi.getPid());
				poiObj.put("type", "IXPOI");
				poiObj.put("command", "BATCH");
				poiObj.put("dbId", json.getInt("dbId"));
				poiObj.put("isLock", false);
				
				editApiImpl.runPoi(poiObj);
				
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}
}
