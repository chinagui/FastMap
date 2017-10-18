package com.navinfo.dataservice.engine.limit.search.gdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.navinfo.dataservice.dao.glm.iface.IObj;
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.iface.ObjLevel;
import com.navinfo.dataservice.dao.glm.iface.ObjType;
import com.navinfo.dataservice.dao.glm.model.rd.link.RdLink;
import com.navinfo.dataservice.dao.glm.selector.rd.link.RdLinkSelector;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RdLinkSearch {
	private Connection conn = null;
	
	public RdLinkSearch(Connection conn){
		this.conn = conn;
	}
	
	public JSONObject searchDataByCondition(int type, JSONObject condition) throws Exception{
		
		if(!condition.containsKey("names")){
			throw new Exception("未输入道路名，无法查询道路信息");
		}
		
		JSONArray names = condition.getJSONArray("names");
		
		StringBuilder sql = new StringBuilder();
		
		if (type == 1) {  
			componentSql(sql, names);   //模糊查询
		} else {
			componentSqlForAccurate(sql, names);   //精准查询
		}
		
		PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        
		try {
			pstmt = conn.prepareStatement(sql.toString());
			
			resultSet = pstmt.executeQuery();
			
			Map<String,List<Integer>> classify = new HashMap<>();  
			
			while (resultSet.next()) {
				
				int pid = resultSet.getInt("pid");
				
				String nameout = resultSet.getString("name");
				
				if(classify.containsKey(nameout)){
					classify.get(nameout).add(pid);
				}else{
					List<Integer> pids = new ArrayList<>();
					pids.add(pid);
					classify.put(nameout, pids);
				}
				
			}

			JSONObject result = componetQueryResult(classify);
			
			return result;
        } catch (Exception e) {

            throw e;

        } finally {
            DBUtils.closeResultSet(resultSet);
            DBUtils.closeStatement(pstmt);
        }
	}
	
	private JSONObject componetQueryResult(Map<String,List<Integer>> classify){
		JSONArray array = new JSONArray();

	    JSONObject result = new JSONObject();
	    
	    for(Map.Entry<String, List<Integer>> entry:classify.entrySet()){
	    	
	    	JSONObject obj = new JSONObject();
	    	
	    	obj.put("name", entry.getKey());
	    	obj.put("pid", entry.getValue().toArray());
	    		    	
	    	array.add(obj);
	    }
	        
		result.put("total", classify.size());

		result.put("rows", array);
		
		result.put("geoLiveType", ObjType.RDLINK);
		
		return result;
	}
	
	private void componentSql(StringBuilder sql, JSONArray names) {
		sql.append("with tmp1 as ( select lang_code,name_groupid,name from rd_name where");

		for (int i = 0; i < names.size(); i++) {
			if (i > 0) {
				sql.append(" or");
			}
			sql.append(" name like '%" + names.getString(i) + "%'");
		}

		sql.append(" and u_record != 2),");

		sql.append(
				" tmp2 AS (SELECT /*+ index(r1)*/ rln.link_pid pid, tmp1.name FROM rd_link_name rln,tmp1,rd_link rl WHERE rln.name_class=1 AND rln.link_pid = rl.link_pid and tmp1.name_groupid = rln.name_groupId AND rln.u_record !=2 )");

		sql.append(
				" select * from tmp2 for update nowait");
	}
	
	private void componentSqlForAccurate(StringBuilder sql,JSONArray names){
		sql.append("with tmp1 as ( select lang_code,name_groupid,name from rd_name where name in (");

		for (int i = 0; i < names.size(); i++) {
			if (i > 0) {
				sql.append(", ");
			}
			sql.append("'" + names.getString(i) + "'");
		}

		sql.append(") and u_record != 2),");

		sql.append(
				" tmp2 AS (SELECT /*+ index(r1)*/ rln.link_pid pid, tmp1.name FROM rd_link_name rln,tmp1,rd_link rl WHERE rln.name_class=1 AND rln.link_pid = rl.link_pid and tmp1.name_groupid = rln.name_groupId AND rln.u_record !=2 )");

		sql.append(
				" select * from tmp2 for update nowait");
	}
	
	public JSONObject searchDataByPid(JSONObject condition) throws Exception {

		if (!condition.containsKey("linkPid")) {
			throw new Exception("未输入道路pid，无法查询道路信息");
		}

		int pid = condition.getInt("linkPid");

		StringBuilder sql = new StringBuilder();

		sql.append(
				"SELECT rl.link_pid pid, rd.name FROM RD_LINK rl, RD_NAME rd, RD_LINK_NAME rln WHERE rln.name_class=1 AND rln.link_pid = rl.link_pid and rd.name_groupid = rln.name_groupId AND rln.u_record !=2");
		sql.append(" AND rl.link_pid = " + pid);
		sql.append(" AND rd.lang_code = 'CHI'");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
			pstmt = conn.prepareStatement(sql.toString());

			resultSet = pstmt.executeQuery();

			Map<String, List<Integer>> classify = new HashMap<>();

			while (resultSet.next()) {
				int linkPid = resultSet.getInt("pid");

				String nameout = resultSet.getString("name");

				if (classify.containsKey(nameout)) {
					classify.get(nameout).add(linkPid);
				} else {
					List<Integer> pids = new ArrayList<>();
					pids.add(linkPid);
					classify.put(nameout, pids);
				}
			}

			JSONObject result = componetQueryResult(classify);

			return result;
		} catch (Exception e) {

			throw e;

		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
	}
	
	public List<RdLink> searchDataByPids(List<Integer> pidList)
			throws Exception {

		RdLinkSelector linkSelector = new RdLinkSelector(conn);

		List<RdLink> linkList = linkSelector.loadByPids(pidList, false);

		return linkList;
	}

}
