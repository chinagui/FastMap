package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class CreateCMSTaskPhase extends JobPhase {

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.SYNC;
    }

    @Override
    public void run() {


    }
}
