package com.navinfo.dataservice.scripts;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.navinfo.dataservice.api.man.model.Program;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.Task;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.program.ProgramService;
import com.navinfo.dataservice.engine.man.subtask.SubtaskService;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.navicommons.database.QueryRunner;

/**
 * @author songhe
 * @version 1.0
 * @deprecated 线下成果导入根据blockID创建对应城市的一个program根据blockID创建三种类型的任务
 * 
 * */
public class ImportPlan {
	
	//默认最早和最晚时间
	private static final String DEFAULT_DATE_BEGAIN = "9999-12-31 00:00:00";
	private static final String DEFAULT_DATE_END = "1970-01-01 00:00:00";
	//用于记录查询groupID的次数
	private static int SELECT_TIMES = 0;

	private Workbook wb;
	private Sheet sheet;
	private Row row;
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	
	public static void main(String[] args) throws SQLException {
		Connection conn = null;
		String filepath = String.valueOf(args[0]);
		try {
			JobScriptsInterface.initContext();
//			String filepath = "E:/2.xls";
			ImportPlan blockPlan = new ImportPlan(filepath);	
			
			// 读取Excel表格内容生成对应条数的blockPlan数据
			List<Map<String, Object>> BlockPlanList = blockPlan.readExcelContent();
			
			conn = DBConnector.getInstance().getManConnection();
			for(Map<String, Object> map : BlockPlanList){
				//保存信息到blockPlan表中
				blockPlan.creatBlockPlan(map, conn);
				
				int blockID = Integer.parseInt(map.get("BLOCK_ID").toString());
				//查询对应block下是否已经有任务存在，该block下没有数据的时候执行创建
				int taskCountInBlock = blockPlan.taskCountInBlock(blockID, conn);
				if(taskCountInBlock == 0){
					//这里每次一个新的blockPlan都需要重置groupID的查询次数
					SELECT_TIMES = 0;
					Map<String, Object> taskDataMap = blockPlan.getGroupId(map, conn);
					//创建三个不同类型的任务
					blockPlan.creatTaskByBlockPlan(taskDataMap, conn);
				}
			}
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
				blockPlan.creakProgramByBlockPlan(programMap, conn);
			}
			
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
			DbUtils.rollbackAndCloseQuietly(conn);
		}finally{
			DbUtils.commitAndClose(conn);
		}
	}
	
	/**
	 * 创建blockplan
	 * @author 宋鹤
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
			if (StringUtils.isNotEmpty(blockPlanMap.get("ROAD_PLAN_TOTAL").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" ROAD_PLAN_TOTAL ";
				valuePart+= "'" + blockPlanMap.get("ROAD_PLAN_TOTAL").toString() + "'";
			};
			if (StringUtils.isNotEmpty(blockPlanMap.get("POI_PLAN_TOTAL").toString())){
				if(StringUtils.isNotEmpty(insertPart)){insertPart+=" , ";valuePart+=" , ";}
				insertPart+=" POI_PLAN_TOTAL ";
				valuePart+= "'" + blockPlanMap.get("POI_PLAN_TOTAL").toString() + "'";
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
	public int taskCountInBlock(int blockID, Connection conn) throws Exception{
		int count = 0;
		try{
			QueryRunner run = new QueryRunner();
			String sql = "select t.task_id from task t where t.block_id = " + blockID;

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
			String sql = "select t.program_id from PROGRAM t where t.city_id = " + cityID;

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
	 *判断excel表格 
	 * 
	 * 
	 * */
	public ImportPlan(String filepath) {
		if(filepath==null){
			return;
		}
		String ext = filepath.substring(filepath.lastIndexOf("."));
		try {
			InputStream is = new FileInputStream(filepath);
			if(".xls".equals(ext)){
				wb = new HSSFWorkbook(is);
			}else if(".xlsx".equals(ext)){
				wb = new XSSFWorkbook(is);
			}else{
				wb=null;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	/**
	 * 读取Excel表格表头的内容
	 * 
	 * @param InputStream
	 * @return String 表头内容的数组
	 * @author songhe
	 */
	public String[] readExcelTitle() throws Exception{
		if(wb==null){
			throw new Exception("Workbook对象为空！");
		}
		sheet = wb.getSheetAt(0);
		row = sheet.getRow(0);
		// 标题总列数
		int colNum = row.getPhysicalNumberOfCells();
		String[] title = new String[colNum];
		for (int i = 0; i < colNum; i++) {
			// title[i] = getStringCellValue(row.getCell((short) i));
			title[i] = row.getCell(i).getCellFormula();
		}
		return title;
	}

	/**
	 * 读取Excel数据内容
	 * 
	 * @param 
	 * @return List 包含单元格数据内容的Map对象
	 * @author songhe
	 */
	public List<Map<String, Object>> readExcelContent() throws Exception{
		if(wb==null){
			throw new Exception("Workbook对象为空！");
		}
		List<Map<String, Object>> BlockPlanList = new ArrayList<>();
		
		sheet = wb.getSheetAt(0);
		// 得到总行数
		int rowNum = sheet.getLastRowNum();
		row = sheet.getRow(0);
		int colNum = row.getPhysicalNumberOfCells();
		// 正文内容应该从第二行开始,第一行为表头的标题
		for (int i = 1; i <= rowNum; i++) {
			row = sheet.getRow(i);
			
			int j = 0;
			Map<String, Object> cellValue = new HashMap<String, Object>();
			String IS_PLAN = getCellFormatValue(row.getCell(14));
			
			while (j < colNum && ("1".equals(IS_PLAN))) {
				String obj = getCellFormatValue(row.getCell(j));
				
				if( j == 0){
					cellValue.put("BLOCK_ID", obj);
				}else if(j == 1){
					cellValue.put("BLOCK_NAME", obj);
				}else if(j == 2){
					cellValue.put("CITY_NAME", obj);
				}else if(j == 3){
					cellValue.put("COLLECT_PLAN_START_DATE", obj);
				}else if(j == 4){
					cellValue.put("COLLECT_PLAN_END_DATE", obj);
				}else if(j == 5){
					cellValue.put("ROAD_PLAN_TOTAL", obj);
				}else if(j == 6){
					cellValue.put("POI_PLAN_TOTAL", obj);
				}else if(j == 7){
					cellValue.put("WORK_KIND", obj);
				}else if(j == 8){
					cellValue.put("MONTH_EDIT_PLAN_START_DATE", obj);
				}else if(j == 9){
					cellValue.put("MONTH_EDIT_PLAN_END_DATE", obj);
				}else if(j == 10){
					cellValue.put("PRODUCE_PLAN_END_DATE", obj);
				}else if(j == 11){
					cellValue.put("PRODUCE_PLAN_START_DATE", obj);
				}else if(j == 12){
					cellValue.put("LOT", obj);
				}else if(j == 13){
					cellValue.put("DESCP", obj);
				}else if(j == 14){
					cellValue.put("IS_PLAN", obj);
				}
				j++;
			}
			if(cellValue.containsKey("BLOCK_ID")){
				BlockPlanList.add(cellValue);
			}
		}
		return BlockPlanList;
	}

	/**
	 * 
	 * 根据Cell类型设置数据
	 * @param cell
	 * @return
	 * @author songhe
	 */
	private String getCellFormatValue(Cell cell) {
		String cellvalue = "";
		if (cell != null) {
			// 判断当前Cell的Type
			switch (cell.getCellType()) {
			case Cell.CELL_TYPE_NUMERIC:// 如果当前Cell的Type为NUMERIC
			case Cell.CELL_TYPE_FORMULA: {
				// 判断当前的cell是否为Date
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					cellvalue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(HSSFDateUtil.getJavaDate(cell.getNumericCellValue()));
				} else {// 如果是纯数字
					cellvalue = String.valueOf((int) cell.getNumericCellValue());
				}
				break;
			}
			case Cell.CELL_TYPE_STRING:// 如果当前Cell的Type为STRING
				// 取得当前的Cell字符串
				cellvalue = cell.getRichStringCellValue().getString();
				break;
			default:// 默认的Cell值
				cellvalue = "";
			}
		} else {
			cellvalue = "";
		}
		return cellvalue;
	}

	/**
	 * 
	 * 处理TaskBEAN，创建task
	 * @param taskDataMap
	 * @param conn
	 * */
	public void creatTaskByBlockPlan(Map<String, Object> taskDataMap, Connection conn) throws Exception{
		Task task = new Task();
		try{
			//一个区县下创建三个任务
			for(int i = 0; i < 3; i++){
				task.setName(taskDataMap.get("BLOCK_NAME").toString()+ "_" + df.format(new Date()));
				//三种type类型分别创建
				if(i == 0){
					task.setType(0);
					if(StringUtils.isNotBlank(taskDataMap.get("COLLECT_PLAN_START_DATE").toString())){
						task.setPlanStartDate(Timestamp.valueOf(taskDataMap.get("COLLECT_PLAN_START_DATE").toString()));
					}
					if(StringUtils.isNotBlank(taskDataMap.get("COLLECT_PLAN_END_DATE").toString())){
						task.setPlanEndDate(Timestamp.valueOf(taskDataMap.get("COLLECT_PLAN_END_DATE").toString()));
					}
					String workKind = taskDataMap.get("WORK_KIND").toString().substring(0, 3);
					//这里workkind特定情况下才赋值组ID，其他情况不赋值
					int type = 0;
					if("0|1".equals(workKind) || "1|0".equals(workKind) || "1|1".equals(workKind)){
						type = 1;
					}
					if(type == 1 && taskDataMap.containsKey("COLECTION_GROUP_ID") && StringUtils.isNotBlank(taskDataMap.get("COLECTION_GROUP_ID").toString())){
						task.setGroupId(Integer.parseInt(taskDataMap.get("COLECTION_GROUP_ID").toString()));
					}
				}else if(i == 1){
					task.setType(2);
					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
						task.setPlanEndDate(Timestamp.valueOf(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString()));
					}
					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
						task.setPlanStartDate(Timestamp.valueOf(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString()));
					}
					if(taskDataMap.containsKey("MONTH_GROUP_ID") && StringUtils.isNotBlank(taskDataMap.get("MONTH_GROUP_ID").toString())){
						task.setGroupId(Integer.parseInt(taskDataMap.get("MONTH_GROUP_ID").toString()));
					}
				}else{
					task.setType(3);
					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
						task.setPlanStartDate(Timestamp.valueOf(taskDataMap.get("MONTH_EDIT_PLAN_END_DATE").toString()));
					}
					if(StringUtils.isNotBlank(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
						task.setPlanEndDate(Timestamp.valueOf(taskDataMap.get("MONTH_EDIT_PLAN_START_DATE").toString()));
					}
					//非采集以及月编任务的数据，先赋值为0
					task.setGroupId(0);
				}
				task.setDescp(taskDataMap.get("DESCP").toString());
				//任务状态赋值为草稿2
				task.setStatus(2);
				task.setCreateUserId(null);
				if(StringUtils.isNotBlank(taskDataMap.get("ROAD_PLAN_TOTAL").toString())){
					task.setRoadPlanTotal(Integer.parseInt(taskDataMap.get("ROAD_PLAN_TOTAL").toString()));
				}
				if(StringUtils.isNotBlank(taskDataMap.get("POI_PLAN_TOTAL").toString())){
					task.setPoiPlanTotal(Integer.parseInt(taskDataMap.get("POI_PLAN_TOTAL").toString()));
				}
				if(StringUtils.isNotBlank(taskDataMap.get("WORK_KIND").toString())){
					task.setWorkKind(taskDataMap.get("WORK_KIND").toString());
				}

				TaskService.getInstance().createWithBean(conn, task);
				}
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
	public void creakProgramByBlockPlan(Map<String, Object> programMap, Connection conn) throws Exception{
		Program program = new Program();
		try{
			program.setName(programMap.get("NAME").toString() + "_" + df.format(new Date()));
			program.setType(1);
			program.setDescp("");
			if(StringUtils.isNotBlank(programMap.get("PLAN_START_DATE").toString())){
				program.setPlanStartDate(Timestamp.valueOf(programMap.get("PLAN_START_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("PLAN_END_DATE").toString())){
				program.setPlanEndDate(Timestamp.valueOf(programMap.get("PLAN_END_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("COLLECT_PLAN_START_DATE").toString())){
				program.setCollectPlanStartDate(Timestamp.valueOf(programMap.get("COLLECT_PLAN_START_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("COLLECT_PLAN_END_DATE").toString())){
				program.setCollectPlanEndDate(Timestamp.valueOf(programMap.get("COLLECT_PLAN_END_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
				program.setMonthEditPlanStartDate(Timestamp.valueOf(programMap.get("MONTH_EDIT_PLAN_START_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
				program.setMonthEditPlanEndDate(Timestamp.valueOf(programMap.get("MONTH_EDIT_PLAN_END_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("PRODUCE_PLAN_START_DATE").toString())){
				program.setProducePlanStartDate(Timestamp.valueOf(programMap.get("PRODUCE_PLAN_START_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("PRODUCE_PLAN_END_DATE").toString())){
				program.setProducePlanEndDate(Timestamp.valueOf(programMap.get("PRODUCE_PLAN_END_DATE").toString()));
			}
			if(StringUtils.isNotBlank(programMap.get("CITY_ID").toString())){
				program.setCityId(Integer.parseInt(programMap.get("CITY_ID").toString()));
			}
			program.setStatus(1);
			program.setCreateUserId(0);
			//创建项目
			ProgramService.getInstance().create(conn, program);
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
	public Map<String, Object> getGroupId(final Map<String, Object> taskMap, Connection conn) throws Exception{

		try{
			QueryRunner run = new QueryRunner();
			String cityName = taskMap.get("CITY_NAME").toString();
			
			String sql = "select ug.group_id as colection_id from user_group ug where ug.group_name = (" 
					+ "select t.COLLECT_GROUP_NAME from admin_group_mapping t, city c "
					+ "where t.admin_code = c.admin_id "
					+ "and c.city_name = '" + cityName + "')";
					
			String selsect ="select ug.group_id as month_id from user_group ug where ug.group_name = ("
					+ "select t.edit_group_name from admin_group_mapping t, city c where t.admin_code = c.admin_id "
					+ "and c.city_name = '"+ cityName + "')";

			ResultSetHandler<Map<String, Object>> rsHandler = new ResultSetHandler<Map<String, Object>>() {
				public Map<String, Object> handle(ResultSet rs) throws SQLException {
					if (rs.next()) {
						//这里一次查询直接查询出两种类型的groupID，以免不同的type类型赋值groupID时再执行一次查询
						if(SELECT_TIMES == 0){
							taskMap.put("MONTH_GROUP_ID", rs.getInt("month_id"));
						}else{
							taskMap.put("COLECTION_GROUP_ID", rs.getInt("colection_id"));
						}
						SELECT_TIMES++;
					}
					return taskMap;
				}
			};
			
			run.query(conn, selsect, rsHandler);	
			return run.query(conn, sql, rsHandler);	
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
			
			List<Map<String,Object>> list = new ArrayList<>();
			list.add(map);
			cityID = map.get("CITY_ID").toString();
			for(int i = j+1; i < programSize; i++){
				blockMap = programList.get(i);
				if(cityID.equals(blockMap.get("CITY_ID").toString())){
					programList.remove(i);
					programSize--;
					list.add(blockMap);
				}
			}
			cityMap.put(cityID, list);
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
		String plan_start_date = DEFAULT_DATE_BEGAIN;
		String plan_end_date = DEFAULT_DATE_END;
		String collection_plan_start_date = DEFAULT_DATE_BEGAIN;
		String collection_plan_end_date = DEFAULT_DATE_END;
		String month_edit_plan_start_date = DEFAULT_DATE_BEGAIN;
		String month_edit_plan_end_date = DEFAULT_DATE_END;
		String produce_plan_start_date = DEFAULT_DATE_BEGAIN;
		String produce_plan_end_date = DEFAULT_DATE_END;
		String city_name = "";
		int city_id = 0;
		
		List<Map<String,Object>> programList = new ArrayList();
		for (String key : map.keySet()) {
			List<Map<String,Object>> blockList = map.get(key);
			 Map<String, Object> programMap = new HashMap<>();
				for(Map<String, Object> blockMap : blockList){
					//处理时间
					if(StringUtils.isNotBlank(blockMap.get("COLLECT_PLAN_START_DATE").toString())){
						if(plan_start_date.compareTo(blockMap.get("COLLECT_PLAN_START_DATE").toString()) > 0){
							plan_start_date = blockMap.get("COLLECT_PLAN_START_DATE").toString();
						}
					}
					if(StringUtils.isNotBlank(blockMap.get("PRODUCE_PLAN_END_DATE").toString())){
						if(plan_end_date.compareTo(blockMap.get("PRODUCE_PLAN_END_DATE").toString()) < 0){
							plan_end_date = blockMap.get("PRODUCE_PLAN_END_DATE").toString();
						}
					}
					if(StringUtils.isNotBlank(blockMap.get("COLLECT_PLAN_START_DATE").toString())){
						if(collection_plan_start_date.compareTo(blockMap.get("COLLECT_PLAN_START_DATE").toString()) > 0){
							collection_plan_start_date = blockMap.get("COLLECT_PLAN_START_DATE").toString();
						}
					}
					if(StringUtils.isNotBlank(blockMap.get("COLLECT_PLAN_END_DATE").toString())){
						if(collection_plan_end_date.compareTo(blockMap.get("COLLECT_PLAN_END_DATE").toString()) < 0){
							collection_plan_end_date = blockMap.get("COLLECT_PLAN_END_DATE").toString();
						}
					}
					if(StringUtils.isNotBlank(blockMap.get("MONTH_EDIT_PLAN_START_DATE").toString())){
						if(month_edit_plan_start_date.compareTo(blockMap.get("MONTH_EDIT_PLAN_START_DATE").toString()) > 0){
							month_edit_plan_start_date = blockMap.get("MONTH_EDIT_PLAN_START_DATE").toString();
						}
					}
					if(StringUtils.isNotBlank(blockMap.get("MONTH_EDIT_PLAN_END_DATE").toString())){
						if(month_edit_plan_end_date.compareTo(blockMap.get("MONTH_EDIT_PLAN_END_DATE").toString()) < 0){
							month_edit_plan_end_date = blockMap.get("MONTH_EDIT_PLAN_END_DATE").toString();
						}
					}
					if(StringUtils.isNotBlank(blockMap.get("PRODUCE_PLAN_START_DATE").toString())){
						if(produce_plan_start_date.compareTo(blockMap.get("PRODUCE_PLAN_START_DATE").toString()) > 0){
							produce_plan_start_date = blockMap.get("PRODUCE_PLAN_START_DATE").toString();
						}
					}
					if(StringUtils.isNotBlank(blockMap.get("PRODUCE_PLAN_END_DATE").toString())){
						if(produce_plan_end_date.compareTo(blockMap.get("PRODUCE_PLAN_END_DATE").toString()) < 0){
							produce_plan_end_date = blockMap.get("PRODUCE_PLAN_END_DATE").toString();
						}
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
