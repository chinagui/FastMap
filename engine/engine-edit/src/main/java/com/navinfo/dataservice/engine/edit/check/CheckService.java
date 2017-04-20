package com.navinfo.dataservice.engine.edit.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.StringUtils;
import com.navinfo.dataservice.dao.check.selector.CkRuleSelector;
import com.navinfo.dataservice.dao.check.selector.CkSuiteSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CheckService {

	public CheckService() {
		// TODO Auto-generated constructor stub
	}
	
	private static class SingletonHolder{
		private static final CheckService INSTANCE =new CheckService();
	}
	public static CheckService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 
	 * @param dbId
	 * @param subTaskId
	 * @param userId
	 * @param checkType 检查类型(1 poi粗编 ;2 poi精编 ; 3 道路粗编 ; 4道路精编 ; 5道路名 ; 6 其他;)
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public long checkRun(int subtaskId,long userId,int checkType,JSONObject jsonReq) throws Exception{
		long jobId=0;
		
		ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
		
		Subtask subtaskObj=manApi.queryBySubtaskId(subtaskId);
		if (subtaskObj == null) {
			throw new Exception("subtaskid未找到数据");
		}	
		int dbId=subtaskObj.getDbId();
		
		String ckRules = "";		
		if (jsonReq.containsKey("ckRules")) {
			ckRules = jsonReq.getString("ckRules");
		}
		
		List<String> ruleList=new ArrayList<String>();
		if (!ckRules.isEmpty()) {
			String[] rules = ckRules.split(",");
			for (String rule:rules) {
				ruleList.add(rule);
			}
		}
		
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		if(checkType == 5){  //道路名检查 ,直接调元数据库 子版本检查		
			DatahubApi datahub = (DatahubApi) ApplicationContextUtil
					.getBean("datahubApi");
			DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
			Integer metaDbid = metaDb.getDbId();
			if(metaDbid != null && metaDbid >0){
				System.out.println(" begin 道路名子版本检查 ");
				JSONObject paramsObj = new JSONObject();
				if(jsonReq.containsKey("params") && jsonReq.getJSONObject("params") != null ){
					paramsObj = jsonReq.getJSONObject("params");
				}
				
				String name = "" ;
				if(paramsObj.containsKey("name") && paramsObj.getString("name") != null 
						&& StringUtils.isNotEmpty(paramsObj.getString("name")) && !paramsObj.getString("name").equals("null")){
					name = paramsObj.getString("name");
				}
				String nameGroupid = "";
				if(paramsObj.containsKey("nameGroupid") && paramsObj.getString("nameGroupid") != null 
						&& StringUtils.isNotEmpty(paramsObj.getString("nameGroupid")) && !paramsObj.getString("nameGroupid").equals("null")){
					nameGroupid = paramsObj.getString("nameGroupid");
				}	
				String adminId = "";
				if(paramsObj.containsKey("adminId") && paramsObj.getString("adminId") != null 
						&& StringUtils.isNotEmpty(paramsObj.getString("adminId")) && !paramsObj.getString("adminId").equals("null")){
					adminId = paramsObj.getString("adminId");
				}
				String roadTypes = "";
				if(paramsObj.containsKey("roadTypes") && paramsObj.getJSONArray("roadTypes") != null && paramsObj.getJSONArray("roadTypes").size() > 0 ){
					JSONArray arr = paramsObj.getJSONArray("roadTypes");
					roadTypes = arr.join(",");
				}
				
				String jobName = "";
				if(jsonReq.containsKey("jobName") && jsonReq.getString("jobName") != null 
						&& StringUtils.isNotEmpty(jsonReq.getString("jobName")) && !jsonReq.getString("jobName").equals("null")){
					jobName = "rdName:"+jsonReq.getString("jobName");
				}else{
					jobName = "rdName:"+"元数据库检查";
				}
			
				JSONObject validationRequestJSON=new JSONObject();
				validationRequestJSON.put("name", name);
				validationRequestJSON.put("nameGroupid", nameGroupid);
				validationRequestJSON.put("adminId", adminId);
				validationRequestJSON.put("roadTypes", roadTypes);
				validationRequestJSON.put("rules", ruleList);
				jobId=apiService.createJob("metaValidation", validationRequestJSON, userId, subtaskId, jobName);
				
		/*	//System.out.println("metaDbid: "+metaDbid);
			JSONObject metaValidationRequestJSON=new JSONObject();
			metaValidationRequestJSON.put("executeDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("kdbDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("ruleIds", ruleList);
			metaValidationRequestJSON.put("timeOut", 600);
			jobId=apiService.createJob("checkCore", metaValidationRequestJSON, userId,subtaskId, jobName);
			//System.out.println("jobId == "+jobId);
*/			}
		}else if(checkType==3 ||checkType ==4 || checkType ==1){//道路 + poi粗编
			List<Integer> grids= new ArrayList<Integer>();
			if(subtaskObj.getGridIds() != null && subtaskObj.getGridIds().size() >0){
				 grids= subtaskObj.getGridIds();
			}
//			if(subtaskObj.getGridIds().keySet() != null && subtaskObj.getGridIds().keySet().size() >0){
//				 grids= (List<Integer>) subtaskObj.getGridIds().keySet();
//			}
			
			JSONObject validationRequestJSON=new JSONObject();
			validationRequestJSON.put("grids", grids);
			validationRequestJSON.put("rules", ruleList);
			validationRequestJSON.put("targetDbId", dbId);
			jobId=apiService.createJob("gdbValidation", validationRequestJSON, userId,subtaskId, "检查");
		}//20161214 by zxy,目前行编按需检查规则未实现，不能使用新框架进行检查，暂时行编自定义检查还是通过cop检查执行，
		//但是相关job接口已ok，检查规则实现后，开放即可
		else if(checkType ==9){//checkType ==1poi行编
			JSONObject poiRequestJSON=new JSONObject();
//			*checkType ==0poi行编 
//			 * 必传参数：subtaskId,ckRules
//			 * 测试用参数：pids
			JSONArray pids = null;
			if (jsonReq.containsKey("pids")) {
				pids = jsonReq.getJSONArray("pids");
			}
			poiRequestJSON.put("pids", pids);
			poiRequestJSON.put("rules", ruleList);
			poiRequestJSON.put("targetDbId", dbId);
			jobId=apiService.createJob("poiRowValidation", poiRequestJSON, userId,subtaskId, "检查");
		}
		else if (checkType ==2){//checkType ==2poi精编
			JSONObject poiRequestJSON=new JSONObject();
			/*checkType ==1poi精编 
			 * 必传参数：subtaskId，firstWorkItem，secondWorkItem
			 * 测试用参数：pids,ckRules
			 */
			JSONArray pids = null;
			if (jsonReq.containsKey("pids")) {
				pids = jsonReq.getJSONArray("pids");
			}
			poiRequestJSON.put("pids", pids);
			poiRequestJSON.put("rules", ruleList);
			poiRequestJSON.put("targetDbId", dbId);
			
			String firstWorkItem = "";
			if (jsonReq.containsKey("firstWorkItem")) {
				firstWorkItem = jsonReq.getString("firstWorkItem");
			}
			poiRequestJSON.put("firstWorkItem", firstWorkItem);
			String secondWorkItem = "";
			if (jsonReq.containsKey("secondWorkItem")) {
				secondWorkItem = jsonReq.getString("secondWorkItem");
			}
			poiRequestJSON.put("secondWorkItem", secondWorkItem);
			int status=1;
			if (jsonReq.containsKey("status")) {
				status = jsonReq.getInt("status");
			}
			poiRequestJSON.put("status", status);
			jobId=apiService.createJob("poiColumnValidation", poiRequestJSON, userId,subtaskId, "检查");
		}
		
		return jobId;
	}
	
	/**
	 * @Title: metaCheckRun
	 * @Description: 元数据编辑平台检查
	 * @param userId
	 * @param checkType
	 * @param jsonReq
	 * @return
	 * @throws Exception  long
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月20日 下午3:25:35 
	 */
	public long metaCheckRun(long userId,int checkType,JSONObject jsonReq) throws Exception{
		long jobId=0;
		String ckRules = "";		
		if (jsonReq.containsKey("ckRules")) {
			ckRules = jsonReq.getString("ckRules");
		}
		
		List<String> ruleList=new ArrayList<String>();
		if (!ckRules.isEmpty()) {
			String[] rules = ckRules.split(",");
			for (String rule:rules) {
				ruleList.add(rule);
			}
		}
		
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		if(checkType == 7){  //道路名检查 ,直接调元数据库 全表检查		
			DatahubApi datahub = (DatahubApi) ApplicationContextUtil
					.getBean("datahubApi");
			DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
			Integer metaDbid = metaDb.getDbId();
			if(metaDbid != null && metaDbid >0){
				String jobName = "";
				if(jsonReq.containsKey("jobName") && jsonReq.getString("jobName") != null 
						&& StringUtils.isNotEmpty(jsonReq.getString("jobName")) && !jsonReq.getString("jobName").equals("null")){
					jobName = "rdName:"+jsonReq.getString("jobName");
				}else{
					jobName = "rdName:"+"元数据库检查";
				}
				
			//System.out.println("metaDbid: "+metaDbid);
			JSONObject metaValidationRequestJSON=new JSONObject();
			metaValidationRequestJSON.put("executeDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("kdbDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("ruleIds", ruleList);
			metaValidationRequestJSON.put("timeOut", 600);
			jobId=apiService.createJob("checkCore", metaValidationRequestJSON, userId, 0, jobName);
			//System.out.println("jobId == "+jobId);
			}
		}else if(checkType == 5){//道路名子版本检查
			
			System.out.println(" begin 道路名子版本检查 ");
			JSONObject paramsObj = new JSONObject();
			if(jsonReq.containsKey("params") && jsonReq.getJSONObject("params") != null ){
				paramsObj = jsonReq.getJSONObject("params");
			}
			
			String name = "" ;
			if(paramsObj.containsKey("name") && paramsObj.getString("name") != null 
					&& StringUtils.isNotEmpty(paramsObj.getString("name")) && !paramsObj.getString("name").equals("null")){
				name = paramsObj.getString("name");
			}
			String nameGroupid = "";
			if(paramsObj.containsKey("nameGroupid") && paramsObj.getString("nameGroupid") != null 
					&& StringUtils.isNotEmpty(paramsObj.getString("nameGroupid")) && !paramsObj.getString("nameGroupid").equals("null")){
				nameGroupid = paramsObj.getString("nameGroupid");
			}	
			String adminId = "";
			if(paramsObj.containsKey("adminId") && paramsObj.getString("adminId") != null 
					&& StringUtils.isNotEmpty(paramsObj.getString("adminId")) && !paramsObj.getString("adminId").equals("null")){
				adminId = paramsObj.getString("adminId");
			}
			String roadTypes = "";
			if(paramsObj.containsKey("roadTypes") && paramsObj.getJSONArray("roadTypes") != null && paramsObj.getJSONArray("roadTypes").size() > 0 ){
				JSONArray arr = paramsObj.getJSONArray("roadTypes");
				roadTypes = arr.join(",");
			}
			String jobName = "";
			if(jsonReq.containsKey("jobName") && jsonReq.getString("jobName") != null 
					&& StringUtils.isNotEmpty(jsonReq.getString("jobName")) && !jsonReq.getString("jobName").equals("null")){
				jobName = "rdName:"+jsonReq.getString("jobName");
			}
			
			System.out.println("name :"+name+" nameGroupid: "+nameGroupid+" adminId:"+adminId+" roadTypes:"+roadTypes+" jobName: "+jobName);
			JSONObject validationRequestJSON=new JSONObject();
			validationRequestJSON.put("name", name);
			validationRequestJSON.put("nameGroupid", nameGroupid);
			validationRequestJSON.put("adminId", adminId);
			validationRequestJSON.put("roadTypes", roadTypes);
			validationRequestJSON.put("rules", ruleList);
			jobId=apiService.createJob("metaValidation", validationRequestJSON, userId, 0, jobName);
		}
		return jobId;
	}
	
	/**
	 * 检查规则查询
	 * @param request
	 * @return
	 * @author wangdongbin
	 */
	public JSONArray getCkRules(int pageSize,int pageNum, int type) throws Exception {
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkSuiteSelector suiteSelector = new CkSuiteSelector(conn);
			
			JSONArray suiteArray = suiteSelector.getSuite(pageSize, pageNum, type);
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getRules(suiteArray);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * @Title: getCkRuleCodes
	 * @Description: 根据检查类型获取规则号
	 * @param type
	 * @return
	 * @throws Exception  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2016年11月24日 下午2:33:42 
	 */
	public JSONArray getCkRuleCodes( Integer type) throws Exception {
		
		Connection conn = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getRulesByType(type);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * @Title: getCkRuleCodes
	 * @Description: 根据规则号获取规则名称
	 * @param ruleCode
	 * @return
	 * @throws Exception  String
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月17日 下午3:12:14 
	 */
	public String getRuleNameById( String ruleCode) throws Exception {
		
		Connection conn = null;
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getRuleNameById(ruleCode);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * @Title: getCkSuites
	 * @Description: 获取某类检查的所有 suite
	 * @param type
	 * @return
	 * @throws Exception  JSONArray
	 * @throws 
	 * @author zl zhangli5174@navinfo.com
	 * @date 2017年4月19日 下午2:01:13 
	 */
	public JSONArray getCkSuites(int type) throws Exception {
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			System.out.println(conn);
			CkSuiteSelector suiteSelector = new CkSuiteSelector(conn);
			
			JSONArray suiteArray = suiteSelector.getSuite(type);
			
//			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			//return ckRuleSelector.getRules(suiteArray);
			return suiteArray;
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}

	public JSONArray getCkRulesBySuiteId(String suiteId) throws Exception {
		
		Connection conn = null;
		
		try {
			
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			CkRuleSelector ckRuleSelector = new CkRuleSelector(conn);
			
			return ckRuleSelector.getCkRulesBySuiteId(suiteId);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
}
