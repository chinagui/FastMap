package com.navinfo.dataservice.engine.man.inforMan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.InforMan;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.navicommons.database.Page;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

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
						map.setDayEditPlanStartDate(rs.getTimestamp("DAY_EDIT_PLAN_START_DATE"));
						map.setDayEditPlanEndDate(rs.getTimestamp("DAY_EDIT_PLAN_END_DATE"));
						map.setMonthEditPlanStartDate(rs.getTimestamp("MONTH_EDIT_PLAN_START_DATE"));
						map.setMonthEditPlanEndDate(rs.getTimestamp("MONTH_EDIT_PLAN_END_DATE"));
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
	
	/*
	 * 查询infor list
	 */
	public static Page selectInforList(Connection conn, String selectSql, List<Object> values, final int currentPageNum,
			final int pageSize) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			ResultSetHandler<Page> rsHandler = new ResultSetHandler<Page>() {
				public Page handle(ResultSet rs) throws SQLException {
					List<HashMap> list = new ArrayList<HashMap>();
					Page page = new Page(currentPageNum);
					int totalCount=0;
					while (rs.next()) {
						HashMap map = new HashMap();
						map.put("inforId", rs.getString("infor_id"));
						map.put("inforName", rs.getString("infor_name"));
						map.put("inforLevel", rs.getString("infor_level"));
						map.put("descp", rs.getString("descp"));
						map.put("version", SystemConfigFactory.getSystemConfig().getValue(PropConstant.gdbVersion));
						map.put("userName", rs.getString("user_name"));
						map.put("collectPlanStartDate", rs.getString("collect_plan_start_date"));
						map.put("collectPlanEndDate", rs.getString("collect_plan_end_date"));					
						map.put("dayEditPlanStartDate", rs.getString("day_edit_plan_start_date"));
						map.put("dayEditPlanEndDate", rs.getString("day_edit_plan_end_date"));
						map.put("dayProducePlanStartDate", rs.getString("day_produce_plan_start_date"));
						map.put("dayProducePlanEndDate", rs.getString("day_produce_plan_end_date"));
						map.put("monthEditPlanStartDate", rs.getString("month_edit_plan_start_date"));
						map.put("monthEditPlanEndDate", rs.getString("month_edit_plan_end_date"));
						map.put("monthProducePlanStartDate", rs.getString("month_produce_plan_start_date"));
						map.put("monthProducePlanEndDate", rs.getString("month_produce_plan_end_date"));
						map.put("inforStatus", rs.getInt("infor_status"));
						map.put("blockId", rs.getInt("block_id"));
						map.put("blockName", rs.getString("block_name"));
						if(totalCount==0){totalCount=rs.getInt("TOTAL_RECORD_NUM_");}
			
						list.add(map);
					}
					page.setResult(list);
					page.setTotalCount(totalCount);
					return page;
				}

			};
			if (null == values || values.size() == 0) {
				return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler);
			}
			return run.query(currentPageNum, pageSize, conn, selectSql, rsHandler, values.toArray());
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("查询列表失败，原因为:" + e.getMessage(), e);
		}
	}
	
	public static void insertInforBlockMapping(Connection conn,JSONArray blockArray,String inforId) throws Exception{
		try{
			QueryRunner run = new QueryRunner();

			String createSql = "insert into infor_block_mapping(infor_id,block_id) values(?,?)";

			Object[][] param = new Object[blockArray.size()][];
			for (int i = 0; i < blockArray.size(); i++) {
				int blockId = blockArray.getInt(i);
				Object[] obj = new Object[] { inforId, blockId};
				param[i] = obj;
			}

			run.batch(conn, createSql, param);	
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new Exception("插入失败，原因为:"+e.getMessage(),e);
		}
	}

}
