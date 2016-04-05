package com.navinfo.dataservice.expcore;

import java.util.concurrent.CountDownLatch;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/** 
* @ClassName: GdbExpJob 
* @author Xiao Xiaowen 
* @date 2016年3月29日 下午4:23:28 
* @Description: TODO
*/
public class GdbExpJob extends AbstractJob {

	public GdbExpJob(JobInfo jobInfo,CountDownLatch doneSignal){
		super(jobInfo,doneSignal);
	}
	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.AbstractJob#volidateRequest()
	 */
	@Override
	public void volidateRequest() throws JobException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see com.navinfo.dataservice.jobframework.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		// TODO Auto-generated method stub
		startNewStep(10,"开始导出第一个库");
		Exporter2OracleByScripts fullCoy=null;
		try{
			fullCoy.execute();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		startNewStep(20,"开始复制第二个库");

		startNewStep(90,"开始差分");

	}

}
