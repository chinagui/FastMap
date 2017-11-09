package com.navinfo.dataservice.job.statics.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.commons.util.ExportExcel;
import com.navinfo.dataservice.dao.fcc.tips.selector.HbaseTipsQuery;
import com.navinfo.dataservice.job.statics.AbstractStatJob;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.geo.computation.CompGeometryUtil;
import com.navinfo.navicommons.geo.computation.GeometryUtils;
import com.vividsolutions.jts.geom.Geometry;
import net.sf.json.JSONObject;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.Map.Entry;

/**
 * 导出质检Tips报表
 * @Title:ExportQualityTipsReportJob
 * @Package:com.navinfo.dataservice.job.statics.job
 * @Description:
 * 导出原则： ①按任务导出，判断任务关联的质检子任务关闭且均未被导出过进行质检报表导出（man库subtask_quictly打导出标识）
          ②根据Field_RD_QCRecord、Field_POI_QCRecord 表中的“质检方式”分别导出两份excel报表，如果同一个子任务存在两种质检（现场、室内）方式，则需要分别导出两个excel。每个excel中包含两个sheet页②《Count Table》《③Problem Summary》
		      文件命导出名：任务名称+“质检报表”+室内/现场+导出日期
 * @date: 2017年11月6日
 */
public class ExportQualityTipsReportJob extends AbstractStatJob {

