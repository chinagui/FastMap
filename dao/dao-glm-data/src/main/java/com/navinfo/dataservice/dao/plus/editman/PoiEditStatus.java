package com.navinfo.dataservice.dao.plus.editman;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.dao.plus.model.basic.OperationType;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectType;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
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
	 * @param pids
	 * @param status//作业状态。0: 未作业；1：待作业,2：已作业,3：已提交
	 * @return
	 * @throws Exception
	 */
	public static Collection<Long> pidFilterByEditStatus(Connection conn
				,Collection<Long> pids,int status) throws Exception{
		
		try{
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT T.PID FROM POI_EDIT_STATUS T WHERE T.STATUS = " + status);
			Clob clobPids=null;
			if(pids.isEmpty()){
				return pids;
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
			return filteredPids;
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
			
			//更新poi_edit_status表
			int status = 1;//待作业
			int isUpload = 1;
			Date uploadDate = new Date();
			int workType = 2;//多源
			updatePoiEditStatus(conn,pids,status,isUpload,uploadDate,workType);
			
			//更新poi_edit_multisrc表
			updatePoiEditMultiSrc(conn,map);
				
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			logger.error(e.getMessage(),e);
			throw new Exception("多源POI打标签失败");
		}
	}
	
	private static void updatePoiEditMultiSrc(Connection conn,Map<Long, String> map) throws Exception {
		try{
			if(map==null||map.isEmpty()){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("MERGE INTO POI_EDIT_MULTISRC A");
			sb.append(" USING (SELECT ? PID FROM DUAL) B");
			sb.append(" ON(A.PID=B.PID)");
			sb.append(" WHEN MATCHED THEN");
			sb.append(" UPDATE SET A.SOURCE_TYPE = ?,A.MAIN_TYPE = ?");
			sb.append(" WHEN NOT MATCHED THEN");
			sb.append(" INSERT (PID,SOURCE_TYPE,MAIN_TYPE) VALUES (?, ?,?)");
			
			
			Object[][] values = new Object[map.keySet().size()][];
			int i = 0;
			for(Map.Entry<Long, String> entry:map.entrySet()){
				String sourceType = entry.getValue();
				int mainType = 0;
				if(sourceType.equals("001000020000")){
					mainType = 1;
				}else if(sourceType.equals("001000030000")||sourceType.equals("001000030001")||sourceType.equals("001000030002")){
					mainType = 2;
				}else if(sourceType.equals("001000030003")||sourceType.equals("001000030004")){
					mainType = 3;
				}
				Object[] value = {entry.getKey(),sourceType,mainType,entry.getKey(),sourceType,mainType};
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
	 * poi_edit_status表中新增记录
	 * @param conn
	 * @param pids
	 * @param status
	 * @param uploadDate
	 * @throws Exception
	 */
	private static void insertPoiEditStatus(Connection conn, Set<Long> pids, int status) throws Exception {
		try{
			if(pids.isEmpty()){
				return;
			}
			StringBuilder sb = new StringBuilder();
			sb.append("INSERT INTO POI_EDIT_STATUS (PID,STATUS) VALUES (?,?)");

			Object[][] values = new Object[pids.size()][];
			int i = 0;
			for(long pid:pids){
				Object[] value = {pid,status};
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


	public static void main(String[] args) throws Exception{

		System.out.println("ok");
	}

	/**
	 * 新增POI对象在poi_edit_status表中新增一条初始状态的记录
	 * @param conn
	 * @param result
	 * @throws Exception 
	 */
	public static void insertPoiEditStatus(Connection conn, OperationResult result) throws Exception {
		// TODO Auto-generated method stub
		Set<Long> pids = new HashSet<Long>();
		for(Entry<Long, BasicObj> entry:result.getObjsMapByType(ObjectType.IX_POI).entrySet()){
			if(entry.getValue().opType().equals(OperationType.INSERT)){
				pids.add(entry.getValue().objPid());
			}
		}
		int status = 0;//默认为初始化状态
		//插入poi_edit_status表记录
		if(pids!=null&&!pids.isEmpty()){
			insertPoiEditStatus(conn,pids,status);
		}
	}
	
}
