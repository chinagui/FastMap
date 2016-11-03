package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExcelReader;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.iface.OperType;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_103 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json,EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			JSONObject poiData = json.getJSONObject("data");
			
			if (!poiData.containsKey("addresses")) {
				return result;
			}
			
			OperType operType = Enum.valueOf(OperType.class,
                    json.getString("command"));
			
			JSONArray addresses = poiData.getJSONArray("addresses");
			
			JSONArray resultArray = new JSONArray();
			
			for (int i=0;i<addresses.size();i++) {
				JSONObject address = addresses.getJSONObject(i);
				
				String objStatus = "";
				if (address.containsKey("objStatus")) {
					objStatus = address.getString("objStatus");
				} 
				
				if (objStatus.equals(ObjStatus.INSERT.toString()) || objStatus.equals(ObjStatus.UPDATE.toString()) || operType.equals(OperType.CREATE)) {
					// 半角转全角
					String fullName = address.getString("fullname");
					fullName = ExcelReader.h2f(fullName);
					
					MetadataApi apiService=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");

					String[] pyStr = apiService.pyConvert(fullName);
					
					JSONObject data = new JSONObject();
					data.put("fullname", fullName);
					data.put("fullnamePhonetic", pyStr[1]);
					if(address.containsKey("rowId") && objStatus.equals(ObjStatus.UPDATE.toString()))
					{
						data.put("rowId", address.getString("rowId"));
					}
					else
					{
						List<IRow> rows = poi.getAddresses();
						
						for(IRow row : rows)
						{
							IxPoiAddress poiAddr = (IxPoiAddress) row;
							
							if(poiAddr.getuRecord() ==1 && fullName.equals(poiAddr.getFullname()) && "CHI".equals(poiAddr.getLangCode()))
							{
								data.put("rowId", poiAddr.rowId());
								break;
							}
						}
					}
					data.put("objStatus", ObjStatus.UPDATE.toString());
					resultArray.add(data);
				} else {
					continue;
				}
			}
			
			result.put("addresses", resultArray);
			
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
