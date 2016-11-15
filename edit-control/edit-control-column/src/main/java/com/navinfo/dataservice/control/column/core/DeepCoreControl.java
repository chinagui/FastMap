package com.navinfo.dataservice.control.column.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONObject;

public class DeepCoreControl {
	private static final Logger logger = Logger.getLogger(DeepCoreControl.class);

	public JSONObject getLogCount(Subtask subtask,int dbId) throws Exception {
		
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT (select count(1) from poi_deep_status p where p.row_id=i.row_id and p.type=1 and p.status=1 and p.handler is null) AS detail,");
		sb.append(" (select count(1) from poi_deep_status p where p.row_id=i.row_id and p.type=2 and p.status=1 and p.handler is null) AS parking,");
		sb.append(" (select count(1) from poi_deep_status p where p.row_id=i.row_id and p.type=3 and p.status=1 and p.handler is null) AS carrental,");
		sb.append(" FROM ix_poi i");
		sb.append(" WHERE sdo_within_distance(i.geometry, sdo_geometry(    :1  , 8307), 'mask=anyinteract') = 'TRUE' ");
		sb.append(" AND i.u_record!=2");
		
		Connection conn = null;
		
		PreparedStatement pstmt = null;
		
		ResultSet resultSet = null;
		
		logger.debug("sql:"+sb);
		
		logger.debug("wkt:"+subtask.getGeometry());
		
		try {
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setString(1, subtask.getGeometry());
			
			resultSet = pstmt.executeQuery();
			
			JSONObject resutlObj = new JSONObject();
			
			if (resultSet.next()) {
				resutlObj.put("detail", resultSet.getInt("detail"));
				resutlObj.put("parking", resultSet.getInt("parking"));
				resutlObj.put("carrental", resultSet.getInt("carrental"));
			}
			
			logger.debug("result:"+resutlObj);
			
			return resutlObj;
		}catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeConnection(conn);
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
}
