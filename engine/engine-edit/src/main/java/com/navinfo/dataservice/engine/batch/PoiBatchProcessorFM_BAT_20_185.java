package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingStation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_185 implements IBatch {

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
			
			List<Integer> pidList = new ArrayList<Integer>();
			for (IRow child : children) {
				IxPoiChildren ixPoiChildren = (IxPoiChildren) child;
				pidList.add(ixPoiChildren.getChildPoiPid());
			}
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			List<IRow> childPois = ixPoiSelector.loadByIds(pidList, false, true);
			int count = 0;
			for (IRow childPoi:childPois) {
				IxPoi child = (IxPoi) childPoi;
				List<IRow> childChargingPlot = child.getChargingplots();
				count += childChargingPlot.size();
			}
			List<IRow> parentChargingStations = poi.getChargingstations();
			if (parentChargingStations.size()==0) {
				return result;
			}
			IxPoiChargingStation parentChargingStation = (IxPoiChargingStation) parentChargingStations.get(0);
			parentChargingStation.setChargingNum(count);
			JSONArray dataArray = new JSONArray();
			JSONObject changeFields = parentChargingStation.Serialize(null);
			changeFields.remove("uDate");
			changeFields.put("objStatus", ObjStatus.UPDATE.toString());
			dataArray.add(changeFields);
			result.put("chargingstations", dataArray);
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
