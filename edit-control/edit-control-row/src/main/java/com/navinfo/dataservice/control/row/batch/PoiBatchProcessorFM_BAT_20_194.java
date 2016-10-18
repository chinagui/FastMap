package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.control.row.batch.util.IBatch;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_194 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn,JSONObject json,EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			JSONObject poiData = json.getJSONObject("data");
			
			if (!poiData.containsKey("gasstations")) {
				return result;
			}
			
			String kindcode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			List<IRow> children = poi.getChildren();
			if (!kindcode.equals("230215") || uRecord ==2 || children.size() == 0) {
				return result;
			}

			List<Integer> pidList = new ArrayList<Integer>();
			for (IRow child : children) {
				IxPoiChildren ixPoiChildren = (IxPoiChildren) child;
				pidList.add(ixPoiChildren.getChildPoiPid());
			}
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			List<IRow> childPois = ixPoiSelector.loadByIds(pidList, false, true);

			List<IRow> gasstationList = poi.getGasstations();

			JSONArray dataArray = new JSONArray();

			for (IRow gasstation : gasstationList) {
				IxPoiGasstation ixPoiGasstation = (IxPoiGasstation) gasstation;
				
				int gasURecord = ixPoiGasstation.getuRecord();
				
				if (gasURecord == 2) {
					continue;
				}
				
				String service = ixPoiGasstation.getService();
				boolean changeFlag = false;
				for (IRow temp : childPois) {
					IxPoi childPoi = (IxPoi) temp;
					if (childPoi.getKindCode().equals("130105")) {
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
					if (childPoi.getKindCode().equals("140203")) {
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
					if (childPoi.getKindCode().equals("140302")) {
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
					if (childPoi.getKindCode().equals("210215")) {
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
					if (childPoi.getKindCode().equals("110101") || childPoi.getKindCode().equals("110102")
							|| childPoi.getKindCode().equals("110103") || childPoi.getKindCode().equals("110200")
							|| childPoi.getKindCode().equals("110301") || childPoi.getKindCode().equals("110302")
							|| childPoi.getKindCode().equals("110303") || childPoi.getKindCode().equals("110304")) {
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
					if (childPoi.getKindCode().equals("120101") || childPoi.getKindCode().equals("120102")
							|| childPoi.getKindCode().equals("120103") || childPoi.getKindCode().equals("120104")
							|| childPoi.getKindCode().equals("120201") || childPoi.getKindCode().equals("120202")) {
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
				}

				if (changeFlag) {
					ixPoiGasstation.setService(service);
					JSONObject changeFields = ixPoiGasstation.Serialize(null);
					changeFields.remove("uDate");
					changeFields.put("objStatus", ObjStatus.UPDATE.toString());
					dataArray.add(changeFields);
				}
			}

			if (dataArray.size() > 0) {
				result.put("gasstations", dataArray);
			}

			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
