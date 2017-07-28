package com.navinfo.dataservice.scripts;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.scripts.model.PoiCountTable;
import com.navinfo.dataservice.scripts.model.PoiProblemSummary;

/**
 * 导出质检报表
 * 
 * @author test
 *
 */
public class ExportQualityPoiReport {

	public static void execute(String subtaskIdStr,String path) throws Exception {
		Connection checkConn = DBConnector.getInstance().getCheckConnection();
		try {
			ManApi apiService = (ManApi) ApplicationContextUtil.getBean("manApi");
			Integer subtaskId = Integer.parseInt(subtaskIdStr);
			Subtask subtask = apiService.queryBySubtaskId(subtaskId);
			if (subtask.getStage() == 0) {
				List<PoiCountTable> countTableList = getCountTableList(subtaskId, checkConn);
				for (PoiCountTable poiCountTable : countTableList) {
					setClobInfo(poiCountTable, checkConn);
				}
				List<PoiProblemSummary> poiProblemSummaryList = getPoiProblemSummaryList(countTableList, checkConn);
				String checkMode = getCheckModeBySubTaskId(subtaskId, checkConn);
				String excelName = subtaskId + "_";
				if(StringUtils.isNotBlank(excelName)){
					excelName+=checkMode+"_";
				}
				excelName+= DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");

				ExportExcel<PoiCountTable> ex1 = new ExportExcel<PoiCountTable>();
				ExportExcel<PoiProblemSummary> ex2 = new ExportExcel<PoiProblemSummary>();
				String[] headers = { "ID号", "类别Level", "POI名称Name", "图幅号", "组名Area", "分类Category", "完全匹配Full Match",
						"部分匹配Partial Match", "多余Extra", "遗漏Missing", "DB计数", "现场计数", "是否修改", "修改前", "修改后", " DB计数",
						"现场计数", "是否修改", "修改前", "修改后", "DB计数", "现场计数", "是否修改", "修改前", "修改后", "DB计数", "现场计数", "是否修改",
						"修改前", "修改后", "DB计数", "现场计数", "是否修改", "修改前", "修改后", "DB计数", "现场计数", "是否修改", "修改前", "修改后",
						"DB计数", "现场计数", "是否修改", "修改前", "修改后", "DB计数", "现场计数", "是否修改", "修改前", "修改后", "DB计数", "现场计数",
						"是否修改", "修改前", "修改后", "DB计数", "现场计数", "是否修改", "修改前", "修改后", "DB计数", "现场计数", "是否修改", "修改前",
						"修改后", "DB计数", "现场计数", "是否修改", "修改前", "修改后", "DB计数", "现场计数", "是否修改", "修改前", "修改后", "采集员GPS",
						"采集日期", "录入员GPS", "录入日期", "质检员GPS", "质检日期", "项目号", "版本号", "备注", "TYPE", "备注作业员", "是否导出" };

				String[] headers2 = { "队别", "省","市","项目","线路号","设施类别","问题编号","照片编号","图幅号","组名","设施ID","分类代码","大分类","中分类",
						"小分类","问题类型","问题现象","问题描述","初步原因","根本原因（RCA）","质检员","质检日期","采集员","采集日期","录入员","录入日期","质检部门",
						"质检方式","更改时间","更改人","确认人","版本号","问题等级","有无照片","备注","备注作业员","类别权重","问题重要度权重","总权重","工作年限"};

				String[] mergeHeaders = { "名称 Name", "点位 Position", "分类  Category", "地址  Address", "电话   Phone", "位置关系",
						"父子关系  Father-son", "深度信息", "标注（LABEL)", "餐饮调查表", "POI关联LINK", "邮政标识", "POI等级" };

				try {
					OutputStream out = new FileOutputStream(path+"/" + excelName + ".xls");
					HSSFWorkbook workbook = new HSSFWorkbook();
					Map<String, Integer> colorMap = new HashMap<>();
					colorMap.put("red", 255);
					colorMap.put("green", 0);
					colorMap.put("blue", 255);
					Map<String, String> mergeMap = new HashMap<>();
					mergeMap.put("rowNum", "2");// 跨行数
					mergeMap.put("colIndex", "10,15,20,25,30,35,40,45,50,55,60,65,70,75");// 合并列序号

					ex1.createSheet("poi_count_table", workbook, headers, countTableList, out, "yyyy-MM-dd", colorMap, mergeMap,
							mergeHeaders);
					ex2.createSheet("poi_problem_summary", workbook, headers2, poiProblemSummaryList, out, "yyyy-MM-dd", colorMap, null, null);

					workbook.write(out);
					out.close();

					updateCountTableHasExport(countTableList, checkConn);
					System.out.println("excel导出成功！");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}

		} catch (Exception e) {
			DbUtils.rollback(checkConn);
			e.printStackTrace();
		} finally {
			DbUtils.commitAndCloseQuietly(checkConn);
		}

	}

	private static String getCheckModeBySubTaskId(int subtaskId, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
				String sql = "SELECT DISTINCT CHECK_MODE FROM poi_problem_summary WHERE SUBTASK_ID = "+subtaskId;
				
				pstmt = checkConn.prepareStatement(sql);

				resultSet = pstmt.executeQuery();
				
				StringBuffer sb = new StringBuffer();
				
				while (resultSet.next()) {
					if(StringUtils.isNotBlank(resultSet.getString(1))){
						sb.append(resultSet.getString(1));
					}
				}
			
			return sb.toString();
		} catch (Exception e) {
			throw e;
		}
	}

