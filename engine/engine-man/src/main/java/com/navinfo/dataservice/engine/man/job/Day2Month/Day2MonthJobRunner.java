package com.navinfo.dataservice.engine.man.job.Day2Month;

import com.navinfo.dataservice.engine.man.job.JobRunner;
import com.navinfo.dataservice.engine.man.job.bean.JobType;

/**
 * Created by wangshishuai3966 on 2017/7/14.
 */
public class Day2MonthJobRunner extends JobRunner {

    @Override
    public void initJobType() {
        this.jobType = JobType.DAY2MONTH;
    }

    @Override
    public void prepare() {
        Day2MonthPhase day2MonthPhase = new Day2MonthPhase();
        CloseMeshPhase closeMeshPhase = new CloseMeshPhase();

        this.addPhase(day2MonthPhase);
        this.addPhase(closeMeshPhase);
    }
}
