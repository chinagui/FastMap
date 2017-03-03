package com.navinfo.dataservice.column.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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

public class ColumnSubmitJob extends AbstractJob {
	
	public ColumnSubmitJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@SuppressWarnings("static-access")
	@Override
	public void execute() throws JobException {
		
		log.info("start submit....");
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<Integer> pidList = new ArrayList<Integer>();
		
		Connection conn = null;
		
		try {
			ColumnSubmitJobRequest columnSubmitJobRequest = (ColumnSubmitJobRequest) this.request;
			
			int taskId = columnSubmitJobRequest.getTaskId();
			int userId = columnSubmitJobRequest.getUserId();
			String firstWorkItem = columnSubmitJobRequest.getFirstWorkItem();
			String secondWorkItem = columnSubmitJobRequest.getSecondWorkItem();
			
			log.info("params:taskId="+taskId+";userId="+userId+";firstWorkItem="+firstWorkItem+";secondWorkItem="+secondWorkItem);
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
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
				pidList = ixPoiDeepStatusSelector.getRowIdForSubmit(firstWorkItem, second, taskId);
				log.info("查询可提交数据pdis:"+pidList);
				// 清理检查结果
				DeepCoreControl deepControl = new DeepCoreControl();
				deepControl.cleanCheckResult(pidList, conn);
				
				OperationResult operationResult=new OperationResult();
				
				List<Long> pids = new ArrayList<Long>();
				for (int pid:pidList) {
					pids.add((long)pid);
				}
				
				PoiLogDetailStat logDetail = new PoiLogDetailStat();
				Map<Long,List<LogDetail>> submitLogs = logDetail.loadByColEditStatus(conn, pids, userId, taskId, firstWorkItem, second);
				log.info("提交log:"+submitLogs);
				List<BasicObj> objList = new ArrayList<BasicObj>();
				ObjHisLogParser logParser = new ObjHisLogParser();
				Set<String> tabNames = new HashSet<String>();
				tabNames.add("IX_POI_NAME");
				tabNames.add("IX_POI_ADDRESS");
				tabNames.add("IX_POI_FLAG");
				for (int pid:pidList) {
					BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", tabNames,false, pid, false);
					if (submitLogs.containsKey(new Long((long)pid))) {
						logParser.parse(obj, submitLogs.get(new Long((long)pid)));
					}
					objList.add(obj);
				}
					
				operationResult.putAll(objList);
				
				PoiColumnOpConf columnOpConf = ixPoiOpConfSelector.getDeepOpConf(firstWorkItem,second, type);
					
				// 批处理
				if (columnOpConf.getSubmitExebatch() == 1) {
					log.info("执行批处理");
					if (columnOpConf.getSubmitBatchrules() != null) {
						log.info(columnOpConf.getSubmitBatchrules());
						BatchCommand batchCommand=new BatchCommand();		
						for (String ruleId:columnOpConf.getSubmitBatchrules().split(",")) {
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
								Iterator <Integer> it = pidList.iterator();
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
				
				// 修改poi_deep_status表作业项状态
				log.info("更新状态pids:"+pidList);
				updateDeepStatus(pidList, conn, 3,second);
				
				
				// 重分类
				if (columnOpConf.getSubmitExeclassify()==1) {
					log.info("执行重分类");
					if (columnOpConf.getSubmitCkrules() != null && columnOpConf.getSubmitClassifyrules() != null) {
						HashMap<String,Object> classifyMap = new HashMap<String,Object>();
						classifyMap.put("userId", userId);
						classifyMap.put("ckRules", columnOpConf.getSubmitCkrules());
						classifyMap.put("classifyRules", columnOpConf.getSubmitClassifyrules());
						log.info(columnOpConf.getSubmitClassifyrules());
						
						classifyMap.put("pids", pidList);
						ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
						columnCoreOperation.runClassify(classifyMap,conn,taskId);
					}
				}
				
				// 清理重分类检查结果
				log.info("清理重分类检查结果");
				if (classifyRules.size()>0) {
					deepControl.cleanExByCkRule(conn, pidList, classifyRules, "IX_POI");
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
	public void updateDeepStatus(List<Integer> pidList,Connection conn,int status,String second) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_column_status SET first_work_status="+status+",second_work_status="+status+",handler=0 WHERE work_item_id IN (SELECT cf.work_item_id FROM POI_COLUMN_WORKITEM_CONF cf WHERE cf.second_work_item='"+second+"') AND  pid in (select to_number(column_value) from table(clob_to_table(?)))");
		
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
