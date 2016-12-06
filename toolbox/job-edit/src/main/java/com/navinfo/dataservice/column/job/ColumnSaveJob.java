package com.navinfo.dataservice.column.job;

import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiDeepOpConf;
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

public class ColumnSaveJob extends AbstractJob {
	
	public ColumnSaveJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<Integer> pidList = new ArrayList<Integer>();
		
		Connection conn = null;
		try {
			ColumnSaveJobRequest columnSaveJobRequest = (ColumnSaveJobRequest) this.request;
			int taskId = columnSaveJobRequest.getTaskId();
			int userId = columnSaveJobRequest.getUserId();
			JSONArray data = columnSaveJobRequest.getData();
			String secondWorkItem = columnSaveJobRequest.getSecondWorkItem();
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			columnSave(dbId, data);
			
			JSONArray dataArray = new JSONArray(); 
			for (int i=0;i<data.size();i++) {
				JSONObject temp = new JSONObject();
				int pid = data.getJSONObject(i).getInt("pid");
				pidList.add(pid);
				temp.put("pid", pid);
				temp.put("taskId", taskId);
				dataArray.add(temp);
			}
			
			// 修改poi_deep_status表作业项状态
			updateDeepStatus(pidList, conn, 2);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			// 查询检查、批处理和重分类配置
			IxPoiOpConfSelector ixPoiOpConfSelector = new IxPoiOpConfSelector(conn);
			PoiDeepOpConf deepOpConf = ixPoiOpConfSelector.getDeepOpConf("",secondWorkItem, type);
			
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
			
			// 批处理
			BatchCommand batchCommand=new BatchCommand();		
			String operationName="COLUMN_SAVE";
			//List<String> batchList=new ArrayList<String>();
//			ruleIdList.add("GLM001TEST");
			batchCommand.setOperationName(operationName);
			
			Batch batch=new Batch(conn,operationResult);
//			batch.setCmd(batchCommand);
//			batch.operate();
			
			// 检查
			CheckCommand checkCommand=new CheckCommand();		
			List<String> checkList=new ArrayList<String>();
//			checkList.add("GLM001TEST");
			checkCommand.setRuleIdList(checkList);
			
			Check check=new Check(conn,operationResult);
//			check.setCmd(checkCommand);
//			check.operate();
			
			// 重分类
			if (deepOpConf.getSaveExeclassify()==1) {
				HashMap<String,Object> classifyMap = new HashMap<String,Object>();
				classifyMap.put("userId", userId);
				classifyMap.put("ckRules", deepOpConf.getSaveCkrules());
				classifyMap.put("classifyRules", deepOpConf.getSaveClassifyrules());
				
				classifyMap.put("data", dataArray);
				ColumnCoreOperation columnCoreOperation = new ColumnCoreOperation();
				columnCoreOperation.runClassify(classifyMap,conn);
			}
			
		} catch (Exception e) {
			throw new JobException(e);
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * 保存精编数据
	 * @param dbId
	 * @param data
	 * @throws Exception
	 */
	public void columnSave(int dbId,JSONArray data) throws Exception {
		try {
			for (int i=0;i<data.size();i++) {
				JSONObject poiObj = new JSONObject();
				poiObj.put("dbId", dbId);
				poiObj.put("data", data.getJSONObject(i));
				EditApi apiEdit=(EditApi) ApplicationContextUtil.getBean("editApi");
				apiEdit.run(poiObj);
			}
			
		} catch (Exception e) {
			throw e;
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
		sb.append("UPDATE poi_column_status SET firstWorkStatus="+status+",secondWorkStatus="+status+" WHERE pid in (select to_number(column_value) from table(clob_to_table(?)))");
		
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
