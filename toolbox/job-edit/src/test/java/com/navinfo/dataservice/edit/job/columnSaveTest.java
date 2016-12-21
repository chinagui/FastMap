package com.navinfo.dataservice.edit.job;

import java.lang.reflect.Method;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.navinfo.dataservice.api.edit.iface.EditApi;
import com.navinfo.dataservice.api.edit.upload.EditJson;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.column.job.ColumnCoreOperation;
import com.navinfo.dataservice.column.job.ColumnSaveJobRequest;
import com.navinfo.dataservice.commons.database.ConnectionUtil;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.token.AccessToken;
import com.navinfo.dataservice.control.column.core.DeepCoreControl;
import com.navinfo.dataservice.dao.glm.model.poi.deep.PoiColumnOpConf;
import com.navinfo.dataservice.dao.glm.selector.poi.deep.IxPoiOpConfSelector;
import com.navinfo.dataservice.dao.plus.obj.BasicObj;
import com.navinfo.dataservice.dao.plus.operation.OperationResult;
import com.navinfo.dataservice.dao.plus.selector.ObjSelector;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.Batch;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.batch.BatchCommand;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.Check;
import com.navinfo.dataservice.engine.editplus.batchAndCheck.check.CheckCommand;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportor;
import com.navinfo.dataservice.engine.editplus.operation.imp.DefaultObjImportorCommand;
import com.navinfo.dataservice.jobframework.exception.JobCreateException;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJobRequest;
import com.navinfo.dataservice.jobframework.runjob.JobCreateStrategy;
import com.navinfo.dataservice.jobframework.service.JobApiImpl;

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
		String param = "{\"taskId\":84,\"secondWorkItem\":\"nameUnify\",\"dataList\":[{\"command\":\"UPDATE\",\"dbId\":19,\"type\":\"IXPOI\",\"objId\":335,\"data\":{\"names\":[{\"name\":\"北京华军中医医院111\",\"rowId\":\"3AE1FCF65D1F92F7E050A8C08304EE4C\",\"pid\":335,\"objStatus\":\"UPDATE\"}],\"pid\":335}}]}";
		Connection conn = DBConnector.getInstance().getConnectionById(17);
		JSONObject dataJson = JSONObject.fromObject(param);
		DefaultObjImportor importor = new DefaultObjImportor(conn,null);
		EditJson editJson = new EditJson();
		editJson.addJsonPoi(dataJson.getJSONArray("dataList"));
		DefaultObjImportorCommand command = new DefaultObjImportorCommand(editJson);
		importor.operate(command);
		conn.commit();
		
	}
	
	
	public void executeSave(String param,int userId) throws JobException {
		
		ManApi apiService=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		List<Integer> pidList = new ArrayList<Integer>();
		
		Connection conn = null;
		try {
			param.replace('/', '"');
			param = "{" + param + "}";
			JSONObject paramJson = JSONObject.fromObject(param);
			int taskId = paramJson.getInt("taskId");
			JSONArray data = paramJson.getJSONArray("data");
			String secondWorkItem = paramJson.getString("secondWorkItem");
			
			
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
			PoiColumnOpConf columnOpConf = ixPoiOpConfSelector.getDeepOpConf("",secondWorkItem, type);
			
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
			}
			
			
			// 重分类
			if (columnOpConf.getSaveExeclassify()==1) {
				HashMap<String,Object> classifyMap = new HashMap<String,Object>();
				classifyMap.put("userId", userId);
				classifyMap.put("ckRules", columnOpConf.getSaveCkrules());
				classifyMap.put("classifyRules", columnOpConf.getSaveClassifyrules());
				
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
	
	public void setAttrValue(String attName,Object attValue)throws JobCreateException{
		if(StringUtils.isEmpty(attName)||attValue==null||(attValue instanceof JSONNull)){
			return;
		}
		try{
			String methodName = "set"+(char)(attName.charAt(0)-32)+attName.substring(1, attName.length());
			Class[] argtypes = null;//默认String
			
			if(attValue instanceof String){
				argtypes = new Class[]{String.class};
			}else if(attValue instanceof Integer){
				argtypes= new Class[]{int.class};
			}else if(attValue instanceof Double){
				argtypes = new Class[]{double.class};
			}else if(attValue instanceof Boolean){
				argtypes= new Class[]{boolean.class};
			}else if(attValue instanceof JSONArray){
				JSONArray attArr = (JSONArray)attValue;
				if(attArr.size()>0){
					Object subObj = attArr.get(0);
					if(subObj instanceof String
							||subObj instanceof Integer
							||subObj instanceof Double
							||subObj instanceof Boolean
							){
						argtypes= new Class[]{List.class};
					}else if(subObj instanceof JSONObject){
						argtypes= new Class[]{Map.class};
						Map newAttValue = new HashMap();
						for(Object o:attArr){
							JSONObject jo = (JSONObject)o;
							Object key = jo.get("key");
							Object value = jo.get("value");
							if(key!=null&&value!=null){
								newAttValue.put(key, value);
							}
						}
						attValue=newAttValue;
					}else{
						throw new Exception(attName+"为数组类型，其内部格式为不支持的json结构");
					}
				}else{
					return;
				}
				
			}else if(attValue instanceof JSONObject){
				argtypes= new Class[]{JSONObject.class};
			}
			Method method = this.getClass().getMethod(methodName, argtypes);
			method.invoke(this, attValue);
		}catch(Exception e){
			throw new JobCreateException("Request解析过程中可能未找到方法,原因为:"+e.getMessage(),e);
		}
	}
}