	private static List<PoiProblemSummary> getPoiProblemSummaryList(List<PoiCountTable> countTableList, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			List<PoiProblemSummary> poiProblemList = new ArrayList<>();
			if(!countTableList.isEmpty()){
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT * FROM POI_PROBLEM_SUMMARY WHERE POI_NUM in (");

				for (PoiCountTable poiCountTable : countTableList) {
					String fid = poiCountTable.getFid();
					sb.append("'" + fid + "',");
				}
				
				sb.deleteCharAt(sb.length() - 1);

				sb.append(")");
				
				pstmt = checkConn.prepareStatement(sb.toString());

				resultSet = pstmt.executeQuery();

				while (resultSet.next()) {
					PoiProblemSummary poiProblemSummary = new PoiProblemSummary();
					ReflectionAttrUtils.executeResultSet(poiProblemSummary, resultSet);
					poiProblemList.add(poiProblemSummary);
				}
			}
			

			return poiProblemList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 将反射中映射不到的clob信息重新赋值
	 * 
	 * @param countTableList
	 * @param checkConn
	 * @return
	 * @throws Exception
	 */
	private static Map<String, String> setClobInfo(PoiCountTable poiCountTable, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT FATHER_SON_DATA_UNMODIFIED,FATHER_SON_DATA_MODIFIED,DEEP_DATA_UNMODIFIED,DEEP_DATA_MODIFIED,");
			sb.append("RESTURANT_DATA_UNMODIFIED,RESTURANT_DATA_MODIFIED FROM POI_COUNT_TABLE where fid = '" + poiCountTable.getFid()+"'");

			pstmt = checkConn.prepareStatement(sb.toString());

			rs = pstmt.executeQuery();

			if (rs.next()) {
				poiCountTable.setFatherSonDataUnmodified(ClobToString(rs.getClob(1)));
				poiCountTable.setFatherSonDataModified(ClobToString(rs.getClob(2)));
				poiCountTable.setDeepDataUnmodified(ClobToString(rs.getClob(3)));
				poiCountTable.setDeepDataModified(ClobToString(rs.getClob(4)));
				poiCountTable.setResturantDataUnmodified(ClobToString(rs.getClob(5)));
				poiCountTable.setResturantDataModified(ClobToString(rs.getClob(6)));
			}

		} catch (Exception e) {
			throw e;
		}
		return null;
	}

	/**
	 * 导出后更新已导出状态
	 * 
	 * @param countTableList
	 * @param checkConn
	 * @throws Exception
	 */
	private static void updateCountTableHasExport(List<PoiCountTable> countTableList, Connection checkConn)
			throws Exception {
		PreparedStatement pstmt = null;
		try {
			if (!countTableList.isEmpty()) {
				StringBuffer sb = new StringBuffer();
				sb.append("UPDATE POI_COUNT_TABLE SET HAS_EXPORT = '1' where fid in (");
				for (PoiCountTable poiCountTable : countTableList) {
					String fid = poiCountTable.getFid();
					sb.append("'" + fid + "',");
				}

				sb.deleteCharAt(sb.length() - 1);

				sb.append(")");
				pstmt = checkConn.prepareStatement(sb.toString());

				pstmt.executeUpdate();
			}

		} catch (Exception e) {
			throw e;
		}

	}

	/**
	 * 利用java反射得到所有的countTable信息
	 * 
	 * @param subtaskId
	 * @param checkConn
	 * @return
	 * @throws Exception
	 */
	private static List<PoiCountTable> getCountTableList(int subtaskId, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT * FROM POI_COUNT_TABLE WHERE HAS_EXPORT = '0' AND QC_SUB_TASKID = '" + subtaskId + "' ");

			pstmt = checkConn.prepareStatement(sb.toString());

			resultSet = pstmt.executeQuery();

			List<PoiCountTable> countTableList = new ArrayList<>();
			while (resultSet.next()) {
				PoiCountTable countTable = new PoiCountTable();
				ReflectionAttrUtils.executeResultSet(countTable, resultSet);
				countTableList.add(countTable);
			}

			return countTableList;
		} catch (Exception e) {
			throw e;
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

	public static String ClobToString(Clob clob) throws Exception {

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
		br.close();
		return reString;
	}

}
