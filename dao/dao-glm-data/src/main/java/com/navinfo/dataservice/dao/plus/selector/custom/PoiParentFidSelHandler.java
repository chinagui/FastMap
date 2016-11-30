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
public class PoiParentFidSelHandler implements ResultSetHandler<Map<Long, String>> {

	@Override
	public Map<Long, String> handle(ResultSet rs) throws SQLException {
		Map<Long,String> res = new HashMap<Long,String>();
		while(rs.next()){
			res.put(rs.getLong("CHILD_POI_PID"), rs.getString("POI_NUM"));
		}
		return res;
	}
}
