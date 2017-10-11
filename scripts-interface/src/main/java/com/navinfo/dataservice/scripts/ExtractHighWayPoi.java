package com.navinfo.dataservice.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
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
import com.navinfo.dataservice.dao.plus.selector.custom.IxPoiSelector;
import com.navinfo.dataservice.scripts.model.HighWayPoi;
import com.navinfo.navicommons.database.QueryRunner;
import com.navinfo.navicommons.exception.ServiceException;

import net.sf.json.JSONObject;
import oracle.spatial.geometry.JGeometry;
import oracle.spatial.util.WKT;
import oracle.sql.STRUCT;

/**
 * 提取内业作业HW与高速出入口变化的POI
 * @Title:ExtractHighWayPoi
 * @Package:com.navinfo.dataservice.scripts
 * @Description: 
 * 	提取数据背景及目的：内业作业HW与高速出入口是参考提取变化的POI
	提取总原则：同时满足(1)、（2）、（3）条件提取子POI数据，生成Excel文件；
	(1)父POI分类是高速服务区（230206）或高速停车区（230207），且子为叶子子（多级父子关系最下面的子）；且子的状态为删除或修改（仅改分类）或新增；
	(2) 且子分类为230215或210215或以110、120、130开头的分类对应的POI；
	(3)若子POI为修改且改分类，则分类修改前是（2）对应的分类或修改后是(2)的分类；
 * @author:Jarvis 
 * @date: 2017年9月13日
 */
public class ExtractHighWayPoi {
	private static Logger log = LoggerRepos.getLogger(ExtractHighWayPoi.class);
	private static Map<Connection,List<Long>> connPidListMap = new HashMap<>();
	private static Map<String,Map<String,String>> adminCodeMap = new HashMap<>();
	private static Map<String,Integer> meshActionMap = new HashMap<>();
	private static Map<Integer, Connection> regionConMap = new HashMap<Integer, Connection>();
	private static Map<Long,HighWayPoi> pidHighWayPoiMap = new HashMap<>();
	private static Map<Long, String> pidSubtaskName = new HashMap<>(); 
	
