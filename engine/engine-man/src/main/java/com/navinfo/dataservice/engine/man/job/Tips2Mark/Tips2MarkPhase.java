package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class Tips2MarkPhase extends JobPhase {

    @Override
    public void initInvokeType(){
        this.invokeType = InvokeType.ASYNC;
    }

    @Override
    public void run() throws Exception {
        try {
            //更新状态为进行中
            this.updateStatus(JobProgressStatus.RUNNING);

            //获取初始参数
            JSONObject jobPara = job.getParameter();

            //获取上步执行结果
            String message = lastJobProgress.getMessage();

            //业务逻辑


            //如果调用了异步方法，则不更新状态；否则更新状态为执行成功
            this.updateStatus(JobProgressStatus.SUCCESS);

        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            this.updateStatus(JobProgressStatus.FAILURE);
            throw ex;
        }
    }
}
