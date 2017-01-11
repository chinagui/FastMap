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
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
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
			ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));
			String kindcode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			List<IRow> parents = poi.getParents();
			JSONArray jsonPolt = null;
			JSONObject poiData = json.getJSONObject("data");
			if (poiData.containsKey("chargingplots")) {
				jsonPolt = poiData.getJSONArray("chargingplots");
			}
			if (!kindcode.equals("230227") || parents.size() == 0 || (jsonPolt == null && uRecord != 2 && objType != ObjType.IXPOIPARENT)) {
				return result;
			}
			
			IxPoiParent temp = (IxPoiParent) parents.get(0);
			
			int parentPid = temp.getParentPoiPid();
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi parentPoi = (IxPoi) ixPoiSelector.loadById(parentPid, false, false);
			
			int plotsNum = getPlotsSize(parentPoi,poi,conn);
			
			List<IRow> parentChargingStations = parentPoi.getChargingstations();
			if (parentChargingStations.size()==0) {
				return result;
			}
			IxPoiChargingStation parentChargingStation = (IxPoiChargingStation) parentChargingStations.get(0);
			parentChargingStation.setChargingNum(plotsNum);
			
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
	
	/**
	 * 查询所有子的充电桩数量
	 * @param parents
	 * @param poi
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private int getPlotsSize(IxPoi parentPoi,IxPoi poi,Connection conn) throws Exception {
		IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
		// 拿出所有子poi
		List<IRow> children = parentPoi.getChildren();
		List<Integer> pidList = new ArrayList<Integer>();
		for (IRow child : children) {
			IxPoiChildren ixPoiChildren = (IxPoiChildren) child;
			pidList.add(ixPoiChildren.getChildPoiPid());
		}
		List<IRow> childPois = ixPoiSelector.loadByIds(pidList, false, true);
		
		// 所有充电桩
		List<IxPoiChargingPlot> allPlots = new ArrayList<IxPoiChargingPlot>();
		for (IRow child:childPois) {
			IxPoi childPoi = (IxPoi) child;
			List<IRow> childPlots = childPoi.getChargingplots();
			for (IRow temp:childPlots) {
				allPlots.add((IxPoiChargingPlot)temp);
			}
		}
		
		return allPlots.size();
	}

}
