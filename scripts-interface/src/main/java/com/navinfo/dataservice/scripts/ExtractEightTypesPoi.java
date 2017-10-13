package com.navinfo.dataservice.scripts;

import java.io.File;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.geom.GeoTranslator;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.DateUtils;
import com.navinfo.dataservice.scripts.model.EightTypesPoi;
import com.navinfo.navicommons.database.QueryRunner;
import com.vividsolutions.jts.geom.Geometry;

import oracle.sql.STRUCT;
/**
 * 8类变化poi提取
 * @Title: ExtractEightTypesPoi
 * @Package: com.navinfo.dataservice.scripts
 * @Description:
 * 	提取数据背景及目的：月度后期或季后期提出数据，导入NM，内业进行作业
 * 	提取总原则：
 * 		提取POI分类为180104、180105、180106、180304、180307、180308、180309、180400（共8类）的POI数据且POI的状态为新增或删除或修改（仅改名称或改分类或改显示坐标）
 * @Author: LittleDog
 * @Date: 2017年9月14日
 * @Version: V1.0
 */
public class ExtractEightTypesPoi {
	
	private static Logger log = LoggerRepos.getLogger(ExtractEightTypesPoi.class);
	private static Map<String, Integer> metaMeshMap = new HashMap<>();
	private static Map<String, Integer> manAdminCodeMap = new HashMap<>();
	private static Map<Integer, Connection> allRegionConn = new HashMap<>();
	private static int valExceptionId = 1;
	final static Map<Long,String> insertAdminCodeMap = new HashMap<>();
	final static Map<Long,String> updateAdminCodeMap = new HashMap<>();
	final static Map<Connection, List<Long>> connPidsMap = new HashMap<>();
	
	public static void execute(String startDate,String endDate) throws Exception {
		
		startDate = startDate + "000000";
		endDate = endDate + "235959";
		Connection monthConn = null;
		Connection manConn = null;
		Connection metaConn = null;
		try {
			
			monthConn = DBConnector.getInstance().getMkConnection();
			metaConn = DBConnector.getInstance().getMetaConnection();
			manConn = DBConnector.getInstance().getManConnection();
					
			metaMeshMap = queryMap(metaConn, "SELECT DISTINCT T.MESH, T.ACTION FROM SC_PARTITION_MESHLIST T");
			manAdminCodeMap = queryMap(manConn, "SELECT DISTINCT T.ADMINCODE, T.REGION_ID FROM CP_REGION_PROVINCE T");
			allRegionConn = queryAllRegionConn(manConn);
			
			String excelName = "partition_result_data_" + DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
			
			Map<Long, Map<String, Object>> insertMap = searchInsertPidExtractEightTypesPoi(monthConn, startDate, endDate);
			Map<Long, Map<String, Object>> updateMap = searchUpdatePidExtractEightTypesPoi(monthConn, startDate, endDate);
			Map<Long, Map<String, Object>> deleteMap = searchDeletePidExtractEightTypesPoi(allRegionConn, startDate, endDate);
			
			updateMap.keySet().removeAll(insertMap.keySet());
			updateMap.keySet().removeAll(deleteMap.keySet());
			insertMap.keySet().removeAll(deleteMap.keySet());
			
			List<EightTypesPoi> insertEightTypesPois = insertAndUpdateResultSet2EightTypesPoi(manConn, insertAdminCodeMap, insertMap);
			List<EightTypesPoi> updateEightTypesPois = insertAndUpdateResultSet2EightTypesPoi(manConn, updateAdminCodeMap, updateMap);
			List<EightTypesPoi> deleteEightTypesPois = deleteResultSet2EightTypesPoi(manConn, connPidsMap, deleteMap);
			
			
			List<EightTypesPoi> exportEightTypesPois = new ArrayList<>();
			exportEightTypesPois.addAll(insertEightTypesPois);
			exportEightTypesPois.addAll(updateEightTypesPois);
			exportEightTypesPois.addAll(deleteEightTypesPois);
			
			String dir = SystemConfigFactory.getSystemConfig().getValue(PropConstant.downloadFilePathPoi) + "/extractPoi/" + excelName + ".db";
			
			File file = new File(dir);
			if(!file.getParentFile().isDirectory()){
				file.getParentFile().mkdirs();
			}
			
			Connection sqliteConn = ExportEightTypes2Sqlite.createSqlite(dir);
			ExportEightTypes2Sqlite.exportEightTypesPoi(sqliteConn, exportEightTypesPois);

		} catch (Exception e) {
			log.error("提取发生异常",e);
			throw e;
		} finally {
			DbUtils.closeQuietly(monthConn);
			DbUtils.closeQuietly(metaConn);
			DbUtils.closeQuietly(manConn);
			for (Connection value : allRegionConn.values()) {  
				DbUtils.closeQuietly(value);
			}  
		}

	}

