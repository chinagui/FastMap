package com.navinfo.dataservice.control.row.batch;

import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_114 extends IBatch {

	@Override
	public JSONObject run(IxPoi poi) throws Exception {
		JSONObject reuslt = new JSONObject();
		
		try {
			if (poi.getuRecord() != 1 && poi.getuRecord() != 3) {
				return reuslt;
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
				
				reuslt.put("gasstations", dataArray);
			}
			
			return reuslt;
		} catch (Exception e) {
			throw e;
		}
	}

}
