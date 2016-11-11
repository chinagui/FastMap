package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiParking;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * @ClassName: PoiBatchProcessorFM_BAT_20_198 
 * @author Gao Pengrong
 * @date 2016-11-10 下午4:41:24 
 * @Description: 深度信息停车场前批：批处理原则："IX_POI.OPEN_24H"字段为“1”的，对停车场的“IX_POI_PARKING.OPEN_TIME”批处理为“00:00-24:00“
 */

public class PoiBatchProcessorFM_BAT_20_198 implements IBatch {

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json,EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			if (poi.getOpen24h()!=1){
				return result;
			}
			
			JSONObject data = new JSONObject();
			List<IRow> rows = poi.getParkings();
			JSONArray resultArray = new JSONArray();
			//模型定义一个poi只有一个parking子表
			for(IRow row : rows)
			{
				IxPoiParking poiPark = (IxPoiParking) row;
				if (!"00:00-24:00".equals(poiPark.getOpenTiime())){
					data.put("OpenTiime", "00:00-24:00");
					data.put("rowId", poiPark.rowId());
					data.put("objStatus", ObjStatus.UPDATE.toString());
					resultArray.add(data);
				}	
			}
			
			result.put("parkings", resultArray);
			return result;
		} catch (Exception e) {
			throw e;
		}
	}

}
