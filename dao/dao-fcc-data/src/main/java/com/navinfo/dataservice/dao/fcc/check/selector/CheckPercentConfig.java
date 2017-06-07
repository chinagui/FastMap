package com.navinfo.dataservice.dao.fcc.check.selector;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;


/** 
 * @ClassName: CheckPercentConfig.java
 * @author y
 * @date 2017-5-24 下午8:44:08
 * @Description: 质检-抽检配置
 *  
 */
public class CheckPercentConfig {
	
	
	public  static Map<String,Integer> PERCENT_CONFIG_MAP=new HashMap<String,Integer>(); 
	
	Logger log=Logger.getLogger(CheckPercentConfig.class);
	
	
	
	public  CheckPercentConfig(){
		
	}
	
	/**
	 * @Description:初始化抽样配置
	 * @author: y
	 * @throws Exception 
	 * @time:2017-5-24 下午8:58:36
	 */
	private void initConfig() throws Exception {
		
		String sql="SELECT tips_code,check_percent FROM check_percent";
		
		PreparedStatement pst=null;
		
		ResultSet rs=null;
		
		
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getCheckConnection();
			
			pst=conn.prepareStatement(sql);
			
			rs=pst.executeQuery();
			
			while(rs.next()){
				
				String tipsCode=rs.getString("tips_code");
				
				int precent= rs.getInt("check_percent");
				
				PERCENT_CONFIG_MAP.put(tipsCode, precent);
				
				log.debug("初始化结果："+tipsCode+":"+precent);
				
			}
			
			log.debug("质检比例初始化完成！");
		}catch (Exception e) {
			PERCENT_CONFIG_MAP.clear(); //清理一下，如果中途异常，保证下次能重新加载
			log.error("初始化抽检配置表出错，"+e.getMessage(), e);
			throw new Exception("初始化抽检配置表出错，"+e.getMessage(), e);
		}finally{
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pst);
			DbUtils.closeQuietly(conn);
		}
		
	}

	public synchronized  Map<String,Integer>  getConfig() throws Exception{
		
		if(PERCENT_CONFIG_MAP.size()==0){
			
		log.debug("开始重新初始化配置表：");
			
			initConfig();
		}
		
		return PERCENT_CONFIG_MAP;
		
	}
	

}
