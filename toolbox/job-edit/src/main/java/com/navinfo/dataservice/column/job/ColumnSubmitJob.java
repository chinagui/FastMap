package com.navinfo.dataservice.column.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ColumnSubmitJob extends AbstractJob {
	
	public ColumnSubmitJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<Integer> pidList = new ArrayList<Integer>();
		
		Connection conn = null;
		
		try {
			ColumnSubmitJobRequest columnSubmitJobRequest = (ColumnSubmitJobRequest) this.request;
			
			int taskId = columnSubmitJobRequest.getTaskId();
			int userId = columnSubmitJobRequest.getUserId();
			String firstWorkItem = columnSubmitJobRequest.getFirstWorkItem();
			String secondWorkItem = columnSubmitJobRequest.getSecondWorkItem();
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			// 查询可提交数据
			IxPoiColumnStatusSelector ixPoiDeepStatusSelector = new IxPoiColumnStatusSelector(conn);
			pidList = ixPoiDeepStatusSelector.getRowIdForSubmit(firstWorkItem, secondWorkItem, taskId);
			
			// 清理检查结果
			DeepCoreControl deepControl = new DeepCoreControl();
			deepControl.cleanCheckResult(pidList, conn);
			
			OperationResult operationResult=new OperationResult();
			
			List<BasicObj> objList = new ArrayList<BasicObj>();
			for (int pid:pidList) {
				BasicObj obj=ObjSelector.selectByPid(conn, "IX_POI", null, pid, false);
				objList.add(obj);
				
			}
			operationResult.putAll(objList);
			
			IxPoiOpConfSelector ixPoiOpConfSelector = new IxPoiOpConfSelector(conn);
			PoiColumnOpConf columnOpConf = ixPoiOpConfSelector.getDeepOpConf(firstWorkItem,secondWorkItem, type);
			
			// 批处理
			if (columnOpConf.getSaveExebatch() == 1) {
				BatchCommand batchCommand=new BatchCommand();		
				for (String ruleId:columnOpConf.getSaveBatchrules().split(",")) {
					batchCommand.setRuleId(ruleId);
				}
				
				Batch batch=new Batch(conn,operationResult);
				batch.operate(batchCommand);
			}
			
			// 检查
			if (columnOpConf.getSaveExecheck() == 1) {
				CheckCommand checkCommand=new CheckCommand();		
				List<String> checkList=new ArrayList<String>();
				for (String ckRule:columnOpConf.getSaveCkrules().split(",")) {
					checkList.add(ckRule);
				}
				checkCommand.setRuleIdList(checkList);
				
				Check check=new Check(conn,operationResult);
				check.operate(checkCommand);
				
				// pidList替换为无检查错误的pidList
				Map<String, Map<Long, Set<String>>> errorMap = check.getErrorPidMap();
				Map<Long, Set<String>> poiMap = errorMap.get("IX_POI");
				for (long pid:poiMap.keySet()) {
					pidList.remove(pid);
				}
				
			}
			
			// 修改poi_deep_status表作业项状态
			updateDeepStatus(pidList, conn, 3);
			
			
			// 重分类
			if (columnOpConf.getSubmitExeclassify()==1) {
				HashMap<String,Object> classifyMap = new HashMap<String,Object>();
				classifyMap.put("userId", userId);
				classifyMap.put("ckRules", columnOpConf.getSaveCkrules());
				classifyMap.put("classifyRules", columnOpConf.getSaveClassifyrules());
				
				classifyMap.put("pids", pidList);
				ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
				columnCoreOperation.runClassify(classifyMap,conn);
			}
			
			// 清理重分类检查结果
			List<String> ckRules = new ArrayList<String>();
			String classifyrules = columnOpConf.getSaveClassifyrules();
			for (String classifyrule:classifyrules.split(",")) {
				ckRules.add(classifyrule);
			}
			deepControl.cleanExByCkRule(conn, pidList, ckRules, "IX_POI");
			
			conn.commit();
			
		} catch (Exception e) {
			throw new JobException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * 更新配置表状态
	 * @param rowIdList
	 * @param conn
	 * @throws Exception
	 */
	public void updateDeepStatus(List<Integer> pidList,Connection conn,int status) throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE poi_column_status SET first_work_status="+status+",second_work_status="+status+",handler=0 WHERE pid in (select to_number(column_value) from table(clob_to_table(?)))");
		
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
