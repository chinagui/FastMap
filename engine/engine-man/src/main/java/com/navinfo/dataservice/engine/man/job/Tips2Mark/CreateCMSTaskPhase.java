package com.navinfo.dataservice.engine.man.job.Tips2Mark;

import com.navinfo.dataservice.api.datahub.iface.DatahubApi;
import com.navinfo.dataservice.api.datahub.model.DbInfo;
import com.navinfo.dataservice.bizcommons.datasource.DBConnector;
import com.navinfo.dataservice.commons.config.SystemConfigFactory;
import com.navinfo.dataservice.commons.constant.PropConstant;
import com.navinfo.dataservice.commons.log.LoggerRepos;
import com.navinfo.dataservice.commons.springmvc.ApplicationContextUtil;
import com.navinfo.dataservice.commons.util.ServiceInvokeUtil;
import com.navinfo.dataservice.engine.man.job.JobPhase;
import com.navinfo.dataservice.engine.man.job.bean.InvokeType;
import com.navinfo.dataservice.engine.man.job.bean.ItemType;
import com.navinfo.dataservice.engine.man.job.bean.JobProgressStatus;
import com.navinfo.dataservice.engine.man.job.operator.JobProgressOperator;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangshishuai3966 on 2017/7/6.
 */
public class CreateCMSTaskPhase extends JobPhase {
    private static Logger log = LoggerRepos.getLogger(CreateCMSTaskPhase.class);

    @Override
    public void initInvokeType() {
        this.invokeType = InvokeType.SYNC;
    }

    @Override
    public JobProgressStatus run() throws Exception {
        log.info("CreateCMSTaskPhase start:phaseId "+jobProgress.getPhaseId());
        Connection conn = null;
        JobProgressOperator jobProgressOperator = null;
        try {
            conn = DBConnector.getInstance().getManConnection();
            jobProgressOperator = new JobProgressOperator(conn);
            //更新状态为进行中
            jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.RUNNING);
            conn.commit();

            if (lastJobProgress.getStatus() == JobProgressStatus.NODATA) {
                //如果无数据，不需要创建cms任务
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.SUCCESS);
                return jobProgress.getStatus();
            }

            //业务逻辑
            Map<String, Object> cmsInfo = Tips2MarkUtils.getItemInfo(conn, jobRelation.getItemId(), jobRelation.getItemType());
            JSONObject parameter = new JSONObject();
            DatahubApi datahub = (DatahubApi) ApplicationContextUtil
                    .getBean("datahubApi");
            DbInfo metaDb = datahub.getOnlyDbByType("metaRoad");
            parameter.put("metaIp", metaDb.getDbServer().getIp());
            parameter.put("metaUserName", metaDb.getDbUserName());

            DbInfo auDb = datahub.getOnlyDbByType("gen2Au");
            parameter.put("fieldDbIp", auDb.getDbServer().getIp());
            parameter.put("fieldDbName", auDb.getDbUserName());

            JSONObject taskPar = new JSONObject();
            taskPar.put("taskName", cmsInfo.get("collectName"));
            taskPar.put("fieldTaskId", cmsInfo.get("collectId"));
            taskPar.put("taskId", cmsInfo.get("collectId"));
            taskPar.put("province", cmsInfo.get("provinceName"));
            taskPar.put("city", cmsInfo.get("cityName"));
            taskPar.put("town", cmsInfo.get("blockName"));

            String area = "中线一体化作业";
            String workType = "更新";
            String workSeason = SystemConfigFactory.getSystemConfig().getValue(PropConstant.seasonVersion);
            if (jobRelation.getItemType() == ItemType.PROJECT) {
                area = "快线一体化作业";
                workType = "快速更新";
                workSeason += "FM快速";
            } else {
                workSeason += "FM";
            }
            taskPar.put("workType", workType);
            taskPar.put("area", area);
            taskPar.put("userId", cmsInfo.get("userNickName"));
            taskPar.put("workSeason", workSeason);
            taskPar.put("markTaskType", jobRelation.getItemType().value());
            parameter.put("taskInfo", taskPar);

            String cmsUrl = SystemConfigFactory.getSystemConfig().getValue(PropConstant.cmsUrl);
            Map<String, String> parMap = new HashMap<>();
            parMap.put("parameter", parameter.toString());
            log.info("phaseId:"+jobProgress.getPhaseId()+",cms param:"+parameter.toString());
            jobProgress.setMessage(parameter.toString());
            String result = ServiceInvokeUtil.invoke(cmsUrl, parMap, 10000);
            log.info("phaseId:"+jobProgress.getPhaseId()+",cms result:"+result);
            //result="{success:false, msg:\"没有找到用户名为【fm_meta_all_sp6】元数据库版本信息！\"}";
            jobProgress.setOutParameter(result);
            JSONObject res = null;
            try {
                res = JSONObject.fromObject(result);
            } catch (Exception ex) {
                res = null;
                jobProgress.setStatus(JobProgressStatus.FAILURE);
                jobProgress.setMessage(jobProgress.getMessage() + ex.getMessage());
            }
            if (res != null) {
                boolean success = res.getBoolean("success");
                if (success) {
                    jobProgress.setStatus(JobProgressStatus.SUCCESS);
                } else {
                    log.error("phaseId:"+jobProgress.getPhaseId()+",cms error msg:" + res.get("msg"));
                    jobProgress.setStatus(JobProgressStatus.FAILURE);
                    jobProgress.setOutParameter("cms error:" + res.get("msg").toString());
                }
            }
            jobProgressOperator.updateStatus(jobProgress, jobProgress.getStatus());

            return jobProgress.getStatus();
        } catch (Exception ex) {
            //有异常，更新状态为执行失败
            log.error(ex.getMessage(), ex);
            DbUtils.rollback(conn);
            if (jobProgressOperator != null && jobProgress != null) {
                jobProgress.setStatus(JobProgressStatus.FAILURE);
                jobProgress.setOutParameter(ex.getMessage());
                jobProgressOperator.updateStatus(jobProgress, JobProgressStatus.FAILURE);
            }
            throw ex;
        } finally {
            log.info("CreateCMSTaskPhase end:phaseId "+jobProgress.getPhaseId() + ",status "+jobProgress.getStatus());
            DbUtils.commitAndCloseQuietly(conn);
        }
    }
}
