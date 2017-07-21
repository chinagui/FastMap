package com.navinfo.dataservice.engine.man.job.NoTask2Medium;

import com.navinfo.dataservice.engine.man.job.JobRunner;
import com.navinfo.dataservice.engine.man.job.Day2Month.CloseMeshPhase;
import com.navinfo.dataservice.engine.man.job.Day2Month.Day2MonthPhase;
import com.navinfo.dataservice.engine.man.job.bean.JobType;

public class NoTask2MediumJobRunner extends JobRunner {

	@Override
	public void prepare() {
		NoTask2MediumPhase noTask2MediumPhase = new NoTask2MediumPhase();
        this.addPhase(noTask2MediumPhase);
	}

	@Override
	public void initJobType() {
		this.jobType = JobType.NOTASK2MID;
	}

}
