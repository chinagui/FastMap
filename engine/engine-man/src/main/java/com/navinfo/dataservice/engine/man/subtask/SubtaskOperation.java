package com.navinfo.dataservice.engine.man.subtask;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.task.TaskOperation;
import com.navinfo.navicommons.database.QueryRunner;

/** 
 * @ClassName: SubtaskOperation
 * @author songdongyan
 * @date 2016年6月13日
 * @Description: SubtaskOperation.java
 */
public class SubtaskOperation {
	private static Logger log = LoggerRepos.getLogger(TaskOperation.class);
	
	public SubtaskOperation() {
		// TODO Auto-generated constructor stub
	}
	
	
	public static void updateSubtask(Connection conn,Subtask bean) throws Exception{
		try{
			String baseSql = "update SUBTASK set ";
			QueryRunner run = new QueryRunner();
			String updateSql="";
//			List<Object> values = new ArrayList<Object>();
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
//				updateSql += " DESCP=? ";				
//				values.add(bean.getDescp());
				updateSql += " DESCP= " + "'" + bean.getDescp() + "'";
			};
			if (bean!=null&&bean.getPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getPlanStartDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
//				updateSql+=" PLAN_START_DATE=? ";
//				values.add("to_date('" + bean.getPlanStartDate() + "','yyyymmdd')");
				updateSql += " PLAN_START_DATE= " + "to_date('" + bean.getPlanStartDate() + "','yyyymmdd')";
			};
			if (bean!=null&&bean.getExeUserId()!=null && StringUtils.isNotEmpty(bean.getExeUserId().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
//				updateSql+=" EXE_USER_ID=? ";
//				values.add(bean.getExeUserId());
				updateSql += " EXE_USER_ID= " + bean.getExeUserId();
			};
			if (bean!=null&&bean.getPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getPlanEndDate().toString())){
				if(StringUtils.isNotEmpty(updateSql)){updateSql+=" , ";}
//				updateSql+=" PLAN_END_DATE=? ";
//				values.add("to_date('" + bean.getPlanEndDate() + "','yyyymmdd')");
				updateSql += " PLAN_END_DATE= " + "to_date('" + bean.getPlanEndDate() + "','yyyymmdd')";
			};
			
			
			if (bean!=null&&bean.getSubtaskId()!=null && StringUtils.isNotEmpty(bean.getSubtaskId().toString())){
//				updateSql+=" where SUBTASK_ID=?";
//				values.add(bean.getSubtaskId());
				updateSql += " where SUBTASK_ID= " + bean.getSubtaskId();
			};
			
//			run.update(conn,baseSql+updateSql,values.toArray());
			run.update(conn,baseSql+updateSql);
			
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("更新失败，原因为:"+e.getMessage(),e);
		}
	}
}
