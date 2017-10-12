package com.navinfo.dataservice.column.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiColumnOpConf;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiOpConfSelector;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.model.ixpoi.IxPoi;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.IxPoiObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import com.navinfo.navicommons.database.sql.DBUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ColumnSaveJob extends AbstractJob {
	
	public ColumnSaveJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@SuppressWarnings("static-access")
	@Override
	public void execute() throws JobException {
		
		log.info("columnSave start...");
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<Integer> pidList = new ArrayList<Integer>();
		List<Long> pidListL = new ArrayList<Long>();
		
		Connection conn = null;
		try {
			ColumnSaveJobRequest columnSaveJobRequest = (ColumnSaveJobRequest) this.request;
			JSONObject paramJson = columnSaveJobRequest.getParam();
			int userId = columnSaveJobRequest.getUserId();
			int taskId = paramJson.getInt("taskId");
			JSONArray data = paramJson.getJSONArray("dataList");
			String secondWorkItem = paramJson.getString("secondWorkItem");
			String firstWorkItem = paramJson.getString("firstWorkItem");
			
			log.info("userId:"+userId+",taskId:"+taskId);
			log.info("secondWorkItem:"+secondWorkItem);
			
			
			int comSubTaskId=0;
			int isQuality=0;
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			isQuality=subtask.getIsQuality();
			if(isQuality==1){
				Subtask comSubtask = apiService.queryBySubTaskIdAndIsQuality(taskId, "2", 1);
				comSubTaskId=comSubtask.getSubtaskId();
				log.info("月编质检保存");
			}else{
				comSubTaskId=taskId;
				log.info("月编常规保存");
			}
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			log.info("dbId:"+dbId);
			
			DefaultObjImportor importor = new DefaultObjImportor(conn,null);
			EditJson editJson = new EditJson();
			editJson.addJsonPoi(data);
			DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
			importor.setPhysiDelete(true);
			importor.operate(command);
			importor.persistChangeLog(OperationSegment.SG_COLUMN, userId);
			
			for (int i=0;i<data.size();i++) {
				int pid = data.getJSONObject(i).getInt("objId");
				pidList.add(pid);
				pidListL.add(data.getJSONObject(i).getLong("objId"));
			}
			
			
			// 修改poi_column_status表作业项状态
			updateColumnStatus(pidList, conn, 2,secondWorkItem);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			// 查询检查、批处理和重分类配置
			IxPoiOpConfSelector ixPoiOpConfSelector = new IxPoiOpConfSelector(conn);
			PoiColumnOpConf columnOpConf = ixPoiOpConfSelector.getDeepOpConf("",secondWorkItem, type);
			
			DeepCoreControl deepControl = new DeepCoreControl();
			OperationResult operationResult = new OperationResult();
			
			PoiLogDetailStat logDetail = new PoiLogDetailStat();
			ObjHisLogParser logParser = new ObjHisLogParser();
			Map<Long,List<LogDetail>> submitLogs = logDetail.loadAllRowLog(conn, pidListL);
			List<BasicObj> objList = importor.getResult().getAllObjs();
			List<BasicObj>  newObjList = new ArrayList<BasicObj>();
			for (BasicObj obj:objList) {
				IxPoiObj poiObj=(IxPoiObj) obj;
				IxPoi poi = (IxPoi) poiObj.getMainrow();
				long pid = poi.getPid();
				if (submitLogs.containsKey(pid)) {
					logParser.parse(obj, submitLogs.get(pid));
				}
				newObjList.add(obj);
			}
			operationResult.putAll(newObjList);
			
			// 批处理
			log.info("执行批处理");
			if (columnOpConf.getSaveExebatch() == 1) {
				BatchCommand batchCommand=new BatchCommand();	
				if (columnOpConf.getSaveBatchrules() != null) {
					for (String ruleId:columnOpConf.getSaveBatchrules().split(",")) {
						batchCommand.setRuleId(ruleId);
					}

					Batch batch=new Batch(conn,operationResult);
					batch.operate(batchCommand);
					batch.setPhysiDelete(true);
					batch.persistChangeLog(OperationSegment.SG_COLUMN, userId);
				}
			}
			boolean isInitQcProblem=false;
			//更新质检问题表
			if(isQuality==1){
				updateColumnQcProblems(pidList,conn,comSubTaskId,firstWorkItem,secondWorkItem,userId);
				if(secondWorkItem.equals("namePinyin")){
					updatePYIsProblem(pidList,conn,comSubTaskId);
				}
				isInitQcProblem=true;
			}

			// 检查
			log.info("执行检查");
			if (columnOpConf.getSaveExecheck() == 1) {
				CheckCommand checkCommand=new CheckCommand();		
				List<String> checkList=new ArrayList<String>();
				if (columnOpConf.getSaveCkrules() != null) {
					for (String ckRule:columnOpConf.getSaveCkrules().split(",")) {
						checkList.add(ckRule);
					}
					
					// 清理检查结果
					log.info("清理检查结果");
					deepControl.cleanExByCkRule(dbId, pidList, checkList, "IX_POI");
					
					checkCommand.setRuleIdList(checkList);
					
					Check check=new Check(conn,operationResult);
					check.operate(checkCommand);
				}
			}
			
			
			// 重分类
			log.info("执行重分类");
			if (columnOpConf.getSaveExeclassify()==1) {
				if (columnOpConf.getSaveCkrules() != null && columnOpConf.getSaveClassifyrules() != null) {
					
					HashMap<String,Object> classifyMap = new HashMap<String,Object>();
					classifyMap.put("userId", userId);
					classifyMap.put("ckRules", columnOpConf.getSaveCkrules());
					classifyMap.put("classifyRules", columnOpConf.getSaveClassifyrules());
					classifyMap.put("firstWorkItem", firstWorkItem);
					classifyMap.put("secondWorkItem", secondWorkItem);
					classifyMap.put("pids", pidList);
					ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
					//增加质检功能后，需要维护重分类后的质检标记
					Map<Integer,JSONObject> qcFlag = columnCoreOperation.getColumnDataQcFlag(pidList,userId,conn,comSubTaskId,isQuality);
					classifyMap.put("qcFlag", qcFlag);
					columnCoreOperation.runClassify(classifyMap,conn,comSubTaskId,isInitQcProblem,isQuality);
				}
			}

			// 清理重分类检查结果
			log.info("清理重分类检查结果");
			List<String> ckRules = new ArrayList<String>();
			String classifyrules = columnOpConf.getSaveClassifyrules();
			if (classifyrules != null) {
				for (String classifyrule:classifyrules.split(",")) {
					ckRules.add(classifyrule);
				}
				deepControl.cleanExByCkRule(dbId, pidList, ckRules, "IX_POI");
			}
			
			log.info("月编保存完成");
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 保存精编数据
	 * @param dbId
	 * @param data
	 * @throws Exception
	 */
//	public void columnSave(JSONArray data,Connection conn) throws Exception {
//		try {
//			for (int i=0;i<data.size();i++) {
//				EditApiImpl apiEdit = new EditApiImpl(conn);
//				apiEdit.runPoi(data.getJSONObject(i));
//			}
//			
//		} catch (Exception e) {
//			throw e;
//		}
//	}
			
	/**
	 * 更新配置表状态
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updateColumnStatus(List<Integer> pidList,Connection conn,int status,String second) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_column_status SET first_work_status="+status+",second_work_status="+status+" WHERE  work_item_id IN (SELECT cf.work_item_id FROM POI_COLUMN_WORKITEM_CONF cf WHERE cf.second_work_item='"+second+"') AND  pid in (select to_number(column_value) from table(clob_to_table(?)))");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			
			Clob pidsClob = ConnectionUtil.createClob(conn);
			
			pidsClob.setString(1, StringUtils.join(pidList, ","));
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setClob(1, pidsClob);
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}
	
	/**
	 * 更新质检问题表
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updateColumnQcProblems(List<Integer> pidList,Connection conn,int comSubTaskId,String firstWorkItem,String secondWorkItem,int userId) throws Exception {
		
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
		sb.append("                  COLUMN_QC_PROBLEM T");
		sb.append("                  USING (SELECT CASE WHEN 'namePinyin' = '"+secondWorkItem+"' THEN CP.IS_PROBLEM ");	
		sb.append("                                     WHEN NVL(CP.OLD_VALUE,'KONG') = NVL(tab.NEWVLAUE,'KONG') THEN '0' ");
		sb.append("                                     WHEN NVL(CP.OLD_VALUE,'KONG') <> NVL(tab.NEWVLAUE,'KONG') AND CP.IS_PROBLEM IN ('1','2') THEN CP.IS_PROBLEM ");
		sb.append("                                     WHEN NVL(CP.OLD_VALUE,'KONG') <> NVL(tab.NEWVLAUE,'KONG') AND CP.IS_PROBLEM='0' THEN '1' ");
		sb.append("                                     WHEN NVL(CP.OLD_VALUE,'KONG') <> NVL(tab.NEWVLAUE,'KONG') AND CP.IS_PROBLEM IS NULL THEN '1' ELSE CP.IS_PROBLEM END IS_PROBLEM,");
		sb.append("                                CASE WHEN CP.OLD_VALUE =tab.NEWVLAUE THEN '' ELSE CP.error_type END errorType,");
		sb.append("                                CASE WHEN CP.OLD_VALUE =tab.NEWVLAUE THEN '' ELSE CP.error_level END errorLevel,");
		sb.append("                                CASE WHEN CP.OLD_VALUE =tab.NEWVLAUE THEN '' ELSE CP.problem_desc END problemDesc,");
		sb.append("                                CASE WHEN CP.OLD_VALUE =tab.NEWVLAUE THEN '' ELSE CP.tech_guidance END techDuidance,");
		sb.append("                                CASE WHEN CP.OLD_VALUE =tab.NEWVLAUE THEN '' ELSE CP.tech_scheme END techScheme,");
		sb.append("                                CP.ID,CP.WORK_ITEM_ID, tab.NEWVLAUE,TAB.PID");
		sb.append("                    FROM COLUMN_QC_PROBLEM CP,");
		sb.append("                    (SELECT  WK.PID, CASE WHEN 'poi_englishname' = '"+firstWorkItem+"' THEN  NM.NAMENEWVLAUE ");
		sb.append("                                          WHEN 'poi_name' = '"+firstWorkItem+"' THEN  NM.NAMENEWVLAUE ELSE ADR.ADDRNEWVLAUE END NEWVLAUE");
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
		sb.append("                         (SELECT CASE");
		sb.append("                                   WHEN 'addrSplit' = '"+secondWorkItem+"' THEN");
		sb.append("                                    A.NAME_ID || ':' || A.PROVINCE||'|'|| A.CITY ||'|'|| A.COUNTY ||'|'||");
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
		sb.append("                         (SELECT '[' || LISTAGG(PS.WORK_ITEM_ID, ',') WITHIN GROUP(ORDER BY PS.PID) || ']' WORK_ITEM_ID,");
		sb.append("                                 PS.PID");
		sb.append("                            FROM POI_COLUMN_STATUS PS, POI_COLUMN_WORKITEM_CONF PC");
		sb.append("                           WHERE PS.WORK_ITEM_ID = PC.WORK_ITEM_ID");
		sb.append("                             AND PC.SECOND_WORK_ITEM = '"+secondWorkItem+"'");
		sb.append("                             AND PC.CHECK_FLAG IN (1, 3)");
		sb.append("                             AND PS.PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append("                           GROUP BY PS.PID) WK");
		sb.append("                   WHERE WK.PID = NM.PID(+)");
		sb.append("                     AND WK.PID = ADR.PID(+))tab");
		sb.append("                   WHERE CP.PID=tab.pid");
		sb.append("                     AND CP.SUBTASK_ID ="+comSubTaskId);
		sb.append("                     AND CP.PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append("                     AND CP.SECOND_WORK_ITEM = '"+secondWorkItem+"'");
		sb.append("                     AND CP.IS_VALID = 0) TP");
		sb.append("                  ON (T.ID = TP.id)");
		sb.append("                  WHEN MATCHED THEN");
		sb.append("                    UPDATE SET T.IS_PROBLEM =TP.IS_PROBLEM,T.new_value=TP.NEWVLAUE,T.qc_time=:1,T.qc_worker="+userId+",T.error_type=TP.errorType,");
		sb.append("                               T.error_level =TP.errorLevel,T.problem_desc =TP.problemDesc,T.tech_guidance =TP.techDuidance,T.tech_scheme =TP.techScheme");

		
		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			pstmt.setTimestamp(1, new Timestamp(new Date().getTime()));
			log.info(sb.toString());
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	/**
	 * 更新质检问题表,拼音进行特殊处理；
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updatePYIsProblem(List<Integer> pidList,Connection conn,int comSubTaskId) throws Exception {

		PreparedStatement pstmt = null;
		try {
			
			Map<String,JSONObject> result =executeQuery(pidList,conn,comSubTaskId);
			for(Integer pid:pidList){
				JSONObject data = result.get(String.valueOf(pid));
				JSONObject newData=dealFields(data);
				executeUpdate(newData,pid,conn,comSubTaskId);
			}
	
		} catch (Exception e) {
			throw e;
		}
	}
	
	private JSONObject string2json(String data){
		JSONObject newdata = new JSONObject();
		if(data==null){ return newdata;}
		String[] strs=data.split("\\|");
		for(String str:strs){
			JSONObject js =JSONObject.fromObject("{\""+str.replace(":", "\":\"")+"\"}");
			Iterator<String> keys = js.keys();  
	        while (keys.hasNext()) {  
				String key=(String) keys.next();
				newdata.put(key, js.get(key));
			}
		}

		return newdata;
	}
	private String json2string(JSONObject js){
		String newdata = "";
		if(js==null){return newdata;}
		Iterator<String> keys = js.keys();  
		while(keys.hasNext()){
			String key=(String) keys.next();
			String str=key+":"+js.getString(key);
			if(newdata.isEmpty()){
				newdata=str;
			}else{
				newdata=newdata+"|"+str;
			}
		}
		return newdata;
	}
	
	private JSONObject dealFields(JSONObject data){
		JSONObject newdata = new JSONObject();

		List<String> newValue=Arrays.asList(((String) data.get("newValue")).split("\\|"));
		List<String> oldValue=Arrays.asList(((String) data.get("oldValue")).split("\\|"));
		
		JSONObject errorType=string2json((String) data.get("errorType"));
		JSONObject errorLevel=string2json((String) data.get("errorLevel"));
		JSONObject problemDesc=string2json((String) data.get("problemDesc"));
		JSONObject techDuidance=string2json((String) data.get("techDuidance"));
		JSONObject techScheme=string2json((String) data.get("techScheme"));
		JSONObject isProblem=string2json((String) data.get("isProblem"));

		//遍历newValue：
		//1、如果在oldValue中存在，且isProblem中值不等于0，则改为0。且删除其它五个字段中该名称对应的值。
		//2、如果在oldValue中不存在，且isProblem中值等于0，则改为1。
		for(String nv:newValue){
			String nameId=nv.substring(0,nv.indexOf(":"));
			if(oldValue.contains(nv)){
				if(isProblem.containsKey(nameId)){
					if(!isProblem.get(nameId).equals("0")){
						isProblem.put(nameId, "0");
						errorType.remove(nameId);
						errorLevel.remove(nameId);
						problemDesc.remove(nameId);
						techDuidance.remove(nameId);
						techScheme.remove(nameId);
					}
				}else{isProblem.put(nameId, "0");}
			}else{
				if(isProblem.containsKey(nameId)){
					if(isProblem.get(nameId).equals("0")){isProblem.put(nameId, "1");}
				}else{isProblem.put(nameId, "1");}
			}
		}
		//更新
		newdata.put("errorType", json2string(errorType));
		newdata.put("errorLevel", json2string(errorLevel));
		newdata.put("problemDesc", json2string(problemDesc));
		newdata.put("techDuidance", json2string(techDuidance));
		newdata.put("techScheme", json2string(techScheme));
		newdata.put("isProblem", json2string(isProblem));
		return newdata;
	}

	private void executeUpdate(JSONObject newData,int pid,Connection conn,int comSubTaskId) throws Exception {
		PreparedStatement pstmt = null;
		String sql="update COLUMN_QC_PROBLEM P SET P.IS_PROBLEM=:1,P.ERROR_TYPE=:2,P.ERROR_LEVEL=:3,"
				+ " P.PROBLEM_DESC=:4,P.TECH_GUIDANCE=:5,P.TECH_SCHEME=:6 where P.SECOND_WORK_ITEM = 'namePinyin' "
				+ " AND P.SUBTASK_ID = :7 "
				+ " AND P.PID = :8";
		try {
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, newData.getString("isProblem"));
			pstmt.setString(2, newData.getString("errorType"));
			pstmt.setString(3, newData.getString("errorLevel"));
			pstmt.setString(4, newData.getString("problemDesc"));
			pstmt.setString(5, newData.getString("techDuidance"));
			pstmt.setString(6, newData.getString("techScheme"));
			pstmt.setInt(7, comSubTaskId);
			pstmt.setInt(8, pid);
			log.info(sql);
			pstmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			DBUtils.closeStatement(pstmt);
		}
	}
	
	private Map<String,JSONObject> executeQuery(List<Integer> pidList,Connection conn,int comSubTaskId) throws Exception {
		Map<String,JSONObject> result = new HashMap<String,JSONObject>();
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		String sql="SELECT P.PID,P.OLD_VALUE,P.NEW_VALUE,P.IS_PROBLEM,P.ERROR_TYPE,P.ERROR_LEVEL,P.PROBLEM_DESC,P.TECH_GUIDANCE,P.TECH_SCHEME"
				+ " FROM COLUMN_QC_PROBLEM P "
				+ " WHERE P.SECOND_WORK_ITEM = 'namePinyin' "
				+ " AND P.PID IN ("+StringUtils.join(pidList, ",")+")"
				+ " AND P.SUBTASK_ID = "+comSubTaskId+" "
				+ " AND P.IS_VALID=0 ";
		try {
			pstmt = conn.prepareStatement(sql);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				JSONObject data = new JSONObject();
				data.put("newValue",resultSet.getString("NEW_VALUE"));
				data.put("oldValue",resultSet.getString("OLD_VALUE"));
				data.put("errorType",resultSet.getString("ERROR_TYPE"));
				data.put("errorLevel",resultSet.getString("ERROR_LEVEL"));
				data.put("problemDesc",resultSet.getString("PROBLEM_DESC"));
				data.put("techDuidance",resultSet.getString("TECH_GUIDANCE"));
				data.put("techScheme",resultSet.getString("TECH_SCHEME"));
				data.put("isProblem",resultSet.getString("IS_PROBLEM"));
				result.put(String.valueOf(resultSet.getInt("pid")),data);
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
	 * 更新质检问题表
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updateColumnQcProblem(List<Integer> pidList,Connection conn,int status,String second) throws Exception {
		
		
		
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE column_qc_problem SET isProblem=1,new_value= ,qc_time=,is_problem=,qc_worker= where FIRST_WORK_ITEM="+status+" and SECOND_WORK_ITEM="+status+" and PID="+status+" and SUBTASK_ID="+status+" ");
		
		PreparedStatement pstmt = null;

		ResultSet resultSet = null;
		try {
			
			Clob pidsClob = ConnectionUtil.createClob(conn);
			
			pidsClob.setString(1, StringUtils.join(pidList, ","));
			
			pstmt = conn.prepareStatement(sb.toString());
			
			pstmt.setClob(1, pidsClob);
			
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(resultSet);
			DbUtils.closeQuietly(pstmt);
		}
	}

}
