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
import com.navinfo.dataservice.dao.glm.iface.IRow;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoi;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiAddress;
import com.navinfo.dataservice.dao.glm.model.poi.index.IxPoiName;
import com.navinfo.dataservice.dao.glm.selector.ReflectionAttrUtils;
import com.navinfo.dataservice.dao.glm.selector.poi.index.IxPoiSelector;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.job.statics.model.PoiCountTable;
import com.navinfo.dataservice.job.statics.model.PoiProblemSummaryExcel;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.vividsolutions.jts.geom.Geometry;

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
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[]  subtaskIdArray = subtaskIds.split("\\,");
		List<PoiCountTable> poiCountTableList = new ArrayList<PoiCountTable>();
		
		
		for (String subtaskId : subtaskIdArray) {
			Map<String, Object> subtaskInfoMap = subtaskMap.get(Integer.parseInt(subtaskId));
			String[] geometryArray = (((StringBuffer) subtaskInfoMap.get("geometry")).toString()).split("\\|");
			conn = (Connection) subtaskInfoMap.get("conn");
			
			
			IxPoiSelector poiSelector = new IxPoiSelector(conn);
			try {
				List<Integer> pidList = new ArrayList<>();
				for (String geometry : geometryArray) {
					pidList.addAll(getPidList(geometry,fidPoiProblemSummaryMap, conn));
				}
				
				log.info("getCountTableList ---- subtaskId---"+subtaskId+",pidList-----------"+pidList);
				List<IRow> iRowList =  poiSelector.loadByIds(pidList, false,true);
				for (IRow iRow : iRowList) {
					IxPoi poi = (IxPoi)iRow;
					PoiProblemSummaryExcel poiProblemSummary = fidPoiProblemSummaryMap.get(poi.getPoiNum());
					if(poiProblemSummary == null){continue;}
					PoiCountTable poiCountTable = new PoiCountTable();
					poiCountTable.setFid(poi.getPoiNum());
					poiCountTable.setLevel(poi.getLevel());
					poiCountTable.setMeshId(String.valueOf(poi.getMeshId()));
					poiCountTable.setCategory(poi.getKindCode());
					
					/**
					 * 统计项相关信息赋值
					 */
					setNameStatisticsByPoi(poiCountTable, poi);//赋值名称统计项
					setPositionStatisticsByPoi(poiCountTable, poi);//赋值点位统计项
					setCategoryStatisticsByPoi(poiCountTable, poi);//赋值分类统计项
					setAddressStatisticsByPoi(poiCountTable, poi);//赋值地址统计项
					setPhoteStatisticsByPoi(poiCountTable, poi);//赋值电话统计项
					setSideStatisticsByPoi(poiCountTable, poi);//赋值位置关系统计项
					setFatherSonStatisticsByPoi(poiCountTable, poi);//赋值父子关系统计项
					setDeepStatisticsByPoi(poiCountTable, poi,conn);//赋值深度信息统计项
					setLebelStatisticsByPoi(poiCountTable, poi);//赋值Lebel统计项
					setResturantStatisticsByPoi(poiCountTable, poi);//赋值餐厅统计项
					setLinkStatisticsByPoi(poiCountTable, poi);//赋值LINK_PID统计项
					setLevelStatisticsByPoi(poiCountTable, poi);//赋值Level统计项
					setPostCodeStatisticsByPoi(poiCountTable, poi);//赋值邮编统计项
					
					if(poiProblemSummary!=null){
						poiCountTable.setCollectorUserid(StringUtils.isBlank(poiProblemSummary.getCollectorUser())?"null":poiProblemSummary.getCollectorUser()); 
						Date ct = poiProblemSummary.getCollectorTime();
						if(ct != null){
							poiCountTable.setCollectorTime(DateUtils.longToString(ct.getTime(), "yyyy.MM.dd"));
						} else {
							poiCountTable.setCollectorTime("null"); 
						}
						poiCountTable.setInputTime("null"); 
						poiCountTable.setInputUserid("null");
						poiCountTable.setQcUserid(StringUtils.isBlank(poiProblemSummary.getModifyUser())?"null":poiProblemSummary.getModifyUser());
						Date qt = poiProblemSummary.getModifyDate();
						if(qt != null){
							poiCountTable.setQcTime(DateUtils.longToString(qt.getTime(), "yyyy.MM.dd"));
						} else {
							poiCountTable.setQcTime("null"); 
						}
						poiCountTable.setMemoUserid(StringUtils.isBlank(poiProblemSummary.getMemoUser())?"null":poiProblemSummary.getMemoUser());
					}
					
					poiCountTable.setQcSubTaskid(subtaskId);
					poiCountTable.setVision(SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion));
					poiCountTable.setMemo("null");
					poiCountTable.setType("0");
					
					
					poiCountTableList.add(poiCountTable);
				}
				
					
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw e;
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(pstmt);
			}
			
		}
		
		return poiCountTableList;
	}

	
	/**
	 * 赋值邮编统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setPostCodeStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		String postCode = poi.getPostCode();
		if(StringUtils.isNotBlank(postCode)){
			poiCountTable.setPostcodeSiteCount("1");
		}else{
			poiCountTable.setPostcodeSiteCount("0");
		}
		
	}

	/**
	 * 赋值位置关系统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setSideStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		Integer side = poi.getSide();
		if(side != 0){
			poiCountTable.setSideSiteCount("1");
		}else{
			poiCountTable.setSideSiteCount("0");
		}
	}

	/**
	 * 赋值Level统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setLevelStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		String level = poi.getLevel();
		if(StringUtils.isNotBlank(level)){
			poiCountTable.setLevelSiteCount("1");
		}else{
			poiCountTable.setLevelSiteCount("0");
		}
		
	}

	/**
	 * 赋值LINK_PID统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setLinkStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		Integer linkPid = poi.getLinkPid();
		if(linkPid != 0){
			poiCountTable.setLinkSiteCount("1");
		}else{
			poiCountTable.setLinkSiteCount("0");
		}
	}

	/**
	 * 赋值餐厅统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setResturantStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		if(poi.getRestaurants() !=null && poi.getRestaurants().size()>0 ){
			poiCountTable.setResturantSiteCount("1");
		}else{
			poiCountTable.setResturantSiteCount("0");
		}
		
	}

	/**
	 * 赋值lebel统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setLebelStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		String label = poi.getLabel();
		if(StringUtils.isNotBlank(label)){
			poiCountTable.setLabelSiteCount("1");
		}else{
			poiCountTable.setLabelSiteCount("0");
		}
	}

	/**
	 * 赋值深度信息统计
	 * @param poiCountTable
	 * @param poi
	 * @param conn
	 * @throws Exception 
	 */
	private void setDeepStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi, Connection conn) throws Exception {
		int data = setDeepInfoBySql(poi.getPid(),conn);
		if(data!=0){
			poiCountTable.setDeepSiteCount("1");
		}else{
			poiCountTable.setDeepSiteCount("0");
		}
		
	}
	
	/**
	 * 根据sql赋值深度信息
	 * @param sql
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public int setDeepInfoBySql(Integer pid,Connection conn) throws Exception{
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT COUNT(1) FROM IX_POI_HOTEL WHERE POI_PID = "+pid+" UNION ALL ");
		sb.append("SELECT COUNT(1) FROM IX_POI_BUILDING WHERE POI_PID = "+pid+" UNION ALL ");
		sb.append("SELECT COUNT(1) FROM IX_POI_GASSTATION WHERE POI_PID = "+pid+" UNION ALL ");
		sb.append("SELECT COUNT(1) FROM IX_POI_CHARGINGSTATION WHERE POI_PID = "+pid+" UNION ALL ");
		sb.append("SELECT COUNT(1) FROM IX_POI_CHARGINGPLOT WHERE POI_PID = "+pid+" UNION ALL ");
		sb.append("SELECT COUNT(1) FROM IX_POI_PARKING WHERE POI_PID = "+pid+"");

		PreparedStatement pstmt = null;
		ResultSet rs =  null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			rs =  pstmt.executeQuery();
			while(rs.next()){
				if(rs.getInt(1)>0){
					return rs.getInt(1);
				}
			}
		} catch (Exception e) {
			DbUtils.rollback(conn);
			log.error("获取深度信息出错,pid = "+pid,e);
		}finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
		return 0;
	
	}
	

	/**
	 * 赋值父子关系统计项
	 * @param poiCountTable
	 * @param poi
	 * @param conn
	 */
	private static void setFatherSonStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		poiCountTable.setFatherSonSiteCount("0");
		if(poi.getParents() != null && poi.getParents().size()>0 ){
			poiCountTable.setFatherSonSiteCount("1");
		}
		if(poi.getChildren() != null && poi.getChildren().size()>0 ){
			poiCountTable.setFatherSonSiteCount("1");
		}
		
	}

	/**
	 * 赋值电话统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setPhoteStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		if(poi.getContacts() != null && poi.getContacts().size()>0){
			poiCountTable.setPhoteSiteCount("1");
		}else{
			poiCountTable.setPhoteSiteCount("0");
		}
		
	}

	/**
	 * 赋值地址统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setAddressStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		poiCountTable.setAddressSiteCount("0");
		if(poi.getAddresses()!=null&&poi.getAddresses().size()>0){
			List<IRow> addresses = poi.getAddresses();
			for (IRow iRow : addresses) {
				IxPoiAddress addressTmp = (IxPoiAddress)iRow;
				if(addressTmp.getLangCode().equals("CHI")){
					poiCountTable.setAddressSiteCount("1");
				}
			}
		}
		
	}

	/**
	 * 赋值分类统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setCategoryStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		String kindCode = poi.getKindCode();
		if(StringUtils.isBlank(kindCode)){
			poiCountTable.setCategorySiteCount("0");
		}else{
			poiCountTable.setCategorySiteCount("1");
		}
	}

	/**
	 * 赋值点位统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setPositionStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		Geometry geometry = poi.getGeometry();
		if(geometry==null){
			poiCountTable.setPositionSiteCount("0");
		}else{
			poiCountTable.setPositionSiteCount("1");
		}
		
	}

	/**
	 * 赋值名称统计项
	 * @param poiCountTable
	 * @param poi
	 */
	private static void setNameStatisticsByPoi(PoiCountTable poiCountTable, IxPoi poi) {
		poiCountTable.setNameSiteCount("0");
		poiCountTable.setPoiName("null");
		if(poi.getNames()!=null&&poi.getNames().size()>0){
			List<IRow> names = poi.getNames();
			for (IRow iRow : names) {
				IxPoiName nameTmp = (IxPoiName)iRow;
				if(nameTmp.getLangCode().equals("CHI")&&nameTmp.getNameType()==2
						&&nameTmp.getNameClass()==1){
					poiCountTable.setPoiName(nameTmp.getName());
					poiCountTable.setNameSiteCount("1");
					break;
				}
			}
		}
		
	}

	/**
	 * 根据geometry扩圈
	 * @param geometry
	 * @param dbId
	 * @return
	 * @throws Exception
	 */
	private List<Integer> getPidList(String geometry, Map<String, PoiProblemSummaryExcel> fidPoiProblemSummaryMap,Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try{
			
			String sql = "SELECT distinct pid FROM ix_poi WHERE sdo_within_distance(geometry,sdo_geometry(?,8307),'mask=anyinteract') = 'TRUE'"
					+ " and poi_num in (select column_value from table(clob_to_table(?)))  and u_record <> 2 ";
			
			pstmt = conn.prepareStatement(sql);
			Clob geoClob =ConnectionUtil.createClob(conn);
			geoClob.setString(1, geometry);
			pstmt.setClob(1, geoClob);
			
			Clob pidClob = ConnectionUtil.createClob(conn);
			pidClob.setString(1, org.apache.commons.lang.StringUtils.join(fidPoiProblemSummaryMap.keySet(),","));
        	
			if(pidClob != null){
				pstmt.setClob(2,pidClob);
			}
			
			resultSet = pstmt.executeQuery();
			
			List<Integer> pidList = new ArrayList<>();
			while (resultSet.next()) {
				pidList.add(resultSet.getInt(1));
			}
			return pidList;
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
			sb.append(" SELECT T.TASK_ID,T.NAME, G.GROUP_NAME,S.SUBTASK_ID, R.DAILY_DB_ID, SQ.GEOMETRY, SQ.QUALITY_ID,S.QUALITY_METHOD ");
			sb.append(" FROM SUBTASK S, TASK T, REGION R, SUBTASK_QUALITY SQ,USER_GROUP G WHERE S.TASK_ID = T.TASK_ID ");
			sb.append(" AND T.REGION_ID = R.REGION_ID  AND SQ.SUBTASK_ID = S.SUBTASK_ID AND T.GROUP_ID = G.GROUP_ID ");
			sb.append(" AND S.IS_QUALITY = 1 AND S.STATUS = 0  ");
			sb.append(" AND SQ.POI_DB_STAT = 0 ORDER BY SUBTASK_ID ");

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
	
}
