package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiChargingPlot;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_187_1 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray dataArray = new JSONArray();
		try {
			OperType operType = Enum.valueOf(OperType.class, json.getString("command"));
            ObjType objType = Enum.valueOf(ObjType.class, json.getString("type"));
			String kindcode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			List<IRow> poiPlot = poi.getChargingplots();
			List<IRow> parents = poi.getParents();
			JSONArray jsonPolt = null;
			JSONObject poiData = json.getJSONObject("data");
			if (poiData.containsKey("chargingplots")) {
				jsonPolt = poiData.getJSONArray("chargingplots");
			}
			
			if (!kindcode.equals("230227") || parents.size()==0 || (jsonPolt == null && uRecord != 2)) {
				return result;
			}
			
			// 对其他子的桩数据进行处理
			Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap = getOldGroups(parents,poi,conn);
			
			List<IxPoiChargingPlot> plotChangeList = new ArrayList<IxPoiChargingPlot>();
			Map<Integer,Integer> deleteMap = new HashMap<Integer,Integer>();
			boolean plotsChanged = false;
			
			if (objType == ObjType.IXPOIPARENT) {
				// 父子关系变更处理
				if (operType == OperType.DELETE) {
					// 解除父子关系
					dataArray = dealDelete(poiPlot, oldPlotsMap);
				} else if (operType == OperType.CREATE) {
					// 增加父子关系
					plotsChanged = true;
					for (IRow tempPlot:poiPlot) {
						IxPoiChargingPlot chargingPlot = (IxPoiChargingPlot) tempPlot;
						plotChangeList.add(chargingPlot);
					}
				}
			}else if (uRecord == 2) {
				// 删除桩poi,特殊处理
				dataArray = dealDelete(poiPlot, oldPlotsMap);
			} else {
				// 判断充电桩是新增、修改还是删除
				
				for (int i=0;i<jsonPolt.size();i++) {
					JSONObject temp = jsonPolt.getJSONObject(i);
					if (temp.containsKey("PLUG_TYPE")||temp.containsKey("ACDC")||temp.containsKey("MODE")||temp.containsKey("OPEN_TYPE")) {
						plotsChanged = true;
						if (temp.getString("objStatus").equals(OperType.DELETE.toString()) ) {
							if (deleteMap.containsKey(temp.getInt("groupId"))) {
								int count = deleteMap.get(temp.getInt("groupId")) + 1;
								deleteMap.put(temp.getInt("groupId"), count);
							} else {
								deleteMap.put(temp.getInt("groupId"), 1);
							}
						} else {
							for (IRow tempPlot:poiPlot) {
								IxPoiChargingPlot chargingPlot = (IxPoiChargingPlot) tempPlot;
								if (chargingPlot.getRowId().equals(temp.getString("rowId"))) {
									plotChangeList.add(chargingPlot);
								}
							}
						}
					} 
				}
				
				if (!plotsChanged) {
					// 指定字段无变化
					return result;
				}
				
				if (plotChangeList.size()>0) {
					// 新增、修改桩
					dataArray = dealData(oldPlotsMap,plotChangeList);
				} 
				if (deleteMap.size()>0) {
					// 删除桩
					dataArray = dealData(oldPlotsMap,deleteMap);
				}
			}
			
			for (int i=0;i<dataArray.size();i++) {
				JSONObject plotJson = dataArray.getJSONObject(i);
				JSONObject poiObj = new JSONObject();
				JSONObject changeFields = new JSONObject();
				changeFields.put("chargingplots", dataArray);
				changeFields.put("pid", plotJson.getLong("poiPid"));
				changeFields.put("rowId", plotJson.getString("rowId"));
				poiObj.put("change", changeFields);
				poiObj.put("pid", plotJson.getLong("poiPid"));
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
	 * 查询包装其他同父的子的充电桩
	 * @param parents
	 * @param poi
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private Map<Integer,List<IxPoiChargingPlot>> getOldGroups(List<IRow> parents,IxPoi poi,Connection conn) throws Exception {
		// 查出父poi
		IxPoiParent tempParent = (IxPoiParent) parents.get(0);
		int parentPid = tempParent.getParentPoiPid();
		IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
		IxPoi parentPoi = (IxPoi) ixPoiSelector.loadById(parentPid, false, false);
		
		// 拿出其他子poi
		List<IRow> children = parentPoi.getChildren();
		List<Integer> pidList = new ArrayList<Integer>();
		for (IRow child : children) {
			IxPoiChildren ixPoiChildren = (IxPoiChildren) child;
			if (ixPoiChildren.getChildPoiPid() != poi.getPid()) {
				pidList.add(ixPoiChildren.getChildPoiPid());
			}
		}
		List<IRow> childPois = ixPoiSelector.loadByIds(pidList, false, true);
		
		// 其他充电桩
		List<IRow> allPlots = new ArrayList<IRow>();
		for (IRow child:childPois) {
			IxPoi childPoi = (IxPoi) child;
			List<IRow> childPlots = childPoi.getChargingplots();
			allPlots.addAll(childPlots);
		}
		
		// 将原有充电桩分组
		return groupPlots(allPlots);
	}
	
	/**
	 * 处理删除子poi的情况
	 * @param poi
	 * @param oldPlotsMap
	 * @return
	 * @throws Exception
	 */
	private JSONArray dealDelete(List<IRow> poiPlot,Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap) throws Exception {
		JSONArray dataArray = new JSONArray();
		for (IRow temp:poiPlot) {
			IxPoiChargingPlot plot = (IxPoiChargingPlot) temp;
			if (oldPlotsMap.containsKey(plot.getGroupId())) {
				List<IxPoiChargingPlot> oldList = oldPlotsMap.get(plot.getGroupId());
				for (IxPoiChargingPlot oldPlot:oldList) {
					if (oldPlot.getCount()!=oldList.size()) {
						oldPlot.setCount(oldList.size());
						JSONObject changeFields = oldPlot.Serialize(null);
						changeFields.put("objStatus", ObjStatus.UPDATE.toString());
						changeFields.remove("uDate");
						dataArray.add(changeFields);
					}
				}
			}
		}
		return dataArray;
	}
	
	/**
	 * 将其他子poi的充电桩数据分组
	 * @param childPlots
	 * @return
	 * @throws Exception
	 */
	private Map<Integer,List<IxPoiChargingPlot>> groupPlots(List<IRow> childPlots) throws Exception{
		Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap = new HashMap<Integer,List<IxPoiChargingPlot>>();
		List<IxPoiChargingPlot> tempList = null;
		for (IRow plots:childPlots) {
			IxPoiChargingPlot chargingPlot = (IxPoiChargingPlot) plots;
			int groupId = chargingPlot.getGroupId();
			if (oldPlotsMap.containsKey(groupId)) {
				tempList = oldPlotsMap.get(groupId);
			} else {
				tempList = new ArrayList<IxPoiChargingPlot>();
			}
			tempList.add(chargingPlot);
			oldPlotsMap.put(groupId, tempList);
		}
		return oldPlotsMap;
	}
	
	/**
	 * 处理删除的桩
	 * @param oldPlotsMap
	 * @param deleteMap
	 * @return
	 * @throws Exception
	 */
	private JSONArray dealData(Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap,Map<Integer,Integer> deleteMap) throws Exception {
		JSONArray dataArray = new JSONArray();
		for (int groupId:deleteMap.keySet()) {
			if (oldPlotsMap.containsKey(groupId)) {
				List<IxPoiChargingPlot> plotsList = oldPlotsMap.get(groupId);
				for (IxPoiChargingPlot oldPlot:plotsList) {
					oldPlot.setCount(oldPlot.getCount() - deleteMap.get(groupId));
					JSONObject changeFields = oldPlot.Serialize(null);
					changeFields.put("objStatus", ObjStatus.UPDATE.toString());
					changeFields.remove("uDate");
					dataArray.add(changeFields);
				}
			}
		}
		return dataArray;
	}
	
	/**
	 * 将本次新增、修改保存的充电桩与其他同父充电桩比较分组
	 * @param oldPlotsMap
	 * @param poiPlots
	 * @return
	 * @throws Exception
	 */
	private JSONArray dealData(Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap,List<IxPoiChargingPlot> poiPlots) throws Exception {
		JSONArray dataArray = new JSONArray();
		Set<Integer> groupIdSet = new HashSet<Integer>();
		for (IxPoiChargingPlot poiPlot:poiPlots) {
			int maxGroupId = 1 + getMaxGroupId(oldPlotsMap);
			int groupId = poiPlot.getGroupId();
			groupIdSet.add(groupId);
			
			// 判断数据属于哪个组，更改组号和该组count值
			JSONArray oldPlotChanges = compareWithOldGroup(oldPlotsMap,poiPlot);
			if (oldPlotChanges.size()>0) {
				dataArray.addAll(oldPlotChanges);
			} else {
				// 不属于已有的组，新建组
				poiPlot.setGroupId(maxGroupId);
				poiPlot.setCount(1);
				List<IxPoiChargingPlot> tempList = new ArrayList<IxPoiChargingPlot>();
				tempList.add(poiPlot);
				oldPlotsMap.put(maxGroupId, tempList);
				JSONObject changeFields = poiPlot.Serialize(null);
				changeFields.put("objStatus", ObjStatus.UPDATE.toString());
				changeFields.remove("uDate");
				dataArray.add(changeFields);
			}
			
		}
		for (int tempGroupId:groupIdSet) {
			if (oldPlotsMap.containsKey(tempGroupId)) {
				// 当为修改时，处理原组内的count值；将改变的充电桩并入新组
				List<IxPoiChargingPlot> oldPlots = oldPlotsMap.get(tempGroupId);
				for (IxPoiChargingPlot oldTemp:oldPlots) {
					oldTemp.setCount(oldPlots.size());
					JSONObject changeFields = oldTemp.Serialize(null);
					changeFields.put("objStatus", ObjStatus.UPDATE.toString());
					changeFields.remove("uDate");
					dataArray.add(changeFields);
				}
			} 
		}
		return dataArray;
	}
	
	/**
	 * 与已有组进行比较，判断是否属于已有的组
	 * @param oldPlotsMap
	 * @param poiPlot
	 * @return
	 * @throws Exception
	 */
	private JSONArray compareWithOldGroup(Map<Integer,List<IxPoiChargingPlot>> oldPlotsMap,IxPoiChargingPlot poiPlot) throws Exception {
		JSONArray changeArray = new JSONArray();
		for(Integer oldGroupId:oldPlotsMap.keySet()) {
			List<IxPoiChargingPlot> oldList = oldPlotsMap.get(oldGroupId);
			IxPoiChargingPlot oldPlot = oldList.get(0);
			String plug = oldPlot.getPlugType();
			int acdc = oldPlot.getAcdc();
			int mode = oldPlot.getMode();
			String open = oldPlot.getOpenType();
			if (poiPlot.getPlugType().equals(plug)&&poiPlot.getAcdc()==acdc&&poiPlot.getMode()==mode&&poiPlot.getOpenType().equals(open)) {
				// 相同，增加groupId,count
				poiPlot.setGroupId(oldGroupId);
				poiPlot.setCount(oldList.size()+1);
				JSONObject changeFields = poiPlot.Serialize(null);
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
		return changeArray;
	}
	
	/**
	 * 获取当前最大的groupId
	 * @param oldPlotsMap
	 * @return
	 */
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
