package com.navinfo.dataservice.edit.job;

import java.lang.reflect.Method;
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
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.column.job.ColumnCoreOperation;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiColumnOpConf;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiOpConfSelector;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class columnSaveTest {

	@Before
	public void before(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(  
                new String[] {"dubbo-consumer-man-test.xml"}); 
		context.start();
		new ApplicationContextUtil().setApplicationContext(context);
	}
	
	@Test
	public void execute() throws Exception{
		String param = "{\"taskId\":\"84\",\"secondWorkItem\":\"netEngName\",\"dataList\":[{\"command\":\"UPDATE\",\"dbId\":\"19\",\"type\":\"IXPOI\",\"objId\":335,\"data\":{\"names\":[{\"name\":\"Beijing Huajun Traditional Controversial Chinese Medicine Hospital 1122\",\"rowId\":\"3AE1FCF65D2092F7E050A8C08304EE4C\",\"pid\":8069341,\"objStatus\":\"UPDATE\"}],\"rowId\":\"3AE1FB4B0B6492F7E050A8C08304EE4C\",\"pid\":335,\"objStatus\":\"UPDATE\"}}]},\"stepCount\":2,\"userId\":2}";
		Connection conn = DBConnector.getInstance().getConnectionById(19);
		JSONObject dataJson = JSONObject.fromObject(param);
		DefaultObjImportor importor = new DefaultObjImportor(conn,null);
		EditJson editJson = new EditJson();
		editJson.addJsonPoi(dataJson.getJSONArray("dataList"));
		DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
		importor.operate(command);
		importor.persistChangeLog(2, 0);
		OperationResult op = importor.getResult();
		List<BasicObj> obj = op.getAllObjs();
		conn.commit();
	}
	
	@Test
	public void classfiyTest() throws Exception {
		Connection conn = null;
		List<Integer> pidList = new ArrayList<Integer>();
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		String param = "{\"taskId\":\"84\",\"secondWorkItem\":\"netEngName\",\"dataList\":[{\"command\":\"UPDATE\",\"dbId\":\"19\",\"type\":\"IXPOI\",\"objId\":335,\"data\":{\"names\":[{\"name\":\"Beijing Huajun Traditional Controversial Chinese Medicine Hospital 1122\",\"rowId\":\"3AE1FCF65D2092F7E050A8C08304EE4C\",\"pid\":8069341,\"objStatus\":\"UPDATE\"}],\"rowId\":\"3AE1FB4B0B6492F7E050A8C08304EE4C\",\"pid\":335,\"objStatus\":\"UPDATE\"}}]},\"stepCount\":2,\"userId\":2}";
		try {
			JSONObject paramJson = JSONObject.fromObject(param);
			int userId = 2;
			int taskId = paramJson.getInt("taskId");
			JSONArray data = paramJson.getJSONArray("dataList");
			String secondWorkItem = paramJson.getString("secondWorkItem");
			
			
			Subtask subtask = apiService.queryBySubtaskId(taskId);
			
			int dbId = subtask.getDbId();
			conn = DBConnector.getInstance().getConnectionById(dbId);
			
			DefaultObjImportor importor = new DefaultObjImportor(conn,null);
			EditJson editJson = new EditJson();
			editJson.addJsonPoi(data);
			DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
			importor.operate(command);
			importor.persistChangeLog(OperationSegment.SG_COLUMN, userId);
			
			for (int i=0;i<data.size();i++) {
				int pid = data.getJSONObject(i).getInt("objId");
				pidList.add(pid);
			}
			
			// 修改poi_column_status表作业项状态
			updateColumnStatus(pidList, conn, 2,secondWorkItem);
			
			// TODO 区分大陆/港澳
			int type = 1;
			
			// 查询检查、批处理和重分类配置
			IxPoiOpConfSelector ixPoiOpConfSelector = new IxPoiOpConfSelector(conn);
			PoiColumnOpConf columnOpConf = ixPoiOpConfSelector.getDeepOpConf("",secondWorkItem, type);
			
			// 清理检查结果
			DeepCoreControl deepControl = new DeepCoreControl();
			deepControl.cleanCheckResult(pidList, conn);
			
			OperationResult operationResult=importor.getResult();
			
			// 批处理
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
			if (columnOpConf.getSaveExecheck() == 1) {
				CheckCommand checkCommand=new CheckCommand();		
				List<String> checkList=new ArrayList<String>();
				if (columnOpConf.getSaveCkrules() != null) {
					for (String ckRule:columnOpConf.getSaveCkrules().split(",")) {
						checkList.add(ckRule);
					}
					checkCommand.setRuleIdList(checkList);
					
					Check check=new Check(conn,operationResult);
					check.operate(checkCommand);
				}
			}
			
			
			// 重分类
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
			List<String> ckRules = new ArrayList<String>();
			String classifyrules = columnOpConf.getSaveClassifyrules();
			if (classifyrules != null) {
				for (String classifyrule:classifyrules.split(",")) {
					ckRules.add(classifyrule);
				}
				deepControl.cleanExByCkRule(conn, pidList, ckRules, "IX_POI");
			}
			
			conn.commit();
			
		} catch (Exception e) {
			throw e;
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
