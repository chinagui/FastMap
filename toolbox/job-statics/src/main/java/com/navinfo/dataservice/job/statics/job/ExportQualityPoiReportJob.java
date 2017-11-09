package com.navinfo.dataservice.job.statics.job;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.dao.log.LogReader;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.job.statics.model.PoiCountTable;
import com.navinfo.dataservice.job.statics.model.PoiProblemSummaryExcel;
import com.navinfo.dataservice.jobframework.exception.JobException;

import oracle.sql.STRUCT;

/**
 * 导出质检poi报表
 * @Title:ExprotQualityPoiReportJob
 * @Package:com.navinfo.dataservice.job.statics.job
 * @Description: 
 * 导出原则： ①按任务导出，判断任务关联的质检子任务关闭且均未被导出过进行质检报表导出（man库subtask_quictly打导出标识）
          ②根据Field_RD_QCRecord、Field_POI_QCRecord 表中的“质检方式”分别导出两份excel报表，如果同一个子任务存在两种质检（现场、室内）方式，则需要分别导出两个excel。每个excel中包含两个sheet页②《Count Table》《③Problem Summary》
		      文件命导出名：任务名称+“质检报表”+室内/现场+导出日期   
 * @author:Jarvis 
 * @date: 2017年10月20日
 */
public class ExportQualityPoiReportJob extends AbstractStatJob {
	
	public ExportQualityPoiReportJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public String stat() throws JobException {
		
		Connection manConn = null;
		Connection checkConn = null;
		
	    List<Integer> subtaskQualityIdList = new ArrayList<>();//质检圈Id list
	    Map<Integer, Map<String, Object>> subtaskMap = new LinkedHashMap<>();//需要导出的子任务信息Map
	    Map<Integer,Map<String, Object>> taskMap = new LinkedHashMap<>();//需要导出的任务信息Map
	    Map<Integer, Connection> dbIdConnMap = new LinkedHashMap<>();//dbId和Conn关联map
	    Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap = new LinkedHashMap<>();//poi_num和PoiProblemSummary 关联map
	    
		try {
			
			log.info("start export quality poi report");
			manConn = DBConnector.getInstance().getManConnection();
			checkConn = DBConnector.getInstance().getCheckConnection();
			
			log.info("start assemble subtask map");
			assembleExportSubtaskMap(manConn,subtaskMap,dbIdConnMap,subtaskQualityIdList);  //拼接需要导出的任务关联的质检子任务关闭且均未被导出过进行质检报表的 Subtask信息
			log.info("end assemble subtask map");
			
			log.info("start assemble task map");
			assembleTaskNameSubtaskIdsMap(taskMap,subtaskMap);//拼接需要导出的task信息
			log.info("end assemble task map");
			
			log.info("start export quality poi by task");
			exportExcelPoiByTask(checkConn,taskMap,subtaskMap,fidPoiProblemSummaryMap);//按任务导出 导出质检报表
			log.info("end export quality poi  by task");
				
			log.info("start update subtask quality set db stat = 1");
			updateSubtaskQualityDbstat(subtaskQualityIdList);//导出后更新SUBTASK_QUALITY中POI_DB_STAT为1，已导出
			log.info("end update subtask quality set db stat = 1");
			
			log.info("end export quality poi report");
		} catch (Exception e) {
			try {
				DbUtils.rollback(manConn);
				DbUtils.rollback(checkConn);
			} catch (SQLException e1) {
				log.error("error export quality poi report", e);
			}
			for (Connection value : dbIdConnMap.values()) {
				DbUtils.rollbackAndCloseQuietly(value);
			}
		} finally {
			DbUtils.commitAndCloseQuietly(manConn);
			DbUtils.commitAndCloseQuietly(checkConn);
			for (Connection value : dbIdConnMap.values()) {  
				DbUtils.commitAndCloseQuietly(value);
			}  
		}
		return "compelete export quality poi report ";
	}

