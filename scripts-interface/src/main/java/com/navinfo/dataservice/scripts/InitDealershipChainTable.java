package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * 代理店IX_DEALERSHIP_CHAIN初始化
 * @author songhe
 * @version 1.0
 * 
 * */
public class InitDealershipChainTable {

	public static void main(String[] args) throws Exception {

		Connection DEconn = null;
		Connection conn = null;
		try {
			JobScriptsInterface.initContext();
			//元数据库
			conn = DBConnector.getInstance().getMetaConnection();
			
			//查询元数据库中的数据
			List<Map<String,Object>> initDataList = InitDealershipChainTable.getinitChainData(conn);
			
			if(initDataList.size() > 0){
				//代理店数据库
				DEconn = DBConnector.getInstance().getConnectionById(399);
				//删除代理店source表中的数据
				InitDealershipChainTable.deletChainDataBeforInit(DEconn);
				//初始化source表
				InitDealershipChainTable.initChainTable(initDataList, DEconn);
			}
		} catch (SQLException e) {
			DbUtils.rollbackAndClose(conn);
			DbUtils.rollbackAndClose(DEconn);
			e.printStackTrace();
		}finally{
			DbUtils.commitAndCloseQuietly(DEconn);
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}


	/**
	 * IX_DEALERSHIP_CHAIN初始化
	 * 将元数据库表SC_POINT_CHAIN_CODE表中CATETORY＝0或2记录进行代理店品牌表初始化
	 * @param List<>
	 * @author songhe
	 * @throws Exception 
	 * 
	 * */
	public static void initChainTable(List<Map<String,Object>> initDataList, Connection conn) throws Exception{

		try{
			QueryRunner run = new QueryRunner();
			
			int status = 1;
			int workType = 0;
			int workStatus = 0;
			
			String sql = "insert into IX_DEALERSHIP_CHAIN t"
					+ "(t.chain_code,t.chain_name,t.chain_weight,t.chain_status,t.work_type,t.work_status) "
					+ "values ('";
			for(Map<String, Object> initMap : initDataList){
				String code = initMap.get("CHAIN_CODE").toString();
				String name = initMap.get("CHAIN_NAME").toString();
				int whight = Integer.parseInt(initMap.get("WEIGHT").toString());
				
				String initSql = sql + code + "','" + name + "'," + whight + "," + status + "," + workType + "," + workStatus + ")";
				
				run.execute(conn, initSql);
			}
		}catch(Exception e){
			e.getMessage();
			throw e;
		}finally{
			
		}
	}
	
	/**
	 * 查询SC_POINT_CHAIN_CODE表中CATETORY＝0或2记录
	 * @return List<>
	 * @author songhe
	 * @throws Exception 
	 * 
	 * */
	public static List<Map<String,Object>> getinitChainData(Connection conn) throws Exception{

		try{
			QueryRunner run = new QueryRunner();
			
			ResultSetHandler<List<Map<String, Object>>> result = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					List<Map<String, Object>> initList = new ArrayList<Map<String,Object>>();
					while(rs.next()){
						Map<String,Object> map = new HashMap<String,Object>();
						map.put("CHAIN_CODE",rs.getString("CHAIN_CODE"));
						map.put("CHAIN_NAME",rs.getString("CHAIN_NAME"));
						map.put("WEIGHT",rs.getInt("WEIGHT"));
						initList.add(map);
					}
					return initList;
				}
			};
			
			String sql = "select t.chain_name,t.chain_code,t.weight from SC_POINT_CHAIN_CODE t where t.category in (0,2)";
			return run.query(conn, sql, result);
		}catch(Exception e){
			e.getMessage();
			throw e;
		}
	}
	
	/**
	 * 初始化之前先删除source表内数据
	 * @author songhe
	 * @throws Exception 
	 * 
	 * */
	public static void deletChainDataBeforInit(Connection conn) throws Exception{

		try{
			QueryRunner run = new QueryRunner();
			
			//初始化表数据之前先清空表内数据
			String berforInitSql = "delete from IX_DEALERSHIP_CHAIN";
			
			run.execute(conn, berforInitSql);
			
		}catch(Exception e){
			e.getMessage();
			throw e;
		}
	}

}
