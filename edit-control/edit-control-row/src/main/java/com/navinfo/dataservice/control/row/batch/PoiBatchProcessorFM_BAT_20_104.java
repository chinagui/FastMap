package com.navinfo.dataservice.control.row.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.row.batch.util.IBatch;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PoiBatchProcessorFM_BAT_20_104 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json) throws Exception {
		JSONObject result = new JSONObject();
		try {
			JSONObject poiData = json.getJSONObject("data");
			
			if (!poiData.containsKey("names")) {
				return result;
			}
			
			JSONArray resultArray = new JSONArray();
			
			List<IRow> names = poi.getNames();
			
			String originalName = "";
			String standardName = "";
			int nameGroupid = 0;
			String langCode = "";
			IxPoiName originalPoiName = new IxPoiName();
			for (IRow temp:names) {
				IxPoiName name = (IxPoiName) temp;
				if (name.getuRecord() == 2) {
					continue;
				}
				if (nameGroupid<name.getNameGroupid()) {
					nameGroupid = name.getNameGroupid();
				}
				if (name.getNameClass()==1 && (name.getLangCode().equals("CHI") || name.getLangCode().equals("CHT"))) {
					if (name.getNameType() == 1) {
						standardName = name.getName();
					} else if (name.getNameType() == 2) {
						originalName = name.getName();
						langCode = name.getLangCode();
						originalPoiName = name;
					}
				}
			}
			
			if (standardName.isEmpty()) {
				standardName = originalName;
				IxPoiName newStandardName = new IxPoiName();
				newStandardName.setNameClass(1);
				newStandardName.setNameType(1);
				newStandardName.setNameGroupid(nameGroupid+1);
				newStandardName.setName(standardName);
				newStandardName.setLangCode(langCode);
				// 转拼音
				MetadataApi apiService=(MetadataApi) ApplicationContextUtil.getBean("metadataApi");
				String[] pyStr = apiService.pyConvert(standardName);
				newStandardName.setNamePhonetic(pyStr[1]);
				JSONObject nameStandardJson = newStandardName.Serialize(null);
				nameStandardJson.put("objStatus", ObjStatus.INSERT.toString());
				
				// 标准，原始转全角
				
				JSONObject nameOriginalJson = originalPoiName.Serialize(null);
				nameOriginalJson.put("objStatus", ObjStatus.UPDATE.toString());
				
				resultArray.add(nameStandardJson);
				resultArray.add(nameOriginalJson);
				result.put("names", resultArray);
			} else {
				if (standardName.equals(originalName)) {
					return result;
				}
			}
			
			return result;
			
		} catch (Exception e) {
			throw e;
		}
		
	}

}
