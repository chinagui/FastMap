package com.navinfo.dataservice.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.scripts.model.FieldRdQcRecordExcel;
import com.navinfo.dataservice.scripts.model.PoiProblemSummaryExcel;
import com.navinfo.navicommons.database.sql.DBUtils;

/**
 * 导出质检报表
 * 
 * @author test
 *
 */
/**
 * @author zhangli5174
 *
 */
public class ExportQualityReport {
	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}

	public static void execute(String path) throws Exception {
		Connection checkConn = DBConnector.getInstance().getCheckConnection();
		try {
			
			List<Map<String,String>> list = searchTaskMap();
			if(list != null && list.size() > 0){
				for(Map<String,String> taskMap : list){
					String taskName = taskMap.get("taskName");
					String subtaskIds = taskMap.get("subtaskIds");
					if(subtaskIds != null && StringUtils.isNotEmpty(subtaskIds)){
						Clob gridClob = ConnectionUtil.createClob(checkConn);
						gridClob.setString(1, subtaskIds);
						
						String fileName =  taskName + "质检报表";
						
						fileName+= DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
						//设置编码,解决乱码问题
						System.out.println("old fileName: "+fileName);
						String encoding = System.getProperty("file.encoding");
						System.out.println("encoding : "+encoding);
						fileName = new String(fileName.getBytes("UTF-8"),encoding);
						System.out.println("fileName: "+fileName);
						//导出poi 质检报表 poi_problem_summary
						exportExcelPoi(path,checkConn,gridClob,fileName);
						
						//导出道路 质检报表 Field_RD_QCRecord
						exportExcelRod(path,checkConn,gridClob,fileName);
						
					}
					
				}
			}
			
		} catch (Exception e) {
			DbUtils.rollback(checkConn);
			e.printStackTrace();
		} finally {
			DbUtils.commitAndCloseQuietly(checkConn);
		}

	}

	
	/**
	 * @Title: exportExcelPoi
	 * @Description: 导出poi质检报表
	 * @param path
	 * @param checkConn
	 * @param gridClob
	 * @param fileName
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月9日 下午1:38:35 
	 */
	private static void exportExcelPoi(String path, Connection checkConn, Clob gridClob, String fileName) throws Exception {
		List<PoiProblemSummaryExcel> poiProblemSummaryList = getPoiProblemSummaryList(gridClob, checkConn);
		System.out.println(poiProblemSummaryList.size());
		if(poiProblemSummaryList != null && poiProblemSummaryList.size() > 0 ){
			ExportExcel<PoiProblemSummaryExcel> ex = new ExportExcel<PoiProblemSummaryExcel>();
			
			String[] headers = { "编号","队别", "省","市","项目","线路号","设施类别","问题编号","照片编号","图幅号","组名","设施ID","分类代码","大分类","中分类",
					"小分类","问题类型","问题现象","问题描述","初步原因","根本原因（RCA）","质检员","质检日期","采集员","采集日期","录入员","录入日期","质检部门",
					"质检方式","更改时间","更改人","确认人","版本号","问题等级","有无照片","备注","备注作业员","类别权重","问题重要度权重","总权重","工作年限"};

			OutputStream out = null ;
			try {
				File file = new File(path+"/poi/");
				if(!file.exists()){
					file.mkdir();
				}
				out = new FileOutputStream(file.getCanonicalPath()+"/" + fileName + ".xls");
				HSSFWorkbook workbook = new HSSFWorkbook();
				Map<String, Integer> colorMap = new HashMap<>();
				colorMap.put("red", 255);
				colorMap.put("green", 0);
				colorMap.put("blue", 255);
				Map<String, String> mergeMap = new HashMap<>();
				mergeMap.put("rowNum", "2");// 跨行数
				mergeMap.put("colIndex", "10,15,20,25,30,35,40,45,50,55,60,65,70,75");// 合并列序号

				ex.createSheet("poi_problem_summary", workbook, headers, poiProblemSummaryList, out, "yyyy-MM-dd", colorMap, null, null,"空");

				workbook.write(out);
				out.close();

				System.out.println("poi 检查报表excel导出成功！");
				System.out.println("开始更新是否导出标识");
				//修改poi_problem_summary 表是否导出标识
				int numPoi = updateTableHasExportFlag("poi_problem_summary", gridClob, checkConn);
				System.out.println("更新 条数numPoi: "+numPoi);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				if(out != null){
					out.close();
				}
			}
		}
	}

	/**
	 * @Title: exportExcelRod
	 * @Description: 导出道路质检报表
	 * @param path
	 * @param checkConn
	 * @param gridClob
	 * @param fileName
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月9日 下午1:39:16 
	 */
	private static void exportExcelRod(String path, Connection checkConn, Clob gridClob, String fileName) throws Exception {
		List<FieldRdQcRecordExcel> fieldRdQcRecordExcelList = getFieldRdQcRecordExcelList(gridClob, checkConn);
		System.out.println(fieldRdQcRecordExcelList.size());
		if(fieldRdQcRecordExcelList != null && fieldRdQcRecordExcelList.size() > 0 ){
			ExportExcel<FieldRdQcRecordExcel> ex = new ExportExcel<FieldRdQcRecordExcel>();
			
			String[] headers = { "编号","区域","基地","LINK号","省","市","项目","线路号","设施类别","问题编号","照片编号","图幅号","组名","设施ID","分类代码","大分类","中分类",
					"小分类","问题类型","问题现象","问题描述","初步原因","根本原因（RCA）","质检员","质检日期","作业员","作业日期","质检部门",
					"质检方式","更改时间","更改人","确认人","版本号","问题等级","是否有照片","种别","功能等级","备注作业员","功能等级权重","属性权重","总权重","工作年限"};

			OutputStream out = null ;
			try {
				File file = new File(path+"/road/");
				if(!file.exists()){
					file.mkdir();
				}
				out = new FileOutputStream(file.getCanonicalPath()+"/" + fileName + ".xls");
				HSSFWorkbook workbook = new HSSFWorkbook();
				Map<String, Integer> colorMap = new HashMap<>();
				colorMap.put("red", 255);
				colorMap.put("green", 0);
				colorMap.put("blue", 255);
				Map<String, String> mergeMap = new HashMap<>();
				mergeMap.put("rowNum", "2");// 跨行数
				mergeMap.put("colIndex", "10,15,20,25,30,35,40,45,50,55,60,65,70,75");// 合并列序号

				ex.createSheet("field_rd_qcrecord", workbook, headers, fieldRdQcRecordExcelList, out, "yyyy-MM-dd", colorMap, null, null,"空");

				workbook.write(out);
				out.close();

				System.out.println(" 道路 检查报表 excel导出成功！");
				System.out.println("开始更新是否导出标识");
				//修改field_rd_qcrecord 表是否导出标识
				int numRod = updateTableHasExportFlag("field_rd_qcrecord", gridClob, checkConn);
				System.out.println("更新 条数numRod: "+numRod);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				if(out != null){
					out.close();
				}
			}
		}
	}
	
	

	/**
	 * @Title: searchTaskMap
	 * @Description: 查询任务对应的子任务列表
	 * @return
	 * @throws SQLException  List<Map<String,String>>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月9日 下午1:39:49 
	 */
	private static List<Map<String, String>> searchTaskMap() throws SQLException {
		Connection conn = null;
		Statement pstmt = null;
		ResultSet resultSet = null;
		String selectSql = "select distinct t.task_id,t.name,(select listagg( s.subtask_id, ',')within GROUP(order by s.task_id)   from  subtask s where t.task_id = s.task_id  ) subtaskIds from task t  where  t.status = 0";
		List<Map<String, String>> list = null;
		try{
			conn = DBConnector.getInstance().getManConnection();
			pstmt = conn.createStatement();
			resultSet = pstmt.executeQuery(selectSql);
			list = new ArrayList<Map<String, String>>();
			while (resultSet.next()) {
				Map<String, String> taskMap = new HashMap<String, String>();
				taskMap.put("taskName", resultSet.getString("name"));
				taskMap.put("subtaskIds", resultSet.getString("subtaskIds"));
				list.add(taskMap);
			}
			return list;
		}catch(Exception e){
			e.printStackTrace();
			DbUtils.rollbackAndCloseQuietly(conn);
			return null;
		}finally {
			DbUtils.commitAndClose(conn);
			DBUtils.closeStatement(pstmt);
		}
	}

	/**
	 * @Title: updateTableHasExportFlag
	 * @Description: 修改是否已经导出过的标识
	 * @param tableName
	 * @param gridClob
	 * @param checkConn
	 * @throws Exception  void
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月9日 下午2:44:41 
	 */
	private static int updateTableHasExportFlag(String tableName,Clob gridClob, Connection checkConn)
			throws Exception {
		PreparedStatement pstmt = null;
		int num = 0;
		try {
			if (tableName != null && !tableName.isEmpty() && gridClob != null) {
				StringBuffer sb = new StringBuffer();
				sb.append("UPDATE "+tableName+" SET HAS_EXPORT =1  where ");
				if(tableName.equals("poi_problem_summary")){
					sb.append(" subtask_id ");
				}else if(tableName.equals("field_rd_qcrecord")){
					sb.append(" qc_subtask ");
				}
				sb.append(" in (select to_number(column_value) from table(clob_to_table(?))) ");
				pstmt = checkConn.prepareStatement(sb.toString());
				pstmt.setClob(1, gridClob);
				
				num =pstmt.executeUpdate();
			}
			return num;
		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * @Title: getPoiProblemSummaryList
	 * @Description: 获取poi质检的集合
	 * @param gridClob
	 * @param checkConn
	 * @return
	 * @throws Exception  List<PoiProblemSummaryExcel>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月9日 下午3:43:21 
	 */
	private static List<PoiProblemSummaryExcel> getPoiProblemSummaryList(Clob gridClob, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			List<PoiProblemSummaryExcel> poiProblemList = new ArrayList<PoiProblemSummaryExcel>();
			if(gridClob != null){
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT * FROM POI_PROBLEM_SUMMARY WHERE subtask_id in (select to_number(column_value) from table(clob_to_table(?))) and has_export = 0 ");
				System.out.println("getPoiProblemSummaryList: "+ sb.toString()  );
				pstmt = checkConn.prepareStatement(sb.toString());
				pstmt.setClob(1, gridClob);
				resultSet = pstmt.executeQuery();
				int num = 1;
				while (resultSet.next()) {
					PoiProblemSummaryExcel poiProblemSummary = new PoiProblemSummaryExcel();
					poiProblemSummary.setNum(num);
					ReflectionAttrUtils.executeResultSet(poiProblemSummary, resultSet);
					poiProblemList.add(poiProblemSummary);
					num++;
				}
			}
			
			return poiProblemList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	/**
	 * @Title: getFieldRdQcRecordExcelList
	 * @Description: 获取道路质检的集合
	 * @param gridClob
	 * @param checkConn
	 * @return
	 * @throws Exception  List<FieldRdQcRecordExcel>
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年8月9日 下午3:48:25 
	 */
	private static List<FieldRdQcRecordExcel> getFieldRdQcRecordExcelList(Clob gridClob, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			List<FieldRdQcRecordExcel> fieldRdQcRecordExcelList = new ArrayList<FieldRdQcRecordExcel>();
			if(gridClob != null){
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT * FROM Field_RD_QCRecord WHERE qc_subtask in (select to_number(column_value) from table(clob_to_table(?))) and has_export = 0 ");
				System.out.println("getFieldRdQcRecordExcelList: "+ sb.toString()  );
				pstmt = checkConn.prepareStatement(sb.toString());
				pstmt.setClob(1, gridClob);
				resultSet = pstmt.executeQuery();
				int num = 1;
				while (resultSet.next()) {
					FieldRdQcRecordExcel fieldRdQcRecordExcel = new FieldRdQcRecordExcel();
					fieldRdQcRecordExcel.setNum(num);
					ReflectionAttrUtils.executeResultSet(fieldRdQcRecordExcel, resultSet);
					fieldRdQcRecordExcelList.add(fieldRdQcRecordExcel);
					num++;
				}
			}
			
			return fieldRdQcRecordExcelList;
		} catch (Exception e) {
			throw e;
		}
	}

	
	public static void main(String[] args) throws Exception {
		initContext();
		System.out.println("args.length:" + args.length);
		if (args == null || args.length != 1) {
			System.out.println("ERROR:need args:");
			return;
		}
		execute(args[0]);
//		execute("F:/exportExcel");
		System.out.println("Over.");
		System.exit(0);
	}

}
