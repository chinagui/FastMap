package com.navinfo.dataservice.dao.plus.log;

import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: LogStat
 * @author xiaoxiaowen4127
 * @date 2016年12月5日
 * @Description: LogStat.java
 */
public class LogDetailStat {

	public static Map<Long,List<LogDetail>> loadByOperation(Connection conn,Collection<Long> pids,String objectName,String tempOpTable)throws Exception{
		if(StringUtils.isEmpty(tempOpTable))return null;
		String sql = "SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID FROM LOG_DETAIL T,"+tempOpTable+" TEMP WHERE T.OP_ID=TEMP.OP_ID";
		return new QueryRunner().query(conn, sql, new LogDetailRsHandler4ChangeLog());
	}
	public static Map<Long,List<LogDetail>> loadByEditStatus(Connection conn,Collection<Long> pids,String objectName)throws Exception{
		String sql = "SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID FROM LOG_DETAIL T,LOG_OPERATION LP,POI_EDIT_STATUS P WHERE T.OP_ID=LP.OP_ID AND T.OB_PID=P.PID AND LP.OP_DT>=P.APPLY_TIME";
		return new QueryRunner().query(conn, sql, new LogDetailRsHandler4ChangeLog());
	}
}
