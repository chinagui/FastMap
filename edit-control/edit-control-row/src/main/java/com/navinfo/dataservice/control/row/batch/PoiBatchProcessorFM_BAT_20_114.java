package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.control.row.batch.util.IBatch;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_114 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi,Connection conn,JSONObject json,EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		
		try {
			JSONObject poiData = json.getJSONObject("data");
			
			if (!poiData.containsKey("gasstations")) {
				return result;
			}
			
			if (poi.getuRecord() == 2) {
				return result;
			}
			
			String adminId = String.valueOf(poi.getAdminReal());

			if (adminId.startsWith("44") || adminId.startsWith("110") || adminId.startsWith("310")
					|| adminId.startsWith("3201") || adminId.startsWith("3202") || adminId.startsWith("3204")
					|| adminId.startsWith("3205") || adminId.startsWith("3206") || adminId.startsWith("3210")
					|| adminId.startsWith("3211") || adminId.startsWith("3212") || adminId.startsWith("12")) {
				
				List<IRow> gasstationList = poi.getGasstations();
				JSONArray dataArray = new JSONArray();
				
				for (IRow gasstation:gasstationList) {
					IxPoiGasstation ixPoiGasstation = (IxPoiGasstation) gasstation;
					
					int gasURecord = ixPoiGasstation.getuRecord();
					
					if (gasURecord != 1 && gasURecord != 3) {
						continue;
					}
					
					String oilType = ixPoiGasstation.getOilType();
					boolean changeFlag = false;
					if (oilType.indexOf("90")>-1) {
						oilType.replace("90", "89");
						changeFlag = true;
					}
					if (oilType.indexOf("93")>-1) {
						oilType.replace("93", "92");
						changeFlag = true;
					}
					if (oilType.indexOf("97")>-1) {
						oilType.replace("97", "95");
						changeFlag = true;
					}
					if (changeFlag) {
						ixPoiGasstation.setOilType(oilType);
						JSONObject changeFields = ixPoiGasstation.Serialize(null);
						changeFields.put("objStatus", ObjStatus.UPDATE.toString());
						dataArray.add(changeFields);
					}
				}
				
				if (dataArray.size()>0) {
					result.put("gasstations", dataArray);
				}
				
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
