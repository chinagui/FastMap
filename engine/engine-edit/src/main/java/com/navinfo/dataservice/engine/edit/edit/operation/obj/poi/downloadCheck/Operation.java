package com.navinfo.dataservice.engine.edit.edit.operation.obj.poi.downloadCheck;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;

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
			PreparedStatement pstmt = null;
			ResultSet resultSet = null;
			int oldRegionId = 0;
			int regionId = 0;
			JSONArray retList = new JSONArray();
			for (int i=0;i<gridDateList.size();i++ ){
				pstmt = manConn.prepareStatement(manQuery);
				JSONObject gridDate = gridDateList.getJSONObject(i);
				pstmt.setString(1, gridDate.getString("grid"));
				resultSet = pstmt.executeQuery();
				if (resultSet.next()){
					regionId = resultSet.getInt("daily_db_id");
					
					if (regionId != oldRegionId){
						//关闭之前的连接，创建新连接
						if (conn != null) {
							try {
								conn.close();
							} catch (Exception e) {

							}
						}
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
					
				} else {
					continue;
				}
			}
			return retList;
		} catch (Exception e) {
			throw e;
		}finally {
			if (manConn != null) {
				try {
					manConn.close();
				} catch (Exception e) {

				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {

				}
			}
		}
		
	}

}