	public ExportQualityTipsReportJob(JobInfo jobInfo) {
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
		try {

			log.info("start export quality tips report");
			manConn = DBConnector.getInstance().getManConnection();
			checkConn = DBConnector.getInstance().getCheckConnection();

			log.info("start assemble subtask map");
			assembleExportSubtaskMap(manConn,subtaskMap,dbIdConnMap,subtaskQualityIdList);  //拼接需要导出的任务关联的质检子任务关闭且均未被导出过进行质检报表的 Subtask信息
			log.info("end assemble subtask map");

			log.info("start assemble task map");
			assembleTaskNameSubtaskIdsMap(taskMap,subtaskMap);//拼接需要导出的task信息
			log.info("end assemble task map");

			log.info("start export quality tips by task");
			exportExcelTipsByTask(checkConn,taskMap,subtaskMap);//按任务导出 导出质检报表
			log.info("end export quality tips  by task");

			log.info("start update subtask quality set db stat = 1");
			updateSubtaskQualityDbstat(subtaskQualityIdList);//导出后更新SUBTASK_QUALITY中TIPS_DB_STAT为1，已导出
			log.info("end update subtask quality set db stat = 1");

			log.info("end export quality tips report");
		} catch (Exception e) {
			try {
				DbUtils.rollback(manConn);
				DbUtils.rollback(checkConn);
			} catch (SQLException e1) {
				log.error("error export quality tips report", e);
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
		return "compelete export quality tips report ";
	}

	/**
	 * 按任务导出 导出质检报表
	 * @throws Exception
	 */
	public void exportExcelTipsByTask(Connection checkConn,Map<Integer,Map<String, Object>> taskMap,
			Map<Integer, Map<String, Object>> subtaskMap) throws Exception{
		String encoding = System.getProperty("file.encoding");
        try {
            for (Entry<Integer, Map<String, Object>> entry : taskMap.entrySet()) {

                Integer taskId = entry.getKey();

                log.info("start export taskId = " + taskId);

                Map<String, Object> taskInfo = entry.getValue();
                String taskGroup = (String) taskInfo.get("taskGroup");
                String taskName = (String) taskInfo.get("taskName");
                StringBuffer xcSubtaskIds = (StringBuffer) taskInfo.get("xcSubtaskIds");
                StringBuffer snSubtaskIds = (StringBuffer) taskInfo.get("snSubtaskIds");
                if (xcSubtaskIds != null && StringUtils.isNotBlank(xcSubtaskIds.toString())) {
                    xcSubtaskIds.deleteCharAt(xcSubtaskIds.length() - 1);
                }
                if (snSubtaskIds != null && StringUtils.isNotBlank(snSubtaskIds.toString())) {
                    snSubtaskIds.deleteCharAt(snSubtaskIds.length() - 1);
                }

                String xcSubtaskIdStr = xcSubtaskIds.toString();
                String snSubtaskIdStr = snSubtaskIds.toString();

                taskGroup = new String(taskGroup.getBytes("UTF-8"), encoding);

                String path = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathTips)
                        + "/" + DateUtils.dateToString(new Date(), "yyyyMMdd") + "/" + taskGroup + "/tip/";

                String fileName = null;
                String dateStr = DateUtils.dateToString(new Date(), "yyyyMMddHHmmssSSS");

                if (xcSubtaskIdStr != null && StringUtils.isNotEmpty(xcSubtaskIdStr)) {
                    log.info("start export taskId = " + taskId + " xnSubtaskIdStr = " + xcSubtaskIdStr);
                    fileName = taskName + "质检报表现场" + dateStr;
                    fileName = new String(fileName.getBytes("UTF-8"), encoding);
                    log.info("start export path = " + path + " fileName = " + fileName);

                    //导出tip 外业质检现场报表
                    exportExcelTip(path, checkConn, xcSubtaskIdStr, fileName, subtaskMap);
                }

                if (snSubtaskIdStr != null && StringUtils.isNotEmpty(snSubtaskIdStr)) {
                    log.info("start export taskId = " + taskId + " snSubtaskIdStr = " + snSubtaskIdStr);
                    fileName = taskName + "质检报表室内" + dateStr;
                    fileName = new String(fileName.getBytes("UTF-8"), encoding);
                    log.info("start export path = " + path + " fileName = " + fileName);
                    //导出tip外业质检室内报表
                    exportExcelTip(path, checkConn, snSubtaskIdStr, fileName, subtaskMap);
                }

                log.info("end export taskId = " + taskId);

            }
        }catch (Exception e) {
            throw  e;
        }
	}

    /**
	 * 导出完成后更新DB统计状态
	 * @param subtaskQualityIdList
	 * @throws Exception
	 */
	private void updateSubtaskQualityDbstat(List<Integer> subtaskQualityIdList) throws Exception {
		Connection conn = null;
		PreparedStatement pstmt = null;
        try {

        	conn = DBConnector.getInstance().getManConnection();

        	Clob gridClob = ConnectionUtil.createClob(conn);
    		gridClob.setString(1, StringUtils.join(subtaskQualityIdList,","));

			if(gridClob != null){
				StringBuffer sb = new StringBuffer();
				sb.append("UPDATE SUBTASK_QUALITY SET TIPS_DB_STAT = 1 WHERE QUALITY_ID IN (select to_number(column_value) from table(clob_to_table(?)))");
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
     * @param path
     * @param checkConn
     * @param subtaskIds
     * @param fileName
     * @param subtaskMap
     * @throws Exception
     */
	private void exportExcelTip(String path, Connection checkConn,
			String subtaskIds, String fileName,Map<Integer, Map<String, Object>> subtaskMap) throws Exception {

		Clob gridClob = ConnectionUtil.createClob(checkConn);
		gridClob.setString(1, subtaskIds);

		log.info("start get tip problem summary list");
        List<Map<Integer, Object>> problemSummaryList = getProblemSummaryList(gridClob, checkConn);//获取poiProblemSummaryList
		log.info("end get tip problem summary list");



		log.info("start get tip count table list");
        List<Map<Integer, Object>> tipCountTableList = getCountTableList(subtaskIds,subtaskMap);//获取poiCountTableList
		log.info("end get tip count table list");

        if((tipCountTableList != null && tipCountTableList.size() > 0) ||
                (problemSummaryList != null && problemSummaryList.size() > 0)) {
            exportExcelWithCheckMode(path, fileName, tipCountTableList, problemSummaryList);
        }
	}

	/**
	 * 现场监察，室内监察导出质检报告
	 * @param path
	 * @param fileName
	 * @param problemSummaryList
	 * @throws Exception
	 */
	private void exportExcelWithCheckMode(String path, String fileName,
			List<Map<Integer, Object>> tipCountTableList,List<Map<Integer, Object>> problemSummaryList) throws Exception{
		ExportExcel<Map<Integer, Object>> ex1 = new ExportExcel<>();
        ExportExcel<Map<Integer, Object>> ex2 = new ExportExcel<>();
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

            String templatePath = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathTips);
            FileInputStream fis = new FileInputStream(templatePath + "/" + TipsQualityReportConstant.QC_TEMPLATE_XLS_NAME);
			HSSFWorkbook workbook = new HSSFWorkbook(fis);
			Map<String, Integer> colorMap = new HashMap<>();
			colorMap.put("red", 255);
			colorMap.put("green", 0);
			colorMap.put("blue", 255);

            ex1.createXLSByTemplate(TipsQualityReportConstant.TEMPLATE_COUNT_TABLE_NAME, workbook,
                    tipCountTableList,  "yyyy-MM-dd", colorMap, "null");
            ex2.createXLSByTemplate(TipsQualityReportConstant.TEMPLATE_PROBLEM_SUMMARY_NAME, workbook,
                    problemSummaryList,  "yyyy-MM-dd", colorMap, "null");
			workbook.write(out);
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
            throw e;
		} catch (IOException e) {
			e.printStackTrace();
            throw e;
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
	private List<Map<Integer, Object>> getCountTableList(String subtaskIds,
                                                  Map<Integer, Map<String, Object>> subtaskMap) throws Exception {

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[]  subtaskIdArray = subtaskIds.split("\\,");
        Connection tipsIndexConn = null;

        int rowNum = 0;
        List<Map<Integer, Object>> resultList = new ArrayList<>();
		for (String subtaskId : subtaskIdArray) {
			Map<String, Object> subtaskInfoMap = subtaskMap.get(Integer.parseInt(subtaskId));
			String[] geometryArray = (((StringBuffer) subtaskInfoMap.get("geometry")).toString()).split("\\|");
			conn = (Connection) subtaskInfoMap.get("conn");
            tipsIndexConn = DBConnector.getInstance().getTipsIdxConnection();
			try {

                //TODO 作业员

                Map<Integer, LinkInfo> allLinkInfoMap = new HashMap<>();
                Map<String, Map<String, Integer>> allLineMap = new HashMap<>();
                Map<String, Set<String>> allNoRelateMap = new HashMap<>();

				for (String geometry : geometryArray) {
                    //1.获取质检子任务范围内的link 完全包含 U_RECORD ≠2或未关联删除标记
                    Map<Integer, LinkInfo> linkInfoMap = getLinkMap(geometry, conn);
                    if(linkInfoMap != null && linkInfoMap.size() > 0){
                        allLinkInfoMap.putAll(linkInfoMap);
                    }


                    //2.非修形用测线或未关联删除标记
                    Map<String, Map<String, Integer>> lineMap = getLineMap(geometry, tipsIndexConn);
                    if(lineMap != null && lineMap.size() > 0) {
                        allLineMap.putAll(lineMap);
                    }

					//3.没有关联link的tips红绿灯、挂接、草图和立交桥名,按照任务统计总数
					//根据质检圈范围统计tips的数据总量记录在第一条行记录对应的列项上
                    Map<String, Set<String>> noRelateMap = getNoRelateTips(geometry, tipsIndexConn);
                    for(String sourceType : noRelateMap.keySet()) {
                        if(allNoRelateMap.containsKey(sourceType)) {
                            Set<String> noRelateSet = allNoRelateMap.get(sourceType);
                            noRelateSet.addAll(noRelateMap.get(sourceType));
                        }else{
                            allNoRelateMap.put(sourceType, noRelateMap.get(sourceType));
                        }
                    }
				}

                //查询link关联的Tips，如果存在删除标记该link不导出报表
                StringBuffer linkPidStr = getStringByMap(allLinkInfoMap);
                if(linkPidStr != null && linkPidStr.length() > 0) {
                    //存在删除标记的link不导出不处理
                    Set<String> delLinkSet = getDelLinkSet(linkPidStr.toString(), tipsIndexConn);
                    for(String linkPid : delLinkSet) {
                        allLinkInfoMap.remove(Integer.valueOf(linkPid));
                    }
                    linkPidStr = getStringByMap(allLinkInfoMap);
                    //查询link的关联Tips
                    Map<String, Map<String, Integer>> linkRelateTipsMap
                            = getLinkRelateTipsMap(linkPidStr.toString(), tipsIndexConn);

                    for(Integer linkPid : allLinkInfoMap.keySet()) {
                        rowNum ++;
                        LinkInfo linkInfo = allLinkInfoMap.get(linkPid);
                        Map<String, Integer> relateTipsCountMap = linkRelateTipsMap.get(linkPid);
                        Map<Integer, Object> resultMap = getLinkResultMap(rowNum, linkInfo, conn, linkPid,
                                relateTipsCountMap, subtaskInfoMap);
                        resultList.add(resultMap);
                    }
                }

                //测线过滤非修形
                if(allLineMap.size() > 0) {
                    String[] queryColNames = { "deep", "source", "geometry" };
                    Map<String, JSONObject> hbaseMap = HbaseTipsQuery.getHbaseTipsByRowkeys(allLineMap.keySet(), queryColNames);
                    for(String rowkey : allLineMap.keySet()) {
                        Map<String, Integer> lineSourceMap = allLineMap.get(rowkey);
                        if(lineSourceMap.containsKey("2101")) {//有删除标记跳过不导出
                            continue;
                        }
                        JSONObject hbaseTips = hbaseMap.get(rowkey);
                        JSONObject geometry = hbaseTips.getJSONObject("geometry");
                        JSONObject deep = hbaseTips.getJSONObject("deep");
                        JSONObject source = hbaseTips.getJSONObject("source");
                        String sourceType = source.getString("s_sourceType");//2001测线 2002ADAS测线
                        if(sourceType.equals("2001")) {//测线
                            int shp = deep.getInt("shp");
                            if(shp == 1) {//修形的跳过不导出
                                continue;
                            }
                        }
                        rowNum ++;
                        Map<Integer, Object> resultMap = getLineResultMap(rowNum, deep, geometry, rowkey, lineSourceMap, subtaskInfoMap);
                        resultList.add(resultMap);
                    }

                }

			} catch (Exception e) {
				log.error(e.getMessage(), e);
				throw e;
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(pstmt);
			}

		}

		return resultList;
	}

    private Map<Integer, Object> getLinkResultMap(int rowNum, LinkInfo linkInfo, Connection conn, Integer linkPid,
                                                  Map<String, Integer> relateTipsCountMap, Map<String, Object> subtaskInfoMap) throws Exception {
        //子任务信息相关
        String cityName = (String)subtaskInfoMap.get("cityName");
        String subtaskName = (String)subtaskInfoMap.get("taskName");
        String groupName = (String)subtaskInfoMap.get("taskGroup");
        String exeUser = (String)subtaskInfoMap.get("exeUser");
        String startDate = (String)subtaskInfoMap.get("startDate");
        String collDate = (String)subtaskInfoMap.get("collDate");
        String memoUser = (String)subtaskInfoMap.get("memoUser");

        Map<Integer, Object> resultMap = new HashMap<>();
        resultMap.put(0, rowNum);
        resultMap.put(1, cityName);//城市
        resultMap.put(2, subtaskName);//子任务名
        resultMap.put(3, groupName);//基地


        resultMap.put(4, linkInfo.getMeshId());//图幅号
        resultMap.put(5, linkPid);//LINK号
        resultMap.put(6, linkInfo.getLength());//距离
        resultMap.put(7, "null");//数据块，不维护
        resultMap.put(8, linkInfo.getFc());//功能等级
        resultMap.put(9, linkInfo.getName());//道路名
        resultMap.put(10, exeUser);//质检员
        resultMap.put(11, startDate);//质检日期
        resultMap.put(12, memoUser);//TODO 作业员
        resultMap.put(13, collDate);//作业日期
        int colNum = 13;
        for(int i = 0; i < TipsQualityReportConstant.COUNT_TABLE_COLUMN_NUM; i++) {
            colNum ++;
            String colContent = TipsQualityReportConstant.countTableMapping.get(colNum + 1);
            String[] colContentArray = colContent.split(TipsQualityReportConstant.COUNT_TABLE_COLUMN_SPLIT);
            String sourceType = colContentArray[0];
            String gdbSql = null;
            if(colContentArray.length > 1) {
                gdbSql = colContentArray[1];
            }

            if(gdbSql != null && gdbSql.equals(TipsQualityReportConstant.COUNT_TABLE_ONE_FLAG)) {
                resultMap.put(colNum, "1");
            }else{
                int statCount = 0;
                if(relateTipsCountMap != null && relateTipsCountMap.get(sourceType) != null) {
                    statCount = relateTipsCountMap.get(sourceType);
                }
                if (gdbSql != null && gdbSql.equals(TipsQualityReportConstant.COUNT_TABLE_TOTAL_FLAG)) {//只统计总量
                    resultMap.put(colNum, "0");
                    if (rowNum == 1) {//总量都写在第一行
                        resultMap.put(colNum, String.valueOf(statCount));
                    }
                } else {
                    if (statCount == 0 && gdbSql != null) {
                        //GDB查询统计
                        statCount = getGdbLinkCountMap(gdbSql, linkPid, conn);
                    }
                    resultMap.put(colNum, String.valueOf(statCount));
                }
            }
        }
        //备注作业员
        resultMap.put(colNum + 1, memoUser);
        return resultMap;
    }

    private Map<Integer, Object> getLineResultMap(int rowNum, JSONObject deep, JSONObject geometry, String rowkey,
                                                  Map<String, Integer> lineSourceMap, Map<String, Object> subtaskInfoMap) throws Exception {
        //子任务信息相关
        String cityName = (String)subtaskInfoMap.get("cityName");
        String subtaskName = (String)subtaskInfoMap.get("taskName");
        String groupName = (String)subtaskInfoMap.get("taskGroup");
        String exeUser = (String)subtaskInfoMap.get("exeUser");
        String startDate = (String)subtaskInfoMap.get("startDate");
        String collDate = (String)subtaskInfoMap.get("collDate");
        String memoUser = (String)subtaskInfoMap.get("memoUser");

        Map<Integer, Object> resultMap = new HashMap<>();
        resultMap.put(0, rowNum);
        resultMap.put(1, cityName);//城市
        resultMap.put(2, subtaskName);//子任务名
        resultMap.put(3, groupName);//基地

        //2001 2002 统计坐标 deep.geo
        String meshId = "null";
        try {
            JSONObject ggeoJSON = deep.getJSONObject("geo");
            Geometry wktGeo = GeoTranslator.geojson2Jts(ggeoJSON);
            String[] meshes = CompGeometryUtil.geo2MeshesWithoutBreak(wktGeo);
            if(meshes != null && meshes.length > 0) {
                meshId = meshes[0];
            }
        }catch (Exception e) {

        }

        Geometry wktLocation = GeoTranslator.geojson2Jts(geometry.getJSONObject("g_location"));
        double lineLength = GeometryUtils.getLinkLength(wktLocation);
        resultMap.put(4, meshId);//图幅号
        resultMap.put(5, rowkey);//LINK号
        resultMap.put(6, lineLength);//距离
        resultMap.put(7, "null");//数据块，不维护
        resultMap.put(8, "null");//功能等级
        resultMap.put(9, "null");//道路名
        resultMap.put(10, exeUser);//质检员
        resultMap.put(11, startDate);//质检日期
        resultMap.put(12, memoUser);//TODO 作业员
        resultMap.put(13, collDate);//作业日期
        int colNum = 13;
        for(int i = 0; i < TipsQualityReportConstant.COUNT_TABLE_COLUMN_NUM; i++) {
            colNum ++;
            String colContent = TipsQualityReportConstant.countTableMapping.get(colNum + 1);
            String[] colContentArray = colContent.split(TipsQualityReportConstant.COUNT_TABLE_COLUMN_SPLIT);
            String sourceType = colContentArray[0];
            String gdbSql = null;
            if(colContentArray.length > 1) {
                gdbSql = colContentArray[1];
            }

            if(gdbSql != null && gdbSql.equals(TipsQualityReportConstant.COUNT_TABLE_ONE_FLAG)) {
                resultMap.put(colNum, "1");
            }else{
                int statCount = 0;
                if(lineSourceMap != null && lineSourceMap.get(sourceType) != null) {
                    statCount = lineSourceMap.get(sourceType);
                }
                if (gdbSql != null && gdbSql.equals(TipsQualityReportConstant.COUNT_TABLE_TOTAL_FLAG)) {//只统计总量
                    resultMap.put(colNum, "0");
                    if (rowNum == 1) {//总量都写在第一行
                        resultMap.put(colNum, String.valueOf(statCount));
                    }
                }else{
                    resultMap.put(colNum, String.valueOf(statCount));
                }
            }
        }
        //备注作业员
        resultMap.put(colNum + 1, memoUser);
        return resultMap;
    }

    private StringBuffer getStringByMap(Map<Integer, LinkInfo> allLinkInfoMap) {
        StringBuffer linkPidStr = null;
        for(Integer linkPid : allLinkInfoMap.keySet()) {
            if(linkPidStr == null) {
                linkPidStr = new StringBuffer();
                linkPidStr.append("'" + linkPid + "'");
            }else{
                linkPidStr.append(",");
                linkPidStr.append("'" + linkPid + "'");
            }
        }
        return linkPidStr;
    }

	/**
	 * 根据geometry扩圈
	 * @param geometry
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private Map<Integer, LinkInfo> getLinkMap(String geometry, Connection conn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try{

			String sql = "WITH TMP AS\n" +
                    " (SELECT LISTAGG(RN.NAME, ',') WITHIN GROUP(ORDER BY RN.NAME_ID) LINK_NAME,\n" +
                    "         RLN.LINK_PID\n" +
                    "    FROM RD_LINK_NAME RLN, RD_NAME RN\n" +
                    "   WHERE RLN.NAME_GROUPID = RN.NAME_GROUPID\n" +
                    "     AND RLN.NAME_CLASS = 1\n" +
                    "     AND RN.LANG_CODE IN ('CHI', 'CHT')\n" +
                    "   GROUP BY LINK_PID)\n" +
                    "SELECT RL.LINK_PID, RL.MESH_ID, T.LINK_NAME, RL.LENGTH, RL.FUNCTION_CLASS\n" +
                    "  FROM RD_LINK RL, TMP T\n" +
                    " WHERE RL.LINK_PID = T.LINK_PID(+)\n" +
                    "   AND SDO_RELATE(GEOMETRY, SDO_GEOMETRY(?, 8307), 'mask=COVEREDBY') =\n" +
                    "       'TRUE'\n" +
                    "   AND U_RECORD <> 2";

			pstmt = conn.prepareStatement(sql);
			Clob geoClob =ConnectionUtil.createClob(conn);
			geoClob.setString(1, geometry);
			pstmt.setClob(1, geoClob);

			resultSet = pstmt.executeQuery();

			Map<Integer, LinkInfo> linkMap = new HashMap<>();
			while (resultSet.next()) {
                //RL.LINK_PID, RL.MESH_ID, T.LINK_NAME, RL.LENGTH
                LinkInfo linkInfo = new LinkInfo();
                linkInfo.setLinkPid(resultSet.getInt("LINK_PID"));
                linkInfo.setMeshId(resultSet.getInt("MESH_ID"));
                linkInfo.setLength(resultSet.getDouble("LENGTH"));
                linkInfo.setName(resultSet.getString("LINK_NAME"));
                linkMap.put(linkInfo.getLinkPid(), linkInfo);
			}
			return linkMap;
		}catch(Exception e){
			DbUtils.rollback(conn);
			log.error(e.getMessage(), e);
			throw e;
		}finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}

	}


    private int getGdbLinkCountMap(String sql, int linkPid, Connection conn) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, linkPid);

            resultSet = pstmt.executeQuery();

            Map<Integer, LinkInfo> linkMap = new HashMap<>();
            if (resultSet.next()) {
                int count = resultSet.getInt("CT");
                return count;
            }
            return 0;
        }catch(Exception e){
            DbUtils.rollback(conn);
            log.error(e.getMessage(), e);
            throw e;
        }finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

    }

    private Map<String, Map<String, Integer>> getLineMap(String geometry, Connection conn) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{

            String sql = "SELECT TI.ID, RTI.S_SOURCETYPE, COUNT(1) RELATE_COUNT\n" +
                    "  FROM TIPS_INDEX TI, TIPS_LINKS TL, TIPS_INDEX RTI\n" +
                    " WHERE TI.ID = TL.LINK_ID\n" +
                    "   AND TL.ID = RTI.ID\n" +
                    "   AND TI.S_SOURCETYPE IN ('2001', '2002')\n" +
                    "   AND SDO_RELATE(TI.WKTLOCATION, SDO_GEOMETRY(?, 8307), 'mask=COVEREDBY') =\n" +
                    "       'TRUE'\n" +
                    " GROUP BY TI.ID, RTI.S_SOURCETYPE";

            pstmt = conn.prepareStatement(sql);
            Clob geoClob =ConnectionUtil.createClob(conn);
            geoClob.setString(1, geometry);
            pstmt.setClob(1, geoClob);

            resultSet = pstmt.executeQuery();

            Map<String, Map<String, Integer>> lineMap = new HashMap<>();
            while (resultSet.next()) {

                String rowkey = resultSet.getString("ID");
                String sourceType = resultSet.getString("S_SOURCETYPE");
                int count = resultSet.getInt("RELATE_COUNT");
                if(lineMap.containsKey(rowkey)) {
                    Map<String, Integer> lineSourceMap = lineMap.get(rowkey);
                    lineSourceMap.put(sourceType, count);
                }else {
                    Map<String, Integer> lineSourceMap = new HashMap<>();
                    lineSourceMap.put(sourceType, count);
                    lineMap.put(rowkey, lineSourceMap);
                }
            }
            return lineMap;
        }catch(Exception e){
            DbUtils.rollback(conn);
            log.error(e.getMessage(), e);
            throw e;
        }finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

    }

    private Map<String, Set<String>> getNoRelateTips(String geometry, Connection conn) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{

            String sql = "SELECT TI.ID, TI.S_SOURCETYPE\n" +
                    "  FROM TIPS_INDEX TI\n" +
                    " WHERE TI.S_SOURCETYPE IN (?)\n" +
                    "   AND SDO_RELATE(TI.WKTLOCATION, SDO_GEOMETRY(?, 8307), 'mask=COVEREDBY') =\n" +
                    "       'TRUE'";

            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, TipsQualityReportConstant.NO_REALTE_TYPE_QUERY);

            Clob geoClob =ConnectionUtil.createClob(conn);
            geoClob.setString(1, geometry);
            pstmt.setClob(2, geoClob);

            resultSet = pstmt.executeQuery();

            Map<String, Set<String>> noRelateMap = new HashMap<>();
            while (resultSet.next()) {
                String rowkey = resultSet.getString("ID");
                String sourceType = resultSet.getString("S_SOURCETYPE");
                if(noRelateMap.containsKey(sourceType)) {
                    Set<String> noRelateSet = noRelateMap.get(sourceType);
                    noRelateSet.add(rowkey);
                }else {
                    Set<String> noRelateSet = new HashSet<>();
                    noRelateSet.add(rowkey);
                    noRelateMap.put(sourceType, noRelateSet);
                }
            }
            return noRelateMap;
        }catch(Exception e){
            DbUtils.rollback(conn);
            log.error(e.getMessage(), e);
            throw e;
        }finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(pstmt);
        }

    }

    private Set<String> getDelLinkSet(String linkPidStr, Connection conn) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{
            String delLinkSql = "SELECT DISTINCT TL.LINK_ID\n" +
                    "  FROM TIPS_INDEX TI, TIPS_LINKS TL\n" +
                    " WHERE TI.ID = TL.ID\n" +
                    "   AND TI.S_SOURCETYPE = '2101'\n" +
                    "   AND TL.LINK_ID IN (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))";

            pstmt = conn.prepareStatement(delLinkSql);

            Clob linkIdClob = ConnectionUtil.createClob(conn);
            linkIdClob.setString(1, linkPidStr);
            pstmt.setClob(1, linkIdClob);

            resultSet = pstmt.executeQuery();

            Set<String> delLinkIdSet = new HashSet<>();
            while (resultSet.next()) {
                String linkId = resultSet.getString("LINK_ID");
                delLinkIdSet.add(linkId);
            }
            return delLinkIdSet;
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
     * 查询link关联的Tips
     * @param linkPidStr
     * @param conn
     * @return
     * @throws Exception
     */
    private Map<String, Map<String, Integer>> getLinkRelateTipsMap(String linkPidStr, Connection conn) throws Exception {
        PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        try{
            String relateTipsSql = "SELECT TL.LINK_ID, TI.S_SOURCETYPE, COUNT(1) CT\n" +
                    "  FROM TIPS_LINKS TL, TIPS_INDEX TI\n" +
                    " WHERE TL.ID = TI.ID\n" +
                    "   AND TL.LINK_ID IN\n" +
                    "       (SELECT COLUMN_VALUE FROM TABLE(CLOB_TO_TABLE(?)))\n" +
                    " GROUP BY TL.LINK_ID, TI.S_SOURCETYPE";

            pstmt = conn.prepareStatement(relateTipsSql);

            Clob linkIdClob = ConnectionUtil.createClob(conn);
            linkIdClob.setString(1, linkPidStr);
            pstmt.setClob(1, linkIdClob);

            resultSet = pstmt.executeQuery();

            Map<String, Map<String, Integer>> linkRelateMap = new HashMap<>();
            while (resultSet.next()) {
                String linkId = resultSet.getString("LINK_ID");
                String sourceType = resultSet.getString("S_SOURCETYPE");
                int count = resultSet.getInt("CT");
                if(linkRelateMap.containsKey(linkId)) {
                    Map<String, Integer> tipsCountMap = linkRelateMap.get(linkId);
                    tipsCountMap.put(sourceType, count);
                }else{
                    Map<String, Integer> tipsCountMap = new HashMap<>();
                    tipsCountMap.put(sourceType, count);
                    linkRelateMap.put(linkId, tipsCountMap);
                }
            }
            return linkRelateMap;
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
	private static List<Map<Integer, Object>> getProblemSummaryList(Clob gridClob, Connection checkConn) throws Exception {
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;

		try {
            List<Map<Integer, Object>> problemResult = new ArrayList<>();
			if(gridClob != null){
				StringBuffer sb = new StringBuffer();
				sb.append("SELECT * FROM FIELD_RD_QCRECORD WHERE QC_SUBTASK in (select to_number(column_value) from table(clob_to_table(?))) ");
				pstmt = checkConn.prepareStatement(sb.toString());
				pstmt.setClob(1, gridClob);
				resultSet = pstmt.executeQuery();
				int num = 1;
				while (resultSet.next()) {
                    Map<Integer, Object> colValueMap = new HashMap<>();
                    for(int i = 0; i < TipsQualityReportConstant.PROBLEM_SUMMARY_COLUMN_NUM; i ++) {
                        int index = i + 1;
                        if(TipsQualityReportConstant.problemSummaryMapping.containsKey(index)) {
                            String colValue = resultSet.getString(TipsQualityReportConstant.problemSummaryMapping.get(index));
                            colValueMap.put(i, colValue);
                        }else{
                            if(i == 0) {
                                colValueMap.put(i, num + "");
                            }else{
                                colValueMap.put(i, "null");
                            }
                        }
                    }
                    problemResult.add(colValueMap);

					num++;
				}
			}

			return problemResult;
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

			String sql = "WITH TMP AS\n" +
                    " (SELECT MT.OBJ_ID, MT.OPERATE_DATE\n" +
                    "    FROM MAN_TIMELINE MT\n" +
                    "   WHERE MT.OPERATE_TYPE = 1\n" +
                    "     AND MT.OBJ_TYPE = 'subtask')\n" +
                    "SELECT T.TASK_ID,\n" +
                    "       T.NAME,\n" +
                    "       G.GROUP_NAME,\n" +
                    "       S.SUBTASK_ID,\n" +
                    "       R.DAILY_DB_ID,\n" +
                    "       SQ.GEOMETRY,\n" +
                    "       SQ.QUALITY_ID,\n" +
                    "       S.QUALITY_METHOD,\n" +
                    "       C.CITY_NAME,\n" +
                    "       S.EXE_USER_ID,\n" +
                    "       OS.ACTUAL_START_DATE,\n" +
                    "       CS.EXE_USER_ID       MEMO_USER,\n" +
                    "       MT.OPERATE_DATE      COLL_DATE\n" +
                    "  FROM SUBTASK                  S,\n" +
                    "       TASK                     T,\n" +
                    "       REGION                   R,\n" +
                    "       SUBTASK_QUALITY          SQ,\n" +
                    "       USER_GROUP               G,\n" +
                    "       PROGRAM                  P,\n" +
                    "       CITY                     C,\n" +
                    "       FM_STAT_OVERVIEW_SUBTASK OS,\n" +
                    "       SUBTASK                  CS,\n" +
                    "       TMP                      MT\n" +
                    " WHERE S.TASK_ID = T.TASK_ID\n" +
                    "   AND T.REGION_ID = R.REGION_ID\n" +
                    "   AND SQ.SUBTASK_ID = S.SUBTASK_ID\n" +
                    "   AND T.GROUP_ID = G.GROUP_ID\n" +
                    "   AND S.IS_QUALITY = 1\n" +
                    "   AND S.STATUS = 0\n" +
                    "   AND SQ.TIPS_DB_STAT = 0\n" +
                    "   AND P.PROGRAM_ID = T.PROGRAM_ID\n" +
                    "   AND P.CITY_ID = C.CITY_ID\n" +
                    "   AND S.SUBTASK_ID = OS.SUBTASK_ID(+)\n" +
                    "   AND S.SUBTASK_ID = CS.QUALITY_SUBTASK_ID\n" +
                    "   AND CS.SUBTASK_ID = MT.OBJ_ID(+)\n" +
                    " ORDER BY SUBTASK_ID";

			pstmt = manConn.prepareStatement(sql);
			rs = pstmt.executeQuery();

			Map<String, Object> subtaskInfoMap = new HashMap<>();
			while (rs.next()) {
				Integer subtaskId = rs.getInt("SUBTASK_ID");

				if (!subtaskMap.containsKey(subtaskId)) {
					subtaskInfoMap = new HashMap<>();
					subtaskInfoMap.put("taskId", rs.getInt("TASK_ID"));
					subtaskInfoMap.put("taskName", rs.getString("NAME"));
					subtaskInfoMap.put("taskGroup", rs.getString("GROUP_NAME"));
                    subtaskInfoMap.put("cityName", rs.getString("CITY_NAME"));
                    subtaskInfoMap.put("exeUser", rs.getString("EXE_USER_ID"));
                    String startDate = rs.getTimestamp("ACTUAL_START_DATE") == null
                            ? "null" : DateUtils.formatDate(rs.getTimestamp("ACTUAL_START_DATE"));
                    subtaskInfoMap.put("startDate", startDate);
                    String collDate = rs.getTimestamp("COLL_DATE") == null
                            ? "null" : DateUtils.formatDate(rs.getTimestamp("COLL_DATE"));
                    subtaskInfoMap.put("collDate", collDate);
                    subtaskInfoMap.put("memoUser", rs.getString("MEMO_USER"));
					int dbId =  rs.getInt("DAILY_DB_ID");
					if(!dbIdConnMap.containsKey(dbId)){
						Connection conn = DBConnector.getInstance().getConnectionById(dbId);
						dbIdConnMap.put(dbId, conn);
					}
					subtaskInfoMap.put("conn",dbIdConnMap.get(dbId));
					int qualityMethod = rs.getInt("QUALITY_METHOD");//灵芸确认，如果质检方式为现场加室内，按现场处理
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

			if(qualityMethod == 1 || qualityMethod == 3){//质检方式 现场
				xcSubtaskIds  = (StringBuffer) taskInfoMap.get("xcSubtaskIds");
				xcSubtaskIds = xcSubtaskIds.append(subtaskId.toString()+",");
			}else if(qualityMethod == 2){//质检方式 室内
				snSubtaskIds  = (StringBuffer) taskInfoMap.get("snSubtaskIds");
				snSubtaskIds = snSubtaskIds.append(subtaskId.toString()+",");
			}
		}

	}

    class LinkInfo {
        private int linkPid;
        private int meshId;
        private double length;
        private String name;
        private int fc;

        public int getLinkPid() {
            return linkPid;
        }

        public void setLinkPid(int linkPid) {
            this.linkPid = linkPid;
        }

        public int getMeshId() {
            return meshId;
        }

        public void setMeshId(int meshId) {
            this.meshId = meshId;
        }

        public double getLength() {
            return length;
        }

        public void setLength(double length) {
            this.length = length;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getFc() {
            return fc;
        }

        public void setFc(int fc) {
            this.fc = fc;
        }
    }
}
