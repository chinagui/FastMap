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
import java.util.ArrayList;
import java.util.Date;
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
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.scripts.model.DeepQcProblem;

/**
 * 导出深度信息质检问题记录表
 * @Title:ExportDeepQcProblem
 * @Package:com.navinfo.dataservice.scripts
 * @Description: 
 * @author:Jarvis 
 * @date: 2017年9月18日
 */
public class ExportDeepQcProblem {
	private static Logger log = LoggerRepos.getLogger(ExportDeepQcProblem.class);
	private static Map<Long,String> pidFidMap = new HashMap<>();
	private static String DEEP_PARKING = "deepParking";
	private static String DEEP_CARRENTAL = "deepCarrental";
	private static String DEEP_DETAIL = "deepDetail";
	private static Map<String,String> workItemMap = new HashMap<>();
	private static Map<String,String> workItemTypeMap = new HashMap<>();
	private static Map<String,String> errorTypeMap = new HashMap<>();
	
	static{
		workItemMap.put(DEEP_PARKING, "深度信息-停车场");
		workItemMap.put(DEEP_CARRENTAL, "深度信息-汽车租赁");
		workItemMap.put(DEEP_DETAIL, "深度信息-通用");
		
		workItemTypeMap.put(DEEP_PARKING, "停车场作业");
		workItemTypeMap.put(DEEP_CARRENTAL, "汽车租赁作业");
		workItemTypeMap.put(DEEP_DETAIL, "通用深度信息作业");
		
		errorTypeMap.put("tollStd","收费标准");
		errorTypeMap.put("tollDes","收费描述");
		errorTypeMap.put("tollWay","收费方式");
		errorTypeMap.put("openTiime","营业时间");
		errorTypeMap.put("totalNum","车位数量");
		errorTypeMap.put("payment","支付方式");
		errorTypeMap.put("remark","收费备注");
		errorTypeMap.put("openHour","营业时间");
		errorTypeMap.put("howToGo","交通路线");
		errorTypeMap.put("address","地址描述");
		errorTypeMap.put("webSite","主页网址");
		errorTypeMap.put("phone400","400电话");
		errorTypeMap.put("briefDesc","简介");
		errorTypeMap.put("contacts","传真");
		errorTypeMap.put("businesstimes","营业时间");
	}
	
