package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class Tips2MarkPhase extends JobPhase {

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.ASYNC;
    }

    @Override
    public void run() throws Exception {
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            conn.setAutoCommit(false);
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgressOperator.updateStatus(jobProgress.getPhaseId(), JobProgressStatus.RUNNING);
            conn.commit();

            //获取初始参数
            JSONObject jobPara = job.getParameter();

            //获取上步执行结果
            String message = lastJobProgress.getMessage();

            //业务逻辑


            //如果调用了异步方法，则不更新状态；否则更新状态为执行成功
            jobProgressOperator.updateStatus(jobProgress.getPhaseId(), JobProgressStatus.SUCCESS);

        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                jobProgressOperator.updateStatus(jobProgress.getPhaseId(), JobProgressStatus.FAILURE);
            }
            throw ex;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
}
