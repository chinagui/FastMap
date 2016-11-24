package com.navinfo.dataservice.control.row.poiEditStatus;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.navicommons.database.QueryRunner;


/** 
 * @ClassName: PoiEditStatus
 * @author songdongyan
 * @date 2016年11月22日
 * @Description: PoiEditStatus.java
 */
public class PoiEditStatus {

	private static final Logger logger = Logger.getLogger(PoiEditStatus.class);
	/**
	 * 
	 */
	public PoiEditStatus() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 删选满足作业状态的pid
	 * @param conn
	 * @param fidMap//{变更类型：pid列表}。变更类型：1 新增；2 删除；3 修改
	 * @param status//作业状态。0: 未作业；1：待作业,2：已作业,3：已提交
	 * @return
	 * @throws Exception 
	 */
	public static Map<Integer,Collection<Long>> pidFilterByEditStatus(Connection conn
			,Map<Integer,Collection<Long>> fidMap,int status) throws Exception{
		
		try{
			for(Map.Entry<Integer,Collection<Long>> entry:fidMap.entrySet()){
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT T.PID FROM POI_EDIT_STATUS T WHERE T.STATUS = " + status);
				Collection<Long> pids = entry.getValue();
				Clob clobPids=null;
				if(pids.isEmpty()){
					continue;
				}
				if(pids.size()>1000){
					clobPids = conn.createClob();
					clobPids.setString(1, StringUtils.join(pids, ","));
					sb.append(" AND T.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
				}else{
					sb.append(" AND T.PID IN (" + StringUtils.join(pids, ",") + ")");
				}
				
				ResultSetHandler<Collection<Long>> rsHandler = new ResultSetHandler<Collection<Long>>() {
					public Collection<Long> handle(ResultSet rs) throws SQLException {
						Collection<Long> result = new ArrayList<Long>();
						while (rs.next()) {
							result.add(rs.getLong("PID"));
						}
						return result;
					}
		
				};
				Collection<Long> filteredPids = new ArrayList<Long>();
				if(clobPids==null){
					filteredPids = new QueryRunner().query(conn, sb.toString(), rsHandler);
				}else{
					filteredPids = new QueryRunner().query(conn, sb.toString(), rsHandler,clobPids);
				}
				fidMap.put(entry.getKey(), filteredPids);			
			}
			return fidMap;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(), e);
			throw new Exception("过滤pid失败:" + e.getMessage(), e);
		}
	}
	
