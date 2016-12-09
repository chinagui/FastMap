package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;

public class poiEditStatusHandler implements ResultSetHandler<Map<Long,Integer>>{

	@Override
	public Map<Long,Integer> handle(ResultSet rs) throws SQLException {
		Map<Long,Integer> editStatusMap = new HashMap<Long,Integer>();
		
		try {
			while (rs.next()){
				editStatusMap.put(rs.getLong("pid"), rs.getInt("commit_his_status"));
			}
			return editStatusMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
