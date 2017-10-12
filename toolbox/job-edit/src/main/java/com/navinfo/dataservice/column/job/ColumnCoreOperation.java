package com.navinfo.dataservice.column.job;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.check.NiValExceptionSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ColumnCoreOperation {

	protected static Logger log = LoggerRepos.getLogger(ColumnCoreOperation.class);

	/**
	 * 实现逻辑 1) 取数据检查结果 2) 检查结果和poi_column_status里已记录的work_item_id做差分比较 3)
	 * 若同一一级项或者二级项下，①已记录了该检查项，则不做处理；②没记录则新增一条记录；③若检查结果没有该规则号，classifyRules中有，
	 * poi_column_status表中记录了该规则号，则从poi_column_status表删除该记录
	 * 
	 * @param mapParams
	 * @throws Exception
	 */
	@SuppressWarnings("rawtypes")
	public void runClassify(HashMap mapParams, Connection conn, int taskId,boolean isInitQcProblem,int isQuality) throws Exception {
		try {
			String[] strCkRules = ((String) mapParams.get("ckRules")).split(",");
			String[] strClassifyRules = ((String) mapParams.get("classifyRules")).split(",");
			int userId = (Integer) mapParams.get("userId");
			List pidList = (List) mapParams.get("pids"); // 每条数据需包含pid
			Map<Integer,JSONObject> qcFlag =(Map<Integer,JSONObject>) mapParams.get("qcFlag");
			String firstWorkItem =(String) mapParams.get("firstWorkItem");
			String secondWorkItem =(String) mapParams.get("secondWorkItem");
			List ckRules = new ArrayList();
			for(int i=0;i<strCkRules.length;i++){
				ckRules.add(strCkRules[i]);
			}
			List classifyRules = new ArrayList();
			for(int i=0;i<strClassifyRules.length;i++){
				classifyRules.add(strClassifyRules[i]);
			}
			List<Integer> qcPidList = new ArrayList<Integer>();
			for (int i = 0; i < pidList.size(); i++) {
				int pid = (Integer) pidList.get(i);
				// 根据数据取检查结果
				NiValExceptionSelector checkSelector = new NiValExceptionSelector(conn);
				List checkResultList = checkSelector.loadByPid(pid, ckRules);
				IxPoiColumnStatusSelector columnStatusSelector = new IxPoiColumnStatusSelector(conn);

				// 取poi_column_status中打标记结果
				List existClassifyList = columnStatusSelector.queryClassifyByPid(pid,classifyRules);
				
				int existQcFlag =0;
				int existComHandler = 0;
				if(qcFlag.containsKey(pid)){
					JSONObject data=qcFlag.get(pid);
					existQcFlag = data.getInt("qc_flag");
					existComHandler = data.getInt("common_handler");
				}
				if(existQcFlag==1){qcPidList.add(pid);}
				
				// poi_deep_status不存在的作业项插入,存在的更新
				checkResultList.retainAll(classifyRules);
				if (checkResultList.size()>0) {
					insertWorkItem(checkResultList, conn, pid, userId, taskId,existQcFlag,existComHandler,firstWorkItem);
				}

				// 重分类回退，本次要重分类classifyRules,检查结果中没有，若poi_deep_status存在,需从poi_deep_status中删掉
				existClassifyList.removeAll(checkResultList);
				deleteWorkItem(existClassifyList, conn, pid);
			}
			
			//质检保存、常规提交、质检提交时，isInitQcProblem为true,对重分类后的数据需要初始化problem表。
			//常规保存时重分类的qc_flag=1的数据，不需要初始化问题表，等常规提交的时候再初始化
			if(isInitQcProblem&&qcPidList!=null&&qcPidList.size()>0){
				Map<Integer,JSONArray> workItemInfo = getWorkItemInfo(qcPidList,userId,conn,taskId,isQuality);
				for(int pid:workItemInfo.keySet()){
					Iterator <JSONObject> it = workItemInfo.get(pid).iterator();
					while (it.hasNext()) {
						JSONObject data=it.next();
						List<Integer> dataList = new ArrayList<Integer>();
						dataList.add(pid);
						String first = data.getString("firstWorkItem");
						String second = data.getString("secondWorkItem");
						insertColumnQcProblems(dataList,conn,taskId,first,second,userId,true);
					}
				}
			}
			

		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * 往poi_column_status表插入作业标记信息
	 * 
	 * @param checkResultList
	 * @param conn
	 * @param pid
	 * @throws Exception
	 */
	public void insertWorkItem(List<String> checkResultList, Connection conn, int pid,int userId,int taskId,int existQcFlag,int existComHandler,String firstWorkItem) throws Exception {
		PreparedStatement pstmt = null;

		try {
			
			for (String workItem : checkResultList) {
				
				StringBuilder sb = new StringBuilder();
				sb.append("MERGE INTO poi_column_status T1 ");
				sb.append(" USING (SELECT '" + workItem+ "' as d," + " sysdate as e," + pid + " as f ,"
						+ " CASE WHEN  pc.first_work_item='"+firstWorkItem+"' THEN "+existQcFlag+" ELSE 0 END existQcFlag,"
						+ " CASE WHEN  pc.first_work_item='"+firstWorkItem+"' THEN "+existComHandler+" ELSE 0 END existComHandler,"
						+ " CASE WHEN  pc.first_work_item='"+firstWorkItem+"' THEN "+userId+" ELSE 0 END b,"
						+ " CASE WHEN  pc.first_work_item='"+firstWorkItem+"' THEN "+taskId+" ELSE 0 END C"
						+ " FROM poi_column_workitem_conf pc WHERE pc.type=1 "
						+ " AND pc.check_flag IN (1,3) AND pc.work_item_id='" + workItem+ "') T2 ");
				sb.append(" ON ( T1.pid=T2.f and T1.work_item_id = T2.d) ");
				sb.append(" WHEN MATCHED THEN ");
				sb.append(" UPDATE SET T1.handler = T2.b,T1.task_id= T2.c,T1.first_work_status = 1,T1.second_work_status = 1,T1.apply_date = T2.e,T1.qc_flag=T2.existQcFlag,T1.common_handler=T2.existComHandler ");
				sb.append(" WHEN NOT MATCHED THEN ");
				sb.append(" INSERT (T1.pid,T1.work_item_id,T1.first_work_status,T1.second_work_status,T1.handler,T1.task_id,T1.apply_date,T1.qc_flag,T1.common_handler) VALUES"
						+ " (T2.f,T2.d,1,1,T2.b,T2.c,T2.e,T2.existQcFlag,T2.existComHandler)");
				log.info(sb.toString());
				pstmt = conn.prepareStatement(sb.toString());
				pstmt.addBatch();
			}

			pstmt.executeBatch();
			pstmt.clearBatch();
		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("往poi_column_status表插入作业标记信息出错，原因：" + e.getMessage(), e);
		}
		finally {
			DBUtils.closeStatement(pstmt);
		}

	}
	/**
	 * 常规提交时，对打质检标记的数据，初始化质检问题表
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void insertColumnQcProblems(List<Integer> pidList,Connection conn,int comSubTaskId,String firstWorkItem,String secondWorkItem,int userId,boolean isClassify) throws Exception {
		
		if(pidList==null||pidList.size()==0){return;}
		String nameClass= "1";
		String nameType= "1";
		String langCode= " 'CHI','CHT'";
		if(secondWorkItem.equals("aliasName")){
			nameClass= "3";
		}else if(secondWorkItem.equals("shortName")){
			nameClass= "5";
		}else if(secondWorkItem.equals("namePinyin")){
			nameClass= "1,3,5";
		}else if(firstWorkItem.equals("poi_englishname")&&secondWorkItem.equals("officalStandardEngName")){
			nameType= "1";
			langCode= "'ENG'";
		}else if(firstWorkItem.equals("poi_englishname")){
			nameType= "2";
			langCode= "'ENG'";
		}else if(firstWorkItem.equals("poi_englishaddress")){
			langCode= "'ENG'";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("                  MERGE INTO");
		sb.append("                  COLUMN_QC_PROBLEM");
		sb.append("                  T");
		sb.append("                  USING (SELECT WK.PID, WK.WORK_ITEM_ID,'' IS_PROBLEM,");
		sb.append("                          CASE WHEN 'poi_name' = '"+firstWorkItem+"' THEN NM.NAMENEWVLAUE ");
		sb.append("                               WHEN 'poi_englishname' = '"+firstWorkItem+"' THEN NM.NAMENEWVLAUE ");
		sb.append("                               WHEN 'poi_address' = '"+firstWorkItem+"' THEN ADR.ADDRNEWVLAUE ");
		sb.append("                               WHEN 'poi_englishaddress' = '"+firstWorkItem+"' THEN ADR.ADDRNEWVLAUE ");
		sb.append("                          ELSE '' ");
		sb.append("                          END OLDVALUE, ");
		sb.append("                          CASE WHEN 'poi_name' = '"+firstWorkItem+"' THEN ORNM.name ");
		sb.append("                               WHEN 'poi_englishname' = '"+firstWorkItem+"' THEN ORNM.name ");
		sb.append("                               WHEN 'poi_address' = '"+firstWorkItem+"' THEN ORADR.fullname ");
		sb.append("                               WHEN 'poi_englishaddress' = '"+firstWorkItem+"' THEN ORADR.fullname ");
		sb.append("                          ELSE '' ");
		sb.append("                          END ORNAME ");
		sb.append("                    FROM (SELECT CASE");
		sb.append("                                   WHEN 'poi_englishname' = '"+firstWorkItem+"' THEN");
		sb.append("                                    LISTAGG(N.NAME_ID || ':' || N.NAME || ',' || F.FLAG_CODE,");
		sb.append("                                            '|') WITHIN GROUP(ORDER BY N.NAME_ID)");
		sb.append("                                   WHEN 'namePinyin' = '"+secondWorkItem+"' THEN");
		sb.append("                                    LISTAGG(N.NAME_ID || ':' || N.name_phonetic, ");
		sb.append("                                            '|') WITHIN GROUP(ORDER BY N.NAME_ID)");
		sb.append("                                   ELSE");
		sb.append("                                    LISTAGG(N.NAME_ID || ':' || N.NAME, '|') WITHIN");
		sb.append("                                    GROUP(ORDER BY N.NAME_ID)");
		sb.append("                                 END NAMENEWVLAUE,");
		sb.append("                                 N.POI_PID PID");
		sb.append("                            FROM IX_POI_NAME N, IX_POI_NAME_FLAG F");
		sb.append("                           WHERE N.POI_PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append("                             AND N.NAME_ID = F.NAME_ID(+)");
		sb.append("                             AND N.LANG_CODE IN ("+langCode+")");
		sb.append("                             AND N.NAME_TYPE IN ("+nameType+")");
		sb.append("                             AND N.NAME_CLASS IN ("+nameClass+")");
		sb.append("                           GROUP BY N.POI_PID) NM,");
		sb.append("                           (SELECT n.name,N.POI_PID FROM ix_poi_name n WHERE  n.LANG_CODE IN ('CHI','CHT') AND n.NAME_TYPE=2 AND n.NAME_CLASS=1 ");
		sb.append("                           AND n.poi_pid IN ("+StringUtils.join(pidList, ",")+" )) ORNM,");
		sb.append("                         (SELECT CASE");
		sb.append("                                   WHEN 'addrSplit' = '"+secondWorkItem+"' THEN");
		sb.append("                                    A.NAME_ID || ':' || A.PROVINCE ||'|'|| A.CITY ||'|'|| A.COUNTY ||'|'||");
		sb.append("                                    A.TOWN ||'|'|| A.PLACE ||'|'|| A.STREET ||'|'|| A.LANDMARK ||'|'|| A.PREFIX ||'|'||");
		sb.append("                                    A.HOUSENUM ||'|'|| A.TYPE ||'|'|| A.SUBNUM ||'|'|| A.SURFIX ||'|'|| A.ESTAB ||'|'||");
		sb.append("                                    A.BUILDING ||'|'|| A.FLOOR ||'|'|| A.UNIT ||'|'|| A.ROOM ||'|'|| A.ADDONS");
		sb.append("                                   WHEN 'addrPinyin' = '"+secondWorkItem+"' THEN");
		sb.append("                                    A.NAME_ID || ':' || A.PROV_PHONETIC ||'|'|| A.CITY_PHONETIC ||'|'||A.COUNTY_PHONETIC||'|'||");
		sb.append("                                    A.TOWN_PHONETIC ||'|'|| A.PLACE_PHONETIC ||'|'|| A.STREET_PHONETIC ||'|'||");
		sb.append("                                    A.LANDMARK_PHONETIC ||'|'|| A.PREFIX_PHONETIC ||'|'||");
		sb.append("                                    A.HOUSENUM_PHONETIC ||'|'|| A.TYPE_PHONETIC ||'|'|| A.SUBNUM_PHONETIC ||'|'||");
		sb.append("                                    A.SURFIX_PHONETIC ||'|'|| A.ESTAB_PHONETIC ||'|'||");
		sb.append("                                    A.BUILDING_PHONETIC ||'|'|| A.FLOOR_PHONETIC ||'|'|| A.UNIT_PHONETIC ||'|'||");
		sb.append("                                    A.ROOM_PHONETIC ||'|'|| A.ADDONS_PHONETIC");
		sb.append("                                   WHEN 'poi_englishaddress' = '"+firstWorkItem+"' THEN");
		sb.append("                                    A.NAME_ID || ':' || A.FULLNAME");
		sb.append("                                   ELSE");
		sb.append("                                    ''");
		sb.append("                                 END ADDRNEWVLAUE,");
		sb.append("                                 A.POI_PID PID");
		sb.append("                            FROM IX_POI_ADDRESS A");
		sb.append("                           WHERE A.POI_PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append("                             AND A.LANG_CODE IN ("+langCode+")) ADR,");
		sb.append("                           (SELECT n.fullname,N.POI_PID FROM ix_poi_address n WHERE  n.LANG_CODE IN ('CHI','CHT') ");
		sb.append("                           AND n.poi_pid IN ("+StringUtils.join(pidList, ",")+" )) ORADR,");
		sb.append("                         (SELECT '[' || LISTAGG(PS.WORK_ITEM_ID, ',') WITHIN GROUP(ORDER BY PS.PID) || ']' WORK_ITEM_ID,");
		sb.append("                                 PS.PID");
		sb.append("                            FROM POI_COLUMN_STATUS PS, POI_COLUMN_WORKITEM_CONF PC");
		sb.append("                           WHERE PS.WORK_ITEM_ID = PC.WORK_ITEM_ID");
		sb.append("                             AND PC.SECOND_WORK_ITEM = '"+secondWorkItem+"'");
		sb.append("                             AND PC.CHECK_FLAG IN (1, 3)");
		sb.append("                             AND PS.PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append("                           GROUP BY PS.PID) WK");
		sb.append("                   WHERE WK.PID = NM.PID(+)");
		sb.append("                     AND WK.PID = ADR.PID(+)");
		sb.append("                     AND WK.PID = ORNM.POI_PID(+)");
		sb.append("                     AND WK.PID = ORADR.POI_PID(+)) TP");
		sb.append("                  ON (T.PID=TP.pid AND T.SUBTASK_ID ="+comSubTaskId+" AND T.SECOND_WORK_ITEM = '"+secondWorkItem+"'  AND T.IS_VALID = 0  )");
		//常规提交初始化时，若已存在，则更新为空；质检重分类初始化时，若已存在且该数据的作业状态是待作业，则赋值为空(后面专门更新)，若已存在且该数据的作业状态是待提交，则保留原值
		if(!isClassify){
			sb.append("                  WHEN MATCHED THEN");
			sb.append("                    UPDATE SET T.IS_PROBLEM =TP.IS_PROBLEM,T.OLD_value=TP.OLDVALUE,T.worker="+userId+",T.NEW_VALUE='',T.work_item_id=TP.WORK_ITEM_ID");
		}
		sb.append("                  WHEN NOT MATCHED THEN ");
		sb.append("                  INSERT (ID,SUBTASK_ID,PID,FIRST_WORK_ITEM,SECOND_WORK_ITEM,WORK_ITEM_ID,OLD_VALUE,WORK_TIME,IS_VALID,WORKER,ORIGINAL_INFO)"
				+ " VALUES(COLUMN_QC_PROBLEM_seq.nextval,"+comSubTaskId+",TP.PID,'"+firstWorkItem+"','"+secondWorkItem+"',TP.WORK_ITEM_ID,TP.OLDVALUE,:1,0,"+userId+",TP.ORNAME)");
		
		
		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
			log.info(sb.toString());
			pstmt.executeUpdate();
			//若已存在且该数据的作业状态是待作业，则赋值为空
			updateProblemStatus(pidList,conn);
			
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 更新COLUMN_QC_PROBLEM
	 * 
	 * @param classifyRules
	 * @param conn
	 * @param pid
	 * @throws Exception
	 */
	public void updateProblemStatus(List<Integer> pidList, Connection conn) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("MERGE INTO COLUMN_QC_PROBLEM T ");
		sb.append(" USING (SELECT CP.ID ");
		sb.append("          FROM POI_COLUMN_STATUS        PS, ");
		sb.append("               POI_COLUMN_WORKITEM_CONF PC, ");
		sb.append("               COLUMN_QC_PROBLEM        CP ");
		sb.append("         WHERE PS.WORK_ITEM_ID = PC.WORK_ITEM_ID ");
		sb.append("           AND CP.PID = PS.PID ");
		sb.append("           AND CP.IS_VALID = 0 ");
		sb.append("           AND CP.IS_PROBLEM IS NOT NULL ");
		sb.append("           AND PC.CHECK_FLAG IN (1, 3) ");
		sb.append("           AND PS.SECOND_WORK_STATUS = 1 ");
		sb.append("           AND PC.FIRST_WORK_ITEM = CP.FIRST_WORK_ITEM ");
		sb.append("           AND PC.SECOND_WORK_ITEM = CP.SECOND_WORK_ITEM ");
		sb.append("           AND CP.PID IN ("+StringUtils.join(pidList, ",")+")) TP ");
		sb.append(" ON (T.ID = TP.ID AND T.IS_VALID = 0) ");
		sb.append(" WHEN MATCHED THEN ");
		sb.append("   UPDATE SET T.IS_PROBLEM = '' ");

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			log.info(sb.toString());
			pstmt.executeUpdate();

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new SQLException("更新COLUMN_QC_PROBLEM表信息出错，原因：" + e.getMessage(), e);
		}
		finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	
	/**
	 * 从poi_column_status表删除作业标记信息
	 * 
	 * @param classifyRules
	 * @param conn
	 * @param pid
	 * @throws Exception
	 */
	public void deleteWorkItem(List<String> classifyRules, Connection conn, int pid) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from poi_column_status where pid=? and work_item_id=? ");

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			for (String workItem : classifyRules) {
				pstmt.setInt(1, pid);
				pstmt.setString(2, workItem);
				pstmt.addBatch();
			}

			pstmt.executeBatch();
			pstmt.clearBatch();

		} catch (SQLException e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			log.error(e.getMessage(), e);
			throw new SQLException("从poi_column_status表删除作业标记信息出错，原因：" + e.getMessage(), e);
		}
		finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	/**
	 * 从没有地址的数据从poi_column_status表删除作业标记信息
	 * 
	 * @param classifyRules
	 * @param conn
	 * @param pid
	 * @throws Exception
	 */
	public void noAddrPoiDeleteWorkItem(String classifyRules, Connection conn, List<Integer> pidList) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from poi_column_status PS where PS.pid in ("+StringUtils.join(pidList, ",")+") and PS.work_item_id='"+classifyRules+"' and  NOT EXISTS (SELECT 1 FROM IX_POI_ADDRESS AD  WHERE PS.PID=AD.POI_PID ) ");

		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			log.info(sb.toString());
			pstmt.executeUpdate();

		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new SQLException("从poi_column_status表删除作业标记信息出错，原因：" + e.getMessage(), e);
		}
		finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	/**
	 * 从poi_column_status表获取重分类前该POI的qc_flag值
	 * 
	 * @param pidList
	 * @param userId
	 * @param conn
	 * @param comSubTaskId
	 * @throws Exception
	 */
	public Map<Integer,JSONObject> getColumnDataQcFlag(List<Integer> pidList,int userId,Connection conn,int comSubTaskId,int isQuality) throws Exception {
		Map<Integer,JSONObject> result = new HashMap<Integer,JSONObject>();
		if(pidList==null||pidList.size()==0){return result;}
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT ps.pid,ps.qc_flag,ps.common_handler FROM POI_COLUMN_STATUS PS ");
		sql.append(" WHERE PS.PID IN ("+StringUtils.join(pidList, ",")+") ");
		sql.append("	AND PS.HANDLER in (0, "+userId+") ");
		sql.append("	AND PS.work_item_id not in ('FM-DETAIL','FM-CARRENTAL','FM-PARKING') ");
		if(isQuality==0){
			sql.append("	AND PS.COMMON_HANDLER = "+userId);
		}else if(isQuality==1){
			sql.append("	AND PS.COMMON_HANDLER <> "+userId);
		}
		sql.append("	AND PS.task_id = "+comSubTaskId);

		try {
			pstmt = conn.prepareStatement(sql.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				JSONObject data=new JSONObject();
				data.put("qc_flag", resultSet.getInt("qc_flag"));
				data.put("common_handler", resultSet.getInt("common_handler"));
				result.put(resultSet.getInt("pid"),data);
			} 
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return result;
	}
	
	/**
	 * 从poi_column_status表获取重分类后的标记信息
	 * 
	 * @param pidList
	 * @param userId
	 * @param conn
	 * @param comSubTaskId
	 * @throws Exception
	 */
	public Map<Integer,JSONArray> getWorkItemInfo(List<Integer> pidList,int userId,Connection conn,int comSubTaskId,int isQuality) throws Exception {
		Map<Integer,JSONArray> result = new HashMap<Integer,JSONArray>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT ps.pid,pw.first_work_item,pw.second_work_item FROM poi_column_status ps,poi_column_workitem_conf pw ");
		sql.append(" WHERE ps.work_item_id=pw.work_item_id ");
		sql.append(" AND PS.PID IN ("+StringUtils.join(pidList, ",")+") ");
		sql.append("	AND PS.HANDLER = "+userId);
		if(isQuality==0){
			sql.append("	AND PS.COMMON_HANDLER = "+userId);
		}else if(isQuality==1){
			sql.append("	AND PS.COMMON_HANDLER <> "+userId);
		}
		sql.append("	AND PS.task_id = "+comSubTaskId);

		try {
			pstmt = conn.prepareStatement(sql.toString());
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				int pid = resultSet.getInt("pid");
				JSONArray datas = new JSONArray();
				JSONObject data=new JSONObject();
				data.put("firstWorkItem", resultSet.getString("first_work_item"));
				data.put("secondWorkItem", resultSet.getString("second_work_item"));
				if(result.containsKey(pid)){
					datas =result.get(pid);
					datas.add(data);
				}else{
					datas.add(data);
				}
				result.put(pid,datas);
				
			} 
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeResultSet(resultSet);
			DBUtils.closeStatement(pstmt);
		}
		return result;
	}

	public static void main(String[] args) throws Exception {

		/**
		 * Connection conn = DriverManager.getConnection(
		 * "jdbc:oracle:thin:@192.168.4.61:1521/orcl", "fm_regiondb_sp6_m_1",
		 * "fm_regiondb_sp6_m_1");
		 * 
		 * ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
		 * HashMap<String, Object> classifyMap = new HashMap<String, Object>();
		 * classifyMap.put("ckRules", "FM-A04-04,FM-A09-01");
		 * classifyMap.put("classifyRules", "FM-A04-04"); List pidList = new
		 * ArrayList(); pidList.add(123); classifyMap.put("pids", pidList);
		 * columnCoreOperation.runClassify(classifyMap, conn);
		 **/

	}

}
