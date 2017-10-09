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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class Tips2MarkPhase extends JobPhase {
    private static Logger log = LoggerRepos.getLogger(Tips2MarkPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.ASYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception {
        log.info("Tips2MarkPhase start:phaseId "+jobProgress.getPhaseId());
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            //更新状态为进行中
            jobProgressOperator = new JobProgressOperator(conn);
            jobProgress.setStatus(JobProgressStatus.RUNNING);
            jobProgressOperator.updateStatus(jobProgress);
            conn.commit();

            //业务逻辑
            Map<String, Object> cmsInfo = Tips2MarkUtils.getItemInfo(conn, jobRelation.getItemId(), jobRelation.getItemType());
            int status = Integer.valueOf(cmsInfo.get("status").toString());
            int type = Integer.valueOf(cmsInfo.get("type").toString());

            String jobType = "中线一体化作业";
            String jobNature = "更新";
            int taskType = 1;
            switch (jobRelation.getItemType()) {
                case PROJECT:
                    jobType = "快线一体化作业";
                    jobNature = "快速更新";
                    taskType = 4;
                    if (type != 4) {
                        throw new Exception("非快速更新项目不能执行tips转mark");
                    }
                    break;
                case SUBTASK:
                    taskType = 2;
                    if (type != 0) {
                        throw new Exception("非采集子任务不能执行tips转mark");
                    }
                    if (status != 0) {
                        throw new Exception("未关闭的子任务不能执行tips转mark");
                    }
                case TASK:
                    if (type != 0) {
                        throw new Exception("非采集任务不能执行tips转mark");
                    }
                    if (status != 0) {
                        throw new Exception("未关闭的任务不能执行tips转mark");
                    }
                    break;
            }

            JSONObject parameter = new JSONObject();
            parameter.put("gdbid", cmsInfo.get("dbId"));
            DatahubApi datahub = (DatahubApi) ApplicationContextUtil
                    .getBean("datahubApi");
            DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
            parameter.put("au_db_ip", auDb.getDbServer().getIp());
            parameter.put("au_db_username", auDb.getDbUserName());
            parameter.put("au_db_password", auDb.getDbUserPasswd());
            parameter.put("au_db_sid", auDb.getDbServer().getServiceName());
            parameter.put("au_db_port", auDb.getDbServer().getPort());
            //modify by songhe 2017/09/27   其实json的传参内容应该修改一下。。。
            if (jobRelation.getItemType() == ItemType.PROJECT) {
                DbInfo auWeekDb = datahub.getOnlyDbByType("gen2AuWeek");
                parameter.put("au_week_db_ip", auWeekDb.getDbServer().getIp());
                parameter.put("au_week_db_username", auWeekDb.getDbUserName());
                parameter.put("au_week_db_password", auWeekDb.getDbUserPasswd());
                parameter.put("au_week_db_sid", auWeekDb.getDbServer().getServiceName());
                parameter.put("au_week_db_port", auWeekDb.getDbServer().getPort());
            }else{
                parameter.put("au_week_db_ip", "");
                parameter.put("au_week_db_username", "");
                parameter.put("au_week_db_password", "");
                parameter.put("au_week_db_sid", "");
                parameter.put("au_week_db_port", "");
            }
            
            parameter.put("types", "");
            parameter.put("phaseId", jobProgress.getPhaseId());
            if (jobRelation.getItemType() == ItemType.PROJECT) {
                Set<Integer> taskIds = new HashSet<>();
                Object tasks = cmsInfo.get("tasks");
                if (tasks != null) {
                    String[] split = tasks.toString().split(",");
                    for (String task : split) {
                        taskIds.add(Integer.valueOf(task));
                    }
                }
                if (taskIds.size() == 0) {
                    throw new Exception("快线项目没有关闭的采集任务，不能执行tips转mark");
                }
                parameter.put("collectTaskIds", taskIds);
            } else {
                parameter.put("collectTaskIds", new JSONArray());
            }
            JSONObject taskPar = new JSONObject();
            taskPar.put("manager_id", cmsInfo.get("collectId"));
            taskPar.put("imp_task_name", cmsInfo.get("collectName"));
            taskPar.put("province", cmsInfo.get("provinceName"));
            taskPar.put("city", cmsInfo.get("cityName"));
            taskPar.put("district", cmsInfo.get("blockName"));

            taskPar.put("job_nature", jobNature);
            taskPar.put("job_type", jobType);
            parameter.put("task_type", taskType);

            parameter.put("taskInfo", taskPar);
            log.info("tips2mark fccApi:" + parameter);
            jobProgress.setInParameter(parameter.toString());
            jobProgressOperator.updateStatus(jobProgress);

            FccApi fccApi = (FccApi) ApplicationContextUtil
                    .getBean("fccApi");
            fccApi.tips2Aumark(parameter);
            
            //return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ex.getMessage(), ex);
            jobProgress.setStatus(JobProgressStatus.FAILURE);
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                JSONObject out = new JSONObject();
                out.put("errmsg",ex.getMessage());
                jobProgress.setOutParameter(out.toString());
                jobProgressOperator.updateStatus(jobProgress);
                //JobService.getInstance().updateJobProgress(jobProgress.getPhaseId(), jobProgress.getStatus(), jobProgress.getOutParameter());
            }
            //throw ex;
        } finally {
            log.info("Tips2MarkPhase end:phaseId "+jobProgress.getPhaseId()+",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
        return jobProgress.getStatus();
    }
}
