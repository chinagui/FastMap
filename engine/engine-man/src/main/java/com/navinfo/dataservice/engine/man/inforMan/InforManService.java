package com.navinfo.dataservice.engine.man.inforMan;

import java.sql.Connection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.navinfo.dataservice.api.man.model.InforMan;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.json.JsonOperation;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.common.DbOperation;
import com.navinfo.navicommons.database.QueryRunner;
import net.sf.json.JSONObject;

/** 
* @ClassName:  InforManService 
* @author code generator
* @date 2016-06-15 02:27:02 
* @Description: TODO
*/
@Service
public class InforManService {
	private Logger log = LoggerRepos.getLogger(this.getClass());

	
	public void create(JSONObject json,long userId)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			InforMan bean = (InforMan) JsonOperation.jsonToBean(json,InforMan.class);
			String createSql = "insert into infor_man (INFOR_ID, INFOR_STATUS, DESCP, CREATE_USER_ID, "
					+ "COLLECT_PLAN_START_DATE, COLLECT_PLAN_END_DATE, DAY_EDIT_PLAN_START_DATE, DAY_EDIT_PLAN_END_DATE, MONTH_EDIT_PLAN_START_DATE, MONTH_EDIT_PLAN_END_DATE, DAY_PRODUCE_PLAN_START_DATE, DAY_PRODUCE_PLAN_END_DATE, MONTH_PRODUCE_PLAN_START_DATE, MONTH_PRODUCE_PLAN_END_DATE) values(?,?,?,?,"
					+ "to_timestamp('"+bean.getCollectPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+bean.getCollectPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp('"+bean.getDayEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+bean.getDayEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp('"+bean.getMonthEditPlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+bean.getMonthEditPlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp('"+bean.getDayProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),"
					+ "to_timestamp('"+bean.getDayProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+bean.getMonthProducePlanStartDate()+"','yyyy-mm-dd hh24:mi:ss.ff'),to_timestamp('"+bean.getMonthProducePlanEndDate()+"','yyyy-mm-dd hh24:mi:ss.ff'))";			
			run.update(conn, 
					   createSql, 
					   bean.getInforId(), 1,bean.getDescp(), userId
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("创建失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void update(JSONObject json)throws Exception{
		Connection conn = null;
		try{
			//持久化
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();	
			JSONObject obj = JSONObject.fromObject(json);	
			InforMan  bean = (InforMan)JSONObject.toBean(obj, InforMan.class);	
			
			String updateSql = "update infor_man set INFOR_ID=?, INFOR_STATUS=?, DESCP=?, CREATE_USER_ID=?, CREATE_DATE=?, COLLECT_PLAN_START_DATE=?, COLLECT_PLAN_END_DATE=?, DAY_EDIT_PLAN_START_DATE=?, DAY_EDIT_PLAN_END_DATE=?, MONTH_EDIT_PLAN_START_DATE=?, MONTH_EDIT_PLAN_END_DATE=?, DAY_PRODUCE_PLAN_START_DATE=?, DAY_PRODUCE_PLAN_END_DATE=?, MONTH_PRODUCE_PLAN_START_DATE=?, MONTH_PRODUCE_PLAN_END_DATE=? where 1=1 INFOR_ID=? and INFOR_STATUS=? and DESCP=? and CREATE_USER_ID=? and CREATE_DATE=? and COLLECT_PLAN_START_DATE=? and COLLECT_PLAN_END_DATE=? and COLLECT_GROUP_ID=? and DAY_EDIT_PLAN_START_DATE=? and DAY_EDIT_PLAN_END_DATE=? and DAY_EDIT_GROUP_ID=? and MONTH_EDIT_PLAN_START_DATE=? and MONTH_EDIT_PLAN_END_DATE=? and MONTH_EDIT_GROUP_ID=? and DAY_PRODUCE_PLAN_START_DATE=? and DAY_PRODUCE_PLAN_END_DATE=? and MONTH_PRODUCE_PLAN_START_DATE=? and MONTH_PRODUCE_PLAN_END_DATE=?";
			List<Object> values=new ArrayList();
			if (bean!=null&&bean.getInforId()!=null && StringUtils.isNotEmpty(bean.getInforId().toString())){
				updateSql+=" and INFOR_ID=? ";
				values.add(bean.getInforId());
			};
			if (bean!=null&&bean.getInforStatus()!=null && StringUtils.isNotEmpty(bean.getInforStatus().toString())){
				updateSql+=" and INFOR_STATUS=? ";
				values.add(bean.getInforStatus());
			};
			if (bean!=null&&bean.getDescp()!=null && StringUtils.isNotEmpty(bean.getDescp().toString())){
				updateSql+=" and DESCP=? ";
				values.add(bean.getDescp());
			};
			if (bean!=null&&bean.getCollectPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanStartDate().toString())){
				updateSql+=" and COLLECT_PLAN_START_DATE=? ";
				values.add(bean.getCollectPlanStartDate());
			};
			if (bean!=null&&bean.getCollectPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getCollectPlanEndDate().toString())){
				updateSql+=" and COLLECT_PLAN_END_DATE=? ";
				values.add(bean.getCollectPlanEndDate());
			};
			if (bean!=null&&bean.getDayEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanStartDate().toString())){
				updateSql+=" and DAY_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getDayEditPlanStartDate());
			};
			if (bean!=null&&bean.getDayEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayEditPlanEndDate().toString())){
				updateSql+=" and DAY_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getDayEditPlanEndDate());
			};
			if (bean!=null&&bean.getMonthEditPlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanStartDate().toString())){
				updateSql+=" and MONTH_EDIT_PLAN_START_DATE=? ";
				values.add(bean.getMonthEditPlanStartDate());
			};
			if (bean!=null&&bean.getMonthEditPlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthEditPlanEndDate().toString())){
				updateSql+=" and MONTH_EDIT_PLAN_END_DATE=? ";
				values.add(bean.getMonthEditPlanEndDate());
			};
			if (bean!=null&&bean.getDayProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanStartDate().toString())){
				updateSql+=" and DAY_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getDayProducePlanStartDate());
			};
			if (bean!=null&&bean.getDayProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getDayProducePlanEndDate().toString())){
				updateSql+=" and DAY_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getDayProducePlanEndDate());
			};
			if (bean!=null&&bean.getMonthProducePlanStartDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanStartDate().toString())){
				updateSql+=" and MONTH_PRODUCE_PLAN_START_DATE=? ";
				values.add(bean.getMonthProducePlanStartDate());
			};
			if (bean!=null&&bean.getMonthProducePlanEndDate()!=null && StringUtils.isNotEmpty(bean.getMonthProducePlanEndDate().toString())){
				updateSql+=" and MONTH_PRODUCE_PLAN_END_DATE=? ";
				values.add(bean.getMonthProducePlanEndDate());
			};
			run.update(conn, 
					   updateSql, 
					   bean.getInforId() ,bean.getInforStatus(),bean.getDescp(),bean.getCollectPlanStartDate(),bean.getCollectPlanEndDate(),bean.getDayEditPlanStartDate(),bean.getDayEditPlanEndDate(),bean.getMonthEditPlanStartDate(),bean.getMonthEditPlanEndDate(),bean.getDayProducePlanStartDate(),bean.getDayProducePlanEndDate(),bean.getMonthProducePlanStartDate(),bean.getMonthProducePlanEndDate(),
					   values.toArray()
					   );
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("修改失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public InforMan query(String inforId)throws Exception{
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select * from infor_man where INFOR_ID='"+inforId+"'";
			List<InforMan> list=InforManOperation.selectTaskBySql2(conn, selectSql, null);
			if(list.size()>0){return list.get(0);}
			else{return null;}
			}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询明细失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	public void close(List<String> inforIdslist) throws Exception {
		Connection conn = null;
		try{
			conn = DBConnector.getInstance().getManConnection();		
			String inforIdStr=inforIdslist.toString().replace("[", "'").replace("]", "'").replace(" ", "").replace(",", "','");
			
			String updateSql="UPDATE INFOR_MAN SET INFOR_STATUS=0 WHERE INFOR_ID IN ("+inforIdStr+")";
			DbOperation.exeUpdateOrInsertBySql(conn, updateSql);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("删除失败，原因为:"+e.getMessage(),e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
}