	public static void execute(String startDate,String endDate) throws Exception {
		
		Connection monthConn = null;
		
		try {
			
			monthConn = DBConnector.getInstance().getMkConnection();
			
			StringBuilder sb = new StringBuilder();
			 
			sb.append(" LO.OP_ID = LD.OP_ID AND LD.OB_PID = p.pid AND LO.OP_DT > TO_DATE('"+startDate+"000000', 'yyyymmddhh24miss')");
			sb.append(" AND LO.OP_DT <= TO_DATE('"+endDate+"235959', 'yyyymmddhh24miss') AND LD.OB_NM  = 'IX_POI' AND TB_NM = 'IX_POI'");
			sb.append(" AND LD.OB_PID IN (SELECT CHILD_POI_PID FROM IX_POI_CHILDREN ) ");
			sb.append(" AND LD.OB_PID NOT IN (SELECT PARENT_POI_PID FROM IX_POI_PARENT)  AND( p.kind_code = '230215' OR ");
			sb.append(" p.kind_code = '210215' OR p.kind_code LIKE '110%' OR  p.kind_code LIKE '120%' OR  p.kind_code LIKE '130%') ");
  
			String condition = sb.toString();
			
			adminCodeMap = getAdminCodeAndProvince();//得到adminCode，省份，region对应的map
			meshActionMap = queryAction();//得到mesh和Action关系
			
			String excelName = "extract_highway_poi_list_"+ DateUtils.dateToString(new Date(), "yyyyMMddHHmmss");
			
			List<HighWayPoi> poiList = new ArrayList<>(); 
			
			Set<Long> insertPidList = searchInsertPidExtractHighWayPoi(condition,monthConn);//新增履历,叶子节点pidList（月库）
			Map<Long,Map<String,String>> updatePidMap = searchUpdatePidExtractHighWayPoi(condition,monthConn);//修改履历,叶子节点pidList（月库）
			
			Set<Long> updatePidList = new HashSet<>();
			for(Map.Entry<Long, Map<String, String>> map: updatePidMap.entrySet()) {
				long pid = map.getKey();
				Map<String, String> valueMap = map.getValue();
				String oldValue = JSONObject.fromObject(valueMap.get("old")).containsKey("KIND_CODE")?JSONObject.fromObject(valueMap.get("old")).getString("KIND_CODE"):"";
				String newValue = JSONObject.fromObject(valueMap.get("new")).containsKey("KIND_CODE")?JSONObject.fromObject(valueMap.get("new")).getString("KIND_CODE"):"";
				if(suitKindCodeCondition(oldValue)||suitKindCodeCondition(newValue)){
					updatePidList.add(pid);
				}
			}
			
			regionConMap = queryAllRegionConn();
			for (Connection regionConn : regionConMap.values()) {
				Set<Long> deletePidList = searchDeletePidExtractHighWayPoi(condition, regionConn);//删除履历，日落月成功,叶子节点HighWayPoiList（日库）
				updatePidList.removeAll(deletePidList);//如果一条pid既有修改履历也有删除履历，需在修改履历的pidList和删除履历pidList求差集，以删除为准。
				insertPidList.removeAll(deletePidList);//如果一条pid既有新增履历也有删除履历，需在新增履历的pidList和删除履历pidList求差集，以删除为准。
				if(!CollectionUtils.isEmpty(deletePidList)){
					filterChildPidListByParent(deletePidList,2, regionConn);//筛选出符合条件修改履历的子pidList
					convertPidListToHighWayList(deletePidList, 2, regionConn);//组装成HighWayList
				}
			}
			
			updatePidList.removeAll(insertPidList);//如果一条pid既有新增履历也有修改履历，需在修改履历的pidList和新增履历pidList求差集，以新增为准。
			
			
			if(!CollectionUtils.isEmpty(updatePidList)){
				filterChildPidListByParent(updatePidList,3, monthConn);//筛选出符合条件修改履历的子pidList
				convertPidListToHighWayList(updatePidList, 3, monthConn);//组装成HighWayList
			}

			
			if(!CollectionUtils.isEmpty(insertPidList)){
				filterChildPidListByParent(insertPidList,1, monthConn);//筛选出符合条件新增履历的子pidList
				convertPidListToHighWayList(insertPidList, 1, monthConn);//组装成HighWayList
			}
			
			
			for (Connection regionConn : regionConMap.values()) {
				List<Long> pidList = connPidListMap.get(regionConn); 
				if(!CollectionUtils.isEmpty(pidList)){
					Map<Long, Long> pidSubtaskIdOrQualitySubtaskId = querySubtaskId(regionConn, pidList);
					Map<Long, String> regionPidSubtaskId = subtaskIdIsQuality(pidSubtaskIdOrQualitySubtaskId);
					pidSubtaskName.putAll(regionPidSubtaskId);
				}
			}
			
			for (Entry<Long, HighWayPoi> pidHighWayPoi : pidHighWayPoiMap.entrySet()) {
				Long pid = pidHighWayPoi.getKey();
				HighWayPoi highWayPoi= pidHighWayPoi.getValue();
				if(pidSubtaskName.containsKey(pid)){
					highWayPoi.setTaskName(pidSubtaskName.get(pid));
				}
				poiList.add(highWayPoi);
			}
			
			log.info("poiList---------------"+pidHighWayPoiMap.keySet());
			
			ExportExcel<HighWayPoi> ex = new ExportExcel<HighWayPoi>();

			String[] headers = { "省份", "任务名称", "子PID", "图幅", "坐标", "数据状态","批次"};

			try {
				String path = SystemConfigFactory.getSystemConfig().getValue(
						PropConstant.downloadFilePathPoi)+"/extractPoi";
//				String path  = "D://";
				File file = new File(path+"/" + excelName + ".xls");
				if(!file.getParentFile().isDirectory()){
					file.getParentFile().mkdirs();
				}
				if(!file.exists()){
					file.createNewFile();
				}
				OutputStream out = new FileOutputStream(file);
				ex.exportExcel("内业作业HW与高速出入口变化记录表",headers, poiList, out, "yyyy-MM-dd HH:mm:ss");
				out.close();

				log.info("excel导出成功！");
			} catch (Exception e) {
				log.error(e.getMessage());
				throw e;
			}


		} catch (Exception e) {
			DbUtils.rollback(monthConn);
			for (Connection value : regionConMap.values()) {
				DbUtils.rollbackAndCloseQuietly(value);
			}
			log.error("提取发生异常",e);
			throw e;
		} finally {
			DbUtils.commitAndCloseQuietly(monthConn);
			for (Connection value : regionConMap.values()) {  
				DbUtils.commitAndCloseQuietly(value);
			}  
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
	
	
	/**
	 * 得到pid和常规子任务的集合
	 * @param pidSubtaskId
	 * @return
	 * @throws Exception
	 */
	private static Map<Long, String> subtaskIdIsQuality(Map<Long, Long> pidSubtaskId) throws Exception {
		Connection manConn = null;
		
		try {
			QueryRunner run = new QueryRunner();
			manConn = DBConnector.getInstance().getManConnection();
			Map<Long, String> map = new HashMap<>();
			String subtaskName = null;
			for(Entry<Long, Long> entry : pidSubtaskId.entrySet()){
				String sql = "SELECT NAME FROM SUBTASK WHERE SUBTASK_ID = ?";
				if(entry.getValue()!=0){
					if(isQuality(manConn, entry.getValue())){
						sql = "SELECT NAME FROM SUBTASK WHERE QUALITY_SUBTASK_ID = ?";
					}
					subtaskName = run.query(manConn, sql, new ResultSetHandler<String>() {
						@Override
						public String handle(ResultSet rs) throws SQLException {
							if (rs.next()) {
								return rs.getString(1);
							}
							return null;
							
						}
					}, entry.getValue());
				}
				map.put(entry.getKey(), StringUtils.isBlank(subtaskName)||entry.getValue()==0?"":subtaskName);
			}
			return map;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			DbUtils.closeQuietly(manConn);
		}
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
	 * 返回action，mesh map
	 * @return
	 * @throws Exception
	 */
	private static Map<String, Integer> queryAction() throws Exception {
		Connection conn = null;
		String sql  = "SELECT DISTINCT T.MESH, T.ACTION FROM SC_PARTITION_MESHLIST T";
		try {
			conn = DBConnector.getInstance().getMetaConnection();
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
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	
	/**
	 * 得到adminCode和Province的关系
	 * @return
	 * @throws ServiceException 
	 */
	private static Map<String,Map<String, String>> getAdminCodeAndProvince() throws ServiceException {
		Connection conn = null;
		try {
			QueryRunner run = new QueryRunner();
			conn = DBConnector.getInstance().getManConnection();
			String selectSql = "select DISTINCT (Substr(admincode,0,2)) admincode,province,region_id from cp_region_province t ORDER BY admincode";
			return run.query(conn, selectSql, new ResultSetHandler<Map<String, Map<String, String>>>(){

				@Override
				public Map<String, Map<String, String>>  handle(ResultSet rs) throws SQLException {
					Map<String, Map<String, String>> map  = new HashMap<>();
					while(rs.next()){
						Map<String, String> adminMap =  new HashMap<>();
						adminMap.put("province", rs.getString("province"));
						adminMap.put("region_id", rs.getString("region_id"));
						map.put(rs.getString("admincode"),adminMap);
					}
					return map;
				}});
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new ServiceException("查询列表失败，原因为:" + e.getMessage(), e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	/**
	 * 转换PidList至HighWayList bean
	 * @param poiList
	 * @param pidList
	 * @param type 1 新增，2 删除，3修改
	 * @return
	 * @throws Exception 
	 */
	public static void convertPidListToHighWayList(Set<Long> pidList,int type,Connection conn) throws Exception{
		if(!CollectionUtils.isEmpty(pidList)){
			String sql = "SELECT P.PID,P.MESH_ID,Substr(ad.admin_id,0,2),p.GEOMETRY FROM IX_POI p,AD_ADMIN AD WHERE p.region_id = ad.region_id AND"
					+ " p.PID IN ";
			String pidsString = StringUtils.join(pidList, ",");
			boolean clobFlag = false;
			if (pidList.size() > 1000) {
				sql += " (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				clobFlag = true;
			} else {
				sql += " (" + pidsString + ")";
			}
			String state = null;
			if(type==1){
				state = "新增";
			}else if(type==2){
				state = "删除";
			}else if(type==3){
				state = "修改";
			}
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				pstmt = conn.prepareStatement(sql);
				if (clobFlag) {
					Clob clob = ConnectionUtil.createClob(conn);
					clob.setString(1, pidsString);
					pstmt.setClob(1, clob);
				}
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					HighWayPoi highWayPoi = new HighWayPoi();
					Long pid = rs.getLong(1);
					highWayPoi.setPid(rs.getInt(1));
					int meshId = rs.getInt(2);
					highWayPoi.setMeshId(meshId);
					String adminCode = rs.getString(3);
					Map<String,String> adminCodeValueMap  = adminCodeMap.get(adminCode);
					String province = adminCodeValueMap.containsKey("province")?adminCodeValueMap.get("province"):"";
					Integer regionId = adminCodeValueMap.containsKey("region_id")?Integer.parseInt(adminCodeValueMap.get("region_id")):0;
					Connection regionConn = regionConMap.get(regionId);
					List<Long> connPidList = new ArrayList<>();
					if(!connPidListMap.containsKey(regionConn)){
						connPidList = new ArrayList<>();
						connPidList.add(pid);
						connPidListMap.put(regionConn,connPidList);
					}else{
						connPidList = connPidListMap.get(regionConn);
						if(!connPidList.contains(pid)){
							connPidList.add(pid);
						}
					}
					
					highWayPoi.setProvince(province);
					highWayPoi.setState(state);
					JGeometry geom = JGeometry.load((STRUCT) rs.getObject("GEOMETRY"));
					String wkt = new String(new WKT().fromJGeometry(geom));
					highWayPoi.setPoint(wkt);
					highWayPoi.setAction(meshActionMap.containsKey(meshId+"")?meshActionMap.get(meshId+""):0);
					
					pidHighWayPoiMap.put(pid, highWayPoi);
				}
				
				
			} catch (Exception e) {
				log.error(e.getMessage());
				throw e;
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(pstmt);
			}
		}
	}
	
	
	
	/**
	 * 取得一级父pid，并判断父POI分类是高速服务区（230206）或高速停车区（230207），筛选出符合条件的子pidList
	 * @param childPid
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	public static Set<Long> filterChildPidListByParent(Set<Long> childPidList,int type,Connection conn) throws Exception{
		Set<Long> tempChildPidList = new HashSet<>();
		tempChildPidList.addAll(childPidList);
		Map<Long, Long> parentMap = new HashMap<>();
		if(type==2){
			parentMap = IxPoiSelector.getAllNoFilterDeleteParentPidsByChildrenPids(conn, tempChildPidList);//通过子poi查询一级父， 不过滤删除
		}else{
			parentMap = IxPoiSelector.getAllParentPidsByChildrenPids(conn, tempChildPidList);//通过子poi查询一级父， 过滤删除
		}
		Map<Long, Long> parentChildrenMap = new HashMap<>();
		Iterator<Long> iter = childPidList.iterator();
		while (iter.hasNext()) {
			Long childPid = iter.next();
			Long parentPid=parentMap.get(childPid);
			while(parentMap.containsKey(parentPid)){
				parentPid=parentMap.get(parentPid);
			}
			if(parentPid==null){
				iter.remove();
				continue;
			}
			parentChildrenMap.put(parentPid, childPid);
		}
		
		
		if(parentChildrenMap.keySet().size()>0){
			String sql = "SELECT PID,KIND_CODE FROM IX_POI WHERE PID IN ";
			String pidsString = StringUtils.join(parentChildrenMap.keySet().toArray(), ",");
			boolean clobFlag = false;
			if (parentChildrenMap.keySet().toArray().length > 1000) {
				sql += " (SELECT TO_NUMBER(COLUMN_VALUE) FROM TABLE(CLOB_TO_TABLE(?)))";
				clobFlag = true;
			} else {
				sql += " (" + pidsString + ")";
			}
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try {
				pstmt = conn.prepareStatement(sql);
				if (clobFlag) {
					Clob clob = ConnectionUtil.createClob(conn);
					clob.setString(1, pidsString);
					pstmt.setClob(1, clob);
				}
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					long pid = rs.getLong(1);
					String kindCode = rs.getString(2);
					if(!kindCode.equals("230206")&&!kindCode.equals("230207")){
						childPidList.remove(parentChildrenMap.get(pid));
					}
				}
				
				return childPidList;
				
			} catch (Exception e) {
				log.error(e.getMessage());
				throw e;
			} finally {
				DbUtils.closeQuietly(rs);
				DbUtils.closeQuietly(pstmt);
			}
		}
		return null;
	}
	
	
	/**
	 * 符合改分类条件
	 * @param str
	 * @return
	 */
	public static boolean suitKindCodeCondition(String str){
		if(str.equals("230215")||str.equals("210215")||str.startsWith("110")||
				str.startsWith("120")||str.startsWith("130")){
			return true;
		}
		return false;
	}


	/**
	 * 查询新增履历的高速变化pid(月库)
	 * @param condition
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private static Set<Long> searchInsertPidExtractHighWayPoi(String condition,
			Connection conn) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT(LD.OB_PID) FROM LOG_OPERATION LO, LOG_DETAIL LD, ix_poi p WHERE ");
		sb.append(condition+" AND LD.OP_TP = 1");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			rs = pstmt.executeQuery();
			
			Set<Long> pidList = new HashSet<>();
			while (rs.next()) {
				pidList.add(rs.getLong(1));
			}
			
			return pidList;
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}

	/**
	 * 查询修改履历的高速变化pid(月库)
	 * @param condition
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private static Map<Long,Map<String,String>> searchUpdatePidExtractHighWayPoi(String condition,
			Connection conn) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT LD.OB_PID,LD.OLD,LD.NEW FROM LOG_OPERATION LO, LOG_DETAIL LD, ix_poi p WHERE ");
		sb.append(condition +" AND LD.FD_LST LIKE '%KIND_CODE%'");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			rs = pstmt.executeQuery();
			
			Map<Long,Map<String,String>> map = new HashMap<>();
			Map<String,String> valueMap = null;
			long pid = 0L;
			while (rs.next()) {
				
				if(pid!=rs.getLong(1)){
					pid = rs.getLong(1);
					valueMap = new HashMap<>();
					valueMap.put("old", rs.getString(2));
					map.put(pid, valueMap);
				}
				valueMap.put("new", rs.getString(3));
			}
			
			return map;
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	
	/**
	 * 查询删除履历且日落月成功的高速变化pid(日库)
	 * @param condition
	 * @param conn
	 * @return
	 * @throws Exception
	 */
	private static Set<Long> searchDeletePidExtractHighWayPoi(String condition,
			Connection conn) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT DISTINCT(LD.OB_PID) FROM LOG_OPERATION LO, LOG_DETAIL LD, ix_poi p WHERE ");
		sb.append(condition+" AND LD.OP_TP = 2 AND LO.COM_STA = 1");
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = conn.prepareStatement(sb.toString());
			
			rs = pstmt.executeQuery();
			
			Set<Long> pidList = new HashSet<>();
			while (rs.next()) {
				pidList.add(rs.getLong(1));
			}
			
			return pidList;
			
		} catch (Exception e) {
			log.error(e.getMessage());
			throw e;
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(pstmt);
		}
	}



	/**
	 * 查询全部region，并返回regionId和Conn的map
	 * @return
	 * @throws SQLException
	 */
	public static Map<Integer,Connection> queryAllRegionConn() throws SQLException {
		log.info("queryAllRegionConn start...");
		Map<Integer,Connection> mapConn = new HashMap<Integer, Connection>();
		String sql = "select t.daily_db_id,region_id from region t order by t.daily_db_id";
		log.info("sql:"+sql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection conn = null;
		try {
				conn = DBConnector.getInstance().getManConnection();
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					Connection regionConn = DBConnector.getInstance().getConnectionById(rs.getInt("daily_db_id"));
					mapConn.put(rs.getInt("region_id"), regionConn);
					log.info("大区库region_id:"+rs.getInt("region_id")+"获取数据库连接成功");
				}
				log.info("queryAllRegionConn end...");
				return mapConn;

		} catch (Exception e) {
			for (Connection value : mapConn.values()) {
				DbUtils.rollbackAndCloseQuietly(value);
			}
			throw new SQLException("加载region失败：" + e.getMessage(), e);
		}finally {
			DbUtils.closeQuietly(conn, pstmt, rs);
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
