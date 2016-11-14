package com.navinfo.dataservice.engine.batch;

import java.sql.Connection;
import java.util.List;

import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjStatus;
import com.navinfo.dataservice.dao.glm.model.poi.deep.IxPoiBusinessTime;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.engine.batch.util.IBatch;
import com.navinfo.dataservice.engine.edit.service.EditApiImpl;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @ClassName: PoiBatchProcessorFM_BAT_20_196
 * @author: zhangpengpeng
 * @date: 2016年11月11日
 * @Desc: 通用营业时间批处理 具体原则见：深度信息批处理配置表——sheet通用营业时间批处理
 */
public class PoiBatchProcessorFM_BAT_20_196 implements IBatch {
	private JSONObject businessTimes = new JSONObject();

	public PoiBatchProcessorFM_BAT_20_196() {
		//businessTimes init
		businessTimes.put("monSrt", "1");
		businessTimes.put("monEnd", "12");
		businessTimes.put("weekInYearSrt", "1");
		businessTimes.put("weekInYearEnd", "-1");
		businessTimes.put("weekInMonthSrt", "1");
		businessTimes.put("weekInMonthEnd", "-1");
		businessTimes.put("validWeek", "1111111");
		businessTimes.put("daySrt", "1");
		businessTimes.put("dayEnd", "-1");
		businessTimes.put("timeSrt", "00:00");
		businessTimes.put("timeDur", "24:00");
	}

	@Override
	public JSONObject run(IxPoi poi, Connection conn, JSONObject json, EditApiImpl editApiImpl) throws Exception {
		JSONObject result = new JSONObject();
		try {
			// POI的open24h=1批处理
			if (poi.getOpen24h() == 1) {
				List<IRow> poiBusinessTimes = poi.getBusinesstimes();
				JSONArray dataArray = new JSONArray();
				for (IRow poiBusinessTime : poiBusinessTimes) {
					JSONObject data = new JSONObject();
					IxPoiBusinessTime ixPoiBusinessTime = (IxPoiBusinessTime) poiBusinessTime;
					// ixPoiBusinessTime不为删除的批处理
					if (ixPoiBusinessTime.getuRecord() != 2) {
						data.put("rowId", ixPoiBusinessTime.getRowId());
						data.put("objStatus", ObjStatus.UPDATE.toString());
						data.putAll(businessTimes);
						dataArray.add(data);
					}
				}

				if (dataArray.size() > 0) {
					result.put("businesstimes", dataArray);
				}
			}
			return result;
		} catch (Exception e) {
			throw e;
		}
	}
}
