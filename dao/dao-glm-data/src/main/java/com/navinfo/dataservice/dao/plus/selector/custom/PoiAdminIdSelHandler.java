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
public class PoiAdminIdSelHandler implements ResultSetHandler<Map<Long, Long>> {

	@Override
	public Map<Long, Long> handle(ResultSet rs) throws SQLException {
		Map<Long,Long> res = new HashMap<Long,Long>();
		while(rs.next()){
			res.put(rs.getLong("PID"), rs.getLong("ADMIN_ID"));
		}
		return res;
	}
}
