package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.navinfo.dataservice.engine.man.job.JobRunner;
import com.navinfo.dataservice.engine.man.job.bean.JobType;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class Tips2MarkJobRunner extends JobRunner {

    @Override
    public void initJobType() {
        this.jobType = JobType.TiPS2MARK;
    }

    @Override
    public void prepare() {
        Tips2MarkPhase tips2MarkPhase = new Tips2MarkPhase();
        CreateCMSTaskPhase createCMSTaskPhase = new CreateCMSTaskPhase();

        this.addPhase(tips2MarkPhase);
        this.addPhase(createCMSTaskPhase);
    }

}
