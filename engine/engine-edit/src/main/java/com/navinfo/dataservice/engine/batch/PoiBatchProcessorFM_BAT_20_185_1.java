package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_185_1 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			String kindcode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			List<IRow> parents = poi.getParents();
			if (!kindcode.equals("230227") || uRecord ==2 || parents.size() == 0) {
				return result;
			}
			JSONObject poiData = json.getJSONObject("data");
			if (!poiData.containsKey("chargingplots")) {
				return result;
			}
			
			JSONArray childChargingplots = poiData.getJSONArray("chargingplots");
			int reduce = 0;
			int increase = 0;
			for (int i=0;i<childChargingplots.size();i++) {
				JSONObject childPlot = childChargingplots.getJSONObject(i);
				if (childPlot.getString("objStatus").equals(ObjStatus.DELETE.toString())) {
					reduce += 1;
				} else if (childPlot.getString("objStatus").equals(ObjStatus.INSERT.toString())) {
					increase += 1;
				}
			}
			if (reduce == 0 && increase == 0) {
				return result;
			}
			
			IxPoiParent temp = (IxPoiParent) parents.get(0);
			int parentPid = temp.getParentPoiPid();
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi parentPoi = (IxPoi) ixPoiSelector.loadById(parentPid, false, false);
			
			List<IRow> parentChargingStations = parentPoi.getChargingstations();
			if (parentChargingStations.size()==0) {
				return result;
			}
			IxPoiChargingStation parentChargingStation = (IxPoiChargingStation) parentChargingStations.get(0);
			int charingNum = parentChargingStation.getChargingNum();
			parentChargingStation.setChargingNum(charingNum-reduce+increase);
			
			JSONArray dataArray = new JSONArray();
			JSONObject fields = parentChargingStation.Serialize(null);
			fields.put("objStatus", ObjStatus.UPDATE.toString());
			fields.remove("uDate");
			dataArray.add(fields);
			
			JSONObject poiObj = new JSONObject();
			JSONObject changeFields = new JSONObject();
			changeFields.put("chargingstations", dataArray);
			changeFields.put("pid", parentPoi.getPid());
			changeFields.put("rowId", parentPoi.getRowId());
			poiObj.put("change", changeFields);
			poiObj.put("pid", parentPoi.getPid());
			poiObj.put("type", "IXPOI");
			poiObj.put("command", "BATCH");
			poiObj.put("dbId", json.getInt("dbId"));
			poiObj.put("isLock", false);
			
			editApiImpl.runPoi(poiObj);
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
