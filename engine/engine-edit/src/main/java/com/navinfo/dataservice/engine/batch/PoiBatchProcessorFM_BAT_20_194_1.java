package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_194_1 implements IBatch {
	private static final Logger logger = Logger.getLogger(PoiBatchProcessorFM_BAT_20_194_1.class);
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
			logger.info("poi.kindCode"+poi.getKindCode());
			if (!poiData.containsKey("kindCode")&&poi.getKindCode().isEmpty()) {
				logger.info(poi.getPid()+" 没有kindCode，返回");
				return result;
			}
			String kindCode = poi.getKindCode();
			if(poiData.containsKey("kindCode")){
				kindCode=poiData.getString("kindCode");
			}
			
			if (!kindCodeList.contains(kindCode)) {
				logger.info(poi.getPid()+"kindcode"+kindCode+" 没有在110101,110102,110103,110200,110301,110302,110303,110304，返回");
				return result;
			}
			
			List<IRow> parents = poi.getParents();
			
			if (parents.size()==0) {
				logger.info(poi.getPid()+" 没有父，返回");
				return result;
			}
			
			IxPoiParent temp = (IxPoiParent) parents.get(0);
			int parentPid = temp.getParentPoiPid();
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi parentPoi = (IxPoi) ixPoiSelector.loadById(parentPid, false, false);
			
			if (!parentPoi.getKindCode().equals("230215")) {
				logger.info("父kindcode为"+parentPoi.getKindCode()+"!=230215");
				return result;
			}
			
			List<IRow> gasstationList = parentPoi.getGasstations();
			
			JSONArray dataArray = new JSONArray();
			if(gasstationList==null||gasstationList.size()==0){
				logger.info("父没有gasstations，返回");
				return result;
			}
			logger.info("开始批父的gas");
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
			logger.info("变化的poi数量:"+dataArray.size());
			if (dataArray.size() > 0) {
				JSONObject poiObj = new JSONObject();
				JSONObject changeFields = new JSONObject();
				changeFields.put("gasstations", dataArray);
				changeFields.put("pid", parentPoi.getPid());
				changeFields.put("rowId", parentPoi.getRowId());
				poiObj.put("change", changeFields);
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
