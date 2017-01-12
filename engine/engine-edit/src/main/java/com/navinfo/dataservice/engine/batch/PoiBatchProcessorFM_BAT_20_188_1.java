package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_188_1 implements IBatch {
	
	private List<String> serviceList = new ArrayList<String>();
	
	public PoiBatchProcessorFM_BAT_20_188_1() {
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
			ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));
			String kindcode = poi.getKindCode();
			List<IRow> parents = poi.getParents();
			JSONArray jsonPolt = null;
			JSONObject poiData = json.getJSONObject("data");
			if (poiData.containsKey("chargingplots")) {
				jsonPolt = poiData.getJSONArray("chargingplots");
			}

			if (!kindcode.equals("230227") || parents.size()==0 || (jsonPolt==null&&objType != ObjType.IXPOIPARENT)) {
				return result;
			}
			IxPoiParent tempParent = (IxPoiParent) parents.get(0);
			int parentPid = tempParent.getParentPoiPid();
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi parentPoi = (IxPoi) ixPoiSelector.loadById(parentPid, false, false);
			if (parentPoi.getChargingstations().size() == 0) {
				return result;
			}
			IxPoiChargingStation poiChargingStation = (IxPoiChargingStation) parentPoi.getChargingstations().get(0);
			String serviceProv = poiChargingStation.getServiceProv();
			if (serviceProv == null || serviceList.contains(serviceProv)) {
				return result;
			}
			List<IRow> chargingPlots = poi.getChargingplots();
			if (chargingPlots.size()==0) {
				return result;
			}
			JSONArray  changeArray = new JSONArray();
			for (IRow plot:chargingPlots) {
				IxPoiChargingPlot chargingPlot = (IxPoiChargingPlot)plot;
				if (!chargingPlot.getOpenType().equals(serviceProv)) {
					chargingPlot.setOpenType(serviceProv);
					JSONObject fields = chargingPlot.Serialize(null);
					fields.put("objStatus", ObjStatus.UPDATE.toString());
					fields.remove("uDate");
					changeArray.add(fields);
				}
			}
			
			if (changeArray.size()>0) {
				result.put("chargingplots", changeArray);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
