package com.navinfo.dataservice.dao.fcc.check.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: CheckResultSelector.java
 * @author y
 * @date 2017-5-24 下午8:35:49
 * @Description: 质检-抽检结果表查询类
 *  
 */
public class CheckResultSelector {
	
	
	Logger log=Logger.getLogger(CheckPercentConfig.class);
	
	static Map<Integer ,JSONArray> CHECK_EXTRACT_RESULT_MAP=new  HashMap<Integer, JSONArray>();
	
	public CheckResultSelector() {
		super();
	}
	
	/**
	 * @Description:按照质检任务号，查询抽检的tips
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-24 下午8:58:36
	 */
	public   JSONArray queryByTipsRowkey(int checkTaskId) throws Exception {
		
		if(CHECK_EXTRACT_RESULT_MAP.get(checkTaskId)!=null){
			
			return CHECK_EXTRACT_RESULT_MAP.get(checkTaskId);
		}
		
		JSONArray rowkeyList=new JSONArray();
		
		String sql="SELECT ROWKEY FROM check_result g WHERE g.TASK_ID=?";
		
		PreparedStatement pst=null;
		
		ResultSet rs=null;
		
		Connection conn=null;
		
		try{
			
			conn=DBConnector.getInstance().getCheckConnection();
			
			pst=conn.prepareStatement(sql);
			
			pst.setInt(1, checkTaskId);
			
			rs=pst.executeQuery();
			
			if(rs.next()){
				
				rowkeyList.add(rs.getString("ROWKEY"));
			}
			
			CHECK_EXTRACT_RESULT_MAP.put(checkTaskId, rowkeyList);
			
			return rowkeyList;
			
		}catch (Exception e) {
			log.error("按照任务号查询抽检记录出错，"+e.getMessage(), e);
			throw new Exception("按照任务号查询抽检记录出错，"+e.getMessage(), e);
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pst);
			DbUtils.closeQuietly(conn);
		}
		
	}
	
	
	
	
	
	
	

}
