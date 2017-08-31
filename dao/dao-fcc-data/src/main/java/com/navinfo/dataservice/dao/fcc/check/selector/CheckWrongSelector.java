package com.navinfo.dataservice.dao.fcc.check.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.fcc.check.model.CheckWrong;
import com.navinfo.dataservice.dao.fcc.check.model.rowmapper.CheckWrongRowMapper;
import com.navinfo.navicommons.database.QueryRunner;

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
	
	
	
	

}
