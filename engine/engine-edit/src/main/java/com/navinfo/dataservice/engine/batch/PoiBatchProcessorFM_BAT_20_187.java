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
			
			List<IRow> allPlots = new ArrayList<IRow>();
			for (IRow child:childPois) {
				IxPoi childPoi = (IxPoi) child;
				List<IRow> childPlots = childPoi.getChargingplots();
				allPlots.addAll(childPlots);
			}
			
			JSONArray dataArray = getChangePlots(allPlots);
			
			
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
	
	// 获取更新的内容
	private JSONArray getChangePlots(List<IRow> childPlots) throws Exception{
		List<IxPoiChargingPlot> newPlotsList = new ArrayList<IxPoiChargingPlot>(); 
		Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap = new HashMap<Integer,List<IxPoiChargingPlot>>();
		// 数据归类，分为需要新增groupId的数据，和已分组的数据
		for (IRow plots:childPlots) {
			IxPoiChargingPlot chargingPlot = (IxPoiChargingPlot) plots;
			int groupId = chargingPlot.getGroupId();
			if (groupId == 0) {
				newPlotsList.add(chargingPlot);
			} else {
				if (oldPlotsMap.containsKey(groupId)) {
					List<IxPoiChargingPlot> tempList = oldPlotsMap.get(groupId);
					tempList.add(chargingPlot);
					oldPlotsMap.put(groupId, tempList);
				} else {
					List<IxPoiChargingPlot> tempList = new ArrayList<IxPoiChargingPlot>();
					tempList.add(chargingPlot);
					oldPlotsMap.put(groupId, tempList);
				}
			}
		}
		return getChangeArray(newPlotsList,oldPlotsMap);
	}
	
	// 获取要新赋值的数据
	private JSONArray getChangeArray(List<IxPoiChargingPlot> newPlotsList,Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap) throws Exception {
		JSONArray changeArray = new JSONArray();
		// 判断新增数据中，是否存在与已存在组同规格的数据
		for(Integer oldGroupId:oldPlotsMap.keySet()) {
			List<IxPoiChargingPlot> oldList = oldPlotsMap.get(oldGroupId);
			IxPoiChargingPlot oldPlot = oldList.get(0);
			String plug = oldPlot.getPlugType();
			int acdc = oldPlot.getAcdc();
			int mode = oldPlot.getMode();
			String open = oldPlot.getOpenType();
			for (IxPoiChargingPlot newPlots:newPlotsList) {
				if (newPlots.getPlugType().equals(plug)&&newPlots.getAcdc()==acdc&&newPlots.getMode()==mode&&newPlots.getOpenType().equals(open)) {
					// 相同，增加groupId,count
					newPlotsList.remove(newPlots);
					newPlots.setGroupId(oldGroupId);
					newPlots.setCount(oldList.size()+1);
					JSONObject changeFields = newPlots.Serialize(null);
					changeFields.put("objStatus", ObjStatus.UPDATE.toString());
					changeFields.remove("uDate");
					changeArray.add(changeFields);
					// 修改旧数据的count值
					for (IxPoiChargingPlot oldPlots:oldList) {
						oldPlots.setCount(oldList.size()+1);
						JSONObject oldChangeFields = oldPlots.Serialize(null);
						oldChangeFields.put("objStatus", ObjStatus.UPDATE.toString());
						oldChangeFields.remove("uDate");
						changeArray.add(oldChangeFields);
					}
				}
			}
		}
		// 新数据分组，增加groupId
		if (newPlotsList.size()>0) {
			// 获取当前最大groupId
			int maxGroupId = 0;
			if (oldPlotsMap.size()>0) {
				maxGroupId = getMaxGroupId(oldPlotsMap);
			}
			changeArray.addAll(detealNewData(maxGroupId,newPlotsList));
		}
		
		return changeArray;
	}
	
	// 处理新增数据的分组
	private JSONArray detealNewData(int maxGroupId,List<IxPoiChargingPlot> newPlotsList) throws Exception {
		Map<Integer,List<IxPoiChargingPlot>> newPlotsMap = new HashMap<Integer,List<IxPoiChargingPlot>>();
		JSONArray changDate = new JSONArray();
		maxGroupId += 1;
		// 将新增的数据分组
		for (IxPoiChargingPlot newPlots:newPlotsList) {
			if (newPlotsMap.containsKey(maxGroupId)) {
				IxPoiChargingPlot tempPlot = newPlotsMap.get(maxGroupId).get(0);
				// 判断是否同规格
				if (newPlots.getPlugType().equals(tempPlot.getPlugType())&&newPlots.getAcdc()==tempPlot.getAcdc()&&newPlots.getMode()==tempPlot.getMode()&&newPlots.getOpenType().equals(tempPlot.getOpenType())) {
					List<IxPoiChargingPlot> tempList = newPlotsMap.get(maxGroupId);
					newPlots.setGroupId(maxGroupId);
					tempList.add(newPlots);
					newPlotsMap.put(maxGroupId, tempList);
				} else {
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
	
	// 获取当前最大的groupId
	private int getMaxGroupId(Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap) {
		int maxGroupId = 0;
		for (Integer gourpId:oldPlotsMap.keySet()) {
			if (maxGroupId == 0) {
				maxGroupId = gourpId;
			} else {
				if (gourpId>maxGroupId) {
					maxGroupId = gourpId;
				}
			}
		}
		return maxGroupId;
	}

}
