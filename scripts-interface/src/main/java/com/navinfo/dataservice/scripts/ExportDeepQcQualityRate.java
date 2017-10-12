package com.navinfo.dataservice.scripts;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.scripts.model.DeepQcQualityRate;

import net.sf.json.JSONObject;

/**
 * 导出深度信息质检品质表
 * @Title:ExportDeepQcQualityRate
 * @Package:com.navinfo.dataservice.scripts
 * @Description: 
 * @author:Jarvis 
 * @date: 2017年9月19日
 */
public class ExportDeepQcQualityRate {
	private static Logger log = LoggerRepos.getLogger(ExportDeepQcProblem.class);
	private static Map<Integer,DeepQcQualityRate>  deepQcQualityRateMap = new HashMap<>();//作业员深度信息质检品质率map
	private static List<DeepQcQualityRate> deepQcQualityRateList =  new ArrayList<>();//作业员深度信息质检品质率List
	private static String DEEP_PARKING = "deepParking";
	private static String DEEP_CARRENTAL = "deepCarrental";
	private static String DEEP_DETAIL = "deepDetail";
	private static int deepDetailQcRate = 0;
	private static int deepParkingQcRate = 0;
	private static int deepCarrentalQcRate = 0;
	
	static{
		   String deepQcQualityRateRatio = SystemConfigFactory.getSystemConfig().getValue(
					PropConstant.deepQcQualityRateRatio);
		 
		   JSONObject jsonObject = JSONObject.fromObject(deepQcQualityRateRatio);
		 
		   deepDetailQcRate = jsonObject.getInt(DEEP_DETAIL);
		   deepParkingQcRate = jsonObject.getInt(DEEP_PARKING);
		   deepCarrentalQcRate = jsonObject.getInt(DEEP_CARRENTAL);
	}
	
