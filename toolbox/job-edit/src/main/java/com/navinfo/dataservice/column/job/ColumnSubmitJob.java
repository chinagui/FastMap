package com.navinfo.dataservice.column.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiColumnOpConf;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiColumnStatusSelector;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiOpConfSelector;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONObject;

public class ColumnSubmitJob extends AbstractJob {
	
	public ColumnSubmitJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@SuppressWarnings("static-access")
	@Override
	public void execute() throws JobException {
		
		log.info("start submit....");
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<Integer> allPidList = new ArrayList<Integer>();
		List<Integer> qcPidList = new ArrayList<Integer>();
		
		
		Connection conn = null;
		
		try {
			ColumnSubmitJobRequest columnSubmitJobRequest = (ColumnSubmitJobRequest) this.request;
			
			int taskId = columnSubmitJobRequest.getTaskId();
			int userId = columnSubmitJobRequest.getUserId();
			String firstWorkItem = columnSubmitJobRequest.getFirstWorkItem();
			String secondWorkItem = columnSubmitJobRequest.getSecondWorkItem();
			
			log.info("params:taskId="+taskId+";userId="+userId+";firstWorkItem="+firstWorkItem+";secondWorkItem="+secondWorkItem);
			
			
			int comSubTaskId=0;
			int isQuality=0;

			Subtask subtask = apiService.queryBySubtaskId(taskId);
			isQuality=subtask.getIsQuality();
			if(isQuality==1){
				Subtask comSubtask = apiService.queryBySubTaskIdAndIsQuality(taskId, "2", 1);
				comSubTaskId=comSubtask.getSubtaskId();
				log.info("月编质检提交");
			}else{
				comSubTaskId=taskId;
				log.info("月编常规提交");
			}
			
			int dbId = subtask.getDbId();
			log.info("dbId="+dbId);
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			IxPoiOpConfSelector ixPoiOpConfSelector = new IxPoiOpConfSelector(conn);
			IxPoiColumnStatusSelector ixPoiDeepStatusSelector = new IxPoiColumnStatusSelector(conn);
			
			List<String> secondWorkList = new ArrayList<String>();
			if (secondWorkItem == null || secondWorkItem.isEmpty()) {
				secondWorkList = ixPoiOpConfSelector.getSecondByFirst(firstWorkItem, type);
			} else {
				secondWorkList.add(secondWorkItem);
			}
			
			for (String second:secondWorkList) {
				log.info("当前提交二级项:"+second);
				// 查询可提交数据
				allPidList = ixPoiDeepStatusSelector.getPIdForSubmit(firstWorkItem, second, comSubTaskId,userId,false);
				qcPidList = ixPoiDeepStatusSelector.getPIdForSubmit(firstWorkItem, second, comSubTaskId,userId,true);
				log.info("查询可提交数据pdis:"+allPidList);
				// 清理检查结果
				DeepCoreControl deepControl = new DeepCoreControl();
				deepControl.cleanCheckResult(allPidList, conn);
				
				OperationResult operationResult=new OperationResult();
				
				List<Long> allPids = new ArrayList<Long>();
				for (int pid:allPidList) {
					allPids.add((long)pid);
				}

				PoiLogDetailStat logDetail = new PoiLogDetailStat();
				Map<Long,List<LogDetail>> submitLogs = logDetail.loadByColEditStatus(conn, allPids, userId, comSubTaskId, firstWorkItem, second);
				log.info("提交log:"+submitLogs);
				List<BasicObj> objList = new ArrayList<BasicObj>();
				ObjHisLogParser logParser = new ObjHisLogParser();
				Set<String> tabNames = new HashSet<String>();
				tabNames.add("IX_POI_NAME");
				tabNames.add("IX_POI_ADDRESS");
				tabNames.add("IX_POI_NAME_FLAG");
				for (int pid:allPidList) {
					BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", tabNames,false, pid, false);
					if (submitLogs.containsKey(new Long((long)pid))) {
						logParser.parse(obj, submitLogs.get(new Long((long)pid)));
					}
					objList.add(obj);
				}
					
				operationResult.putAll(objList);
				
				PoiColumnOpConf columnOpConf = ixPoiOpConfSelector.getDeepOpConf(firstWorkItem,second, type);
					
				// 批处理
				boolean isBatch164 =false;
				if (columnOpConf.getSubmitExebatch() == 1) {
					log.info("执行批处理");
					if (columnOpConf.getSubmitBatchrules() != null) {
						log.info(columnOpConf.getSubmitBatchrules());
						BatchCommand batchCommand=new BatchCommand();		
						for (String ruleId:columnOpConf.getSubmitBatchrules().split(",")) {
							//常规提交时，这块不执行FM-BAT-20-164批处理，等抽样完成后，对未抽样的数据再执行164批处理；
							if(isQuality==0&&ruleId.equals("FM-BAT-20-164")){
								isBatch164=true;
								continue;
							}
							batchCommand.setRuleId(ruleId);
						}
						Batch batch=new Batch(conn,operationResult);
						batch.operate(batchCommand);
						batch.setPhysiDelete(true);
						batch.persistChangeLog(OperationSegment.SG_COLUMN, userId);
					}
				}
				
				// 获取重分类规则号
				List<String> classifyRules = new ArrayList<String>();
				String classifyrules = columnOpConf.getSubmitClassifyrules();
				if (classifyrules != null) {
					for (String classifyrule:classifyrules.split(",")) {
						classifyRules.add(classifyrule);
					}
				}
				
				// 检查
				if (columnOpConf.getSubmitExecheck() == 1) {
					log.info("检查批处理");
					if (columnOpConf.getSubmitCkrules() != null) {
						log.info(columnOpConf.getSubmitCkrules());
						CheckCommand checkCommand=new CheckCommand();		
						List<String> checkList=new ArrayList<String>();
						for (String ckRule:columnOpConf.getSubmitCkrules().split(",")) {
							checkList.add(ckRule);
						}
						checkCommand.setRuleIdList(checkList);
						
						Check check=new Check(conn,operationResult);
						check.operate(checkCommand);
						
						// pidList替换为无检查错误的pidList
						Map<String, Map<Long, Set<String>>> errorMap = check.getErrorPidMap();
						if (errorMap != null) {
							Map<Long, Set<String>> poiMap = errorMap.get("IX_POI");
							for (long pid:poiMap.keySet()) {
								// 检查出的规则号
								Set<String> ckRules = poiMap.get(pid);
								Iterator <Integer> it = allPidList.iterator();
								while (it.hasNext()) {
									if (it.next() == pid) {
										for (String ckRule:ckRules) {
											if (!classifyRules.contains(ckRule)) {
												it.remove();
												break;
											}
										}
									}
								}
							}
						}
					}
				}
				
				ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
				if(isQuality==0){
					//去除掉有检查错误的数据，留下打了质检标记，且无检查错误的数据；
					qcPidList.retainAll(allPidList);
					log.info("查询打了质检标记的数据:"+qcPidList);
					allPidList.removeAll(qcPidList);
					
					OperationResult comOperationResult=new OperationResult();
					List<Long> commenPids = new ArrayList<Long>();
					for (int pid:allPidList) {
						commenPids.add((long)pid);
					}
					List<BasicObj> commenObjList = new ArrayList<BasicObj>();
					for(BasicObj Obj:operationResult.getAllObjs()){
						if(commenPids.contains(Obj.getMainrow().getObjPid())){
							commenObjList.add(Obj);
						}
					}

					comOperationResult.putAll(commenObjList);

					// 批处理
					if (isBatch164) {
						log.info("执行批处理FM-BAT-20-164");
						BatchCommand batchCommand=new BatchCommand();
						batchCommand.setRuleId("FM-BAT-20-164");
						Batch batch=new Batch(conn,comOperationResult);
						batch.operate(batchCommand);
						batch.setPhysiDelete(true);
						batch.persistChangeLog(OperationSegment.SG_COLUMN, userId);
					}
					
					log.info("常规提交时，对未打质检标记的数据，更新poi_deep_status表作业项状态:FIRST_WORK_STATUS=3、SECOND_WORK_STATUS=3、HANDLER=0:"+allPidList);
					updateDeepStatus(allPidList, conn, 3,second,0);
					if(qcPidList!=null&&qcPidList.size()>0){
						log.info("常规提交时，对打了质检标记的数据，更新poi_deep_status表作业项状态:FIRST_WORK_STATUS=1、SECOND_WORK_STATUS=1、HANDLER=0:"+qcPidList);
						updateDeepStatus(qcPidList, conn, 1,second,1);
						log.info("常规提交时，初始化质检问题记录表column_qc_problem");
						columnCoreOperation.insertColumnQcProblems(qcPidList,conn,comSubTaskId,firstWorkItem,second,userId,false);
					}
				}else{
					//质检提交时调用，更新质检问题表状态
					if(allPidList!=null&&allPidList.size()>0){
						updateColumnQcProblems(allPidList,conn,comSubTaskId,firstWorkItem,secondWorkItem,userId);
						log.info("常规提交时，对未打质检标记的数据，更新poi_deep_status表作业项状态:FIRST_WORK_STATUS=3、SECOND_WORK_STATUS=3、HANDLER=0:"+allPidList);
						updateDeepStatus(allPidList, conn, 3,second,1);
					}
				}
				
				
				// 重分类
				if (columnOpConf.getSubmitExeclassify()==1) {
					log.info("执行重分类");
					List<Integer> pidList = new ArrayList<Integer>();
					pidList.addAll(qcPidList);
					pidList.addAll(allPidList);
					if (columnOpConf.getSubmitCkrules() != null && columnOpConf.getSubmitClassifyrules() != null) {
						HashMap<String,Object> classifyMap = new HashMap<String,Object>();
						classifyMap.put("userId", userId);
						classifyMap.put("ckRules", columnOpConf.getSubmitCkrules());
						classifyMap.put("classifyRules", columnOpConf.getSubmitClassifyrules());
						classifyMap.put("firstWorkItem", firstWorkItem);
						classifyMap.put("secondWorkItem", second);
						//增加质检功能后，需要维护重分类后的质检标记
						Map<Integer,JSONObject> qcFlag = columnCoreOperation.getColumnDataQcFlag(pidList,userId,conn,comSubTaskId,isQuality);
						classifyMap.put("qcFlag", qcFlag);
						log.info(columnOpConf.getSubmitClassifyrules());

						classifyMap.put("pids", pidList);
						
						columnCoreOperation.runClassify(classifyMap,conn,comSubTaskId,true,isQuality);
					}
				}
				
				// 清理重分类检查结果
				log.info("清理重分类检查结果");
				if (classifyRules.size()>0) {
					deepControl.cleanExByCkRule(conn, allPidList, classifyRules, "IX_POI");
				}
				
			}
			
			log.info("提交完成");
		} catch (Exception e) {
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		} finally {
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	
	/**
	 * 更新配置表状态
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updateDeepStatus(List<Integer> pidList,Connection conn,int status,String second,int qcFlag) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_column_status SET first_work_status="+status+",second_work_status="+status+",handler=0 ");
		sb.append(" WHERE work_item_id IN (SELECT cf.work_item_id FROM POI_COLUMN_WORKITEM_CONF cf WHERE cf.second_work_item='"+second+"') AND  pid in (select to_number(column_value) from table(clob_to_table(?)))");
		sb.append(" and QC_FLAG="+qcFlag );

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
	 * 质检提交时调用，更新质检问题表状态
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updateColumnQcProblems(List<Integer> pidList,Connection conn,int comSubTaskId,String firstWorkItem,String secondWorkItem,int userId) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE COLUMN_QC_PROBLEM SET IS_VALID=1 ");
		sb.append(" WHERE SUBTASK_ID ="+comSubTaskId);
		sb.append(" AND PID IN ("+StringUtils.join(pidList, ",")+")");
		sb.append(" AND SECOND_WORK_ITEM = '"+secondWorkItem+"'");
		sb.append(" AND IS_VALID = 0 ");
		
		PreparedStatement pstmt = null;
		try {
			
			pstmt = conn.prepareStatement(sb.toString());

			pstmt.executeUpdate();
			
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(pstmt);
		}
	}
	

	

}