	public static void execute(String startDate,String endDate) throws Exception {
		
		Connection manConn = null;
		Connection monthConn = null;
		
		try {
			
			manConn = DBConnector.getInstance().getManConnection();
			
			monthConn = DBConnector.getInstance().getMkConnection();
			
			String excelName = "deep_qc_problem_list_"+ DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
			String timeContidion  = " D.QC_TIME BETWEEN TO_DATE('"+startDate+" 00:00:00','yyyyMMdd hh24:mi:ss') AND TO_DATE('"+endDate+" 23:59:59','yyyyMMdd hh24:mi:ss')";
			
			
			List<Long> pidList = selectPidListByDate(timeContidion, manConn);
			
			convertPidToFid(pidList,monthConn);
			
			List<DeepQcProblem>  deepQcProblemList = convertDeepQcProblemListByDate(timeContidion,manConn);
			
			ExportExcel<DeepQcProblem> ex = new ExportExcel<DeepQcProblem>();

			String[] headers = { "序号", "当前项目编号", "项目名称", "作业对象", "fid", "作业项目（大分类）",
					"项目类型（中分类）", "详细检查项（子分类）", "错误内容", "错误类型", "问题等级", "问题描述", "正确内容",
					"作业员","项目组","作业时间","质检员","质检时间"};

			try {
				String path = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.downloadFilePathPoi)+"/poiQuality/deepQcProblem";
				
//				String path = "D://";
				File file = new File(path+"/" + excelName + ".xls");
				if(!file.getParentFile().isDirectory()){
					file.getParentFile().mkdirs();
				}
				if(!file.exists()){
					file.createNewFile();
				}
				OutputStream out = new FileOutputStream(file);
				ex.exportExcel("深度信息质检问题记录表",headers, deepQcProblemList, out, "yyyy-MM-dd HH:mm:ss");
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
			log.error("导出深度信息质检问题记录表失败",e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(manConn);
			DbUtils.commitAndCloseQuietly(monthConn);
		}

	}



	/**
	 * 转换pid至Fid
	 * @param pidList
	 * @param monthConn
	 * @throws SQLException 
	 */
	private static void convertPidToFid(List<Long> pidList, Connection monthConn) throws SQLException {
		String sql  = "SELECT PID,POI_NUM FROM IX_POI WHERE PID IN ";
		String pidsString = StringUtils.join(pidList, ",");
		boolean clobFlag = false;
		if (pidList.size() > 1000) {
			sql += " (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
			clobFlag = true;
		} else {
			sql += " (" + pidsString + ")";
		}
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = monthConn.prepareStatement(sql);
			if (clobFlag) {
				Clob clob = ConnectionUtil.createClob(monthConn);
				clob.setString(1, pidsString);
				pstmt.setClob(1, clob);
			}
			rs = pstmt.executeQuery();
			
			while(rs.next()){
				long pid = rs.getLong(1);
				if(!pidFidMap.containsKey(pid)){
					pidFidMap.put(pid, rs.getString(2));
				}
			}
			
			
		} catch (Exception e) {
			DbUtils.rollback(monthConn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(monthConn);
		}
	}


	
	/**
	 * 根据日期筛选pid
	 * @param startDate
	 * @param endDate
	 * @param conn
	 * @throws Exception
	 */
	private static List<Long> selectPidListByDate(String timeContidion,Connection conn) throws Exception {
		String sql  = "SELECT DISTINCT PID FROM DEEP_QC_PROBLEM D WHERE "+timeContidion+"";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sql);
			
			rs = pstmt.executeQuery();
			
			List<Long> pidList = new ArrayList<>();
			while (rs.next()) {
				pidList.add(rs.getLong("pid"));
			}
			
			return pidList;
			
		} catch (Exception e) {
			log.error("查询DEEP_QC_PROBLEM失败",e);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
	
	
	/**
	 * 根据日期组装对象
	 * @param startDate
	 * @param endDate
	 * @param conn
	 * @throws Exception
	 */
	private static List<DeepQcProblem> convertDeepQcProblemListByDate(String timeContidion,
			Connection conn) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT D.*,S.NAME,G.GROUP_NAME FROM DEEP_QC_PROBLEM D,SUBTASK S,USER_GROUP G ");
		sb.append("WHERE D.SUBTASK_ID  = S.SUBTASK_ID AND S.EXE_GROUP_ID  = G.GROUP_ID AND "+timeContidion+" ORDER BY PID,QC_TIME");

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			rs = pstmt.executeQuery();
			
			int count = 0;
			
			List<DeepQcProblem> qcProblems = new ArrayList<>();
			while (rs.next()) {
				DeepQcProblem deepQcProblem = new DeepQcProblem();
				count++;
				deepQcProblem.setId(count);
				deepQcProblem.setSubtaskId(rs.getInt("SUBTASK_ID"));
				deepQcProblem.setSubtaskName(rs.getString("NAME"));
				deepQcProblem.setWorkObject("深度信息");
				long pid = rs.getLong("PID");
				deepQcProblem.setPoiNum(pidFidMap.containsKey(pid)?pidFidMap.get(pid):"");
				String secondWorkitem = rs.getString("SECOND_WORKITEM");
				deepQcProblem.setWorkItem(workItemMap.containsKey(secondWorkitem)?workItemMap.get(secondWorkitem):"");
				deepQcProblem.setWorkItemType(workItemTypeMap.containsKey(secondWorkitem)?workItemTypeMap.get(secondWorkitem):"");
				deepQcProblem.setDetailField("");
				deepQcProblem.setOldValue(rs.getString("OLD_VALUE"));
				String errorType = rs.getString("POI_PROPERTY");
				deepQcProblem.setErrorType(errorTypeMap.containsKey(errorType)?errorTypeMap.get(errorType):"");
				deepQcProblem.setErrorLevel(rs.getString("PROBLEM_LEVEL"));
				deepQcProblem.setProblemDesc(rs.getString("PROBLEM_DESC"));
				deepQcProblem.setNewValue(rs.getString("NEW_VALUE"));
				String commonWorker = rs.getString("COMMON_WORKER");
				deepQcProblem.setWorker(commonWorker.substring(0, commonWorker.indexOf("-")));
				deepQcProblem.setSubtaskGroup(rs.getString("GROUP_NAME"));
				deepQcProblem.setQcWorker(rs.getString("QC_WORKER"));
				deepQcProblem.setWorkTime(rs.getTimestamp("WORK_TIME"));
				deepQcProblem.setQcTime(rs.getTimestamp("QC_TIME"));
				
				qcProblems.add(deepQcProblem);
			}
			
			return qcProblems;
		} catch (Exception e) {
			log.error("查询DEEP_QC_PROBLEM失败",e);
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
