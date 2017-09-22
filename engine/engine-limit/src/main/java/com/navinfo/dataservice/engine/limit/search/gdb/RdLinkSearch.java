package com.navinfo.dataservice.engine.limit.search.gdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;

public class RdLinkSearch {
	private Connection conn = null;
	
	public RdLinkSearch(Connection conn){
		this.conn = conn;
	}
	
	public JSONObject searchDataByCondition(JSONObject condition) throws Exception{
		
		if(!condition.containsKey("name")){
			throw new Exception("未输入道路名，无法查询道路信息");
		}
		
		String name = condition.getString("name");
		String[] names = name.split(",");
		
		StringBuilder sql = new StringBuilder();
		componentSql(sql,names);
		
		PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        
        int total = 0;
        
        int pageSize = condition.getInt("pageSize");
        int pageNum = condition.getInt("pageNum");

        JSONObject result = new JSONObject();
        
		try {
			pstmt = conn.prepareStatement(sql.toString());

			int startRow = (pageNum - 1) * pageSize + 1;
			int endRow = pageNum * pageSize;

			pstmt.setInt(1, endRow);
			pstmt.setInt(2, startRow);
			
			resultSet = pstmt.executeQuery();
			
			JSONArray array = new JSONArray();
			
			while (resultSet.next()) {
				
				total = resultSet.getInt("total");
				
				JSONObject json = new JSONObject();
		
				int pid = resultSet.getInt("pid");
				json.put("pid", pid);
				
				STRUCT struct = (STRUCT) resultSet.getObject("geometry");
				json.put("geometry", GeoTranslator.jts2Geojson(GeoTranslator
						.struct2Jts(struct)));
				
				String nameout = resultSet.getString("name");
				json.put("name", nameout == null? "":nameout);
				
			     array.add(json);
			}
			result.put("total", total);

			result.put("rows", array);

			return result;
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
	}
	
	private void componentSql(StringBuilder sql, String[] names) {
		sql.append("with tmp1 as ( select lang_code,name_groupid,name from rd_name where");

		for (int i = 0; i < names.length; i++) {
			if (i > 0) {
				sql.append(" or");
			}
			sql.append(" name like '%" + names[i] + "%'");
		}

		sql.append(" and u_record != 2)");

		sql.append(
				"tmp2 AS (SELECT /*+ index(r1)*/ rln.link_pid pid,rl.geometry AS geometry,tmp1.name FROM rd_link_name rln,tmp1,rd_link rl WHERE rln.name_class=1 AND rln.link_pid = rl.link_pid and tmp1.name_groupid = rln.name_groupId AND rln.u_record !=2 )");

		sql.append(
				"tmp3 AS (SELECT count(*) over () total,tmp2.*, ROWNUM rn FROM tmp2 ) ,tmp4 as ( select * from tmp3 where ROWNUM <=:1 ) SELECT * FROM tmp4 WHERE rn >=:2 for update nowait");
	}
}
