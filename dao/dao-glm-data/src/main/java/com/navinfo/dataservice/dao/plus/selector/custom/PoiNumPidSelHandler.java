package com.navinfo.dataservice.dao.plus.selector.custom;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/** 
 * @ClassName: PoiNumPidSelHandler
 * @author xiaoxiaowen4127
 * @date 2016年11月28日
 * @Description: PoiNumPidSelHandler.java
 */
public class PoiNumPidSelHandler implements ResultSetHandler<Map<String, Long>> {

	@Override
	public Map<String, Long> handle(ResultSet rs) throws SQLException {
		Map<String,Long> res = new HashMap<String,Long>();
		while(rs.next()){
			res.put(rs.getString("POI_NUM"), rs.getLong("PID"));
		}
		return res;
	}
}
