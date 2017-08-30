package com.navinfo.dataservice.dao.log;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: LogOpTypeStat
 * @author xiaoxiaowen4127
 * @date 2017年8月28日
 * @Description: LogOpTypeStat.java
 */
public class LogOpTypeStat {

	private Logger log = LoggerRepos.getLogger(this.getClass());
	private Connection conn;
	private QueryRunner run;
	
	public LogOpTypeStat(Connection conn){
		this.conn=conn;
		run = new QueryRunner();
	}
	
	public Collection<Long> getUpdatedObj(String objName,String startDate,String endDate)throws SQLException{
		return getUpdatedObjByPids(objName,null,startDate,endDate);
	}
	
	public Collection<Long> getUpdatedObjByGrids(String objName,Collection<String> grids,String startDate,String endDate)throws SQLException{
		if(grids==null||grids.size()==0){
			return getUpdatedObjByPids(objName,null,startDate,endDate);
		}
		Collection<Long> updPids = null;
		StringBuilder sb = new StringBuilder();
		//是否有时间
		if(StringUtils.isNotEmpty(startDate)||StringUtils.isNotEmpty(endDate)){//时间段有效
			sb.append("SELECT DISTINCT D.OB_PID FROM LOG_OPERATION O,LOG_DETAIL D,LOG_DETAIL_GRID G WHERE O.OP_ID=D.OP_ID AND D.ROW_ID=G.LOG_ROW_ID");
			if(StringUtils.isNotEmpty(startDate)){
				sb.append("     AND O.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
			}
			if(StringUtils.isNotEmpty(endDate)){
				sb.append("     AND O.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
			}
		}else{//无时间段，取所有履历
			sb.append("SELECT DISTINCT D.OB_PID FROM LOG_DETAIL D,LOG_DETAIL_GRID G WHERE D.ROW_ID=G.LOG_ROW_ID");
		}
		if(grids.size()>1000){
			sb.append(" AND G.GRID_ID IN (select to_number(column_value) from table(clob_to_table(?))) AND G.GRID_TYPE = 1 AND D.OB_NM=?");
			Clob clob=ConnectionUtil.createClob(conn);
			clob.setString(1, StringUtils.join(grids, ","));
			updPids = run.query(conn, sb.toString(), new SingleOpTypeHandler(),clob,objName);
		}else{
			sb.append(" AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1 AND D.OB_NM=?");
			updPids = run.query(conn, sb.toString(), new SingleOpTypeHandler(),objName);
		}
		return updPids;
	}
	
	public Collection<Long> getUpdatedObjByPids(String objName,Collection<Long> pids,String startDate,String endDate)throws SQLException{
		Collection<Long> updPids = null;
		StringBuilder sb = new StringBuilder();
		//是否有时间
		if(StringUtils.isNotEmpty(startDate)||StringUtils.isNotEmpty(endDate)){//时间段有效
			sb.append("SELECT DISTINCT D.OB_PID FROM LOG_OPERATION O,LOG_DETAIL D WHERE O.OP_ID=D.OP_ID");
			if(StringUtils.isNotEmpty(startDate)){
				sb.append("     AND O.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
			}
			if(StringUtils.isNotEmpty(endDate)){
				sb.append("     AND O.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
			}
		}else{//无时间段，取所有履历
			sb.append("SELECT DISTINCT D.OB_PID FROM LOG_DETAIL D WHERE 1=1");
		}
		if(pids==null||pids.size()==0){
			sb.append(" AND D.OB_NM=?");
			updPids = run.query(conn, sb.toString(), new SingleOpTypeHandler(),objName);
		}else{
			if(pids.size()>1000){
				sb.append(" AND D.OB_PID IN (select to_number(column_value) from table(clob_to_table(?))) AND D.OB_NM=?");
				Clob clob=ConnectionUtil.createClob(conn);
				clob.setString(1, StringUtils.join(pids, ","));
				updPids = run.query(conn, sb.toString(), new SingleOpTypeHandler(),clob,objName);
			}else{
				sb.append(" AND D.OB_PID IN ("+StringUtils.join(pids, ",")+") AND D.OB_NM=?");
				updPids = run.query(conn, sb.toString(), new SingleOpTypeHandler(),objName);
			}
		}
		return updPids;
	}
	
	public Map<Integer,Collection<Long>> getOpType(String objName,String mainTabName,String startDate,String endDate)throws SQLException{
		return getOpTypeByPids(objName,mainTabName,null,startDate,endDate);
	}
	
	/**
	 * 按Grids范围计算有履历的对象，在给定时间段内的新增删除修改状态
	 * Grids为空，则返回时间段范围内所有有变更的对象
	 * 给定有意义的时间段则按时间段，未给定则按所有履历
	 * @param objName
	 * @param mainTabName
	 * @param grids
	 * @param startDate
	 * @param endDate
	 * @return key:op_type,value:obj pid collection
	 * @throws SQLException
	 */
	public Map<Integer,Collection<Long>> getOpTypeByGrids(String objName,String mainTabName,Collection<String> grids,String startDate,String endDate)throws SQLException{
		Map<Integer,Collection<Long>> allPids = new HashMap<Integer,Collection<Long>>();
		if(grids==null||grids.size()==0){
			return getOpTypeByPids(objName,mainTabName,null,startDate,endDate);
		}
		//1. 查询所有的变更的数据，赋默认状态3
		Collection<Long> updPids = getUpdatedObjByGrids(objName,grids,startDate,endDate);
		
		if(updPids.size()==0){
			return allPids;
		}
		//2. 查询为新增和删除状态的对象
		Map<Integer,Collection<Long>> insdelPids = null;
		StringBuilder sb2 = new StringBuilder();
		sb2.append("WITH A AS (");
		sb2.append("SELECT D.OB_PID,D.OP_TP,O.OP_DT FROM LOG_OPERATION O,LOG_DETAIL D,LOG_DETAIL_GRID G WHERE O.OP_ID=D.OP_ID AND D.ROW_ID=G.LOG_ROW_ID");
		if(StringUtils.isNotEmpty(startDate)){
			sb2.append("     AND O.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
		}
		if(StringUtils.isNotEmpty(endDate)){
			sb2.append("     AND O.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
		}
		if(grids.size()>1000){
			sb2.append(" AND G.GRID_ID IN (select to_number(column_value) from table(clob_to_table(?))) AND G.GRID_TYPE = 1");
			sb2.append(" AND D.OP_TP IN (1,2) AND D.OB_NM=? AND D.TB_NM=?");
			sb2.append(") SELECT A.OB_PID,A.OP_TP FROM A,(SELECT OB_PID,MAX(OP_DT) OP_DT FROM A GROUP BY OB_PID) B WHERE A.OB_PID=B.OB_PID AND A.OP_DT=B.OP_DT");
			Clob clob2=ConnectionUtil.createClob(conn);
			clob2.setString(1, StringUtils.join(grids, ","));
			insdelPids = run.query(conn, sb2.toString(), new OpTypeHandler(),clob2,objName,mainTabName);
		}else{
			sb2.append(" AND G.GRID_ID IN ("+StringUtils.join(grids, ",")+") AND G.GRID_TYPE = 1");
			sb2.append(" AND D.OP_TP IN (1,2) AND D.OB_NM=? AND D.TB_NM=?");
			sb2.append(") SELECT A.OB_PID,A.OP_TP FROM A,(SELECT OB_PID,MAX(OP_DT) OP_DT FROM A GROUP BY OB_PID) B WHERE A.OB_PID=B.OB_PID AND A.OP_DT=B.OP_DT");
			insdelPids = run.query(conn, sb2.toString(), new OpTypeHandler(),objName,mainTabName);
		}
		//添加新增删除的。
		allPids.putAll(insdelPids);
		//updPids中去除新增删除的
		if(insdelPids.containsKey(1)){
			updPids.removeAll(insdelPids.get(1));//过滤新增的
		}
		if(insdelPids.containsKey(2)){
			updPids.removeAll(insdelPids.get(2));//过滤删除的
		}
		if(updPids.size()>0){
			allPids.put(3, updPids);
		}
		return allPids;
	}

	/**
	 * 按pids计算有履历的对象，在给定时间段内的新增删除修改状态
	 * 如果pids集合为空，则返回时间段范围内所有有变更的对象
	 * 给定有意义的时间段则按时间段，未给定则按所有履历
	 * @param objName
	 * @param mainTabName
	 * @param pids
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws SQLException
	 */
	public Map<Integer,Collection<Long>> getOpTypeByPids(String objName,String mainTabName,Collection<Long> pids,String startDate,String endDate)throws SQLException{
		Map<Integer,Collection<Long>> allPids = new HashMap<Integer,Collection<Long>>();
		//1. 查询所有的变更的数据
		Collection<Long> updPids = getUpdatedObjByPids(objName,pids,startDate,endDate);
		
		if(updPids.size()==0){
			return allPids;
		}
		//2. 查询为新增和删除状态的对象
		Map<Integer,Collection<Long>> insdelPids = null;
		StringBuilder sb2 = new StringBuilder();
		sb2.append("WITH A AS (");
		sb2.append("SELECT D.OB_PID,D.OP_TP,O.OP_DT FROM LOG_OPERATION O,LOG_DETAIL D WHERE O.OP_ID=D.OP_ID");
		if(StringUtils.isNotEmpty(startDate)){
			sb2.append("     AND O.OP_DT > TO_DATE('"+startDate+"', 'yyyymmddhh24miss')");
		}
		if(StringUtils.isNotEmpty(endDate)){
			sb2.append("     AND O.OP_DT <= TO_DATE('"+endDate+"', 'yyyymmddhh24miss')");
		}
		if(pids==null||pids.size()==0){
			sb2.append(" AND D.OP_TP IN (1,2) AND D.OB_NM=? AND D.TB_NM=?");
			sb2.append(") SELECT A.OB_PID,A.OP_TP FROM A,(SELECT OB_PID,MAX(OP_DT) OP_DT FROM A GROUP BY OB_PID) B WHERE A.OB_PID=B.OB_PID AND A.OP_DT=B.OP_DT");
			insdelPids = run.query(conn, sb2.toString(), new OpTypeHandler(),objName,mainTabName);
		}else{
			if(pids.size()>1000){
				sb2.append(" AND D.OB_PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				sb2.append(" AND D.OP_TP IN (1,2) AND D.OB_NM=? AND D.TB_NM=?");
				sb2.append(") SELECT A.OB_PID,A.OP_TP FROM A,(SELECT OB_PID,MAX(OP_DT) OP_DT FROM A GROUP BY OB_PID) B WHERE A.OB_PID=B.OB_PID AND A.OP_DT=B.OP_DT");
				Clob clob2=ConnectionUtil.createClob(conn);
				clob2.setString(1, StringUtils.join(pids, ","));
				insdelPids = run.query(conn, sb2.toString(), new OpTypeHandler(),clob2,objName,mainTabName);
			}else{
				sb2.append(" AND D.OB_PID IN ("+StringUtils.join(pids, ",")+")");
				sb2.append(" AND D.OP_TP IN (1,2) AND D.OB_NM=? AND D.TB_NM=?");
				sb2.append(") SELECT A.OB_PID,A.OP_TP FROM A,(SELECT OB_PID,MAX(OP_DT) OP_DT FROM A GROUP BY OB_PID) B WHERE A.OB_PID=B.OB_PID AND A.OP_DT=B.OP_DT");
				insdelPids = run.query(conn, sb2.toString(), new OpTypeHandler(),objName,mainTabName);
			}
		}
		//添加新增删除的。
		allPids.putAll(insdelPids);
		//updPids中去除新增删除的
		if(insdelPids.containsKey(1)){
			updPids.removeAll(insdelPids.get(1));//过滤新增的
		}
		if(insdelPids.containsKey(2)){
			updPids.removeAll(insdelPids.get(2));//过滤删除的
		}
		if(updPids.size()>0){
			allPids.put(3, updPids);
		}
		return allPids;
	}
	
	/**
	 * 只取新增删除状态
	 * @ClassName: OpTypeHandler
	 * @author xiaoxiaowen4127
	 * @date 2017年8月29日
	 * @Description: LogOpTypeStat.java
	 */
	class OpTypeHandler implements ResultSetHandler<Map<Integer,Collection<Long>>>{
		@Override
		public Map<Integer, Collection<Long>> handle(ResultSet rs) throws SQLException {
			Map<Integer, Collection<Long>> results = new HashMap<Integer,Collection<Long>>();
			Collection<Long> addPids = new HashSet<Long>();
			Collection<Long> delPids = new HashSet<Long>();
			while(rs.next()){
				int status = rs.getInt("OP_TP");
				if(status==1){
					addPids.add(rs.getLong("OB_PID"));
				}else if(status==2){
					delPids.add(rs.getLong("OB_PID"));
				}
			}
			if(addPids.size()>0){
				results.put(1,addPids);
			}
			if(delPids.size()>0){
				results.put(2, delPids);
			}
			return results;
		}
		
	}
	class SingleOpTypeHandler implements ResultSetHandler<Collection<Long>>{
		@Override
		public Collection<Long> handle(ResultSet rs) throws SQLException {
			Collection<Long> pids = new HashSet<Long>();
			while(rs.next()){
				pids.add(rs.getLong(1));
			}
			return pids;
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Connection conn = null;
		try{
			conn = MultiDataSourceFactory.getInstance().getDriverManagerDataSource(
					"ORACLE", "oracle.jdbc.driver.OracleDriver", "jdbc:oracle:thin:@192.168.3.103:1521/orcl", "fm280_regionsp7_17win_d_1", "fm280_regionsp7_17win_d_1").getConnection();
			LogOpTypeStat stat = new LogOpTypeStat(conn);
			Set<String> grids = new HashSet<String>();
			grids.add("60560321");
			grids.add("60561200");
			Map<Integer,Collection<Long>> results = stat.getOpTypeByGrids(ObjectName.IX_POI, ObjectName.IX_POI, grids, "20170805000000", "20170805230000");
			for(Entry<Integer,Collection<Long>> entry:results.entrySet()){
				System.out.println(entry.getKey()+":"+StringUtils.join(entry.getValue(),","));
			}
			Set<Long> pids = new HashSet<Long>();
			pids.add(96428903L);
			pids.add(404000001L);
			pids.add(502000001L);
			pids.add(505000001L);
			Map<Integer,Collection<Long>> results2 = stat.getOpTypeByPids(ObjectName.IX_POI, ObjectName.IX_POI, pids, "", "");
			for(Entry<Integer,Collection<Long>> entry:results2.entrySet()){
				System.out.println(entry.getKey()+":"+StringUtils.join(entry.getValue(),","));
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			DbUtils.closeQuietly(conn);
		}
	}

}
