package com.navinfo.dataservice.engine.edit.check;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import com.navinfo.dataservice.api.job.iface.JobApi;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;

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
	public long checkRun(int subtaskId,long userId,int checkType) throws Exception{
		long jobId=0;
		
		ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
		Subtask subtaskObj=manApi.queryBySubtaskId(subtaskId);
		int dbId=subtaskObj.getDbId();
		List<Integer> grids= subtaskObj.getGridIds();
		//TODO 根据checkType获取 规则 号 
		List<String> rules=new ArrayList<String>();
		rules.add("1");
		
		JSONObject validationRequestJSON=new JSONObject();
		validationRequestJSON.put("grids", grids);
		validationRequestJSON.put("rules", rules);
		validationRequestJSON.put("targetDbId", dbId);
		JobApi apiService=(JobApi) ApplicationContextUtil.getBean("jobApi");
		jobId=apiService.createJob("gdbValidation", validationRequestJSON, userId, "检查");
		
		return jobId;
	}

}
