package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiGasstation;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_D20_001 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			JSONObject poiData = json.getJSONObject("data");
			
			if (!poiData.containsKey("gasstations")) {
				return result;
			}
			
			JSONArray changeArray = new JSONArray();
			
			List<IRow> gasstationList = poi.getGasstations();
			for (IRow gas:gasstationList) {
				IxPoiGasstation gasstation = (IxPoiGasstation) gas;
				String oilType = gasstation.getOilType();
				String egType = gasstation.getEgType();
				String mgType = gasstation.getMgType();
				String fuelType = gasstation.getFuelType();
				
				if (StringUtils.isNotEmpty(oilType)) {
					if (StringUtils.isEmpty(fuelType)) {
						fuelType = "1";
					} else {
						if (fuelType.indexOf("1")<0) {
							fuelType += "|1";
						}
					}
				}
				if (StringUtils.isNotEmpty(egType)) {
					if (StringUtils.isEmpty(fuelType)) {
						fuelType = "6";
					} else {
						if (fuelType.indexOf("6")<0) {
							fuelType += "|6";
						}
					}
				}
				if (StringUtils.isNotEmpty(mgType)) {
					if (StringUtils.isEmpty(fuelType)) {
						fuelType = "2";
					} else {
						if (fuelType.indexOf("2")<0) {
							fuelType += "|2";
						}
					}
				}
				
				gasstation.setFuelType(fuelType);
				JSONObject gasObj = gasstation.Serialize(null);
				gasObj.put("objStatus", ObjStatus.UPDATE.toString());
				gasObj.remove("uDate");
				changeArray.add(gasObj);
			}
			
			if (changeArray.size()>0) {
				result.put("gasstations", changeArray);
			}
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
