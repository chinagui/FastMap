package org.navinfo.dataservice.engine.meta.area;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;
/**
 * 区域信息查询
 * @author zhaokk
 *
 */
public class ScPointAdminArea {

	public JSONArray searchByProvince(String name)
			throws Exception {
	    StringBuilder builder = new StringBuilder();
		builder.append(" SELECT DECODE (type,'省直辖县',district ,city, ");
		builder.append(" type, '省直辖市',district,city, ");
		builder.append(" type,'独立区号', district,city )city, ");
		builder.append(" adminareacode, areacode,phonenum_len");
		builder.append(" FROM sc_point_adminarea ");
		builder.append(" WHERE province = :1");
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
	}
	
	
	public String searchTelLength(String code)
			throws Exception {
	    StringBuilder builder = new StringBuilder();
		builder.append(" SELECT phonenum_len ");
		builder.append(" FROM sc_point_adminarea ");
		builder.append(" WHERE areacode = :1");
		QueryRunner runner = new QueryRunner();
	    return runner.query(DBConnector.getInstance().getMetaConnection(),builder.toString(), new ResultSetHandler<String>(){
			@Override
			public String handle(ResultSet rs) throws SQLException {
				if(rs.next()){
					return rs.getString("phonenum_len");
				}
				return "";
			}
		},code);
	}
	

	public JSONArray searchFoodType(String kindId)
			throws Exception {

	    StringBuilder builder = new StringBuilder();
		builder.append(" SELECT poikind,foodtype,type,foodtypename ");
		builder.append(" FROM sc_point_foodtype ");
		builder.append(" WHERE kind_id = :1");
		QueryRunner runner = new QueryRunner();
	    return runner.query(DBConnector.getInstance().getMetaConnection(),builder.toString(), new ResultSetHandler<JSONArray>(){
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
	}
	
	public JSONObject searchkindLevel(String kindCode)
			throws Exception {

	    StringBuilder builder = new StringBuilder();
		builder.append(" SELECT chain,KIND_CODE,\"LEVEL\",\"EXTEND\" ");
		builder.append(" FROM sc_fm_control ");
		builder.append(" WHERE kind_code = :1");
		try{
			QueryRunner runner = new QueryRunner();
		    return runner.query(DBConnector.getInstance().getMetaConnection(),builder.toString(), new ResultSetHandler<JSONObject>(){
		    	JSONObject  jsonObject =null;
		    	@Override
				public JSONObject handle(ResultSet rs) throws SQLException {
					if(rs.next()){
						jsonObject.put("chainFlag", rs.getInt("chain"));
						jsonObject.put("kindId", rs.getString("kind_code"));
						jsonObject.put("extend", rs.getString("extend"));
		
					}else{
						try {
							throw new Exception("对应KIND_CODE数据不存在不存在!");
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					return jsonObject;
				}
			},kindCode);
		}catch (Exception e) {
			throw e;
		}
	}
	
}
