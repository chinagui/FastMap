package com.navinfo.dataservice.dao.plus.log;

import java.sql.Clob;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.mysql.fabric.xmlrpc.base.Array;
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
public class SamepoiLogDetailStat {
	
	/**
	 * 对应行编提交的履历统计
	 *
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long,List<LogDetail>> loadByRowEditStatus(Connection conn,Collection<Long> pids)throws Exception{
		if(pids==null||pids.size()==0)return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T,LOG_OPERATION LP,POI_EDIT_STATUS P"
				+ " WHERE T.OP_ID=LP.OP_ID "
				+ " AND T.OB_NM='"+ObjectName.IX_SAMEPOI+"'"
				+ " AND T.GEO_NM='"+ObjectName.IX_POI+"'"
				+ "   AND T.GEO_PID=P.PID");
		//若P.SUBMIT_DATE最后一次提交时间为空，则，取poi的全部履历；否则取SUBMIT_DATE最后一次提交时间之后的所有履历。
		sb.append(" AND ((LP.OP_DT>=P.SUBMIT_DATE AND P.SUBMIT_DATE IS NOT NULL) OR P.SUBMIT_DATE IS NULL)");
		
		List<Object> values=new ArrayList<Object>();
		if(pids!=null && pids.size()>0){
			if(pids.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids,","));
				sb.append(" AND P.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			}else{
				sb.append(" AND P.PID IN ("+StringUtils.join(pids,",")+")");
			}}
		if(values!=null&&values.size()>0){
			Object[] queryValues=new Object[values.size()];
			for(int i=0;i<values.size();i++){
				queryValues[i]=values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog(),queryValues);
		}else{
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog());
		}
	}
	
	/**
	 * 对应行编提交的履历统计
	 *
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	public static Map<Long,List<LogDetail>> loadAllLog(Connection conn,Collection<Long> pids)throws Exception{
		if(pids==null||pids.size()==0)return null;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT T.OB_NM,T.OB_PID,T.TB_NM,T.OLD,T.NEW,T.FD_LST,T.OP_TP,T.TB_ROW_ID "
				+ "FROM LOG_DETAIL T"
				+ " WHERE T.OB_NM='"+ObjectName.IX_POI+"'");
		
		List<Object> values=new ArrayList<Object>();
		if(pids!=null && pids.size()>0){
			if(pids.size()>1000){
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids,","));
				sb.append(" AND T.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				values.add(clob);
			}else{
				sb.append(" AND T.OB_PID IN ("+StringUtils.join(pids,",")+")");
			}}
		if(values!=null&&values.size()>0){
			Object[] queryValues=new Object[values.size()];
			for(int i=0;i<values.size();i++){
				queryValues[i]=values.get(i);
			}
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog(),queryValues);
		}else{
			return new QueryRunner().query(conn, sb.toString(), new LogDetailRsHandler4ChangeLog());
		}
	}
}
