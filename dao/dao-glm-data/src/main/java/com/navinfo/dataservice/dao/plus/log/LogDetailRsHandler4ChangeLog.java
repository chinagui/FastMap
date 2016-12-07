package com.navinfo.dataservice.dao.plus.log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;

/** 只能处理单个object类型
 * @ClassName: LogDetailRsHandler4ChangeLog
 * @author xiaoxiaowen4127
 * @date 2016年12月5日
 * @Description: LogDetailRsHandler4ChangeLog.java
 */
public class LogDetailRsHandler4ChangeLog implements ResultSetHandler<Map<Long,List<LogDetail>>> {

	@Override
	public Map<Long,List<LogDetail>> handle(ResultSet rs) throws SQLException {
		Map<Long,List<LogDetail>> result = new HashMap<Long,List<LogDetail>>();
		while(rs.next()){
			LogDetail ld = new LogDetail();
			ld.setObNm(rs.getString("OB_NM"));
			ld.setObPid(rs.getLong("OB_PID"));
			ld.setTbNm(rs.getString("TB_NM"));
			ld.setOld(rs.getString("OLD"));
			ld.setNew(rs.getString("NEW"));
			ld.setFdLst(rs.getString("FD_LST"));
			ld.setOpTp(rs.getInt("OP_TP"));
			ld.setTbRowId(rs.getString("TB_ROW_ID"));
			List<LogDetail> details = result.get(ld.getObPid());
			if(details==null){
				details = new ArrayList<LogDetail>();
				result.put(ld.getObPid(),details);
			}
			details.add(ld);
		}
		return result;
	}

}
