package com.navinfo.dataservice.engine.man.inforMan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;
import org.json.JSONException;

import com.navinfo.dataservice.api.man.model.BlockMan;
import com.navinfo.dataservice.api.man.model.Infor;
import com.navinfo.dataservice.api.man.model.InforMan;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
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
	
	public static void updateTask(Connection conn,String inforId,int taskId) throws Exception{
		String sql="update infor set plan_status=1,task_id="+taskId+" where infor_id='"+inforId+"'";
		QueryRunner run = new QueryRunner();
		run.update(conn,sql);
	}
	
	public static void updatePlanStatus(Connection conn,String inforId,int planStatus) throws Exception{
		String sql="update infor set plan_status="+planStatus+" where infor_id='"+inforId+"'";
		QueryRunner run = new QueryRunner();
		run.update(conn,sql);
	}
	
	public static void closeByTasks(Connection conn,List<Integer> taskIds) throws Exception{
		String sql="UPDATE INFOR I SET I.PLAN_STATUS = 2 WHERE I.TASK_ID IN ("+taskIds.toString().replace("[", "").
						replace("]", "").replace("\"", "")+") AND I.PLAN_STATUS = 1";
		QueryRunner run = new QueryRunner();
		run.update(conn,sql);
	}
	
	/*
	 * 根据sql语句查询inforMan
	 * private String inforName;
	private String geometry;
	private Integer inforLevel;
	private Integer planStatus;
	private String inforContent;
	private Integer taskId;
	private Timestamp insertTime;
	 */
	public static List<HashMap<String,Object>> selectTaskBySql2(Connection conn,String selectSql,List<Object> values) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			ResultSetHandler<List<HashMap<String,Object>>> rsHandler = new ResultSetHandler<List<HashMap<String,Object>>>(){
				public List<HashMap<String,Object>> handle(ResultSet rs) throws SQLException {
					List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
					while(rs.next()){
						HashMap<String,Object> map = new HashMap<String,Object>();
						map.put("inforId", rs.getString("INFOR_ID"));
						map.put("inforName", rs.getString("INFOR_NAME"));
						JSONArray geoList = new JSONArray();
						String inforGeo=rs.getString("GEOMETRY");
						String[] inforGeoList=inforGeo.split(";");
						for(String geoTmp:inforGeoList){
							try {
								geoList.add(GeoTranslator.jts2Geojson(GeoTranslator.wkt2Geometry(geoTmp)));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						map.put("geometry", geoList);
						map.put("inforLevel",rs.getInt("INFOR_LEVEL"));
						map.put("planStatus",rs.getInt("PLAN_STATUS"));
						map.put("inforContent",rs.getString("INFOR_CONTENT"));
						map.put("taskId",rs.getInt("TASK_ID"));						
						map.put("insertTime",rs.getTimestamp("INSERT_TIME"));
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
