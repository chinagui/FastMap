package com.navinfo.dataservice.engine.man.job.medium2quick;

import com.navinfo.dataservice.engine.man.job.JobRunner;
import com.navinfo.dataservice.engine.man.job.bean.JobType;

public class TaskMedium2QuickRunner extends JobRunner{

	@Override
	public void prepare() {
		TaskMedium2QuickPhase taskMedium2QuickPhase = new TaskMedium2QuickPhase();
        this.addPhase(taskMedium2QuickPhase);		
	}

	@Override
	public void initJobType() {
		this.jobType = JobType.MID2QUICK;		
	}

}
