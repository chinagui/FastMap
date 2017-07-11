package com.navinfo.dataservice.engine.man.job;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.engine.man.job.Tips2Mark.Tips2MarkJobRunner;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobOperator;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;

/**
 * Created by wangshishuai3966 on 2017/7/10.
 */
public class JobService {
    private Logger log = LoggerRepos.getLogger(JobService.class);
    private JobService(){}
    private static class SingletonHolder{
        private static final JobService INSTANCE =new JobService();
    }
    public static JobService getInstance(){
        return JobService.SingletonHolder.INSTANCE;
    }


    /**
     * 执行tips转mark
     * @param itemId    目标对象ID
     * @param itemType  目标对象类型（项目、任务、子任务、批次）
     * @param operator  执行人
     * @param isContinue 是否继续
     * @return jobId
     * @throws Exception
     */
    public long tips2Mark(long itemId, ItemType itemType, long operator, boolean isContinue, String parameter) throws Exception{
        try {
            Tips2MarkJobRunner runner = new Tips2MarkJobRunner();
            return runner.run(itemId, itemType, isContinue, operator, parameter);
        }catch (Exception e){
            log.error(e.getMessage(), e);
            throw new Exception("执行tips转mark失败，原因为:"+e.getMessage(),e);
        }
    }

    /**
     * 获取JOB每个步骤的执行状态
     * @param jobId
     * @return
     * @throws Exception
     */
    public JSONArray getJobProgress(long jobId) throws Exception{
        Connection conn=null;
        try{
            conn = DBConnector.getInstance().getManConnection();
            JobOperator jobOperator = new JobOperator(conn);
            return jobOperator.getJobProgressStatus(jobId);
        }catch (Exception e){
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("获取JOB状态失败，原因为:"+e.getMessage(),e);
        }finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }

    /**
     * 更新Job步骤的状态，并继续执行Job
     * @param phaseId
     * @param status
     * @param outParameter
     * @throws Exception
     */
    public void updateJobProgress(long phaseId, JobProgressStatus status, String outParameter) throws Exception{
        Connection conn=null;
        try{
            conn = DBConnector.getInstance().getManConnection();
            JobProgressOperator jobProgressOperator = new JobProgressOperator(conn);
            jobProgressOperator.updateStatus(phaseId, status, outParameter);



        }catch (Exception e){
            DbUtils.rollbackAndCloseQuietly(conn);
            log.error(e.getMessage(), e);
            throw new Exception("获取JOB状态失败，原因为:"+e.getMessage(),e);
        }finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
}
