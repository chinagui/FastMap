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
	 * @param checkType 检查类型（0 poi行编，1poi精编, 2道路,3 道路名）
	 * @return
	 * @throws Exception 
	 */
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
		if(checkType == 3){  //道路名检查 ,直接调元数据库 全表检查		
			//System.out.println("checkType == 3");
			DatahubApi datahub = (DatahubApi) ApplicationContextUtil
					.getBean("datahubApi");
			DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
			Integer metaDbid = metaDb.getDbId();
			if(metaDbid != null && metaDbid >0){
			//System.out.println("metaDbid: "+metaDbid);
			JSONObject metaValidationRequestJSON=new JSONObject();
			metaValidationRequestJSON.put("executeDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("kdbDBId", metaDbid);//元数据库dbId
			metaValidationRequestJSON.put("ruleIds", ruleList);
			metaValidationRequestJSON.put("timeOut", 0);
			jobId=apiService.createJob("checkCore", metaValidationRequestJSON, userId,subtaskId, "元数据库检查");
			//System.out.println("jobId == "+jobId);
			}
		}else if(checkType==2 ||checkType ==0){//道路
			List<Integer> grids= subtaskObj.getGridIds();
			JSONObject validationRequestJSON=new JSONObject();
			validationRequestJSON.put("grids", grids);
			validationRequestJSON.put("rules", ruleList);
			validationRequestJSON.put("targetDbId", dbId);
			jobId=apiService.createJob("gdbValidation", validationRequestJSON, userId,subtaskId, "检查");
		}//20161214 by zxy,目前行编按需检查规则未实现，不能使用新框架进行检查，暂时行编自定义检查还是通过cop检查执行，
		//但是相关job接口已ok，检查规则实现后，开放即可
		/*else if(checkType ==0){//checkType ==0poi行编
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
		}*/
		else if (checkType ==1){//checkType ==1poi精编
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
				firstWorkItem = jsonReq.getString("secondWorkItem");
			}
			int status=1;
			if (jsonReq.containsKey("status")) {
				status = jsonReq.getInt("status");
			}
			poiRequestJSON.put("secondWorkItem", secondWorkItem);
			jobId=apiService.createJob("poiColumnValidation", poiRequestJSON, userId,subtaskId, "检查");
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

}
