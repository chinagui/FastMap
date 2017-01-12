package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_187 implements IBatch {

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
			
			// 取出所有子桩
			List<IxPoiChargingPlot> allPlots = new ArrayList<IxPoiChargingPlot>();
			for (IRow child:childPois) {
				IxPoi childPoi = (IxPoi) child;
				List<IRow> childPlots = childPoi.getChargingplots();
				for (IRow temp:childPlots) {
					allPlots.add((IxPoiChargingPlot)temp);
				}
				
			}
			
			JSONArray dataArray = detealNewData(allPlots);
			
			
			Map<Long,JSONArray> retObj = new HashMap<Long,JSONArray>();
			for (int i=0;i<dataArray.size();i++) {
				JSONArray tempArray = new JSONArray();
				JSONObject plotJson = dataArray.getJSONObject(i);
				Long pid = plotJson.getLong("poiPid");
				if (retObj.containsKey(pid)) {
					tempArray = retObj.get(pid);
				}
				tempArray.add(plotJson);
				retObj.put(pid, tempArray);
			}
			
			for (Long pid:retObj.keySet()) {
				JSONObject poiObj = new JSONObject();
				JSONObject changeFields = new JSONObject();
				JSONArray tempArray = retObj.get(pid);
				changeFields.put("chargingplots", tempArray);
				changeFields.put("pid", pid);
				poiObj.put("change", changeFields);
				poiObj.put("pid", pid);
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
	
	/**
	 * 重新分组所有桩
	 * @param maxGroupId
	 * @param newPlotsList
	 * @return
	 * @throws Exception
	 */
	private JSONArray detealNewData(List<IxPoiChargingPlot> newPlotsList) throws Exception {
		Map<Integer,List<IxPoiChargingPlot>> newPlotsMap = new HashMap<Integer,List<IxPoiChargingPlot>>();
		JSONArray changDate = new JSONArray();
		int maxGroupId = 1;
		// 将新增的数据分组
		for (IxPoiChargingPlot newPlots:newPlotsList) {
			if (newPlotsMap.containsKey(maxGroupId)) {
				boolean flag = false;
				for (int tempGroupId:newPlotsMap.keySet()) {
					IxPoiChargingPlot tempPlot = newPlotsMap.get(tempGroupId).get(0);
					// 判断是否同规格
					if (newPlots.getPlugType().equals(tempPlot.getPlugType())&&newPlots.getAcdc()==tempPlot.getAcdc()&&newPlots.getMode()==tempPlot.getMode()&&newPlots.getOpenType().equals(tempPlot.getOpenType())) {
						flag = true;
						List<IxPoiChargingPlot> tempList = newPlotsMap.get(tempPlot.getGroupId());
						newPlots.setGroupId(tempPlot.getGroupId());
						tempList.add(newPlots);
						newPlotsMap.put(tempPlot.getGroupId(), tempList);
						break;
					} 
				}
				if (!flag) {
					maxGroupId += 1;
					List<IxPoiChargingPlot> tempList = new ArrayList<IxPoiChargingPlot>();
					newPlots.setGroupId(maxGroupId);
					tempList.add(newPlots);
					newPlotsMap.put(maxGroupId, tempList);
				}
			} else {
				List<IxPoiChargingPlot> tempList = new ArrayList<IxPoiChargingPlot>();
				newPlots.setGroupId(maxGroupId);
				tempList.add(newPlots);
				newPlotsMap.put(maxGroupId, tempList);
			}
		}
		
		// 更改count
		for (Integer groupId:newPlotsMap.keySet()) {
			List<IxPoiChargingPlot> tempList = newPlotsMap.get(groupId);
			for (IxPoiChargingPlot temp:tempList) {
				temp.setCount(tempList.size());
				JSONObject changeFields = temp.Serialize(null);
				changeFields.put("objStatus", ObjStatus.UPDATE.toString());
				changeFields.remove("uDate");
				changDate.add(changeFields);
			}
		}
		return changDate;
	}
	
}
