package com.navinfo.dataservice.engine.edit.batch;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.database.MultiDataSourceFactory;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.dao.batch.selector.BatchRuleSelector;
import com.navinfo.dataservice.dao.batch.selector.BatchSuiteSelector;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BatchService {

	public BatchService() {
		// TODO Auto-generated constructor stub
	}
	
	private static class SingletonHolder{
		private static final BatchService INSTANCE =new BatchService();
	}
	public static BatchService getInstance(){
		return SingletonHolder.INSTANCE;
	}
	
	/**
	 * 批处理规则查询
	 * @param request
	 * @return
	 * @author wangdongbin
	 */
	public JSONArray getBatchRules(int pageSize,int pageNum, int type) throws Exception {
		
		Connection conn = null;
		
		try {
			conn = MultiDataSourceFactory.getInstance().getSysDataSource().getConnection();
			
			BatchSuiteSelector batchSuiteSelector = new BatchSuiteSelector(conn);
			
			JSONArray suiteArray = batchSuiteSelector.getSuite(pageSize, pageNum, type);
			
			BatchRuleSelector batchRuleSelector = new BatchRuleSelector(conn);
			
			return batchRuleSelector.getRules(suiteArray);
		} catch (Exception e) {
			throw e;
		} finally {
			DbUtils.closeQuietly(conn);
		}
	}
	
	/**
	 * 
	 * @param dbId
	 * @param subTaskId
	 * @param userId
	 * @return
	 * @throws Exception 
	 */
	public long batchRun(long userId,JSONObject jsonReq) throws Exception{
		long jobId=0;
		
		int subtaskId=jsonReq.getInt("subtaskId");
		int batchType=jsonReq.getInt("batchType");
		String batchRules = "";
		if (jsonReq.containsKey("batchRules")) {
			batchRules = jsonReq.getString("batchRules");
		}
		ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
		Subtask subtaskObj=manApi.queryBySubtaskId(subtaskId);
		int dbId=subtaskObj.getDbId();
		List<Integer> grids= (List<Integer>) subtaskObj.getGridIds().keySet();
		
		List<String> ruleList=new ArrayList<String>();
		
		if (!batchRules.isEmpty()) {
			String[] rules = batchRules.split(";");
			for (String rule:rules) {
				ruleList.add(rule);
			}
		} else {
			//TODO 根据checkType获取 规则 号 
			ruleList.add("1");
		}
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		if(batchType==9){//测试用poi月编poi批处理
			JSONObject batchRequestJSON=new JSONObject();
			batchRequestJSON.put("pids", jsonReq.getJSONArray("pids"));
			batchRequestJSON.put("batchRules", ruleList);
			batchRequestJSON.put("targetDbId", dbId);
			jobId=apiService.createJob("editPoiBatchPlus", batchRequestJSON, userId,subtaskId,"POI批处理");			
		}else{
			JSONObject batchRequestJSON=new JSONObject();
			batchRequestJSON.put("grids", grids);
			batchRequestJSON.put("rules", ruleList);
			batchRequestJSON.put("targetDbId", dbId);
			jobId=apiService.createJob("gdbBatch", batchRequestJSON, userId,subtaskId,"批处理");}
		
		return jobId;
	}	
}
