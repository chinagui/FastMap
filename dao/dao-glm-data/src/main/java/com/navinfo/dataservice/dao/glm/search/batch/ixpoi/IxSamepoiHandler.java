package com.navinfo.dataservice.dao.glm.search.batch.ixpoi;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.dbutils.ResultSetHandler;

public class IxSamepoiHandler implements ResultSetHandler<Map<Long,String>>{

	@Override
	public Map<Long,String> handle(ResultSet rs) throws SQLException {
		Map<Long,String> samepoiMap = new HashMap<Long,String>();
		
		try {
			while (rs.next()){
				samepoiMap.put(rs.getLong("pid"), rs.getString("poi_num"));
				
			}
			return samepoiMap;
		} catch (Exception e) {
			throw e;
		}
	}
}
