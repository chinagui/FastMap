package com.navinfo.dataservice.engine.man.city;

import java.sql.Connection;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class CityOperation {
	private static Logger log = LoggerRepos.getLogger(CityOperation.class);

	public CityOperation() {
		// TODO Auto-generated constructor stub
	}
	
	public static void updatePlanStatus(Connection conn,int cityId,int PlanStatus) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String updateCity="UPDATE CITY SET PLAN_STATUS="+PlanStatus+" WHERE CITY_ID="+cityId;
			run.update(conn,updateCity);			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}

}
