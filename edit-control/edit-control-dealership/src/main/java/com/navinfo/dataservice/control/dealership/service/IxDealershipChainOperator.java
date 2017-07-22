package com.navinfo.dataservice.control.dealership.service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class IxDealershipChainOperator {
	private static Logger log = LoggerRepos.getLogger(DataEditService.class);
	/**
	 * 表表差分后，修改IX_DEALERSHIP_CHAIN表状态
	 * @param conn
	 * @param chainCode
	 * @throws SQLException
	 */
	public static void changeChainStatus(Connection conn, String chainCode,int status) throws SQLException {
		log.info("start 表表差分修改chain表状态");
		String sql="UPDATE IX_DEALERSHIP_CHAIN SET WORK_STATUS = "+status+" WHERE CHAIN_CODE = '"+chainCode+"'";
		QueryRunner run=new QueryRunner();
		run.update(conn, sql);
	}
	/**
	 * @param conn
	 * @param chainCode
	 * @return
	 * @throws SQLException 
	 */
	public static Map<String, Object> getByChainCode(Connection conn, String chainCode) throws SQLException {
		String sql = "SELECT * FROM IX_DEALERSHIP_CHAIN C WHERE C.CHAIN_CODE = '" + chainCode + "'";
		QueryRunner run = new QueryRunner();
		
		ResultSetHandler<Map<String,Object>> rsHandler = new ResultSetHandler<Map<String,Object>>() {
			public Map<String,Object> handle(ResultSet rs) throws SQLException {
				if (rs.next()) {
					Map<String,Object> result = new HashMap<String,Object>();
					result.put("chainName", rs.getString("CHAIN_NAME"));
					result.put("chainWeight", rs.getInt("CHAIN_WEIGHT"));
					result.put("chainStatus", rs.getInt("CHAIN_STATUS"));
					result.put("workType", rs.getInt("WORK_TYPE"));
					result.put("workStatus", rs.getInt("WORK_STATUS"));

					return result;
				}
				return null;
			}	
		};
		log.info("getByChainCode sql:" + sql);
		return run.query(conn, sql,rsHandler);
	}
}
