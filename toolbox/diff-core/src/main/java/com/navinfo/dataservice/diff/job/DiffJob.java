package com.navinfo.dataservice.diff.job;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/**
 * 左表为修改后的表，右表为修改前的表
 */
public class DiffJob extends AbstractJob{

	public DiffJob(JobInfo jobInfo) {
		super(jobInfo);
	}

	@Override
	public void volidateRequest() throws JobException {
		//to do
	}
	
	@Override
	public void execute() throws JobException{
		DiffTool tool = null;
		try {
			DiffJobRequest req = (DiffJobRequest)request;
			tool = DiffToolFactory.getInstance().create(req);
			String initStr = tool.init();
			log.info("初始化："+initStr);
			response(initStr,null);
			String diffStr = tool.diff();
			response(diffStr,null);
			log.info("差分："+diffStr);
			String actId = tool.writeLog(jobInfo.getUserId(), "Diff"+jobInfo.getId(), jobInfo.getTaskId());
			response("写履历完成。",null);
			log.info("写履历完成。");
			this.exeResultMsg="本次差分履历action_id为："+actId+"。";
		}catch(Exception e){
			log.error(e.getMessage(), e);
			throw new JobException(e.getMessage(),e);
		}finally{
			if(tool!=null){
				tool.releaseResources();
			}
		}
	}
}