	public static void tagMultiSrcPoi(Connection conn,Map<Long,String> map) throws Exception{
		try{
			Set<Long> pids = map.keySet();
			if(pids.isEmpty()){
				return;
			}
			//poi_edit_status中已存在的，更新其状态
			Set<Long> pidExistsInPoiEditStatus = selectPidExistsInPoiEditStatus(conn,pids);
			//poi_edit_status中不存在的
			Set<Long> pidNotExistsInPoiEditStatus = new HashSet<Long>();
			pidNotExistsInPoiEditStatus.addAll(pids);
			pidNotExistsInPoiEditStatus.removeAll(pidExistsInPoiEditStatus);
			int status = 1;//待作业
			int isUpload = 1;
			Date uploadDate = new Date();
			int workType = 2;//多源
			if(!pidExistsInPoiEditStatus.isEmpty()){
				updatePoiEditStatus(conn,pidExistsInPoiEditStatus,status,isUpload,uploadDate,workType);
				//存在于poi_edit_status,poi_edit_multisrc中的
				Set<Long> pidExistsInPoiEditMultiSrc = selectPidExistsInPoiEditMultiSrc(conn,pidExistsInPoiEditStatus);
				if(!pidExistsInPoiEditMultiSrc.isEmpty()){
					updatePoiEditMultiSrc(conn,pidExistsInPoiEditMultiSrc,map);				
				}
				//存在于poi_edit_status表内,不存在于poi_edit_multisrc表中的pid
				pidExistsInPoiEditStatus.removeAll(pidExistsInPoiEditMultiSrc);
				if(!pidExistsInPoiEditStatus.isEmpty()){
					insertPoiEditMultiSrc(conn,pidExistsInPoiEditStatus,map);
				}
			}
			
			//不存在于poi_edit_status表内的pid，插入poi_edit_status,poi_edit_multisrc
			if(!pidNotExistsInPoiEditStatus.isEmpty()){
				insertPoiEditStatus(conn,pidNotExistsInPoiEditStatus,status,isUpload,uploadDate,workType);
				insertPoiEditMultiSrc(conn,pidNotExistsInPoiEditStatus,map);
			}
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}

	/**
	 * @param conn
	 * @param pidExistsInPoiEditMultiSrc
	 * @param map
	 * @throws Exception 
	 */
	private static void updatePoiEditMultiSrc(Connection conn, Set<Long> pids,
			Map<Long, String> map) throws Exception {
		try{
			if(pids.isEmpty()||map.isEmpty()){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE POI_EDIT_MULTISRC SET ");
			sb.append("SOURCE_TYPE=?");
			sb.append(",MAIN_TYPE=?");
			sb.append(" WHERE PID=?");
			
			Object[][] values = new Object[pids.size()][];
			int i = 0;
			for(long pid:pids){
				String sourceType = map.get(pid);
				int mainType = 0;
				if(sourceType.equals("001000020000")){
					mainType = 1;
				}else if(sourceType.equals("001000030000")||sourceType.equals("001000030001")||sourceType.equals("001000030002")){
					mainType = 2;
				}else if(sourceType.equals("001000030003")||sourceType.equals("001000030004")){
					mainType = 3;
				}
				Object[] value = {sourceType,mainType,pid};
				values[i] = value;
				i++;
			}
			new QueryRunner().batch(conn, sb.toString(),values);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
		
	}

	/**
	 * @param conn
	 * @param pids
	 * @param map
	 * @throws Exception 
	 */
	private static void insertPoiEditMultiSrc(Connection conn, Set<Long> pids, Map<Long, String> map) throws Exception {
		try{
			if(pids.isEmpty()||map.isEmpty()){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO POI_EDIT_MULTISRC (PID,SOURCE_TYPE,MAIN_TYPE) VALUES (?,?,?)");

			Object[][] values = new Object[pids.size()][];
			int i = 0;
			for(long pid:pids){
				String sourceType = map.get(pid);
				int mainType = 0;
				if(sourceType.equals("001000020000")){
					mainType = 1;
				}else if(sourceType.equals("001000030000")||sourceType.equals("001000030001")||sourceType.equals("001000030002")){
					mainType = 2;
				}else if(sourceType.equals("001000030003")||sourceType.equals("001000030004")){
					mainType = 3;
				}
				Object[] value = {pid,sourceType,mainType};
				values[i] = value;
				i++;
			}
			new QueryRunner().batch(conn, sb.toString(),values);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
		
	}

	/**
	 * @param conn
	 * @param pids
	 * @param status
	 * @param isUpload
	 * @param uploadDate
	 * @param workType
	 * @throws Exception 
	 */
	private static void insertPoiEditStatus(Connection conn, Set<Long> pids, int status, int isUpload, Date uploadDate,
			int workType) throws Exception {
		try{
			if(pids.isEmpty()){
				return;
			}
			StringBuilder sb = new StringBuilder();
//			sb.append("INSERT INTO POI_EDIT_STATUS (PID,STATUS,IS_UPLOAD,UPLOAD_DATE,WORK_TYPE) VALUES (?,?,?,TO_DATE(?,'yyyy-MM-dd HH24:MI:ss'),?)");
			sb.append("INSERT INTO POI_EDIT_STATUS (ROW_ID,PID,STATUS,IS_UPLOAD,UPLOAD_DATE,WORK_TYPE) VALUES (?,?,?,?,TO_DATE(?,'yyyy-MM-dd HH24:MI:ss'),?)");

			DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Object[][] values = new Object[pids.size()][];
			int i = 0;
			for(long pid:pids){
//				Object[] value = {pid,status,isUpload,uploadDate.toString().substring(0, 10),workType};
				Object[] value = {String.valueOf(pid),pid,status,isUpload,format.format(uploadDate),workType};
				values[i] = value;
				i++;
			}
			new QueryRunner().batch(conn, sb.toString(),values);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}

	/**
	 * @param conn
	 * @param pidExistsInPoiEditStatus
	 * @return
	 * @throws Exception 
	 */
	private static Set<Long> selectPidExistsInPoiEditMultiSrc(Connection conn, Set<Long> pids) throws Exception {
		// TODO Auto-generated method stub
		try{
			Set<Long> pidExistsInPoiEditMultiSrc = new HashSet<Long>();
			if(pids.isEmpty()){
				return pidExistsInPoiEditMultiSrc;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT T.PID FROM POI_EDIT_MULTISRC T WHERE ");
			Clob clobPids=null;
			if(pids.size()>1000){
				clobPids = conn.createClob();
				clobPids.setString(1, StringUtils.join(pids, ","));
				sb.append("T.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			}else{
				sb.append("T.PID IN (" + StringUtils.join(pids, ",") + ")");
			}
			
			ResultSetHandler<Set<Long>> rsHandler = new ResultSetHandler<Set<Long>>() {
				public Set<Long> handle(ResultSet rs) throws SQLException {
					Set<Long> result = new HashSet<Long>();
					while (rs.next()) {
						result.add(rs.getLong("PID"));
					}
					return result;
				}
	
			};
			if(clobPids==null){
				pidExistsInPoiEditMultiSrc = new QueryRunner().query(conn, sb.toString(), rsHandler);
			}else{
				pidExistsInPoiEditMultiSrc = new QueryRunner().query(conn, sb.toString(), rsHandler,clobPids);
			}
			return pidExistsInPoiEditMultiSrc;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}

	/**
	 * @param conn
	 * @param pidExistsInPoiEditStatus
	 * @throws Exception 
	 */
	private static void updatePoiEditStatus(Connection conn, Set<Long> pids
			,int status,int isUpload, Date uploadDate,int workType) throws Exception {
		try{
			if(pids.isEmpty()){
				return;
			}
			DateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			StringBuilder sb = new StringBuilder();
			sb.append("UPDATE POI_EDIT_STATUS T SET STATUS="+status);
			sb.append(",IS_UPLOAD="+isUpload);
			sb.append(",UPLOAD_DATE=TO_DATE('" + format.format(uploadDate) + "','yyyy-MM-dd HH24:MI:ss')");
			sb.append(",WORK_TYPE="+workType);
			Clob clobPids=null;
			if(pids.size()>1000){
				clobPids = conn.createClob();
				clobPids.setString(1, StringUtils.join(pids, ","));
				sb.append(" WHERE T.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			}else{
				sb.append(" WHERE T.PID IN (" + StringUtils.join(pids, ",") + ")");
			}

			if(clobPids==null){
				new QueryRunner().update(conn, sb.toString());
			}else{
				new QueryRunner().update(conn, sb.toString(),clobPids);
			}
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}

	/**
	 * 存在于poi_edit_status表内的pid
	 * @param conn
	 * @param pids
	 * @return
	 * @throws Exception 
	 */
	private static Set<Long> selectPidExistsInPoiEditStatus(Connection conn, Set<Long> pids) throws Exception {
		// TODO Auto-generated method stub
		try{
			Set<Long> pidExistsInPoiEditStatus = new HashSet<Long>();
			if(pids.isEmpty()){
				return pidExistsInPoiEditStatus;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT T.PID FROM POI_EDIT_STATUS T WHERE ");
			Clob clobPids=null;
			if(pids.size()>1000){
				clobPids = conn.createClob();
				clobPids.setString(1, StringUtils.join(pids, ","));
				sb.append("T.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
			}else{
				sb.append("T.PID IN (" + StringUtils.join(pids, ",") + ")");
			}
			
			ResultSetHandler<Set<Long>> rsHandler = new ResultSetHandler<Set<Long>>() {
				public Set<Long> handle(ResultSet rs) throws SQLException {
					Set<Long> result = new HashSet<Long>();
					while (rs.next()) {
						result.add(rs.getLong("PID"));
					}
					return result;
				}
	
			};
			if(clobPids==null){
				pidExistsInPoiEditStatus = new QueryRunner().query(conn, sb.toString(), rsHandler);
			}else{
				pidExistsInPoiEditStatus = new QueryRunner().query(conn, sb.toString(), rsHandler,clobPids);
			}
			return pidExistsInPoiEditStatus;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}

	public static void main(String[] args) throws Exception{
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@192.168.4.61:1521/orcl",
				"fm_regiondb_sp6_d_1", "fm_regiondb_sp6_d_1");
		Map<Integer,Collection<Long>> pids = new HashMap<Integer,Collection<Long>>();
		Collection<Long> insertList = new ArrayList<Long>();
		insertList.add((long) 1);
		Collection<Long> deleteList = new ArrayList<Long>();
		deleteList.add((long) 2);
		Collection<Long> updateList = new ArrayList<Long>();
		updateList.add((long) 3);
		pids.put(1, insertList);
		pids.put(2, deleteList);
		pids.put(3, updateList);

		Map<Integer,Collection<Long>> pids2 = pidFilterByEditStatus(conn,pids,3);

		System.out.println("ok");
	}
	
}
