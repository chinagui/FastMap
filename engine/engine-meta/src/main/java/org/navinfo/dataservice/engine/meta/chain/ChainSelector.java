package org.navinfo.dataservice.engine.meta.chain;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.ResultSetHandler;

import com.alibaba.dubbo.common.json.JSONArray;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/**
 * e获取chain值
 * @author zhangxiaolong
 *
 */
public class ChainSelector {

	/**
	 * 根据kindCode获取chain值
	 * @param kindCode
	 * @return
	 * @throws Exception
	 */
	public JSONArray getChainByKindCode(String kindCode) throws Exception {

		String sql = "select a.chain_name, a.chain_code from sc_point_chain_code a, sc_point_kind_new b where a.chain_code = b.r_kind and b.poikind = :1";

		QueryRunner run = new QueryRunner();
		
		Connection conn = DBConnector.getInstance().getManConnection();
		
		ResultSetHandler<JSONArray> handler = new ResultSetHandler<JSONArray>() {
			
			@Override
			public JSONArray handle(ResultSet rs) throws SQLException {
				
				JSONArray array = new JSONArray();
				
				while(rs.next())
				{
					JSONObject object = new JSONObject();
					
					object.put("chainCode", rs.getString("chain_code"));
					
					object.put("chainName", rs.getString("chain_name"));
					
					array.add(object);
				}
				
				return array;
			}
		};
		
		return run.query(conn,sql, handler, kindCode);
	}
	
	/**
	 * 根据chain获取level
	 * @param kindCode
	 * @return chain的level
	 * @throws Exception
	 */
	public String getLevelByChain(String chain,String kindCode) throws Exception {
		StringBuilder sb = new StringBuilder();
		
		sb.append("select a.\"LEVEL\" from sc_fm_control a,sc_point_kind_new b where a.kind_code = b.poikind");
		
		if(chain != null)
		{
			sb.append(" and b.r_kind = "+chain);
		}
		if(kindCode != null)
		{
			sb.append(" and a.kind_code = "+kindCode);
		}

		QueryRunner run = new QueryRunner();
		
		Connection conn = DBConnector.getInstance().getManConnection();
		
		ResultSetHandler<String> handler = new ResultSetHandler<String>() {
			
			@Override
			public String handle(ResultSet rs) throws SQLException {
				
				return rs.getString("LEVEL");
			}
		};
		
		return run.query(conn,sb.toString(), handler);
	}
}
