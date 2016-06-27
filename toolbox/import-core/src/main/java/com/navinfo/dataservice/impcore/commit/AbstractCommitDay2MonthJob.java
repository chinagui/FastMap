package com.navinfo.dataservice.impcore.commit;

import com.navinfo.dataservice.api.job.model.JobInfo;
import com.navinfo.dataservice.jobframework.exception.JobException;
import com.navinfo.dataservice.jobframework.runjob.AbstractJob;

/*
 * @author MaYunFei
 * 2016年6月14日
 * 描述：import-coreCommit.java
 */
public abstract class AbstractCommitDay2MonthJob extends AbstractJob{
	public AbstractCommitDay2MonthJob(JobInfo jobInfo) {
		super(jobInfo);
		// TODO Auto-generated constructor stub
	}
	/* 给定grid列表；
	 * 根据grid计算出对应的大区库，并将对应大区日库中grid范围内的履历刷到月库；
	 * 刷履历过程中，如果出现异常，需要跳过异常继续刷其他的履历；
	 * 出现异常的grid需要给grid打标记为"日落月失败"；
	 * @see com.navinfo.dataservice.jobframework.runjob.AbstractJob#execute()
	 */
	@Override
	public void execute() throws JobException {
		
	}
}

