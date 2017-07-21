package com.navinfo.dataservice.job;

import java.sql.Connection;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONArray;

public class MidTask2QuickJob  extends AbstractJob{

	
	public MidTask2QuickJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		try {
			MidTask2QuickJobRequest request = (MidTask2QuickJobRequest) this.request;
			
			int dbId = request.getDbId();
			JSONArray pois = request.getPois();
			JSONArray tips = request.getTips();
			int subtaskId = request.getSubtaskId();
			int taskId = request.getTaskId();
			
			TaskService.getInstance().batchQuickTask(dbId, subtaskId, taskId, pois, tips);
			
		}catch(Exception e){
			log.error("MidTask2QuickJob异常:"+e.getMessage(), e);
		}
	}

}
