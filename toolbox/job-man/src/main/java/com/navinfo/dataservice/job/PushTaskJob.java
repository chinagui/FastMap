package com.navinfo.dataservice.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.engine.man.task.TaskService;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;
import net.sf.json.JSONArray;
/**
 * 1.中线任务转快线任务。
 *   fcc的转换调用http接口
 * 2.修改对应任务状态
 *
 */
public class PushTaskJob extends AbstractJob{

	public PushTaskJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void execute() throws JobException {
		log.info("start");
		PushTaskJobRequest jobRequest = (PushTaskJobRequest)request;
		JSONArray taskIds=new JSONArray();
		taskIds.add(jobInfo.getTaskId());
		try {
			TaskService.getInstance().taskPushMsg(0, taskIds);
		} catch (Exception e) {
			log.error("任务发布失败", e);
			throw new JobException(e);
		}
    	log.info("end");
	}

}
