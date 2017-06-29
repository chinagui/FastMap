package com.navinfo.dataservice.dao.plus.selector.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

public class PoiParentPidSelHandler implements ResultSetHandler<Map<Long, Long>> { 
	@Override
	public Map<Long, Long> handle(ResultSet rs) throws SQLException {
		Map<Long,Long> res = new HashMap<Long,Long>();
		while(rs.next()){
			res.put(rs.getLong("CHILD_POI_PID"), rs.getLong("PID"));
		}
		return res;
	}
}
