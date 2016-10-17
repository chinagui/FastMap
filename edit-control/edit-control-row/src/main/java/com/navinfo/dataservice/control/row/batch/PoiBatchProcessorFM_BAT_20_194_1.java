package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.control.row.batch.util.IBatch;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_194_1 implements IBatch {
	
	private List<String> kindCodeList = new ArrayList<String>();
	
	public PoiBatchProcessorFM_BAT_20_194_1() {
		kindCodeList.add("130105");
		kindCodeList.add("140203");
		kindCodeList.add("140302");
		kindCodeList.add("210215");
		kindCodeList.add("110101");
		kindCodeList.add("110102");
		kindCodeList.add("110103");
		kindCodeList.add("110200");
		kindCodeList.add("110301");
		kindCodeList.add("110302");
		kindCodeList.add("110303");
		kindCodeList.add("110304");
		kindCodeList.add("120101");
		kindCodeList.add("120102");
		kindCodeList.add("120103");
		kindCodeList.add("120104");
		kindCodeList.add("120201");
		kindCodeList.add("120202");
	}

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json,EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			JSONObject poiData = json.getJSONObject("data");
			
			if (!poiData.containsKey("kindCode")) {
				return result;
			}
			String kindCode = poiData.getString("kindCode");
			
			if (!kindCodeList.contains(kindCode)) {
				return result;
			}
			
			List<IRow> parents = poi.getParents();
			
			if (parents.size()==0) {
				return result;
			}
			
			IxPoiParent temp = (IxPoiParent) parents.get(0);
			int parentPid = temp.getParentPoiPid();
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi parentPoi = (IxPoi) ixPoiSelector.loadById(parentPid, false, false);
			
			if (!parentPoi.getKindCode().equals("230215")) {
				return result;
			}
			
			List<IRow> gasstationList = parentPoi.getGasstations();
			
			JSONArray dataArray = new JSONArray();
			
			for (IRow gasstation : gasstationList) {
				IxPoiGasstation ixPoiGasstation = (IxPoiGasstation) gasstation;
				
				int gasURecord = ixPoiGasstation.getuRecord();
				
				if (gasURecord == 2) {
					continue;
				}
				
				String service = ixPoiGasstation.getService();
				boolean changeFlag = false;
				
				if (kindCode.equals("130105")) {
					if (service != null && service.length() > 0) {
						if (service.indexOf("1") < 0) {
							service += "|1";
							changeFlag = true;
						}
					} else {
						service = "1";
						changeFlag = true;
					}
				}
				if (kindCode.equals("140203")) {
					if (service != null && service.length() > 0) {
						if (service.indexOf("2") < 0) {
							service += "|2";
							changeFlag = true;
						}
					} else {
						service = "2";
						changeFlag = true;
					}
				}
				if (kindCode.equals("140302")) {
					if (service != null && service.length() > 0) {
						if (service.indexOf("3") < 0) {
							service += "|3";
							changeFlag = true;
						}
					} else {
						service = "3";
						changeFlag = true;
					}
				}
				if (kindCode.equals("210215")) {
					if (service != null && service.length() > 0) {
						if (service.indexOf("4") < 0) {
							service += "|4";
							changeFlag = true;
						}
					} else {
						service = "4";
						changeFlag = true;
					}
				}
				if (kindCode.equals("110101") || kindCode.equals("110102")
						|| kindCode.equals("110103") || kindCode.equals("110200")
						|| kindCode.equals("110301") || kindCode.equals("110302")
						|| kindCode.equals("110303") || kindCode.equals("110304")) {
					if (service != null && service.length() > 0) {
						if (service.indexOf("5") < 0) {
							service += "|5";
							changeFlag = true;
						}
					} else {
						service = "5";
						changeFlag = true;
					}
				}
				if (kindCode.equals("120101") || kindCode.equals("120102")
						|| kindCode.equals("120103") || kindCode.equals("120104")
						|| kindCode.equals("120201") || kindCode.equals("120202")) {
					if (service != null && service.length() > 0) {
						if (service.indexOf("6") < 0) {
							service += "|6";
							changeFlag = true;
						}
					} else {
						service = "6";
						changeFlag = true;
					}
				}
				
				if (changeFlag) {
					ixPoiGasstation.setService(service);
					JSONObject fields = ixPoiGasstation.Serialize(null);
					fields.put("objStatus", ObjStatus.UPDATE.toString());
					dataArray.add(fields);
				}
				
			}
			
			if (dataArray.size() > 0) {
				JSONObject poiObj = new JSONObject();
				JSONObject changeFields = new JSONObject();
				changeFields.put("gasstations", dataArray);
				changeFields.put("pid", parentPoi.getPid());
				changeFields.put("rowId", parentPoi.getRowId());
				parentPoi.fillChangeFields(changeFields);
				poiObj.put("poi", parentPoi.Serialize(null));
				poiObj.put("pid", parentPoi.getPid());
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
