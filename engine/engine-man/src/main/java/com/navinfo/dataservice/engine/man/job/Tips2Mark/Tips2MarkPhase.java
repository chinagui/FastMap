package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.alibaba.fastjson.JSONObject;
import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import com.navinfo.dataservice.engine.man.task.TaskService;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.Map;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class Tips2MarkPhase extends JobPhase {
    private static Logger log = LoggerRepos.getLogger(Tips2MarkPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.SYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception {
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.RUNNING);
            conn.commit();

            //业务逻辑
//            Map<String, Object> cmsInfo = Tips2MarkUtils.getCmsInfo(conn, jobRelation.getItemId());
//            net.sf.json.JSONObject par=new net.sf.json.JSONObject();
//            par.put("gdbid", cmsInfo.get("dbId"));
//            DatahubApi datahub = (DatahubApi) ApplicationContextUtil
//                    .getBean("datahubApi");
//            DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
//            par.put("au_db_ip", auDb.getDbServer().getIp());
//            par.put("au_db_username", auDb.getDbUserName());
//            par.put("au_db_password", auDb.getDbUserPasswd());
//            par.put("au_db_sid",auDb.getDbServer().getServiceName());
//            par.put("au_db_port",auDb.getDbServer().getPort());
//            par.put("types","");
//            par.put("phaseId",job.getJobId());
//            par.put("collectTaskIds",TaskService.getInstance().getCollectTaskIdsByTaskId((int)cmsInfo.get("cmsId")));
//
//            net.sf.json.JSONObject taskPar=new net.sf.json.JSONObject();
//            taskPar.put("manager_id", cmsInfo.get("collectId"));
//            taskPar.put("imp_task_name", cmsInfo.get("collectName"));
//            taskPar.put("province", cmsInfo.get("provinceName"));
//            taskPar.put("city", cmsInfo.get("cityName"));
//            taskPar.put("district", cmsInfo.get("blockName"));
//            Object workProperty=cmsInfo.get("workProperty");
//            if(workProperty==null){workProperty="更新";}
//            taskPar.put("job_nature", workProperty);
//            Object workType=cmsInfo.get("workType");
//            if(workType==null){workType="行人导航";}
//            taskPar.put("job_type", workType);
//
//            par.put("taskid", taskPar);
//            log.info("tips2Aumark:"+par);
//
//            FccApi fccApi = (FccApi) ApplicationContextUtil
//                    .getBean("fccApi");
//            fccApi.tips2Aumark(par);
//
//            return JobProgressStatus.RUNNING;
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.SUCCESS);
            return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.FAILURE);
            }
            throw ex;
        } finally {
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
}
