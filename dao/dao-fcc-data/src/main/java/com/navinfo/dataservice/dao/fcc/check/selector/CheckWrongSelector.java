package com.navinfo.dataservice.dao.fcc.check.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.dao.fcc.check.model.rowmapper.CheckWrongRowMapper;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONObject;

/** 
 * @ClassName: CheckResultSelector.java
 * @author y
 * @date 2017-5-24 下午8:35:49
 * @Description: 质检-质检问题记录表查询类
 *  
 */
public class CheckWrongSelector {
	
	
	
	Logger log=Logger.getLogger(CheckPercentConfig.class);
	
	

	public CheckWrongSelector() {
		super();
	}
	
	
	
	
	
	
	/**
	 * @Description:按照tips的rowkey 查询tips下的问题记录
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-24 下午8:58:36
	 */
	public  CheckWrong queryByTipsRowkey(int checkTaskId,String objectId) throws Exception {
		
		CheckWrong wrong=null;
		
		String sql="SELECT * FROM check_wrong g WHERE g.check_task_id=?  AND g.object_id= ?";
		
		PreparedStatement pst=null;
		
		ResultSet rs=null;
		
		Connection conn=null;
		try{
			
			conn=DBConnector.getInstance().getCheckConnection();
			
			pst=conn.prepareStatement(sql);
			
			pst.setInt(1, checkTaskId);
			
			pst.setString(2, objectId);
			
			rs=pst.executeQuery();
			
			if(rs.next()){
				
				wrong=CheckWrongRowMapper.getInstance().mapRow(rs);
			}
			
			return wrong;
		}catch (Exception e) {
			log.error("按照rowkey查询质检问题记录出错，"+e.getMessage(), e);
			throw new Exception("按照rowkey查询质检问题记录出错，"+e.getMessage(), e);
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pst);
			DbUtils.closeQuietly(conn);
		}
		
	}
	
	
	
	/**
	 * @Description:查询rowkey是否已经记录了错误信息
	 * @param checkTaskId
	 * @param rowkey
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-25 下午3:43:06
	 */
	public  boolean rowkeyHasExtract(int checkTaskId,String objectId) throws Exception{
		
		int count=0;
		Connection conn=null;
		try{
			
			conn=DBConnector.getInstance().getCheckConnection();
			QueryRunner run = new QueryRunner();
			count = run.queryForInt(conn, "SELECT count(1) FROM check_wrong g WHERE g.check_task_id=?  AND g.object_id= ?",checkTaskId,objectId);	
		}catch (Exception e) {
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
			
		}
		return count>0;
	}
	
	
	
	
	
	/**
	 * @Description:根据主键查询是否存在
	 * @param logId
	 * @return
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-25 下午3:43:06
	 */
	public  boolean isExists(String logId) throws Exception{
		
		int count=0;
		Connection conn=null;
		try{
			
			conn=DBConnector.getInstance().getCheckConnection();
			QueryRunner run = new QueryRunner();
			count = run.queryForInt(conn, "SELECT count(1) FROM check_wrong g WHERE g.log_Id=?",logId);	
		}catch (Exception e) {
			throw e;
		}finally{
			DbUtils.closeQuietly(conn);
			
		}
		return count>0;
	}
	
	/**
	 * 根据logId获取质检问题记录
	 * @param logId
	 * @throws Exception
	 */
	public static JSONObject getByLogId(String logId) throws Exception {
		JSONObject obj = new JSONObject();
		String sql = "select log_id, check_task_id, object_type, object_id, qu_desc, reason, "
				+ "er_content, qu_rank, work_time, check_time, is_prefer, worker, checker, er_type "
				+ "from check_wrong where id=:1";
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			conn = DBConnector.getInstance().getCheckConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, logId);
			
			rs = pstmt.executeQuery();
			while(rs.next()){
				obj.put("logId", rs.getString("log_id"));
				obj.put("checkTaskId", rs.getInt("check_task_id"));
				obj.put("objectType", rs.getString("object_type")!=null?rs.getString("object_type"):"");
				obj.put("objectId", rs.getString("object_id")!=null?rs.getString("object_id"):"");
				obj.put("erType", rs.getInt("er_type"));
				obj.put("quDesc", rs.getString("qu_desc")!=null?rs.getString("qu_desc"):"");
				obj.put("reason", rs.getString("reason")!=null?rs.getString("reason"):"");
				obj.put("erContent", rs.getString("er_content")!=null?rs.getString("er_content"):"");
				obj.put("quRank", rs.getString("qu_rank")!=null?rs.getString("qu_rank"):"");
				obj.put("worker", rs.getString("worker")!=null?rs.getString("worker"):"");
				obj.put("checker", rs.getString("checker")!=null?rs.getString("checker"):"");
				obj.put("isPrefer", rs.getInt("is_prefer"));
				Date wTime = rs.getDate("work_time");
				Date cTime = rs.getDate("check_time");
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String workTime = wTime!=null?formatter.format(wTime):"";
				String checkTime= cTime!=null?formatter.format(cTime):"";
				obj.put("workTime", workTime);
				obj.put("checkTime", checkTime);
			}
			
			
			return obj;
			
		}catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new Exception(e.getMessage(), e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	

}
