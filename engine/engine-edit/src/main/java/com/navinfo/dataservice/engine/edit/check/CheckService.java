package com.navinfo.dataservice.engine.edit.check;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

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
	 * @param checkType 检查类型（0 poi行编，1poi精编, 2道路）
	 * @return
	 * @throws Exception 
	 */
	public long checkRun(int subtaskId,long userId,int checkType,String ckRules) throws Exception{
		long jobId=0;
		
		ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
		Subtask subtaskObj=manApi.queryBySubtaskId(subtaskId);
		if (subtaskObj == null) {
			throw new Exception("subtaskid未找到数据");
		}
		
		int dbId=subtaskObj.getDbId();
		List<Integer> grids= subtaskObj.getGridIds();
		
		List<String> ruleList=new ArrayList<String>();
		
		if (!ckRules.isEmpty()) {
			String[] rules = ckRules.split(",");
			for (String rule:rules) {
				ruleList.add(rule);
			}
		} else {
			//TODO 根据checkType获取 规则 号 
			ruleList.add("1");
		}
		
		JSONObject validationRequestJSON=new JSONObject();
		validationRequestJSON.put("grids", grids);
		validationRequestJSON.put("rules", ruleList);
		validationRequestJSON.put("targetDbId", dbId);
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		jobId=apiService.createJob("gdbValidation", validationRequestJSON, userId, "检查");
		
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

}
