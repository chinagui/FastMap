package com.navinfo.dataservice.engine.limit.search;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.limit.glm.model.limit.ScPlateresInfo;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

public class ScPlatersInfoSearch {
	private QueryRunner run;
	
	private Connection conn;
	
	public ScPlatersInfoSearch() throws Exception{
		run = new QueryRunner();
		
		conn = DBConnector.getInstance().getlimitLineInfoConnection();
	}
	
	public void getInfoByCondition(JSONObject obj) throws Exception{
		
		if(!obj.containsKey("adminArea")||!obj.containsKey("pageSize")||!obj.containsKey("pageNum")){
			throw new Exception("筛选情报参数不完善，请重新输入！");
		}
		
		String adminCode = obj.getString("adminArea");
		int pageSize = obj.getInt("pageSize");
		int pageNum = obj.getInt("pageNum");
		
		StringBuilder sql = new StringBuilder();
		
		List<Object> params = new ArrayList<>();
		
		sql.append("SELECT * FROM SC_PLATERES_INFO WHERE ADMIN_CODE = ?");
		params.add(adminCode);
		componentSql(obj,sql,params,pageSize,pageNum);

		try{
			ResultSetHandler<List<ScPlateresInfo>> handler = new ResultSetHandler<List<ScPlateresInfo>>(){
				
				public List<ScPlateresInfo> handle(ResultSet rs) throws SQLException {
					
					List<ScPlateresInfo> res = new ArrayList<>();
					
					while (rs.next()) {
						
						ScPlateresInfo item = new ScPlateresInfo();
						item.loadResultSet(rs);
						res.add(item);
					}
					return res;
				}
			};
		
		List<ScPlateresInfo> result = run.query(conn,sql.toString(),handler,params);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void componentSql(JSONObject obj,StringBuilder sql,List<Object> params,int pageSize,int pageNum){

		if (obj.containsKey("infoCode")) {
			String infoCode = obj.getString("infoCode");
			
			if (infoCode != null && !infoCode.isEmpty()) {
				sql.append(" AND INFO_CODE = ?");
				params.add(infoCode);
			}
		}

		if (obj.containsKey("newsTime")) {
			String newsTime = obj.getString("newsTime");
			
			if (newsTime != null && !newsTime.isEmpty()) {
				sql.append(" AND NEWS_TIME = ?");
				params.add(newsTime);
			}
		}
		
		if (obj.containsKey("complete")) {
			String complete = obj.getString("complete");
			
			if (complete != null && !complete.isEmpty()) {
				sql.append(" AND COMPLETE IN ?");
				params.add("(" + complete + ")");
			}
		}

		if (obj.containsKey("condition")) {
			String condition = obj.getString("condition");
			
			if (condition != null && !condition.isEmpty()) {
				sql.append(" AND CONDITION = ?");
				params.add("(" + condition + ")");
			}
		}
		
		sql.append(" AND rownum BETWEEN ? AND ?");
		params.add((pageNum - 1) * pageSize + 1);
		params.add(pageNum * pageSize);
	}
	
}
