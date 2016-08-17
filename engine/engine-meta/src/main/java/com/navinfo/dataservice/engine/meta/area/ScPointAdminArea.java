package com.navinfo.dataservice.engine.meta.area;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;
/**
 * 区域信息查询
 * @author zhaokk
 *
 */
public class ScPointAdminArea {
	private Logger log = LoggerRepos.getLogger(this.getClass());
	/**
	 * 根据省份获取电话列表
	 * @param name
	 * @return JSONArray
	 * @throws Exception
	 */
	public JSONArray searchByProvince(String name)
			throws Exception {
	    StringBuilder builder = new StringBuilder();
		builder.append(" SELECT DECODE (type,'省直辖县',district ,city, ");
		builder.append(" type, '省直辖市',district,city, ");
		builder.append(" type,'独立区号', district,city )city, ");
		builder.append(" adminareacode, areacode,phonenum_len");
		builder.append(" FROM sc_point_adminarea ");
		builder.append(" WHERE province = :1");
		Connection conn = DBConnector.getInstance().getMetaConnection();
		try{
			QueryRunner runner = new QueryRunner();
		    return runner.query(DBConnector.getInstance().getMetaConnection(),builder.toString(), new ResultSetHandler<JSONArray>(){
				@Override
				public JSONArray handle(ResultSet rs) throws SQLException {
					JSONArray array  = new JSONArray();
					while(rs.next()){
						JSONObject  jsonObject = new JSONObject();
						jsonObject.put("city", rs.getString("city"));
						jsonObject.put("cityCode", rs.getString("adminareacode"));
						jsonObject.put("code", rs.getString("areacode"));
						jsonObject.put("telLength", rs.getString("phonenum_len"));
						array.add(jsonObject);
					}
					return array;
				}
			},name);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 电话长度查询
	 * @param name
	 * @return String
	 * @throws Exception
	 */
	
	public String searchTelLength(String code)
			throws Exception {
	    StringBuilder builder = new StringBuilder();
		builder.append(" SELECT phonenum_len ");
		builder.append(" FROM sc_point_adminarea ");
		builder.append(" WHERE areacode = :1");
		Connection conn = DBConnector.getInstance().getMetaConnection();
		try{
			QueryRunner runner = new QueryRunner();
		
		    return runner.query(conn,builder.toString(), new ResultSetHandler<String>(){
				@Override
				public String handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						return rs.getString("phonenum_len");
					}
					return "";
				}
			},code);
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
}
	/**
	 * 查询foodType
	 * @param kindId
	 * @returnJ SONArray
	 * @throws Exception
	 */
	public JSONArray searchFoodType(String kindId)
			throws Exception {

	    StringBuilder builder = new StringBuilder();
		builder.append(" SELECT poikind,foodtype,type,foodtypename ");
		builder.append(" FROM sc_point_foodtype ");
		builder.append(" WHERE poikind = :1");
		Connection conn = DBConnector.getInstance().getMetaConnection();
		try{
		QueryRunner runner = new QueryRunner();
	    return runner.query(conn,builder.toString(), new ResultSetHandler<JSONArray>(){
			@Override
			public JSONArray handle(ResultSet rs) throws SQLException {
				JSONArray  array = new JSONArray();
				while(rs.next()){
					JSONObject  jsonObject = new JSONObject();
					jsonObject.put("kindId", rs.getString("poikind"));
					jsonObject.put("foodType", rs.getString("type"));
					jsonObject.put("foodCode", rs.getString("foodtype"));
					jsonObject.put("foodName", rs.getString("foodtypename"));
					array.add(jsonObject);
				}
				return array;
			}
		},kindId);
	}catch (Exception e) {
		DbUtils.rollbackAndCloseQuietly(conn);
		log.error(e.getMessage(), e);
		throw new ServiceException("查询明细失败，原因为:" + e.getMessage(), e);
	} finally {
		DbUtils.commitAndCloseQuietly(conn);
	}
  }
	
	/**
	 * 获取行政区划号和名称
	 * @return
	 * @throws Exception
	 */
	public JSONArray getAdminArea() throws Exception {
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;

		Connection conn = null;
		
		String sql = "SELECT adminareacode,whole from SC_POINT_ADMINAREA";
		
		try {
			conn = DBConnector.getInstance().getMetaConnection();
			
			pstmt = conn.prepareStatement(sql);
			
			resultSet = pstmt.executeQuery();
			
			JSONArray result = new JSONArray();
			
			while (resultSet.next()) {
				JSONObject data = new JSONObject();
				data.put("adminareacode", resultSet.getInt("adminareacode"));
				data.put("whole", resultSet.getString("whole"));
				result.add(data);
			}
			
			return result;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
			DbUtils.closeQuietly(conn);
		}
	}
	
}