	/**
	 * 查询新增履历的8类变化pid(月库)
	 * @param condition
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private static Map<Long, Map<String, Object>> searchInsertPidExtractEightTypesPoi(Connection monthConn, String startDate, String endDate) throws Exception {
		StringBuilder insertSql = new StringBuilder();
		
		insertSql.append("SELECT P.PID,															  ");
		insertSql.append("       P.KIND_CODE,													  ");
		insertSql.append("       P.MESH_ID,														  ");
		insertSql.append("       SUBSTR(TO_CHAR(AD.ADMIN_ID), 0, 2) || '0000' ADMIN_ID,			  ");
		insertSql.append("       P.GEOMETRY,													  ");
		insertSql.append("       N.NAME															  ");
		insertSql.append("  FROM IX_POI P, IX_POI_NAME N, AD_ADMIN AD							  ");
		insertSql.append(" WHERE P.PID = N.POI_PID												  ");
		insertSql.append("   AND AD.REGION_ID = P.REGION_ID										  ");
		insertSql.append("   AND N.LANG_CODE = 'CHI'											  ");
		insertSql.append("   AND N.NAME_CLASS = 1												  ");
		insertSql.append("   AND N.NAME_TYPE = 1												  ");
		insertSql.append("   AND P.KIND_CODE IN													  ");
		insertSql.append("       (180104, 180105, 180106, 180304, 180307, 180308, 180309, 180400) ");
		insertSql.append("   AND EXISTS															  ");
		insertSql.append(" (SELECT 1															  ");
		insertSql.append("          FROM LOG_OPERATION LO, LOG_DETAIL LD						  ");
		insertSql.append("         WHERE LO.OP_ID = LD.OP_ID									  ");
		insertSql.append("           AND LD.OB_PID = P.PID										  ");
		insertSql.append("           AND LO.OP_DT > TO_DATE(?, 'yyyymmddhh24miss')		  		  ");
		insertSql.append("           AND LO.OP_DT <= TO_DATE(?, 'yyyymmddhh24miss')	   			  ");
		insertSql.append("           AND LD.OB_NM = 'IX_POI'									  ");
		insertSql.append("           AND LD.TB_NM = 'IX_POI'									  ");
		insertSql.append("           AND LD.OP_TP = 1)											  ");
		
		log.info("查询新增poi SQL：" + insertSql.toString());
		try {
			QueryRunner run = new QueryRunner();
			Map<Long,Map<String, Object>> insertMap = run.query(monthConn, insertSql.toString(), new ResultSetHandler<Map<Long,Map<String, Object>>>() {
				public Map<Long,Map<String, Object>> handle(ResultSet rs) throws SQLException {
					Map<Long,Map<String, Object>> map = new HashMap<>();
					while (rs.next()) {
						long pid = rs.getLong("PID");
						String kindCode = rs.getString("KIND_CODE");
						String meshId = rs.getString("MESH_ID");
						String adminId = rs.getString("ADMIN_ID");
						insertAdminCodeMap.put(pid, adminId);
						STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
						Geometry poiGeo = null;
						try {
							poiGeo = GeoTranslator.struct2Jts(struct);
						} catch (Exception e) {
							e.printStackTrace();
						}
						String xShow = String.valueOf(poiGeo.getCoordinate().x);
						String yShow = String.valueOf(poiGeo.getCoordinate().y);
						String name = rs.getString("NAME");
						
						//若为新增:新增POI:官方标准中文名称|分类代码；
						String information = new StringBuilder().append("新增POI：").append(name).append("|").append(kindCode).toString();
						//POINT (120.31579 31.55737)
						String location = new StringBuilder().append("POINT(").append(xShow).append(" ").append(yShow).append(")").toString();
						//[IX_POI,96706311]
						String targets = new StringBuilder().append("[IX_POI,").append(String.valueOf(pid)).append("]").toString();
						
						Map<String, Object> temp = new HashMap<>();
						temp.put("information", information);
						temp.put("location", location);
						temp.put("targets", targets);
						temp.put("meshId", meshId);
						temp.put("additionInfo", metaMeshMap.get(meshId));
						map.put(pid, temp);
					}
					return map;
				}
			}, startDate, endDate);
			
			log.info("================== 新增poi ： totalNum ： " + insertMap.size() + " ==================");
			
			//TODO
			return insertMap;
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}
	
	/**
	 * 查询删除履历的8类变化pid(日库)
	 * @param allRegionConn
	 * @param manConn
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	private static Map<Long,Map<String, Object>> searchDeletePidExtractEightTypesPoi(Map<Integer, Connection> allRegionConn, String startDate, String endDate) throws Exception {
		StringBuilder deteleSql = new StringBuilder();
		
		deteleSql.append("SELECT P.PID, P.KIND_CODE, P.MESH_ID, P.GEOMETRY, N.NAME				  ");
		deteleSql.append("  FROM IX_POI P, IX_POI_NAME N										  ");
		deteleSql.append(" WHERE P.PID = N.POI_PID												  ");
		deteleSql.append("   AND N.LANG_CODE = 'CHI'											  ");
		deteleSql.append("   AND N.NAME_CLASS = 1												  ");
		deteleSql.append("   AND N.NAME_TYPE = 1												  ");
		deteleSql.append("   AND P.KIND_CODE IN													  ");
		deteleSql.append("       (180104, 180105, 180106, 180304, 180307, 180308, 180309, 180400) ");
		deteleSql.append("   AND EXISTS															  ");
		deteleSql.append(" (SELECT 1															  ");
		deteleSql.append("          FROM LOG_OPERATION LO, LOG_DETAIL LD						  ");
		deteleSql.append("         WHERE LO.OP_ID = LD.OP_ID									  ");
		deteleSql.append("           AND LD.OB_PID = P.PID										  ");
		deteleSql.append("           AND LO.OP_DT > TO_DATE(?, 'yyyymmddhh24miss')				  ");
		deteleSql.append("           AND LO.OP_DT <= TO_DATE(?, 'yyyymmddhh24miss')				  ");
		deteleSql.append("           AND LD.OB_NM = 'IX_POI'									  ");
		deteleSql.append("           AND LD.TB_NM = 'IX_POI'									  ");
		deteleSql.append("           AND LD.OP_TP = 2											  ");
		deteleSql.append("           AND LO.COM_STA = 1)										  ");
		
		log.info("查询删除poi SQL：" + deteleSql.toString());
		Map<Long,Map<String, Object>> deleteMap = new HashMap<>();
		try {
			QueryRunner run = new QueryRunner();
			
			for (final Connection regionConn : allRegionConn.values()) {  
				Map<Long,Map<String, Object>> tempMap = run.query(regionConn, deteleSql.toString(), new ResultSetHandler<Map<Long, Map<String, Object>>>() {
					public Map<Long, Map<String, Object>> handle(ResultSet rs) throws SQLException {
						
						Map<Connection, List<Long>> tempConnPidsMap = new HashMap<>();
						List<Long> tempPidList = new ArrayList<>();
						
						Map<Long, Map<String, Object>> map = new HashMap<>();
						while (rs.next()) {
							long pid = rs.getLong("PID");
							tempPidList.add(pid);
							String kindCode = rs.getString("KIND_CODE");
							String meshId = rs.getString("MESH_ID");
							STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
							Geometry poiGeo = null;
							try {
								poiGeo = GeoTranslator.struct2Jts(struct);
							} catch (Exception e) {
								e.printStackTrace();
							}
							String xShow = String.valueOf(poiGeo.getCoordinate().x);
							String yShow = String.valueOf(poiGeo.getCoordinate().y);
							String name = rs.getString("NAME");
							
							//若为新增:新增POI:官方标准中文名称|分类代码；
							String information = new StringBuilder().append("删除POI：").append(name).append("|").append(kindCode).toString();
							//POINT (120.31579 31.55737)
							String location = new StringBuilder().append("POINT(").append(xShow).append(" ").append(yShow).append(")").toString();
							//[IX_POI,96706311]
							String targets = new StringBuilder().append("[IX_POI,").append(String.valueOf(pid)).append("]").toString();
							
							Map<String, Object> temp = new HashMap<>();
							temp.put("information", information);
							temp.put("location", location);
							temp.put("targets", targets);
							temp.put("meshId", meshId);
							temp.put("additionInfo", metaMeshMap.get(meshId));
							map.put(pid, temp);
						}
						
						tempConnPidsMap.put(regionConn, tempPidList);
						connPidsMap.putAll(tempConnPidsMap);
						return map;
					}
				}, startDate, endDate);
				
				deleteMap.putAll(tempMap);
			}
			
			log.info("================== 删除poi ： totalNum ： " + deleteMap.size() + " ==================");
			
			return deleteMap;
			
			//TODO
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	/**
	 * 查询修改履历的高速变化pid(月库)
	 * @param condition
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private static Map<Long, Map<String, Object>> searchUpdatePidExtractEightTypesPoi(Connection monthConn, String startDate, String endDate) throws Exception {
		StringBuilder updateSql = new StringBuilder();
		
		updateSql.append("SELECT P.PID,															  ");
		updateSql.append("       P.KIND_CODE,													  ");
		updateSql.append("       P.MESH_ID,														  ");
		updateSql.append("       SUBSTR(TO_CHAR(AD.ADMIN_ID), 0, 2) || '0000' ADMIN_ID,			  ");
		updateSql.append("       P.GEOMETRY,												      ");
		updateSql.append("       N.NAME,														  ");
		updateSql.append("       'NAME' FLAG													  ");
		updateSql.append("  FROM IX_POI P, IX_POI_NAME N, AD_ADMIN AD							  ");
		updateSql.append(" WHERE P.PID = N.POI_PID											      ");
		updateSql.append("   AND AD.REGION_ID = P.REGION_ID										  ");
		updateSql.append("   AND N.LANG_CODE = 'CHI'											  ");
		updateSql.append("   AND N.NAME_CLASS = 1												  ");
		updateSql.append("   AND N.NAME_TYPE = 1												  ");
		updateSql.append("   AND P.KIND_CODE IN													  ");
		updateSql.append("       (180104, 180105, 180106, 180304, 180307, 180308, 180309, 180400) ");
		updateSql.append("   AND EXISTS															  ");
		updateSql.append(" (SELECT 1															  ");
		updateSql.append("          FROM LOG_OPERATION LO, LOG_DETAIL LD, IX_POI_NAME PN		  ");
		updateSql.append("         WHERE LO.OP_ID = LD.OP_ID									  ");
		updateSql.append("           AND LD.TB_ROW_ID = PN.ROW_ID								  ");
		updateSql.append("           AND LD.OB_PID = P.PID									      ");
		updateSql.append("           AND LO.OP_DT > TO_DATE(?, 'yyyymmddhh24miss')				  ");
		updateSql.append("           AND LO.OP_DT <= TO_DATE(?, 'yyyymmddhh24miss')				  ");
		updateSql.append("           AND LD.OB_NM = 'IX_POI'							          ");
		updateSql.append("           AND LD.TB_NM = 'IX_POI_NAME'								  ");
		updateSql.append("           AND PN.LANG_CODE = 'CHI'									  ");
		updateSql.append("           AND PN.NAME_CLASS = 1										  ");
		updateSql.append("           AND PN.NAME_TYPE = 2										  ");
		updateSql.append("           AND LD.FD_LST LIKE '%\"NAME\"%')							  ");

		updateSql.append("UNION ALL																  ");

		updateSql.append("SELECT P.PID,															  ");
		updateSql.append("       P.KIND_CODE,													  ");
		updateSql.append("       P.MESH_ID,														  ");
		updateSql.append("       SUBSTR(TO_CHAR(AD.ADMIN_ID), 0, 2) || '0000' ADMIN_ID,			  ");
		updateSql.append("       P.GEOMETRY,													  ");
		updateSql.append("       N.NAME,												  		  ");
		updateSql.append("       'KIND_CODE' FLAG												  ");
		updateSql.append("  FROM IX_POI P, IX_POI_NAME N, AD_ADMIN AD							  ");
		updateSql.append(" WHERE P.PID = N.POI_PID												  ");
		updateSql.append("   AND AD.REGION_ID = P.REGION_ID										  ");
		updateSql.append("   AND N.LANG_CODE = 'CHI'											  ");
		updateSql.append("   AND N.NAME_CLASS = 1												  ");
		updateSql.append("   AND N.NAME_TYPE = 1												  ");
		updateSql.append("   AND P.KIND_CODE IN													  ");
		updateSql.append("       (180104, 180105, 180106, 180304, 180307, 180308, 180309, 180400) ");
		updateSql.append("   AND EXISTS															  ");
		updateSql.append(" (SELECT 1															  ");
		updateSql.append("          FROM LOG_OPERATION LO, LOG_DETAIL LD						  ");
		updateSql.append("         WHERE LO.OP_ID = LD.OP_ID									  ");
		updateSql.append("           AND LD.OB_PID = P.PID										  ");
		updateSql.append("           AND LO.OP_DT > TO_DATE(?, 'yyyymmddhh24miss')				  ");
		updateSql.append("           AND LO.OP_DT <= TO_DATE(?, 'yyyymmddhh24miss')				  ");
		updateSql.append("           AND LD.OB_NM = 'IX_POI'									  ");
		updateSql.append("           AND LD.TB_NM = 'IX_POI'									  ");
		updateSql.append("           AND LD.FD_LST LIKE '%KIND_CODE%')							  ");

		updateSql.append("UNION ALL																  ");

		updateSql.append("SELECT P.PID,															  ");
		updateSql.append("       P.KIND_CODE,													  ");
		updateSql.append("       P.MESH_ID,														  ");
		updateSql.append("       SUBSTR(TO_CHAR(AD.ADMIN_ID), 0, 2) || '0000' ADMIN_ID,			  ");
		updateSql.append("       P.GEOMETRY,													  ");
		updateSql.append("       N.NAME,														  ");
		updateSql.append("       'GEOMETRY' FLAG												  ");
		updateSql.append("  FROM IX_POI P, IX_POI_NAME N, AD_ADMIN AD							  ");
		updateSql.append(" WHERE P.PID = N.POI_PID												  ");
		updateSql.append("   AND AD.REGION_ID = P.REGION_ID										  ");
		updateSql.append("   AND N.LANG_CODE = 'CHI'											  ");
		updateSql.append("   AND N.NAME_CLASS = 1												  ");
		updateSql.append("   AND N.NAME_TYPE = 1												  ");
		updateSql.append("   AND P.KIND_CODE IN													  ");
		updateSql.append("       (180104, 180105, 180106, 180304, 180307, 180308, 180309, 180400) ");
		updateSql.append("   AND EXISTS															  ");
		updateSql.append(" (SELECT 1															  ");
		updateSql.append("          FROM LOG_OPERATION LO, LOG_DETAIL LD						  ");
		updateSql.append("         WHERE LO.OP_ID = LD.OP_ID									  ");
		updateSql.append("           AND LD.OB_PID = P.PID										  ");
		updateSql.append("           AND LO.OP_DT > TO_DATE(?, 'yyyymmddhh24miss')		     	  ");
		updateSql.append("           AND LO.OP_DT <= TO_DATE(?, 'yyyymmddhh24miss')	  			  ");
		updateSql.append("           AND LD.OB_NM = 'IX_POI'									  ");
		updateSql.append("           AND LD.TB_NM = 'IX_POI'									  ");
		updateSql.append("           AND LD.FD_LST LIKE '%GEOMETRY%') 							  ");
		
		log.info("查询改分类，改名称及改显示坐标SQL：" + updateSql.toString());
		try {
			QueryRunner run = new QueryRunner();
			Map<Long,Map<String, Object>> updateMap = run.query(monthConn, updateSql.toString(), new ResultSetHandler<Map<Long,Map<String, Object>>>() {
				@Override
				public Map<Long,Map<String, Object>> handle(ResultSet rs) throws SQLException {
					Map<Long,Map<String, Object>> map = new HashMap<>();
					while (rs.next()) {
						long pid = rs.getLong("PID");
						String flag = rs.getString("FLAG");
						
						if(map.containsKey(pid)){
							if ("NAME".equals(flag)){
								map.get(pid).put("information", new StringBuilder("POI改名称|").append(map.get(pid).get("information")).toString());
							} else if ("KIND_CODE".equals(flag)){
								map.get(pid).put("information", new StringBuilder("POI改分类|").append(map.get(pid).get("information")).toString());
							} else if ("GEOMETRY".equals(flag)){
								map.get(pid).put("information", new StringBuilder("POI改位移|").append(map.get(pid).get("information")).toString());
							}
						} else {
							String kindCode = rs.getString("KIND_CODE");
							STRUCT struct = (STRUCT) rs.getObject("GEOMETRY");
							Geometry poiGeo = null;
							try {
								poiGeo = GeoTranslator.struct2Jts(struct);
							} catch (Exception e) {
								e.printStackTrace();
							}
							String xShow = String.valueOf(poiGeo.getCoordinate().x);
							String yShow = String.valueOf(poiGeo.getCoordinate().y);
							String meshId = rs.getString("MESH_ID");
							String name = rs.getString("NAME");
							String adminId = rs.getString("ADMIN_ID");
							
							updateAdminCodeMap.put(pid, adminId);
							
							StringBuilder information = new StringBuilder();
							if ("NAME".equals(flag)){
								information.append("POI改名称：").append(name).append("|").append(kindCode);
							} else if ("KIND_CODE".equals(flag)){
								information.append("POI改分类：").append(name).append("|").append(kindCode);
							} else if ("GEOMETRY".equals(flag)){
								information.append("POI改位移：").append(name).append("|").append(kindCode);
							}
							String location = new StringBuilder().append("POINT(").append(xShow).append(" ").append(yShow).append(")").toString();
							String targets = new StringBuilder().append("[IX_POI,").append(String.valueOf(pid)).append("]").toString();
							
							Map<String, Object> temp = new HashMap<>();
							temp.put("information", information.toString());
							temp.put("location", location);
							temp.put("targets", targets);
							temp.put("meshId", meshId);
							temp.put("additionInfo", metaMeshMap.get(meshId));
							map.put(pid, temp);
						}
					}
					return map;
				}
			}, startDate, endDate, startDate, endDate, startDate, endDate);
			
			log.info("================== 修改poi ： totalNum ： " + updateMap.size() + " ==================");
			
			//TODO
			return updateMap;
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		}
	}

	private static List<EightTypesPoi> insertAndUpdateResultSet2EightTypesPoi(Connection manConn, Map<Long,String> adminCodeMap, Map<Long, Map<String, Object>> insertMapOrUpdateMap) throws Exception {
		List<EightTypesPoi> list = new ArrayList<>();
		Map<Integer, List<Long>> regionAndPidsMap = getRegionAndPidsMap(adminCodeMap);
		Map<Long, Long> pidSubtaskId = new HashMap<>(); 
		for (Entry<Integer, List<Long>> regionAndPidsEntry : regionAndPidsMap.entrySet()) {
			Connection regionConn = allRegionConn.get(regionAndPidsEntry.getKey());
			List<Long> pidList = regionAndPidsEntry.getValue();
			if(!CollectionUtils.isEmpty(pidList)){
				Map<Long, Long> pidSubtaskIdOrQualitySubtaskId = querySubtaskId(regionConn, pidList);
				Map<Long, Long> regionPidSubtaskId = subtaskIdIsQuality(manConn, pidSubtaskIdOrQualitySubtaskId);
				pidSubtaskId.putAll(regionPidSubtaskId);
			}
		}
		
		for (Entry<Long, Map<String, Object>> entry : insertMapOrUpdateMap.entrySet()) {
			EightTypesPoi eightTypesPoi = new EightTypesPoi();
			
			eightTypesPoi.setValExceptionId(valExceptionId);
			eightTypesPoi.setGroupId(0);
			eightTypesPoi.setLevel(0);
			eightTypesPoi.setRuleId(0);
			eightTypesPoi.setSituation("NULL");
			eightTypesPoi.setInformation((String) entry.getValue().get("information") == null ? "" : (String) entry.getValue().get("information"));
			eightTypesPoi.setSuggestion("NULL");
			eightTypesPoi.setLocation((String) entry.getValue().get("location") == null ? "" : (String) entry.getValue().get("location"));
			eightTypesPoi.setTargets((String) entry.getValue().get("targets") == null ? "" : (String) entry.getValue().get("targets"));
			eightTypesPoi.setAdditionInfo((Integer) entry.getValue().get("additionInfo") == null ? 0 : (Integer) entry.getValue().get("additionInfo"));
			eightTypesPoi.setScopeFlag(1);
			eightTypesPoi.setCreated(DateUtils.longToString(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss"));
			eightTypesPoi.setUpdated(DateUtils.longToString(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss"));
			eightTypesPoi.setMeshId((String) entry.getValue().get("meshId") == null ? "" : (String) entry.getValue().get("meshId"));
			eightTypesPoi.setProvinceName("NULL");
			eightTypesPoi.setTaskId(pidSubtaskId.get(entry.getKey()) == null ? 0L : pidSubtaskId.get(entry.getKey()));
			eightTypesPoi.setQaStatus(2);
			eightTypesPoi.setWorker("NULL");
			eightTypesPoi.setQaWorker("NULL");
			eightTypesPoi.setReserved("NULL");
			eightTypesPoi.setTaskName("NULL");
			eightTypesPoi.setLogType(0);
			
			valExceptionId ++;
			list.add(eightTypesPoi);
		}
		
		return list;
	}
	
	private static List<EightTypesPoi> deleteResultSet2EightTypesPoi(Connection manConn, Map<Connection, List<Long>> connPidsMap, Map<Long, Map<String, Object>> deleteMap) throws Exception {
		Map<Long, Long> pidSubtaskId = new HashMap<>(); 
		for (Entry<Connection, List<Long>> entry : connPidsMap.entrySet()) {
			if(!CollectionUtils.isEmpty(entry.getValue())){
				Map<Long, Long> pidSubtaskIdOrQualitySubtaskId = querySubtaskId(entry.getKey(), entry.getValue());
				Map<Long, Long> regionPidSubtaskId = subtaskIdIsQuality(manConn, pidSubtaskIdOrQualitySubtaskId);
				pidSubtaskId.putAll(regionPidSubtaskId);
			}
		}
		
		List<EightTypesPoi> list = new ArrayList<>();
		for (Entry<Long, Map<String, Object>> entry : deleteMap.entrySet()) {
			EightTypesPoi eightTypesPoi = new EightTypesPoi();
			
			eightTypesPoi.setValExceptionId(valExceptionId);
			eightTypesPoi.setGroupId(0);
			eightTypesPoi.setLevel(0);
			eightTypesPoi.setRuleId(0);
			eightTypesPoi.setSituation("NULL");
			eightTypesPoi.setInformation((String) entry.getValue().get("information") == null ? "" : (String) entry.getValue().get("information"));
			eightTypesPoi.setSuggestion("NULL");
			eightTypesPoi.setLocation((String) entry.getValue().get("location") == null ? "" : (String) entry.getValue().get("location"));
			eightTypesPoi.setTargets((String) entry.getValue().get("targets") == null ? "" : (String) entry.getValue().get("targets"));
			eightTypesPoi.setAdditionInfo((Integer) entry.getValue().get("additionInfo") == null ? 0 : (Integer) entry.getValue().get("additionInfo"));
			eightTypesPoi.setScopeFlag(1);
			eightTypesPoi.setCreated(DateUtils.longToString(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss"));
			eightTypesPoi.setUpdated(DateUtils.longToString(System.currentTimeMillis(), "yyyy/MM/dd HH:mm:ss"));
			eightTypesPoi.setMeshId((String) entry.getValue().get("meshId") == null ? "" : (String) entry.getValue().get("meshId"));
			eightTypesPoi.setProvinceName("NULL");
			eightTypesPoi.setTaskId(pidSubtaskId.get(entry.getKey()) == null ? 0L : pidSubtaskId.get(entry.getKey()));
			eightTypesPoi.setQaStatus(2);
			eightTypesPoi.setWorker("NULL");
			eightTypesPoi.setQaWorker("NULL");
			eightTypesPoi.setReserved("NULL");
			eightTypesPoi.setTaskName("NULL");
			eightTypesPoi.setLogType(0);
			
			valExceptionId ++;
			list.add(eightTypesPoi);
		}
		
		return list;
	}
	
	/**
	 * 返回所有日大区库的连接
	 * @return
	 * @throws Exception
	 */
	private static Map<Integer, Connection> queryAllRegionConn(Connection manConn) throws Exception {
		Map<Integer,Connection> mapConn = new HashMap<Integer, Connection>();
		String sql = "SELECT T.DAILY_DB_ID,REGION_ID FROM REGION T";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = manConn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Connection regionConn = DBConnector.getInstance().getConnectionById(rs.getInt("DAILY_DB_ID"));
				mapConn.put(rs.getInt("REGION_ID"), regionConn);
			}
			return mapConn;

		} catch (Exception e) {
			for (Connection value : mapConn.values()) {
				DbUtils.rollbackAndCloseQuietly(value);
			}
			throw new Exception("加载REGION失败：" + e.getMessage(), e);
		}finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * map二合一（得到regionId 和 pid 的一对多的集合）
	 * @param adminCodeMap
	 * @return
	 */
	private static Map<Integer, List<Long>> getRegionAndPidsMap(Map<Long,String> adminCodeMap) {
		Map<Integer, List<Long>> regionIdAndPidMap = new HashMap<>();
		//key---adminCode     value---regionId
		for (Entry<String, Integer> metaEntry : manAdminCodeMap.entrySet()) {
			//key---pid      value---adminCode
			for (Entry<Long, String> entry : adminCodeMap.entrySet()) {
				if(metaEntry.getKey().equals(entry.getValue())){
					if(regionIdAndPidMap.containsKey(metaEntry.getValue())){
						regionIdAndPidMap.get(metaEntry.getValue()).add(entry.getKey());
					} else {
						List<Long> list = new ArrayList<>();
						list.add(entry.getKey());
						regionIdAndPidMap.put(metaEntry.getValue(), list);
					}
					
				}
			}
		}
		return regionIdAndPidMap;
	}
	
	/**
	 * 根据pid查询常规子任务（也有可能是质检子任务）
	 * @param regionConn
	 * @param pids
	 * @return
	 * @throws Exception
	 */
	private static Map<Long, Long> querySubtaskId(Connection regionConn, List<Long> pids) throws Exception {
		Map<Long, Long> pidSubtaskIdMap = new HashMap<Long, Long>();
		String sql = "SELECT PID, QUICK_SUBTASK_ID, MEDIUM_SUBTASK_ID FROM POI_EDIT_STATUS WHERE PID IN ";
		String pidsString = StringUtils.join(pids, ",");
		boolean clobFlag = false;
		if (pids.size() > 1000) {
			sql += " (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
			clobFlag = true;
		} else {
			sql += " (" + pidsString + ")";
		}
		
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		try {
			pstmt = regionConn.prepareStatement(sql);
			if (clobFlag) {
				Clob clob = ConnectionUtil.createClob(regionConn);
				clob.setString(1, pidsString);
				pstmt.setClob(1, clob);
			}
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				long subtaskId = 0L;
				long pid = resultSet.getLong("PID");
				long quickSubtaskId = resultSet.getLong("QUICK_SUBTASK_ID");
				long mediumSubtaskId = resultSet.getLong("MEDIUM_SUBTASK_ID");
				if (quickSubtaskId == 0L) {
					subtaskId = mediumSubtaskId;
				}
				if (mediumSubtaskId == 0L) {
					subtaskId = quickSubtaskId;
				}
				pidSubtaskIdMap.put(pid, subtaskId);
			}
			return pidSubtaskIdMap;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 得到pid和常规子任务的集合
	 * @param pidSubtaskId
	 * @return
	 * @throws Exception
	 */
	private static Map<Long, Long> subtaskIdIsQuality(Connection manConn, Map<Long, Long> pidSubtaskId) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			String sql = "SELECT SUBTASK_ID FROM SUBTASK WHERE QUALITY_SUBTASK_ID = ?";
			for(Entry<Long, Long> entry : pidSubtaskId.entrySet()){
				if(entry.getValue() != 0L){
					if(isQuality(manConn, entry.getValue())){
						Long subtaskId = run.query(manConn, sql, new ResultSetHandler<Long>() {
							@Override
							public Long handle(ResultSet rs) throws SQLException {
								if (rs.next()) {
									return rs.getLong(1);
								}
								return -1L;
							}
						}, entry.getValue());
						pidSubtaskId.put(entry.getKey(), subtaskId);
					}
				}
			}
			return pidSubtaskId;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}  
	
	/**
	 * 判断是否是质检子任务
	 * @param manConn
	 * @param subtaskId
	 * @return
	 * @throws Exception
	 */
	private static Boolean isQuality(Connection manConn, long subtaskId) throws Exception {
		try{
			QueryRunner run = new QueryRunner();
			String sql = "SELECT is_quality FROM subtask WHERE subtask_id = ?";
			return run.query(manConn, sql, new ResultSetHandler<Boolean>() {
				@Override
				public Boolean handle(ResultSet rs) throws SQLException {
					Boolean flag = false;
					if (rs.next()) {
						int isQuality = rs.getInt(1);
						if(isQuality == 1){
							flag = true;
						}
					}
					return flag;
				}
			}, subtaskId);
		}catch(Exception e){
			throw e;
		}
	}
	
	private static Map<String, Integer> queryMap(Connection conn ,String sql) throws Exception {
		try {
			QueryRunner run = new QueryRunner();
			return run.query(conn, sql, new ResultSetHandler<Map<String, Integer>>() {
					public Map<String, Integer> handle(ResultSet rs) throws SQLException {
						Map<String, Integer> map = new HashMap<>();
						while (rs.next()) {
							map.put(rs.getString(1), rs.getInt(2));
						}
						return map;
					}
				});
		} catch (Exception e) {
			throw e;
		}
	}

	public static void main(String[] args) throws Exception {
		initContext();
		log.info("args.length:" + args.length + ", 数组元素为：" + Arrays.toString(args));
		if (args == null || args.length != 2) {
			log.info("ERROR:need args:");
			return;
		}

		execute(args[0],args[1]);
		log.info("Over.");
		System.exit(0);
	}
	
	
	public static void initContext() {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				new String[] { "dubbo-app-scripts.xml", "dubbo-scripts.xml" });
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
}
