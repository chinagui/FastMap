package com.navinfo.dataservice.scripts;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.excel.ExcelReader;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.QueryRunner;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 线下成果导入根据blockID创建对应城市的一个program根据blockID创建三种类型的任务
 * @author song
 * @version 1.0
 * 
 * */
public class ImportPlan {
	
	//默认最早和最晚时间
	private static final String DEFAULT_DATE_BEGAIN = "9999-12-31 00:00:00";
	private static final String DEFAULT_DATE_END = "1970-01-01 00:00:00";
	//这里添加一个默认的日期格式，用于转换
	private static final String DEFAULT_FORMATE = "yyyy-MM-dd HH:mm:ss";
	private static final String DEFAULT_TIME = " 12:00:00";
	//这里没有tocken，没办法从tocken中获取userid，只能先写死一个数据库存在的值直接赋值
	private static final long userID = 0;

	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		JSONArray programUpdateIDs = new JSONArray();
		JSONArray taskIds = new JSONArray();
		String filepath = String.valueOf(args[0]);
		try {
			JobScriptsInterface.initContext();
//			String filepath = "E:/2.xls";
			ImportPlan blockPlan = new ImportPlan();	
			
			// 读取Excel表格内容生成对应条数的blockPlan数据
			List<Map<String, Object>> BlockPlanList = blockPlan.impAddDataExcel(filepath);
			
			//创建项目
			try{
				conn = DBConnector.getInstance().getManConnection();
				List<Map<String,Object>> programList = new ArrayList<>();
				for(Map<String, Object> programMap : BlockPlanList){
					//根据block获取对应cityID
					int blockID = Integer.parseInt(programMap.get("BLOCK_ID").toString());
					int cityID = blockPlan.getCityId(blockID, conn);
					//查询对应city下是否已经有项目存在,城市下无项目才创建，否则不创建项目
					int programCountInCity = blockPlan.programCountInCity(cityID, conn);
					if(programCountInCity == 0){
						programMap.put("CITY_ID", cityID);
						programList.add(programMap);
					}
				}
				//通过cityID对每一个block的数据进行分组
				Map<String,List<Map<String,Object>>> blockListBycity = blockPlan.groupingBlockListBycity(programList);
				//创建项目需要的数据处理，各种时间的取值
				List<Map<String, Object>> programs = blockPlan.conductProgramData(blockListBycity);
				for(Map<String, Object> programMap:programs){
					//创建项目
					int programId = blockPlan.creakProgramByBlockPlan(programMap, conn);
					programUpdateIDs.add(programId);
				}
			}catch(Exception e){
				e.printStackTrace();
				DbUtils.rollbackAndCloseQuietly(conn);
			}finally {
				DbUtils.commitAndCloseQuietly(conn);
			}
			//创建任务
			try{
				conn = DBConnector.getInstance().getManConnection();
				for(Map<String, Object> map : BlockPlanList){
					//保存信息到blockPlan表中
					blockPlan.creatBlockPlan(map, conn);
					
					int blockID = Integer.parseInt(map.get("BLOCK_ID").toString());
					//查询一个block对应city下的有效的program
					int programID = getprogramIdByBlockID(conn , blockID);
					map.put("programID", programID);
					//查询对应block下是否已经有任务存在，该block下没有数据的时候执行创建
					JSONArray tasks = blockPlan.taskCountInBlock(blockID, conn);
					if(tasks.size() == 0){
						blockPlan.getGroupId(map, conn);
						//创建两个不同类型的任务
						blockPlan.creatTaskByBlockPlan(map);
						taskIds.addAll(blockPlan.taskCountInBlock(blockID, conn));
					}
				}
			}catch(Exception e){
				e.printStackTrace();
				DbUtils.rollbackAndCloseQuietly(conn);
			}finally {
				DbUtils.commitAndCloseQuietly(conn);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		//发布项目
		ImportPlan.pushProgram(programUpdateIDs);
		//发布任务
		ImportPlan.pushTask(taskIds);
		System.out.println("执行完成");
		System.exit(0);
	}
	
	
	/**
	 * 发布任务
	 * @param JSONArray
	 * 
	 * */
	public static void pushTask(JSONArray tasks){
		//创建完成后发布项目,任务创建的时候状态已经ok，不用单独处理
		try {
			if(tasks.size() > 0){
				TaskService.getInstance().taskPushMsg(userID, tasks);
			}
		} catch (Exception e) {
			System.out.println("任务发布失败");
		}
	}
	
	
	/**
	 * 发布项目
	 * @param JSONArray
	 * 
	 * */
	public static void pushProgram(JSONArray programUpdateIDs){
		//创建完成后发布项目,任务创建的时候状态已经ok，不用单独处理
		try {
			if(programUpdateIDs.size() > 0){
				ProgramService.getInstance().pushMsg(userID, programUpdateIDs);
			}
		} catch (Exception e) {
			System.out.println("项目发布失败");
		}
	}
	
	/**
	 * 创建blockplan
	 * @param  con
	 * @param  blockplanMap
	 * @throws Exception 
	 * 
	 * */
	public void creatBlockPlan(Map<String,Object> blockPlanMap, Connection con) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			
			String insertPart = "";
			String valuePart = "";
			
			if (StringUtils.isNotEmpty(blockPlanMap.get("BLOCK_ID").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" BLOCK_ID ";
				valuePart+= "'" + blockPlanMap.get("BLOCK_ID").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("BLOCK_NAME").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" BLOCK_NAME ";
				valuePart+= "'" + blockPlanMap.get("BLOCK_NAME").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("CITY_NAME").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" CITY_NAME ";
				valuePart+= "'" + blockPlanMap.get("CITY_NAME").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("COLLECT_PLAN_START_DATE").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" COLLECT_PLAN_START_DATE ";
				valuePart+= "to_date('" + blockPlanMap.get("COLLECT_PLAN_START_DATE").toString() + "','yyyy-mm-dd hh24:mi:ss')";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("COLLECT_PLAN_END_DATE").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" COLLECT_PLAN_END_DATE ";
				valuePart+= "to_date('" + blockPlanMap.get("COLLECT_PLAN_END_DATE").toString() + "','yyyy-mm-dd hh24:mi:ss')";
			};
//			if (StringUtils.isNotEmpty(blockPlanMap.get("ROAD_PLAN_TOTAL").toString())){
//				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
//				insertPart+=" ROAD_PLAN_TOTAL ";
//				valuePart+= "'" + blockPlanMap.get("ROAD_PLAN_TOTAL").toString() + "'";
//			};
//			if (StringUtils.isNotEmpty(blockPlanMap.get("POI_PLAN_TOTAL").toString())){
//				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
//				insertPart+=" POI_PLAN_TOTAL ";
//				valuePart+= "'" + blockPlanMap.get("POI_PLAN_TOTAL").toString() + "'";
//			};
			
			if (StringUtils.isNotEmpty(blockPlanMap.get("ROAD_PLAN_IN").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" ROAD_PLAN_IN ";
				valuePart+= "'" + blockPlanMap.get("ROAD_PLAN_IN").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("ROAD_PLAN_OUT").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" ROAD_PLAN_OUT ";
				valuePart+= "'" + blockPlanMap.get("ROAD_PLAN_OUT").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("POI_PLAN_IN").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" POI_PLAN_IN ";
				valuePart+= "'" + blockPlanMap.get("POI_PLAN_IN").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("POI_PLAN_OUT").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" POI_PLAN_OUT ";
				valuePart+= "'" + blockPlanMap.get("POI_PLAN_OUT").toString() + "'";
			};
			
			if (StringUtils.isNotEmpty(blockPlanMap.get("WORK_KIND").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" WORK_KIND ";
				valuePart+= "'" + blockPlanMap.get("WORK_KIND").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" MONTH_EDIT_PLAN_START_DATE ";
				valuePart+= "to_date('" + blockPlanMap.get("MONTH_EDIT_PLAN_START_DATE").toString() + "','yyyy-mm-dd hh24:mi:ss')";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" MONTH_EDIT_PLAN_END_DATE ";
				valuePart+= "to_date('" + blockPlanMap.get("MONTH_EDIT_PLAN_END_DATE").toString() + "','yyyy-mm-dd hh24:mi:ss')";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("PRODUCE_PLAN_END_DATE").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" PRODUCE_PLAN_END_DATE ";
				valuePart+= "to_date('" + blockPlanMap.get("PRODUCE_PLAN_END_DATE").toString() + "','yyyy-mm-dd hh24:mi:ss')";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("PRODUCE_PLAN_START_DATE").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" PRODUCE_PLAN_START_DATE ";
				valuePart+= "to_date('" + blockPlanMap.get("PRODUCE_PLAN_START_DATE").toString() + "','yyyy-mm-dd hh24:mi:ss')";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("LOT").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" LOT ";
				valuePart+= "'" + blockPlanMap.get("LOT").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("DESCP").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" DESCP ";
				valuePart+= "'" + blockPlanMap.get("DESCP").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("IS_PLAN").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" IS_PLAN ";
				valuePart+= "'" + blockPlanMap.get("IS_PLAN").toString() + "'";
			};
			
			String createSql = "insert into BLOCK_PLAN ("+insertPart+") values("+valuePart+")";
			run.execute(con, createSql);		
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 根据blockID判断是否已经存在任务
	 * @author 宋鹤
	 * @param  con
	 * @param  blockID
	 * @throws Exception 
	 * 
	 * */
	public JSONArray taskCountInBlock(int blockID, Connection conn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.task_id, t.type, t.GROUP_ID from task t where t.block_id = " + blockID;

			ResultSetHandler<JSONArray> rsHandler = new ResultSetHandler<JSONArray>() {
				public JSONArray handle(ResultSet rs) throws SQLException {
					//1有数据，不创建任务；0无数据，创建任务
					JSONArray tasks = new JSONArray();
					while(rs.next()){
						int type = rs.getInt("type");
						int groupId = rs.getInt("GROUP_ID");
						//采集任务有没有作业组都可以发布,非采集任务必须有作业组
						if(groupId != 0 || type == 0){
							tasks.add(rs.getInt("task_id"));
						}
					}
					return tasks;
				}
			};
			return run.query(conn, sql, rsHandler);	
		}catch(Exception e){
			throw e;
		}
	}
	
	/**
	 * 根据cityID判断城市下是否已经有项目
	 * @author 宋鹤
	 * @param  con
	 * @param  blockID
	 * @throws Exception 
	 * 
	 * */
	public int programCountInCity(int cityID, Connection conn) throws Exception{
		int count = 0;
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.program_id from PROGRAM t where t.city_id = " + cityID + "and t.latest = 1";

			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					//1有数据，不创建任务；0无数据，创建任务
					if(rs.next()){
						return 1;
					}else{
						return 0;
					}
				}
			};
			
			count = run.query(conn, sql, rsHandler);	
		}catch(Exception e){
			throw e;
		}
		return count;
	}
	
	
	/**
	 * @param 增量数据upFile
	 * @return
	 * @throws Exception 
	 */
	private List<Map<String, Object>> impAddDataExcel(String upFile) throws Exception {
		ExcelReader excleReader = new ExcelReader(upFile);
		Map<String,String> excelHeader = new HashMap<String,String>();
		excelHeader.put("BLOCK_ID", "BLOCK_ID");
		excelHeader.put("BLOCK_NAME", "BLOCK_NAME");
		excelHeader.put("CITY_NAME", "CITY_NAME");
		excelHeader.put("COLLECT_PLAN_START_DATE", "COLLECT_PLAN_START_DATE");
		excelHeader.put("COLLECT_PLAN_END_DATE", "COLLECT_PLAN_END_DATE");
		excelHeader.put("ROAD_PLAN_IN", "ROAD_PLAN_IN");
		excelHeader.put("ROAD_PLAN_OUT", "ROAD_PLAN_OUT");
		excelHeader.put("POI_PLAN_IN", "POI_PLAN_IN");
		excelHeader.put("POI_PLAN_OUT", "POI_PLAN_OUT");
		excelHeader.put("WORK_KIND", "WORK_KIND");
		excelHeader.put("MONTH_EDIT_PLAN_START_DATE", "MONTH_EDIT_PLAN_START_DATE");
		excelHeader.put("MONTH_EDIT_PLAN_END_DATE", "MONTH_EDIT_PLAN_END_DATE");
		excelHeader.put("PRODUCE_PLAN_END_DATE", "PRODUCE_PLAN_END_DATE");		
		excelHeader.put("PRODUCE_PLAN_START_DATE", "PRODUCE_PLAN_START_DATE");
		excelHeader.put("LOT", "LOT");
		excelHeader.put("DESCP", "DESCP");
		excelHeader.put("IS_PLAN", "IS_PLAN");
		
		List<Map<String, Object>> sources = excleReader.readExcelContent(excelHeader);
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> map : sources){

			if(map.get("COLLECT_PLAN_START_DATE") != null && map.get("COLLECT_PLAN_START_DATE").toString().length() == 10){
				map.put("COLLECT_PLAN_START_DATE", (map.get("COLLECT_PLAN_START_DATE") == null) ? "" : map.get("COLLECT_PLAN_START_DATE").toString() + DEFAULT_TIME);
			}
			if(map.get("COLLECT_PLAN_END_DATE") != null && map.get("COLLECT_PLAN_END_DATE").toString().length() == 10){
				map.put("COLLECT_PLAN_END_DATE", (map.get("COLLECT_PLAN_END_DATE") == null) ? "" : map.get("COLLECT_PLAN_END_DATE").toString() + DEFAULT_TIME);
			}
			if(map.get("MONTH_EDIT_PLAN_START_DATE") != null && map.get("MONTH_EDIT_PLAN_START_DATE").toString().length() == 10){
				map.put("MONTH_EDIT_PLAN_START_DATE", (map.get("MONTH_EDIT_PLAN_START_DATE") == null) ? "" : map.get("MONTH_EDIT_PLAN_START_DATE").toString() + DEFAULT_TIME);
			}
			if(map.get("MONTH_EDIT_PLAN_END_DATE") != null && map.get("MONTH_EDIT_PLAN_END_DATE").toString().length() == 10){
				map.put("MONTH_EDIT_PLAN_END_DATE", (map.get("MONTH_EDIT_PLAN_END_DATE") == null) ? "" : map.get("MONTH_EDIT_PLAN_END_DATE").toString() + DEFAULT_TIME);
			}
			if(map.get("PRODUCE_PLAN_END_DATE") != null && map.get("PRODUCE_PLAN_END_DATE").toString().length() == 10){
				map.put("PRODUCE_PLAN_END_DATE", (map.get("PRODUCE_PLAN_END_DATE") == null) ? "" : map.get("PRODUCE_PLAN_END_DATE").toString() + DEFAULT_TIME);
			}
			if(map.get("PRODUCE_PLAN_START_DATE") != null && map.get("PRODUCE_PLAN_START_DATE").toString().length() == 10){
				map.put("PRODUCE_PLAN_START_DATE", (map.get("PRODUCE_PLAN_START_DATE") == null) ? "" : map.get("PRODUCE_PLAN_START_DATE").toString() + DEFAULT_TIME);
			}
			
			int isPlan = Integer.parseInt(map.get("IS_PLAN").toString());
			if(isPlan == 1){
				result.add(map);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * 对应block查询program
	 * @param blokID
	 * @param conn
	 * @throws Exception 
	 * */
	public static int getprogramIdByBlockID(Connection conn , int blockID) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.program_id from PROGRAM t,block b where b.city_id = t.city_id and t.latest = 1 and b.block_id = " + blockID;

			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					int programID = 0;
					if(rs.next()){
						programID  = rs.getInt("program_id");
					}
					return programID;
				}
			};
			return run.query(conn, sql, rsHandler);
		}catch(Exception e){
			throw e;
		}
	}
	

	/**
	 * 
	 * 处理TaskBEAN，创建task
	 * @param taskDataMap
	 * @param conn
	 * */
	public void creatTaskByBlockPlan(Map<String, Object> taskDataMap) throws Exception{
		JSONArray list = new JSONArray();
		JSONObject json = new JSONObject();
		try{
			//一个区县下创建两个任务
			for(int i = 0; i < 2; i++){
				JSONObject taskJson = new JSONObject();
				taskJson.put("name", taskDataMap.get("BLOCK_NAME").toString()+ "_" + df.format(new Date()));
				taskJson.put("blockId", Integer.parseInt(taskDataMap.get("BLOCK_ID").toString()));
				taskJson.put("programId", Integer.parseInt(taskDataMap.get("programID").toString()));
				taskJson.put("lot", 0);
				//三种type类型分别创建
				if(i == 0){
					taskJson.put("type", 0);
					
					if(StringUtils.isNotBlank(taskDataMap.get("WORK_KIND").toString())){
						//这个得特殊处理
						List<Integer> kind = new ArrayList<Integer>();
						String result = taskDataMap.get("WORK_KIND").toString().replace("|", "");
						for(int k = 0; k < result.length(); k++){
							int digit = Integer.parseInt(String.valueOf(result.charAt(k)));
							if(digit == 1){
								//为了转换成task封装的对应的workkind数据
								kind.add(k + 1);
							}
						}
						
						taskJson.put("workKind", kind);
					}
					
					if(StringUtils.isNotBlank(taskDataMap.get("COLLECT_PLAN_START_DATE").toString())){
						taskJson.put("planStartDate", df.format(DateUtils.parseDateTime2(taskDataMap.get("COLLECT_PLAN_START_DATE").toString())));
					}
					if(StringUtils.isNotBlank(taskDataMap.get("COLLECT_PLAN_END_DATE").toString())){
						taskJson.put("planEndDate",  df.format(DateUtils.parseDateTime2(taskDataMap.get("COLLECT_PLAN_END_DATE").toString())));
					}
					String workKind = taskDataMap.get("WORK_KIND").toString().substring(0, 3);
					//这里workkind特定情况下才赋值组ID，其他情况不赋值
					int type = 0;
					if("0|1".equals(workKind) || "1|0".equals(workKind) || "1|1".equals(workKind)){
						type = 1;
					}
					if(type == 1 && taskDataMap.containsKey("COLECTION_GROUP_ID") && StringUtils.isNotBlank(taskDataMap.get("COLECTION_GROUP_ID").toString())){
						taskJson.put("groupId", Integer.parseInt(taskDataMap.get("COLECTION_GROUP_ID").toString()));
					}
				}else if(i == 1){
					taskJson.put("type", 2);
					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
						taskJson.put("planEndDate", df.format(DateUtils.parseDateTime2(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString())));
					}
					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
						taskJson.put("planStartDate", df.format(DateUtils.parseDateTime2(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString())));
					}
					if(taskDataMap.containsKey("MONTH_GROUP_ID") && StringUtils.isNotBlank(taskDataMap.get("MONTH_GROUP_ID").toString())){
						taskJson.put("groupId", Integer.parseInt(taskDataMap.get("MONTH_GROUP_ID").toString()));
					}
				}
//				else{
//					taskJson.put("type", 3);
//					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
//						taskJson.put("planEndDate", df.format(DateUtils.parseDateTime2(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString())));
//					}
//					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
//						taskJson.put("planStartDate", df.format(DateUtils.parseDateTime2(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString())));
//					}
//					//非采集以及月编任务的数据，先赋值为0
//					taskJson.put("groupId", 0);
//				}
				taskJson.put("descp", taskDataMap.get("DESCP").toString());
				taskJson.put("createUserId", userID);
//				if(StringUtils.isNotBlank(taskDataMap.get("ROAD_PLAN_TOTAL").toString())){
//					taskJson.put("roadPlanTotal", Integer.parseInt(taskDataMap.get("ROAD_PLAN_TOTAL").toString()));
//				}
//				if(StringUtils.isNotBlank(taskDataMap.get("POI_PLAN_TOTAL").toString())){
//					taskJson.put("poiPlanTotal", Integer.parseInt(taskDataMap.get("POI_PLAN_TOTAL").toString()));
//				}
				
				if(StringUtils.isNotBlank(taskDataMap.get("ROAD_PLAN_IN").toString())){
					taskJson.put("roadPlanIn", Integer.parseInt(taskDataMap.get("ROAD_PLAN_IN").toString()));
				}
				if(StringUtils.isNotBlank(taskDataMap.get("ROAD_PLAN_OUT").toString())){
					taskJson.put("roadPlanOut", Integer.parseInt(taskDataMap.get("ROAD_PLAN_OUT").toString()));
				}
				if(StringUtils.isNotBlank(taskDataMap.get("POI_PLAN_IN").toString())){
					taskJson.put("poiPlanIn", Integer.parseInt(taskDataMap.get("POI_PLAN_IN").toString()));
				}
				if(StringUtils.isNotBlank(taskDataMap.get("POI_PLAN_OUT").toString())){
					taskJson.put("poiPlanOut", Integer.parseInt(taskDataMap.get("POI_PLAN_OUT").toString()));
				}

				if(StringUtils.isNotBlank(taskDataMap.get("LOT").toString())){
					taskJson.put("lot", Integer.parseInt(taskDataMap.get("LOT").toString()));
				}
				if(StringUtils.isNotBlank(taskDataMap.get("PRODUCE_PLAN_START_DATE").toString())){
					taskJson.put("producePlanStartDate", df.format(DateUtils.parseDateTime2(taskDataMap.get("PRODUCE_PLAN_START_DATE").toString())));
				}
				if(StringUtils.isNotBlank(taskDataMap.get("PRODUCE_PLAN_END_DATE").toString())){
					taskJson.put("producePlanEndDate", df.format(DateUtils.parseDateTime2(taskDataMap.get("PRODUCE_PLAN_END_DATE").toString())));
				}
				
				//拼装创建时候的数据格式....
				list.add(taskJson);
				}
			json.put("tasks", list);
			TaskService.getInstance().create(userID, json);
			}catch(Exception e){
				throw new Exception(e);
			}
	}
	
	/**
	 * 处理programBEAN，创建program
	 * @param programMap
	 * @param conn
	 * @throws Exception 
	 * 
	 * */
	public int creakProgramByBlockPlan(Map<String, Object> programMap, Connection conn) throws Exception{
		Program program = new Program();
		try{
			program.setName(programMap.get("NAME").toString() + "_" + df.format(new Date()));
			program.setType(1);
			program.setDescp("");
			program.setLatest(1);
			if(StringUtils.isNotBlank(programMap.get("PLAN_START_DATE").toString())){
				program.setPlanStartDate(DateUtils.stringToTimestamp(programMap.get("PLAN_START_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("PLAN_END_DATE").toString())){
				program.setPlanEndDate(DateUtils.stringToTimestamp(programMap.get("PLAN_END_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("COLLECT_PLAN_START_DATE").toString())){
				program.setCollectPlanStartDate(DateUtils.stringToTimestamp(programMap.get("COLLECT_PLAN_START_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("COLLECT_PLAN_END_DATE").toString())){
				program.setCollectPlanEndDate(DateUtils.stringToTimestamp(programMap.get("COLLECT_PLAN_END_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
				program.setMonthEditPlanStartDate(DateUtils.stringToTimestamp(programMap.get("MONTH_EDIT_PLAN_START_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
				program.setMonthEditPlanEndDate(DateUtils.stringToTimestamp(programMap.get("MONTH_EDIT_PLAN_END_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("PRODUCE_PLAN_START_DATE").toString())){
				program.setProducePlanStartDate(DateUtils.stringToTimestamp(programMap.get("PRODUCE_PLAN_START_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("PRODUCE_PLAN_END_DATE").toString())){
				program.setProducePlanEndDate(DateUtils.stringToTimestamp(programMap.get("PRODUCE_PLAN_END_DATE").toString(), DEFAULT_FORMATE));
			}
			if(StringUtils.isNotBlank(programMap.get("CITY_ID").toString())){
				program.setCityId(Integer.parseInt(programMap.get("CITY_ID").toString()));
			}
			program.setCreateUserId(0);
			
			//创建项目
			return ProgramService.getInstance().create(conn, program);
		}catch(Exception e){
			throw new Exception(e);
		}
	}
	
	/**
	 * 根据任务的数据获取grouID
	 * @param 数据不完整的taskMap
	 * @param conn
	 * @return 创建task所需的信息
	 * 这里groupID有两种，所以执行两个查询全部放到map中
	 * 
	 */
	public void getGroupId(Map<String, Object> taskMap, Connection conn) throws Exception{

		try{
			QueryRunner run = new QueryRunner();
			String cityName = taskMap.get("CITY_NAME").toString();
			
			String sql = "select ug.group_id from user_group ug where ug.group_name = (" 
					+ "select t.COLLECT_GROUP_NAME from admin_group_mapping t, city c "
					+ "where t.admin_code = c.admin_id "
					+ "and c.city_name = '" + cityName + "')";
					
			String selsect ="select ug.group_id from user_group ug where ug.group_name = ("
					+ "select t.month_group_name from admin_group_mapping t, city c where t.admin_code = c.admin_id "
					+ "and c.city_name = '"+ cityName + "')";

			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					int group = 0;
					if (rs.next()) {
						group = rs.getInt("group_id");
					}
					return group;
				}
			};
			
			taskMap.put("MONTH_GROUP_ID", run.query(conn, selsect, rsHandler));
			taskMap.put("COLECTION_GROUP_ID", run.query(conn, sql, rsHandler));
		}catch(Exception e){
			throw new Exception(e);
		}
	}
	
	/**
	 * 根据blockID获取cityID
	 * @param blockID
	 * @param conn
	 * @return 创建program所需的cityID信息
	 * 
	 */
	public int getCityId(int blockID, Connection conn) throws Exception{
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.city_id from block t where t.block_id = " + blockID;

			ResultSetHandler<Integer> rsHandler = new ResultSetHandler<Integer>() {
				public Integer handle(ResultSet rs) throws SQLException {
					int cityID = 0;
					if(rs.next()){
						cityID = rs.getInt("city_id");
					}
					return cityID;
				}
			};

			return run.query(conn, sql, rsHandler);
		}catch(Exception e){
			throw new Exception(e);
		}
	}
	
	/*
	 * 根据cityID分组block
	 * 
	 * */
	public Map<String,List<Map<String,Object>>> groupingBlockListBycity(List<Map<String,Object>> programList){
		int programSize = programList.size();
		if(programSize == 0){
			return new HashMap<>();
		}
		Map<String,Object> blockMap = new HashMap<>();
		String cityID = "";
		Map<String,List<Map<String,Object>>> cityMap = new HashMap<>();
		//类似冒泡排序循环对每一个block进行city的分组处理
		for(int j = 0; j < programSize; j++){
			Map<String,Object> map = programList.get(j);
			
			cityID = map.get("CITY_ID").toString();
			if(!cityMap.containsKey(cityID)){
				cityMap.put(cityID, new ArrayList<Map<String,Object>>());
			}
			cityMap.get(cityID).add(map);
		}
		return cityMap;
	}
	
	
	/**
	 * 处理按照城市分类后的block的各种时间数据
	 * @param map同一个城市下的所有block数据
	 * @return programMap创建项目的所有数据
	 * @throws ParseException 
	 * 
	 * */
	
	public List<Map<String, Object>> conductProgramData(Map<String,List<Map<String,Object>>> map) throws Exception{
		//先定义要用到的变量
		String city_name = "";
		int city_id = 0;
		
		List<Map<String,Object>> programList = new ArrayList<Map<String, Object>>();
		for (String key : map.keySet()) {
			String plan_start_date = DEFAULT_DATE_BEGAIN;
			String plan_end_date = DEFAULT_DATE_END;
			String collection_plan_start_date = DEFAULT_DATE_BEGAIN;
			String collection_plan_end_date = DEFAULT_DATE_END;
			String month_edit_plan_start_date = DEFAULT_DATE_BEGAIN;
			String month_edit_plan_end_date = DEFAULT_DATE_END;
			String produce_plan_start_date = DEFAULT_DATE_BEGAIN;
			String produce_plan_end_date = DEFAULT_DATE_END;
			List<Map<String,Object>> blockList = map.get(key);
			Map<String, Object> programMap = new HashMap<>();
			for(Map<String, Object> blockMap : blockList){
				//处理时间
				if(StringUtils.isNotBlank(blockMap.get("COLLECT_PLAN_START_DATE").toString())){
					if(plan_start_date.compareTo(blockMap.get("COLLECT_PLAN_START_DATE").toString()) > 0){
						plan_start_date = blockMap.get("COLLECT_PLAN_START_DATE").toString();
					}
				}else{
					plan_start_date = "";
				}
				if(StringUtils.isNotBlank(blockMap.get("PRODUCE_PLAN_END_DATE").toString())){
					if(plan_end_date.compareTo(blockMap.get("PRODUCE_PLAN_END_DATE").toString()) < 0){
						plan_end_date = blockMap.get("PRODUCE_PLAN_END_DATE").toString();
					}
				}else{
					plan_end_date = "";
				}
				if(StringUtils.isNotBlank(blockMap.get("COLLECT_PLAN_START_DATE").toString())){
					if(collection_plan_start_date.compareTo(blockMap.get("COLLECT_PLAN_START_DATE").toString()) > 0){
						collection_plan_start_date = blockMap.get("COLLECT_PLAN_START_DATE").toString();
					}
				}else{
					collection_plan_start_date = "";
				}
				if(StringUtils.isNotBlank(blockMap.get("COLLECT_PLAN_END_DATE").toString())){
					if(collection_plan_end_date.compareTo(blockMap.get("COLLECT_PLAN_END_DATE").toString()) < 0){
						collection_plan_end_date = blockMap.get("COLLECT_PLAN_END_DATE").toString();
					}
				}else{
					collection_plan_end_date = "";
				}
				if(StringUtils.isNotBlank(blockMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
					if(month_edit_plan_start_date.compareTo(blockMap.get("MONTH_EDIT_PLAN_START_DATE").toString()) > 0){
						month_edit_plan_start_date = blockMap.get("MONTH_EDIT_PLAN_START_DATE").toString();
					}
				}else{
					month_edit_plan_start_date = "";
				}
				if(StringUtils.isNotBlank(blockMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
					if(month_edit_plan_end_date.compareTo(blockMap.get("MONTH_EDIT_PLAN_END_DATE").toString()) < 0){
						month_edit_plan_end_date = blockMap.get("MONTH_EDIT_PLAN_END_DATE").toString();
					}
				}else{
					month_edit_plan_end_date = "";
				}
				if(StringUtils.isNotBlank(blockMap.get("PRODUCE_PLAN_START_DATE").toString())){
					if(produce_plan_start_date.compareTo(blockMap.get("PRODUCE_PLAN_START_DATE").toString()) > 0){
						produce_plan_start_date = blockMap.get("PRODUCE_PLAN_START_DATE").toString();
					}
				}else{
					produce_plan_start_date = "";
				}
				if(StringUtils.isNotBlank(blockMap.get("PRODUCE_PLAN_END_DATE").toString())){
					if(produce_plan_end_date.compareTo(blockMap.get("PRODUCE_PLAN_END_DATE").toString()) < 0){
						produce_plan_end_date = blockMap.get("PRODUCE_PLAN_END_DATE").toString();
					}
				}else{
					produce_plan_end_date = "";
				}
			
				city_name = blockMap.get("CITY_NAME").toString();
				city_id = Integer.parseInt(blockMap.get("CITY_ID").toString());
			}
			programMap.put("NAME", city_name);
			programMap.put("TYPE", 1);
			programMap.put("DESCP", "");
			programMap.put("PLAN_START_DATE", plan_start_date);
			programMap.put("PLAN_END_DATE", plan_end_date);
			programMap.put("COLLECT_PLAN_START_DATE", collection_plan_start_date);
			programMap.put("COLLECT_PLAN_END_DATE", collection_plan_end_date);
			programMap.put("MONTH_EDIT_PLAN_START_DATE", month_edit_plan_start_date);
			programMap.put("MONTH_EDIT_PLAN_END_DATE", month_edit_plan_end_date);
			programMap.put("PRODUCE_PLAN_START_DATE", produce_plan_start_date);
			programMap.put("PRODUCE_PLAN_END_DATE", produce_plan_end_date);
			programMap.put("CITY_ID", city_id);
			programMap.put("STATUS", 1);
			programMap.put("CREATE_USER_ID", "");
			
			programList.add(programMap);
		}
		return programList;
	}
	
}
