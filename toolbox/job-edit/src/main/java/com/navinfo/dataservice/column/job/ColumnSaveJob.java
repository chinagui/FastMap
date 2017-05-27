package com.navinfo.dataservice.column.job;

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
			
			//更新质检问题表
			if(isQuality==1){
				updateColumnQcProblems(pidList,conn,comSubTaskId,firstWorkItem,secondWorkItem,userId);
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
					deepControl.cleanExByCkRule(conn, pidList, checkList, "IX_POI");
					
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
					
					classifyMap.put("pids", pidList);
					ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
					columnCoreOperation.runClassify(classifyMap,conn,taskId);
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
				deepControl.cleanExByCkRule(conn, pidList, ckRules, "IX_POI");
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
		sb.append("                  COLUMN_QC_PROBLEM");
		sb.append("                  T");
		sb.append("                  USING (SELECT CASE WHEN CP.OLD_VALUE =tab.NAMENEWVLAUE   THEN 0 ELSE 1 END IS_PROBLEM,CP.ID,tab.WORK_ITEM_ID, tab.NAMENEWVLAUE, tab.ADDRNEWVLAUE,TAB.PID");
		sb.append("                    FROM COLUMN_QC_PROBLEM CP,");
		sb.append("                    (SELECT WK.PID, WK.WORK_ITEM_ID, NM.NAMENEWVLAUE, ADR.ADDRNEWVLAUE");
		sb.append("                    FROM (SELECT CASE");
		sb.append("                                   WHEN 'poi_englishname' = '"+firstWorkItem+"' THEN");
		sb.append("                                    LISTAGG(N.POI_PID || ':' || N.NAME || ',' || F.FLAG_CODE,");
		sb.append("                                            '|') WITHIN GROUP(ORDER BY N.NAME_ID)");
		sb.append("                                   ELSE");
		sb.append("                                    LISTAGG(N.POI_PID || ':' || N.NAME, '|') WITHIN");
		sb.append("                                    GROUP(ORDER BY N.NAME_ID)");
		sb.append("                                 END NAMENEWVLAUE,");
		sb.append("                                 N.POI_PID PID");
		sb.append("                            FROM IX_POI_NAME N, IX_POI_NAME_FLAG F");
		sb.append("                           WHERE N.POI_PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append("                             AND N.NAME_ID = F.NAME_ID(+)");
		sb.append("                             AND N.LANG_CODE IN "+langCode);
		sb.append("                             AND N.NAME_TYPE IN "+nameType);
		sb.append("                             AND N.NAME_CLASS IN "+nameClass);
		sb.append("                           GROUP BY N.POI_PID) NM,");
		sb.append("                         (SELECT CASE");
		sb.append("                                   WHEN 'addrSplit' = '"+secondWorkItem+"' THEN");
		sb.append("                                    A.NAME_ID || ':' || A.PROVINCE || A.CITY || A.COUNTY ||");
		sb.append("                                    A.TOWN || A.PLACE || A.STREET || A.LANDMARK || A.PREFIX ||");
		sb.append("                                    A.HOUSENUM || A.TYPE || A.SUBNUM || A.SURFIX || A.ESTAB ||");
		sb.append("                                    A.BUILDING || A.FLOOR || A.UNIT || A.ROOM || A.ADDONS");
		sb.append("                                   WHEN 'addrPinyin' = '"+secondWorkItem+"' THEN");
		sb.append("                                    A.NAME_ID || ':' || A.PROV_PHONETIC || A.CITY_PHONETIC ||");
		sb.append("                                    A.TOWN_PHONETIC || A.PLACE_PHONETIC || A.STREET_PHONETIC ||");
		sb.append("                                    A.LANDMARK_PHONETIC || A.PREFIX_PHONETIC ||");
		sb.append("                                    A.HOUSENUM_PHONETIC || A.TYPE_PHONETIC || A.SUBNUM_PHONETIC ||");
		sb.append("                                    A.SURFIX_PHONETIC || A.ESTAB_PHONETIC ||");
		sb.append("                                    A.BUILDING_PHONETIC || A.FLOOR_PHONETIC || A.UNIT_PHONETIC ||");
		sb.append("                                    A.ROOM_PHONETIC || A.ADDONS_PHONETIC");
		sb.append("                                   WHEN 'poi_englishaddress' = '"+firstWorkItem+"' THEN");
		sb.append("                                    A.NAME_ID || ':' || A.FULLNAME");
		sb.append("                                   ELSE");
		sb.append("                                    ''");
		sb.append("                                 END ADDRNEWVLAUE,");
		sb.append("                                 A.POI_PID PID");
		sb.append("                            FROM IX_POI_ADDRESS A");
		sb.append("                           WHERE A.POI_PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append("                             AND A.LANG_CODE IN "+langCode+") ADR,");
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
		sb.append("                    UPDATE SET T.IS_PROBLEM =TP.IS_PROBLEM,T.new_value=TP.NAMENEWVLAUE,T.qc_time="+new Date()+",T.qc_worker="+userId);

		
		PreparedStatement pstmt = null;

		try {
			pstmt = conn.prepareStatement(sb.toString());
			log.info(sb.toString());
			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {

		}
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
