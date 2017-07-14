package com.navinfo.dataservice.engine.man.job.Day2Month;

import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import org.apache.log4j.Logger;

/**
 * Created by wangshishuai3966 on 2017/7/14.
 */
public class CloseMeshPhase extends JobPhase {
    private static Logger log = LoggerRepos.getLogger(CloseMeshPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.SYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception {


        return null;
    }
}
