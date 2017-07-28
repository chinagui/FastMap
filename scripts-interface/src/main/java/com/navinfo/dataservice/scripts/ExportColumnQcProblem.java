package com.navinfo.dataservice.scripts;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.api.man.model.UserInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.scripts.model.ColumnQcProblem;

/**
 * 导出质检问题记录表
 * @author test
 *
 */
public class ExportColumnQcProblem {

	public static void execute(String startDate,String endDate) throws Exception {
		
		Connection conn = null;
		try {
			
			int dbId = searchMonthDbId();//查询月库dbId
			if(0==dbId){throw new Exception("对应月库不存在");}
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			String excelName = "质检问题记录表_"+ DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
			
			List<ColumnQcProblem> columnQcProblemList = searchColumnQcProblemListByDate(startDate,endDate,conn);
			for (ColumnQcProblem columnQcProblem : columnQcProblemList) {
				int subtaskId = columnQcProblem.getSubtaskId();
				Subtask subtask = apiService.queryBySubtaskId(subtaskId);
				columnQcProblem.setSubtaskName(subtask.getName());
				int workerId = Integer.parseInt(columnQcProblem.getWorker());
				int qcWorkerId = Integer.parseInt(columnQcProblem.getQcWorker());
				UserInfo worker  = apiService.getUserInfoByUserId(workerId);
				columnQcProblem.setWorker(workerId+"-"+worker.getUserRealName());
				UserInfo qcWorker  = apiService.getUserInfoByUserId(qcWorkerId);
				columnQcProblem.setQcWorker(qcWorkerId+"-"+qcWorker.getUserRealName());
				columnQcProblem.setSubtaskGroup(searchByGroupId(subtask.getExeGroupId()));
			}
			
			
			ExportExcel<ColumnQcProblem> ex = new ExportExcel<ColumnQcProblem>();

			String[] headers = { "序号", "当前项目编号", "项目名称", "作业对象", "fid", "作业项目（大分类）",
					"项目类型（中分类）", "详细检查项（子分类）", "错误内容", "错误类型", "问题等级", "问题描述", "正确内容",
					"作业员","项目组","作业时间","质检员","质检时间","原始信息"};

			try {
				String path = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.downloadFilePathPoi)+"/poiQuality/columnQcProblem";
				OutputStream out = new FileOutputStream(path+"/" + excelName + ".xls");
				ex.exportExcel("质检问题记录表",headers, columnQcProblemList, out, "yyyy-MM-dd HH:mm:ss");
				out.close();

				System.out.println("excel导出成功！");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}


		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}

	}


	/**
	 * 根据exeGroupId查询项目组名称
	 * @param exeGroupId
	 * @return
	 * @throws SQLException 
	 */
	private static String searchByGroupId(Integer exeGroupId) throws SQLException {
		Connection conn = null;
		String sql  = "SELECT group_name FROM User_Group WHERE group_id = "+exeGroupId;
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				return resultSet.getString(1);
			}
			
			return null;
			
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}



	/**
	 * 查询月库dbId
	 * @return
	 * @throws Exception
	 */
	private static int searchMonthDbId() throws Exception {
		Connection conn = null;
		String sql  = "SELECT DISTINCT monthly_db_id FROM REGION";
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				return resultSet.getInt(1);
			}
			
			return 0;
			
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}


	private static List<ColumnQcProblem> searchColumnQcProblemListByDate(String startDate, String endDate,
			Connection conn) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT SUBTASK_ID,p.poi_num,FIRST_WORK_ITEM,SECOND_WORK_ITEM,WORK_ITEM_ID,OLD_VALUE,ERROR_TYPE,");
		sb.append("ERROR_LEVEL,PROBLEM_DESC,NEW_VALUE,WORKER,WORK_TIME,QC_WORKER,QC_TIME,ORIGINAL_INFO ");
		sb.append("FROM Column_Qc_Problem c,ix_poi p WHERE c.pid = p.pid AND  c.qc_time between to_date('"+startDate+" 00:00:00','yyyyMMdd hh24:mi:ss') and to_date('"+endDate+" 23:59:59','yyyyMMdd hh24:mi:ss')");

		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			resultSet = pstmt.executeQuery();
			
			List<ColumnQcProblem> columnQcProblemList = new ArrayList<>();
			int count = 0;
			while (resultSet.next()) {
				ColumnQcProblem columnQcProblem = new ColumnQcProblem();
				count++;
				columnQcProblem.setId(count);
				columnQcProblem.setWorkObject("设施");
				ReflectionAttrUtils.executeResultSet(columnQcProblem, resultSet);
				columnQcProblemList.add(columnQcProblem);
			}
			
			return columnQcProblemList;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
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
