package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.api.fcc.iface.FccApi;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import com.navinfo.dataservice.engine.man.task.TaskService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
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
//            Map<String, Object> cmsInfo = Tips2MarkUtils.getTaskInfo(conn, jobRelation.getItemId());
//            JSONObject parameter=new JSONObject();
//            parameter.put("gdbid", cmsInfo.get("dbId"));
//            DatahubApi datahub = (DatahubApi) ApplicationContextUtil
//                    .getBean("datahubApi");
//            DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
//            parameter.put("au_db_ip", auDb.getDbServer().getIp());
//            parameter.put("au_db_username", auDb.getDbUserName());
//            parameter.put("au_db_password", auDb.getDbUserPasswd());
//            parameter.put("au_db_sid",auDb.getDbServer().getServiceName());
//            parameter.put("au_db_port",auDb.getDbServer().getPort());
//            parameter.put("types","");
//            parameter.put("phaseId",jobProgress.getPhaseId());
//            if(jobRelation.getItemType()== ItemType.PROJECT) {
//                parameter.put("collectTaskIds", TaskService.getInstance().getCollectTaskIdsByTaskId((int) cmsInfo.get("cmsId")));
//            }else{
//                parameter.put("collectTaskIds", new JSONArray());
//            }
//            JSONObject taskPar=new JSONObject();
//            taskPar.put("manager_id", cmsInfo.get("collectId"));
//            taskPar.put("imp_task_name", cmsInfo.get("collectName"));
//            taskPar.put("province", cmsInfo.get("provinceName"));
//            taskPar.put("city", cmsInfo.get("cityName"));
//            taskPar.put("district", cmsInfo.get("blockName"));
//
//            String jobType="中线一体化作业";
//            String jobNature = "更新";
//            int taskType=1;
//            if(jobRelation.getItemType()==ItemType.PROJECT){
//                jobType="快线一体化作业";
//                jobNature="快速更新";
//                taskType=4;
//            }else if(jobRelation.getItemType()==ItemType.SUBTASK){
//                taskType=2;
//            }
//            taskPar.put("job_nature", jobNature);
//            taskPar.put("job_type", jobType);
//            parameter.put("task_type", taskType);
//
//            parameter.put("taskInfo", taskPar);
//            log.info("tips2Aumark:"+parameter);
//
//            FccApi fccApi = (FccApi) ApplicationContextUtil
//                    .getBean("fccApi");
//            fccApi.tips2Aumark(parameter);

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
