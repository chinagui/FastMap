package com.navinfo.dataservice.control.app.search;

import java.sql.Connection;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.metadata.iface.MetadataApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Operation {
	
	/**
	 * 
	 * @param gridDateList
	 * @return
	 * @throws Exception
	 */
	public JSONArray downloadCheck(JSONArray gridDateList) throws Exception {
		Connection manConn = null;
		Connection conn = null;
		try {
			manConn = DBConnector.getInstance().getManConnection();
			String manQuery = "SELECT r.daily_db_id FROM grid g,region r WHERE g.region_id=r.region_id and grid_id=:1";
			int oldRegionId = 0;
			int regionId = 0;
			JSONArray retList = new JSONArray();
			for (int i=0;i<gridDateList.size();i++ ){
				JSONObject gridDate = gridDateList.getJSONObject(i);
				QueryRunner qRunner = new QueryRunner();
				regionId = qRunner.queryForInt(manConn, manQuery, gridDate.getString("grid"));
				
				if (regionId != oldRegionId){
					//关闭之前的连接，创建新连接
					DBUtils.closeConnection(conn);
					oldRegionId = regionId;
					conn = DBConnector.getInstance().getConnectionById(regionId);
				}
				
				if (!gridDate.getString("date").isEmpty()) {
					IxPoiSelector poiSelector = new IxPoiSelector(conn);
					JSONObject ret = poiSelector.downloadCheck(gridDate);
					retList.add(ret);
				} else {
					JSONObject ret = new JSONObject();
					ret.put("gridId", gridDate.getString("grid"));
					ret.put("flag", 1);
					retList.add(ret);
				}
					
			}
			return retList;
		} catch (Exception e) {
			throw e;
		}finally {
			DBUtils.closeConnection(conn);
			DBUtils.closeConnection(manConn);
		}
		
	}

}
