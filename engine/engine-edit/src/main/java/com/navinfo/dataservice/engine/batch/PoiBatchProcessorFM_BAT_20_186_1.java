package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiChildren;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_186_1 implements IBatch {

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
			JSONObject poiData = json.getJSONObject("data");
			if (!poiData.containsKey("addresses")&&!poiData.containsKey("contacts")) {
				return result;
			}
			List<Integer> pidList = new ArrayList<Integer>();
			for (IRow child : children) {
				IxPoiChildren ixPoiChildren = (IxPoiChildren) child;
				pidList.add(ixPoiChildren.getChildPoiPid());
			}
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			List<IRow> childPois = ixPoiSelector.loadByIds(pidList, false, true);
			
			for (IRow child:childPois) {
				IxPoi childPoi = (IxPoi) child;
				
				// 删除子的地址，覆盖为父的地址
				JSONArray addressArray = new JSONArray();
				if (poiData.containsKey("addresses")) {
					List<IRow> childAddresses = childPoi.getAddresses();
					for (IRow childAddress:childAddresses) {
						IxPoiAddress childAdd = (IxPoiAddress) childAddress;
						JSONObject childAddObj = childAdd.Serialize(null);
						childAddObj.put("objStatus", ObjStatus.DELETE.toString());
						childAddObj.remove("uDate");
						addressArray.add(childAddObj);
					}
					
					List<IRow> parentAddresses = poi.getAddresses();
					for (IRow parentAddress:parentAddresses) {
						IxPoiAddress parentAdd = (IxPoiAddress) parentAddress;
						parentAdd.setPoiPid(childPoi.getPid());
						JSONObject parentAddObj = parentAdd.Serialize(null);
						parentAddObj.put("objStatus", ObjStatus.INSERT.toString());
						parentAddObj.remove("uDate");
						parentAddObj.remove("rowId");
						addressArray.add(parentAddObj);
					}
				}
				
				// 删除子的电话，更新为父的电话
				JSONArray contactsArray = new JSONArray();
				if (poiData.containsKey("contacts")) {
					List<IRow> childContacts = childPoi.getContacts();
					for (IRow childContact:childContacts) {
						IxPoiContact childCon = (IxPoiContact) childContact;
						JSONObject childConObj = childCon.Serialize(null);
						childConObj.put("objStatus", ObjStatus.DELETE.toString());
						childConObj.remove("uDate");
						contactsArray.add(childConObj);
					}
					
					List<IRow> parentContacts = poi.getContacts();
					for (IRow parentContact:parentContacts) {
						IxPoiContact parentCon = (IxPoiContact) parentContact;
						parentCon.setPoiPid(childPoi.getPid());
						JSONObject parentConObj = parentCon.Serialize(null);
						parentConObj.put("objStatus", ObjStatus.INSERT.toString());
						parentConObj.remove("uDate");
						parentConObj.remove("rowId");
						contactsArray.add(parentConObj);
					}
					
				}
				
				JSONObject poiObj = new JSONObject();
				JSONObject changeFields = new JSONObject();
				if (addressArray.size()>0||contactsArray.size()>0) {
					changeFields.put("addresses", addressArray);
					changeFields.put("contacts", contactsArray);
					changeFields.put("pid", childPoi.getPid());
					changeFields.put("rowId", childPoi.getRowId());
					poiObj.put("change", changeFields);
					poiObj.put("pid", childPoi.getPid());
					poiObj.put("type", "IXPOI");
					poiObj.put("command", "BATCH");
					poiObj.put("dbId", json.getInt("dbId"));
					poiObj.put("isLock", false);
					
					editApiImpl.runPoi(poiObj);
				}
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
