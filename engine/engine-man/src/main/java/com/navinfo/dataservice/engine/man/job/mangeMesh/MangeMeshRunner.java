package com.navinfo.dataservice.engine.man.job.mangeMesh;

import com.navinfo.dataservice.engine.man.job.JobRunner;
import com.navinfo.dataservice.engine.man.job.bean.JobType;
import com.navinfo.dataservice.engine.man.job.medium2quick.TaskMedium2QuickPhase;

public class MangeMeshRunner extends JobRunner{

	@Override
	public void prepare() {
		MangeMeshPhase mangeMeshPhase = new MangeMeshPhase();
        this.addPhase(mangeMeshPhase);		
	}

	@Override
	public void initJobType() {
		this.jobType = JobType.MANGE_MESH;		
	}

}
