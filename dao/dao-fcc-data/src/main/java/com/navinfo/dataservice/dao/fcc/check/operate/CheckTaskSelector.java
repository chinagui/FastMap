package com.navinfo.dataservice.dao.fcc.check.operate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.dao.fcc.check.selector.CheckPercentConfig;

/** 
 * @ClassName: CheckTaskSelector.java
 * @author y
 * @date 2017-5-31 下午10:09:23
 * @Description: TODO
 *  
 */
public class CheckTaskSelector {
	
	
	
	Logger log=Logger.getLogger(CheckPercentConfig.class);
	
	/**
	 * @Description:根据质检任务号获取质检作业量和tips类型量
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-31 下午22:26:02
	 */
	public  Map<String,Integer> queryTaskCountByTaskId(int checkTaskId) throws Exception {
		
		Map<String,Integer> resultMap=new HashMap<String, Integer>();
		
		String sql="SELECT CHECK_TOTAL_COUNT,TIPS_TYPE_COUNT FROM CHECK_TASK g WHERE g.TASK_ID=?";
		
		PreparedStatement pst=null;
		
		ResultSet rs=null;
		
		Connection conn=null;
		try{
			
			conn=DBConnector.getInstance().getCheckConnection();
			
			pst=conn.prepareStatement(sql);
			
			pst.setInt(1, checkTaskId);
			
			rs=pst.executeQuery();
			
			if(rs.next()){
				resultMap.put("checkCount", rs.getInt("CHECK_TOTAL_COUNT"));
				resultMap.put("tipsTypeCount", rs.getInt("TIPS_TYPE_COUNT"));
			}else{
				resultMap.put("checkCount", 0);
				resultMap.put("tipsTypeCount", 0);
			}
			
			return resultMap;
		}catch (Exception e) {
			log.error("查询抽检任务信息出错，"+e.getMessage(), e);
			throw new Exception("查询抽检任务信息出错，"+e.getMessage(), e);
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pst);
			DbUtils.closeQuietly(conn);
		}
	}

}
