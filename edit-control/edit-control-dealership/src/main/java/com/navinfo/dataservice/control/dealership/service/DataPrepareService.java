package com.navinfo.dataservice.control.dealership.service;

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
 * 代理店数据准备类
 * 
 * @author jicaihua
 *
 */
public class DataPrepareService {

	private DataPrepareService() {
	}

	private static class SingletonHolder {
		private static final DataPrepareService INSTANCE = new DataPrepareService();
	}

	public static DataPrepareService getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 查询代理店品牌
	 * @param chainStatus品牌状态
	 * @param begainSize 开始的数据条数
	 * @param endSize 结束的数据条数
	 * @return 分页后的某种品牌状态的List
	 * @author songhe
	 * 
	 * */
	public List<Map<String, Object>> queryDealerBrand(int chainStatus,int pageSize, int pageNum) throws SQLException{
		
		//处理分页数据
		int begainSize = (pageSize - 1) * pageNum;
		int endSize = pageSize * pageNum;
		
		Connection con = null;
		try{
			con = DBConnector.getInstance().getConnectionById(399);
			QueryRunner run = new QueryRunner();
			String selectSql = "SELECT * FROM "
					+ "(SELECT A.*, ROWNUM RN FROM "
					+ "(SELECT t.* FROM IX_DEALERSHIP_CHAIN t where t.chain_status  = " + chainStatus + ") "
							+ "A WHERE ROWNUM <= " + endSize + ")WHERE RN > " + begainSize;
			
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					
					List<Map<String, Object>> dealerShipList = new ArrayList();
					while (rs.next()) {
						Map<String, Object> result = new HashMap<>();
						result.put("CHAIN_CODE", rs.getString("CHAIN_CODE"));
						result.put("CHAIN_NAME", rs.getString("CHAIN_NAME"));
						result.put("CHAIN_WEIGHT", rs.getInt("CHAIN_WEIGHT"));
						result.put("CHAIN_STATUS", rs.getInt("CHAIN_STATUS"));
						result.put("WORK_TYPE", rs.getInt("WORK_TYPE"));
						result.put("WORK_STATUS", rs.getInt("WORK_STATUS"));
						dealerShipList.add(result);
					}
					return dealerShipList;
				}
			};
			
			return run.query(con, selectSql, rs);
		}catch(Exception e){
			DbUtils.rollbackAndClose(con);
		}finally{
			DbUtils.commitAndClose(con);
		}
		return null;
	}
	
	/**
	 * 差分结果列表
	 * @param chainCode品牌代码
	 * @return 
	 * @author songhe
	 * 
	 * */
	public List<Map<String, Object>> loadDiffList(String chainCode) throws SQLException{
		
		Connection con = null;
		try{
			con = DBConnector.getInstance().getConnectionById(399);
			QueryRunner run = new QueryRunner();
			String selectSql = "select r.result_id,s.source_id,r.city,r.kind_code,r.name as result_name, s.name as source_name,c.work_type,c.work_status,r.workflow_status "
					+ "from IX_DEALERSHIP_RESULT r, IX_DEALERSHIP_SOURCE s, IX_DEALERSHIP_CHAIN c "
					+ "where r.source_id = s.source_id and c.chain_code = s.source_id and r.result_id = '"+chainCode+"'";
			
			ResultSetHandler<List<Map<String, Object>>> rs = new ResultSetHandler<List<Map<String, Object>>>() {
				@Override
				public List<Map<String, Object>> handle(ResultSet rs) throws SQLException {
					
					List<Map<String, Object>> diffList = new ArrayList();
					while (rs.next()) {
						Map<String, Object> result = new HashMap<>();
						result.put("resultId", rs.getString("result_id"));
						result.put("sourceId", rs.getString("source_id"));
						result.put("city", rs.getString("city"));
						result.put("kindCode", rs.getString("kind_code"));
						result.put("resultName", rs.getString("result_name"));
						result.put("sourceName", rs.getString("source_name"));
						result.put("workType", rs.getInt("work_type"));
						result.put("dealSrcDiff", rs.getInt("work_status"));
						result.put("workflowStatus", rs.getInt("workflow_status"));
						diffList.add(result);
					}
					return diffList;
				}
			};
			
			return run.query(con, selectSql, rs);
		}catch(Exception e){
			DbUtils.rollbackAndClose(con);
		}finally{
			DbUtils.commitAndClose(con);
		}
		return null;
	}
	
}
