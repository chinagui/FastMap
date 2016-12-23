package com.navinfo.dataservice.dao.glm.selector.poi.index;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiEditStatus;
import com.navinfo.dataservice.dao.glm.selector.AbstractSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

public class IxPoiEditStatusSelector extends AbstractSelector {

	private Connection conn;

	public IxPoiEditStatusSelector(Connection conn) {
		super(conn);
		this.conn = conn;
		this.setCls(IxPoiEditStatus.class);
	}

	/**
	 * 根据poi的pid查询poi状态表的字段值
	 * @param pid
	 * @param isLock
	 * @return
	 * @throws Exception
	 */
	public Map<String,Integer> loadStatusByRowId(int pid, boolean isLock) throws Exception {
		//poi作业状态
		int status = 0;
		
		//poi鲜度验证字段
		int freshVerified = 0;
		
		Map<String,Integer> poiEditStatusData = new HashMap<String, Integer>();
		
		String sql = "select status,FRESH_VERIFIED from poi_edit_status where pid=:1 ";

		if (isLock) {
			sql += " for update nowait";
		}

		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql);

			pstmt.setInt(1, pid);

			resultSet = pstmt.executeQuery();

			if (resultSet.next()) {
				status = resultSet.getInt("status");
				
				freshVerified = resultSet.getInt("FRESH_VERIFIED");
			} 
			poiEditStatusData.put("status", status);
			
			poiEditStatusData.put("freshVerified", freshVerified);
		} catch (Exception e) {

			throw e;

		} finally {

			DBUtils.closeResultSet(resultSet);

			DBUtils.closeStatement(pstmt);
		}
		return poiEditStatusData;
	}
}
