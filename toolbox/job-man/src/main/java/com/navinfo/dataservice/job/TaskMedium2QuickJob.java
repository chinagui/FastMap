package com.navinfo.dataservice.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.api.man.iface.ManApi;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.fcc.tips.TipsOperator;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 * 1.中线任务转快线任务。
 *   fcc的转换调用http接口
 * 2.修改对应任务状态
 *
 */
public class TaskMedium2QuickJob extends AbstractJob{

	public TaskMedium2QuickJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		TaskMedium2QuickJobRequest jobRequest = (TaskMedium2QuickJobRequest)request;
		ManApi api = (ManApi) ApplicationContextUtil.getBean("manApi");
		long jobId = jobRequest.getJobId();
		long phaseId = jobRequest.getPhaseId();
		try {
    		int dbId = jobRequest.getDbId();
    		JSONArray pois = jobRequest.getPids();
    		JSONArray tips = jobRequest.getTips();
    		int subtaskId = jobRequest.getSubtaskId();
    		int taskId = jobRequest.getTaskId();
    			
    		//批tips的快线任务号
    		if(tips != null && tips.size()>0){
    			List<String> tipsPids = new ArrayList<String>(); 
    			for(Object tipRowkey : tips){ 
    				tipsPids.add(tipRowkey.toString()); 
    			}
    			TipsOperator tipsOperator = new TipsOperator();
        		tipsOperator.batchQuickTask(taskId, subtaskId, tipsPids);
     		}
    		//poi批快线任务号
    		if(pois != null && pois.size() > 0){
    			TaskService.getInstance().batchQuickTask(dbId, subtaskId, taskId, pois, tips);
    		}
    		api.updateJobProgress(phaseId, 2, "jobId:"+jobId);
    		log.info("jobId:"+jobId+",phaseId:"+phaseId);
		} catch (Exception e) {
			try {
				api.updateJobProgress(phaseId, 3, "jobId:"+jobId);
			} catch (Exception e1) {
				log.error(e.getMessage(), e);
				throw new JobException(e);
			}
			log.error(e.getMessage(), e);
			throw new JobException(e);
		}
	}

}
