package com.navinfo.dataservice.column.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
			
			log.info("userId:"+userId+",taskId:"+taskId);
			log.info("secondWorkItem:"+secondWorkItem);
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
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
					batch.persistChangeLog(OperationSegment.SG_COLUMN, userId);
				}
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

}
