package com.navinfo.dataservice.control.row.poiEditStatus;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
	 * 
	 * @param conn
	 * @param fidMap//{变更类型：pid列表}。变更类型：1 新增；2 删除；3 修改
	 * @param status//作业状态。0: 未作业；1：待作业,2：已作业,3：已提交
	 * @return
	 * @throws Exception 
	 */
	public static Map<Integer,Collection<Long>> fidFilterByEditStatus(Connection conn
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

		Map<Integer,Collection<Long>> pids2 = fidFilterByEditStatus(conn,pids,3);

		System.out.println("ok");
	}
	
}