	/**
	 * 按任务导出 导出质检报表
	 * @throws Exception 
	 */
	public void exportExcelPoiByTask(Connection checkConn,Map<Integer,Map<String, Object>> taskMap,
			Map<Integer, Map<String, Object>> subtaskMap,Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap) throws Exception{
		String[] headers = { "ID号", "类别Level", "POI名称Name", "图幅号", "分类Category", "名称 Name", "点位 Position","分类  Category",
				"地址  Address","电话   Phone", "位置关系","父子关系  Father-son", "深度信息", "标注（LABEL)", "餐饮调查表", "POI关联LINK", "邮政标识", "POI等级",
				"采集员GPS","采集日期", "录入员GPS", "录入日期", "质检员GPS", "质检日期", "项目号", "版本号", "备注", "TYPE", "备注作业员"};
		
		String[] headers2 = { "编号","队别", "省","市","项目","线路号","设施类别","问题编号","照片编号","图幅号","组名","设施ID","分类代码","大分类","中分类",
				"小分类","问题类型","问题现象","问题描述","初步原因","根本原因（RCA）","质检员","质检日期","采集员","采集日期","录入员","录入日期","质检部门",
				"质检方式","更改时间","更改人","确认人","版本号","有无照片","备注","备注作业员","类别权重","问题重要度权重","总权重","工作年限"};
		
		String encoding = System.getProperty("file.encoding");
		
		for (Entry<Integer, Map<String, Object>> entry : taskMap.entrySet()) {
			
			Integer taskId  = entry.getKey();
			
			log.info("start export taskId = "+taskId);
			
			Map<String, Object> taskInfo = entry.getValue();
			String taskGroup = (String) taskInfo.get("taskGroup");
			String taskName = (String) taskInfo.get("taskName");
			StringBuffer xcSubtaskIds = (StringBuffer) taskInfo.get("xcSubtaskIds");
			StringBuffer snSubtaskIds = (StringBuffer) taskInfo.get("snSubtaskIds");
			if(xcSubtaskIds != null && StringUtils.isNotBlank(xcSubtaskIds.toString())){
				xcSubtaskIds.deleteCharAt(xcSubtaskIds.length()-1);
			}
			if(snSubtaskIds != null && StringUtils.isNotBlank(snSubtaskIds.toString())){
				snSubtaskIds.deleteCharAt(snSubtaskIds.length()-1);
			}
			
			String xnSubtaskIdStr = xcSubtaskIds.toString();
			String snSubtaskIdStr = snSubtaskIds.toString();
			
			taskGroup = new String(taskGroup.getBytes("UTF-8"),encoding);
			
			String path = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathPoi)
					+"/"+DateUtils.dateToString(new Date(), "yyyyMMdd")+"/"+taskGroup+"/poi/";
			
			String fileName = null;
			String dateStr = DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS");
			
			if(xnSubtaskIdStr != null && StringUtils.isNotEmpty(xnSubtaskIdStr)){
				log.info("start export taskId = "+taskId+" xnSubtaskIdStr = "+xnSubtaskIdStr);
				fileName = taskName + "质检报表现场"+dateStr;
				fileName = new String(fileName.getBytes("UTF-8"),encoding);
				log.info("start export path = "+path+" fileName = "+fileName);
				
				//导出poi 外业质检现场报表 
				exportExcelPoi(path,headers,headers2,checkConn,xnSubtaskIdStr,fileName,subtaskMap,fidPoiProblemSummaryMap);
			}
			
			if(snSubtaskIdStr != null && StringUtils.isNotEmpty(snSubtaskIdStr)){
				log.info("start export taskId = "+taskId+" snSubtaskIdStr = "+snSubtaskIdStr);
				fileName = taskName + "质检报表室内"+dateStr;
				fileName = new String(fileName.getBytes("UTF-8"),encoding);
				log.info("start export path = "+path+" fileName = "+fileName);
				//导出poi 外业质检室内报表 
				exportExcelPoi(path,headers,headers2,checkConn,snSubtaskIdStr,fileName,subtaskMap,fidPoiProblemSummaryMap);
			}
			
			log.info("end export taskId = "+taskId);
			
		}
		
	}
	

	/**
	 * 导出完成后更新DB统计状态
	 * @param qualityId
	 * @throws Exception 
	 */
	private void updateSubtaskQualityDbstat(List<Integer> subtaskQualityIdList) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
        try {
        	conn = DBConnector.getInstance().getManConnection();
        	
        	Clob gridClob = ConnectionUtil.createClob(conn);
    		gridClob.setString(1, org.apache.commons.lang.StringUtils.join(subtaskQualityIdList,","));
        	
			if(gridClob != null){
				StringBuffer sb = new StringBuffer();
				sb.append("UPDATE SUBTASK_QUALITY SET POI_DB_STAT = 1 WHERE QUALITY_ID IN (select to_number(column_value) from table(clob_to_table(?)))");
				pstmt = conn.prepareStatement(sb.toString());
				pstmt.setClob(1, gridClob);
				pstmt.executeUpdate();
			}

            
        } catch (Exception e) {
        	DbUtils.rollbackAndCloseQuietly(conn);
        	log.error("error update subtask quality dbstat", e);
			throw e;
		} finally{
			DbUtils.closeQuietly(pstmt);
			DbUtils.commitAndCloseQuietly(conn);
		}
		
	}
	
	/**
	 * 导出质检报表
	 * @param string
	 * @param checkConn
	 * @param gridClob
	 * @param fileName
	 * @throws Exception 
	 */
	private void exportExcelPoi(String path, String[] headers,String[] headers2,Connection checkConn, 
			String subtaskIds, String fileName,Map<Integer, Map<String, Object>> subtaskMap,
			Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap) throws Exception {
		
		Clob gridClob = ConnectionUtil.createClob(checkConn);
		gridClob.setString(1, subtaskIds);
		
		log.info("start get poi problem summary list");
		List<PoiProblemSummaryExcel> poiProblemSummaryList = getPoiProblemSummaryList(gridClob, checkConn,fidPoiProblemSummaryMap);//获取poiProblemSummaryList
		log.info("end get poi problem summary list");
		
		log.info("start get poi count table list");
		List<PoiCountTable> poiCountTableList = getCountTableList(subtaskIds,subtaskMap,fidPoiProblemSummaryMap);//获取poiCountTableList
		log.info("end get poi count table list");
		
		if(poiCountTableList != null && CollectionUtils.isNotEmpty(poiCountTableList)){
			if(poiProblemSummaryList != null && CollectionUtils.isNotEmpty(poiProblemSummaryList)){
				exportExcelPoiWithCheckMode(path, headers, headers2, fileName, poiCountTableList,poiProblemSummaryList);
			}
		}
	}
	
	/**
	 * 现场监察，室内监察导出质检报告
	 * @param path
	 * @param headers
	 * @param fileName
	 * @param poiProblemSummaryList
	 * @throws Exception
	 */
	private void exportExcelPoiWithCheckMode(String path,String[] headers,String[] headers2, String fileName,
			List<PoiCountTable> poiCountTableList,List<PoiProblemSummaryExcel> poiProblemSummaryList) throws Exception{
		ExportExcel<PoiCountTable> ex1 = new ExportExcel<PoiCountTable>();
		ExportExcel<PoiProblemSummaryExcel> ex2 = new ExportExcel<PoiProblemSummaryExcel>();
		OutputStream out = null ;
		try {
			File file = new File(path+"/" + fileName + ".xls");
			if(!file.getParentFile().isDirectory()){
				file.getParentFile().mkdirs();
			}
			if(!file.exists()){
				file.createNewFile();
			}
			out = new FileOutputStream(file);
			HSSFWorkbook workbook = new HSSFWorkbook();
			Map<String, Integer> colorMap = new HashMap<>();
			colorMap.put("red", 255);
			colorMap.put("green", 0);
			colorMap.put("blue", 255);

			ex1.createSheet("poi_count_table", workbook, headers, poiCountTableList, out, "yyyy-MM-dd", colorMap, null, null,"null");
			ex2.createSheet("poi_problem_summary", workbook, headers2, poiProblemSummaryList, out, "yyyy-MM-dd", colorMap, null, null,"null");

			workbook.write(out);
			out.close();

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
	
	/**
	 * 获取countTable List
	 * @return
	 * @throws Exception
	 */
	private List<PoiCountTable> getCountTableList(String subtaskIds,Map<Integer, Map<String, Object>> subtaskMap,
			Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap)  throws Exception {
		
		Connection conn = null;
		String[]  subtaskIdArray = subtaskIds.split("\\,");
		Map<Integer,PoiCountTable> poiCountTableMap = new HashMap<>();
		List<Integer> collectUserInfoPidList = new ArrayList<>();//需要去履历中查询赋值的pidList
		List<PoiCountTable> poiCountTableList = new ArrayList<>();
		
		for (String subtaskId : subtaskIdArray) {
			Map<String, Object> subtaskInfoMap = subtaskMap.get(Integer.parseInt(subtaskId));
			String[] geometryArray = (((StringBuffer) subtaskInfoMap.get("geometry")).toString()).split("\\|");
			conn = (Connection) subtaskInfoMap.get("conn");
			
			IxPoiSelector poiSelector = new IxPoiSelector(conn);
			Map<String,List<Integer>> pidListMap = getPidList(geometryArray,conn);
			List<Integer> nonDelPidList = pidListMap.get("nonDelPidList");//非删除pidList
			List<Integer> delPidList = pidListMap.get("delPidList");//删除pidList
			
			Integer exeUserId = (Integer) subtaskInfoMap.get("exeUserId");
			
			log.info("start assembleNonDelPoiCountTable---subtaskId "+subtaskId);
			assembleNonDelPoiCountTable(nonDelPidList, poiSelector, subtaskId,exeUserId,fidPoiProblemSummaryMap,
					poiCountTableMap,collectUserInfoPidList,conn);//封装非删除的poiCountTable信息
			log.info("end assembleNonDelPoiCountTable---subtaskId "+subtaskId);
			
			log.info("start assembleDelPoiCountTable---subtaskId "+subtaskId);
			assembleDelPoiCountTable(delPidList, subtaskId, exeUserId,fidPoiProblemSummaryMap,
					poiCountTableMap,collectUserInfoPidList,conn);//封装删除的poiCountTable信息,统计项赋值0
			log.info("end assembleDelPoiCountTable---subtaskId "+subtaskId);
			
			log.info("start assembleCollectUserInfo---subtaskId "+subtaskId);
			assembleCollectUserInfo(collectUserInfoPidList,subtaskInfoMap,poiCountTableMap,conn);//需要去履历中查询赋值采集员采集日期等
			log.info("start assembleCollectUserInfo---subtaskId "+subtaskId);
		}
		
		
		for (PoiCountTable poiCountTable : poiCountTableMap.values()) {
			poiCountTableList.add(poiCountTable);
		}
		
		return poiCountTableList;
	}

	
	/**
	 * 需要去履历中查询赋值采集员采集日期等
	 * @param collectUserInfoPidList
	 * @param subtaskInfoMap
	 * @param poiCountTableMap 
	 * @param conn
	 * @throws Exception 
	 */
	private void assembleCollectUserInfo(List<Integer> collectUserInfoPidList, Map<String, Object> subtaskInfoMap,
			Map<Integer, PoiCountTable> poiCountTableMap, Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		try {
			
			StringBuilder sb = new StringBuilder();
			sb.append("	SELECT OB_PID,US_ID, OP_DT,STK_ID ");
			sb.append("	FROM (SELECT LD.OB_PID,LA.US_ID, LO.OP_DT,LA.STK_ID, ROW_NUMBER() OVER(partition by LD.OB_PID order BY LO.OP_DT DESC) ROW_NUM ");
			sb.append("		FROM LOG_ACTION LA, LOG_OPERATION LO, LOG_DETAIL LD");
			sb.append("		WHERE LA.ACT_ID = LO.ACT_ID");
			sb.append("		AND LO.OP_ID = LD.OP_ID");
			sb.append("		AND LD.OB_PID IN ");
			
			Clob pidClod = null;
			String ids = org.apache.commons.lang.StringUtils.join(collectUserInfoPidList, ",");
	        if (collectUserInfoPidList.size() > 1000) {
	            pidClod = ConnectionUtil.createClob(conn);
	            pidClod.setString(1, ids);
	            sb.append(" (select to_number(column_value) from table(clob_to_table(?)))");
	        } else {
	            sb.append(" (" + ids + ")");
	        }
	        
	        sb.append(" ) WHERE ROW_NUM = 1");
			
			pstmt = conn.prepareStatement(sb.toString());
			if(pidClod!=null){
				pstmt.setClob(1,pidClod);
			}
			rs =  pstmt.executeQuery();
			
			LogReader logRead = new LogReader(conn);
			
			Integer comSubtaskId = (Integer) subtaskInfoMap.get("comSubtaskId");
			
			List<Long> pidList = new ArrayList<>();
			for (Integer pid : collectUserInfoPidList) {
				pidList.add((long)pid);
			}
			Map<Long,Integer> stateResult  = logRead.getObjectState(pidList,"IX_POI");
			while(rs.next()){
				Integer pid = rs.getInt("OB_PID");
				Integer state = (stateResult.get(pid.longValue())==null?0:stateResult.get(pid.longValue()));
				Integer sktId = rs.getInt("stk_id");
				Integer usId = rs.getInt("us_id");
				long opDt = rs.getTimestamp("op_dt").getTime();
				
				PoiCountTable poiCountTable = poiCountTableMap.get(pid);
				boolean flag = false;
				if(state != 1){//修改，删除
					if(comSubtaskId != null && comSubtaskId == sktId){
						flag = true;
					}
				}else{
					flag = true;
				}
				if(flag){
					poiCountTable.setCollectorUserid(usId.toString().equals("0")?"AAA":usId.toString());
					poiCountTable.setCollectorTime(usId.toString().equals("0")?"null":DateUtils.longToString(opDt, "yyyy.MM.dd"));
					poiCountTableMap.put(pid, poiCountTable);
				}
					
				
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);

		}
		
	}


	/**
	 * 根据geometry扩圈,不过滤POI行记录为删除状态
	 * @param geometry
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	private Map<String,List<Integer>> getPidList(String[] geometryArray,Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try{
			
			StringBuffer sb = new StringBuffer();
			sb.append("SELECT distinct pid,u_record FROM ix_poi WHERE ");
			List<Clob> clobs = new ArrayList<>();
			for (int i=0;i< geometryArray.length ;i++) {
				Clob geoClob = ConnectionUtil.createClob(conn);
				geoClob.setString(1, geometryArray[i]);
				clobs.add(geoClob);
				sb.append(" sdo_within_distance(geometry,sdo_geometry(?,8307),'mask=anyinteract') = 'TRUE' ");
				if(i != (geometryArray.length-1)){
					sb.append(" OR ");
				}
			}
			
			pstmt = conn.prepareStatement(sb.toString());
			
			int clobSize = clobs.size();
			if(clobSize != 0){
				for (int i = 0; i < clobSize; i++){
					pstmt.setClob(i+1, clobs.get(i));
				}
			}
			
			resultSet = pstmt.executeQuery();
			
			Map<String,List<Integer>> map = new HashMap<>();
			List<Integer> nonDelPidList = new ArrayList<>();//非删除pidList
			List<Integer> delPidList = new ArrayList<>();//删除pidList
			while (resultSet.next()) {
				int pid = resultSet.getInt(1);
				if(resultSet.getInt(2) != 2){//非删除
					nonDelPidList.add(pid);
				}else{//删除
					delPidList.add(pid);
				}
			}
			map.put("delPidList", delPidList);
			map.put("nonDelPidList", nonDelPidList);
			return map;
		}catch(Exception e){
			DbUtils.rollback(conn);
			log.error(e.getMessage(), e);
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
		
	}

	
	
	/**
	 * 获取ProblemSummary列表
	 * @param gridClob
	 * @param checkConn
	 * @return
	 * @throws Exception
	 */
	private static List<PoiProblemSummaryExcel> getPoiProblemSummaryList(Clob gridClob, Connection checkConn,Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		
		try {
			List<PoiProblemSummaryExcel> poiProblemList = new ArrayList<PoiProblemSummaryExcel>();
			if(gridClob != null){
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT * FROM POI_PROBLEM_SUMMARY WHERE subtask_id in (select to_number(column_value) from table(clob_to_table(?))) ");
				pstmt = checkConn.prepareStatement(sb.toString());
				pstmt.setClob(1, gridClob);
				resultSet = pstmt.executeQuery();
				int num = 1;
				while (resultSet.next()) {
					String poiNum = resultSet.getString("POI_NUM");
					PoiProblemSummaryExcel poiProblemSummary = new PoiProblemSummaryExcel();
					poiProblemSummary.setNum(num);
					ReflectionAttrUtils.executeResultSet(poiProblemSummary, resultSet);
					poiProblemList.add(poiProblemSummary);
					if(!fidPoiProblemSummaryMap.containsKey(poiNum)){
						fidPoiProblemSummaryMap.put(poiNum, poiProblemSummary);
					}
					num++;
				}
			}
			
			return poiProblemList;
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 拼接需要导出的任务关联的质检子任务关闭且均未被导出过进行质检报表的 Subtask信息
	 * @return
	 * @throws Exception
	 */
	private void assembleExportSubtaskMap(Connection manConn,Map<Integer, Map<String, Object>> subtaskMap,
			Map<Integer, Connection> dbIdConnMap,List<Integer> subtaskQualityIdList) throws Exception {

		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			StringBuffer sb = new StringBuffer();
			sb.append(" SELECT ST.SUBTASK_ID COM_SUBTASK_ID, ST.EXE_USER_ID, AA.* FROM SUBTASK ST,");
			sb.append(" (SELECT T.TASK_ID,T.NAME, G.GROUP_NAME,S.SUBTASK_ID, R.DAILY_DB_ID, SQ.GEOMETRY, SQ.QUALITY_ID,S.QUALITY_METHOD ");
			sb.append(" FROM SUBTASK S, TASK T, REGION R, SUBTASK_QUALITY SQ,USER_GROUP G WHERE S.TASK_ID = T.TASK_ID ");
			sb.append(" AND T.REGION_ID = R.REGION_ID  AND SQ.SUBTASK_ID = S.SUBTASK_ID AND T.GROUP_ID = G.GROUP_ID ");
			sb.append(" AND S.IS_QUALITY = 1 AND S.STATUS = 0 AND SQ.POI_DB_STAT = 0 ORDER BY SUBTASK_ID )  AA");
			sb.append(" WHERE ST.QUALITY_SUBTASK_ID = AA.SUBTASK_ID");

			pstmt = manConn.prepareStatement(sb.toString());
			rs = pstmt.executeQuery();

			Map<String, Object> subtaskInfoMap = new HashMap<>();
			while (rs.next()) {
				Integer subtaskId = rs.getInt("SUBTASK_ID");

				if (!subtaskMap.containsKey(subtaskId)) {
					subtaskInfoMap = new HashMap<>();
					subtaskInfoMap.put("taskId", rs.getInt("TASK_ID"));
					subtaskInfoMap.put("taskName", rs.getString("NAME"));
					subtaskInfoMap.put("taskGroup", rs.getString("GROUP_NAME"));
					int dbId =  rs.getInt("DAILY_DB_ID");
					if(!dbIdConnMap.containsKey(dbId)){
						Connection conn = DBConnector.getInstance().getConnectionById(dbId);
						dbIdConnMap.put(dbId, conn);
					}
					subtaskInfoMap.put("conn",dbIdConnMap.get(dbId));
					int qualityMethod = (rs.getInt("QUALITY_METHOD") == 2?0:rs.getInt("QUALITY_METHOD"));//灵芸确认，如果质检方式为现场加室内，按现场处理
					subtaskInfoMap.put("qualityMethod", qualityMethod);
					subtaskInfoMap.put("comSubtaskId",rs.getInt("COM_SUBTASK_ID"));
					subtaskInfoMap.put("exeUserId",rs.getInt("EXE_USER_ID"));
					
					subtaskMap.put(subtaskId, subtaskInfoMap);
				}

				subtaskInfoMap = subtaskMap.get(subtaskId);

				StringBuffer geometry = new StringBuffer();
				if (subtaskInfoMap.containsKey("geometry")) {
					geometry = (StringBuffer) subtaskInfoMap.get("geometry");
					geometry.append("|");
				}

				STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
				try {
					geometry.append(GeoTranslator.struct2Wkt(struct));
				} catch (Exception e) {
					e.printStackTrace();
				}
				subtaskInfoMap.put("geometry", geometry);

				subtaskQualityIdList.add(rs.getInt("QUALITY_ID"));
			}
		} catch (Exception e) {
			log.error("error assembleExportSubtaskMap", e);
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 封装任务名-子任务号列表map
	 * @param subtaskMap
	 */
	private void assembleTaskNameSubtaskIdsMap(Map<Integer,Map<String, Object>> taskMap,
			Map<Integer, Map<String, Object>> subtaskMap){
		Map<String, Object> taskInfoMap = new HashMap<>();
		for (Entry<Integer, Map<String, Object>> entry : subtaskMap.entrySet()) {
			Integer subtaskId = entry.getKey();
			Map<String, Object> subtaskInfoMap = entry.getValue();
			Integer taskId  = (Integer) subtaskInfoMap.get("taskId");
			String taskName = (String) subtaskInfoMap.get("taskName");
			String taskGroup = (String) subtaskInfoMap.get("taskGroup");
			Integer qualityMethod = (Integer) subtaskInfoMap.get("qualityMethod");
			if(!taskMap.containsKey(taskId)){
				taskInfoMap = new HashMap<>();
				taskInfoMap.put("taskName", taskName);
				taskInfoMap.put("taskGroup", taskGroup);
				taskMap.put(taskId, taskInfoMap);
			}
			taskInfoMap = taskMap.get(taskId);
			
			StringBuffer xcSubtaskIds = new StringBuffer();
			StringBuffer snSubtaskIds = new StringBuffer();
			
			if (!taskInfoMap.containsKey("xcSubtaskIds")) {
				taskInfoMap.put("xcSubtaskIds", xcSubtaskIds);
			}
			if (!taskInfoMap.containsKey("snSubtaskIds")) {
				taskInfoMap.put("snSubtaskIds", snSubtaskIds);
			}
			
			if(qualityMethod == 0){//质检方式 现场
				xcSubtaskIds  = (StringBuffer) taskInfoMap.get("xcSubtaskIds");
				xcSubtaskIds = xcSubtaskIds.append(subtaskId.toString()+",");
			}else if(qualityMethod == 1){//质检方式 室内
				snSubtaskIds  = (StringBuffer) taskInfoMap.get("snSubtaskIds");
				snSubtaskIds = snSubtaskIds.append(subtaskId.toString()+",");
			}
			
			
		}
		
	}
	
	/**
	 * 封装非删除的poiCountTable信息
	 * @param delPidList
	 * @param conn
	 * @return
	 */
	private void assembleNonDelPoiCountTable(List<Integer> nonDelPidList,IxPoiSelector poiSelector,String subtaskId,Integer exeUserId,
			Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap,Map<Integer,PoiCountTable> poiCountTableMap,
			List<Integer> collectUserInfoPidList,Connection conn) throws Exception{
		log.info("getCountTableList ---- subtaskId---"+subtaskId+",nonDelPidList-----------"+nonDelPidList);
		
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT P.PID,P.POI_NUM,P.\"LEVEL\", P.MESH_ID, P.KIND_CODE, P.GEOMETRY,P.SIDE,P.LABEL,P.LINK_PID,P.POST_CODE, ");//poi基本信息项
		sb.append(" (SELECT N.NAME FROM IX_POI_NAME N WHERE N.NAME_CLASS = 1 AND N.NAME_TYPE = 2 AND N.LANG_CODE = 'CHI' AND P.PID = N.POI_PID) NAME,"); //名称统计项
		sb.append(" (SELECT COUNT(1) FROM IX_POI_ADDRESS PA WHERE PA.LANG_CODE = 'CHI' AND P.PID = PA.POI_PID) ADDRESS, ");//地址统计项
		sb.append(" (SELECT COUNT(1) FROM IX_POI_CONTACT PC WHERE P.PID = PC.POI_PID) CONTACT, ");//电话统计项
		sb.append(" (SELECT COUNT(1) FROM IX_POI_PARENT PP WHERE PP.PARENT_POI_PID = P.PID) FATHER,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_CHILDREN PC WHERE PC.CHILD_POI_PID = P.PID) SON,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_HOTEL WHERE POI_PID = P.PID) HOTEL,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_BUILDING WHERE POI_PID = P.PID) BUILDING,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_CHARGINGPLOT WHERE POI_PID = P.PID) CHARGINGPLOT,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_GASSTATION WHERE POI_PID = P.PID) GASSTATION,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_CHARGINGSTATION WHERE POI_PID = P.PID) CHARGINGSTATION,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_PARKING WHERE POI_PID = P.PID) PARKING,");
		sb.append(" (SELECT COUNT(1) FROM IX_POI_RESTAURANT WHERE POI_PID = P.PID) RESTAURANT ");
		sb.append(" FROM IX_POI P WHERE P.PID ") ;
		
		Clob pidClod = null;
		String ids = org.apache.commons.lang.StringUtils.join(nonDelPidList, ",");
        if (nonDelPidList.size() > 1000) {
            pidClod = ConnectionUtil.createClob(conn);
            pidClod.setString(1, ids);
            sb.append(" IN (select to_number(column_value) from table(clob_to_table(?)))");
        } else {
            sb.append(" IN (" + ids + ")");
        }
		
		
		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			if(pidClod!=null){
				pstmt.setClob(1, pidClod);
			}
			rs =  pstmt.executeQuery();
			
			while(rs.next()){
				PoiCountTable poiCountTable = new PoiCountTable();
				poiCountTable.setFid(rs.getString(2));
				poiCountTable.setLevel(rs.getString(3));
				poiCountTable.setLevelSiteCount(StringUtils.isBlank(rs.getString(3))?"0":"1");
				poiCountTable.setMeshId(String.valueOf(rs.getInt(4)));
				poiCountTable.setCategory(rs.getString(5));
				poiCountTable.setCategorySiteCount(StringUtils.isBlank(rs.getString(5))?"0":"1");
				poiCountTable.setPositionSiteCount(rs.getObject("GEOMETRY") == null ? "0":"1");
				poiCountTable.setSideSiteCount(rs.getInt("SIDE") == 0 ? "0":"1");
				poiCountTable.setLabelSiteCount(StringUtils.isBlank(rs.getString("LABEL"))?"0":"1");
				poiCountTable.setLinkSiteCount(rs.getInt("LINK_PID") == 0 ?"0":"1");
				poiCountTable.setPostcodeSiteCount(StringUtils.isBlank(rs.getString("POST_CODE"))?"0":"1");
				poiCountTable.setPoiName(StringUtils.isBlank(rs.getString("NAME"))?"null":rs.getString("NAME"));
				poiCountTable.setNameSiteCount(StringUtils.isBlank(rs.getString("NAME"))?"0":"1");
				poiCountTable.setAddressSiteCount(rs.getInt("ADDRESS") == 0 ? "0":"1");
				poiCountTable.setPhoteSiteCount(rs.getInt("CONTACT") == 0 ? "0":"1");
				poiCountTable.setFatherSonSiteCount((rs.getInt("FATHER") == 0 && rs.getInt("SON") == 0) ?"0":"1");
				poiCountTable.setDeepSiteCount((rs.getInt("HOTEL") == 0 && rs.getInt("BUILDING") == 0
							&&rs.getInt("CHARGINGPLOT") == 0 && rs.getInt("GASSTATION") == 0 && rs.getInt("CHARGINGSTATION") == 0
							&&rs.getInt("PARKING") == 0) ? "0":"1");
				poiCountTable.setResturantSiteCount(rs.getInt("RESTAURANT") == 0?"0":"1");
				
				PoiProblemSummaryExcel poiProblemSummary = fidPoiProblemSummaryMap.get(rs.getString(2));
				if(poiProblemSummary != null){//通过pid关联的poiProblemSummary 赋值CollectUser 相关信息
					setCollectUserInfoByProblemSummary(poiCountTable, poiProblemSummary);
				}else{
					poiCountTable.setCollectorUserid("AAA");
					poiCountTable.setCollectorTime("null");
					poiCountTable.setQcUserid("null");
					poiCountTable.setQcTime("null");
					poiCountTable.setMemoUserid(exeUserId.toString());
					collectUserInfoPidList.add(rs.getInt(1));
				}
				poiCountTable.setQcSubTaskid(subtaskId);
				poiCountTable.setVision(SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
				poiCountTable.setMemo("null");
				poiCountTable.setType("0");
				
				poiCountTableMap.put(rs.getInt(1), poiCountTable);
			}
		} catch (Exception e) {
			DbUtils.rollback(conn);
			log.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		
		
	}
	
	/**
	 * 通过pid关联的poiProblemSummary 赋值CollectUser 相关信息
	 * @param poiCountTable
	 * @param poiProblemSummary
	 * @throws Exception
	 */
	public void setCollectUserInfoByProblemSummary(PoiCountTable poiCountTable,PoiProblemSummaryExcel poiProblemSummary) throws Exception{
		poiCountTable.setCollectorUserid(StringUtils.isBlank(poiProblemSummary.getCollectorUser())?"null":poiProblemSummary.getCollectorUser()); 
		Date ct = poiProblemSummary.getCollectorTime();
		if(ct != null){
			poiCountTable.setCollectorTime(DateUtils.longToString(ct.getTime(), "yyyy.MM.dd"));
		} else {
			poiCountTable.setCollectorTime("null"); 
		}
		poiCountTable.setQcUserid(StringUtils.isBlank(poiProblemSummary.getModifyUser())?"null":poiProblemSummary.getModifyUser());
		Date qt = poiProblemSummary.getModifyDate();
		if(qt != null){
			poiCountTable.setQcTime(DateUtils.longToString(qt.getTime(), "yyyy.MM.dd"));
		} else {
			poiCountTable.setQcTime("null"); 
		}
		poiCountTable.setMemoUserid(StringUtils.isBlank(poiProblemSummary.getMemoUser())?"null":poiProblemSummary.getMemoUser());
	}
	
	
	/**
	 * 封装删除的poiCountTable信息
	 * @param delPidList
	 * @param conn
	 * @return
	 */
	private void assembleDelPoiCountTable(List<Integer> delPidList,String subtaskId,Integer exeUserId,
			Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap,Map<Integer,PoiCountTable> poiCountTableMap,
			List<Integer> collectUserInfoPidList,Connection conn) throws Exception{
		
		log.info("getCountTableList ---- subtaskId---"+subtaskId+",delPidList-----------"+delPidList);
		
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT P.PID,P.POI_NUM,P.\"LEVEL\", P.MESH_ID, P.KIND_CODE, NVL(PN.NAME,'null') FROM IX_POI P, ");
		sb.append(" (SELECT NAME,poi_pid FROM IX_POI_NAME N WHERE N.NAME_CLASS = 1 AND N.NAME_TYPE = 2 AND N.LANG_CODE = 'CHI') pn ");
		sb.append(" WHERE p.pid = pn.poi_pid(+) ");
		Clob pidClod = null;
		String ids = org.apache.commons.lang.StringUtils.join(delPidList, ",");
        if (delPidList.size() > 1000) {
            pidClod = ConnectionUtil.createClob(conn);
            pidClod.setString(1, ids);
            sb.append(" AND P.PID IN (select to_number(column_value) from table(clob_to_table(?)))");
        } else {
            sb.append(" AND P.PID IN (" + ids + ")");
        }
		
		
		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			if(pidClod!=null){
				pstmt.setClob(1, pidClod);
			}
			rs =  pstmt.executeQuery();
			
			while(rs.next()){
				PoiCountTable poiCountTable = new PoiCountTable();
				poiCountTable.setFid(rs.getString(2));
				poiCountTable.setLevel(rs.getString(3));
				poiCountTable.setMeshId(String.valueOf(rs.getInt(4)));
				poiCountTable.setCategory(rs.getString(5));
				poiCountTable.setPoiName(rs.getString(6));
				poiCountTable.setNameSiteCount("0");
				poiCountTable.setCategorySiteCount("0");
				poiCountTable.setAddressSiteCount("0");
				poiCountTable.setPhoteSiteCount("0");
				poiCountTable.setPositionSiteCount("0");
				poiCountTable.setSideSiteCount("0");
				poiCountTable.setFatherSonSiteCount("0");
				poiCountTable.setDeepSiteCount("0");
				poiCountTable.setLabelSiteCount("0");
				poiCountTable.setResturantSiteCount("0");
				poiCountTable.setLinkSiteCount("0");
				poiCountTable.setLevelSiteCount("0");
				poiCountTable.setPostcodeSiteCount("0");
				PoiProblemSummaryExcel poiProblemSummary = fidPoiProblemSummaryMap.get(rs.getString(2));
				if(poiProblemSummary != null){//通过pid关联的poiProblemSummary 赋值CollectUser 相关信息
					setCollectUserInfoByProblemSummary(poiCountTable, poiProblemSummary);
				}else{
					poiCountTable.setCollectorUserid("AAA");
					poiCountTable.setCollectorTime("null");
					poiCountTable.setQcUserid("null");
					poiCountTable.setQcTime("null");
					poiCountTable.setMemoUserid(exeUserId.toString());
					collectUserInfoPidList.add(rs.getInt(1));
				}
				poiCountTable.setQcSubTaskid(subtaskId);
				poiCountTable.setVision(SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
				poiCountTable.setMemo("null");
				poiCountTable.setType("0");
				
				poiCountTableMap.put(rs.getInt(1), poiCountTable);
			}
		} catch (Exception e) {
			DbUtils.rollback(conn);
			log.error(e.getMessage(),e);
			throw e;
		}finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		
	}
}
