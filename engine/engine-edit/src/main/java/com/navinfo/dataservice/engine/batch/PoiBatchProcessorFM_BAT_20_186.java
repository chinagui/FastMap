package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiContact;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiParent;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_186 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			String kindcode = poi.getKindCode();
			int uRecord = poi.getuRecord();
			List<IRow> parents = poi.getParents();
			if (!kindcode.equals("230227") || uRecord ==2 || parents.size() == 0) {
				return result;
			}
			
			IxPoiParent temp = (IxPoiParent) parents.get(0);
			int parentPid = temp.getParentPoiPid();
			
			IxPoiSelector ixPoiSelector = new IxPoiSelector(conn);
			IxPoi parentPoi = (IxPoi) ixPoiSelector.loadById(parentPid, false, false);
			
			// 删除原来的电话和地址，赋值为父的电话和地址
			JSONArray addressDataArray = new JSONArray();
			List<IRow> childAddresses = poi.getAddresses();
			for (IRow childAddress:childAddresses) {
				IxPoiAddress childAdd = (IxPoiAddress) childAddress;
				JSONObject childAddObj = childAdd.Serialize(null);
				childAddObj.put("objStatus", ObjStatus.DELETE.toString());
				childAddObj.remove("uDate");
				addressDataArray.add(childAddObj);
			}
			
			List<IRow> parentAddresses = parentPoi.getAddresses();
			for (IRow parentAddress:parentAddresses) {
				IxPoiAddress parentAdd = (IxPoiAddress) parentAddress;
				JSONObject parentAddObj = parentAdd.Serialize(null);
				parentAddObj.put("objStatus", ObjStatus.INSERT.toString());
				parentAddObj.remove("uDate");
				addressDataArray.add(parentAddObj);
			}
			
			JSONArray contactDataArray = new JSONArray();
			List<IRow> childContacts = poi.getContacts();
			for (IRow childContact:childContacts) {
				IxPoiContact childCon = (IxPoiContact) childContact;
				JSONObject childConObj = childCon.Serialize(null);
				childConObj.put("objStatus", ObjStatus.DELETE.toString());
				childConObj.remove("uDate");
				contactDataArray.add(childConObj);
			}
			
			List<IRow> parentContacts = parentPoi.getContacts();
			for (IRow parentContact:parentContacts) {
				IxPoiContact parentCon = (IxPoiContact) parentContact;
				JSONObject parentConObj = parentCon.Serialize(null);
				parentConObj.put("objStatus", ObjStatus.INSERT.toString());
				parentConObj.remove("uDate");
				contactDataArray.add(parentConObj);
			}
			
			result.put("addresses", addressDataArray);
			result.put("contacts", contactDataArray);
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
