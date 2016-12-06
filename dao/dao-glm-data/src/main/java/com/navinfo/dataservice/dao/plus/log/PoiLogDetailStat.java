package com.navinfo.dataservice.dao.plus.log;

import java.sql.Clob;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * 只统计POI的履历
 * @ClassName: LogStat
 * @author xiaoxiaowen4127
 * @date 2016年12月5日
 * @Description: LogStat.java
 */
public class PoiLogDetailStat {

	/**
	 * 对应日落月的履历统计
	 * @param conn
	 * @param tempOpTable：履历操作id的临时表
	 * @return
	 * @throws Exception
	 */
	public static Map<Long,List<LogDetail>> loadByOperation(Connection conn,String tempOpTable)throws Exception{
		if(StringUtils.isEmpty(tempOpTable))return null;
		String sql = "SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID FROM LOG_DETAIL T,"+tempOpTable+" TEMP "
				+ "WHERE T.OP_ID=TEMP.OP_ID";
		return new QueryRunner().query(conn, sql, new LogDetailRsHandler4ChangeLog());
	}
	/**
	 * 对应精编提交的履历统计
	 * ？提交按一级作业项
	 * ？有没有日落月后某个一级精编作业项不需要作业的？不需要作业的是否直接打上已提交
	 * ？按作业项
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long,List<LogDetail>> loadByColEditStatus(Connection conn,Collection<Long> pids)throws Exception{
		if(pids==null||pids.size()==0)return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID FROM LOG_DETAIL T,LOG_OPERATION LP,POI_COLUMN_STATUS P WHERE T.OP_ID=LP.OP_ID AND T.OB_PID=P.PID");
		sb.append(" AND LP.OP_DT>=P.APPLY_DATE");
		sb.append(" AND T.OB_NM='"+ObjectName.IX_POI+"'");
		if(pids.size()>1000){
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(pids,","));
			sb.append(" AND P.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog(),clob);
		}else{
			sb.append(" AND P.PID IN ("+StringUtils.join(pids,",")+")");
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog());
		}
	}
}
