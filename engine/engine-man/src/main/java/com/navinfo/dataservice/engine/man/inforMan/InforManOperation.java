package com.navinfo.dataservice.engine.man.inforMan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.InforMan;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.QueryRunner;

public class InforManOperation {
	private static Logger log = LoggerRepos.getLogger(InforManOperation.class);

	public InforManOperation() {
		// TODO Auto-generated constructor stub
	}
	
	/*
	 * 根据sql语句查询inforMan
	 */
	public static List<InforMan> selectTaskBySql2(Connection conn,String selectSql,List<Object> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<InforMan>> rsHandler = new ResultSetHandler<List<InforMan>>(){
				public List<InforMan> handle(ResultSet rs) throws SQLException {
					List<InforMan> list = new ArrayList<InforMan>();
					while(rs.next()){
						InforMan map = new InforMan();
						map.setInforId(rs.getString("INFOR_ID"));
						map.setInforStatus(rs.getInt("INFOR_STATUS"));
						map.setDescp(rs.getString("DESCP"));
						map.setCreateUserId(rs.getInt("CREATE_USER_ID"));
						map.setCreateDate(rs.getTimestamp("CREATE_DATE"));
						map.setCollectPlanStartDate(rs.getTimestamp("COLLECT_PLAN_START_DATE"));
						map.setCollectPlanEndDate(rs.getTimestamp("COLLECT_PLAN_END_DATE"));
						map.setCollectGroupId(rs.getInt("COLLECT_GROUP_ID"));
						map.setDayEditPlanStartDate(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE"));
						map.setDayEditPlanEndDate(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE"));
						map.setDayEditGroupId(rs.getInt("DAY_EDIT_GROUP_ID"));
						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
						map.setMonthEditGroupId(rs.getInt("MONTH_EDIT_GROUP_ID"));
						map.setDayEditPlanStartDate(rs.getTimestamp("DAY_PRODUCE_PLAN_START_DATE"));
						map.setDayEditPlanEndDate(rs.getTimestamp("DAY_PRODUCE_PLAN_END_DATE"));
						map.setMonthProducePlanStartDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_START_DATE"));
						map.setMonthProducePlanEndDate(rs.getTimestamp("MONTH_PRODUCE_PLAN_END_DATE"));
						list.add(map);
					}
					return list;
				}
	    		
	    	}		;
	    	if (null==values || values.size()==0){
	    		return run.query( conn, selectSql, rsHandler);
	    	}
	    	return run.query(conn, selectSql, rsHandler,values.toArray());
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}
	}

}
