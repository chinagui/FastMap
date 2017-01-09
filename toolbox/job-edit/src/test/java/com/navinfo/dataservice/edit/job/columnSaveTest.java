package com.navinfo.dataservice.edit.job;

import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
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
import com.navinfo.dataservice.column.job.PoiColumnValidationJobRequest;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiColumnOpConf;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiOpConfSelector;
import com.navinfo.dataservice.dao.plus.log.LogDetail;
import com.navinfo.dataservice.dao.plus.log.ObjHisLogParser;
import com.navinfo.dataservice.dao.plus.log.PoiLogDetailStat;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.obj.ObjectName;
import com.navinfo.dataservice.dao.plus.operation.AbstractOperation;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.operation.OperationSegment;
import com.navinfo.dataservice.dao.plus.selector.ObjBatchSelector;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.navicommons.database.QueryRunner;

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
		String param = "{\"taskId\":\"84\",\"secondWorkItem\":\"addrSplit\",\"dataList\":[{\"command\":\"UPDATE\",\"dbId\":\"19\",\"type\":\"IXPOI\",\"objId\":335,\"data\":{\"addresses\":[{\"city\":\"4\",\"county\":\"5\",\"town\":\"6\",\"rowId\":\"3AE1F6852F6B92F7E050A8C08304EE4C\",\"pid\":338,\"objStatus\":\"UPDATE\"}],\"rowId\":\"3AE1FB4B0B6492F7E050A8C08304EE4C\",\"pid\":335,\"objStatus\":\"UPDATE\"}}]},\"stepCount\":2,\"userId\":2}";
		Connection conn = DBConnector.getInstance().getConnectionById(19);
		JSONObject dataJson = JSONObject.fromObject(param);
		DefaultObjImportor importor = new DefaultObjImportor(conn,null);
		EditJson editJson = new EditJson();
		JSONArray temp = dataJson.getJSONArray("dataList");
		editJson.addJsonPoi(temp);
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
		String param = "{\"taskId\":\"84\",\"secondWorkItem\":\"namePinyin\",\"dataList\":[{\"command\":\"UPDATE\",\"dbId\":\"19\",\"type\":\"IXPOI\",\"objId\":335,\"data\":{\"names\":[{\"namePhonetic\":\"Tai Xing Qu Huang No.Dai Fu 1\",\"rowId\":\"3AE1FCF65D1D92F7E050A8C08304EE23\",\"pid\":1341414,\"objStatus\":\"UPDATE\"}],\"rowId\":\"3AE1FB4B0B6492F7E050A8C08304EE4C\",\"pid\":335,\"objStatus\":\"UPDATE\"}}]},\"stepCount\":2,\"userId\":2}";
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
	
	@Test
	public void executeCheck() throws JobException {
		Connection conn=null;
		try{
			conn=DBConnector.getInstance().getConnectionById(19);
			//获取要检查的数据pid
			List<Long> pids = getCheckPidList(conn);
			
			//获取规则号列表
			List<String> ruleList = getCheckRuleList(conn);			
			// 清理检查结果
			DeepCoreControl deepControl = new DeepCoreControl();
			List<Integer> pidIntList=new ArrayList<Integer>();
			for(Long pidTmp:pids){
				pidIntList.add(Integer.valueOf(pidTmp.toString()));
			}
			deepControl.cleanExByCkRule(conn, pidIntList, ruleList, ObjectName.IX_POI);
			/*JSONObject jsonReq=new JSONObject();
			jsonReq.put("subtaskId", jobInfo.getTaskId());
			jsonReq.put("pids", myRequest.getPids());
			jsonReq.put("ckRules", myRequest.getRules());
			jsonReq.put("checkType", 1);
			deepControl.cleanCheck(jsonReq, jobInfo.getUserId());*/
			//获取log
			Map<Long, List<LogDetail>> logs = PoiLogDetailStat.loadByColEditStatus(conn, pids,
					2,84,"poi_address","addrSplit");
			Set<String> tabNames=getChangeTableSet(logs);
			//获取poi对象			
			Map<Long, BasicObj> objs = ObjBatchSelector.selectByPids(conn, ObjectName.IX_POI, tabNames, false,
					pids, false, false);
			//将poi对象与履历合并起来
			ObjHisLogParser.parse(objs, logs);
			//构造检查参数，执行检查
			OperationResult operationResult=new OperationResult();
			Map<String,Map<Long,BasicObj>> objsMap=new HashMap<String, Map<Long,BasicObj>>();
			objsMap.put(ObjectName.IX_POI, objs);
			operationResult.putAll(objsMap);
			
			CheckCommand checkCommand=new CheckCommand();
			checkCommand.setRuleIdList(ruleList);
			
			Check check=new Check(conn, operationResult);
			check.operate(checkCommand);
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}finally{
			DbUtils.commitAndCloseQuietly(conn);
		}
	}
	/**
	 * 分析履历，将履历中涉及的变更过的子表集合返回
	 * @param logs
	 * @return [IX_POI_NAME,IX_POI_ADDRESS]
	 */
	private Set<String> getChangeTableSet(Map<Long, List<LogDetail>> logs) {
		Set<String> subtables=new HashSet<String>();
		if(logs==null || logs.size()==0){return subtables;}
		String mainTable="IX_POI";
		for(Long objId:logs.keySet()){
			List<LogDetail> logList = logs.get(objId);
			for(LogDetail logTmp:logList){
				String tableName = logTmp.getTbNm();
				if(!mainTable.equals(tableName)){subtables.add(tableName);}
			}
		}
		return subtables;
	}

	/**
	 * 获取精编检查对象pid
	 * 1.pids有值，则直接针对改pid进行检查
	 * 2.pids无值,查询job用户，子任务，一级项，二级项对应的待作业，已作业状态的poi列表
	 * @param conn
	 * @param myRequest
	 * @throws JobException
	 */
	private List<Long> getCheckPidList(Connection conn) throws JobException {
		try{
			List<Long> pids = new ArrayList<Long>();
			String sql="SELECT DISTINCT P.PID"
					+ "  FROM POI_COLUMN_STATUS P, POI_COLUMN_WORKITEM_CONF C"
					+ " WHERE P.WORK_ITEM_ID = C.WORK_ITEM_ID"
					+ "   AND C.CHECK_FLAG IN (1, 2)"
					+ "   AND C.FIRST_WORK_ITEM = 'poi_address'"
					+ "   AND P.HANDLER="+2
					+ "   AND P.TASK_ID="+84
					+ "   AND P.FIRST_WORK_STATUS IN (1,2)";
			//若针对二级项进行自定义检查，则检查对象应该是二级项状态为待作业/已作业状态
			sql+="   AND C.SECOND_WORK_ITEM = 'addrSplit'"
					+"   AND P.SECOND_WORK_STATUS =2";
			QueryRunner run=new QueryRunner();
			pids=run.query(conn, sql,new ResultSetHandler<List<Long>>(){

				@Override
				public List<Long> handle(ResultSet rs) throws SQLException {
					List<Long> pids =new ArrayList<Long>();
					while (rs.next()) {
						pids.add(rs.getLong("PID"));						
					}
					return pids;
				}});
			return pids;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}
	}

	/**
	 * 获取精编自定义检查规则
	 * 1.参数request中rules有值，则直接返回
	 * 2.rule没值，通过request的FirstWorkItem参数获取一级项对应的自定义检查规则列表
	 * 
	 * poi精编按照一级项获取检查规则，poi精编深度信息按照二级项获取检查规则
	 * @param conn
	 * @param myRequest
	 * @throws JobException
	 */
	private List<String>  getCheckRuleList(Connection conn) throws JobException{
		try{
			List<String> rules = null;
			String sql="SELECT DISTINCT WORK_ITEM_ID"
					+ "  FROM POI_COLUMN_WORKITEM_CONF C"
					+ " WHERE C.FIRST_WORK_ITEM = 'poi_address'"
					+ "   AND CHECK_FLAG IN (2, 3)";
			//poi精编按照一级项获取检查规则，poi精编深度信息按照二级项获取检查规则
			QueryRunner run=new QueryRunner();
			rules=run.query(conn, sql, new ResultSetHandler<List<String>>(){

				@Override
				public List<String> handle(ResultSet rs) throws SQLException {
					List<String> rules=new ArrayList<String>();
					while(rs.next()){
						rules.add(rs.getString("WORK_ITEM_ID"));
					}
					return rules;
				}});
			return rules;
		}catch(Exception e){
			DbUtils.rollbackAndCloseQuietly(conn);
			throw new JobException(e);
		}
	}
	
}
