package com.navinfo.dataservice.engine.edit.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.api.man.model.Subtask;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ArrayUtil;

public class CheckService {

	public CheckService() {}
	
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
	public int checkRun(int dbId,int subtaskId,long userId,int checkType) throws Exception{
		int jobId=0;
		
		ManApi manApi=(ManApi) ApplicationContextUtil.getBean("manApi");
		Subtask subtaskObj=manApi.queryBySubtaskId(subtaskId);
		String[] gridsStr= subtaskObj.getGridIds();
		List<Integer> grids=ArrayUtil.convertList(Arrays.asList(gridsStr));
		//todo 根据checkType获取 规则 号 
		List<String> rules=new ArrayList<String>();
		rules.add("1");
		/*
		JSONObject jobReq=new JSONObject();
		jobReq.put("targetDbId", dbId);
		jobReq.put("gridIds", gridIds);
				
		AccessToken tokenObj=(AccessToken) request.getAttribute("token");
		long userId=tokenObj.getUserId();
		//long userId=2;
		
		JSONObject validationRequestJSON=new JSONObject();
		validationRequestJSON.put("grids", releaseJobRequest.getGridIds());
		validationRequestJSON.put("rules", releaseJobRequest.getCheckRuleList());
		validationRequestJSON.put("targetDbId", dbId);
		validationRequestJSON.put("createValDb", releaseJobRequest.createDbJSON("validation temp db"));
		validationRequestJSON.put("expValDb", releaseJobRequest.expDbJSON());
		
		AbstractJobRequest gdbValidationRequest=JobCreateStrategy.createJobRequest("gdbValidation", validationRequestJSON);
		
		JobApiService apiService=(JobApiService) ApplicationContextUtil.getBean("jobApiService");
		long jobId=apiService.createJob("gdbValidation", jobReq, userId, "检查");*/
		
		return jobId;
	}

}