	public static void execute(String startDate,String endDate) throws Exception {
		
		Connection manConn = null;
		Connection monthConn = null;
		
		try {
			
			manConn = DBConnector.getInstance().getManConnection();
			
			monthConn = DBConnector.getInstance().getMkConnection();
			
			String excelName = "deep_quality_rate_list_"+startDate+"_"+endDate;

			convertQcNum(monthConn);
			
			convertQcProblemNumByDate(startDate, endDate, manConn);
			
			calculateRate(deepQcQualityRateMap, manConn);
			
			ExportExcel<DeepQcQualityRate> ex = new ExportExcel<DeepQcQualityRate>();

			String[] headers = { "作业员姓名", "员工编号", "项目组名称", "深度信息-通用", "深度信息-通用质检量", "深度信息-通用错误量",
					"深度信息-停车场", "深度信息-停车场质检量", "深度信息-停车场错误量", "深度信息-汽车租赁", "深度信息-汽车租赁质检量", "深度信息-汽车租赁错误量"};

			try {
				String path = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.downloadFilePathPoi)+"/poiQuality/deepQcQualityRate";
//				String path = "D:/"; 
				File file = new File(path+"/" + excelName + ".xls");
				if(!file.getParentFile().isDirectory()){
					file.getParentFile().mkdirs();
				}
				if(!file.exists()){
					file.createNewFile();
				}
				OutputStream out = new FileOutputStream(file);
				ex.exportExcel("质检品质率表",headers, deepQcQualityRateList, out, "yyyy-MM-dd HH:mm:ss");
				out.close();

				System.out.println("excel导出成功！");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


		} catch (Exception e) {
			DbUtils.rollback(manConn);
			DbUtils.rollback(monthConn);
			log.error("导出深度信息质检质检率失败",e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(manConn);
			DbUtils.commitAndCloseQuietly(monthConn);
		}

	}

	/**
	 * 计算品质率
	 * @param deepQcQualityRateMap
	 * @param conn
	 * @throws Exception
	 */
	public static void calculateRate(Map<Integer,DeepQcQualityRate>  deepQcQualityRateMap,Connection manConn) throws Exception {
		for (DeepQcQualityRate data : deepQcQualityRateMap.values()) {
			 String workerName = data.getWorkerName();
			 if(StringUtils.isBlank(workerName)){//无错误量，没赋值上用户名，组名
				 setWorkerInfo(data, manConn);
			 }
			
			 DecimalFormat df=new DecimalFormat("0.00");
			
			 int deepDetailQcNum = data.getDeepDetailQcNum();
			 int deepDetailQcProblemNum = data.getDeepDetailQcProblemNum();
			 int deepParkingQcNum = data.getDeepParkingQcNum();
			 int deepParkingQcProblemNum = data.getDeepParkingQcProblemNum();
			 int deepCarrentalQcNum = data.getDeepCarrentalQcNum();
			 int deepCarrentalQcProblemNum = data.getDeepCarrentalQcProblemNum();
			 
			
			 double deepDetail = (double)deepDetailQcProblemNum/((double)deepDetailQcNum*deepDetailQcRate);
			 double deepParking = (double)deepParkingQcProblemNum/((double)deepParkingQcNum*deepParkingQcRate);
			 double deepCarrental = (double)deepCarrentalQcProblemNum/((double)deepCarrentalQcNum*deepCarrentalQcRate);
			 data.setDeepDetail(deepDetailQcProblemNum==0?"0":(df.format((1-deepDetail)*100))+"%");
			 data.setDeepParking(deepParkingQcProblemNum==0?"0":(df.format((1-deepParking)*100))+"%");
			 data.setDeepCarrental(deepCarrentalQcProblemNum==0?"0":(df.format((1-deepCarrental)*100))+"%");
		
			 deepQcQualityRateList.add(data);
		}
	}
	
	/**
	 * 赋值作业员姓名和组名
	 * @param data
	 * @param conn
	 * @throws Exception
	 */
	private static void setWorkerInfo(DeepQcQualityRate data,Connection conn) throws Exception {
		String sql  = "SELECT  U.USER_REAL_NAME, G.GROUP_NAME FROM SUBTASK S, USER_INFO U, USER_GROUP G";
		sql += " WHERE  S.EXE_GROUP_ID = G.GROUP_ID AND U.USER_ID = "+data.getWorkerId()+" AND subtask_id = "+data.getSubtaskGroup()+"";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				data.setWorkerName(resultSet.getString(1));
				data.setSubtaskGroup(resultSet.getString(2));
			}
			
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 封装质检量
	 * @param monthConn
	 * @throws SQLException 
	 */
	private static void convertQcNum(Connection monthConn) throws SQLException {
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT PS.COMMON_HANDLER,TASK_ID, PW.SECOND_WORK_ITEM, COUNT(1) NUM FROM POI_COLUMN_STATUS PS, POI_COLUMN_WORKITEM_CONF PW ");
		sb.append(" WHERE PS.WORK_ITEM_ID = PW.WORK_ITEM_ID  AND PS.QC_FLAG = 1 AND PS.SECOND_WORK_STATUS = 3 AND PW.FIRST_WORK_ITEM = 'poi_deep'");
		sb.append(" GROUP BY COMMON_HANDLER,TASK_ID, SECOND_WORK_ITEM ");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = monthConn.prepareStatement(sb.toString());
			
			rs = pstmt.executeQuery();
			
			DeepQcQualityRate deepQcQualityRate = null;
			while (rs.next()) {
				int worker = rs.getInt(1);
				if(!deepQcQualityRateMap.containsKey(worker)) {
					deepQcQualityRate = new DeepQcQualityRate();
					deepQcQualityRate.setWorkerId(worker);
					deepQcQualityRate.setSubtaskGroup(rs.getInt(2)+"");
					deepQcQualityRateMap.put(worker, deepQcQualityRate);
				}
				
				deepQcQualityRate = deepQcQualityRateMap.get(worker);
				
				String secondWorkItem = rs.getString("SECOND_WORK_ITEM");
				int num = rs.getInt("NUM");
				
				if(secondWorkItem.equals(DEEP_PARKING)){
					deepQcQualityRate.setDeepParkingQcNum(num);
				}else if(secondWorkItem.equals(DEEP_CARRENTAL)){
					deepQcQualityRate.setDeepCarrentalQcNum(num);
				}else if(secondWorkItem.equals(DEEP_DETAIL)){
					deepQcQualityRate.setDeepDetailQcNum(num);
				}
				
			}
			
		} catch (Exception e) {
			DbUtils.rollback(monthConn);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		
	}



	

	/**
	 * 通过时间封装错误量
	 * @param startDate
	 * @param endDate
	 * @param conn
	 * @throws Exception
	 */
	private static void convertQcProblemNumByDate(String startDate, String endDate,
			Connection conn) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT U.USER_REAL_NAME,D.COMMON_WORKER_ID, G.GROUP_NAME, D.SECOND_WORKITEM, D.NUM ");
		sb.append("FROM (SELECT COMMON_WORKER_ID, SECOND_WORKITEM,SUBTASK_ID, COUNT(1) NUM FROM DEEP_QC_PROBLEM WHERE QC_TIME BETWEEN ");
		sb.append("TO_DATE('" + startDate + " 00:00:00', 'yyyyMMdd hh24:mi:ss') AND TO_DATE('" + endDate + " 23:59:59','yyyyMMdd hh24:mi:ss')");
		sb.append(" GROUP BY COMMON_WORKER_ID, SECOND_WORKITEM, SUBTASK_ID) D, SUBTASK S, USER_INFO U, USER_GROUP G ");
		sb.append(" WHERE D.SUBTASK_ID = S.SUBTASK_ID  AND S.EXE_GROUP_ID = G.GROUP_ID AND D.COMMON_WORKER_ID = U.USER_ID ORDER BY COMMON_WORKER_ID");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			rs = pstmt.executeQuery();
			
			DeepQcQualityRate deepQcQualityRate = null;
			while (rs.next()) {
				int worker = rs.getInt(2);
				if(!deepQcQualityRateMap.containsKey(worker)) {
					deepQcQualityRate = new DeepQcQualityRate();
					deepQcQualityRate.setWorkerId(worker);
					
					deepQcQualityRateMap.put(worker, deepQcQualityRate);
				}
				
				deepQcQualityRate = deepQcQualityRateMap.get(worker);
				
				if(StringUtils.isBlank(deepQcQualityRate.getWorkerName())){
					deepQcQualityRate.setWorkerName(rs.getString(1));
				}

				deepQcQualityRate.setSubtaskGroup(rs.getString(3));
				
				String secondWorkItem = rs.getString("SECOND_WORKITEM");
				int num = rs.getInt("NUM");
				
				if(secondWorkItem.equals(DEEP_PARKING)){
					deepQcQualityRate.setDeepParkingQcProblemNum(num);
				}else if(secondWorkItem.equals(DEEP_CARRENTAL)){
					deepQcQualityRate.setDeepCarrentalQcProblemNum(num);
				}else if(secondWorkItem.equals(DEEP_DETAIL)){
					deepQcQualityRate.setDeepDetailQcProblemNum(num);
				}
				
			}
			
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}

	
	public static void main(String[] args) throws Exception {
		initContext();
		System.out.println("args.length:" + args.length);
		if (args == null || args.length != 2) {
			System.out.println("ERROR:need args:");
			return;
		}

		execute(args[0],args[1]);
		// System.out.println(response);
		System.out.println("Over.");
		System.exit(0);
	}

	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}


}
