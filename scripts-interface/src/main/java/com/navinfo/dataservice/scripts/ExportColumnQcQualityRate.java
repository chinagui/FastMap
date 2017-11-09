package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.scripts.model.ColumnQcQualityRate;

/**
 * 导出质检品质率
 * @author test
 *
 */
public class ExportColumnQcQualityRate {

	public static void execute(String startDate,String endDate) throws Exception {
		
		Connection conn = null;
		try {
			
			conn = DBConnector.getInstance().getMkConnection();
			
			String excelName = "column_quality_rate_list_"+startDate+"_"+endDate;
			
			List<ColumnQcQualityRate> columnQcQualityRateList = searchColumnQcQualityRateListByDate(startDate,endDate,conn);
			for (ColumnQcQualityRate columnQcQualityRate : columnQcQualityRateList) {
				String subtaskId = columnQcQualityRate.getSubtaskGroup();
				setSubtaskGroup(Integer.parseInt(subtaskId), columnQcQualityRate);
				columnQcQualityRate.setWorkerName(getUserRealName(columnQcQualityRate.getWorkerId()));
			}
			
			
			ExportExcel<ColumnQcQualityRate> ex = new ExportExcel<ColumnQcQualityRate>();

			String[] headers = { "作业员姓名", "员工编号", "项目组名称", "中文名称", "中文名称质检量", "中文名称错误量",
					"中文地址", "中文地址质检量", "中文地址错误量", "英文名称", "英文名称质检量", "英文名称错误量", "英文地址",
					"英文地址质检量","英文地址错误量"};

			try {
				String path = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.downloadFilePathPoi)+"/poiQuality/columnQcQualityRate";
//				String path = "D:/";
				File file = new File(path+"/" + excelName + ".xls");
				if(!file.getParentFile().isDirectory()){
					file.getParentFile().mkdirs();
				}
				if(!file.exists()){
					file.createNewFile();
				}
				OutputStream out = new FileOutputStream(file);
				ex.exportExcel("质检品质率表",headers, columnQcQualityRateList, out, "yyyy-MM-dd HH:mm:ss");
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
	 * 根据userId查询getUserRealName
	 * @param userId
	 * @return
	 * @throws SQLException 
	 */
	private static String getUserRealName(Integer userId) throws SQLException {
		Connection conn = null;
		String sql  = "SELECT USER_REAL_NAME FROM USER_INFO WHERE USER_ID = "+userId;
		
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
	
	private static void setSubtaskGroup(int subtaskId,ColumnQcQualityRate columnQcQualityRate) throws SQLException{
		Connection conn = null;
		String sql  = "SELECT G.GROUP_NAME FROM SUBTASK S,USER_GROUP G WHERE S.EXE_GROUP_ID = G.GROUP_ID AND S.SUBTASK_ID = "+subtaskId;
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			conn = DBConnector.getInstance().getManConnection();
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();
			
			if(resultSet.next()){
				columnQcQualityRate.setSubtaskGroup(resultSet.getString(1));
			}
			
			
		} catch (Exception e) {
			DbUtils.rollback(conn);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}



	private static List<ColumnQcQualityRate> searchColumnQcQualityRateListByDate(String startDate, String endDate,
			Connection conn) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT WORKER, SUBTASK_ID,FIRST_WORK_ITEM, COUNT(1) NUM, REPLACE(WM_CONCAT(is_problem),',','|') is_PROBLEM ");
		sb.append(" FROM COLUMN_QC_PROBLEM C WHERE C.FIRST_WORK_ITEM IN ('poi_name','poi_address','poi_englishname','poi_englishaddress') ");
		sb.append(" AND C.QC_TIME BETWEEN  TO_DATE('"+startDate+" 00:00:00', 'yyyyMMdd hh24:mi:ss') AND TO_DATE('"+endDate+" 23:59:59','yyyyMMdd hh24:mi:ss')");
		sb.append(" GROUP BY WORKER, SUBTASK_ID,FIRST_WORK_ITEM");
 

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			rs = pstmt.executeQuery();
			
			List<ColumnQcQualityRate> columnQcQualityRateList = new ArrayList<>();
			int worker = 0;
			ColumnQcQualityRate columnQcQualityRate = new ColumnQcQualityRate();
			while (rs.next()) {
				if(worker!=rs.getInt(1)) {
					worker = rs.getInt(1);
					columnQcQualityRate = new ColumnQcQualityRate();
					columnQcQualityRate.setWorkerId(worker);
					columnQcQualityRate.setSubtaskGroup(rs.getInt(2)+"");
					columnQcQualityRateList.add(columnQcQualityRate);
				}
				 
				String firstWorkItem = rs.getString(3);
				int qcNum = rs.getInt(4);
				String qcProblemNumString = clobToString(rs.getClob(5));
				String[] qcProblemNumArray = qcProblemNumString.split("\\|");
				
				if(firstWorkItem.equals("poi_name")){
					qcNum = qcProblemNumArray.length;
					columnQcQualityRate.setPoiNameQcNum(qcNum);
				}else if(firstWorkItem.equals("poi_address")){
					columnQcQualityRate.setPoiAddressQcNum(qcNum);
				}else if(firstWorkItem.equals("poi_englishname")){
					columnQcQualityRate.setPoiEnglishNameQcNum(qcNum);
				}else if(firstWorkItem.equals("poi_englishaddress")){
					columnQcQualityRate.setPoiEnglishAddressQcNum(qcNum);
				}
				
				int count = 0;
				for (String qcProblemNum : qcProblemNumArray) {
					if(firstWorkItem.equals("poi_name")){
						if(qcProblemNum.contains(":")){
							String num = qcProblemNum.substring(qcProblemNum.indexOf(":")+1);
							if(!num.equals("0")){
								count++;
							}
							continue;
						}
					}
					if(!qcProblemNum.equals("0")){
						count++;
					}
				}
				
				DecimalFormat df=new DecimalFormat("0.00");
				
				String rate = qcNum==0?"0":(df.format((1-(double)count/(double)qcNum)*100))+"%";
				
				if(firstWorkItem.equals("poi_name")){
					columnQcQualityRate.setPoiName(rate);
					columnQcQualityRate.setPoiNameQcProblemNum(qcNum==0?0:count);
				}else if(firstWorkItem.equals("poi_address")){
					columnQcQualityRate.setPoiAddress(rate);
					columnQcQualityRate.setPoiAddressQcProblemNum(qcNum==0?0:count);
				}else if(firstWorkItem.equals("poi_englishname")){
					columnQcQualityRate.setPoiEnglishName(rate);
					columnQcQualityRate.setPoiEnglishNameQcProblemNum(qcNum==0?0:count);
				}else if(firstWorkItem.equals("poi_englishaddress")){
					columnQcQualityRate.setPoiEnglishAddress(rate);
					columnQcQualityRate.setPoiEnglishAddressQcProblemNum(qcNum==0?0:count);
				}
				
				
				
			}
			
			return columnQcQualityRateList;
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	public static String clobToString(Clob clob) throws SQLException, IOException {

		String reString = "";
		Reader is = clob.getCharacterStream();// 得到流
		BufferedReader br = new BufferedReader(is);
		String s = br.readLine();
		StringBuffer sb = new StringBuffer();
		while (s != null) {// 执行循环将字符串全部取出付值给StringBuffer由StringBuffer转成STRING
		sb.append(s);
		s = br.readLine();
		}
		reString = sb.toString();
		return reString;
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
